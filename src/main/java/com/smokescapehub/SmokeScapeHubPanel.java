package com.smokescapehub;

import com.smokescapehub.calendar.CalendarClient;
import com.smokescapehub.panels.CalendarPanel;
import com.smokescapehub.panels.CompetitionsPanel;
import com.smokescapehub.panels.DiscordPanel;
import com.smokescapehub.panels.LeaderboardsPanel;
import com.smokescapehub.panels.MilestonesPanel;
import com.smokescapehub.temple.TempleOsrsClient;
import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

public class SmokeScapeHubPanel extends PluginPanel
{
	private final MilestonesPanel milestonesPanel;
	private final CompetitionsPanel competitionsPanel;
	private final LeaderboardsPanel leaderboardsPanel;
	private final CalendarPanel calendarPanel;
	private final DiscordPanel discordPanel;

	private SmokeScapeHubConfig config;

	@Inject
	public SmokeScapeHubPanel(TempleOsrsClient templeOsrsClient, CalendarClient calendarClient)
	{
		super(false);

		milestonesPanel = new MilestonesPanel(templeOsrsClient);
		competitionsPanel = new CompetitionsPanel(templeOsrsClient);
		leaderboardsPanel = new LeaderboardsPanel(templeOsrsClient);
		calendarPanel = new CalendarPanel(calendarClient);
		discordPanel = new DiscordPanel(templeOsrsClient);

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel display = new JPanel();
		display.setBackground(ColorScheme.DARK_GRAY_COLOR);

		MaterialTabGroup tabGroup = new MaterialTabGroup(display);
		tabGroup.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		tabGroup.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		MaterialTab milestonesTab = new MaterialTab("Milestones", tabGroup, scroll(milestonesPanel));
		MaterialTab competitionsTab = new MaterialTab("Comps", tabGroup, scroll(competitionsPanel));
		MaterialTab leaderboardsTab = new MaterialTab("Ranks", tabGroup, leaderboardsPanel);
		MaterialTab calendarTab = new MaterialTab("Calendar", tabGroup, scroll(calendarPanel));
		MaterialTab discordTab = new MaterialTab("Discord", tabGroup, scroll(discordPanel));

		tabGroup.addTab(milestonesTab);
		tabGroup.addTab(competitionsTab);
		tabGroup.addTab(leaderboardsTab);
		tabGroup.addTab(calendarTab);
		tabGroup.addTab(discordTab);

		add(tabGroup, BorderLayout.NORTH);
		add(display, BorderLayout.CENTER);

		tabGroup.select(milestonesTab);
	}

	public void init(SmokeScapeHubConfig config)
	{
		this.config = config;
	}

	public void refreshAll()
	{
		if (config == null)
		{
			return;
		}

		milestonesPanel.refresh(config.templeGroupId());
		competitionsPanel.refresh(config.templeGroupId());
		leaderboardsPanel.refresh(config.templeGroupId());
		calendarPanel.refresh(config.calendarJsonUrl());
		discordPanel.refresh(config.templeGroupId(), config.discordInviteUrl());
	}

	private JScrollPane scroll(JPanel content)
	{
		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		return scrollPane;
	}
}
