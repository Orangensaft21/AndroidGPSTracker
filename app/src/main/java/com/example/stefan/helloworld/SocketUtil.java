package com.example.stefan.helloworld;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

/**
 * Created by stefan on 04.07.17.
 */

public class SocketUtil {

    public static Socket socket;

    public static void emitPosition(Location location){
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
}
