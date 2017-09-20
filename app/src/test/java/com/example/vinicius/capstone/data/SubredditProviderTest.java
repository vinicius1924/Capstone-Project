package com.example.vinicius.capstone.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.compat.BuildConfig;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by vinicius on 18/09/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SubredditProviderTest
{
	private Context context;
	private SubRedditProvider subRedditProvider;
	private ContentValues values;

	@Before
	public void setup()
	{
		context = RuntimeEnvironment.application;
		values = new ContentValues();

		subRedditProvider = Robolectric.setupContentProvider(SubRedditProvider.class);
	}

	@Test
	public void insertSubredditLocalDatabase()
	{
		Uri uri = insertSubreddit(11, "Teste", "url", 0);

		assertNotNull(uri);
	}

	@Test
	public void insertSubredditPostLocalDatabase()
	{
		Uri uri = insertSubredditPost(1, "thumbnail", "title", 23, "Author", "name", "permalink", 1234, 11);
		assertNotNull(uri);
	}

	@Test
	public void querySubredditByIdLocalDatabase()
	{
		int id = 11;

		Uri uri = insertSubreddit(id, "Teste", "url", 0);

		assertNotNull(uri);

		Cursor cursor = querySubredditById(id);

		Assert.assertNotNull(cursor);
		assertThat(cursor.getCount(), greaterThan(0));
	}

	@Test
	public void querySubredditPostByIdLocalDatabase()
	{
		int id = 1;

		Uri uri = insertSubredditPost(id, "thumbnail", "title", 23, "Author", "name", "permalink", 1234, 11);
		assertNotNull(uri);

		Cursor cursor = querySubredditPostById(id);

		assertNotNull(cursor);
		assertThat(cursor.getCount(), greaterThan(0));
	}

	@Test
	public void queryAllSubreddits()
	{
		int id1 = 11;
		int id2 = 12;

		Uri uri1 = insertSubreddit(id1, "Teste1", "url1", 0);
		Uri uri2 = insertSubreddit(id2, "Teste2", "url2", 0);

		assertNotNull(uri1);
		assertNotNull(uri2);

		Cursor cursor = querySubreddits();

		Assert.assertNotNull(cursor);
		assertThat(cursor.getCount(), greaterThan(1));
	}

	@Test
	public void queryAllSubredditsPosts()
	{
		int id1 = 1;
		int id2 = 2;

		Uri uri1 = insertSubredditPost(id1, "thumbnail1", "title1", 23, "Author1", "name1", "permalink1", 1234, 11);
		Uri uri2 = insertSubredditPost(id2, "thumbnail2", "title2", 23, "Author2", "name2", "permalink2", 1234, 11);
		assertNotNull(uri1);
		assertNotNull(uri2);

		Cursor cursor = querySubredditPosts();

		assertNotNull(cursor);
		assertThat(cursor.getCount(), greaterThan(1));
	}

	private Uri insertSubreddit(int id, String name, String url, int subscribed)
	{
		values.clear();
		values.put(SubredditContract.SubredditsEntry._ID, id);
		values.put(SubredditContract.SubredditsEntry.COLUMN_NAME, name);
		values.put(SubredditContract.SubredditsEntry.COLUMN_URL, url);
		values.put(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED, subscribed);

		return subRedditProvider.insert(SubredditContract.SubredditsEntry.CONTENT_URI, values);
	}

	private Uri insertSubredditPost(long id, String thumbanil, String title, int numberOfComments,
											  String author, String name, String permalink, long createdUTC,
											  long subredditsId)
	{
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

		return subRedditProvider.insert(SubredditContract.SubredditsPostsEntry.CONTENT_URI, values);
	}

	private Cursor querySubredditById(int id)
	{
		return subRedditProvider.query(SubredditContract.SubredditsEntry.buildSubredditsUri(id), null, null,
				  null, null);
	}

	private Cursor querySubredditPostById(int id)
	{
		return subRedditProvider.query(SubredditContract.SubredditsPostsEntry.buildPostUri(id), null, null,
				  null, null);
	}

	private Cursor querySubreddits()
	{
		return subRedditProvider.query(SubredditContract.SubredditsEntry.CONTENT_URI, null, null, null, null);
	}

	private Cursor querySubredditPosts()
	{
		return subRedditProvider.query(SubredditContract.SubredditsPostsEntry.CONTENT_URI, null, null, null, null);
	}
}
