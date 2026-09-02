package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CollectionLogMember
{
	public String player;

	@SerializedName("player_name_with_capitalization")
	public String playerNameWithCapitalization;

	@SerializedName("total_collections_finished")
	public int totalCollectionsFinished;

	// Only populated when the request includes categories=all.
	public List<Integer> items;
}
