package com.mathck.android.wearable.stockmonitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class GetStockDataService extends Service
{
    Alarm alarm = new Alarm();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.SetAlarm(GetStockDataService.this);
        return START_STICKY;
    }

    public void onStart(Context context, Intent intent, int startId)
    {
        alarm.SetAlarm(context);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}