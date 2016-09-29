package com.wolper.formmasterwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ConfigActivity extends Activity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    public final static String WIDGET_PREF = "formaster_widget_pass";
    public final static String WIDGET_PAS = "widget_pass";
    public final static String WIDGET_SERV = "widget_server";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        // отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);
        setContentView(R.layout.activity_config);
    }


    public void onClick(View v) {
        //записываем конфигурацию
        EditText etTextServ = (EditText) findViewById(R.id.editText1);
        EditText etTextPasw = (EditText) findViewById(R.id.editText2);

        // Записываем значения с экрана в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(WIDGET_SERV, etTextServ.getText().toString());
        editor.putString(WIDGET_PAS, etTextPasw.getText().toString());
        editor.commit();

        //немедленно обновить
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        FMHWidget.updateWidget(this, appWidgetManager, widgetID);

        // положительный ответ 
        setResult(RESULT_OK, resultValue);
        finish();
    }
}