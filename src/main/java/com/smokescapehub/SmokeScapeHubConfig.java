package com.smokescapehub;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("smokescapehub")
public interface SmokeScapeHubConfig extends Config
{
	@ConfigItem(
		keyName = "templeGroupId",
		name = "TempleOSRS Group ID",
		description = "The numeric group ID from your clan's templeosrs.com group page URL",
		position = 0
	)
	default String templeGroupId()
	{
		return "";
	}

	@ConfigItem(
		keyName = "discordInviteUrl",
		name = "Discord invite link (optional)",
		description = "Leave blank to use the Discord link set on your TempleOSRS group page, or set one here to override it",
		position = 1
	)
	default String discordInviteUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "calendarJsonUrl",
		name = "Calendar JSON URL",
		description = "URL to a JSON file (e.g. a GitHub Gist raw link) listing upcoming clan events",
		position = 2
	)
	default String calendarJsonUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "refreshMinutes",
		name = "Refresh interval (minutes)",
		description = "How often to refresh milestone and competition data",
		position = 3
	)
	default int refreshMinutes()
	{
		return 15;
	}
}
