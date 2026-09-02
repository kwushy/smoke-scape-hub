package com.smokescapehub.reminders;

import com.smokescapehub.SmokeScapeHubConfig;
import com.smokescapehub.temple.GroupCompetition;
import com.smokescapehub.temple.TempleOsrsClient;
import com.smokescapehub.util.TimeUtil;
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

// Fires chatbox reminders for upcoming TempleOSRS group competitions
// (24h/6h/2h/1h/30m/start before), and a "currently running" message on a
// real login (not a world hop). No external data source - this rides on
// the same group_competitions.php data the Comps tab already fetches.
//
// State here is touched from three threads - the OkHttp callback thread
// (competition fetches), the 1-minute tick executor thread, and the client
// thread (GameStateChanged) - so the collections are concurrent and the
// competition list is published via a volatile reference.
public class CompetitionReminderManager
{
	private static final int[] THRESHOLD_MINUTES = {1440, 360, 120, 60, 30, 0};

	private final Client client;
	private final ChatMessageManager chatMessageManager;
	private final TempleOsrsClient templeOsrsClient;
	private final SmokeScapeHubConfig config;

	private final Set<String> firedReminders = ConcurrentHashMap.newKeySet();
	private final Set<Integer> knownCompetitionIds = ConcurrentHashMap.newKeySet();
	private volatile List<GroupCompetition> competitions = Collections.emptyList();
	private volatile GameState previousState = GameState.UNKNOWN;

	@Inject
	public CompetitionReminderManager(Client client, ChatMessageManager chatMessageManager, TempleOsrsClient templeOsrsClient, SmokeScapeHubConfig config)
	{
		this.client = client;
		this.chatMessageManager = chatMessageManager;
		this.templeOsrsClient = templeOsrsClient;
		this.config = config;
	}

	// Call once from Plugin#startUp(), in case the plugin is enabled while
	// already logged in mid-session (no GameStateChanged would fire for that).
	public void init()
	{
		previousState = client.getGameState();
		if (previousState == GameState.LOGGED_IN)
		{
			announceActiveCompetitions();
		}
	}

	public void refresh(String groupId)
	{
		if (groupId == null || groupId.trim().isEmpty())
		{
			return;
		}

		templeOsrsClient.getGroupCompetitions(groupId.trim(), this::onCompetitionsFetched, error -> { });
	}

	private void onCompetitionsFetched(List<GroupCompetition> fetched)
	{
		Instant now = Instant.now();

		for (GroupCompetition competition : fetched)
		{
			if (!knownCompetitionIds.add(competition.id))
			{
				continue;
			}

			// First time seeing this competition: silently mark any threshold
			// already in the past as fired, so we don't dump a burst of
			// "24h/6h/2h ago" reminders the moment it's first fetched.
			long minutesUntilStart = (competition.startDateUnix - now.getEpochSecond()) / 60;
			for (int threshold : THRESHOLD_MINUTES)
			{
				if (minutesUntilStart < threshold)
				{
					firedReminders.add(reminderKey(competition.id, threshold));
				}
			}
		}

		competitions = fetched;
	}

	// Call on a frequent timer (e.g. every minute) - the hourly data refresh
	// is too coarse to reliably hit a 30-minute-before window.
	public void tick()
	{
		if (!config.chatReminders())
		{
			return;
		}

		long nowEpoch = Instant.now().getEpochSecond();

		for (GroupCompetition competition : competitions)
		{
			if (competition.endDateUnix < nowEpoch)
			{
				continue;
			}

			long minutesUntilStart = (competition.startDateUnix - nowEpoch) / 60;

			for (int threshold : THRESHOLD_MINUTES)
			{
				String key = reminderKey(competition.id, threshold);
				if (minutesUntilStart <= threshold && firedReminders.add(key))
				{
					fireReminder(competition, threshold);
				}
			}
		}
	}

	public void onGameStateChanged(GameState newState)
	{
		if (newState == GameState.LOGGED_IN && previousState != GameState.HOPPING)
		{
			announceActiveCompetitions();
		}
		previousState = newState;
	}

	private void announceActiveCompetitions()
	{
		if (!config.chatReminders())
		{
			return;
		}

		long nowEpoch = Instant.now().getEpochSecond();

		for (GroupCompetition competition : competitions)
		{
			if (competition.startDateUnix <= nowEpoch && nowEpoch <= competition.endDateUnix)
			{
				sendMessage("Competition currently running: " + competition.name);
			}
		}
	}

	private void fireReminder(GroupCompetition competition, int thresholdMinutes)
	{
		String message = thresholdMinutes <= 0
			? competition.name + " starts now!"
			: TimeUtil.describeThresholdMinutes(thresholdMinutes) + " until " + competition.name + " starts";
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

	private String reminderKey(int competitionId, int thresholdMinutes)
	{
		return competitionId + ":" + thresholdMinutes;
	}
}
