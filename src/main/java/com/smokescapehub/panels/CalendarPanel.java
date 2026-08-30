package com.smokescapehub.panels;

import com.smokescapehub.calendar.CalendarClient;
import com.smokescapehub.calendar.CalendarEvent;
import com.smokescapehub.util.TimeUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.LinkBrowser;

public class CalendarPanel extends JPanel
{
	private static final DateTimeFormatter EVENT_DATE_FORMAT = DateTimeFormatter
		.ofPattern("EEE, MMM d 'at' HH:mm 'UTC'")
		.withZone(ZoneOffset.UTC);

	private final CalendarClient client;
	private final JPanel listPanel = new JPanel();
	private final JLabel statusLabel = new JLabel();

	public CalendarPanel(CalendarClient client)
	{
		this.client = client;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		statusLabel.setForeground(Color.GRAY);
		statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
		statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		add(statusLabel, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
	}

	public void refresh(String url)
	{
		if (url == null || url.trim().isEmpty())
		{
			showStatus("Set a Calendar JSON URL in the plugin settings to show clan events here.");
			return;
		}

		showStatus("Loading events...");

		client.getEvents(url,
			events -> SwingUtilities.invokeLater(() -> populate(events)),
			error -> SwingUtilities.invokeLater(() -> showStatus("Failed to load the calendar.")));
	}

	private void populate(List<CalendarEvent> events)
	{
		listPanel.removeAll();

		Instant cutoff = Instant.now().minus(Duration.ofHours(6));

		List<CalendarEvent> upcoming = (events == null ? Collections.<CalendarEvent>emptyList() : events).stream()
			.filter(e -> e.date != null)
			.filter(e -> !TimeUtil.parseIso(e.date).isBefore(cutoff))
			.sorted(Comparator.comparing(e -> TimeUtil.parseIso(e.date)))
			.collect(Collectors.toList());

		if (upcoming.isEmpty())
		{
			showStatus("No upcoming events.");
			revalidate();
			repaint();
			return;
		}

		statusLabel.setText("");

		boolean first = true;
		for (CalendarEvent event : upcoming)
		{
			listPanel.add(createRow(event, first));
			first = false;
		}

		revalidate();
		repaint();
	}

	private JPanel createRow(CalendarEvent event, boolean highlight)
	{
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setBackground(highlight ? ColorScheme.DARK_GRAY_HOVER_COLOR : ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(highlight
			? BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 3, 0, 0, ColorScheme.BRAND_ORANGE),
				BorderFactory.createEmptyBorder(6, 6, 6, 8))
			: BorderFactory.createEmptyBorder(6, 9, 6, 8));

		Instant time = TimeUtil.parseIso(event.date);

		JLabel titleLabel = new JLabel(event.title == null ? "Event" : event.title);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
		titleLabel.setAlignmentX(0f);

		JLabel timeLabel = new JLabel(EVENT_DATE_FORMAT.format(time) + "  •  " + TimeUtil.until(time));
		timeLabel.setForeground(Color.LIGHT_GRAY);
		timeLabel.setFont(timeLabel.getFont().deriveFont(11f));
		timeLabel.setAlignmentX(0f);

		row.add(titleLabel);
		row.add(timeLabel);

		if (event.description != null && !event.description.trim().isEmpty())
		{
			JLabel descLabel = new JLabel("<html><body style='width: 170px'>" + escapeHtml(event.description) + "</body></html>");
			descLabel.setForeground(Color.LIGHT_GRAY);
			descLabel.setFont(descLabel.getFont().deriveFont(11f));
			descLabel.setAlignmentX(0f);
			descLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
			row.add(descLabel);
		}

		if (event.url != null && !event.url.trim().isEmpty())
		{
			JLabel linkLabel = new JLabel("More info");
			linkLabel.setForeground(ColorScheme.BRAND_ORANGE);
			linkLabel.setFont(linkLabel.getFont().deriveFont(11f));
			linkLabel.setAlignmentX(0f);
			linkLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
			linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			String url = event.url.trim();
			linkLabel.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					LinkBrowser.browse(url);
				}
			});
			row.add(linkLabel);
		}

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		wrapper.add(row, BorderLayout.CENTER);
		wrapper.setAlignmentX(0f);
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));

		return wrapper;
	}

	private String escapeHtml(String text)
	{
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private void showStatus(String text)
	{
		listPanel.removeAll();
		statusLabel.setText(text);
		revalidate();
		repaint();
	}
}
