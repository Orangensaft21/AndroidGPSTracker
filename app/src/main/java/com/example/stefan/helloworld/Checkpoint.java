package com.example.stefan.helloworld;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by stefan on 18.06.17.
 */

public class Checkpoint implements Parcelable{

    /*
    CHECKPOINT CLASS: Speichert alle verfügbaren Daten über den Checkpoint,
    inklusive Highscores.
     */

    private Location loc;  
    private int spot;
    private long durchgangszeit=0;
    private boolean visited;

    public Checkpoint(int spot, Location loc) {
        this.loc = loc;
        this.spot = spot;
        this.visited = false;
    }

    /*
    Parcellable Konstruktor und Creator, automatisch generiert
     */

    protected Checkpoint(Parcel in) {
        loc = in.readParcelable(Location.class.getClassLoader());
        spot = in.readInt();
        durchgangszeit = in.readLong();
        visited = in.readByte() != 0;
    }

    public static final Creator<Checkpoint> CREATOR = new Creator<Checkpoint>() {
        @Override
        public Checkpoint createFromParcel(Parcel in) {
            return new Checkpoint(in);
        }

        @Override
        public Checkpoint[] newArray(int size) {
            return new Checkpoint[size];
        }
    };

    public synchronized void visit(long time){
        if (isVisited())
            return;
        System.err.println("checkpoint"+spot+" reached");
        this.visited=true;
        setDurchgangszeit(time);

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

    /*
    Sprachdurchsage erstellen
     */

    public String toSpeech() {
        String[] s = getDurchgangszeit().split(":");
        int minutes = Integer.parseInt(s[0]);
        String secondText = Float.parseFloat(s[1])+" Seconds";
        String minuteText = (minutes==0)?"":((minutes==1)?minutes+" Minute":minutes+" Minutes");
        return "Checkpoint "+getSpot()+" "+minuteText+secondText;
    }

    public String getDurchgangszeit() {
        return Utility.millisToDate(durchgangszeit);
    }

    public Long getDurchgangszeitMillis() { return durchgangszeit;}

    public void setDurchgangszeit(long durchgangszeit) {
        this.durchgangszeit = durchgangszeit;
    }

    public void reset(){
        durchgangszeit=0;
        visited=false;
    }

    /*
    Parcellable Methoden, automatisch generiert
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(loc, i);
        parcel.writeInt(spot);
        parcel.writeLong(durchgangszeit);
        parcel.writeByte((byte) (visited ? 1 : 0));
    }
}
