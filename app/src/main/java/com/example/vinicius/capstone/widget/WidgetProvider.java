package com.example.vinicius.capstone.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.vinicius.capstone.R;
import com.example.vinicius.capstone.view.MainActivity;
import com.example.vinicius.capstone.view.ReadPostActivity;

import static com.example.vinicius.capstone.sync.SubRedditSyncAdapter.ACTION_DATA_UPDATED;

/**
 * Created by vinicius on 05/06/17.
 */

public class WidgetProvider extends AppWidgetProvider
{
	private final String TAG = getClass().getSimpleName();

	/**
	 * Método chamado a cada passagem de tempo definido no atributo updatePeriodMillis
	 * do arquivo widget_info.xml. Esse método também é chamado quando o usuário
	 * adiciona o widget. Esse método não será chamado quando o usuário adiciona o widget
	 * caso tenha sido declarado no arquivo widget_info.xml a propriedade configure que
	 * indica que uma Activity deve ser aberta assim que o widget é adicionado
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		for (int appWidgetId : appWidgetIds)
		{
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

			/* Cria um Intent para chamar a MainActivity */
			Intent intent = new Intent(context, MainActivity.class);

			/*
			 * O PendingIntent é passado para uma aplicação diferente (por exemplo: NotificationManager,
			 * AlarmManager, Home Screen AppWidgetManager ou outra aplicação de terceiros) permitindo
			 * essa aplicação diferente usar as permissões da minha aplicação para executar um pedaço
			 * de código pré definido. Neste caso, quando o usuário clicar na parte onde tem o titulo
			 * do aplicativo no widget, será aberta a MainActivity
			 */
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.widget, pendingIntent);

			/* este método diz onde a collection view deve pegar seus dados */
			views.setRemoteAdapter(R.id.widget_list, new Intent(context, CapstoneRemoteViewsService.class));


			Intent clickIntentTemplate = new Intent(context, ReadPostActivity.class);
			PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
					  .addNextIntentWithParentStack(clickIntentTemplate)
					  .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);

			views.setEmptyView(R.id.widget_list, R.id.widget_empty);

			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	/**
	 * Método chamado para cada broadcast e antes de qualquer um dos métodos acima
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);

		if(ACTION_DATA_UPDATED.equals(intent.getAction()))
		{
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
		}
	}
}
