// Cloudflare Worker: token-gated clan events store for the Smoke Scape Hub
// RuneLite plugin. No Gist, no Discord bot - events live in a Workers KV
// key as a JSON array. Anyone can read; only whoever has EVENTS_WRITE_TOKEN
// (an "event mod") can add or remove events, via the admin page at "/".
//
// Required (set in the Cloudflare dashboard, not in this file):
//   EVENTS_KV          - a KV namespace binding (see wrangler.toml)
//   EVENTS_WRITE_TOKEN  - a secret string; whoever has it can add/delete events
//
// Event shape read by the plugin: { id, title, date, endDate, description, url }
//   date / endDate are ISO-8601 (e.g. "2026-09-06T19:00:00Z"). endDate is
//   optional - if omitted, the event won't trigger the "currently running"
//   login message, but countdown reminders still work fine.

const EVENTS_KEY = 'events';

async function readEvents(env) {
	const raw = await env.EVENTS_KV.get(EVENTS_KEY);
	return raw ? JSON.parse(raw) : [];
}

async function writeEvents(env, events) {
	await env.EVENTS_KV.put(EVENTS_KEY, JSON.stringify(events));
}

function isAuthorized(request, env) {
	const token = request.headers.get('X-Events-Token');
	return !!token && token === env.EVENTS_WRITE_TOKEN;
}

function json(data, status = 200) {
	return new Response(JSON.stringify(data), {
		status,
		headers: { 'content-type': 'application/json' },
	});
}

export default {
	async fetch(request, env) {
		const url = new URL(request.url);

		if (url.pathname === '/' && request.method === 'GET') {
			return new Response(ADMIN_PAGE, { headers: { 'content-type': 'text/html' } });
		}

		if (url.pathname === '/events' && request.method === 'GET') {
			const events = await readEvents(env);
			events.sort((a, b) => new Date(a.date) - new Date(b.date));
			return json(events);
		}

		if (url.pathname === '/events' && request.method === 'POST') {
			if (!isAuthorized(request, env)) {
				return json({ error: 'Invalid or missing token' }, 401);
			}

			let body;
			try {
				body = await request.json();
			} catch (e) {
				return json({ error: 'Invalid JSON body' }, 400);
			}

			if (!body.title || !body.date) {
				return json({ error: 'title and date are required' }, 400);
			}

			const event = {
				id: crypto.randomUUID(),
				title: String(body.title),
				date: String(body.date),
				endDate: body.endDate ? String(body.endDate) : null,
				description: body.description ? String(body.description) : null,
				url: body.url ? String(body.url) : null,
			};

			const events = await readEvents(env);
			events.push(event);
			await writeEvents(env, events);

			return json(event, 201);
		}

		if (url.pathname === '/events' && request.method === 'DELETE') {
			if (!isAuthorized(request, env)) {
				return json({ error: 'Invalid or missing token' }, 401);
			}

			const id = url.searchParams.get('id');
			if (!id) {
				return json({ error: 'id query param is required' }, 400);
			}

			const events = await readEvents(env);
			const filtered = events.filter((e) => e.id !== id);
			await writeEvents(env, filtered);

			return json({ deleted: events.length !== filtered.length });
		}

		return new Response('Not found', { status: 404 });
	},
};

const ADMIN_PAGE = `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Smoke Scape Hub - Events</title>
<style>
	body { font-family: sans-serif; max-width: 640px; margin: 40px auto; padding: 0 16px; background: #1e1e1e; color: #ddd; }
	h1 { font-size: 20px; }
	label { display: block; margin-top: 12px; font-size: 13px; color: #aaa; }
	input, textarea { width: 100%; box-sizing: border-box; padding: 8px; margin-top: 4px; background: #2b2b2b; border: 1px solid #444; color: #eee; border-radius: 4px; font-family: inherit; }
	button { margin-top: 16px; padding: 10px 16px; background: #c77d1a; border: none; color: #fff; border-radius: 4px; cursor: pointer; font-weight: bold; }
	button.delete { background: #a33; padding: 4px 10px; font-weight: normal; margin: 0; }
	#status { margin-top: 12px; font-size: 13px; }
	.event { border: 1px solid #333; border-radius: 6px; padding: 10px 12px; margin-top: 10px; display: flex; justify-content: space-between; align-items: center; }
	.event div { font-size: 13px; }
	.event .title { font-weight: bold; font-size: 14px; }
	.event .meta { color: #999; margin-top: 2px; }
</style>
</head>
<body>
	<h1>Smoke Scape Hub - Add Clan Event</h1>

	<label>Event mod token</label>
	<input id="token" type="password" placeholder="paste your token here">

	<label>Title</label>
	<input id="title" type="text" placeholder="Vorkath Mass">

	<label>Start (your local time)</label>
	<input id="date" type="datetime-local">

	<label>End (optional, your local time)</label>
	<input id="endDate" type="datetime-local">

	<label>Description (optional)</label>
	<textarea id="description" rows="2" placeholder="Bring your own supplies"></textarea>

	<label>URL (optional)</label>
	<input id="url" type="text" placeholder="https://discord.gg/...">

	<button onclick="addEvent()">Add Event</button>
	<div id="status"></div>

	<h1>Current Events</h1>
	<div id="list">Loading...</div>

<script>
function toIso(localValue) {
	if (!localValue) return null;
	return new Date(localValue).toISOString();
}

async function loadEvents() {
	const res = await fetch('/events');
	const events = await res.json();
	const list = document.getElementById('list');
	if (events.length === 0) {
		list.innerHTML = '<p style="color:#888">No events yet.</p>';
		return;
	}
	list.innerHTML = events.map(e => \`
		<div class="event">
			<div>
				<div class="title">\${e.title}</div>
				<div class="meta">\${new Date(e.date).toLocaleString()}\${e.endDate ? ' - ' + new Date(e.endDate).toLocaleString() : ''}</div>
			</div>
			<button class="delete" onclick="deleteEvent('\${e.id}')">Delete</button>
		</div>
	\`).join('');
}

async function addEvent() {
	const token = document.getElementById('token').value;
	const status = document.getElementById('status');
	const body = {
		title: document.getElementById('title').value,
		date: toIso(document.getElementById('date').value),
		endDate: toIso(document.getElementById('endDate').value),
		description: document.getElementById('description').value || null,
		url: document.getElementById('url').value || null,
	};

	if (!body.title || !body.date) {
		status.textContent = 'Title and start time are required.';
		return;
	}

	const res = await fetch('/events', {
		method: 'POST',
		headers: { 'content-type': 'application/json', 'X-Events-Token': token },
		body: JSON.stringify(body),
	});

	if (res.ok) {
		status.textContent = 'Added.';
		document.getElementById('title').value = '';
		document.getElementById('date').value = '';
		document.getElementById('endDate').value = '';
		document.getElementById('description').value = '';
		document.getElementById('url').value = '';
		loadEvents();
	} else {
		const err = await res.json();
		status.textContent = 'Error: ' + (err.error || res.status);
	}
}

async function deleteEvent(id) {
	const token = document.getElementById('token').value;
	if (!token) {
		document.getElementById('status').textContent = 'Enter your token first to delete.';
		return;
	}
	await fetch('/events?id=' + encodeURIComponent(id), {
		method: 'DELETE',
		headers: { 'X-Events-Token': token },
	});
	loadEvents();
}

loadEvents();
</script>
</body>
</html>`;
