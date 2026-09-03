package com.smokescapehub.panels;

import com.smokescapehub.temple.GroupAchievement;
import com.smokescapehub.temple.RecentCollectionItem;
import com.smokescapehub.temple.TempleOsrsClient;
import com.smokescapehub.util.TimeUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

// Merges two TempleOSRS feeds into one "Recent" list, newest first:
// group achievements (99s, boss KC milestones) and notable recent
// collection log unlocks (pets, rare clue items, etc). The two calls
// finish independently, so each just updates its own field and
// re-renders the combined, sorted result.
public class MilestonesPanel extends JPanel
{
	private static final int MAX_ENTRIES = 30;

	private final TempleOsrsClient client;
	private final JPanel listPanel = new JPanel();
	private final JLabel statusLabel = new JLabel();

	private List<GroupAchievement> latestAchievements = Collections.emptyList();
	private List<RecentCollectionItem> latestRecentItems = Collections.emptyList();
	private boolean achievementsLoaded;
	private boolean recentItemsLoaded;

	public MilestonesPanel(TempleOsrsClient client)
	{
		this.client = client;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		statusLabel.setForeground(Color.GRAY);
		statusLabel.setFont(FontManager.getRunescapeFont().deriveFont(14f));
		statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		add(statusLabel, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
	}

	public void refresh(String groupId)
	{
		if (groupId == null || groupId.trim().isEmpty())
		{
			showStatus("Set a TempleOSRS Group ID in the plugin settings.");
			return;
		}

		achievementsLoaded = false;
		recentItemsLoaded = false;
		showStatus("Loading...");

		String id = groupId.trim();

		client.getGroupAchievements(id,
			achievements -> SwingUtilities.invokeLater(() ->
			{
				latestAchievements = achievements != null ? achievements : Collections.emptyList();
				achievementsLoaded = true;
				render();
			}),
			error -> SwingUtilities.invokeLater(() ->
			{
				achievementsLoaded = true;
				render();
			}));

		client.getGroupRecentItems(id,
			items -> SwingUtilities.invokeLater(() ->
			{
				latestRecentItems = items != null ? items : Collections.emptyList();
				recentItemsLoaded = true;
				render();
			}),
			error -> SwingUtilities.invokeLater(() ->
			{
				recentItemsLoaded = true;
				render();
			}));
	}

	private void render()
	{
		List<Entry> entries = new ArrayList<>();

		for (GroupAchievement a : latestAchievements)
		{
			String name = a.username == null ? "Unknown" : a.username;
			entries.add(new Entry(TimeUtil.parseTempleDate(a.date), name, describeMilestone(a)));
		}

		for (RecentCollectionItem item : latestRecentItems)
		{
			String name = item.playerNameWithCapitalization != null && !item.playerNameWithCapitalization.isEmpty()
				? item.playerNameWithCapitalization
				: (item.player == null ? "Unknown" : item.player);
			entries.add(new Entry(Instant.ofEpochSecond(item.dateUnix), name, "Collection log: " + item.name));
		}

		if (entries.isEmpty())
		{
			if (achievementsLoaded && recentItemsLoaded)
			{
				showStatus("No recent activity found.");
			}
			return;
		}

		listPanel.removeAll();
		statusLabel.setText("");

		entries.sort(Comparator.comparing((Entry e) -> e.time).reversed());
		entries.stream().limit(MAX_ENTRIES).forEach(e -> listPanel.add(createRow(e)));

		revalidate();
		repaint();
	}

	private JPanel createRow(Entry entry)
	{
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

		JLabel nameLabel = new JLabel(entry.username);
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setFont(FontManager.getRunescapeBoldFont().deriveFont(15f));
		nameLabel.setAlignmentX(0f);

		String detail = entry.description + "  •  " + TimeUtil.ago(entry.time);
		JLabel detailLabel = new JLabel(detail);
		detailLabel.setForeground(Color.LIGHT_GRAY);
		detailLabel.setFont(FontManager.getRunescapeFont().deriveFont(14f));
		detailLabel.setAlignmentX(0f);

		row.add(nameLabel);
		row.add(detailLabel);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		wrapper.add(row, BorderLayout.CENTER);
		wrapper.setAlignmentX(0f);
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));

		return wrapper;
	}

	private String describeMilestone(GroupAchievement achievement)
	{
		String skill = achievement.skill == null ? "Unknown" : achievement.skill;

		if ("Pvm".equalsIgnoreCase(achievement.type))
		{
			return skill + " — " + TimeUtil.formatNumber(achievement.xp) + " KC";
		}

		if ("Level".equalsIgnoreCase(achievement.milestone))
		{
			return skill + " level " + achievement.xp;
		}

		return skill + " — " + TimeUtil.formatCompact(achievement.xp) + " XP";
	}

	private void showStatus(String text)
	{
		listPanel.removeAll();
		statusLabel.setText(text);
		revalidate();
		repaint();
	}

	private static final class Entry
	{
		final Instant time;
		final String username;
		final String description;

		Entry(Instant time, String username, String description)
		{
			this.time = time;
			this.username = username;
			this.description = description;
		}
	}
}
