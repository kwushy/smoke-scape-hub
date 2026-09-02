package com.smokescapehub;

import com.google.inject.Provides;
import com.smokescapehub.reminders.CompetitionReminderManager;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Smoke Scape Hub",
	description = "Clan milestones, competitions, leaderboards and Discord link, powered by TempleOSRS",
	tags = {"clan", "templeosrs", "milestones", "competitions", "discord"}
)
public class SmokeScapeHubPlugin extends Plugin
{
	private static final String TEMPLE_GROUP_ID = "27";
	private static final int REFRESH_HOURS = 1;
	private static final int REMINDER_TICK_MINUTES = 1;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private SmokeScapeHubPanel panel;

	@Inject
	private CompetitionReminderManager reminderManager;

	@Inject
	private ScheduledExecutorService executor;

	private NavigationButton navButton;
	private ScheduledFuture<?> refreshTask;
	private ScheduledFuture<?> reminderTask;

	@Override
	protected void startUp()
	{
		navButton = NavigationButton.builder()
			.tooltip("Smoke Scape Hub")
			.icon(ImageUtil.loadImageResource(getClass(), "icon.png"))
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		reminderManager.init();
		refreshAll();

		refreshTask = executor.scheduleWithFixedDelay(this::refreshAll, REFRESH_HOURS, REFRESH_HOURS, TimeUnit.HOURS);
		reminderTask = executor.scheduleWithFixedDelay(reminderManager::tick, REMINDER_TICK_MINUTES, REMINDER_TICK_MINUTES, TimeUnit.MINUTES);
	}

	@Override
	protected void shutDown()
	{
		if (refreshTask != null)
		{
			refreshTask.cancel(true);
			refreshTask = null;
		}

		if (reminderTask != null)
		{
			reminderTask.cancel(true);
			reminderTask = null;
		}

		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		reminderManager.onGameStateChanged(event.getGameState());
	}

	private void refreshAll()
	{
		panel.refreshAll();
		reminderManager.refresh(TEMPLE_GROUP_ID);
	}

	@Provides
	SmokeScapeHubConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SmokeScapeHubConfig.class);
	}
}
