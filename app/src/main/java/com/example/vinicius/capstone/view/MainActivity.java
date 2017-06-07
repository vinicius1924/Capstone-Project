package com.example.vinicius.capstone.view;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.vinicius.capstone.DividerItemDecoration;
import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.StateMaintainer;
import com.example.vinicius.capstone.SubredditsRecyclerAdapter;
import com.example.vinicius.capstone.interfaces.IMainMVP;
import com.example.vinicius.capstone.model.MainModel;
import com.example.vinicius.capstone.presenter.MainPresenter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity implements IMainMVP.RequiredViewOps
{
	public static final String MAINACTIVITYTAG = MainActivity.class.getSimpleName();
	private CoordinatorLayout coordinatorLayout;
	private Toolbar toolbar;
	private Snackbar snackbar;
	private RecyclerView subredditsRecyclerView;
	private SubredditsRecyclerAdapter subredditsRecyclerAdapter;
	private ProgressBar progressBar;
	// Responsável por manter estado dos objetos inscritos
	// durante mudanças de configuração
	private final StateMaintainer mStateMaintainer = new StateMaintainer(this.getSupportFragmentManager(), MAINACTIVITYTAG);

	// Operações no Presenter
	private IMainMVP.PresenterOps mPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		startMVPOps();

		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		subredditsRecyclerView = (RecyclerView) findViewById(R.id.subredditsRecyclerView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		subredditsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		subredditsRecyclerView.setHasFixedSize(true);

		subredditsRecyclerAdapter = new SubredditsRecyclerAdapter(this.getApplicationContext(), null, 0,
				  (MainPresenter)mPresenter, (MainModel)mPresenter.getModel());

		subredditsRecyclerView.addItemDecoration(new DividerItemDecoration(this));
		subredditsRecyclerView.setItemAnimator(new DefaultItemAnimator());
		subredditsRecyclerView.setAdapter(subredditsRecyclerAdapter);

		mPresenter.onCreate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(mPresenter.onOptionsItemSelected(item))
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		mPresenter.onStart();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		mPresenter.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		mPresenter.onPause();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		mPresenter.onStop();
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
				Log.d(MAINACTIVITYTAG, "Criado fragmento para manter o estado da instância do presenter " +
						  "e do model");
				initialize(this);
			}
			else
			{
				reinitialize(this);
			}
		}
		catch(InstantiationException e)
		{
			Log.e(MAINACTIVITYTAG, "MainActivity.startMVPOps() " + e.toString());
			throw new RuntimeException(e);
		}
		catch(IllegalAccessException e)
		{
			Log.e(MAINACTIVITYTAG, "MainActivity.startMVPOps() " + e.toString());
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
	private void initialize(IMainMVP.RequiredViewOps view) throws InstantiationException, IllegalAccessException
	{
		mPresenter = new MainPresenter(view);
		mStateMaintainer.put(IMainMVP.PresenterOps.class.getSimpleName(), mPresenter);
	}

	/**
	 * Recupera o presenter e informa à instância que houve uma mudança
	 * de configuração no View.
	 * Caso o presenter tenha sido perdido, uma nova instância é criada
	 */
	private void reinitialize(IMainMVP.RequiredViewOps view) throws InstantiationException, IllegalAccessException
	{
		mPresenter = mStateMaintainer.get(IMainMVP.PresenterOps.class.getSimpleName());

		if(mPresenter == null)
		{
			Log.w(MAINACTIVITYTAG, "recriando o Presenter");
			initialize(view);
		}
		else
		{
			mPresenter.onConfigurationChanged(view);
		}
	}

	@Override
	public void showToast(String msg)
	{
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void showAlert(String msg)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
		builder.setMessage(msg)
				  .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int id) {
						  dialog.dismiss();
					  }
				  }).show();
	}

	@Override
	public void showSnackBar(String message)
	{
		progressBarVisibility(View.INVISIBLE);

		snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
					  .setAction(getResources().getString(R.string.tryAgain), new View.OnClickListener() {
					  @Override
					  public void onClick(View view) {
						  snackbar.dismiss();
						  progressBarVisibility(View.VISIBLE);
						  mPresenter.getDefaultSubreddits();
					  }
				  });

		snackbar.show();
	}

	@Override
	public void finishActivity()
	{
		finish();
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
	public void onLoadDataFinished(Cursor data)
	{
		/* significa que os subreddits default não foram baixados da api e armazenados no banco de dados local */
		if(data == null || !data.moveToFirst())
		{
			progressBarVisibility(View.VISIBLE);
		}
		else
		{
			progressBarVisibility(View.INVISIBLE);
		}

		subredditsRecyclerAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		subredditsRecyclerAdapter.swapCursor(null);
	}

	private void progressBarVisibility(int visibility)
	{
		progressBar.setVisibility(visibility);
	}
}
