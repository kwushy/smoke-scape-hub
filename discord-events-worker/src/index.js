// Cloudflare Worker: proxies Discord's Guild Scheduled Events API into the
// plain JSON shape the Smoke Scape Hub RuneLite plugin's Calendar tab reads.
//
// Required secrets/vars (set in the Cloudflare dashboard, not in this file):
//   DISCORD_BOT_TOKEN - a bot token, bot must be a member of the guild
//   DISCORD_GUILD_ID  - the numeric ID of your Discord server
//
// The plugin only needs: title, date (ISO-8601), description, url.

const SCHEDULED = 1;
const ACTIVE = 2;
const EXTERNAL = 3;

export default {
	async fetch(request, env) {
		const discordResponse = await fetch(
			`https://discord.com/api/v10/guilds/${env.DISCORD_GUILD_ID}/scheduled-events`,
			{
				headers: {
					Authorization: `Bot ${env.DISCORD_BOT_TOKEN}`,
				},
			}
		);

		if (!discordResponse.ok) {
			return new Response(
				JSON.stringify({ error: 'Discord API error', status: discordResponse.status }),
				{ status: 502, headers: { 'content-type': 'application/json' } }
			);
		}

		const discordEvents = await discordResponse.json();

		const events = discordEvents
			.filter((e) => e.status === SCHEDULED || e.status === ACTIVE)
			.map((e) => {
				const location = e.entity_type === EXTERNAL && e.entity_metadata
					? e.entity_metadata.location
					: null;

				const descriptionParts = [];
				if (e.description) descriptionParts.push(e.description);
				if (location) descriptionParts.push(`Location: ${location}`);

				return {
					title: e.name,
					date: e.scheduled_start_time,
					description: descriptionParts.length > 0 ? descriptionParts.join(' — ') : null,
					url: `https://discord.com/events/${e.guild_id}/${e.id}`,
				};
			})
			.sort((a, b) => new Date(a.date) - new Date(b.date));

		return new Response(JSON.stringify(events), {
			headers: {
				'content-type': 'application/json',
				'cache-control': 'public, max-age=300',
			},
		});
	},
};
