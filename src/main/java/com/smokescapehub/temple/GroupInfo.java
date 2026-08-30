package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;

public class GroupInfo
{
	public int id;
	public String name;

	@SerializedName("discord_link")
	public String discordLink;

	@SerializedName("member_count")
	public int memberCount;
}
