package com.smokescapehub.temple;

import java.util.List;

public class GroupCollectionLogResponse
{
	public Data data;

	public static class Data
	{
		public List<CollectionLogMember> members;
	}
}
