package com.example.vinicius.capstone.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.util.Log;

import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.api.ApiClient;
import com.example.vinicius.capstone.api.GetSubredditsPostsResponse;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.interfaces.IApiServices;
import com.example.vinicius.capstone.interfaces.IToken;
import com.example.vinicius.capstone.utils.NetworkUtils;
import com.example.vinicius.capstone.utils.PreferencesUtils;
import com.example.vinicius.capstone.utils.TokenImpl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.vinicius.capstone.sync.AccountGeneral.ACCOUNT_TOKEN_TYPE_FULL_ACCESS;

/**
 * Created by vinicius on 16/05/17.
 */

public class SubRedditSyncAdapter extends AbstractThreadedSyncAdapter
{
	public static final String TAG = "SubRedditSyncAdapter";
	public static final String ACTION_DATA_UPDATED = "com.example.vinicius.capstone.sync.ACTION_DATA_UPDATED";
	// Interval at which to sync with the weather, in seconds.
	// 60 seconds (1 minute) * 180 = 3 hours
	public static final int SYNC_INTERVAL = 60 * 180;
	public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
	private final Context mContext;
	private final AccountManager accountManager;

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({GET_USER_IDENTITY})
	public @interface Method
	{}

	public static final int GET_USER_IDENTITY = 0;

	public SubRedditSyncAdapter(Context context, boolean autoInitialize)
	{
		super(context, autoInitialize);
		mContext = context;
		accountManager = (AccountManager) mContext.getSystemService(Context.ACCOUNT_SERVICE);
	}

	@Override
	public void onPerformSync(final Account account, Bundle extras, String authority, ContentProviderClient provider,
									  SyncResult syncResult)
	{
		if(accountManager.getAccountsByType(mContext.getString(R.string.sync_account_type)).length > 0)
		{
			try
			{
				String token = getAccountToken();

				if(token != null)
				{
					getSubredditsPosts();
				}
				else
				{

				}
			}
			catch(OperationCanceledException e)
			{
				Log.e(TAG, "SubRedditSyncAdapter.onPerformSync() - " + e.toString());
				e.printStackTrace();
			}
			catch(IOException e)
			{
				Log.e(TAG, "SubRedditSyncAdapter.onPerformSync() - " + e.toString());
				e.printStackTrace();
			}
			catch(AuthenticatorException e)
			{
				Log.e(TAG, "SubRedditSyncAdapter.onPerformSync() - " + e.toString());
				e.printStackTrace();
			}
		}
	}

	private String getAccountToken() throws AuthenticatorException, OperationCanceledException, IOException
	{
		IToken tokenImpl = new TokenImpl();

		String token = tokenImpl.getAccountTokenAsynchronously(mContext);

		return token;
	}

