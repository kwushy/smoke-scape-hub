package com.smokescapehub;

import com.smokescapehub.panels.CompetitionsPanel;
import com.smokescapehub.panels.DiscordPanel;
import com.smokescapehub.panels.LeaderboardsPanel;
import com.smokescapehub.panels.MilestonesPanel;
import com.smokescapehub.temple.TempleOsrsClient;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.ui.laf.RuneLiteScrollBarUI;

public class SmokeScapeHubPanel extends PluginPanel
{
	private static final String TEMPLE_GROUP_ID = "27";
	private static final String DISCORD_INVITE_URL = "https://discord.gg/nVK6tbAHSa";

	private final MilestonesPanel milestonesPanel;
	private final CompetitionsPanel competitionsPanel;
	private final LeaderboardsPanel leaderboardsPanel;
	private final DiscordPanel discordPanel;

	@Inject
	public SmokeScapeHubPanel(TempleOsrsClient templeOsrsClient)
	{
		super(false);

		milestonesPanel = new MilestonesPanel(templeOsrsClient);
		competitionsPanel = new CompetitionsPanel(templeOsrsClient);
		leaderboardsPanel = new LeaderboardsPanel(templeOsrsClient);
		discordPanel = new DiscordPanel(templeOsrsClient);

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel display = new JPanel();
		display.setBackground(ColorScheme.DARK_GRAY_COLOR);

		MaterialTabGroup tabGroup = new MaterialTabGroup(display);
		tabGroup.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		tabGroup.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		// MaterialTabGroup's own FlowLayout wraps overflowing tabs onto a
		// row that never gets shown in this panel's ~225px width - a fixed
		// grid guarantees every tab is always visible.
		tabGroup.setLayout(new GridLayout(2, 2, 2, 2));

		MaterialTab milestonesTab = new MaterialTab("Milestones", tabGroup, scroll(milestonesPanel));
		MaterialTab competitionsTab = new MaterialTab("Comps", tabGroup, scroll(competitionsPanel));
		MaterialTab leaderboardsTab = new MaterialTab("Leaderboards", tabGroup, leaderboardsPanel);
		MaterialTab discordTab = new MaterialTab("Info", tabGroup, scroll(discordPanel));

		tabGroup.addTab(milestonesTab);
		tabGroup.addTab(competitionsTab);
		tabGroup.addTab(leaderboardsTab);
		tabGroup.addTab(discordTab);

		add(tabGroup, BorderLayout.NORTH);
		add(display, BorderLayout.CENTER);

		tabGroup.select(milestonesTab);
	}

	public void refreshAll()
	{
		milestonesPanel.refresh(TEMPLE_GROUP_ID);
		competitionsPanel.refresh(TEMPLE_GROUP_ID);
		leaderboardsPanel.refresh(TEMPLE_GROUP_ID);
		discordPanel.refresh(TEMPLE_GROUP_ID, DISCORD_INVITE_URL);
	}

	private JScrollPane scroll(JPanel content)
	{
		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUI(new RuneLiteScrollBarUI());
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(9, 0));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		return scrollPane;
	}
}
