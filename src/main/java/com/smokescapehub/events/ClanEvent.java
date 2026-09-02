package com.smokescapehub.events;

public class ClanEvent
{
	public String id;
	public String title;

	// ISO-8601, e.g. "2026-09-06T19:00:00Z"
	public String date;

	// ISO-8601, optional - if absent this event never shows a
	// "currently running" login message, but countdowns still work.
	public String endDate;

	public String description;
	public String url;
}
