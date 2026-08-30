package com.smokescapehub.temple;

import java.util.Map;

public class GroupMemberInfoResponse
{
	public Data data;

	public static class Data
	{
		// Keyed by username; do not rely on map iteration order.
		public Map<String, GroupMemberStats> memberlist;
	}
}
