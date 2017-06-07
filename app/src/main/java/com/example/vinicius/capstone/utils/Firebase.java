package com.example.vinicius.capstone.utils;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by vinicius on 31/05/17.
 */

public class Firebase
{
	private static FirebaseAnalytics firebaseAnalytics = null;

	public static FirebaseAnalytics getFirebaseAnalytics(Context context)
	{
		if(firebaseAnalytics == null)
		{
			firebaseAnalytics = FirebaseAnalytics.getInstance(context);
		}

		return firebaseAnalytics;
	}
}
