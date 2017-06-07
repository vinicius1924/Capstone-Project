package com.example.vinicius.capstone.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SubRedditSyncService extends Service
{
	private static final Object sSyncAdapterLock = new Object();
	private static SubRedditSyncAdapter sSubredditSyncAdapter = null;

	@Override
	public void onCreate()
	{
		synchronized (sSyncAdapterLock){
			if(sSubredditSyncAdapter == null) {
				sSubredditSyncAdapter = new SubRedditSyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return sSubredditSyncAdapter.getSyncAdapterBinder();
	}
}
