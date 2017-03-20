package br.com.power.dolarhoje;

import android.app.Activity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import java.util.concurrent.ExecutionException;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

/**
 * Criado por Edicarlos em 06/12/2015 09:35.
 */
public class DolarAppWidgetConfig extends Activity {

    protected ProgressDialog mProgressDialog;
    private Spinner mAppWidgetTipo;
    int mAppWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dolar_app_widget_configure);
        setResult(RESULT_CANCELED);

        mAppWidgetTipo = (Spinner) findViewById(R.id.appwidget_tipo);

        initListViews();
    }

    public void initListViews() {

        Button okButton = (Button) findViewById(R.id.add_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    handleOkButton();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void handleOkButton() throws ExecutionException, InterruptedException {
        mProgressDialog = ProgressDialog.show(this, "Por favor aguarde.","Buscando dados...", true);

        new Thread() {
            @Override
            public void run() {

                int venda = GetCode.getCode(mAppWidgetTipo.getSelectedItem().toString() + " (venda)");
                int compra = GetCode.getCode(mAppWidgetTipo.getSelectedItem().toString() + " (compra)");
                String nome_moeda = mAppWidgetTipo.getSelectedItem().toString();

                PrefsUtils.setInteger(getApplicationContext(), "MOEDA_VENDA", venda);
                PrefsUtils.setInteger(getApplicationContext(), "MOEDA_COMPRA", compra);
                PrefsUtils.setString(getApplicationContext(), "MOEDA_NOME", nome_moeda);

                if(venda != 0 && compra != 0) {

                    try {
                        showAppWidget();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                        }
                    });
                } catch (final Exception ex) {
                    Log.i("---","Exception in thread");
                }
            }
        }.start();

    }

    private void showAppWidget() throws ExecutionException, InterruptedException {

        mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

            // Build/Update widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

            // This is equivalent to your ChecksWidgetProvider.updateAppWidget()
            appWidgetManager.updateAppWidget(mAppWidgetId,
                    DolarAppWidget.buildRemoteViews(getApplicationContext(),
                            mAppWidgetId));

            // Updates the collection view, not necessary the first time
            appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.tvNomeMoeda);
            appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.tvTaxa_Compra);
            appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.tvTaxa_Venda);
            appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.tvData);
            // Destroy activity
            finish();
        }
        if (mAppWidgetId == INVALID_APPWIDGET_ID) {
            Log.i("I am invalid", "I am invalid");
            finish();
        }
    }

}
