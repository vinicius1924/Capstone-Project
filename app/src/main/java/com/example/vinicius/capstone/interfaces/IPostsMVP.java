package com.example.vinicius.capstone.interfaces;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by vinicius on 29/05/17.
 */

public interface IPostsMVP
{
	/**
	 * Métodos obrigatórios em view, disponíveis para presenter
	 * presenter -> view
	 */
	interface RequiredViewOps
	{
		void showSnackBar(String message);

		void showToast(String msg);

		void onLoadDataFinished(Cursor data);

		void onLoaderReset(android.support.v4.content.Loader<Cursor> loader);

		void progressBarVisibility(int visibility);

		void swipeRefreshEnabled(boolean enabled);

		void onSwipeRefreshStopped();

		Context getAppContext();

		Context getActivityContext();

		void onLoadPostsFinished();
	}

	/**
	 * Métodos obrigatorios em presenter, disponíveis para view.
	 * Operações oferecidas ao layer View para comunicação com Presenter
	 * view -> presenter
	 */
	interface PresenterOps
	{
		void onConfigurationChanged(IPostsMVP.RequiredViewOps view);

		void onDestroy(boolean isChangingConfig);

		void onCreate();

		void onStart();

		void onResume();

		void onStop();

		void onPause();

		void onSwipeRefresh();

		void onListItemSwiped(int postId);

		IPostsMVP.ModelOps getModel();

		// qualquer outra operação a ser chamada pelo View
	}

	/**
	 * operações oferecidas pelo layer Presenter para comunicações com Model
	 * Model -> Presenter
	 */
	interface RequiredPresenterOps
	{
		void onError(String errorMsg);

		Context getAppContext();

		Context getActivityContext();

		AppCompatActivity getActivity();

		void onLoadDataFinished(Cursor data);

		void onLoaderReset(android.support.v4.content.Loader<Cursor> loader);

		void noInternetConnection();

		void onLoadPostsFinished();

		void showMessageOnToast(String message);

		void progressBarVisibility(int visibility);

		void swipeRefreshEnabled(boolean enabled);

		void loadMore25PostsRequestStopped();

		// qualquer operação de retorno Model -> Presenter
	}

	/**
	 * operações oferecidos pelo layer Model para comunicações com Presenter
	 * Presenter -> Model
	 */
	interface ModelOps
	{
		void startLoader();

		void onStart();

		void onStop();

		void loadMore25Posts();

		void removePost(int postId);

		void fetchSubredditPosts();

		void onDestroy();
		// Qualquer operação referente à dados a ser chamado pelo Presenter
	}
}
