package com.smokescapehub.panels;

import com.smokescapehub.temple.TempleOsrsClient;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

public class DiscordPanel extends JPanel
{
	private final TempleOsrsClient client;
	private final JLabel statusLabel = new JLabel();
	private final JLabel linkLabel = new JLabel();
	private final JButton joinButton = new JButton("Open Discord Invite");
	private String inviteUrl = "";

	public DiscordPanel(TempleOsrsClient client)
	{
		this.client = client;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

		JLabel intro = new JLabel("<html><body style='width: 190px'>"
			+ "Smoke Scape is an established Social clan but we do it all!"
			+ "<br><br>"
			+ "&bull; Clan events<br>"
			+ "&bull; Weekly competitions<br>"
			+ "&bull; PVM and Raids<br>"
			+ "&bull; Mass Minigames<br>"
			+ "&bull; Active discord"
			+ "<br><br>"
			+ "<b>Join Today!</b><br>"
			+ "Clan Chat: Smoke Scape"
			+ "</body></html>");
		intro.setForeground(Color.LIGHT_GRAY);
		intro.setFont(FontManager.getRunescapeFont().deriveFont(13f));
		intro.setAlignmentX(0.5f);
		intro.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

		JLabel title = new JLabel("Join the clan Discord");
		title.setForeground(Color.WHITE);
		title.setFont(FontManager.getRunescapeBoldFont().deriveFont(14f));
		title.setAlignmentX(0.5f);

		statusLabel.setForeground(Color.LIGHT_GRAY);
		statusLabel.setFont(FontManager.getRunescapeFont().deriveFont(12f));
		statusLabel.setAlignmentX(0.5f);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));

		linkLabel.setForeground(ColorScheme.BRAND_ORANGE);
		linkLabel.setFont(FontManager.getRunescapeFont().deriveFont(12f));
		linkLabel.setAlignmentX(0.5f);
		linkLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
		linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		linkLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (!inviteUrl.isEmpty())
				{
					LinkBrowser.browse(inviteUrl);
				}
			}
		});

		joinButton.setAlignmentX(0.5f);
		joinButton.setFocusPainted(false);
		joinButton.setEnabled(false);
		joinButton.addActionListener(e ->
		{
			if (!inviteUrl.isEmpty())
			{
				LinkBrowser.browse(inviteUrl);
			}
		});

		content.add(intro);
		content.add(title);
		content.add(statusLabel);
		content.add(linkLabel);
		content.add(joinButton);

		add(content, BorderLayout.NORTH);
	}

	public void refresh(String groupId, String manualInviteUrl)
	{
		String manual = manualInviteUrl == null ? "" : manualInviteUrl.trim();
		if (!manual.isEmpty())
		{
			setInvite(manual);
			return;
		}

		if (groupId == null || groupId.trim().isEmpty())
		{
			setInvite("");
			statusLabel.setText("Set a TempleOSRS Group ID (or a Discord URL) in settings.");
			return;
		}

		statusLabel.setText("Looking up the clan's Discord link...");

		client.getGroupInfo(groupId.trim(),
			info -> SwingUtilities.invokeLater(() ->
			{
				String link = info != null ? info.discordLink : null;
				setInvite(normalizeDiscordLink(link));
				if (inviteUrl.isEmpty())
				{
					statusLabel.setText("No Discord link set on this TempleOSRS group.");
				}
			}),
			error -> SwingUtilities.invokeLater(() -> statusLabel.setText("Couldn't look up the Discord link.")));
	}

	private void setInvite(String url)
	{
		this.inviteUrl = url == null ? "" : url;
		boolean has = !inviteUrl.isEmpty();
		joinButton.setEnabled(has);
		linkLabel.setText(has ? inviteUrl : "");
		if (has)
		{
			statusLabel.setText("Click below to open the invite link.");
		}
	}

	private String normalizeDiscordLink(String link)
	{
		if (link == null || link.trim().isEmpty())
		{
			return "";
		}
		String trimmed = link.trim();
		if (trimmed.startsWith("http://") || trimmed.startsWith("https://"))
		{
			return trimmed;
		}
		return "https://discord.gg/" + trimmed;
	}
}
