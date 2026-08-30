package com.smokescapehub.panels;

import com.smokescapehub.temple.TempleOsrsClient;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.LinkBrowser;

public class DiscordPanel extends JPanel
{
	private final TempleOsrsClient client;
	private final JLabel statusLabel = new JLabel();
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

		JLabel title = new JLabel("Join the clan Discord");
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
		title.setAlignmentX(0.5f);

		statusLabel.setForeground(Color.LIGHT_GRAY);
		statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
		statusLabel.setAlignmentX(0.5f);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 12, 0));

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

		content.add(title);
		content.add(statusLabel);
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
