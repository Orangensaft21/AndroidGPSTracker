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
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class GPSService extends Service implements LocationListener,TextToSpeech.OnInitListener,SocketListener{


    private final IBinder gpsBinder = new GPSServiceBinder();  // binder is sowas wie ne bridge vom client zum service
    private static final String TAG = "HermGPSservice";
    private static final String broadcastIntent = "GPSintent";
    private TextToSpeech tts;
    private SocketHelper sock;
    private String zwZeiten="";
    private Route route;



    //private String routeName="";

    private boolean started=false;
    private ArrayList<Checkpoint> checkpointList = new ArrayList<Checkpoint>(); // wird vom Socket Helper gesetzt

    final float ACCURACY = 20f;
    private Location currentLoc;




    long startTime;

    public GPSService() {

    }

    public void getSocketResult(Object... args){
        switch ((String) args[0]){
            case "setRoute":
                setRoute((Route)args[1]);
                break;
            case "zieldurchsage":
                String r = (String) args[1];
                tts.setSpeechRate(0.5f);
                tts.speak(r, TextToSpeech.QUEUE_ADD,null,null);
                tts.setSpeechRate(1f);
                break;
        }
    }

    @Override
    public void onCreate() {

        tts = new TextToSpeech(this,this); // geht irgendwie nur in OnCreate, Lenz fragen
        super.onCreate();

        sock = SocketHelper.getInstance(this);
        //socket = sock.getSocket();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates("gps", 1000, 10, this);
    }


    public void start() {

        if (nextCheck==null){
            Toast.makeText(getApplicationContext(),"Server nicht gestartet!",Toast.LENGTH_LONG);
            Log.e(TAG,"server nicht gestartet/keine Daten");
            return;
        }
        Log.e(TAG,"sttaaarrtt");
        Log.e(TAG,""+insideCheck);
        Log.e(TAG,""+nextCheck.getLoc());
        Log.e(TAG,""+currentLoc);

        if (insideCheck && nextCheck == checkpointList.get(0)) {
            startTime=SystemClock.elapsedRealtime() ;
            started=true;
            sendStringToClient("started","true");
            tts.speak("Start!", TextToSpeech.QUEUE_ADD, null, null);
            nextCheck = checkpointList.get(1); // 1 check hinter dem start
        }
    }

    public void reset() {
        if (started)
            nextCheck = checkpointList.get(0);
        for (Checkpoint check:checkpointList){
            check.reset();
        }
        started=false;

    }


    private boolean insideCheck=false;
    private Checkpoint nextCheck; //nextCheck = start

    @Override
    public void onLocationChanged(Location location) {
        //abfrage ob server gestartet, vllt woanders rein
        currentLoc=location;
        if(nextCheck!=null)
            insideCheck = nextCheck.getLoc().distanceTo(location)<(ACCURACY);

        Log.v(TAG,"elapsed Time: " + getElapsedTime());

        // Arbeit/Batterie einsparen
        if(!started) {
            return;
        }
        sendStringToClient("location",location.getLatitude()+","+location.getLongitude());
        sock.emitPosition(location);

        if (zielErreicht()) return;
        checkpointReached();
        Log.e(TAG,location.toString());
        Log.e(TAG,nextCheck.toString());

    }

    private void checkpointReached() {
        if (insideCheck){
            nextCheck.visit(getElapsedTime());
            setZwischenzeitenText(false);
            tts.speak(nextCheck.toSpeech(), TextToSpeech.QUEUE_ADD,null,null);
            nextCheck=getNextCheck(); //getSpot liefert (buggy) einen eine location weiter aus
            sendStringToClient("Zwischenzeiten",zwZeiten);
        }
    }

    private boolean zielErreicht() {
        if (insideCheck && nextCheck.getSpot()==1){
            nextCheck.visit(getElapsedTime());
            setZwischenzeitenText(true);
            sock.emitLap("Stefan",getElapsedTime(),checkpointList,route.getName());
            started=false;
            tts.speak("finish", TextToSpeech.QUEUE_ADD,null,null);
            sendStringToClient("finished",zwZeiten);

            //start StatActivity
            Intent i = new Intent(this,StatActivity.class);
            i.putExtra("nick","Stefan");
            i.putExtra("RouteParcel",route);
            startActivity(i);
            return true;
        }
        return false;
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

    public void setCheckpointList(ArrayList<Checkpoint> checks){


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
            zwZeiten += "\n"+"LAPTIME "+millisToDate(getElapsedTime());
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


    private void sendStringToClient(String reason, String data){
        Intent i = new Intent(broadcastIntent);
        i.putExtra(reason,data);
        sendBroadcast(i);
    }

    public long getElapsedTime() {
        if (!started)
            return 0;
        return SystemClock.elapsedRealtime() -startTime;
    }

    public void reqRoute(String s){
        sock.getSocket().emit("reqcheck",s);
    }

    public boolean isStarted() {
        return started;
    }

    public long getStartTime() {
        return startTime;
    }

    public TextToSpeech getTts() {
        return this.tts;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
        checkpointList = route.getCheckpointList();
        //nextCheck ist momentan noch immer auf 0 gesetzt
        nextCheck = route.getNextCheck();
        onLocationChanged(currentLoc);
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

    public String millisToDate(long millis){
        long hsec = ( millis / 10 ) % 100;
        long s = millis / 1000;
        long seconds = s % 60;
        long minutes = ( s / 60 ) % 60;
        return String.format("%02d:%02d.%02d",minutes,seconds,hsec);
    }
}
