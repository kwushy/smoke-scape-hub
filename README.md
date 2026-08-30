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
- **Discord** — one-click invite link, pulled automatically from your
  TempleOSRS group's Discord link, or overridden manually.

## Configuration

Open the plugin's config panel in RuneLite and set:

| Setting | Description |
|---|---|
| TempleOSRS Group ID | The numeric ID from your clan's `templeosrs.com/groups/<id>/...` URL |
| Discord invite link *(optional)* | Overrides the Discord link from your TempleOSRS group page |
| Refresh interval | How often (in minutes) to refresh milestones, competitions and ranks |

## Data source

This plugin reads from the public [TempleOSRS API](https://templeosrs.com/api_doc.php).
It does not require a TempleOSRS account or API key — just a public
TempleOSRS group set up for your clan.
