package com.wolper.formmasterwidget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static android.content.Context.MODE_PRIVATE;
import static com.wolper.formmasterwidget.ConfigActivity.WIDGET_PAS;
import static com.wolper.formmasterwidget.ConfigActivity.WIDGET_PREF;
import static com.wolper.formmasterwidget.ConfigActivity.WIDGET_SERV;


public class FMHWidget extends AppWidgetProvider {


    //переменные хранящие информацию
    private final static String ACTION_PRESS = "com.wolper.pressOnFMHWidget";
    private static volatile boolean working=false;



    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int id : appWidgetIds) {
            //апдейтим вид
            updateWidget(context, appWidgetManager, id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // Удаляем Preferences
        Editor editor = context.getSharedPreferences(WIDGET_PREF, MODE_PRIVATE).edit();
        editor.remove(WIDGET_SERV);
        editor.remove(WIDGET_PAS);
        editor.commit();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }


    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {

        //вызываем ретрофит для апдейта виджета
        callGetSpring(context, appWidgetManager, widgetID);
    }



    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // Проверяем, что это intent от нажатия на третью зону
        if (intent.getAction().equalsIgnoreCase(ACTION_PRESS)) {

            // извлекаем ID экземпляра
            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

            }
            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Обновляем виджет
                updateWidget(context, AppWidgetManager.getInstance(context), mAppWidgetId);
            }
        }
    }


    //запрос Гет со спринг андроидом
    public static void callGetSpring(Context context, AppWidgetManager widgetManager, int ID) {

            class HttpRequestTask extends AsyncTask<Void, Void, String> {
                final AppWidgetManager appWidgetManager_here;
                final int widgetID_here;
                final Context contex_here;
                final String server_here;
                final  String pass_here;

                HttpRequestTask(AppWidgetManager appWidgetManager, Context context, int widgerID, String server, String pass){
                     this.appWidgetManager_here =appWidgetManager;
                     this.contex_here =context;
                     this.widgetID_here =widgerID;
                     this.server_here =server;
                     this.pass_here =pass;
                }
                @Override
                protected String doInBackground(Void... params) {
                    String result;
                    HttpAuthentication authHeader = new HttpBasicAuthentication("FMH", pass_here);
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setAuthorization(authHeader);
                    HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                    try {
                        ResponseEntity<String> res = restTemplate.exchange(server_here, HttpMethod.GET, requestEntity, String.class);
                        result=res.getBody()!=null? res.getBody(): "...";
                    } catch (RestClientException e) {result=e.getCause().toString();}
                    return result;
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);

                    // Настраиваем внешний вид виджета
                    RemoteViews widgetView = new RemoteViews(contex_here.getPackageName(),
                            R.layout.activity_fmhwidget);
                    widgetView.setTextViewText(R.id.textView_wid1, "Присутсвуют");
                    widgetView.setTextViewText(R.id.textView_wid2, result+" чел");

                    //установка обработчиков нажатий
                    Intent countIntent = new Intent(contex_here, FMHWidget.class);
                    countIntent.setAction(ACTION_PRESS);
                    countIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID_here);
                    PendingIntent pIntent = PendingIntent.getBroadcast(contex_here, widgetID_here, countIntent, 0);
                    widgetView.setOnClickPendingIntent(R.id.textView_wid2, pIntent);

                    //апдейтим
                    appWidgetManager_here.updateAppWidget(widgetID_here, widgetView);
                    working=false;
                }
            }

        //не допускаем запуска повторно, пока не закончился предыдущий процесс и ошибочного запуска
        if ((getServerName(context).isEmpty()) || (getPassword(context).isEmpty())) return;
        if (working) return;
        working=true;
        new HttpRequestTask( widgetManager,  context, ID, getServerName(context), getPassword(context)).execute();
    }



    public static String getServerName(Context context) {
        SharedPreferences sp = context.getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        return sp.getString(WIDGET_SERV, "");

    }

    public static String getPassword(Context context) {
        SharedPreferences sp = context.getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        return sp.getString(WIDGET_PAS, "");
    }
}



