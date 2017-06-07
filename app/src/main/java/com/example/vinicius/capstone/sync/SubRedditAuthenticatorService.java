package com.example.vinicius.capstone.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SubRedditAuthenticatorService extends Service
{
	// Instance field that stores the authenticator object
	private SubRedditAuthenticator mAuthenticator;

	@Override
	public void onCreate()
	{
		// Create a new authenticator object
		mAuthenticator = new SubRedditAuthenticator(this);
	}

	/*
	 * When the system binds to this Service to make the RPC call
	 * return the authenticator's IBinder.
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		Log.d("Authenticator", "SubRedditAuthenticatorService.onBind()");
		return mAuthenticator.getIBinder();
	}
}
