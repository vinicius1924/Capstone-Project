package com.example.vinicius.capstone.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.utils.PreferencesUtils;
import com.example.vinicius.capstone.view.ReadPostActivity;

import java.util.concurrent.ExecutionException;

import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;

/**
 * Created by vinicius on 05/06/17.
 */

/**
 * Classe usada para exibir coleções de dados remotos como os de um content provider.
 * Os dados providos pelo RemoteViewsService são apresentados no widget utilizando
 * um dos tipos de view abaixo listados, aos quais nos referimos como "collection views":
 *
 * ListView
 * GridView
 * StackView
 * AdapterViewFlipper
 *
 * Como dito antes essas "collection views" exibem coleções de dados remotos. Isso significa
 * que elas usam um Adapter para ligar sua interface aos dados. Um Adapter liga um item individual de
 * um conjunto de dados em um View object individual. No widget o Adapter é substituido por um
 * RemoteViewsFactory que é simplesmente um invólucro em torno da interface do Adapter. Quando requisitado
 * por um item especifico da colecao, o RemoteViewsFactory cria e retorna o item para a coleção como
 * um RemoteViews object. Para incluir uma "collection view" no widget, deve ser implementado
 * RemoteViewsService e RemoteViewsFactory
 *
 * RemoteViewsService é um serviço que permite um adaptador remoto requisitar RemoteViews objects
 */
public class CapstoneRemoteViewsService  extends RemoteViewsService
{
	private Context context;
	private final String TAG = getClass().getSimpleName();

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		context = this.getApplicationContext();

		/* É uma interface para um Adapter entre uma collection view
		(como ListView, GridView e assim por diante) e os dados para essa view.
		Essa implementação é responsável por criar objetos RemoteViews para cada item do conjunto de dados */
		return new RemoteViewsFactory()
		{
			private Cursor data = null;

			@Override
			public void onCreate()
			{

			}

			@Override
			public void onDataSetChanged()
			{
				if (data != null)
				{
					data.close();
				}

				final long identityToken = Binder.clearCallingIdentity();

				String selectedSubreddit = PreferencesUtils.getSelectedSubredditWidget(context);

				if(!selectedSubreddit.equals(getResources().getString(R.string.prefDefaultValue)) &&
						  !selectedSubreddit.equals(""))
				{
					Uri uri = SubredditContract.SubredditsPostsEntry.buildSubredditsPostsUri(Long.valueOf(selectedSubreddit));
					data = getContentResolver().query(uri, null, null, null,
							  SubredditContract.SubredditsPostsEntry.COLUMN_CREATED_UTC + " DESC");
				}

				Binder.restoreCallingIdentity(identityToken);
			}

			@Override
			public void onDestroy()
			{
				if(data != null)
				{
					data.close();
					data = null;
				}
			}

			@Override
			public int getCount()
			{
				return data == null ? 0 : data.getCount();
			}

			@Override
			public RemoteViews getViewAt(int position)
			{
				if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position))
				{
					return null;
				}

				RemoteViews views = new RemoteViews(getPackageName(), R.layout.post_list_item);
				views.setImageViewResource(R.id.postThumbnail, R.color.cardview_dark_background);
				views.setTextViewText(R.id.postTitle, "");
				views.setTextViewText(R.id.postAuthor, "");

				try
				{
					Bitmap image = Glide.
							  with(context).asBitmap().
							  load(data.getString(data.getColumnIndex(SubredditContract.
										 SubredditsPostsEntry.COLUMN_THUMBNAIL))).
							  into(SIZE_ORIGINAL, SIZE_ORIGINAL). // Width and height
							  get();

					views.setImageViewBitmap(R.id.postThumbnail, image);
				}
				catch(InterruptedException e)
				{
					Log.e(TAG, e.toString());
				}
				catch(ExecutionException e)
				{
					Log.e(TAG, e.toString());
				}

				views.setTextViewText(R.id.postTitle, data.getString(data.getColumnIndex(SubredditContract.SubredditsPostsEntry.COLUMN_TITLE)));
				views.setTextColor(R.id.postTitle, ContextCompat.getColor(context, R.color.primary_text_default_material_light));

				views.setTextViewText(R.id.postAuthor, data.getString(data.getColumnIndex(SubredditContract.SubredditsPostsEntry.COLUMN_AUTHOR)));
				views.setTextColor(R.id.postAuthor, ContextCompat.getColor(context, R.color.secondary_text_default_material_light));

				String permalink = data.getString(data.getColumnIndex(SubredditContract.
						  SubredditsPostsEntry.COLUMN_PERMALINK));

				final Intent fillIntent = new Intent();
				fillIntent.putExtra(ReadPostActivity.EXTRA_PERMALINK, permalink);
				views.setOnClickFillInIntent(R.id.widget_list_item, fillIntent);

				return views;
			}

			@Override
			public RemoteViews getLoadingView()
			{
				return null;
			}

			@Override
			public int getViewTypeCount()
			{
				return 1;
			}

			@Override
			public long getItemId(int position)
			{
				if(data.moveToPosition(position))
					return data.getLong(0);
				return position;
			}

			@Override
			public boolean hasStableIds()
			{
				return true;
			}
		};
	}
}
