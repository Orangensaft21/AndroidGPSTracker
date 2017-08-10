package com.example.stefan.helloworld;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MenuActivity extends AppCompatActivity {

    private SocketHelper sock;
    private String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ListView routenTable = (ListView) findViewById(R.id.RoutenListView);
        EditText nickText = (EditText) findViewById(R.id.nickinput);

        String[] values = new String[] { "Android:test", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile" };

        final ArrayAdapter lenzAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item, values);
        routenTable.setAdapter(lenzAdapter);
    }



}
