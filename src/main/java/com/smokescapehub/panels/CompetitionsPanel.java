package com.smokescapehub.panels;

import com.smokescapehub.temple.CompetitionParticipant;
import com.smokescapehub.temple.GroupCompetition;
import com.smokescapehub.temple.TempleOsrsClient;
import com.smokescapehub.util.TimeUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class CompetitionsPanel extends JPanel
{
	private static final long MAX_UPCOMING_SECONDS = 14L * 24 * 60 * 60;
	private static final int COLLAPSED_TOP_N = 5;
	private static final int EXPANDED_TOP_N = 25;

	private final TempleOsrsClient client;
	private final JPanel listPanel = new JPanel();
	private final JLabel statusLabel = new JLabel();

	public CompetitionsPanel(TempleOsrsClient client)
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

		showStatus("Loading competitions...");

		client.getGroupCompetitions(groupId.trim(),
			competitions -> SwingUtilities.invokeLater(() -> populate(competitions)),
			error -> SwingUtilities.invokeLater(() -> showStatus("Failed to load competitions. Check the Group ID.")));
	}

	private void populate(List<GroupCompetition> competitions)
	{
		listPanel.removeAll();

		long nowEpoch = Instant.now().getEpochSecond();

		List<GroupCompetition> relevant = (competitions == null ? Collections.<GroupCompetition>emptyList() : competitions).stream()
			.filter(c -> c.status == 1 || (c.status == 0 && c.startDateUnix - nowEpoch <= MAX_UPCOMING_SECONDS))
			.sorted(Comparator.comparingInt((GroupCompetition c) -> c.status == 1 ? 0 : 1)
				.thenComparingLong(c -> c.startDateUnix))
			.collect(Collectors.toList());

		if (relevant.isEmpty())
		{
			showStatus("No active or upcoming competitions.");
			revalidate();
			repaint();
			return;
		}

		statusLabel.setText("");

		for (GroupCompetition competition : relevant)
		{
			listPanel.add(buildCard(competition));
		}

		revalidate();
		repaint();
	}

	private JPanel buildCard(GroupCompetition competition)
	{
		boolean ongoing = competition.status == 1;

		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		JLabel titleLabel = new JLabel(competition.name == null ? "Competition" : competition.name);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(FontManager.getRunescapeBoldFont().deriveFont(13f));
		titleLabel.setAlignmentX(0f);

		String timing = ongoing
			? "Ends " + TimeUtil.until(Instant.ofEpochSecond(competition.endDateUnix))
			: "Starts " + TimeUtil.until(Instant.ofEpochSecond(competition.startDateUnix));

		JLabel metaLabel = new JLabel(prettySkill(competition.skill) + "  •  " + timing);
		metaLabel.setForeground(Color.LIGHT_GRAY);
		metaLabel.setFont(FontManager.getRunescapeFont().deriveFont(12f));
		metaLabel.setAlignmentX(0f);

		JPanel standings = new JPanel();
		standings.setLayout(new BoxLayout(standings, BoxLayout.Y_AXIS));
		standings.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		standings.setAlignmentX(0f);
		standings.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

		card.add(titleLabel);
		card.add(metaLabel);
		card.add(standings);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
		wrapper.add(card, BorderLayout.CENTER);
		wrapper.setAlignmentX(0f);

		if (ongoing)
		{
			JLabel loading = new JLabel("Loading standings...");
			loading.setForeground(Color.GRAY);
			loading.setFont(FontManager.getRunescapeFont().deriveFont(12f));
			standings.add(loading);

			client.getCompetitionParticipants(competition.id,
				participants -> SwingUtilities.invokeLater(() -> populateStandings(standings, participants)),
				error -> SwingUtilities.invokeLater(() -> populateStandingsError(standings)));
		}

		return wrapper;
	}

	private void populateStandings(JPanel standings, List<CompetitionParticipant> participants)
	{
		List<CompetitionParticipant> sorted = participants == null ? Collections.emptyList() : participants.stream()
			.sorted(Comparator.comparingLong((CompetitionParticipant p) -> p.xpGained).reversed())
			.limit(EXPANDED_TOP_N)
			.collect(Collectors.toList());

		renderStandings(standings, sorted, false);
	}

	private void renderStandings(JPanel standings, List<CompetitionParticipant> sorted, boolean expanded)
	{
		standings.removeAll();

		if (sorted.isEmpty())
		{
			JLabel none = new JLabel("No participants yet.");
			none.setForeground(Color.GRAY);
			none.setFont(FontManager.getRunescapeFont().deriveFont(12f));
			standings.add(none);
		}
		else
		{
			int shown = Math.min(expanded ? EXPANDED_TOP_N : COLLAPSED_TOP_N, sorted.size());

			int rank = 1;
			for (CompetitionParticipant p : sorted.subList(0, shown))
			{
				String name = p.username == null ? "Unknown" : p.username;
				JLabel line = new JLabel(rank + ". " + name + " — " + TimeUtil.formatCompact(p.xpGained));
				line.setForeground(Color.WHITE);
				line.setFont(FontManager.getRunescapeFont().deriveFont(12f));
				line.setAlignmentX(0f);
				standings.add(line);
				rank++;
			}

			if (sorted.size() > COLLAPSED_TOP_N)
			{
				JButton toggle = new JButton(expanded ? "Show less" : "Show more");
				toggle.setFocusPainted(false);
				toggle.setFont(FontManager.getRunescapeFont().deriveFont(11f));
				toggle.setAlignmentX(0f);
				toggle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
				toggle.addActionListener(e -> renderStandings(standings, sorted, !expanded));
				standings.add(toggle);
			}
		}

		standings.revalidate();
		listPanel.revalidate();
		listPanel.repaint();
	}

	private void populateStandingsError(JPanel standings)
	{
		standings.removeAll();
		JLabel err = new JLabel("Couldn't load standings.");
		err.setForeground(Color.GRAY);
		err.setFont(FontManager.getRunescapeFont().deriveFont(12f));
		standings.add(err);
		standings.revalidate();
		listPanel.revalidate();
		listPanel.repaint();
	}

	private String prettySkill(String skill)
	{
		if (skill == null || skill.isEmpty())
		{
			return "Overall";
		}
		String[] words = skill.replace('_', ' ').split(" ");
		StringBuilder sb = new StringBuilder();
		for (String w : words)
		{
			if (w.isEmpty())
			{
				continue;
			}
			if (sb.length() > 0)
			{
				sb.append(' ');
			}
			sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase(Locale.ROOT));
		}
		return sb.toString();
	}

	private void showStatus(String text)
	{
		listPanel.removeAll();
		statusLabel.setText(text);
		revalidate();
		repaint();
	}
}
