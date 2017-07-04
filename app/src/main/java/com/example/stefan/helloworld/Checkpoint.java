package com.example.stefan.helloworld;

import android.location.Location;

/**
 * Created by stefan on 18.06.17.
 */

public class Checkpoint {

    Location loc;
    int spot;
    String durchgangszeit="";
    boolean visited;

    public Checkpoint(int spot, Location loc) {
        this.loc = loc;
        this.spot = spot;
        this.visited = false;
    }


    public synchronized void visit(String time){
        if (isVisited()) //onLocationchanged kann mehrfach aufgerufen werden
            return;
        System.err.println("checkpoint"+spot+" reached");
        this.visited=true;
        durchgangszeit=time;

    }

    public boolean isVisited() {
        return visited;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public int getSpot() {
        return spot;
    }

    public void setSpot(int spot) {
        this.spot = spot;
    }

    @Override
    public String toString() {
        return "Checkpoint{" +
                "loc=" + loc +
                ", spot=" + spot +
                '}';
    }

    public String toSpeech() {
        String[] s = durchgangszeit.split(":");
        int minutes = Integer.parseInt(s[0]);
        int seconds = Integer.parseInt(s[1]);
        String secondText = (seconds==1)?seconds+" Second":seconds+" Seconds";
        String minuteText = (minutes==0)?"":((minutes==1)?minutes+" Minute":minutes+" Minutes");
        return "Checkpoint "+getSpot()+" "+minuteText+secondText;
    }

    public String getDurchgangszeit() {
        return durchgangszeit;
    }

    public void setDurchgangszeit(String durchgangszeit) {
        this.durchgangszeit = durchgangszeit;
    }

    public void reset(){
        durchgangszeit="";
        visited=false;
    }
}
