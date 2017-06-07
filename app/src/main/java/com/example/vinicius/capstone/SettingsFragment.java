package com.example.vinicius.capstone;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vinicius.capstone.data.SubredditContract;

import java.util.ArrayList;

import static com.example.vinicius.capstone.sync.SubRedditSyncAdapter.ACTION_DATA_UPDATED;

/**
 * Created by vinicius on 05/06/17.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
		  LoaderManager.LoaderCallbacks<Cursor>
{
	private ArrayList<String> entries = new ArrayList<>();
	private ArrayList<String> entryValues = new ArrayList<>();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
			  savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		view.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		String selected;

		if(key.equals(getResources().getString(R.string.widget_preference_key)))
		{
			Preference preference = findPreference(key);
			int index = ((ListPreference)preference).findIndexOfValue(sharedPreferences.getString(key, ""));

			if(index == -1)
			{
				selected = getResources().getString(R.string.prefDefaultValue);
			}
			else
			{
				selected = ((ListPreference) preference).getEntries()[index].toString();
			}

			preference.setSummary(selected);

			Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
			getActivity().sendBroadcast(dataUpdatedIntent);
		}
	}

	@Override
	public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		return new android.content.CursorLoader(getActivity().getBaseContext(),
				  SubredditContract.SubredditsEntry.CONTENT_URI,
				  null,
				  SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED + " = ?",
				  new String[]{String.valueOf(1)},
				  null);
	}

	@Override
	public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data)
	{
		if(data == null || !data.moveToFirst())
		{
			Preference preference = findPreference(getResources().getString(R.string.widget_preference_key));
			preference.setSummary(getResources().getString(R.string.prefDefaultValue));
		}
		else
		{
			do
			{
				entries.add(data.getString(data.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_NAME)));
				entryValues.add(String.valueOf(data.getInt(data.getColumnIndex(SubredditContract.SubredditsEntry._ID))));

				data.moveToNext();
			}while(!data.isAfterLast());
		}

		ListPreference listPreference = (ListPreference)findPreference(getResources().getString(R.string.widget_preference_key));

		listPreference.setEntries(entries.toArray(new String[entries.size()]));
		listPreference.setEntryValues(entryValues.toArray(new String[entryValues.size()]));

		/* Adapted from: http://codetheory.in/saving-user-settings-with-android-preferences/ */
		for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++)
		{
			pickPreferenceObject(getPreferenceScreen().getPreference(i));
		}
	}

	private void pickPreferenceObject(Preference preference)
	{
		if(preference instanceof PreferenceCategory)
		{
			PreferenceCategory cat = (PreferenceCategory) preference;

			for (int i = 0; i < cat.getPreferenceCount(); i++)
			{
				pickPreferenceObject(cat.getPreference(i));
			}
		}
		else
		{
			initSummary(preference);
		}
	}

	private void initSummary(Preference p)
	{
		if (p instanceof ListPreference)
		{
			ListPreference listPref = (ListPreference) p;
			int index = listPref.findIndexOfValue(listPref.getValue());

			/*
			 * se o index for -1 significa que o valor que foi selecionado não está na lista das entradas
			 * que deverão aparecer para o usuário selecionar, então a key widget_preference_key, que é a key
			 * usada para salvar o valor selecionado da lista, deve ser removida das SharedPreferences
			 */
			if(index == -1)
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove(getResources().getString(R.string.widget_preference_key));
				editor.commit();
			}
			else
			{
				String selected = listPref.getEntries()[index].toString();
				p.setSummary(selected);
			}
		}
	}

	@Override
	public void onLoaderReset(android.content.Loader<Cursor> loader)
	{

	}
}
