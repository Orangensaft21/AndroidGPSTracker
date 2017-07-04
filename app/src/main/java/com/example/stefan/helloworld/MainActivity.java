package com.example.stefan.helloworld;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//auth PPhWX+PXzLn2tRFC
//checks (49.977498, 7.082554) (49.977127, 7.082847) (49.975834, 7.080333) (49.976496, 7.079528)

public class MainActivity extends AppCompatActivity {

    boolean gpsBound = false;
    GPSService gpsService;

    private static final String TAG = "HermActivity";
    private Socket socket;
    private LocationManager locationManager;
    EditText out, lat, lon, multi;
    Chronometer time;
    Location start;
    String checkTimes="",lapTime="";
    TextToSpeech tts;

    //{1:'20:12', 2:'20:13'}
    boolean started=false, insideStart = false, finished = false;
    List<Checkpoint> checkpointList = new ArrayList<Checkpoint>();

    private IntentFilter mIntentFilter;

    public static void log(String text){
        System.err.println(text);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1337);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intent intent = new Intent(this,HermService.class);
        //intent.putExtra("hermreceiver",new MyResultReceiver(new Handler()));
        //this.startService(intent);

        Intent gpsServiceIntent = new Intent(this,GPSService.class);
        bindService(gpsServiceIntent,gpsServiceConn, Context.BIND_AUTO_CREATE); //(autocreate) automatically create the service as long as the binding exists.
        registerReceiver(new GpsReceiver(),new IntentFilter("GPSintent"));
        //

        out = (EditText) findViewById(R.id.out);
        lat = (EditText) findViewById(R.id.lat);
        lon = (EditText) findViewById(R.id.lon);
        multi = (EditText) findViewById(R.id.zeiten);
        time = (Chronometer) findViewById(R.id.chronometer2);
        //tts = new TextToSpeech(this,this);

        start = new Location("gps");
        //socketInit();
        //socket.connect();
        //route "route" abfragen
        //socket.emit("reqcheck","route");

        Context context = getApplicationContext();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        GPSListener gpsListener = new GPSListener();
        locationManager.requestLocationUpdates("gps", 1000, 10, gpsListener);

        System.err.println("01:34".compareTo("00:35"));


        Toast.makeText(getApplicationContext(),"scheisspuffonCreate",Toast.LENGTH_LONG).show();
        //locationManager.requestLocationUpdates("gps",1000,100,new GPS100mListener());

        //mIntentFilter = new IntentFilter("com.example.stefan.helloworld.ProximityAlert123");
        //registerReceiver(new ProximityAlert(),mIntentFilter);


