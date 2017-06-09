package com.example.vinicius.capstone.presenter;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.SettingsFragment;
import com.example.vinicius.capstone.SubredditsRecyclerAdapter;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.interfaces.IMainMVP;
import com.example.vinicius.capstone.model.MainModel;
import com.example.vinicius.capstone.sync.SubRedditSyncAdapter;
import com.example.vinicius.capstone.utils.Firebase;
import com.example.vinicius.capstone.utils.PreferencesUtils;
import com.example.vinicius.capstone.view.MainActivity;
import com.example.vinicius.capstone.view.PostsActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.example.vinicius.capstone.sync.AccountGeneral.ACCOUNT_TOKEN_TYPE_FULL_ACCESS;

/**
 * Created by vinicius on 16/05/17.
 */

public class MainPresenter implements IMainMVP.PresenterOps, IMainMVP.RequiredPresenterOps,
		  SubredditsRecyclerAdapter.SubredditListItemClickListener
{
	/*
	 * Referência para layer View (neste caso MainActivity). Usa-se uma WeakReference pois
	 * a Activity pode ser destruída a qualquer momento e causar um memory leak
	 */
	private WeakReference<IMainMVP.RequiredViewOps> mView;
	// Referência para o layer Model
	private IMainMVP.ModelOps mModel;
	private AccountManager mAccountManager;
	private FirebaseAnalytics mFirebaseAnalytics;

	public MainPresenter(IMainMVP.RequiredViewOps mView)
	{
		this.mView = new WeakReference<>(mView);
		this.mModel = new MainModel(this);

		mAccountManager = AccountManager.get(getView().getActivityContext());
		mFirebaseAnalytics = Firebase.getFirebaseAnalytics(getView().getActivityContext());
	}

	private IMainMVP.RequiredViewOps getView() throws NullPointerException
	{
		if(mView != null)
		{
			return mView.get();
		}
		else
		{
			throw new NullPointerException("View is unavailable");
		}
	}

	@Override
	public IMainMVP.ModelOps getModel()
	{
		return mModel;
	}

	@Override
	public AppCompatActivity getActivity()
	{
		return (AppCompatActivity) getView();
	}

	@Override
	public void onLoadDataFinished(Cursor data)
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainPresenter.onLoadDataFinished()");
		getView().onLoadDataFinished(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainPresenter.onLoaderReset()");
		getView().onLoaderReset(loader);
	}

	@Override
	public void noInternetConnection()
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainPresenter.noInternetConnection()");
		getView().showSnackBar(getView().getActivityContext().getResources()
				  .getString(R.string.no_internet_connection));
		getView().setProgressBarVisibility(View.INVISIBLE);
	}

	@Override
	public void onCreate()
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainPresenter.onCreate()");
		Log.d("Authenticator", "MainPresenter.onCreate()");
		addAccount();
		mModel.startLoader();
	}

	private void addAccount()
	{
		Log.d("Authenticator", "MainPresenter.addAccount()");
		/* Chama o método addAccount da classe SubRedditAuthenticator */
		mAccountManager.addAccount(getView().getActivityContext().getString(R.string.sync_account_type), ACCOUNT_TOKEN_TYPE_FULL_ACCESS, null, null, (AppCompatActivity) getView(), new AccountManagerCallback<Bundle>()
		{
			@Override
			public void run(AccountManagerFuture<Bundle> future)
			{
				try
				{
					Bundle bundle = future.getResult();
					int error = bundle.getInt(AccountManager.KEY_ERROR_CODE);

					if(error == 400)
					{
						Log.d(MainActivity.MAINACTIVITYTAG, "Já existe uma conta criada");

						if(!PreferencesUtils.isSyncAdapterInitialized(getView().getActivityContext(),
								  getView().getActivityContext().
											 getString(R.string.sync_adapter_initialized_preferences_key)))
						{
							Log.d(MainActivity.MAINACTIVITYTAG, "SyncAdapter not initialized");

							PreferencesUtils.writeSyncAdapterInitialized(getView().getActivityContext(),
									  getView().getActivityContext().
												 getString(R.string.sync_adapter_initialized_preferences_key), true);

							SubRedditSyncAdapter.onAccountCreated(mAccountManager.getAccountsByType(getView().
									  getActivityContext().getString(R.string.sync_account_type))[0], getView().
									  getActivityContext());
						}
					}
				}
				catch(OperationCanceledException | IOException | AuthenticatorException e)
				{
					Log.e(MainActivity.MAINACTIVITYTAG, "MainPresenter.addAccount() - " + e.toString());
				}
			}
		}, null);

		if(mAccountManager.getAccountsByType(getView().getActivityContext().getString(R.string.sync_account_type)).length == 0)
		{
			Log.d(MainActivity.MAINACTIVITYTAG, "Finish MainActivity");
			getView().finishActivity();
		}
	}

	@Override
	public void onStart()
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainPresenter.onStart()");
		getDefaultSubreddits();
	}

	@Override
	public void getDefaultSubreddits()
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "MainPresenter.getDefaultSubreddits()");
		mModel.getDefaultSubreddits();
	}

	@Override
	public void onResume()
	{

	}

	@Override
	public void onPause()
	{

	}

	/**
	 * Disparado por Activity após mudança de configuração
	 *
	 * @param view Referência para View
	 */
	@Override
	public void onConfigurationChanged(IMainMVP.RequiredViewOps view)
	{
		mView = new WeakReference<>(view);
	}

	@Override
	public void onDestroy(boolean isChangingConfig)
	{

	}

	@Override
	public void onStop()
	{

	}

	@Override
	public void onErrorGetDefaultSubreddits(String errorMsg)
	{
		getView().showSnackBar(errorMsg);
		getView().setProgressBarVisibility(View.INVISIBLE);
	}

	@Override
	public Context getAppContext()
	{
		try
		{
			return getView().getAppContext();
		}
		catch(NullPointerException e)
		{
			Log.e(MainActivity.MAINACTIVITYTAG, "MainPresenter.getAppContext() - " + e.toString());
			return null;
		}
	}

	@Override
	public Context getActivityContext()
	{
		try
		{
			return getView().getActivityContext();
		}
		catch(NullPointerException e)
		{
			Log.e(MainActivity.MAINACTIVITYTAG, "MainPresenter.getActivityContext() - " + e.toString());
			return null;
		}
	}

	@Override
	public void onListItemClick(int subredditId)
	{
		Uri subredditUri = SubredditContract.SubredditsEntry.buildSubredditsUri(subredditId);

		Cursor subredditCursor = getView().getActivityContext().getContentResolver().
				  query(subredditUri, null, null, null, null);

		if(subredditCursor.moveToFirst())
		{
			Bundle bundle = new Bundle();

			bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, subredditCursor.getString(subredditCursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_NAME)));
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, subredditCursor.getString(subredditCursor.getColumnIndex(SubredditContract.SubredditsEntry._ID)));
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

			if(subredditCursor.getInt(subredditCursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED)) == 1)
			{
				String subredditUrl = subredditCursor.getString(subredditCursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_URL));
				String subredditName = subredditCursor.getString(subredditCursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_NAME));

				Log.d(MainActivity.MAINACTIVITYTAG, "SUBSCRIBED");
				Intent intent = new Intent(getView().getActivityContext(), PostsActivity.class);
				intent.putExtra(PostsActivity.EXTRA_SUBREDDIT_ID, subredditId);
				intent.putExtra(PostsActivity.EXTRA_SUBREDDIT_URL, subredditUrl);
				intent.putExtra(PostsActivity.EXTRA_SUBREDDIT_NAME, subredditName);
				getView().getActivityContext().startActivity(intent);
			}
			else
			{
				Log.d(MainActivity.MAINACTIVITYTAG, "UNSUBSCRIBED");
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_settings:
				((AppCompatActivity)getView()).getFragmentManager().beginTransaction()
						  .replace(android.R.id.content, new SettingsFragment())
						  .addToBackStack("Settings")
						  .commit();
				return true;

			default:
				return false;
		}
	}

	@Override
	public void progressBarVisibility(int visibility)
	{
		getView().setProgressBarVisibility(visibility);
	}
}
