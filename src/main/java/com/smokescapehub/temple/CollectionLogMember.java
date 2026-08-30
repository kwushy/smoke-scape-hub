package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;

public class CollectionLogMember
{
	public String player;

	@SerializedName("player_name_with_capitalization")
	public String playerNameWithCapitalization;

	@SerializedName("total_collections_finished")
	public int totalCollectionsFinished;
}