	private void getSubredditsPosts()
	{
		//pega os subreddits que o usuário está subscribed
		final Cursor cursor = mContext.getContentResolver().query(SubredditContract.SubredditsEntry.CONTENT_URI,
				  null,
				  SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED + " = ?",
				  new String[]{String.valueOf(1)},
				  null);

		if(cursor != null)
		{
			while(cursor.moveToNext())
			{
				final int subredditId = cursor.getInt(cursor.getColumnIndex(SubredditContract.SubredditsEntry._ID));
				final String subredditUrl = cursor.getString(cursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_URL));


				String token = null;

				try
				{
					token = accountManager.blockingGetAuthToken(
							  accountManager.getAccountsByType(mContext.getString(R.string.sync_account_type))[0],
							  ACCOUNT_TOKEN_TYPE_FULL_ACCESS, true);
				}
				catch(OperationCanceledException e)
				{
					Log.e(TAG, "SubRedditSyncAdapter.getSubredditsPosts() - " + e.toString());
					e.printStackTrace();
				}
				catch(IOException e)
				{
					Log.e(TAG, "SubRedditSyncAdapter.getSubredditsPosts() - " + e.toString());
					e.printStackTrace();
				}
				catch(AuthenticatorException e)
				{
					Log.e(TAG, "SubRedditSyncAdapter.getSubredditsPosts() - " + e.toString());
					e.printStackTrace();
				}

				final IApiServices apiServices = new Retrofit.Builder()
						  .baseUrl(ApiClient.BASE_URL)
						  .addConverterFactory(GsonConverterFactory.create())
						  .build().create(IApiServices.class);
				Call<GetSubredditsPostsResponse> callGetSubredditsPosts;

				Uri uri = SubredditContract.SubredditsEntry.buildSubredditsUri(subredditId);

				final Cursor cursorSubredddit = mContext.getContentResolver().query(uri,
						  new String[]{SubredditContract.SubredditsEntry.COLUMN_LAST_DOWNLOADED},
						  null,
						  null,
						  null);

				cursorSubredddit.moveToFirst();

				String lastDownloaded = cursorSubredddit.getString(cursorSubredddit.getColumnIndex(SubredditContract.
						  SubredditsEntry.COLUMN_LAST_DOWNLOADED));

				if(lastDownloaded != null && !lastDownloaded.isEmpty())
				{
					callGetSubredditsPosts = apiServices.getMore100SubredditsPosts("bearer " + token, subredditUrl,
							  lastDownloaded);
				}
				else
				{
					callGetSubredditsPosts = apiServices.getSubredditsPosts("bearer " + token, subredditUrl);
				}

				if(NetworkUtils.isOnline(mContext))
				{
					callGetSubredditsPosts.enqueue(new Callback<GetSubredditsPostsResponse>()
					{
						@Override
						public void onResponse(Call<GetSubredditsPostsResponse> call, Response<GetSubredditsPostsResponse>
								  response)
						{
							if(response.code() == 401)
							{
								AccountManager accountManager = (AccountManager) mContext.
										  getSystemService(Context.ACCOUNT_SERVICE);

								String token = null;

								try
								{
									token = accountManager.blockingGetAuthToken(
											  accountManager.getAccountsByType(mContext.getString(R.string.sync_account_type))[0],
											  ACCOUNT_TOKEN_TYPE_FULL_ACCESS, true);

									getSubredditsPosts();
								}
								catch(OperationCanceledException e)
								{
									Log.e(TAG, "SubRedditSyncAdapter.getSubredditsPosts() - " + e.toString());
									e.printStackTrace();
								}
								catch(IOException e)
								{
									Log.e(TAG, "SubRedditSyncAdapter.getSubredditsPosts() - " + e.toString());
									e.printStackTrace();
								}
								catch(AuthenticatorException e)
								{
									Log.e(TAG, "SubRedditSyncAdapter.getSubredditsPosts() - " + e.toString());
									e.printStackTrace();
								}
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

								ContentResolver resolver = mContext.getContentResolver();

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

									ContentValues subredditValues = new ContentValues();
									subredditValues.put(SubredditContract.SubredditsEntry.COLUMN_LAST_DOWNLOADED,
											  cVVector.get(0).getAsString(SubredditContract.SubredditsPostsEntry.COLUMN_NAME));

									resolver.update(SubredditContract.SubredditsEntry.CONTENT_URI, subredditValues,
											  SubredditContract.SubredditsEntry._ID + " = ?", new String[]{String.valueOf(subredditId)});

									if(PreferencesUtils.getSelectedSubredditWidget(mContext).equals(String.valueOf(subredditId)))
									{
										Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
										mContext.sendBroadcast(dataUpdatedIntent);
									}
								}
							}
						}

						@Override
						public void onFailure(Call<GetSubredditsPostsResponse> call, Throwable t)
						{
							Log.e(TAG, "SubredditSyncAdapter.getSubredditsPosts() - " + t.toString());
						}
					});
				}
			}
		}
	}

	public static void onAccountCreated(Account newAccount, Context context)
	{
        /*
         * Since we've created an account
         */
		SubRedditSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
		ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
	}

	/**
	 * Helper method to schedule the sync adapter periodic execution
	 */
	public static void configurePeriodicSync(Context context, int syncInterval, int flexTime)
	{
		Account account = AccountManager.get(context).getAccountsByType(context.getString(R.string.sync_account_type))[0];//getSyncAccount(context);

		String authority = context.getString(R.string.content_authority);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			// we can enable inexact timers in our periodic sync
			SyncRequest request = new SyncRequest.Builder().
					  syncPeriodic(syncInterval, flexTime).
					  setSyncAdapter(account, authority).
					  setExtras(new Bundle()).build();
			ContentResolver.requestSync(request);
		}
		else
		{
			ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
		}
	}

	/**
	 * Helper method to have the sync adapter sync immediately
	 *
	 * @param context The context used to access the account service
	 */
	public static void syncImmediately(Context context)
	{
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(((AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE)).
							 getAccountsByType(context.getString(R.string.sync_account_type))[0],
				  context.getString(R.string.content_authority), bundle);
	}
}
