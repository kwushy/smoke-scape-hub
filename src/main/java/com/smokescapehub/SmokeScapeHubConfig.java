package com.smokescapehub;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("smokescapehub")
public interface SmokeScapeHubConfig extends Config
{
	@ConfigItem(
		keyName = "chatReminders",
		name = "Chat reminders for competitions",
		description = "Shows countdown reminders in the chatbox before clan competitions start (24h, 6h, 2h, 1h, 30m, start), "
			+ "and a message when a competition is already running the moment you log in.",
		position = 0
	)
	default boolean chatReminders()
	{
		return false;
	}
}
