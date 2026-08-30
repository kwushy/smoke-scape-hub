package com.smokescapehub;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SmokeScapeHubPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SmokeScapeHubPlugin.class);
		RuneLite.main(args);
	}
}
