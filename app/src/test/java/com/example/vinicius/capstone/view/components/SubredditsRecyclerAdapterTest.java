package com.example.vinicius.capstone.view.components;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.support.compat.BuildConfig;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.SubredditsRecyclerAdapter;
import com.example.vinicius.capstone.data.SubRedditProvider;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.view.MainActivity;
import com.example.vinicius.capstone.view.PostsActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by vinicius on 19/09/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, packageName = "com.example.vinicius.capstone")
public class SubredditsRecyclerAdapterTest
{
	private Context context;
	private SubRedditProvider subRedditProvider;
	private ContentValues values;
	//private MainActivity mainActivity;

	@Before
	public void setUp() throws Exception
	{
		context = RuntimeEnvironment.application;
		values = new ContentValues();

		subRedditProvider = Robolectric.setupContentProvider(SubRedditProvider.class);
		//mainActivity = mock(MainActivity.class);
	}

	@Test
	public void countAdapterItems()
	{
		Uri uri1 = insertSubreddit(11, "Teste", "url", 0);
		Uri uri2 = insertSubreddit(12, "Teste1", "url1", 0);

		assertNotNull(uri1);
		assertNotNull(uri2);

		Cursor cursor = querySubreddits();

		SubredditsRecyclerAdapter subredditsRecyclerAdapter = new SubredditsRecyclerAdapter(context, cursor, 0,
			new SubredditsRecyclerAdapter.SubredditListItemClickListener()
			{
				@Override
				public void onListItemClick(int subredditId)
				{

				}
			}, new SubredditsRecyclerAdapter.SubscribeUnsubscribeSubredditListener()
			{
				@Override
				public void onSubscribeReddit(int subredditId, String subredditUrl)
				{

				}

				@Override
				public void onUnSubscribeReddit(int subredditId, String subredditUrl)
				{

				}
			});

		assertEquals(subredditsRecyclerAdapter.getItemCount(), 2);
	}

	@Test
	public void getItemAtPosition() {

		Uri uri1 = insertSubreddit(11, "Teste", "url", 0);
		Uri uri2 = insertSubreddit(12, "Teste1", "url1", 0);

		assertNotNull(uri1);
		assertNotNull(uri2);

		Cursor cursor = querySubreddits();

		SubredditsRecyclerAdapter subredditsRecyclerAdapter = new SubredditsRecyclerAdapter(context, cursor, 0,
		new SubredditsRecyclerAdapter.SubredditListItemClickListener()
		{
			@Override
			public void onListItemClick(int subredditId)
			{

			}
		}, new SubredditsRecyclerAdapter.SubscribeUnsubscribeSubredditListener()
		{
			@Override
			public void onSubscribeReddit(int subredditId, String subredditUrl)
			{

			}

			@Override
			public void onUnSubscribeReddit(int subredditId, String subredditUrl)
			{

			}
		});

		cursor.moveToFirst();

		assertEquals(cursor.getString(cursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_NAME)),
				  ((SQLiteCursor)subredditsRecyclerAdapter.getItem(0)).
							 getString(((SQLiteCursor)subredditsRecyclerAdapter.getItem(0)).
										getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_NAME)));

		cursor.moveToNext();

		assertEquals(cursor.getString(cursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_NAME)),
				  ((SQLiteCursor)subredditsRecyclerAdapter.getItem(1)).
							 getString(((SQLiteCursor)subredditsRecyclerAdapter.getItem(1)).
										getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_NAME)));
	}

	@Test
	public void testIfViewholderShowCorrectValuesRetrievedFromCursor()
	{
		Uri uri = insertSubreddit(11, "Teste", "url", 0);

		assertNotNull(uri);

		SubredditsRecyclerAdapter subredditsRecyclerAdapter = new SubredditsRecyclerAdapter(context, null, 0,
				  new SubredditsRecyclerAdapter.SubredditListItemClickListener()
			{
				@Override
				public void onListItemClick(int subredditId)
				{
					//assertEquals("value not equals", 11, subredditId);
				}
			}, new SubredditsRecyclerAdapter.SubscribeUnsubscribeSubredditListener()
			{
				@Override
				public void onSubscribeReddit(int subredditId, String subredditUrl)
				{

				}

				@Override
				public void onUnSubscribeReddit(int subredditId, String subredditUrl)
				{

				}
			});

		RelativeLayout coordinatorLayout = new RelativeLayout(context);

		SubredditsRecyclerAdapter.CustomViewHolder viewHolder = subredditsRecyclerAdapter.createViewHolder(context,
				  coordinatorLayout, 0);

		Cursor cursor = querySubreddits();

		cursor.moveToFirst();
		subredditsRecyclerAdapter.bindViewHolder(viewHolder, context, cursor);

		assertEquals(viewHolder.getSubredditName().getText().toString(), "Teste");
		assertEquals(viewHolder.getSubscribeUnsubscribeButton().getText().toString(), context.getString(R.string.unsubscribed));
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

	private Cursor querySubreddits()
	{
		return subRedditProvider.query(SubredditContract.SubredditsEntry.CONTENT_URI, null, null, null, null);
	}
}