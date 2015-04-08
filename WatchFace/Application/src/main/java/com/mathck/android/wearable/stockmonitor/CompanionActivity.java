package com.mathck.android.wearable.stockmonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.companion.WatchFaceCompanion;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
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

/**
 * The phone-side config activity for {@code DigitalWatchFaceService}. Like the watch-side config
 * activity ({@code DigitalWatchFaceWearableConfigActivity}), allows for setting the background
 * color. Additionally, enables setting the color for hour, minute and second digits.
 */
public class CompanionActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
                ResultCallback<DataApi.DataItemResult> {
    private static final String TAG = "DigitalWatchFaceConfig";

    // TODO: use the shared constants (needs covering all the samples with Gradle build model)
    public static final String STOCK = "STOCK";
    public static final String STOCK_SYMBOL = "STOCK_SYMBOL";
    public static final String REFRESH_TIMER = "REFRESH_TIMER";
    private static final String WEATHER = "WEATHER_TOGGLE";
    private static final String THEME_DARK = "THEME_TOGGLE";
    public static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

    SeekBar mRefreshTimer;
    TextView mRefreshInfoText;

    final Context context = this;
    EditText mSymbol = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_watch_face_config);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE).edit().putString("PEER_ID", mPeerId).apply();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        //ComponentName name = getIntent().getParcelableExtra(
         //       WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);
        //TextView label = (TextView)findViewById(R.id.label);
        //label.setText(label.getText() + " (" + name.getClassName() + ")");

        Spannable text = new SpannableString("Stock Watch");
        text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        getActionBar().setTitle(text);

        mRefreshInfoText = (TextView) findViewById(R.id.refreshInfoText);

        mRefreshTimer = (SeekBar) findViewById(R.id.refreshbar);

        mSymbol = (EditText) findViewById(R.id.symbol);
        mSymbol.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        imageSwitcher = (ImageSwitcher)findViewById(R.id.watchSample);

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            @Override
            public View makeView() {
                ImageView myView = new ImageView(getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(AbsListView.LayoutParams.
                        MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
                return myView;
            }

        });

        Animation in = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out);
        imageSwitcher.setInAnimation(in);
        imageSwitcher.setOutAnimation(out);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if (aboutDialog != null) {
            aboutDialog.dismiss();
        }

        super.onStop();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        } else {
            displayNoConnectedDeviceDialog();
        }
    }

    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setUpAllPickers(config);
        } else {
            // If DataItem with the current config can't be retrieved, select the default items on
            // each picker.
            setUpAllPickers(null);
        }
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }

    private void displayNoConnectedDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Sets up selected items for all pickers according to given {@code config} and sets up their
     * item selection listeners.
     *
     * @param config the {@code DigitalWatchFaceService} config {@link DataMap}. If null, the
     *         default items are selected.
     */
    private void setUpAllPickers(DataMap config) {
        //setUpToggleSelection(R.id.toggleweather, WEATHER, config, false);
        setUpToggleSelection(R.id.togglestyle, THEME_DARK, config, false);

        setUpStringSelection(R.id.symbol, STOCK_SYMBOL, config, "GOOG");
        setUpIntSelection(REFRESH_TIMER, config, 2);

        setUpButtonListener(R.id.assignSymbol, STOCK);
        setUpProgressListener();

        setUpToggleListener(R.id.togglestyle, THEME_DARK);
        //setUpToggleListener(R.id.toggleweather, WEATHER);

        updateSampleImage();

        Intent intent = new Intent(context, GetStockDataService.class);
        context.startService(intent);
    }

    private void setUpIntSelection(final String configKey, DataMap config, int defaultValue) {
        int selection = defaultValue;

        if (config != null) {
            selection = config.getInt(configKey, defaultValue);
        }

        mRefreshTimer.setProgress(selection);
        mRefreshInfoText.setText("refresh stock data every " + (mRefreshTimer.getProgress() * 5 + 5) + " minutes");
    }

    private void setUpStringSelection(int textId, final String configKey, DataMap config, String defaultValue) {
        String selection = defaultValue;

        if (config != null) {
            selection = config.getString(configKey, defaultValue);
        }

        EditText text = (EditText) findViewById(textId);
        text.setText(selection);

        context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE).edit().putString(STOCK_SYMBOL, selection).apply();
    }

    private void setUpToggleSelection(int btnId, final String configKey, DataMap config, boolean defaultValue) {
        boolean selection = defaultValue;

        if (config != null) {
            selection = config.getBoolean(configKey, defaultValue);
        }

        ToggleButton btn = (ToggleButton) findViewById(btnId);
        btn.setChecked(selection);
    }

    private void setUpButtonListener(int btnId, final String configKey) {
        Button btn = (Button) findViewById(btnId);
        btn.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAssign(v);
            }
        });
    }

    private void setUpProgressListener() {
        mRefreshTimer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRefreshInfoText.setText("refresh stock data every " + (mRefreshTimer.getProgress() * 5 + 5) + " minutes");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendTimerUpdateMessage(mRefreshTimer.getProgress());
                mRefreshInfoText.setText("refresh stock data every " + (mRefreshTimer.getProgress() * 5 + 5) + " minutes");
                context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE).edit().putInt(REFRESH_TIMER, (mRefreshTimer.getProgress() * 5 + 5)).apply();
                Intent intent = new Intent(context, GetStockDataService.class);
                context.startService(intent);
            }
        });
    }

    private void setUpToggleListener(int btnId, final String configKey) {
        ToggleButton btn = (ToggleButton) findViewById(btnId);
        btn.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
            ToggleButton view = (ToggleButton) v;
            updateSampleImage();
            sendConfigUpdateMessage(configKey, view.isChecked());
            }
        });
    }

    private void sendTimerUpdateMessage(int value) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putInt(REFRESH_TIMER, value);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + REFRESH_TIMER + " -> " + value);
            }
        }
    }

    private void sendConfigUpdateMessage(String configKey, boolean value) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putBoolean(configKey, value);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> " + value);
            }
        }
    }

    private void sendStockUpdateMessage(String configKey, String text) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putString(configKey, text);
            config.putString(STOCK_SYMBOL, mSymbol.getText().toString());
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> " + text);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private AlertDialog aboutDialog;

    private void showAboutDialog() {
        // Linkify the message
        final SpannableString s = new SpannableString("Feel free to fill out my Google Forms http://goo.gl/forms/7p3XKYL3je to request new features.");
        Linkify.addLinks(s, Linkify.ALL);

        aboutDialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle("Finance Stock Watch")
                .setIcon(R.drawable.ic_launcher)
                .setMessage( s )
                .create();

        aboutDialog.show();

        // Make the textview clickable. Must be called after show()
        ((TextView)aboutDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void sendConfigUpdateMessage(String configKey, float value) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putFloat(configKey, value);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> " + value);
            }
        }
    }

    private ImageSwitcher imageSwitcher;

    private void updateSampleImage() {
        //ToggleButton weather = (ToggleButton) findViewById(R.id.toggleweather);
        ToggleButton theme = (ToggleButton) findViewById(R.id.togglestyle);

        /*
        if(weather.isChecked() && theme.isChecked())
            imageSwitcher.setImageResource(R.drawable.void_light_weather);
        else if(!weather.isChecked() && theme.isChecked())
            imageSwitcher.setImageResource(R.drawable.void_light);
        else if(weather.isChecked() && !theme.isChecked())
            imageSwitcher.setImageResource(R.drawable.void_weather);
        else if(!weather.isChecked() && !theme.isChecked())
            imageSwitcher.setImageResource(R.drawable.void_sample);
        */
        if(theme.isChecked())
            imageSwitcher.setImageResource(R.drawable.void_light);
        else
            imageSwitcher.setImageResource(R.drawable.void_sample);
    }

    public void onAssign(View view) {

        String symbol = mSymbol.getText().toString();
        context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE).edit().putString(STOCK_SYMBOL, symbol).apply();

        if(symbol.isEmpty()) {
            Toast.makeText(this, "enter stock symbol", Toast.LENGTH_LONG).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSymbol.getWindowToken(), 0);

        try {
            new RetrieveStockData().execute(symbol);
        }
        catch (Exception e) {
            Toast.makeText(this, e.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
    }

    public class RetrieveStockData extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... symbol) {

            // http://download.finance.yahoo.com/d/quotes.csv?s=%40%5EDJI,GOOG&f=nl1c4p2&e=.csv

            String result = "";

            HttpResponse response;
            BufferedReader reader;

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet("http://download.finance.yahoo.com/d/quotes.csv?s=%40%5EDJI," + symbol[0] +"&f=nl1c4p2&e=.csv");
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
                Toast.makeText(context, e.getStackTrace().toString(), Toast.LENGTH_LONG).show();
            }

            return result;
        }

        protected void onPostExecute(String feed) {
            // assign name
            sendStockUpdateMessage(STOCK, feed);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(feed.split(",")[0].replace("\"", "") + " was successfully assigned.")
                    .setTitle("Stock assigned")
                    .setIcon(R.drawable.ic_launcher)
                    .setPositiveButton("Ok", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
