package com.smokescapehub.temple;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Item IDs for every OSRS pet, per TempleOSRS's own pet reference
// (pets/hours.php) cross-referenced against its collection log item names
// (collection-log/items.php). TempleOSRS's dedicated pet-tracking endpoint
// (pets/pet_count.php) turned out to have almost no data for most clans -
// pet ownership is derived here from collection log data instead, which is
// already synced for far more members. A handful of pet name variants
// (game mode variants like "(CM)"/"(HM)", or hard vs normal mode) share a
// single underlying item id with their base pet, which is expected.
//
// Needs a manual update if Jagex adds a new pet - a rare, simple change.
public final class PetItems
{
	private static final int[] IDS = {
		11995, 12643, 12644, 12645, 12646, 12647, 12648, 12649, 12650, 12651, 12652, 12653,
		12655, 12703, 12816, 12921, 13071, 13177, 13178, 13179, 13181, 13225, 13247, 13262,
		13320, 13321, 13322, 13324, 19730, 20659, 20661, 20663, 20665, 20693, 20851, 21273,
		21291, 21509, 21748, 21992, 22473, 22746, 23495, 23757, 23760, 24491, 25348, 25602,
		26348, 26901, 27352, 27590, 28246, 28248, 28250, 28252, 28801, 28960, 28962, 29836,
		30152, 30154, 30622, 30888, 31130, 31283, 31285, 33124,
	};

	public static final Set<Integer> ITEM_IDS;

	static
	{
		Set<Integer> set = new HashSet<>();
		for (int id : IDS)
		{
			set.add(id);
		}
		ITEM_IDS = Collections.unmodifiableSet(set);
	}

	private PetItems()
	{
	}

	public static int countPets(List<Integer> itemIds)
	{
		if (itemIds == null)
		{
			return 0;
		}
		int count = 0;
		for (Integer id : itemIds)
		{
			if (id != null && ITEM_IDS.contains(id))
			{
				count++;
			}
		}
		return count;
	}
}
