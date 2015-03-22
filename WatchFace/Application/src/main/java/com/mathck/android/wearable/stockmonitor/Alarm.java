package com.mathck.android.wearable.stockmonitor;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

// taken from http://stackoverflow.com/questions/4459058/alarm-manager-example
public class Alarm extends BroadcastReceiver
{
    String mSymbol = "";
    String mPeerId = "";
    private GoogleApiClient mGoogleApiClient;

    private Context mContext;

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        mPeerId = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE).getString("PEER_ID", "");
        mSymbol = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE).getString(CompanionActivity.STOCK_SYMBOL, "GOOG");
        mContext = context;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        new RetrieveStockData().execute(mSymbol);
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {

                    }
                }).build();
        mGoogleApiClient.connect();

        wl.release();
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 5, pi); // Millisec * Second * Minute
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void sendStockUpdateMessage(String configKey, String text) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putString(configKey, text);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, CompanionActivity.PATH_WITH_FEATURE, rawData);
        }
    }

    public class RetrieveStockData extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... symbol) {

            // http://download.finance.yahoo.com/d/quotes.csv?s=%40%5EDJI,GOOG&f=npc4p2&e=.csv

            String result = "";

            HttpResponse response;
            BufferedReader reader;

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet("http://download.finance.yahoo.com/d/quotes.csv?s=%40%5EDJI," + symbol[0] +"&f=npc4p2&e=.csv");
            try {
                response = httpClient.execute(httpGet, localContext);
                InputStreamReader is = new InputStreamReader(response.getEntity().getContent());
                reader = new BufferedReader(is);

                try {
                    String line;
                    reader.readLine();
                    while ((line = reader.readLine()) != null) {
                        result = line;
                    }

                }
                catch (IOException ex) {
                    throw new IOException();
                }
                finally {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        throw new IOException();
                    }
                }

            } catch (IOException e) {

            }

            return result;
        }

        protected void onPostExecute(String feed) {
            sendStockUpdateMessage(CompanionActivity.STOCK, feed);
        }
    }
}
