package com.smokescapehub.temple;

import java.util.List;

public class CompetitionDetailResponse
{
	public Data data;

	public static class Data
	{
		public List<CompetitionParticipant> participants;
	}
}
