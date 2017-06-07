package com.example.vinicius.capstone.view;

import android.accounts.AccountManager;
import android.graphics.Bitmap;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.api.ApiClient;
import com.example.vinicius.capstone.interfaces.IApiServices;
import com.example.vinicius.capstone.interfaces.IToken;
import com.example.vinicius.capstone.utils.NetworkUtils;
import com.example.vinicius.capstone.utils.TokenImpl;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReadPostActivity extends AppCompatActivity
{
	private WebView webView;
	private String permalink;
	private AccountManager mAccountManager;
	private String grabbedToken;
	private Snackbar snackbar;
	private CoordinatorLayout coordinatorLayout;
	private ProgressBar progressBar;
	public static final String TAG = ReadPostActivity.class.getSimpleName();


	public static final String EXTRA_PERMALINK = "EXTRA_PERMALINK";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_post);

		mAccountManager = AccountManager.get(this);

		webView = (WebView) findViewById(R.id.webView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

		permalink = getIntent().getStringExtra(EXTRA_PERMALINK);

		webView.getSettings().setJavaScriptEnabled(true);

		webView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
			{
				super.onReceivedError(view, request, error);

				Log.e(TAG, error.toString());
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url)
			{
				super.onPageFinished(view, url);

				progressBar.setVisibility(View.INVISIBLE);
			}
		});

		loadPost();
	}

	private void loadPost()
	{
		webView.clearCache(true);
		webView.clearHistory();

		progressBar.setVisibility(View.VISIBLE);

		IToken tokenImpl = new TokenImpl();

		tokenImpl.getAccountTokenSynchronously(this, new TokenImpl.ITokenResponse()
		{
			@Override
			public void onTokenResponse(final String token)
			{
				IApiServices apiServices = new Retrofit.Builder()
						  .baseUrl(ApiClient.BASE_URL)
						  .addConverterFactory(GsonConverterFactory.create())
						  .build().create(IApiServices.class);

				Call<ResponseBody> callGetUserIdentity = apiServices.getUserIdentity("bearer "
						  + token);

				if(NetworkUtils.isOnline(ReadPostActivity.this))
				{
					callGetUserIdentity.enqueue(new Callback<ResponseBody>()
					{
						@Override
						public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
						{
							if(response.code() == 401)
							{
								IToken refreshToken = new TokenImpl();

								refreshToken.refreshToken(ReadPostActivity.this, mAccountManager.
										  getAccountsByType(ReadPostActivity.this.
													 getString(R.string.sync_account_type))[0], token, new TokenImpl.IRefreshTokenResponse()

								{
									@Override
									public void onRefreshTokenResponse(String token)
									{
										grabbedToken = token;

										HashMap<String, String> headerMap = new HashMap<>();
										headerMap.put("User-Agent", "Capstone");
										headerMap.put("Authorization", "bearer " + grabbedToken);

										webView.loadUrl("https://www.reddit.com" + permalink, headerMap);
									}
								});
							}
							else
							{
								grabbedToken = token;

								HashMap<String, String> headerMap = new HashMap<>();
								headerMap.put("User-Agent", "Capstone");
								headerMap.put("Authorization", "bearer " + grabbedToken);

								webView.loadUrl("https://www.reddit.com" + permalink, headerMap);
							}
						}

						@Override
						public void onFailure(Call<ResponseBody> call, Throwable t)
						{
							progressBar.setVisibility(View.INVISIBLE);
							showSnackBar(t.getMessage());
							for(int i = 0; i < t.getStackTrace().length; i++)
								Log.e(TAG, "ReadPostActivity.getAccountTokenSynchronously() - " + t.getStackTrace()[i].toString());
						}
					});
				}
				else
				{
					progressBar.setVisibility(View.INVISIBLE);
					showSnackBar(getResources().getString(R.string.no_internet_connection));
				}
			}
		});
	}

	public void showSnackBar(String message)
	{
		snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
				  .setAction(getResources().getString(R.string.tryAgain), new View.OnClickListener() {
					  @Override
					  public void onClick(View view) {
						  snackbar.dismiss();
						  loadPost();
					  }
				  });

		snackbar.show();
	}
}
