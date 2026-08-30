package com.smokescapehub.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class TimeUtil
{
	private static final DateTimeFormatter TEMPLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private TimeUtil()
	{
	}

	// TempleOSRS timestamps are "yyyy-MM-dd HH:mm:ss" server time, which TempleOSRS documents as UTC.
	public static Instant parseTempleDate(String date)
	{
		try
		{
			return LocalDateTime.parse(date, TEMPLE_DATE_FORMAT).atZone(ZoneOffset.UTC).toInstant();
		}
		catch (Exception e)
		{
			return Instant.EPOCH;
		}
	}

	public static Instant parseIso(String iso)
	{
		try
		{
			return Instant.parse(iso);
		}
		catch (Exception e)
		{
			return Instant.EPOCH;
		}
	}

	public static String ago(Instant time)
	{
		return describe(Duration.between(time, Instant.now())) + " ago";
	}

	public static String until(Instant time)
	{
		Duration duration = Duration.between(Instant.now(), time);
		if (duration.isNegative())
		{
			return "any moment";
		}
		return "in " + describe(duration);
	}

	private static String describe(Duration duration)
	{
		long minutes = Math.abs(duration.toMinutes());
		if (minutes < 1)
		{
			return "moments";
		}
		if (minutes < 60)
		{
			return minutes + "m";
		}
		long hours = Math.abs(duration.toHours());
		if (hours < 24)
		{
			return hours + "h";
		}
		long days = Math.abs(duration.toDays());
		return days + "d";
	}

	public static String formatNumber(long value)
	{
		return String.format("%,d", value);
	}

	public static String formatCompact(long value)
	{
		if (value >= 1_000_000_000L)
		{
			return String.format("%.1fb", value / 1_000_000_000.0);
		}
		if (value >= 1_000_000L)
		{
			return String.format("%.1fm", value / 1_000_000.0);
		}
		if (value >= 1_000L)
		{
			return String.format("%.1fk", value / 1_000.0);
		}
		return String.valueOf(value);
	}
}
