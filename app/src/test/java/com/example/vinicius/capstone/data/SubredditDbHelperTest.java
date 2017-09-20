package com.example.vinicius.capstone.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.compat.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertThat;


/**
 * Created by vinicius on 06/09/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SubredditDbHelperTest
{
	private Context context;
	private ContentResolver contentResolver;
	private ContentValues values;
	private SubRedditProvider subRedditProvider;
	private SubredditDbHelper subredditDbHelper;
	private SQLiteDatabase sqLiteDatabase;

	@Before
	public void setup()
	{
		context = RuntimeEnvironment.application;
		subredditDbHelper = new SubredditDbHelper(context);
		/*
		 * quando getReadableDatabase é chamado o método onCreate da classe SubredditDbHelper
		 * é chamado e o banco de dados é criado no telefone
		 */
		sqLiteDatabase = subredditDbHelper.getReadableDatabase();
		values = new ContentValues();
		//subRedditProvider = Robolectric.setupContentProvider(SubRedditProvider.class);

//		ContentProviderController<SubRedditProvider> providerController = Robolectric.buildContentProvider(
//				  SubRedditProvider.class).create();
		//providerController.get().onCreate();
//
//		subRedditProvider = providerController.get();
		//contentResolver = context.getContentResolver();
	}

	@After
	public void cleanup()
	{
		sqLiteDatabase.close();
	}

	@Test
	public void testIfDbCreated()
	{
		// Verify is the DB is opening correctly
		assertNotNull("Database not created", sqLiteDatabase);
		assertTrue("DB didn't open", sqLiteDatabase.isOpen());
		sqLiteDatabase.close();
	}

	@Test
	public void testTablesColumnsCreation()
	{
		Cursor cursorSubreddits = sqLiteDatabase.query(SubredditContract.SubredditsEntry.TABLE_NAME,
				  null, null, null, null, null, null);
		assertNotNull(cursorSubreddits);

		String[] columnsSubreddits = cursorSubreddits.getColumnNames();

		assertThat("Column not implemented: " + SubredditContract.SubredditsEntry._ID,
				  columnsSubreddits, hasItemInArray(SubredditContract.SubredditsEntry._ID));
		assertThat("Column not implemented: " + SubredditContract.SubredditsEntry.COLUMN_NAME,
				  columnsSubreddits, hasItemInArray(SubredditContract.SubredditsEntry.COLUMN_NAME));
		assertThat("Column not implemented: " + SubredditContract.SubredditsEntry.COLUMN_URL,
				  columnsSubreddits, hasItemInArray(SubredditContract.SubredditsEntry.COLUMN_URL));
		assertThat("Column not implemented: " + SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED,
				  columnsSubreddits, hasItemInArray(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED));
		assertThat("Column not implemented: " + SubredditContract.SubredditsEntry.COLUMN_LAST_DOWNLOADED,
				  columnsSubreddits, hasItemInArray(SubredditContract.SubredditsEntry.COLUMN_LAST_DOWNLOADED));

		cursorSubreddits.close();

		Cursor cursorSubredditsPosts = sqLiteDatabase.query(SubredditContract.SubredditsPostsEntry.TABLE_NAME,
				  null, null, null, null, null, null);
		assertNotNull(cursorSubredditsPosts);

		String[] columnsSubredditsPosts = cursorSubredditsPosts.getColumnNames();

		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry._ID,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry._ID));
		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry.COLUMN_THUMBNAIL,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry.COLUMN_THUMBNAIL));
		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry.COLUMN_TITLE,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry.COLUMN_TITLE));
		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry.COLUMN_NUM_COMMENTS,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry.COLUMN_NUM_COMMENTS));
		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry.COLUMN_AUTHOR,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry.COLUMN_AUTHOR));
		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry.COLUMN_NAME,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry.COLUMN_NAME));
		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry.COLUMN_PERMALINK,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry.COLUMN_PERMALINK));
		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry.COLUMN_CREATED_UTC,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry.COLUMN_CREATED_UTC));
		assertThat("Column not implemented: " + SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID,
				  columnsSubredditsPosts, hasItemInArray(SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID));

		cursorSubredditsPosts.close();

	}

	@Test
	public void testDBDelete()
	{
		assertTrue(context.deleteDatabase(SubredditDbHelper.DATABASE_NAME));
	}

	@Test
	public void testClose() throws Exception
	{
		assertTrue(sqLiteDatabase.isOpen());
		subredditDbHelper.close();
		assertFalse(sqLiteDatabase.isOpen());
	}

