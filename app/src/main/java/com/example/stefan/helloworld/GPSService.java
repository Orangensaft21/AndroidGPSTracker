package com.example.stefan.helloworld;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GPSService extends Service implements LocationListener,TextToSpeech.OnInitListener{

    private final IBinder gpsBinder = new GPSServiceBinder();  // binder is sowas wie ne bridge vom client zum service
    private static final String TAG = "HermGPSservice";
    private static final String broadcastIntent = "GPSintent";
    private TextToSpeech tts;
    private Socket socket;
    private String zwZeiten="";

    boolean started=false;
    List<Checkpoint> checkpointList = new ArrayList<Checkpoint>();

    final float ACCURACY = 20f;

    public GPSService() {

    }

    @Override
    public void onCreate() {

        tts = new TextToSpeech(this,this); // geht irgendwie nur in OnCreate, Lenz fragen
        super.onCreate();
        socketInit();
        socket.connect();
        SocketUtil.socket = socket;
        //route "route" abfragen
        socket.emit("reqcheck","route");

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates("gps", 1000, 10, this);
    }


    public void start() {

        if (insideCheck && nextCheck == checkpointList.get(0)) {
            started=true;
            sendStringToClient("started","true");
            tts.speak("Start!", TextToSpeech.QUEUE_ADD, null, null);
            nextCheck = checkpointList.get(1); // 1 check hinter dem start
        }
    }


    private boolean insideCheck=false;
    private Checkpoint nextCheck; //nextCheck = start

    @Override
    public void onLocationChanged(Location location) {

        insideCheck = nextCheck.getLoc().distanceTo(location)<(ACCURACY);

        // Arbeit/Batterie einsparen
        if(!started) {
            return;
        }
        sendStringToClient("location",location.getLatitude()+","+location.getLongitude());
        SocketUtil.emitPosition(location);

        //ziel erreicht
        if (insideCheck && nextCheck.getSpot()==1){
            setZwischenzeitenText(true);
            started=false;
            tts.speak("finish",TextToSpeech.QUEUE_ADD,null,null);
            sendStringToClient("finished",zwZeiten);
            return;
        }

        //Nächster Checkpoint erreicht
        if (insideCheck){
            nextCheck.visit("00:01");
            setZwischenzeitenText(false);
            tts.speak(nextCheck.toSpeech(),TextToSpeech.QUEUE_ADD,null,null);
            nextCheck=getNextCheck(); //getSpot liefert (buggy) einen eine location weiter aus
            sendStringToClient("Zwischenzeiten",zwZeiten);
        }

    }



    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void setZwischenzeitenText(boolean finished){
        zwZeiten="";
        for (Checkpoint check:checkpointList){
            if (check.getSpot()==1)             //start/finish bekommt keine ZwZeit
                continue;
            if (check.isVisited()){
                zwZeiten = (zwZeiten+"\n"
                        +"Checkpoint "+check.getSpot()+": "+check.getDurchgangszeit());
            }
        }
        if (finished){
            zwZeiten += "\n"+"LAPTIME "+"00:21";
        }
    }

    private Checkpoint getNextCheck(){
        int index = checkpointList.indexOf(nextCheck) +1;
        return checkpointList.get(index % checkpointList.size());
    }

    @Override
    public void onInit(int i) {
        tts.setLanguage(new Locale("en_US"));
    }

    public void socketInit(){

        try {
            socket = IO.socket("http://84.166.15.95:3002");
            Log.v(TAG,"socket initialisiert");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on("route", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                JSONObject r = (JSONObject) args[0];

                try {
                    Log.v(TAG,"Checkpoints werden empfangen");
                    JSONArray checkpoints = (JSONArray) r.get("checkpoints");

                    //start
                    //start = new Location("gps");
                    //start.setLatitude((double) checkpoints.getJSONObject(0).get("lat"));
                    //start.setLongitude((double) checkpoints.getJSONObject(0).get("lon"));


                    for (int i = 0; i < checkpoints.length(); i++) {
                        JSONObject checkpoint = checkpoints.getJSONObject(i);
                        int spot = (int) checkpoint.get("spot");
                        Location loc = new Location("gps");
                        loc.setLatitude((double)checkpoint.get("lat"));
                        loc.setLongitude((double)checkpoint.get("lon"));
                        checkpointList.add(new Checkpoint(spot,loc));
                    }

                    nextCheck = checkpointList.get(0);
                    //Log.e(TAG,"start"+start.getLatitude()+" "+start.getLongitude());
                    Log.e(TAG,checkpointList.toString());
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



    private void sendStringToClient(String reason, String data){
        Intent i = new Intent(broadcastIntent);
        i.putExtra(reason,data);
        sendBroadcast(i);
    }

    public class GPSServiceBinder extends Binder {

        GPSService myService(){
            return GPSService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return gpsBinder;
    }
}
