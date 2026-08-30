package com.smokescapehub.temple;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TempleOsrsClient
{
	private static final String BASE_URL = "https://templeosrs.com/api";
	private static final String USER_AGENT = "Mozilla/5.0 (SmokeScapeHub RuneLite Plugin)";

	private final OkHttpClient okHttpClient;
	private final Gson gson;

	@Inject
	public TempleOsrsClient(OkHttpClient okHttpClient, Gson gson)
	{
		this.okHttpClient = okHttpClient;
		this.gson = gson;
	}

	public void getGroupInfo(String groupId, Consumer<GroupInfo> onSuccess, Consumer<Exception> onError)
	{
		get(BASE_URL + "/group_info.php?id=" + groupId, GroupInfoResponse.class,
			response -> onSuccess.accept(response.data != null ? response.data.info : null), onError);
	}

	public void getGroupAchievements(String groupId, Consumer<List<GroupAchievement>> onSuccess, Consumer<Exception> onError)
	{
		get(BASE_URL + "/group_achievements.php?id=" + groupId, GroupAchievementsResponse.class,
			response -> onSuccess.accept(response.data != null ? response.data : Collections.emptyList()), onError);
	}

	public void getGroupCompetitions(String groupId, Consumer<List<GroupCompetition>> onSuccess, Consumer<Exception> onError)
	{
		get(BASE_URL + "/group_competitions.php?id=" + groupId, GroupCompetitionsResponse.class,
			response -> onSuccess.accept(response.data != null ? response.data : Collections.emptyList()), onError);
	}

	public void getCompetitionParticipants(int competitionId, Consumer<List<CompetitionParticipant>> onSuccess, Consumer<Exception> onError)
	{
		get(BASE_URL + "/competition_info.php?id=" + competitionId, CompetitionDetailResponse.class,
			response -> onSuccess.accept(response.data != null && response.data.participants != null
				? response.data.participants : Collections.emptyList()),
			onError);
	}

	public void getPetCounts(String groupId, int count, Consumer<List<PetCountEntry>> onSuccess, Consumer<Exception> onError)
	{
		get(BASE_URL + "/pets/pet_count.php?group=" + groupId + "&count=" + count, PetCountResponse.class,
			response -> onSuccess.accept(response.data != null ? new ArrayList<>(response.data.values()) : Collections.emptyList()),
			onError);
	}

	public void getCollectionLogLeaderboard(String groupId, Consumer<List<CollectionLogMember>> onSuccess, Consumer<Exception> onError)
	{
		get(BASE_URL + "/collection-log/group_collection_log.php?group=" + groupId, GroupCollectionLogResponse.class,
			response -> onSuccess.accept(response.data != null && response.data.members != null
				? response.data.members : Collections.emptyList()),
			onError);
	}

	public void getMemberStats(String groupId, Consumer<List<GroupMemberStats>> onSuccess, Consumer<Exception> onError)
	{
		get(BASE_URL + "/group_member_info.php?id=" + groupId + "&skills=1&bosses=1&details=1", GroupMemberInfoResponse.class,
			response -> onSuccess.accept(response.data != null && response.data.memberlist != null
				? new ArrayList<>(response.data.memberlist.values()) : Collections.emptyList()),
			onError);
	}

	private <T> void get(String url, Class<T> responseType, Consumer<T> onSuccess, Consumer<Exception> onError)
	{
		Request request = new Request.Builder()
			.url(url)
			.header("User-Agent", USER_AGENT)
			.build();

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
					T result = gson.fromJson(r.body().charStream(), responseType);
					onSuccess.accept(result);
				}
				catch (Exception ex)
				{
					onError.accept(ex);
				}
			}
		});
	}
}
