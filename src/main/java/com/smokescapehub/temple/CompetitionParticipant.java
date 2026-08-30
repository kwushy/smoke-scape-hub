package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;

public class CompetitionParticipant
{
	public String username;

	@SerializedName("xp_gained")
	public long xpGained;

	@SerializedName("start_level")
	public int startLevel;

	@SerializedName("current_level")
	public int currentLevel;
}
