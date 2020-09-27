package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Adapter adapter;
    ArrayList<String> items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items = new ArrayList<>();
        items.add("first");
        items.add("Second");
        items.add("Third");
        items.add("Fourth");
        items.add("Fifth");
        items.add("Sixth");

        /*description = new ArrayList<>();
        description.add("first111");
        description.add("Second222");
        description.add("Third333");
        description.add("Fourth444");
        description.add("Fifth555");
        description.add("Sixth666");*/


        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this,items);
        //adapter1 = new Adapter(this,description);
        recyclerView.setAdapter(adapter);
        //recyclerView.setAdapter(adapter1);

    }
}
