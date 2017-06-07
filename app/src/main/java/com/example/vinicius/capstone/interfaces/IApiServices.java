package com.example.vinicius.capstone.interfaces;

import com.example.vinicius.capstone.api.GetAccessTokenResponse;
import com.example.vinicius.capstone.api.GetDefaultSubredditsResponse;
import com.example.vinicius.capstone.api.GetSubredditsPostsResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by vinicius on 22/05/17.
 */

public interface IApiServices
{
	@POST("https://www.reddit.com/api/v1/access_token")
	@Headers({"User-Agent: Capstone"})
	@FormUrlEncoded
	Call<GetAccessTokenResponse> getAccessToken(@Header("Authorization") String authorization,
															  @Field("grant_type") String grantType, @Field("code") String code,
															  @Field("redirect_uri") String redirectUri);

	/* aqui o grant_type deve ser igual a refresh_token */
	@POST("https://www.reddit.com/api/v1/access_token")
	@Headers({"User-Agent: Capstone"})
	@FormUrlEncoded
	Call<GetAccessTokenResponse> refreshToken(@Header("Authorization") String authorization,
															@Field("grant_type") String grantType,
															@Field("refresh_token") String refresToken);

	@GET("api/v1/me")
	@Headers({"User-Agent: Capstone"})
	Call<ResponseBody> getUserIdentity(@Header("Authorization") String authorization);

	@GET("/subreddits/default?limit=100")
	@Headers({"User-Agent: Capstone"})
	Call<GetDefaultSubredditsResponse> getDefaultSubreddits(@Header("Authorization") String authorization);

	@GET("{subredditUrl}new/?limit=25")
	@Headers({"User-Agent: Capstone"})
	Call<GetSubredditsPostsResponse> getSubredditsPosts(@Header("Authorization") String authorization,
																		 @Path(value = "subredditUrl", encoded = true) String subredditUrl);

	@GET("{subredditUrl}new/?limit=25")
	@Headers({"User-Agent: Capstone"})
	Call<GetSubredditsPostsResponse> getMore25SubredditsPosts(@Header("Authorization") String authorization,
																		 @Path(value = "subredditUrl", encoded = true) String subredditUrl,
																				 @Query("before") String before);

	@GET("{subredditUrl}new/?limit=100")
	@Headers({"User-Agent: Capstone"})
	Call<GetSubredditsPostsResponse> getMore100SubredditsPosts(@Header("Authorization") String authorization,
																				 @Path(value = "subredditUrl", encoded = true) String subredditUrl,
																				 @Query("before") String before);
}
