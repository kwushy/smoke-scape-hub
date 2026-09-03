package com.smokescapehub.panels;

import com.smokescapehub.temple.CollectionLogMember;
import com.smokescapehub.temple.GroupMemberStats;
import com.smokescapehub.temple.PetItems;
import com.smokescapehub.temple.TempleOsrsClient;
import com.smokescapehub.util.TimeUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.ui.laf.RuneLiteScrollBarUI;

public class LeaderboardsPanel extends JPanel
{
	private static final int TOP_N = 20;

	private final TempleOsrsClient client;
	private final RankedListView petView = new RankedListView();
	private final RankedListView clogView = new RankedListView();
	private final RankedListView ehpView = new RankedListView();
	private final RankedListView ehbView = new RankedListView();

	public LeaderboardsPanel(TempleOsrsClient client)
	{
		this.client = client;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel display = new JPanel();
		display.setBackground(ColorScheme.DARK_GRAY_COLOR);

		MaterialTabGroup subTabs = new MaterialTabGroup(display);
		subTabs.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		subTabs.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		subTabs.setLayout(new GridLayout(1, 4, 2, 0));

		MaterialTab ehpTab = new MaterialTab("EHP", subTabs, scroll(ehpView.root));
		MaterialTab ehbTab = new MaterialTab("EHB", subTabs, scroll(ehbView.root));
		MaterialTab clogTab = new MaterialTab("Clog", subTabs, scroll(clogView.root));
		MaterialTab petTab = new MaterialTab("Pets", subTabs, scroll(petView.root));

		subTabs.addTab(ehpTab);
		subTabs.addTab(ehbTab);
		subTabs.addTab(clogTab);
		subTabs.addTab(petTab);

		add(subTabs, BorderLayout.NORTH);
		add(display, BorderLayout.CENTER);

		subTabs.select(ehpTab);
	}

	public void refresh(String groupId)
	{
		if (groupId == null || groupId.trim().isEmpty())
		{
			String message = "Set a TempleOSRS Group ID in the plugin settings.";
			petView.showStatus(message);
			clogView.showStatus(message);
			ehpView.showStatus(message);
			ehbView.showStatus(message);
			return;
		}

		String id = groupId.trim();

		// Pets are derived from the same collection log fetch (see PetItems) -
		// TempleOSRS's dedicated pet-tracking endpoint has almost no data.
		petView.showStatus("Loading...");
		clogView.showStatus("Loading...");
		client.getCollectionLogLeaderboard(id,
			members -> SwingUtilities.invokeLater(() ->
			{
				populateClog(members);
				populatePets(members);
			}),
			error -> SwingUtilities.invokeLater(() ->
			{
				clogView.showStatus("Failed to load the collection log leaderboard.");
				petView.showStatus("Failed to load pet counts.");
			}));

		ehpView.showStatus("Loading...");
		ehbView.showStatus("Loading...");
		client.getMemberStats(id,
			members -> SwingUtilities.invokeLater(() -> populateEhpEhb(members)),
			error -> SwingUtilities.invokeLater(() ->
			{
				ehpView.showStatus("Failed to load EHP standings.");
				ehbView.showStatus("Failed to load EHB standings.");
			}));
	}

	private void populatePets(List<CollectionLogMember> members)
	{
		List<CollectionLogMember> sorted = (members == null ? Collections.<CollectionLogMember>emptyList() : members).stream()
			.filter(m -> PetItems.countPets(m.items) > 0)
			.sorted(Comparator.comparingInt((CollectionLogMember m) -> PetItems.countPets(m.items)).reversed())
			.limit(TOP_N)
			.collect(Collectors.toList());

		petView.showRows(
			sorted.stream().map(this::clogName).collect(Collectors.toList()),
			sorted.stream().map(m -> {
				int count = PetItems.countPets(m.items);
				return count + (count == 1 ? " pet" : " pets");
			}).collect(Collectors.toList()));
	}

	private void populateClog(List<CollectionLogMember> members)
	{
		List<CollectionLogMember> sorted = (members == null ? Collections.<CollectionLogMember>emptyList() : members).stream()
			.sorted(Comparator.comparingInt((CollectionLogMember m) -> m.totalCollectionsFinished).reversed())
			.limit(TOP_N)
			.collect(Collectors.toList());

		clogView.showRows(
			sorted.stream().map(this::clogName).collect(Collectors.toList()),
			sorted.stream().map(m -> m.totalCollectionsFinished + " items").collect(Collectors.toList()));
	}

