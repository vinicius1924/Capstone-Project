package com.example.vinicius.capstone.interfaces;

import android.accounts.Account;
import android.content.Context;

import com.example.vinicius.capstone.utils.TokenImpl;

/**
 * Created by vinicius on 26/05/17.
 */

public interface IToken
{
	void refreshToken(final Context context, final Account account, final String token, final TokenImpl.IRefreshTokenResponse refreshTokenResponse);
	String getAccountTokenAsynchronously(Context context);
	void getAccountTokenSynchronously(Context context, final TokenImpl.ITokenResponse tokenResponse);
}
