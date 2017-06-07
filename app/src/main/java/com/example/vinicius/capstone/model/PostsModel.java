package com.example.vinicius.capstone.model;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.api.ApiClient;
import com.example.vinicius.capstone.api.GetSubredditsPostsResponse;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.interfaces.IApiServices;
import com.example.vinicius.capstone.interfaces.IPostsMVP;
import com.example.vinicius.capstone.interfaces.IToken;
import com.example.vinicius.capstone.presenter.PostsPresenter;
import com.example.vinicius.capstone.utils.NetworkUtils;
import com.example.vinicius.capstone.utils.PreferencesUtils;
import com.example.vinicius.capstone.utils.TokenImpl;
import com.example.vinicius.capstone.view.PostsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.vinicius.capstone.sync.SubRedditSyncAdapter.ACTION_DATA_UPDATED;
import static com.example.vinicius.capstone.view.PostsActivity.EXTRA_SUBREDDIT_ID;
import static com.example.vinicius.capstone.view.PostsActivity.EXTRA_SUBREDDIT_URL;

/**
 * Created by vinicius on 29/05/17.
 */

public class PostsModel implements IPostsMVP.ModelOps, LoaderManager.LoaderCallbacks<Cursor>
{
	// Referência para layer Presenter
	private IPostsMVP.RequiredPresenterOps mPresenter;
	private AccountManager mAccountManager;
	private static final int POSTSLOADER = 1;
	private int subredditId;
	private String subredditUrl;

	public PostsModel(PostsPresenter mPresenter)
	{
		this.mPresenter = mPresenter;
		mAccountManager = AccountManager.get(mPresenter.getActivityContext());
		subredditId = mPresenter.getActivity().getIntent().getIntExtra(EXTRA_SUBREDDIT_ID, 0);
		subredditUrl = mPresenter.getActivity().getIntent().getStringExtra(EXTRA_SUBREDDIT_URL);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		Uri postsUri = SubredditContract.SubredditsPostsEntry.buildSubredditsPostsUri(subredditId);

		android.support.v4.content.CursorLoader cursorLoader = new android.support.v4.content.CursorLoader(
				  mPresenter.getActivityContext(),
				  postsUri,
				  null,
				  null,
				  null,
				  SubredditContract.SubredditsPostsEntry.COLUMN_CREATED_UTC + " DESC");

		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		mPresenter.onLoadDataFinished(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		mPresenter.onLoaderReset(loader);
	}

	@Override
	public void initLoader()
	{
		(mPresenter.getActivity()).getSupportLoaderManager().initLoader(POSTSLOADER, null, this);
	}

	@Override
	public void onStart()
	{

	}

	@Override
	public void onStop()
	{

	}

	@Override
	public void loadMore25Posts()
	{
		final IToken tokenImpl = new TokenImpl();

		Uri uri = SubredditContract.SubredditsPostsEntry.buildSubredditsPostsUri(subredditId);

		final Cursor cursor = mPresenter.getActivityContext().getContentResolver().query(uri, null,
				  SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID + " = ?",
				  new String[]{String.valueOf(subredditId)},
				  SubredditContract.SubredditsPostsEntry.COLUMN_CREATED_UTC + " DESC");


		tokenImpl.getAccountTokenSynchronously(mPresenter.getActivityContext(), new TokenImpl.ITokenResponse()
		{
			@Override
			public void onTokenResponse(final String token)
			{
				final IApiServices apiServices = new Retrofit.Builder()
						  .baseUrl(ApiClient.BASE_URL)
						  .addConverterFactory(GsonConverterFactory.create())
						  .build().create(IApiServices.class);
				Call<GetSubredditsPostsResponse> callGetSubredditsPosts;

				if(cursor.moveToFirst())
				{
					callGetSubredditsPosts = apiServices.getMore25SubredditsPosts("bearer " + token, subredditUrl,
							  cursor.getString(cursor.getColumnIndex(SubredditContract.SubredditsPostsEntry.COLUMN_NAME)));
				}
				else
				{
					callGetSubredditsPosts = apiServices.getSubredditsPosts("bearer " + token, subredditUrl);
				}

				if(NetworkUtils.isOnline(mPresenter.getActivityContext()))
				{
					callGetSubredditsPosts.enqueue(new Callback<GetSubredditsPostsResponse>()
					{
						@Override
						public void onResponse(Call<GetSubredditsPostsResponse> call, Response<GetSubredditsPostsResponse>
								  response)
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
										loadMore25Posts();
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

									if(PreferencesUtils.getSelectedSubredditWidget(mPresenter.getActivityContext()).equals(String.valueOf(subredditId)))
									{
										Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
										mPresenter.getActivityContext().sendBroadcast(dataUpdatedIntent);
									}
								}
								else
								{
									mPresenter.showMessage(mPresenter.getActivityContext().getResources().
											  getString(R.string.no_more_posts));
								}

								mPresenter.onLoadPostsFinished();
							}
						}

						@Override
						public void onFailure(Call<GetSubredditsPostsResponse> call, Throwable t)
						{
							Log.e(PostsActivity.POSTSACTIVITYTAG, "PostsModel.loadMore25Posts() - " + t.toString());
							mPresenter.onLoadPostsFinished();
						}
					});
				}
				else
				{
					mPresenter.noInternetConnection();
					mPresenter.onLoadPostsFinished();
				}
			}
		});
	}

	/**
	 * Disparada por {@link PostsPresenter#onDestroy(boolean)}
	 * para as operações necessárias que eventualmente
	 * estiverem executando no BG
	 */
	@Override
	public void onDestroy()
	{

	}
}
