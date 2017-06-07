package com.example.vinicius.capstone.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by vinicius on 21/03/17.
 */

public final class NetworkUtils
{
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo == null)
			return false;
		if (!netInfo.isConnected())
			return false;
		if (!netInfo.isAvailable())
			return false;
		return true;
	}
}
