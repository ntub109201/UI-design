package com.example.myapplication;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private TextView selectedText;
    private MaterialDatePicker.Builder<Pair<Long,Long>> builder;
    private CalendarConstraints.Builder constraintsBuilder;
    private MaterialDatePicker<Pair<Long,Long>> materialDatePicker;
    private MaterialDatePicker<?> picker;
    private long today;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();


        PieChart pieChart= findViewById(R.id.pie_chart);
        pieChart.setEntryLabelTextSize(17f); //圖表裡文字大小
        pieChart.setEntryLabelColor(Color.parseColor("#ffffff")); //圖表裡文字顏色
        ArrayList<PieEntry> visitors = new ArrayList<>();
        visitors.add(new PieEntry(20,"美食"));
        visitors.add(new PieEntry(30,"購物"));
        visitors.add(new PieEntry(10,"感情"));
        visitors.add(new PieEntry(10,"旅遊"));
        visitors.add(new PieEntry(30,"休閒娛樂"));

        PieDataSet pieDateSet = new PieDataSet(visitors,"");
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(245, 187, 207));
        colors.add(Color.rgb(248, 210, 189));
        colors.add(Color.rgb(236, 228, 76));
        colors.add(Color.rgb(119, 183, 246));
        colors.add(Color.rgb(142, 225, 149));
        pieDateSet.setColors(colors);
        pieDateSet.setValueTextColor(Color.DKGRAY);
        pieDateSet.setValueTextSize(16f);


        PieData pieData = new PieData(pieDateSet);
        pieData.setDrawValues(true);
        pieData.setValueFormatter(new PercentFormatter());


        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.animate();
        pieChart.setDrawHoleEnabled(false); //true = 空心圓
        pieChart.getLegend().setTextSize(14f); //圖例文字大小
        pieChart.getLegend().setFormSize(10);  //圖例大小
        pieChart.getLegend().setTextColor(Color.parseColor("#87C3C0"));//圖例顏色
        pieChart.getLegend().setFormToTextSpace(10f); //圖例與文字的間鉅
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);//圖例水平居中






        //date
        selectedText = findViewById(R.id.selected_date);
        findViewById(R.id.show_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.show(getSupportFragmentManager(), picker.toString());
            }
        });
        findViewById(R.id.show_dialog2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.show(getSupportFragmentManager(), picker.toString());
            }
        });
    }
    private void showDatePickerDialog(){
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                this,
//                this,
//                Calendar.getInstance().get(Calendar.YEAR),
//                Calendar.getInstance().get(Calendar.MONTH),
//                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
//        );
//        datePickerDialog.show();
        materialDatePicker.show(getSupportFragmentManager(), "NiCe");
    }
    private void initialize(){
        today = MaterialDatePicker.todayInUtcMilliseconds();
        // CalenderConstraintBuilder
        constraintsBuilder = new CalendarConstraints.Builder();

        // MaterialDatePickerBuilder
        // set mode -> range
        builder = MaterialDatePicker.Builder.dateRangePicker();
        // set theme -> dialog
        TypedValue typedValue = new TypedValue();
        if (getApplicationContext().getTheme().resolveAttribute(R.attr.materialCalendarTheme, typedValue, true)) {
            int dialogTheme = typedValue.data;
            builder.setTheme(dialogTheme);
        }
        // set bounds -> default
        // set valid days -> last 1 year
        Calendar upperBoundCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        upperBoundCalendar.add(Calendar.DAY_OF_YEAR, 0);
        long upperBound = upperBoundCalendar.getTimeInMillis();
        List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
        validators.add(DateValidatorPointBackward.before(upperBound));
        //validators.add(new DateValidatorWeekdays());
        constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators));
        // set picker title -> NiCeTest
        builder.setTitleText("NiCeTest");
        // set opening month
        constraintsBuilder.setOpenAt(today);
        // set default selection
        builder.setSelection(new Pair<>(today, today));
        // set input mode
        builder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR);
        try {
            builder.setCalendarConstraints(constraintsBuilder.build());
            picker = builder.build();
            addSnackBarListeners(picker);
            picker.show(getSupportFragmentManager(), picker.toString());
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = "日期："+ year + "/" + month + "/" + dayOfMonth;
        selectedText.setText(date);
        Log.d("NiCe", "onDateSet: ");
    }
    private static int resolveOrThrow(Context context, @AttrRes int attributeResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
            return typedValue.data;
        }
        throw new IllegalArgumentException(context.getResources().getResourceName(attributeResId));
    }
    private void addSnackBarListeners(MaterialDatePicker<?> materialCalendarPicker) {
        materialCalendarPicker.addOnPositiveButtonClickListener(
                selection -> {
                    Toast.makeText(this, "positive", Toast.LENGTH_SHORT).show();
                    if (selection instanceof Pair){
                        long startDate=0, endDate=0;
                        if (Long.class.equals(((Pair) selection).first.getClass()))
                            startDate = (long) ((Pair) selection).first;
                        if (Long.class.equals(((Pair) selection).second.getClass()))
                            endDate = (long) ((Pair) selection).second;
                        if (startDate!=0 && endDate!=0){
                            try {
                                String d1 = DateConvertTool.longToString(startDate, "yyyy-MM-dd");
                                String d2 = DateConvertTool.longToString(endDate, "yyyy-MM-dd");
                                String s = "Start: "+d1+", \nEnd: "+d2;
                                //selectedText.setText(s);
                                selectedText = findViewById(R.id.date_start);
                                selectedText.setText(d1);
                                selectedText = findViewById(R.id.date_end);
                                selectedText.setText(d2);
                                // 建議(需判斷)
                                selectedText = findViewById(R.id.suggestion);
                                selectedText.append("這段期間購物的頻率偏高耶！要謹慎理財，掌握金錢的支出～\n");
                                selectedText.append("\n適度的娱樂能放鬆人的情緒，陶冶人的情操");

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        materialCalendarPicker.addOnNegativeButtonClickListener(
                dialog -> {
                    Toast.makeText(this, "negative", Toast.LENGTH_SHORT).show();
                });
        materialCalendarPicker.addOnCancelListener(
                dialog -> {
                    Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
                });
    }
}
