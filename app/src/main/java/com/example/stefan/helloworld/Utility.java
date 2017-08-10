package com.example.stefan.helloworld;

/**
 * Created by stefan on 08.08.17.
 */

public class Utility {

    public static String millisToDate(long millis){
        long hsec = ( millis /10 ) %100;
        long s = millis / 1000;
        long seconds = s % 60;
        long minutes = ( s / 60 ) % 60;
        return String.format("%02d:%02d.%02d",minutes,seconds,hsec);
    }
}
