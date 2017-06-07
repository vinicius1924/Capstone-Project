package com.example.vinicius.capstone.presenter;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.vinicius.capstone.PostsRecyclerAdapter;
import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.interfaces.IPostsMVP;
import com.example.vinicius.capstone.model.PostsModel;
import com.example.vinicius.capstone.view.PostsActivity;
import com.example.vinicius.capstone.view.ReadPostActivity;

import java.lang.ref.WeakReference;

/**
 * Created by vinicius on 29/05/17.
 */

public class PostsPresenter implements IPostsMVP.PresenterOps, IPostsMVP.RequiredPresenterOps,
		  PostsRecyclerAdapter.PostListItemClickListener
{
	/*
	 * Referência para layer View (neste caso MainActivity). Usa-se uma WeakReference pois
	 * a Activity pode ser destruída a qualquer momento e causar um memory leak
	 */
	private WeakReference<IPostsMVP.RequiredViewOps> mView;
	// Referência para o layer Model
	private IPostsMVP.ModelOps mModel;

	// Estado da mudança de configuração
	private boolean mIsChangingConfig;
	private AccountManager mAccountManager;

	public PostsPresenter(IPostsMVP.RequiredViewOps mView)
	{
		this.mView = new WeakReference<>(mView);
		this.mModel = new PostsModel(this);
		mAccountManager = AccountManager.get(getView().getActivityContext());
	}

	private IPostsMVP.RequiredViewOps getView() throws NullPointerException
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
	public void onConfigurationChanged(IPostsMVP.RequiredViewOps view)
	{

	}

	/**
	 * Recebe evento {@link PostsActivity#onDestroy()}
	 *
	 * @param isChangingConfig Se está mudando de config
	 */
	@Override
	public void onDestroy(boolean isChangingConfig)
	{
		mView = null;
		mIsChangingConfig = isChangingConfig;

		if(!mIsChangingConfig)
		{
			mModel.onDestroy();
		}
	}

	@Override
	public void onCreate()
	{
		mModel.initLoader();
	}

	@Override
	public void onSwipeRefresh()
	{
		mModel.loadMore25Posts();
	}

	@Override
	public IPostsMVP.ModelOps getModel()
	{
		return mModel;
	}

	@Override
	public void onError(String errorMsg)
	{

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
			Log.e(PostsActivity.POSTSACTIVITYTAG, "PostsPresenter.getAppContext() - " + e.toString());
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
			Log.e(PostsActivity.POSTSACTIVITYTAG, "PostsPresenter.getActivityContext() - " + e.toString());
			return null;
		}
	}

	@Override
	public AppCompatActivity getActivity()
	{
		return (AppCompatActivity) getView();
	}

	@Override
	public void onLoadDataFinished(Cursor data)
	{
		getView().onLoadDataFinished(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		getView().onLoaderReset(loader);
	}

	@Override
	public void noInternetConnection()
	{
		getView().showSnackBar(getView().getActivityContext().getResources()
				  .getString(R.string.no_internet_connection));
	}

	@Override
	public void onLoadPostsFinished()
	{
		getView().onLoadPostsFinished();
	}

	@Override
	public void showMessage(String message)
	{
		getView().showToast(message);
	}

	@Override
	public void onListItemClick(int postId)
	{
		Uri postUri = SubredditContract.SubredditsPostsEntry.buildPostUri(postId);

		Cursor postCursor = getView().getActivityContext().getContentResolver().
				  query(postUri, new String[]{SubredditContract.SubredditsPostsEntry.COLUMN_PERMALINK},
							 null, null, null);

		if(postCursor.moveToFirst())
		{
			String permalink = postCursor.getString(postCursor.getColumnIndex(SubredditContract.
					  SubredditsPostsEntry.COLUMN_PERMALINK));

			Log.d(PostsActivity.POSTSACTIVITYTAG, permalink);

			Intent intent = new Intent(getView().getActivityContext(), ReadPostActivity.class);
			intent.putExtra(ReadPostActivity.EXTRA_PERMALINK, permalink);

			getView().getActivityContext().startActivity(intent);

			Log.d(PostsActivity.POSTSACTIVITYTAG, permalink);
		}
	}
}
