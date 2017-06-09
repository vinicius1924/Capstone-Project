package com.example.vinicius.capstone;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.vinicius.capstone.api.ApiClient;
import com.example.vinicius.capstone.api.GetAccessTokenResponse;
import com.example.vinicius.capstone.interfaces.IApiServices;
import com.example.vinicius.capstone.sync.AccountGeneral;
import com.example.vinicius.capstone.utils.NetworkUtils;
import com.example.vinicius.capstone.view.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by vinicius on 20/05/17.
 */

public class LoginActivity extends AccountAuthenticatorActivity
{
	private ProgressBar progressBar;
	private final String TAG = getClass().getSimpleName();
	private static final String AUTH_URL =
			  "https://www.reddit.com/api/v1/authorize.compact?client_id=%s" +
						 "&response_type=code&state=%s&redirect_uri=%s&" +
						 "duration=permanent&scope=identity, read, mysubreddits, vote, submit";

	public static final String CLIENT_ID = "amKcZtPXo_kmpw";

	private static final String STATE = "MY_RANDOM_STRING_1";

	public static final String REDIRECT_URI = "https://www.google.com/teste";

	public static final String REFRESH_TOKEN = "refreshToken";

	private AccountManager mAccountManager;
	private String mAccountType;
	private String mAuthTokenType;

	private WebView webView;
	private Snackbar snackbar;
	private CoordinatorLayout coordinatorLayout;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("Authenticator", "LoginActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mAccountManager = AccountManager.get(getBaseContext());
		webView = (WebView)findViewById(R.id.webView);
		coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		//mAccountName = getIntent().getStringExtra(AccountGeneral.ARG_ACCOUNT_NAME);
		mAccountType = getIntent().getStringExtra(AccountGeneral.ARG_ACCOUNT_TYPE);
		mAuthTokenType = getIntent().getStringExtra(AccountGeneral.ARG_AUTH_TOKEN_TYPE);

		startLogin();
	}

	private void showSnackBar(String message)
	{
		progressBar.setVisibility(View.INVISIBLE);
		snackbar = Snackbar.make(coordinatorLayout, message,
				  Snackbar.LENGTH_INDEFINITE)
				  .setAction(getResources().getString(R.string.tryAgain), new View.OnClickListener() {
					  @Override
					  public void onClick(View view) {
						  snackbar.dismiss();
						  startLogin();
					  }
				  });

		snackbar.show();
	}

	private void showToast(String msg)
	{
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private void startLogin()
	{
		progressBar.setVisibility(View.VISIBLE);

		if(!NetworkUtils.isOnline(this))
		{
			showSnackBar(getResources().getString(R.string.no_internet_connection));
		}
		else
		{
			Log.d("LoginActivity", "startLogin");

			String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);

			webView.getSettings().setJavaScriptEnabled(true);
			webView.clearCache(true);
			webView.loadUrl(url);

			webView.setWebViewClient(new WebViewClient()
			{
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon)
				{
					if(!NetworkUtils.isOnline(LoginActivity.this))
					{
						showToast(getResources().getString(R.string.no_internet_connection));
					}
					else
					{
						super.onPageStarted(view, url, favicon);

						if(url.contains("state="))
						{
							Uri uri = Uri.parse(url);

							handleRedirectUri(uri);
						}
					}
				}

				@Override
				public void onPageFinished(WebView view, String url)
				{
					super.onPageFinished(view, url);

					progressBar.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
				{
					super.onReceivedError(view, request, error);

					Log.e(TAG, error.toString());

					progressBar.setVisibility(View.INVISIBLE);
				}
			});
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	private void handleRedirectUri(Uri uri)
	{
		Log.d("Authenticator", "LoginActivity.handleRedirectUri()");

		if(uri.getQueryParameter("error") != null)
		{
			String error = uri.getQueryParameter("error");

			if(error.equals("access_denied"))
			{
				showToast(getResources().getString(R.string.accept_permissions));
			}

			Log.e(TAG, "An error has occurred : " + error);
		}
		else
		{
			if(uri.getQueryParameter("code") != null)
			{
				String state = uri.getQueryParameter("state");

				if(state.equals(STATE))
				{
					String code = uri.getQueryParameter("code");
					getAccessToken(code);
				}
			}
		}
	}

	private void getAccessToken(String code)
	{
		webView.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		Log.d("Authenticator", "LoginActivity.getAccessToken()");

		IApiServices apiServices = new Retrofit.Builder()
				  .baseUrl(ApiClient.BASE_URL)
				  .addConverterFactory(GsonConverterFactory.create())
				  .build().create(IApiServices.class);

		String authString = CLIENT_ID + ":";
		final String encodedAuthString = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
		String authorization = "Basic " + encodedAuthString;

		Call<GetAccessTokenResponse> callGetToken = apiServices.getAccessToken(authorization, "authorization_code", code,
				  LoginActivity.REDIRECT_URI);

		callGetToken.enqueue(new Callback<GetAccessTokenResponse>()
		{
			@Override
			public void onResponse(Call<GetAccessTokenResponse> call, Response<GetAccessTokenResponse> response)
			{
				String accessToken = response.body().getAccessToken();
				String refreshToken = response.body().getRefreshToken();

				int countAccounts = mAccountManager.getAccountsByType(mAccountType).length;

				Account newAccount = new Account(getString(R.string.app_name), mAccountType);

				// Creating the account on the device and setting the auth token we got
				// (Not setting the auth token will cause another call to the server to authenticate the user)
				mAccountManager.addAccountExplicitly(newAccount, "", null);
				mAccountManager.setAuthToken(newAccount, mAuthTokenType, accessToken);
				mAccountManager.setUserData(newAccount, REFRESH_TOKEN, refreshToken);

				Bundle bundle = new Bundle();

				//bundle.putString(AccountManager.KEY_ACCOUNT_NAME, mUserName);
				bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
				bundle.putString(AccountManager.KEY_AUTHTOKEN, accessToken);

				final Intent res = new Intent();
				res.putExtras(bundle);

				setAccountAuthenticatorResult(res.getExtras());
				progressBar.setVisibility(View.INVISIBLE);
				finish();

				if(countAccounts == 0){
					startActivity(new Intent(LoginActivity.this, MainActivity.class));
				}
			}

			@Override
			public void onFailure(Call<GetAccessTokenResponse> call, Throwable t)
			{
				progressBar.setVisibility(View.INVISIBLE);
				showSnackBar(getResources().getString(R.string.connection_error));
				Log.e(TAG, "LoginActivity.getAccessToken() - " + t.toString());
			}
		});
	}
}
