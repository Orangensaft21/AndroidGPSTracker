package com.example.stefan.helloworld;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

//auth PPhWX+PXzLn2tRFC
//checks (49.977498, 7.082554) (49.977127, 7.082847) (49.975834, 7.080333) (49.976496, 7.079528)

public class MainActivity extends AppCompatActivity {

    boolean gpsBound = false;
    GPSService gpsService;

    private static final String TAG = "HermActivity";
    EditText out, lat, lon, multi;
    Chronometer time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1337);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent gpsServiceIntent = new Intent(this,GPSService.class);
        bindService(gpsServiceIntent,gpsServiceConn, Context.BIND_AUTO_CREATE); //(autocreate) automatically create the service as long as the binding exists.
        registerReceiver(new GpsReceiver(),new IntentFilter("GPSintent"));


        out = (EditText) findViewById(R.id.out);
        lat = (EditText) findViewById(R.id.lat);
        lon = (EditText) findViewById(R.id.lon);
        multi = (EditText) findViewById(R.id.zeiten);
        time = (Chronometer) findViewById(R.id.chronometer2);

        //Zeit neu ziehen
        if (gpsService!=null&& gpsService.isStarted()){
            Log.e(TAG,"geil");
            start();
        }

        Toast.makeText(getApplicationContext(),"scheisspuffonCreate",Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (gpsService.isStarted())
            return true;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.route1:
                gpsService.reqRoute("RobSchumRoute");
                gpsService.getTts().speak("Robert Schuman Route", TextToSpeech.QUEUE_ADD,null,null);
                return true;
            case R.id.route2:
                gpsService.reqRoute("NormaRoute");
                gpsService.getTts().speak("Norma Route", TextToSpeech.QUEUE_ADD,null,null);
                return true;
            case R.id.route3:
                gpsService.reqRoute("Sportplatz");
                gpsService.getTts().speak("Sportplatz Lap", TextToSpeech.QUEUE_ADD,null,null);
                return true;
            case R.id.route4:
                gpsService.reqRoute("Neukauf");
                gpsService.getTts().speak("Neukauf Route", TextToSpeech.QUEUE_ADD,null,null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        //Toast.makeText(getApplicationContext(),"scheisspuffgestoppt",Toast.LENGTH_LONG).show();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        //überarbeiten
        //unbindService(gpsServiceConn);
    }

    public void start(){
        time.setBase(gpsService.getStartTime());
        time.start();
    }

    public void pissButtonClick(View v){

        if (gpsBound){
            gpsService.start();
        }

    }


    public void reset(View v){
        time.setBase(SystemClock.elapsedRealtime());
        time.stop();
        gpsService.reset();
        multi.setText("");
    }


    private class GpsReceiver extends BroadcastReceiver{


        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = null; // hat sonst die alten daten noch drin, was aber net unbedingt schlecht ist.
            bundle = intent.getExtras();

            String loc = bundle.getString("location");
            String zwZeiten = bundle.getString("Zwischenzeiten");
            String finishString = bundle.getString("finished");

            if (bundle.getString("started")!=null){
                start();
                Log.v(TAG,"Service hat den Timer gestartet!");
            }
            if (loc!=null){
                String[] d = loc.split(",");
                lat.setText(String.valueOf(d[0]));
                lon.setText(String.valueOf(d[1]));
                Log.v(TAG,"lat:"+d[0]+" lon:"+d[1]);
            }
            if(zwZeiten!=null){
                multi.setText(zwZeiten);
            }
            if(finishString!=null){
                time.stop();
                multi.setText(finishString);
            }
        }
    }

    ServiceConnection gpsServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            GPSService.GPSServiceBinder binder = (GPSService.GPSServiceBinder) iBinder;
            gpsService = binder.myService();
            //timer neustarten bei neuem oncreate
            gpsBound = true;
            time.setBase(SystemClock.elapsedRealtime());
            //falls der Service im Hintergrund schon läuft
            if (gpsService.isStarted())
                start();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            gpsBound = false;
        }
    };



}
