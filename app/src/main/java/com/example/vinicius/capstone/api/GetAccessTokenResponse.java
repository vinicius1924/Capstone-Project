package com.example.vinicius.capstone.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vinicius on 22/05/17.
 */

public class GetAccessTokenResponse
{
	@SerializedName("access_token")
	@Expose
	private String accessToken;

	@SerializedName("token_type")
	@Expose
	private String tokenType;

	@SerializedName("expires_in")
	@Expose
	private long expiresIn;

	@SerializedName("refresh_token")
	@Expose
	private String refreshToken;

	public String getAccessToken()
	{
		return accessToken;
	}

	public String getTokenType()
	{
		return tokenType;
	}

	public long getExpiresIn()
	{
		return expiresIn;
	}

	public String getRefreshToken()
	{
		return refreshToken;
	}
}
