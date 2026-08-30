# Smoke Scape Hub

A RuneLite sidebar plugin for Old School RuneScape clans, built around
[TempleOSRS](https://templeosrs.com) group tracking.

## Features

- **Milestones** — recent clan-wide 99s and boss KC milestones, pulled from
  your TempleOSRS group's achievement feed.
- **Comps** — active and upcoming TempleOSRS group competitions (skill of
  the week, yearly XP competitions, boss competitions, etc.) with live
  top-5 standings.
- **Ranks** — clan leaderboards for pet count, collection log completion,
  EHP and EHB.
- **Discord** — one-click invite link.

This plugin is wired to one clan (Smoke Scape) with no config panel — the
TempleOSRS group ID and Discord invite are hardcoded constants at the top of
[`SmokeScapeHubPanel`](src/main/java/com/smokescapehub/SmokeScapeHubPanel.java).
Data refreshes every 6 hours. To reuse this for a different clan, fork the
repo and change those two constants.

## Data source

This plugin reads from the public [TempleOSRS API](https://templeosrs.com/api_doc.php).
It does not require a TempleOSRS account or API key — just a public
TempleOSRS group set up for your clan.
