package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PieChart pieChart= findViewById(R.id.pie_chart);
        ArrayList<PieEntry> visitors = new ArrayList<>();
        visitors.add(new PieEntry(508,"美食"));
        visitors.add(new PieEntry(600,"購物"));
        visitors.add(new PieEntry(750,"感情"));
        visitors.add(new PieEntry(600,"旅遊"));
        visitors.add(new PieEntry(670,"休閒娛樂"));

        PieDataSet pieDateSet = new PieDataSet(visitors,"");
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(252, 204, 203));
        colors.add(Color.rgb(114, 188, 223));
        colors.add(Color.rgb(255, 123, 124));
        colors.add(Color.rgb(57, 135, 200));
        colors.add(Color.rgb(197, 212, 231));
        pieDateSet.setColors(colors);
        pieDateSet.setValueTextColor(Color.BLACK);
        pieDateSet.setValueTextSize(16f);


        PieData pieData = new PieData(pieDateSet);

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.animate();
        pieChart.setDrawHoleEnabled(false);


    }
}
