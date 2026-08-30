package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;

public class GroupAchievement
{
	@SerializedName("Username")
	public String username;

	@SerializedName("Date")
	public String date;

	@SerializedName("Skill")
	public String skill;

	// Either "Level" or "XP" per TempleOSRS docs.
	@SerializedName("Milestone")
	public String milestone;

	// "Skill" or "Pvm". For "Pvm", the xp field actually holds a kill count.
	@SerializedName("Type")
	public String type;

	@SerializedName("Xp")
	public long xp;
}
