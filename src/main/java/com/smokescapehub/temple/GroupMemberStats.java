package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;

public class GroupMemberStats
{
	public String player;

	@SerializedName("player_name_with_capitalization")
	public String playerNameWithCapitalization;

	public Skills skills;
	public Bosses bosses;

	public static class Skills
	{
		@SerializedName("Ehp")
		public double ehp;
	}

	public static class Bosses
	{
		@SerializedName("Ehb")
		public double ehb;
	}
}
