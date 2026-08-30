package com.smokescapehub;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
	name = "Smoke Scape Hub",
	description = "Clan milestones, competitions, calendar and Discord link, powered by TempleOSRS",
	tags = {"clan", "templeosrs", "milestones", "competitions", "discord"}
)
public class SmokeScapeHubPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private SmokeScapeHubConfig config;

	@Inject
	private SmokeScapeHubPanel panel;

	@Inject
	private ScheduledExecutorService executor;

	private NavigationButton navButton;
	private ScheduledFuture<?> refreshTask;

	@Override
	protected void startUp()
	{
		panel.init(config);

		navButton = NavigationButton.builder()
			.tooltip("Smoke Scape Hub")
			.icon(createIcon())
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		panel.refreshAll();

		long period = Math.max(1, config.refreshMinutes());
		refreshTask = executor.scheduleWithFixedDelay(panel::refreshAll, period, period, TimeUnit.MINUTES);
	}

	@Override
	protected void shutDown()
	{
		if (refreshTask != null)
		{
			refreshTask.cancel(true);
			refreshTask = null;
		}

		clientToolbar.removeNavigation(navButton);
	}

	@Provides
	SmokeScapeHubConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SmokeScapeHubConfig.class);
	}

	private BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(70, 130, 180));
		g.fillRoundRect(0, 0, 16, 16, 4, 4);
		g.setColor(Color.WHITE);
		g.setFont(g.getFont().deriveFont(Font.BOLD, 9f));
		g.drawString("S", 5, 12);
		g.dispose();
		return image;
	}
}
