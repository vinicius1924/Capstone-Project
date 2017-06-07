package com.example.vinicius.capstone.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.vinicius.capstone.LoginActivity;
import com.example.vinicius.capstone.R;

/**
 * Created by vinicius on 16/05/17.
 */

public class SubRedditAuthenticator extends AbstractAccountAuthenticator
{
	private final Context mContext;

	public SubRedditAuthenticator(Context context)
	{
		super(context);

		mContext = context;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
	{
		return null;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[]
			  requiredFeatures, Bundle options) throws NetworkErrorException
	{
		Log.d("Authenticator", "SubRedditAuthenticator.addAccount()");

		AccountManager accountManager = AccountManager.get(mContext);

		/*
		 * Verifica se já existe uma conta do tipo capstone.example.com. Se já existir então retorna um bundle
		 * com um error corde e uma mensagem de erro para o callback do método chamador para que o mesmo trate
		 * o fato de já existir uma conta
		 */
		if(accountManager.getAccountsByType(mContext.getString(R.string.sync_account_type)).length > 0)
		{
			final Bundle result = new Bundle();

			result.putInt(AccountManager.KEY_ERROR_CODE, 400);
			result.putString(AccountManager.KEY_ERROR_MESSAGE, mContext.getResources().getString(R.string.one_account_allowed));

			return result;
		}

		/*
		 * Caso não exista uma conta do tipo capstone.example.com então é chamado LoginActivity para que o usuário
		 * escreva seu nome de usuário
		 */
		final Intent intent = new Intent(mContext, LoginActivity.class);
		// This key can be anything. Try to use your domain/package
		intent.putExtra(AccountGeneral.ARG_ACCOUNT_TYPE, accountType);
		/* This key can be anything too. It's just a way of identifying the token's type
		(used when there are multiple permissions) */
		intent.putExtra(AccountGeneral.ARG_AUTH_TOKEN_TYPE, authTokenType != null ? authTokenType :
				  AccountGeneral.ACCOUNT_TOKEN_TYPE_FULL_ACCESS);
		// This key can be anything too. Used for your reference. Can skip it too.
		//intent.putExtra(AccountGeneral.ARG_IS_ADDING_NEW_ACCOUNT, true);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);

		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws
			  NetworkErrorException
	{
		return null;
	}

	/*
	 * Quando eu chamo blockingGetAuthToken() na classe SubRedditSyncAdapter, se esse método verificar
	 * que a conta não tem um token associado a ela, que deve ser associado usando o método setAuthToken,
	 * então será chamado o método getAuthToken daqui de baixo que vai pegar um token para a conta
	 */
	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle
			  options) throws NetworkErrorException
	{
		Log.d("Authenticator", "SubRedditAuthenticator.getAuthToken()");
		// If the caller requested an authToken type we don't support, then
		// return an error
		if (!authTokenType.equals(AccountGeneral.ACCOUNT_TOKEN_TYPE_FULL_ACCESS))
		{
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
			return result;
		}

		// Extract the username and password from the Account Manager, and ask
		// the server for an appropriate AuthToken.
		final AccountManager accountManager = AccountManager.get(mContext);

		String authToken = accountManager.peekAuthToken(account, authTokenType);

		// Lets give another try to authenticate the user
//		if (TextUtils.isEmpty(authToken)) {
//			final String password = accountManager.getPassword(account);
//			if (password != null) {
//				authToken = serverAuthenticate.userSignIn(account.name, password, authTokenType);
//			}
//		}

		// If we get an authToken - we return it
		if (!TextUtils.isEmpty(authToken)) {
			final Bundle result = new Bundle();
			//result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
			return result;
		}


		Log.e("Authenticator", "Não existe authToken");

		// If we get here, then we couldn't access the user's password - so we
		// need to re-prompt them for their credentials. We do that by creating
		// an intent to display our AuthenticatorActivity.
		final Intent intent = new Intent(mContext, LoginActivity.class);
		//intent.putExtra(AccountGeneral.ARG_ACCOUNT_NAME, account.name);
		intent.putExtra(AccountGeneral.ARG_ACCOUNT_TYPE, account.type);
		intent.putExtra(AccountGeneral.ARG_AUTH_TOKEN_TYPE, authTokenType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType)
	{
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
											  Bundle options) throws NetworkErrorException
	{
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException
	{
		return null;
	}


}