        //start = new Location("gps");
        //start.setLatitude(49.9773760);
        //start.setLongitude(7.0819155);


    }

    @Override
    protected void onStop(){
        super.onStop();

        //Toast.makeText(getApplicationContext(),"scheisspuffgestoppt",Toast.LENGTH_LONG).show();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        unbindService(gpsServiceConn);
    }

    public void pissButtonClick(View v){

        if (gpsBound){
            gpsService.start();
        }

        if (insideStart){
            //time.setBase(SystemClock.elapsedRealtime());
            //time.start();
            started=true;
            //tts.speak("Start!",TextToSpeech.QUEUE_ADD,null,null);
        }

        Button b = (Button) v;
        //b.setText("scheiss");

    }


    public void reset(View v){
        time.setBase(SystemClock.elapsedRealtime());
        time.stop();
        for (Checkpoint check: checkpointList){
            check.reset();
        }
        finished=false;
        multi.setText("");
    }

    public void socketInit(){

        try {
            socket = IO.socket("http://84.166.15.95:3002");
            out.setText("socket init");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            out.setText(e.getReason());
        }

        socket.on("route", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                JSONObject r = (JSONObject) args[0];
                try {
                    JSONArray checkpoints = (JSONArray) r.get("checkpoints");
                    //start

                    start.setLatitude((double) checkpoints.getJSONObject(0).get("lat"));
                    start.setLongitude((double) checkpoints.getJSONObject(0).get("lon"));

                    for (int i = 1; i < checkpoints.length(); i++) {
                        JSONObject checkpoint = checkpoints.getJSONObject(i);
                        int spot = (int) checkpoint.get("spot");
                        Location loc = new Location("gps");
                        loc.setLatitude((double)checkpoint.get("lat"));
                        loc.setLongitude((double)checkpoint.get("lon"));
                        checkpointList.add(new Checkpoint(spot,loc));
                    }
                    System.err.println("start"+start.getLatitude()+" "+start.getLongitude());
                    System.err.println(checkpointList);
                    //System.err.println(checkpointList.get(2));
                }
                catch (Exception e) {System.err.println(e.getMessage());}
            }
        });

        socket.on("zieldurchsage", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                tts.setSpeechRate(0.5f);
                String r = (String) args[0];
                tts.speak(r,TextToSpeech.QUEUE_ADD,null,null);
                tts.setSpeechRate(1f);
            }
        });
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
                time.setBase(SystemClock.elapsedRealtime());
                time.start();
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
            gpsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            gpsBound = false;
        }
    };




    private class GPSListener implements LocationListener{

        //final MediaPlayer MP = MediaPlayer.create(getApplicationContext(), R.raw.awp);
        //final MediaPlayer MPFIN = MediaPlayer.create(getApplicationContext(), R.raw.applause3);
        final float ACCURACY = 20f;
        double latval = 0;
        double longval = 0;;

        
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderDisabled(String s) {


        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onLocationChanged(Location location) {

            insideStart = start.distanceTo(location)<(ACCURACY+10f);


            // Schleife soll nur einmal gefeuert werden.
            if(finished || !started) {
                return;
            }
            latval= location.getLatitude();
            longval = location.getLongitude();

            lat.setText(String.valueOf(latval));
            lon.setText(String.valueOf(longval));
            emitPosition();

            boolean allChecked = true;
            //vielleicht besser nur den nächsten checkpoint checken wegen energieffizienz
            for (Checkpoint check : checkpointList){
                if (!check.isVisited() && location.distanceTo(check.getLoc())<ACCURACY) {
                    //checkpoint Reached
                    check.visit(time.getText().toString());
                    setZwischenzeitenText();
                    System.err.println(check.toSpeech());
                    tts.speak(check.toSpeech(),TextToSpeech.QUEUE_ADD,null,null);
                }
                if (!check.isVisited()){
                    allChecked = false;
                }
            }
            if (checkpointList!=null && allChecked){
                //Runde komplett
                if(insideStart){
                    multi.setText(multi.getText()+"\n"
                            +"LAPTIME "+time.getText());
                    lapTime = time.getText().toString();
                    finished = true;
                    time.stop();
                    emitLap();
                    tts.speak("Finished",TextToSpeech.QUEUE_ADD,null,null);
                }

            }




        }

        private void emitPosition(){
            JSONObject loc = new JSONObject();
            try {
                loc.put("lat",latval);
                loc.put("lon",longval);
                JSONObject msg = new JSONObject();
                msg.put("id", 1);
                msg.put("location", loc);

                socket.emit("test1",msg);
            } catch (JSONException e) {
                e.printStackTrace();
                out.setText(e.toString());
            }
        }

    }



    private void emitLap(){
        checkTimes="Stefan,";
        JSONObject times = new JSONObject();
        for (Checkpoint check:checkpointList) {
            checkTimes += check.getDurchgangszeit()+",";
        }
        checkTimes+=lapTime;
        try {
            times.put("times",checkTimes);
            socket.emit("finish",times);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setZwischenzeitenText(){
        multi.setText("");
        for (Checkpoint check:checkpointList){
            if (check.getSpot()==1)             //start/finish bekommt keine ZwZeit
                continue;
            if (check.isVisited()){
                multi.setText(multi.getText()+"\n"
                        +"Checkpoint "+check.getSpot()+": "+check.getDurchgangszeit());
            }
        }

    }

    /*public void setProximityAlert(double lat,double lon){
        float radius = 30f;
        long expiration = -1;
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Intent intent = new Intent("com.example.stefan.helloworld.ProximityAlert123");
        PendingIntent proximityIntent = PendingIntent.getBroadcast(getApplicationContext(),0,intent,0);

        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.addProximityAlert(lat,lon,radius,expiration,proximityIntent);
    }*/

}
