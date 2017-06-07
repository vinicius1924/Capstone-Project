package com.example.vinicius.capstone.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.example.vinicius.capstone.LoginActivity;
import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.api.ApiClient;
import com.example.vinicius.capstone.api.GetAccessTokenResponse;
import com.example.vinicius.capstone.interfaces.IApiServices;
import com.example.vinicius.capstone.interfaces.IToken;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.vinicius.capstone.sync.AccountGeneral.ACCOUNT_TOKEN_TYPE_FULL_ACCESS;

/**
 * Created by vinicius on 26/05/17.
 */

public class TokenImpl implements IToken
{
	private final String TAG = getClass().getSimpleName();

	public interface IRefreshTokenResponse
	{
		void onRefreshTokenResponse(String token);
	}

	public interface ITokenResponse
	{
		void onTokenResponse(final String token);
	}


	@Override
	public void refreshToken(final Context context, final Account account, final String token, final IRefreshTokenResponse refreshTokenResponse)
	{
		final IApiServices apiServices = new Retrofit.Builder()
				  .baseUrl(ApiClient.BASE_URL)
				  .addConverterFactory(GsonConverterFactory.create())
				  .build().create(IApiServices.class);

		String authString = LoginActivity.CLIENT_ID + ":";
		final String encodedAuthString = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
		String authorization = "Basic " + encodedAuthString;

		Call<GetAccessTokenResponse> callRefreshToken = apiServices.refreshToken(authorization, "refresh_token",
				  ((AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE)).getUserData(account, LoginActivity.REFRESH_TOKEN));

		callRefreshToken.enqueue(new Callback<GetAccessTokenResponse>()
		{
			@Override
			public void onResponse(Call<GetAccessTokenResponse> call, Response<GetAccessTokenResponse>
					  response)
			{
				String newToken = response.body().getAccessToken();

				((AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE)).invalidateAuthToken(account.type, token);
				((AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE)).setAuthToken(((AccountManager)context.
						  getSystemService(Context.ACCOUNT_SERVICE)).getAccountsByType(context.
						  getString(R.string.sync_account_type))[0], ACCOUNT_TOKEN_TYPE_FULL_ACCESS, newToken);

				refreshTokenResponse.onRefreshTokenResponse(newToken);
			}

			@Override
			public void onFailure(Call<GetAccessTokenResponse> call, Throwable t)
			{
				Log.e(TAG, "TokenImpl.refreshToken() - " + t.toString());
			}
		});
	}

	@Override
	public String getAccountTokenAsynchronously(Context context)
	{
		AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		String token = null;

		try
		{
			/*
			 * blockingGetAuthToken() faz o mesmo que getAuthToken() só que de forma sincrona, aqui pode
			 * ser usado o método sincrono pois já estamos executando em uma thread diferente da main thread
			 */
			token = accountManager.blockingGetAuthToken(
					  accountManager.getAccountsByType(context.getString(R.string.sync_account_type))[0],
					  ACCOUNT_TOKEN_TYPE_FULL_ACCESS, true);
		}
		catch(OperationCanceledException e)
		{
			Log.e(TAG, "TokenImpl.getAccountTokenAsynchronously() - " + e.toString());
			e.printStackTrace();
		}
		catch(IOException e)
		{
			Log.e(TAG, "TokenImpl.getAccountTokenAsynchronously() - " + e.toString());
			e.printStackTrace();
		}
		catch(AuthenticatorException e)
		{
			Log.e(TAG, "TokenImpl.getAccountTokenAsynchronously() - " + e.toString());
			e.printStackTrace();
		}

		return token;
	}

	@Override
	public void getAccountTokenSynchronously(Context context, final ITokenResponse tokenResponse)
	{
		AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		accountManager.getAuthToken(accountManager.getAccountsByType(context.getString(R.string.sync_account_type))[0],
				  ACCOUNT_TOKEN_TYPE_FULL_ACCESS, null, (AppCompatActivity)context, new AccountManagerCallback<Bundle>()

		{
			@Override
			public void run(AccountManagerFuture<Bundle> future)
			{
				Bundle bundle = null;

				try
				{
					bundle = future.getResult();
				}
				catch(OperationCanceledException e)
				{
					Log.e(TAG, "TokenImpl.getAccountTokenSynchronously() - " + e.toString());
					e.printStackTrace();
				}
				catch(IOException e)
				{
					Log.e(TAG, "TokenImpl.getAccountTokenSynchronously() - " + e.toString());
					e.printStackTrace();
				}
				catch(AuthenticatorException e)
				{
					Log.e(TAG, "TokenImpl.getAccountTokenSynchronously() - " + e.toString());
					e.printStackTrace();
				}

				final String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

				tokenResponse.onTokenResponse(token);
			}
		}, null);
	}
}
