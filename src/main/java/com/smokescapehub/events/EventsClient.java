package com.smokescapehub.events;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EventsClient
{
	private final OkHttpClient okHttpClient;
	private final Gson gson;

	@Inject
	public EventsClient(OkHttpClient okHttpClient, Gson gson)
	{
		this.okHttpClient = okHttpClient;
		this.gson = gson;
	}

	public void getEvents(String eventsUrl, Consumer<List<ClanEvent>> onSuccess, Consumer<Exception> onError)
	{
		if (eventsUrl == null || eventsUrl.trim().isEmpty())
		{
			onSuccess.accept(Collections.emptyList());
			return;
		}

		Request request = new Request.Builder().url(eventsUrl.trim()).build();
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				onError.accept(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try (Response r = response)
				{
					if (!r.isSuccessful() || r.body() == null)
					{
						onError.accept(new IOException("Unexpected response " + r.code()));
						return;
					}
					List<ClanEvent> events = gson.fromJson(r.body().charStream(), new TypeToken<List<ClanEvent>>() {}.getType());
					onSuccess.accept(events != null ? events : Collections.emptyList());
				}
				catch (Exception ex)
				{
					onError.accept(ex);
				}
			}
		});
	}
}
