package com.smokescapehub.events;

import com.smokescapehub.SmokeScapeHubConfig;
import com.smokescapehub.util.TimeUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

// Fires chatbox reminders for upcoming clan events (24h/6h/2h/1h/30m/start
// before), and a "currently running" message on a real login (not a world
// hop). All state here is touched from three different threads - the OkHttp
// callback thread (event fetches), the 1-minute tick executor thread, and
// the client thread (GameStateChanged) - so the collections are concurrent
// and the event list is published via a volatile reference.
public class EventReminderManager
{
	private static final int[] THRESHOLD_MINUTES = {1440, 360, 120, 60, 30, 0};

	private final Client client;
	private final ChatMessageManager chatMessageManager;
	private final EventsClient eventsClient;
	private final SmokeScapeHubConfig config;

	private final Set<String> firedReminders = ConcurrentHashMap.newKeySet();
	private final Set<String> knownEventIds = ConcurrentHashMap.newKeySet();
	private volatile List<ClanEvent> events = Collections.emptyList();
	private volatile GameState previousState = GameState.UNKNOWN;

	@Inject
	public EventReminderManager(Client client, ChatMessageManager chatMessageManager, EventsClient eventsClient, SmokeScapeHubConfig config)
	{
		this.client = client;
		this.chatMessageManager = chatMessageManager;
		this.eventsClient = eventsClient;
		this.config = config;
	}

	// Call once from Plugin#startUp(), after any earlier GameStateChanged
	// subscriptions would have been missed (e.g. plugin enabled mid-session).
	public void init()
	{
		previousState = client.getGameState();
		if (previousState == GameState.LOGGED_IN)
		{
			announceActiveEvents();
		}
	}

	public void refreshEvents(String eventsUrl)
	{
		eventsClient.getEvents(eventsUrl, this::onEventsFetched, error -> { });
	}

	private void onEventsFetched(List<ClanEvent> fetched)
	{
		for (ClanEvent event : fetched)
		{
			if (event.id == null || event.date == null || !knownEventIds.add(event.id))
			{
				continue;
			}

			// First time seeing this event: silently mark any threshold
			// that's already in the past as fired, so we don't dump a burst
			// of "24h/6h/2h ago" reminders the moment it's first fetched.
			long minutesUntilStart = Duration.between(Instant.now(), TimeUtil.parseIso(event.date)).toMinutes();
			for (int threshold : THRESHOLD_MINUTES)
			{
				if (minutesUntilStart < threshold)
				{
					firedReminders.add(reminderKey(event.id, threshold));
				}
			}
		}

		events = fetched;
	}

	// Call on a frequent timer (e.g. every minute) - the hourly data refresh
	// is too coarse to reliably hit a 30-minute-before window.
	public void tick()
	{
		if (!config.chatReminders())
		{
			return;
		}

		Instant now = Instant.now();

		for (ClanEvent event : events)
		{
			if (event.id == null || event.date == null)
			{
				continue;
			}

			long minutesUntilStart = Duration.between(now, TimeUtil.parseIso(event.date)).toMinutes();

			for (int threshold : THRESHOLD_MINUTES)
			{
				String key = reminderKey(event.id, threshold);
				if (minutesUntilStart <= threshold && firedReminders.add(key))
				{
					fireReminder(event, threshold);
				}
			}
		}
	}

	public void onGameStateChanged(GameState newState)
	{
		if (newState == GameState.LOGGED_IN && previousState != GameState.HOPPING)
		{
			announceActiveEvents();
		}
		previousState = newState;
	}

	private void announceActiveEvents()
	{
		if (!config.chatReminders())
		{
			return;
		}

		Instant now = Instant.now();

		for (ClanEvent event : events)
		{
			if (event.endDate == null || event.date == null)
			{
				continue;
			}

			Instant start = TimeUtil.parseIso(event.date);
			Instant end = TimeUtil.parseIso(event.endDate);

			if (!now.isBefore(start) && now.isBefore(end))
			{
				sendMessage("Event currently running: " + event.title);
			}
		}
	}

	private void fireReminder(ClanEvent event, int thresholdMinutes)
	{
		String message = thresholdMinutes <= 0
			? event.title + " starts now!"
			: TimeUtil.describeThresholdMinutes(thresholdMinutes) + " until " + event.title + " starts";
		sendMessage(message);
	}

	private void sendMessage(String text)
	{
		String formatted = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(text)
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(formatted)
			.build());
	}

	private String reminderKey(String eventId, int thresholdMinutes)
	{
		return eventId + ":" + thresholdMinutes;
	}
}
