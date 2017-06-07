package com.example.vinicius.capstone.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vinicius on 25/05/17.
 */

public class SubredditDbHelper extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;

	static final String DATABASE_NAME = "subreddit.db";

	public SubredditDbHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase)
	{
		final String SQL_CREATE_SUBREDDITS_TABLE = "CREATE TABLE " + SubredditContract.SubredditsEntry.TABLE_NAME + " (" +
				  SubredditContract.SubredditsEntry._ID + " INTEGER PRIMARY KEY, " +
				  SubredditContract.SubredditsEntry.COLUMN_NAME + " TEXT NOT NULL, " +
				  SubredditContract.SubredditsEntry.COLUMN_URL + " TEXT NOT NULL, " +
				  SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED + " INTEGER NOT NULL " +
				  " );";

		final String SQL_CREATE_POSTS_TABLE = "CREATE TABLE " + SubredditContract.SubredditsPostsEntry.TABLE_NAME + " (" +
				  SubredditContract.SubredditsPostsEntry._ID + " INTEGER PRIMARY KEY, " +
				  SubredditContract.SubredditsPostsEntry.COLUMN_THUMBNAIL + " TEXT, " +
				  SubredditContract.SubredditsPostsEntry.COLUMN_TITLE + " TEXT, " +
				  SubredditContract.SubredditsPostsEntry.COLUMN_NUM_COMMENTS + " INTEGER, " +
				  SubredditContract.SubredditsPostsEntry.COLUMN_AUTHOR + " TEXT, " +
				  SubredditContract.SubredditsPostsEntry.COLUMN_NAME + " TEXT, " +
				  SubredditContract.SubredditsPostsEntry.COLUMN_PERMALINK + " TEXT, " +
				  SubredditContract.SubredditsPostsEntry.COLUMN_CREATED_UTC + " INTEGER, " +
				  SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID + " INTEGER NOT NULL " +
				  " );";


		sqLiteDatabase.execSQL(SQL_CREATE_SUBREDDITS_TABLE);
		sqLiteDatabase.execSQL(SQL_CREATE_POSTS_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
	{
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SubredditContract.SubredditsEntry.TABLE_NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SubredditContract.SubredditsPostsEntry.TABLE_NAME);

		onCreate(sqLiteDatabase);
	}
}
