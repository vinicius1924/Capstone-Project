package com.example.vinicius.capstone.view;

import android.content.Context;
import android.database.Cursor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vinicius.capstone.DividerItemDecoration;
import com.example.vinicius.capstone.PostsRecyclerAdapter;
import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.StateMaintainer;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.interfaces.IPostsMVP;
import com.example.vinicius.capstone.presenter.PostsPresenter;
import com.example.vinicius.capstone.sync.SubRedditSyncAdapter;

public class PostsActivity extends AppCompatActivity implements IPostsMVP.RequiredViewOps,
		  SwipeRefreshLayout.OnRefreshListener
{
	public static final String POSTSACTIVITYTAG = PostsActivity.class.getSimpleName();
	public static final String EXTRA_SUBREDDIT_ID = "EXTRA_SUBREDDIT_ID";
	public static final String EXTRA_SUBREDDIT_URL = "EXTRA_SUBREDDIT_URL";
	public static final String EXTRA_SUBREDDIT_NAME = "EXTRA_SUBREDDIT_NAME";
	private CoordinatorLayout coordinatorLayout;
	private RecyclerView postsRecyclerView;
	private PostsRecyclerAdapter postsRecyclerAdapter;
	private Toolbar toolbar;
	private Snackbar snackbar;
	private SwipeRefreshLayout swipeRefreshLayout;
	private ProgressBar progressBar;
	private TextView toolbarTitle;
	private TextView noDataAvailable;

	// Responsável por manter estado dos objetos inscritos
	// durante mudanças de configuração
	private final StateMaintainer mStateMaintainer = new StateMaintainer(this.getSupportFragmentManager(), POSTSACTIVITYTAG);
	// Operações no Presenter
	private IPostsMVP.PresenterOps mPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//Log.d(POSTSACTIVITYTAG, "PostsActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_posts);

		startMVPOps();

		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		postsRecyclerView = (RecyclerView) findViewById(R.id.postsRecyclerView);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
		noDataAvailable = (TextView) findViewById(R.id.noDataAvailable);

		String title = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);

		toolbarTitle.setText(title);

		swipeRefreshLayout.setOnRefreshListener(this);

		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowTitleEnabled(false);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		postsRecyclerView.setLayoutManager(layoutManager);
		postsRecyclerView.setHasFixedSize(true);

		postsRecyclerAdapter = new PostsRecyclerAdapter(this.getApplicationContext(), null, 0, (PostsPresenter)mPresenter);

		postsRecyclerView.addItemDecoration(new DividerItemDecoration(this));
		postsRecyclerView.setItemAnimator(new DefaultItemAnimator());
		postsRecyclerView.setAdapter(postsRecyclerAdapter);

		new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT)
		{
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder
					  target)
			{
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
			{
				int postid = postsRecyclerAdapter.getIdAtPosition(viewHolder.getAdapterPosition());
				mPresenter.onListItemSwiped(postid);
			}
		}).attachToRecyclerView(postsRecyclerView);

		mPresenter.onCreate();
	}

	@Override
	protected void onStart()
	{
		Log.d(POSTSACTIVITYTAG, "PostsActivity.onStart()");
		super.onStart();

		mPresenter.onStart();
	}

	@Override
	protected void onResume()
	{
		//Log.d(POSTSACTIVITYTAG, "PostsActivity.onResume()");
		super.onResume();

		mPresenter.onResume();
	}

	@Override
	protected void onPause()
	{
		//Log.d(POSTSACTIVITYTAG, "PostsActivity.onPause()");
		super.onPause();

		mPresenter.onPause();
	}

	@Override
	protected void onStop()
	{
		Log.d(POSTSACTIVITYTAG, "PostsActivity.onStop()");
		super.onStop();

		mPresenter.onStop();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			/* Este é o id do back button */
			case android.R.id.home:
				onBackPressed();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Inicia e reinicia o Presenter. Este método precisa ser chamado
	 * após {@link Activity#onCreate(Bundle)}
	 */
	public void startMVPOps()
	{
		try
		{
			if(mStateMaintainer.firstTimeIn())
			{
//				Log.d(POSTSACTIVITYTAG, "Criado fragmento para manter o estado da instância do presenter " +
//						  "e do model");
				initialize(this);
			}
			else
			{
				reinitialize(this);
			}
		}
		catch(InstantiationException e)
		{
			Log.e(POSTSACTIVITYTAG, "PostsActivity.startMVPOps() - " + e.toString());
			throw new RuntimeException(e);
		}
		catch(IllegalAccessException e)
		{
			Log.e(POSTSACTIVITYTAG, "PostsActivity.startMVPOps() - " + e.toString());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Inicializa os objetos relevantes para o MVP.
	 * Cria uma instância do Presenter, salva o presenter
	 * no {@link StateMaintainer} e informa à instância do
	 * presenter que objeto foi criado.
	 *
	 * @param view Operações no View exigidas pelo Presenter
	 */
	private void initialize(IPostsMVP.RequiredViewOps view) throws InstantiationException, IllegalAccessException
	{
		mPresenter = new PostsPresenter(view);
		mStateMaintainer.put(IPostsMVP.PresenterOps.class.getSimpleName(), mPresenter);
	}

	/**
	 * Recupera o presenter e informa à instância que houve uma mudança
	 * de configuração no View.
	 * Caso o presenter tenha sido perdido, uma nova instância é criada
	 */
	private void reinitialize(IPostsMVP.RequiredViewOps view) throws InstantiationException, IllegalAccessException
	{
		mPresenter = mStateMaintainer.get(IPostsMVP.PresenterOps.class.getSimpleName());

		if(mPresenter == null)
		{
			Log.w(POSTSACTIVITYTAG, "recriando o Presenter");
			initialize(view);
		}
		else
		{
			mPresenter.onConfigurationChanged(view);
		}
	}

	@Override
	public void showSnackBar(String message)
	{
		progressBarVisibility(View.INVISIBLE);

		snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

		snackbar.show();
	}

	@Override
	public void showToast(String msg)
	{
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void progressBarVisibility(int visibility)
	{
		progressBar.setVisibility(visibility);
	}

	@Override
	public void swipeRefreshEnabled(boolean enabled)
	{
		swipeRefreshLayout.setEnabled(enabled);
	}

	private void noDataAvailableVisibility(int visibility)
	{
		noDataAvailable.setVisibility(visibility);
	}

	@Override
	public Context getAppContext()
	{
		return getApplicationContext();
	}

	@Override
	public Context getActivityContext()
	{
		return this;
	}

	@Override
	public void onRefresh()
	{
		mPresenter.onSwipeRefresh();
	}

	@Override
	public void onLoadPostsFinished()
	{
		swipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public void onSwipeRefreshStopped()
	{
		swipeRefreshLayout.setRefreshing(true);
		onRefresh();
	}

	@Override
	public void onLoadDataFinished(Cursor data)
	{
		//Log.d(POSTSACTIVITYTAG, "PostsActivity.onLoadDataFinished()");
		if(data == null || !data.moveToFirst())
		{
			noDataAvailableVisibility(View.VISIBLE);
		}
		else
		{
			noDataAvailableVisibility(View.INVISIBLE);
		}

		postsRecyclerAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		//Log.d(POSTSACTIVITYTAG, "PostsActivity.onLoaderReset()");
		postsRecyclerAdapter.swapCursor(null);
	}
}