	private void populateEhpEhb(List<GroupMemberStats> members)
	{
		List<GroupMemberStats> nonNull = members == null ? Collections.emptyList() : members;

		List<GroupMemberStats> byEhp = nonNull.stream()
			.filter(m -> m.skills != null && m.skills.ehp > 0)
			.sorted(Comparator.comparingDouble((GroupMemberStats m) -> m.skills.ehp).reversed())
			.limit(TOP_N)
			.collect(Collectors.toList());

		ehpView.showRows(
			byEhp.stream().map(this::memberName).collect(Collectors.toList()),
			byEhp.stream().map(m -> TimeUtil.formatNumber(Math.round(m.skills.ehp)) + " EHP").collect(Collectors.toList()));

		List<GroupMemberStats> byEhb = nonNull.stream()
			.filter(m -> m.bosses != null && m.bosses.ehb > 0)
			.sorted(Comparator.comparingDouble((GroupMemberStats m) -> m.bosses.ehb).reversed())
			.limit(TOP_N)
			.collect(Collectors.toList());

		ehbView.showRows(
			byEhb.stream().map(this::memberName).collect(Collectors.toList()),
			byEhb.stream().map(m -> TimeUtil.formatNumber(Math.round(m.bosses.ehb)) + " EHB").collect(Collectors.toList()));
	}

	private String clogName(CollectionLogMember m)
	{
		if (m.playerNameWithCapitalization != null && !m.playerNameWithCapitalization.isEmpty())
		{
			return m.playerNameWithCapitalization;
		}
		return m.player == null ? "Unknown" : m.player;
	}

	private String memberName(GroupMemberStats m)
	{
		if (m.playerNameWithCapitalization != null && !m.playerNameWithCapitalization.isEmpty())
		{
			return m.playerNameWithCapitalization;
		}
		return m.player == null ? "Unknown" : m.player;
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

	private static final class RankedListView
	{
		final JPanel root = new JPanel(new BorderLayout());
		final JPanel listPanel = new JPanel();
		final JLabel statusLabel = new JLabel();

		RankedListView()
		{
			root.setBackground(ColorScheme.DARK_GRAY_COLOR);

			listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
			listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

			statusLabel.setForeground(Color.GRAY);
			statusLabel.setFont(FontManager.getRunescapeFont().deriveFont(14f));
			statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

			root.add(statusLabel, BorderLayout.NORTH);
			root.add(listPanel, BorderLayout.CENTER);
		}

		void showStatus(String text)
		{
			listPanel.removeAll();
			statusLabel.setText(text);
			listPanel.revalidate();
			listPanel.repaint();
		}

		void showRows(List<String> names, List<String> values)
		{
			listPanel.removeAll();

			if (names.isEmpty())
			{
				statusLabel.setText("No data yet.");
				listPanel.revalidate();
				listPanel.repaint();
				return;
			}

			statusLabel.setText("");
			for (int i = 0; i < names.size(); i++)
			{
				listPanel.add(buildRow(i + 1, names.get(i), values.get(i)));
			}

			listPanel.revalidate();
			listPanel.repaint();
		}

		private JPanel buildRow(int rank, String name, String value)
		{
			JPanel row = new JPanel(new BorderLayout());
			row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

			JLabel nameLabel = new JLabel(rank + ".  " + name);
			nameLabel.setForeground(Color.WHITE);
			nameLabel.setFont(FontManager.getRunescapeFont().deriveFont(15f));

			JLabel valueLabel = new JLabel(value);
			valueLabel.setForeground(ColorScheme.BRAND_ORANGE);
			valueLabel.setFont(FontManager.getRunescapeFont().deriveFont(14f));

			row.add(nameLabel, BorderLayout.WEST);
			row.add(valueLabel, BorderLayout.EAST);

			JPanel wrapper = new JPanel(new BorderLayout());
			wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
			wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
			wrapper.add(row, BorderLayout.CENTER);
			wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));

			return wrapper;
		}
	}
}
