package com.smokescapehub.temple;

import java.util.Map;

public class PetCountResponse
{
	// Keyed by rank (as a string); do not rely on map iteration order.
	public Map<String, PetCountEntry> data;
}
