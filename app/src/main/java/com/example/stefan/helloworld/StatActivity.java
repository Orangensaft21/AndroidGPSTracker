package com.example.stefan.helloworld;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class StatActivity extends AppCompatActivity {

    private static final String TAG = "HermStatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);
        Intent iintent = getIntent();
        TextView out = (TextView) findViewById(R.id.TextViewRoute);

        final ListView listview = (ListView) findViewById(R.id.StatsList);

        String[] values = new String[] { "Android:test", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile" };

        values = iintent.getStringArrayExtra("times");
        Route route = iintent.getParcelableExtra("RouteParcel");

        out.setText(route.getName());

        /*final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add("Checkpoint"+(i+1)+"#"+values[i]);
        }*/
        final ArrayAdapter adapter = new ListAdapter(this,
                R.layout.linestats, route.getCheckpointList());
        listview.setAdapter(adapter);
    }
}
class ListAdapter extends ArrayAdapter{

    public ListAdapter(@NonNull Context context, @LayoutRes int resource,  @NonNull List objects) {
        super(context, resource,  objects);
    }

    /*@NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String[] items = ((String) getItem(position)).split("#");
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.linestats, parent, false);
        }
        String first = items[0];
        String second = items.length>1?items[1]:"";
        TextView firstText = (TextView) convertView.findViewById(R.id.firstLine);
        TextView secondText = (TextView) convertView.findViewById(R.id.secondLine);
        firstText.setText(first);
        secondText.setText(second);
        return convertView;
    }*/

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Checkpoint check = (Checkpoint) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.linestats, parent, false);
        }
        String first = "Checkpoint "+check.getSpot();
        if (first.equals("Checkpoint 1"))
            first= "LAPTIME";
        String second = check.getDurchgangszeit();
        TextView firstText = convertView.findViewById(R.id.firstLine);
        TextView secondText = convertView.findViewById(R.id.secondLine);
        firstText.setText(first);
        secondText.setText(second);
        return convertView;
    }
}
