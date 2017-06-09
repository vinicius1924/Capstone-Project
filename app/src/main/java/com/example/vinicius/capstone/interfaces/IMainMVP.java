package com.example.vinicius.capstone.interfaces;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by vinicius on 16/05/17.
 */

public interface IMainMVP
{
	/**
	 * Métodos obrigatórios em view, disponíveis para presenter
	 * presenter -> view
	 */
	interface RequiredViewOps
	{
		void showSnackBar(String message);

		void finishActivity();

		void onLoadDataFinished(Cursor data);

		void onLoaderReset(android.support.v4.content.Loader<Cursor> loader);

		Context getAppContext();

		Context getActivityContext();

		void setProgressBarVisibility(int visibility);

		// qualquer outra operação na UI
	}

	/**
	 * Métodos obrigatorios em presenter, disponíveis para view.
	 * Operações oferecidas ao layer View para comunicação com Presenter
	 * view -> presenter
	 */
	interface PresenterOps
	{
		void onConfigurationChanged(RequiredViewOps view);

		void onDestroy(boolean isChangingConfig);

		void onCreate();

		void onStart();

		void onResume();

		void onPause();

		void onStop();

		void getDefaultSubreddits();

		boolean onOptionsItemSelected(MenuItem item);

		IMainMVP.ModelOps getModel();

		// qualquer outra operação a ser chamada pelo View
	}

	/**
	 * operações oferecidas pelo layer Presenter para comunicações com Model
	 * Model -> Presenter
	 */
	interface RequiredPresenterOps
	{
		void onErrorGetDefaultSubreddits(String errorMsg);

		Context getAppContext();

		Context getActivityContext();

		AppCompatActivity getActivity();

		void onLoadDataFinished(Cursor data);

		void onLoaderReset(android.support.v4.content.Loader<Cursor> loader);

		void noInternetConnection();

		void progressBarVisibility(int visibility);
		// qualquer operação de retorno Model -> Presenter
	}

	/**
	 * operações oferecidos pelo layer Model para comunicações com Presenter
	 * Presenter -> Model
	 */
	interface ModelOps
	{
		void startLoader();

		void getDefaultSubreddits();
		// Qualquer operação referente à dados a ser chamado pelo Presenter
	}
}
