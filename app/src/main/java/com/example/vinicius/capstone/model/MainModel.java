package com.example.vinicius.capstone.model;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.SubredditsRecyclerAdapter;
import com.example.vinicius.capstone.api.ApiClient;
import com.example.vinicius.capstone.api.GetDefaultSubredditsResponse;
import com.example.vinicius.capstone.api.GetSubredditsPostsResponse;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.interfaces.IApiServices;
import com.example.vinicius.capstone.interfaces.IMainMVP;
import com.example.vinicius.capstone.interfaces.IToken;
import com.example.vinicius.capstone.utils.NetworkUtils;
import com.example.vinicius.capstone.utils.TokenImpl;
import com.example.vinicius.capstone.view.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by vinicius on 16/05/17.
 */

public class MainModel implements IMainMVP.ModelOps, LoaderManager.LoaderCallbacks<Cursor>,
		  SubredditsRecyclerAdapter.SubscribeUnsubscribeSubredditListener
{
	// Referência para layer Presenter
	private IMainMVP.RequiredPresenterOps mPresenter;
	private AccountManager mAccountManager;
	public static final int SUBREDDITSLOADER = 0;

	public MainModel(IMainMVP.RequiredPresenterOps mPresenter)
	{
		this.mPresenter = mPresenter;
		mAccountManager = AccountManager.get(mPresenter.getActivityContext());
	}

	@Override
	public void initLoader()
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainModel.initLoader()");
		(mPresenter.getActivity()).getSupportLoaderManager().initLoader(SUBREDDITSLOADER, null, this);
	}

	@Override
	public void getDefaultSubreddits()
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainModel.getDefaultSubreddits()");
		/*
		 * Testa se o banco de dados tem dados na tabela que guarda os subreddits.
		 * Se não tiver dados então faz uma chamada para pegar os subreddits default e salva no banco de dados.
		 * Se tiver os dados então não precisa fazer a chamada
		 */
		Cursor subredditCursor = mPresenter.getActivityContext().getContentResolver().
				  query(SubredditContract.SubredditsEntry.CONTENT_URI, new String[]{SubredditContract.SubredditsEntry._ID},
							 null, null, null);

		if(!subredditCursor.moveToFirst())
		{
			final IToken tokenImpl = new TokenImpl();

			tokenImpl.getAccountTokenSynchronously(mPresenter.getActivityContext(), new TokenImpl.ITokenResponse()
			{
				@Override
				public void onTokenResponse(final String token)
				{
					final IApiServices apiServices = new Retrofit.Builder()
							  .baseUrl(ApiClient.BASE_URL)
							  .addConverterFactory(GsonConverterFactory.create())
							  .build().create(IApiServices.class);//ApiClient.getClient().create(IApiServices.class);
					Call<GetDefaultSubredditsResponse> callGetDefaultSubreddits = apiServices.getDefaultSubreddits("bearer " + token);

					if(NetworkUtils.isOnline(mPresenter.getActivityContext()))
					{
						callGetDefaultSubreddits.enqueue(new Callback<GetDefaultSubredditsResponse>()
						{
							@Override
							public void onResponse(Call<GetDefaultSubredditsResponse> call, Response<GetDefaultSubredditsResponse> response)
							{
								if(response.code() == 401)
								{
									AccountManager accountManager = (AccountManager) mPresenter.getActivityContext().
											  getSystemService(Context.ACCOUNT_SERVICE);

									tokenImpl.refreshToken(mPresenter.getActivityContext(), accountManager.
											  getAccountsByType(mPresenter.getActivityContext().getString(R.string.sync_account_type))[0], token, new TokenImpl.IRefreshTokenResponse()
									{
										@Override
										public void onRefreshTokenResponse(String token)
										{
											getDefaultSubreddits();
										}
									});
								}
								else
								{
									Vector<ContentValues> cVVector = new Vector<ContentValues>(response.body().getData().
											  getChildren().size());

									ContentResolver resolver = mPresenter.getActivityContext().getContentResolver();

									for(int i = 0; i < response.body().getData().getChildren().size(); i++)
									{
										ContentValues subredditValues = new ContentValues();

										String displayName = response.body().getData().getChildren().get(i).getData().getDisplayName();
										String url = response.body().getData().getChildren().get(i).getData().getUrl();
										int subscribed = response.body().getData().getChildren().get(i).getData().getSubscribed();


										subredditValues.put(SubredditContract.SubredditsEntry.COLUMN_NAME, displayName);
										subredditValues.put(SubredditContract.SubredditsEntry.COLUMN_URL, url);
										subredditValues.put(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED, subscribed);

										cVVector.add(subredditValues);
									}

									if(cVVector.size() > 0)
									{
										ContentValues[] cvArray = new ContentValues[cVVector.size()];
										cVVector.toArray(cvArray);
										resolver.bulkInsert(SubredditContract.SubredditsEntry.CONTENT_URI, cvArray);
									}
								}
							}

							@Override
							public void onFailure(Call<GetDefaultSubredditsResponse> call, Throwable t)
							{
								mPresenter.onErrorGetDefaultSubreddits(mPresenter.getActivityContext().getResources().
										  getString(R.string.connection_error));
								Log.e(MainActivity.MAINACTIVITYTAG, "MainModel.getDefaultSubreddits() - " + t.toString());
							}
						});
					}
					else
					{
						mPresenter.noInternetConnection();
					}
				}
			});
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainModel.onCreateLoader()");
		return new android.support.v4.content.CursorLoader(mPresenter.getActivityContext(),
				  SubredditContract.SubredditsEntry.CONTENT_URI,
				  null,
				  null,
				  null,
				  null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainModel.onLoadFinished()");
		mPresenter.onLoadDataFinished(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainModel.onLoaderReset()");
		mPresenter.onLoaderReset(loader);
	}

	@Override
	public void onSubscribeReddit(int subredditId, final String subredditUrl)
	{
		ContentValues values = new ContentValues();
		values.put(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED, 1);

		int updateResult = mPresenter.getActivityContext().getContentResolver().update(SubredditContract.SubredditsEntry.CONTENT_URI, values,
				  SubredditContract.SubredditsEntry._ID + " = ?", new String[]{String.valueOf(subredditId)});

		if(updateResult > 0)
		{
			fetchSubredditPosts(subredditUrl, subredditId);
		}
	}

	@Override
	public void onUnSubscribeReddit(int subredditId, String subredditUrl)
	{
		ContentValues values = new ContentValues();
		values.put(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED, 0);

		int updateResult = mPresenter.getActivityContext().getContentResolver().update(SubredditContract.SubredditsEntry.CONTENT_URI, values,
				  SubredditContract.SubredditsEntry._ID + " = ?", new String[]{String.valueOf(subredditId)});

		if(updateResult > 0)
		{
			// atualiza o banco de dados removendo todos os posts do subreddit unsubscribed
			eraseSubredditPosts(subredditId);
		}
	}

	private void eraseSubredditPosts(int subredditId)
	{
		mPresenter.getActivityContext().getContentResolver().delete(SubredditContract.SubredditsPostsEntry.CONTENT_URI,
				  SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID + " = ?", new String[]{String.valueOf(subredditId)});
	}

	private void fetchSubredditPosts(final String subredditUrl, final int subredditId)
	{
		// baixa os ultimos 25 posts do subreddit e armazena no banco e dados
		final IToken tokenImpl = new TokenImpl();

		tokenImpl.getAccountTokenSynchronously(mPresenter.getActivityContext(), new TokenImpl.ITokenResponse()
		{
			@Override
			public void onTokenResponse(final String token)
			{
				final IApiServices apiServices = new Retrofit.Builder()
						  .baseUrl(ApiClient.BASE_URL)
						  .addConverterFactory(GsonConverterFactory.create())
						  .build().create(IApiServices.class);//ApiClient.getClient().create(IApiServices.class);
				Call<GetSubredditsPostsResponse> callGetSubredditsPosts = apiServices.getSubredditsPosts("bearer "
						  + token, subredditUrl);

				callGetSubredditsPosts.enqueue(new Callback<GetSubredditsPostsResponse>()
				{
					@Override
					public void onResponse(Call<GetSubredditsPostsResponse> call, Response<GetSubredditsPostsResponse> response)
					{
						if(response.code() == 401)
						{
							IToken refreshToken = new TokenImpl();

							refreshToken.refreshToken(mPresenter.getActivityContext(), mAccountManager.
												 getAccountsByType(mPresenter.getActivityContext().
															getString(R.string.sync_account_type))[0], token,
									  new TokenImpl.IRefreshTokenResponse()
									  {
										  @Override
										  public void onRefreshTokenResponse(String token)
										  {
											  fetchSubredditPosts(subredditUrl, subredditId);
										  }
									  });
						}
						else
						{
							List<GetSubredditsPostsResponse.Children> safeForWork = new ArrayList<GetSubredditsPostsResponse
									  .Children>();

							for(GetSubredditsPostsResponse.Children children : response.body().getData().getChildren())
							{
								if(!children.getData().isOver18())
								{
									safeForWork.add(children);
								}
							}

							Vector<ContentValues> cVVector = new Vector<ContentValues>(safeForWork.size());

							ContentResolver resolver = mPresenter.getActivityContext().getContentResolver();

							for(int i = 0; i < safeForWork.size(); i++)
							{
								ContentValues postsValues = new ContentValues();

								String thumbnail = safeForWork.get(i).getData().getThumbnail();
								String title = safeForWork.get(i).getData().getTitle();
								int numberOfComments = safeForWork.get(i).getData().getNumberOfComments();
								String author = safeForWork.get(i).getData().getAuthor();
								String name = safeForWork.get(i).getData().getName();
								String permalink = safeForWork.get(i).getData().getPermalink();
								long createdUtc = safeForWork.get(i).getData().getCreatedUtc();
								int subredditid = subredditId;


								postsValues.put(SubredditContract.SubredditsPostsEntry.COLUMN_THUMBNAIL, thumbnail);
								postsValues.put(SubredditContract.SubredditsPostsEntry.COLUMN_TITLE, title);
								postsValues.put(SubredditContract.SubredditsPostsEntry.COLUMN_NUM_COMMENTS, numberOfComments);
								postsValues.put(SubredditContract.SubredditsPostsEntry.COLUMN_AUTHOR, author);
								postsValues.put(SubredditContract.SubredditsPostsEntry.COLUMN_NAME, name);
								postsValues.put(SubredditContract.SubredditsPostsEntry.COLUMN_PERMALINK, permalink);
								postsValues.put(SubredditContract.SubredditsPostsEntry.COLUMN_CREATED_UTC, createdUtc);
								postsValues.put(SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID, subredditid);

								cVVector.add(postsValues);
							}

							if(cVVector.size() > 0)
							{
								ContentValues[] cvArray = new ContentValues[cVVector.size()];
								cVVector.toArray(cvArray);
								resolver.bulkInsert(SubredditContract.SubredditsPostsEntry.CONTENT_URI, cvArray);
							}
						}
					}

					@Override
					public void onFailure(Call<GetSubredditsPostsResponse> call, Throwable t)
					{
						Log.e(MainActivity.MAINACTIVITYTAG, "MainModel.fetchSubredditPosts() - " + t.toString());
					}
				});
			}
		});
	}
}
