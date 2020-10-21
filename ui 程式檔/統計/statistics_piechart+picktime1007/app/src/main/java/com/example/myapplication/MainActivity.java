package com.example.myapplication;

import androidx.annotation.AttrRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.security.AccessController.getContext;

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

        //date
        selectedText = findViewById(R.id.selected_date);
        findViewById(R.id.show_dialog).setOnClickListener(new View.OnClickListener() {
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
                                selectedText.setText(s);
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
