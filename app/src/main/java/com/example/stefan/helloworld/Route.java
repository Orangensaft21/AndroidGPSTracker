package com.example.stefan.helloworld;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by stefan on 18.07.17.
 */

public class Route implements Parcelable{

    private ArrayList<Checkpoint> checkpointList;
    private Checkpoint nextCheck;
    private String name;

    public Route(String name, ArrayList<Checkpoint> checkpointList) {
        this.checkpointList = checkpointList;
        this.name = name;
        this.nextCheck = checkpointList.get(0);
    }
    /*
    TODO Bestzeiten rein und so
     */


    protected Route(Parcel in) {
        checkpointList = in.createTypedArrayList(Checkpoint.CREATOR);
        nextCheck = in.readParcelable(Checkpoint.class.getClassLoader());
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(checkpointList);
        dest.writeParcelable(nextCheck, flags);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public void reset(){
        this.nextCheck = checkpointList.get(0);
    }

    public ArrayList<Checkpoint> getCheckpointList() {
        return checkpointList;
    }

    public Checkpoint getNextCheck() {
        return nextCheck;
    }

    public String getName() {
        return name;
    }

    /*
    returns an array with time splits
     */
    public String[] getZwischenZeiten(){
        ArrayList<String> res = new ArrayList<String>();
        for (Checkpoint check : checkpointList){
            if (check.getDurchgangszeit()!=null)
                res.add(check.getDurchgangszeit());
        }
        return res.toArray(new String[res.size()]);
    }


}
