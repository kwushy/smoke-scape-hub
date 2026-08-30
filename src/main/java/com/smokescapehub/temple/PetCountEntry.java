package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;

public class PetCountEntry
{
	public String player;

	@SerializedName("pet_count")
	public int petCount;
}
