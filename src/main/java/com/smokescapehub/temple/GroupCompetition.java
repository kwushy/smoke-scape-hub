package com.smokescapehub.temple;

import com.google.gson.annotations.SerializedName;

public class GroupCompetition
{
	public int id;
	public String name;
	public String skill;

	@SerializedName("start_date_unix")
	public long startDateUnix;

	@SerializedName("end_date_unix")
	public long endDateUnix;

	// 0 = Upcoming, 1 = In progress, 2 = Finished
	public int status;

	@SerializedName("status_text")
	public String statusText;

	@SerializedName("participant_count")
	public int participantCount;
}
