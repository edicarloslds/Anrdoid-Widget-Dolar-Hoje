package br.com.power.dolarhoje;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of App Widget functionality.
 */
public class DolarAppWidget extends AppWidgetProvider {

    public static final String WIDGET_BUTTON = "br.com.power.dolarhoje.WIDGET_BUTTON";
    public static final String WIDGET_CONFIGURE = "br.com.power.dolarhoje.WIDGET_CONFIGURE";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (WIDGET_BUTTON.equals(intent.getAction())) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), DolarAppWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static RemoteViews buildRemoteViews(final Context context, int appWidgetId) throws ExecutionException, InterruptedException {

        String valores[];

        System.out.println("Atualizado!");

        int moeda_venda = PrefsUtils.getInteger(context.getApplicationContext(), "MOEDA_VENDA");
        int moeda_compra = PrefsUtils.getInteger(context.getApplicationContext(), "MOEDA_COMPRA");
        String moeda_nome = PrefsUtils.getString(context.getApplicationContext(), "MOEDA_NOME");

        if(moeda_venda == 0 && moeda_compra == 0 && moeda_nome.equals("")){
            moeda_venda = 1;
            moeda_compra = 10813;
            moeda_nome = "Dólar Americano";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        getDataMoeda taskData = new getDataMoeda();
        String myData = (String) taskData.execute((long) moeda_compra).get();

        if(myData != null){
            currentDate = myData;
        }

        getValorMoedaSeries task = new getValorMoedaSeries();
        valores = (String[]) task.execute(String.valueOf(moeda_venda),String.valueOf(moeda_compra), currentDate).get();

        if(valores == null){

            RemoteViews views_erro = new RemoteViews(context.getPackageName(), R.layout.sem_conexao);

            Intent intent = new Intent(WIDGET_BUTTON);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views_erro.setOnClickPendingIntent(R.id.btnSync, pendingIntent );

            return views_erro;

        }else if(valores[0] == null || valores[1] == null) {

            RemoteViews views_erro = new RemoteViews(context.getPackageName(), R.layout.sem_conexao);

            Intent intent = new Intent(WIDGET_BUTTON);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views_erro.setOnClickPendingIntent(R.id.btnSync, pendingIntent );

            return views_erro;
        }
        else {

            String taxa_venda = valores[1];
            String taxa_compra = valores[0];

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dolar_app_widget);

            views.removeAllViews(R.layout.dolar_app_widget);

            // Set the text
            views.setTextViewText(R.id.tvData, currentDate);
            views.setTextViewText(R.id.tvTaxa_Compra, "R$ " + taxa_compra);
            views.setTextViewText(R.id.tvTaxa_Venda, "R$ " + taxa_venda);
            views.setTextViewText(R.id.tvNomeMoeda, moeda_nome);

            Intent configIntent = new Intent(context, DolarAppWidgetConfig.class);
            configIntent.setAction(WIDGET_CONFIGURE);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.btn_config, pendingIntent);

            Arrays.fill(valores, null);

            // Instruct the widget manager to update the widget
            return views;
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int appWidgetId) throws ExecutionException, InterruptedException {

        final RemoteViews views = buildRemoteViews(context, appWidgetId);

        System.out.println("appWidgetId: " + appWidgetId);

        // Updating widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    private static class getValorMoedaSeries extends AsyncTask<String, Void, Object> {
        public static String response[];
        public Long Lista[];
        private static final String NAMESPACE = "https://www3.bcb.gov.br/wssgs/services/FachadaWSSGS";
        DecimalFormat df = new DecimalFormat("##.####");
        private Context context;


        @Override
        protected Object doInBackground(String... params) {

            Lista = new Long[]{Long.valueOf(params[0]), Long.valueOf(params[1])};
            String[] ListaValores = new String[2];


            try {

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                SoapObject request = new SoapObject(NAMESPACE,"getValoresSeriesVO");
                envelope.setOutputSoapObject(request);


                SoapObject soapMoedas = new SoapObject(NAMESPACE, "in0");
                for (Long i : Lista){
                    soapMoedas.addProperty("long", i);
                }
                request.addSoapObject(soapMoedas);
                request.addProperty("in1",params[2]); //Data da busca
                request.addProperty("in2",params[2]); //Data da busca
                HttpTransportSE transportSE = new HttpTransportSE(NAMESPACE);
                try {
                    transportSE.call(NAMESPACE, envelope);
                }catch (IOException | XmlPullParserException e){
                    e.printStackTrace();
                }

                if(!envelope.bodyIn.toString().contains("SGSNegocioException")){

                    SoapObject result = (SoapObject) envelope.bodyIn;

                    Vector<?> responseVector = (Vector<?>) result.getProperty(0);

                    //PEGA OS VALORES DE COMPRA DA MOEDA
                    SoapObject ValoresCompra = (SoapObject)responseVector.get(1);
                    Vector<?>  VectorCompra = (Vector<?>) ValoresCompra.getProperty("valores");
                    SoapObject ValorMoedaCompra = (SoapObject)VectorCompra.get(0);
                    String Compra = ValorMoedaCompra.getProperty(11).toString();

                    //System.out.println("COMPRA: "+Compra+"\n");
                    ListaValores[0] = df.format(Double.valueOf(Compra));

                    //PEGA OS VALORES DE VENDA DA MOEDA
                    SoapObject ValoresVenda = (SoapObject)responseVector.get(0);
                    Vector<?>  VectorVenda = (Vector<?>) ValoresVenda.getProperty("valores");
                    SoapObject ValorMoedaVenda = (SoapObject)VectorVenda.get(0);
                    String Venda = ValorMoedaVenda.getProperty(11).toString();

                    //System.out.println("VENDA: "+Venda+"\n");
                    ListaValores[1] = df.format(Double.valueOf(Venda));
                    response = ListaValores;

                }else{
                    response = null;
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return response;
        }
    }


    private static class getDataMoeda extends AsyncTask<Long, Void, Object> {
        public static String response;
        private static final String NAMESPACE = "https://www3.bcb.gov.br/wssgs/services/FachadaWSSGS";

        @Override
        protected Object doInBackground(Long... params) {

            try {

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                SoapObject request = new SoapObject(NAMESPACE,"getUltimoValorVO");
                envelope.setOutputSoapObject(request);

                request.addProperty("in0",params[0]); //Moeda

                HttpTransportSE transportSE = new HttpTransportSE(NAMESPACE);
                try {
                    transportSE.call(NAMESPACE, envelope);
                }catch (IOException | XmlPullParserException e){
                    e.printStackTrace();
                }

                if(!envelope.bodyIn.toString().contains("SGSNegocioException")){

                    SoapObject result = (SoapObject) envelope.bodyIn;

                    SoapObject ValoresData = (SoapObject) result.getProperty(0);
                    SoapObject DataValor = (SoapObject) ValoresData.getProperty("ultimoValor");

                    String ano = DataValor.getProperty(0).toString();
                    String mes = DataValor.getProperty(6).toString();
                    String dia = DataValor.getProperty(4).toString();

                    if(Integer.parseInt(mes) < 10){
                        mes = "0"+mes;
                    }

                    if(Integer.parseInt(dia) < 10){
                        dia = "0"+dia;
                    }

                    response = dia+"/"+mes+"/"+ano;

                }else{
                    response = null;
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return response;
        }

    }

}