//	@Test
//	public void testCloseMultipleDbs() throws Exception
//	{
//		System.out.println("chamei testCloseMultipleDbs");
//		SubredditDbHelper subredditDbHelper2 = new SubredditDbHelper(context);
//		SQLiteDatabase database1 = subredditDbHelper.getWritableDatabase();
//		SQLiteDatabase database2 = subredditDbHelper2.getWritableDatabase();
//		assertTrue(database1.isOpen());
//		assertTrue(database2.isOpen());
//		subredditDbHelper.close();
//		assertFalse(database1.isOpen());
//		assertTrue(database2.isOpen());
//		subredditDbHelper2.close();
//		assertFalse(database2.isOpen());
//	}

	@Test
	public void insertSubredditLocalDatabase()
	{
		long id = insertSubreddit(11, "Teste", "url", 0);
		long zero = 0;
		assertThat(id, greaterThanOrEqualTo(zero));
	}

	@Test
	public void deleteSubredditLocalDatabase()
	{
		long id = insertSubreddit(11, "Teste", "url", 0);
		long zero = 0;
		assertThat(id, greaterThanOrEqualTo(zero));

		int numberOfRowsAffected = deleteSubreddit(id);
		assertThat(numberOfRowsAffected, greaterThan(0));
	}

	@Test
	public void insertSubredditPostLocalDatabase()
	{
		System.out.println("chamei insertSubredditPostLocalDatabase");

		long id = insertSubredditPost(1, "thumbnail", "title", 23, "Author", "name", "permalink", 1234, 11);
		long zero = 0;

		assertThat(id, greaterThanOrEqualTo(zero));
	}

	@Test
	public void deleteSubredditPostLocalDatabase()
	{
		long id = insertSubredditPost(1, "thumbnail", "title", 23, "Author", "name", "permalink", 1234, 11);
		long zero = 0;
		assertThat(id, greaterThanOrEqualTo(zero));

		int numberOfRowsAffected = deleteSubredditPost(id);
		assertThat(numberOfRowsAffected, greaterThan(0));
	}

	@Test
	public void querySubreddits()
	{
		System.out.println("chamei querySubreddits");
		insertSubreddit(11, "Teste", "url", 0);
		//Uri uri = insertSubreddit(1, "Potato");
		//Cursor cursor = contentResolver.query(SubredditContract.SubredditsEntry.CONTENT_URI, null, null, null, null);
		//Cursor cursor = subRedditProvider.query(SubredditContract.SubredditsEntry.CONTENT_URI, null, null, null, null);
		Cursor cursor = sqLiteDatabase.query(SubredditContract.SubredditsEntry.TABLE_NAME, null, null, null, null, null, null);

		//System.out.println("uri = " + uri);

		assertNotNull(cursor);
		assertThat(cursor.getCount(), greaterThan(0));

		cursor.close();
	}

	@Test
	public void querySubredditsById()
	{
		long id = insertSubreddit(11, "Teste", "url", 0);
		Cursor cursor = sqLiteDatabase.query(SubredditContract.SubredditsEntry.TABLE_NAME, null,
				  SubredditContract.SubredditsEntry._ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

		assertNotNull(cursor);
		assertThat(cursor.getCount(), greaterThan(0));

		cursor.moveToFirst();

		assertThat(cursor.getLong(cursor.getColumnIndex(SubredditContract.SubredditsEntry._ID)), equalTo(id));

		cursor.close();
	}

	@Test
	public void querySubredditsPosts()
	{
		System.out.println("chamei querySubredditsPosts");
		insertSubredditPost(1, "thumbnail", "title", 23, "Author", "name", "permalink", 1234, 11);

		Cursor cursor = sqLiteDatabase.query(SubredditContract.SubredditsPostsEntry.TABLE_NAME, null, null, null, null, null, null);

		assertNotNull(cursor);
		assertThat(cursor.getCount(), greaterThan(0));

		cursor.close();
	}

	@Test
	public void querySubredditsPostsById()
	{
		long id = insertSubredditPost(1, "thumbnail", "title", 23, "Author", "name", "permalink", 1234, 11);
		Cursor cursor = sqLiteDatabase.query(SubredditContract.SubredditsPostsEntry.TABLE_NAME, null,
				  SubredditContract.SubredditsPostsEntry._ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

		assertNotNull(cursor);
		assertThat(cursor.getCount(), greaterThan(0));

		cursor.moveToFirst();

		assertThat(cursor.getLong(cursor.getColumnIndex(SubredditContract.SubredditsPostsEntry._ID)), equalTo(id));

		cursor.close();
	}

	@Test
	public void queryPostsOfASubredditByHisId()
	{
		long idSubreddit = insertSubreddit(11, "Teste", "url", 0);
		insertSubredditPost(1, "thumbnail", "title", 23, "Author", "name", "permalink", 1234, 11);

		Cursor cursorSubreddit = sqLiteDatabase.query(SubredditContract.SubredditsEntry.TABLE_NAME, null,
				  SubredditContract.SubredditsEntry._ID + " = ?", new String[]{String.valueOf(idSubreddit)}, null, null, null);

		assertNotNull(cursorSubreddit);
		assertThat(cursorSubreddit.getCount(), greaterThan(0));

		cursorSubreddit.moveToFirst();

		Cursor cursorSubredditPosts = sqLiteDatabase.query(SubredditContract.SubredditsPostsEntry.TABLE_NAME, null,
				  SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID + " = ?",
				  new String[]{String.valueOf(cursorSubreddit.getLong(cursorSubreddit.getColumnIndex(
				  		  SubredditContract.SubredditsEntry._ID)))},
				  null, null, null);

		assertNotNull(cursorSubredditPosts);
		assertThat(cursorSubredditPosts.getCount(), greaterThan(0));
		cursorSubredditPosts.moveToFirst();
		long subredditId = cursorSubredditPosts.getLong(cursorSubredditPosts.getColumnIndex(
				  SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID));

		assertThat(subredditId, equalTo(idSubreddit));

		cursorSubreddit.close();
		cursorSubredditPosts.close();
	}

	private long insertSubreddit(int id, String name, String url, int subscribed)
	{
		System.out.println("chamei insertSubreddit");
		values.clear();
		values.put(SubredditContract.SubredditsEntry._ID, id);
		values.put(SubredditContract.SubredditsEntry.COLUMN_NAME, name);
		values.put(SubredditContract.SubredditsEntry.COLUMN_URL, url);
		values.put(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED, subscribed);
		//return contentResolver.insert(SubredditContract.SubredditsEntry.CONTENT_URI, values);
		//return subRedditProvider.insert(SubredditContract.SubredditsEntry.CONTENT_URI, values);
		return sqLiteDatabase.insert(SubredditContract.SubredditsEntry.TABLE_NAME, null, values);
	}

	private int deleteSubreddit(long id)
	{
		return sqLiteDatabase.delete(SubredditContract.SubredditsEntry.TABLE_NAME,
				  SubredditContract.SubredditsEntry._ID + " = ?", new String[]{String.valueOf(id)});
	}

	private long insertSubredditPost(long id, String thumbanil, String title, int numberOfComments,
												String author, String name, String permalink, long createdUTC,
												long subredditsId)
	{
		System.out.println("chamei insertSubredditPost");
		values.clear();
		values.put(SubredditContract.SubredditsPostsEntry._ID, id);
		values.put(SubredditContract.SubredditsPostsEntry.COLUMN_THUMBNAIL, thumbanil);
		values.put(SubredditContract.SubredditsPostsEntry.COLUMN_TITLE, title);
		values.put(SubredditContract.SubredditsPostsEntry.COLUMN_NUM_COMMENTS, numberOfComments);
		values.put(SubredditContract.SubredditsPostsEntry.COLUMN_AUTHOR, author);
		values.put(SubredditContract.SubredditsPostsEntry.COLUMN_NAME, name);
		values.put(SubredditContract.SubredditsPostsEntry.COLUMN_PERMALINK, permalink);
		values.put(SubredditContract.SubredditsPostsEntry.COLUMN_CREATED_UTC, createdUTC);
		values.put(SubredditContract.SubredditsPostsEntry.COLUMN_SUBREDDITS_ID, subredditsId);

		return sqLiteDatabase.insert(SubredditContract.SubredditsPostsEntry.TABLE_NAME, null, values);
	}

	private int deleteSubredditPost(long id)
	{
		return sqLiteDatabase.delete(SubredditContract.SubredditsPostsEntry.TABLE_NAME,
				  SubredditContract.SubredditsPostsEntry._ID + " = ?", new String[]{String.valueOf(id)});
	}
}