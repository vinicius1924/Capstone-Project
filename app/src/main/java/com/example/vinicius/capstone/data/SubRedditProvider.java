package com.example.vinicius.capstone.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.vinicius.capstone.view.MainActivity;

/**
 * Created by vinicius on 17/05/17.
 */

public class SubRedditProvider extends ContentProvider
{
	/*
	 * Aqui precisa ser definido um identificador inteiro para cada URI que pretendemos escrever.
	 * Neste caso teremos dois identificadores para cada query. Uma URI para todas as linhas
	 * e uma URI para uma linha individual
	 */
	private static final int SUBREDDIT = 200;
	private static final int SUBREDDIT_ID = 201;

	private static final int SUBREDDIT_POSTS = 300;
	private static final int SUBREDDIT_POSTS_ID = 301;
	private static final int POST_ID = 302;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	/* Variavel usada para acessar o banco de dados */
	private SubredditDbHelper mOpenHelper;

	@Override
	public boolean onCreate()
	{
		mOpenHelper = new SubredditDbHelper(getContext());
		return true;
	}

	/*
	 * Os parâmetros deste metodo são:
	 *
	 * uri - a URI que deve ser consultada
	 * projection - um array de strings de colunas que serão retornadas no conjunto de resultados
	 * selection - especifica o critério para selecionar as linhas (equivalente ao WHERE col = value em SQL)
	 * selectionArgs - lista de argumentos que substituem os ? no parametro selection
	 * sortOrder - especifica a ordem na qual as linhas irão aparecer no Cursor retornado
	 */
	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[]
			  selectionArgs, @Nullable String sortOrder)
	{
		Log.d(MainActivity.MAINACTIVITYTAG, "SubRedditProvider.query()");
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor cursor = null;
		long _id;

		/*
		 * Se houver um match da URI passada para cosulta então será retornado o inteiro (terceiro parametro
		 * que foi adicionado dentro do método "matcher.addURI()") que foi
		 * definido para essa URI dentro do método buildUriMatcher()
		 */
		switch(sUriMatcher.match(uri))
		{
			case SUBREDDIT:
				cursor = db.query(SubredditContract.SubredditsEntry.TABLE_NAME,
						  projection,
						  selection,
						  selectionArgs,
						  null,
						  null,
						  sortOrder);
				break;

			case SUBREDDIT_ID:
				_id = ContentUris.parseId(uri);
				cursor = db.query(
						  SubredditContract.SubredditsEntry.TABLE_NAME,
						  projection,
						  SubredditContract.SubredditsEntry._ID + " = ?",
						  new String[]{String.valueOf(_id)},
						  null,
						  null,
						  sortOrder);
				break;

			case SUBREDDIT_POSTS:
				cursor = db.query(SubredditContract.SubredditsPostsEntry.TABLE_NAME,
						  projection,
						  selection,
						  selectionArgs,
						  null,
						  null,
						  sortOrder);
				break;

			case SUBREDDIT_POSTS_ID:
				_id = ContentUris.parseId(uri);
				cursor = db.query(
						  SubredditContract.SubredditsPostsEntry.TABLE_NAME,
						  projection,
						  SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID + " = ?",
						  new String[]{String.valueOf(_id)},
						  null,
						  null,
						  sortOrder);
				break;

			case POST_ID:
				_id = Long.parseLong(uri.getPathSegments().get(2));
				cursor = db.query(SubredditContract.SubredditsPostsEntry.TABLE_NAME,
						  projection,
						  SubredditContract.SubredditsPostsEntry._ID + " = ?",
						  new String[]{String.valueOf(_id)},
						  null,
						  null,
						  sortOrder);
				break;

		}

		/* notifica o listener do content resolver do chamador desta função sobre mudanças na uri */
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	/*
	 * Este método é usado para retornar o content type de cada URI que foi definida dentro do método
	 * buildUriMatcher()
	 */
	@Nullable
	@Override
	public String getType(@NonNull Uri uri)
	{
		/*
		 * Se houver um match da URI passada para cosulta então será retornado o inteiro (terceiro parametro
		 * que foi adicionado dentro do método "matcher.addURI()") que foi
		 * definido para essa URI dentro do método buildUriMatcher()
		 */
		switch(sUriMatcher.match(uri))
		{
			case SUBREDDIT:
				return SubredditContract.SubredditsEntry.CONTENT_TYPE;

			case SUBREDDIT_ID:
				return SubredditContract.SubredditsEntry.CONTENT_ITEM_TYPE;

			case SUBREDDIT_POSTS:
				return SubredditContract.SubredditsPostsEntry.CONTENT_TYPE;

			case SUBREDDIT_POSTS_ID:
				return SubredditContract.SubredditsPostsEntry.CONTENT_ITEM_TYPE;

			case POST_ID:
				return  SubredditContract.SubredditsPostsEntry.CONTENT_ITEM_TYPE;

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues)
	{
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long _id;
		Uri returnUri;

		/*
		 * Se houver um match da URI passada para cosulta então será retornado o inteiro (terceiro parametro
		 * que foi adicionado dentro do método "matcher.addURI()") que foi
		 * definido para essa URI dentro do método buildUriMatcher()
		 */
		switch(sUriMatcher.match(uri))
		{
			case SUBREDDIT:
				_id = db.insert(SubredditContract.SubredditsEntry.TABLE_NAME, null, contentValues);

				if(_id > 0)
				{
					returnUri = SubredditContract.SubredditsEntry.buildSubredditsUri(_id);
				}
				else
				{
					throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
				}

				break;

			case SUBREDDIT_POSTS:
				_id = db.insert(SubredditContract.SubredditsPostsEntry.TABLE_NAME, null, contentValues);

				if(_id > 0)
				{
					returnUri = SubredditContract.SubredditsPostsEntry.buildSubredditsPostsUri(_id);
				}
				else
				{
					throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
				}

				break;

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		/* Este método é usado para notificar os listeners que aconteceu uma mudança na uri */
		getContext().getContentResolver().notifyChange(uri, null);
		return returnUri;
	}

	@Override
	public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)
	{
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rows;
		long _id;

		switch(sUriMatcher.match(uri))
		{
			case SUBREDDIT:
				rows = db.delete(SubredditContract.SubredditsEntry.TABLE_NAME, selection, selectionArgs);
				break;

			case SUBREDDIT_ID:
				_id = ContentUris.parseId(uri);
				rows = db.delete(SubredditContract.SubredditsEntry.TABLE_NAME, SubredditContract.SubredditsEntry._ID + " = ?",
						  new String[]{String.valueOf(_id)});
				break;

			case SUBREDDIT_POSTS:
				rows = db.delete(SubredditContract.SubredditsPostsEntry.TABLE_NAME, selection, selectionArgs);
				break;

			case SUBREDDIT_POSTS_ID:
				_id = ContentUris.parseId(uri);
				rows = db.delete(SubredditContract.SubredditsPostsEntry.TABLE_NAME,
						  SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID + " = ?",
						  new String[]{String.valueOf(_id)});
				break;

			case POST_ID:
				_id = Long.parseLong(uri.getPathSegments().get(2));
				rows = db.delete(SubredditContract.SubredditsPostsEntry.TABLE_NAME,
						  SubredditContract.SubredditsPostsEntry._ID + " = ?",
						  new String[]{String.valueOf(_id)});
				break;

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		if(selection == null || rows != 0)
		{
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rows;
	}

	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[]
			  selectionArgs)
	{
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rows;

		switch(sUriMatcher.match(uri))
		{
			case SUBREDDIT:
				rows = db.update(SubredditContract.SubredditsEntry.TABLE_NAME, values, selection, selectionArgs);
				break;

			case SUBREDDIT_POSTS:
				rows = db.update(SubredditContract.SubredditsPostsEntry.TABLE_NAME, values, selection, selectionArgs);
				break;

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		if(rows != 0){
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rows;

	}

	@Override
	public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values)
	{
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		switch (match)
		{
			case SUBREDDIT:
				db.beginTransaction();
				int returnCount = 0;
				try
				{
					for (ContentValues value : values)
					{
						long _id = db.insert(SubredditContract.SubredditsEntry.TABLE_NAME, null, value);

						if (_id != -1)
						{
							returnCount++;
						}
					}

					db.setTransactionSuccessful();
				}
				finally
				{
					db.endTransaction();
				}

				getContext().getContentResolver().notifyChange(uri, null);

				return returnCount;

			default:
				return super.bulkInsert(uri, values);
		}
	}

	/* Constói uma UriMatcher que é usada para determinar qual requisição ao banco de dados está sendo feita */
	public static UriMatcher buildUriMatcher(){
		String content = SubredditContract.CONTENT_AUTHORITY;

		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(content, SubredditContract.PATH_SUBREDDITS, SUBREDDIT);
		/*
		 * O /# é usado como placeholder para um valor numérico. Neste caso usaríamos
		 *	content://com.example.vinicius.capstone/subreddits/1 para o subreddit com id 1
		 */
		matcher.addURI(content, SubredditContract.PATH_SUBREDDITS + "/#", SUBREDDIT_ID);

		matcher.addURI(content, SubredditContract.PATH_SUBREDDITS_POSTS, SUBREDDIT_POSTS);
		matcher.addURI(content, SubredditContract.PATH_SUBREDDITS_POSTS + "/#", SUBREDDIT_POSTS_ID);
		matcher.addURI(content, SubredditContract.PATH_SUBREDDITS_POSTS + "/postId/#", POST_ID);

		return matcher;
	}
}
