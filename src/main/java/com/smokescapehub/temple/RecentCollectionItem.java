package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;

public class RecentCollectionItem
{
	public int id;
	public String name;
	public String player;

	@SerializedName("player_name_with_capitalization")
	public String playerNameWithCapitalization;

	@SerializedName("date_unix")
	public long dateUnix;

	@SerializedName("notable_item")
	public boolean notableItem;
}
