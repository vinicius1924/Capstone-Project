package com.example.vinicius.capstone.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.vinicius.capstone.R;

/**
 * Created by vinicius on 17/05/17.
 */

public final class PreferencesUtils
{
	public static void writeSyncAdapterInitialized(Context context, String key, boolean initialized)
	{
		SharedPreferences prefs = context.getSharedPreferences(context.getString(
				  R.string.sync_adapter_initialized_preferences_file_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, initialized);
		editor.apply();
	}

	public static boolean isSyncAdapterInitialized(Context context, String key)
	{
		SharedPreferences prefs = context.getSharedPreferences(context.getString(
				  R.string.sync_adapter_initialized_preferences_file_name), Context.MODE_PRIVATE);
		return prefs.getBoolean(key, false);
	}

	public static String getSelectedSubredditWidget(Context context)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getResources().getString(R.string.widget_preference_key), "");
	}
}
