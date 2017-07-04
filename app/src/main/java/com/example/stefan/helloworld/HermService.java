package com.example.stefan.helloworld;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class HermService extends Service {

    public static final String TAG = "Hermservice";
    private static final int METHOD_STATUS_RUNNING = 1337 ;
    ResultReceiver resultReceiver;
    BroadcastReceiver receiver;
    public HermService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"azfgerufen");
        receiver = new FooReceiver();
        registerReceiver(receiver, new IntentFilter("test"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Service destr.");
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        Bundle parameters = intent.getExtras();
        resultReceiver = parameters.getParcelable("hermreceiver");
        resultReceiver.send(METHOD_STATUS_RUNNING, Bundle.EMPTY);
        Log.e(TAG,"scheiss hermservice");
        return Service.START_STICKY;
        /*new Thread(){
            public void run(){
                while (true)
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

            }
        }.run();*/
    }

    private class FooReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "received from main activity");
            Bundle params = intent.getExtras();
            if (params != null)
                Log.e(TAG, String.valueOf(params.getBoolean("start")));
        }
    }

}
