package com.example.stefan.helloworld;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by stefan on 06.07.17.
 */

/*
  Diese Klasse regelt die Kommunikation mit dem Node.js Server über Socket.io für
  alle Activities und Services.
  Sie implementiert das Singleton Design Pattern, damit sie nur einmal existiert
  und für alle Activities/Services zur Verfügung steht.
  Desweiteren implementiert sie das Observer Design Pattern. Hierdurch kann sie
  auf den Listernern, die ein Interface implementiert müssen, Methoden aufrufen.
  Interface methode call("route", params)

 */

public class SocketHelper {


    private static final String TAG = "HermSocket";
    private Socket socket;
    private GPSService gpsService;
    private static List<SocketListener> listeners = new ArrayList<SocketListener>();

    private static SocketHelper socketHelper = new SocketHelper();

    private SocketHelper(){
        socketInit();
        socket.connect();
    }

    public static SocketHelper getInstance(SocketListener toAdd) {
        listeners.add(toAdd);
        return socketHelper;
    }

    /*public SocketHelper(GPSService gpsService){
        this.tts=gpsService.getTts();
        socketInit();
        socket.connect();

        this.gpsService=gpsService;
    }*/


    public void getRouteNames(){

    }

    public void socketInit(){

        Log.e(TAG,"scheissesasdasd");

        try {
            socket = IO.socket("http://84.166.5.135:3002");
            Log.v(TAG,"socket initialisiert");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on("routeNames", new Emitter.Listener(){

            @Override
            public void call(Object... args) {

            }
        });
        socket.on("route", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                JSONObject r = (JSONObject) args[0];

                try {

                    ArrayList<Checkpoint> checkpointList = new ArrayList<Checkpoint>();
                    Log.v(TAG,"Checkpoints werden empfangen");
                    JSONArray checkpoints = (JSONArray) r.get("checkpoints");

                    for (int i = 0; i < checkpoints.length(); i++) {
                        JSONObject checkpoint = checkpoints.getJSONObject(i);
                        int spot = (int) checkpoint.get("spot");
                        Location loc = new Location("gps");
                        loc.setLatitude((double)checkpoint.get("lat"));
                        loc.setLongitude((double)checkpoint.get("lon"));
                        checkpointList.add(new Checkpoint(spot,loc));
                        Log.e(TAG,checkpointList.toString());
                    }
                    Route route = new Route(r.getString("name"),checkpointList);
                            //nextCheck = checkpointList.get(0);
                    notifyListeners("setRoute", route);

                    //Log.e(TAG,"start"+start.getLatitude()+" "+start.getLongitude());
                    //Log.e(TAG,checkpointList.toString());
                }
                catch (Exception e) {System.err.println(e.getMessage());}
            }
        });


        socket.on("zieldurchsage", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                notifyListeners("zieldurchsage", args[0]);
            }
        });
    }

    private void notifyListeners(String action, Object data){
        for (SocketListener hl : listeners){
            hl.getSocketResult(action, data);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    void emitPosition(Location location){
        JSONObject loc = new JSONObject();
        try {
            loc.put("lat",location.getLatitude());
            loc.put("lon",location.getLongitude());
            JSONObject msg = new JSONObject();
            msg.put("id", 1);
            msg.put("location", loc);

            socket.emit("test1",msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void emitLap(String name, Long lapTime, ArrayList<Checkpoint> checkpointList, String routeName){
        String result=name+",";
        JSONObject times = new JSONObject();
        for (Checkpoint check:checkpointList) {
            result += check.getDurchgangszeitMillis()+",";
        }
        result+=lapTime;
        try {
            times.put("times",result);
            times.put("routeName",routeName);
            times.put("name",name);
            socket.emit("finish",times);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setActivitiy(MenuActivity act){

    }
}
