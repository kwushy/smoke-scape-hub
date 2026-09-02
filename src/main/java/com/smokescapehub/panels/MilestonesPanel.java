package com.smokescapehub.panels;

import com.smokescapehub.temple.GroupAchievement;
import com.smokescapehub.temple.TempleOsrsClient;
import com.smokescapehub.util.TimeUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class MilestonesPanel extends JPanel
{
	private final TempleOsrsClient client;
	private final JPanel listPanel = new JPanel();
	private final JLabel statusLabel = new JLabel();

	public MilestonesPanel(TempleOsrsClient client)
	{
		this.client = client;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		statusLabel.setForeground(Color.GRAY);
		statusLabel.setFont(FontManager.getRunescapeFont().deriveFont(12f));
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

		showStatus("Loading milestones...");

		client.getGroupAchievements(groupId.trim(),
			achievements -> SwingUtilities.invokeLater(() -> populate(achievements)),
			error -> SwingUtilities.invokeLater(() -> showStatus("Failed to load milestones. Check the Group ID.")));
	}

	private void populate(List<GroupAchievement> achievements)
	{
		listPanel.removeAll();

		if (achievements == null || achievements.isEmpty())
		{
			showStatus("No recent milestones found.");
			revalidate();
			repaint();
			return;
		}

		statusLabel.setText("");

		achievements.stream()
			.sorted(Comparator.comparing((GroupAchievement a) -> TimeUtil.parseTempleDate(a.date)).reversed())
			.forEach(a -> listPanel.add(createRow(a)));

		revalidate();
		repaint();
	}

	private JPanel createRow(GroupAchievement achievement)
	{
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

		JLabel nameLabel = new JLabel(achievement.username == null ? "Unknown" : achievement.username);
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setFont(FontManager.getRunescapeBoldFont().deriveFont(13f));
		nameLabel.setAlignmentX(0f);

		String detail = describeMilestone(achievement) + "  •  " + TimeUtil.ago(TimeUtil.parseTempleDate(achievement.date));
		JLabel detailLabel = new JLabel(detail);
		detailLabel.setForeground(Color.LIGHT_GRAY);
		detailLabel.setFont(FontManager.getRunescapeFont().deriveFont(12f));
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
}
