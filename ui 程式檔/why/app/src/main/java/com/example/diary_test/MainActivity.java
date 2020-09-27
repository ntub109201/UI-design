package com.example.diary_test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_modify);
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        final String[] lunch = {"職業", "", "", "", ""};
        ArrayAdapter<String> lunchList = new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,lunch);
        spinner.setAdapter(lunchList);
    }
}
