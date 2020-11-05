package com.example.myapplication;

import androidx.annotation.AttrRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import java.util.HashMap;
import android.app.Activity;
import java.lang.ref.WeakReference;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Map;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    private TextView selectedText;
    private MaterialDatePicker.Builder<Pair<Long,Long>> builder;
    private CalendarConstraints.Builder constraintsBuilder;
    private MaterialDatePicker<Pair<Long,Long>> materialDatePicker;
    private MaterialDatePicker<?> picker;
    private static ProgressBar progressbar;
    private long today;
    int moodResult01 = sqlReturn.moodResult01;
    int moodResult02 = sqlReturn.moodResult02;
    int moodResult03 = sqlReturn.moodResult03;
    int moodResult04 = sqlReturn.moodResult04;
    int moodResult05 = sqlReturn.moodResult05;
    String d1;
    String d2;
    PieChart pieChart;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 隱藏所有物件
        findViewById(R.id.suggestion).setVisibility( View.INVISIBLE );
        findViewById(R.id.recommend).setVisibility( View.INVISIBLE );
        findViewById(R.id.pie_chart).setVisibility( View.INVISIBLE );
        findViewById(R.id.statistics__no_text_1).setVisibility( View.INVISIBLE );
        findViewById(R.id.statistics__no_text_2).setVisibility( View.INVISIBLE );
        findViewById(R.id.statistics_no).setVisibility( View.INVISIBLE );

        progressbar = findViewById(R.id.progressBar);
        progressbar.setZ(10);

        //取得這一個禮拜的日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -7);
        String startDate=sdf.format(c.getTime());
        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 0);
        String endDate = sdf.format(c.getTime());

        selectedText = findViewById(R.id.date_start);
        selectedText.setText(startDate);
        selectedText = findViewById(R.id.date_end);
        selectedText.setText(endDate);
        mood_statistics(startDate,endDate);

        initialize();

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


    private  void suggest() {
        // 建議
        int[] moodResult = {sqlReturn.moodResult01, sqlReturn.moodResult02, sqlReturn.moodResult03, sqlReturn.moodResult04, sqlReturn.moodResult05};

        // 資料 - 心情
        String[] mood1 =
                {"每天做一件令別人愉快的事，自己也會特別快樂。",
                        "好心情像冬天裡難得的好天氣，一瞬間照亮瞭心間；好心情像夏天的冰棍，一下子涼爽瞭心田。",
                        "有了積極的心態，便有了戰勝一切困難取得成功的信心。繼續保持！"};
        String[] mood2 =
                {"心情普遍不錯唷！要記得，只要心情是晴朗的，人生就沒有雨天，繼續保持：）",
                        "微笑，是最美的陽光",};
        String[] mood3 =
                {"健康良好的心理是取得成功的開端",
                        "保持一顆年輕的心，做個簡單的人，享受陽光和溫暖。",
                        "只要你還願意，世界一定會給你驚喜。"};
        String[] mood4 =
                {"活在當下，別在懷念過去或者憧憬未來中浪費掉你現在的生活。",
                        "結局很美妙的事，但開頭並非如此，不必太灰心。",
                        "任何事情，總有答案。與其煩惱，不如順其自然",
                        "一切都會好起來的，即使不會是在今天，但總有一天會的",
                        "日出东海落西山，愁也一天，喜也一天；遇事不钻牛角尖，人也舒坦，心也舒坦。"};
        String[] mood5 =
                {"如果我不堅強、誰能替我勇敢，如果我不獨立誰又能給與支持！找到問題點，一起面對它、解決他！",
                        "當你不能夠再擁有的時候，你唯一可以做的就是令自己不要忘記。",
                        "不要小看自己，因為人有無限的可能。",
                        "所有看似美好的，都經歷過或者正在經歷著不美好。"};
        // 資料 - tag
        String[] tag1 =
                {"每天做一件令別人愉快的事，自己也會特別快樂。",
                        "好心情像冬天裡難得的好天氣，一瞬間照亮瞭心間；好心情像夏天的冰棍，一下子涼爽瞭心田。",
                        "有了積極的心態，便有了戰勝一切困難取得成功的信心。繼續保持！"};
        String[] tag2 =
                {"心情普遍不錯唷！要記得，只要心情是晴朗的，人生就沒有雨天，繼續保持：）",
                        "微笑，是最美的陽光",};
        String[] tag3 =
                {"健康良好的心理是取得成功的開端",
                        "保持一顆年輕的心，做個簡單的人，享受陽光和溫暖。",
                        "只要你還願意，世界一定會給你驚喜。"};
        String[] tag4 =
                {"活在當下，別在懷念過去或者憧憬未來中浪費掉你現在的生活。",
                        "結局很美妙的事，但開頭並非如此，不必太灰心。",
                        "任何事情，總有答案。與其煩惱，不如順其自然",
                        "一切都會好起來的，即使不會是在今天，但總有一天會的",
                        "日出东海落西山，愁也一天，喜也一天；遇事不钻牛角尖，人也舒坦，心也舒坦。"};
        String[] tag5 =
                {"如果我不堅強、誰能替我勇敢，如果我不獨立誰又能給與支持！找到問題點，一起面對它、解決他！",
                        "當你不能夠再擁有的時候，你唯一可以做的就是令自己不要忘記。",
                        "不要小看自己，因為人有無限的可能。",
                        "所有看似美好的，都經歷過或者正在經歷著不美好。"};


        selectedText = findViewById(R.id.suggestion);
        selectedText.setText("");
        if(moodResult[0] >= moodResult[1] && moodResult[0] >= moodResult[2] && moodResult[0] >= moodResult[3] && moodResult[0] >= moodResult[4]) {
            int num=mood1.length;
            int number_random = (int)(Math.random()*num);
            selectedText.append(mood1[number_random]);
        }else if(moodResult[1] >= moodResult[0] && moodResult[1] >= moodResult[2] && moodResult[1] >= moodResult[3] && moodResult[1] >= moodResult[4]) {
            int num=mood1.length;
            int number_random = (int)(Math.random()*num);
            selectedText.append(mood2[number_random]);
        }else if(moodResult[2] >= moodResult[0] && moodResult[2] >= moodResult[1] && moodResult[2] >= moodResult[3] && moodResult[1] >= moodResult[4]) {
            int num=mood1.length;
            int number_random = (int)(Math.random()*num);
            selectedText.append(mood3[number_random]);
        }else if(moodResult[3] >= moodResult[0] && moodResult[3] >= moodResult[2] && moodResult[3] >= moodResult[1] && moodResult[1] >= moodResult[4]) {
            int num=mood1.length;
            int number_random = (int)(Math.random()*num);
            selectedText.append(mood4[number_random]);
        }else if(moodResult[4] >= moodResult[0] && moodResult[4] >= moodResult[2] && moodResult[4] >= moodResult[3] && moodResult[4] >= moodResult[1]) {
            int num=mood1.length;
            int number_random = (int)(Math.random()*num);
            selectedText.append(mood5[number_random]);
        }else{
            selectedText.append("資料不足");
        }
    }


    private  void pieChart() {
        selectedText = findViewById(R.id.suggestion);
        selectedText.append(Integer.toString(moodResult01));

        pieChart= findViewById(R.id.pie_chart);
        pieChart.setEntryLabelTextSize(17f); //圖表裡文字大小
        pieChart.setEntryLabelColor(Color.parseColor("#ffffff")); //圖表裡文字顏色
        ArrayList<PieEntry> visitors = new ArrayList<>();


        if(moodResult01 != 0){
            visitors.add(new PieEntry(moodResult01,"晴天"));
        }
        if(moodResult02 != 0){
            visitors.add(new PieEntry(moodResult02,"時晴"));
        }
        if(moodResult03 != 0){
            visitors.add(new PieEntry(moodResult03,"多雲"));
        }
        if(moodResult04 != 0){
            visitors.add(new PieEntry(moodResult04,"陣雨"));
        }
        if(moodResult05 != 0){
            visitors.add(new PieEntry(moodResult05,"雷雨"));
        }

//        visitors.add(new PieEntry(moodResult01,"美食"));
//        visitors.add(new PieEntry(moodResult02,"購物"));
//        visitors.add(new PieEntry(moodResult03,"感情"));
//        visitors.add(new PieEntry(moodResult04,"旅遊"));
//        visitors.add(new PieEntry(moodResult05,"休閒娛樂"));

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
        pieData.setValueFormatter(new DefaultValueFormatter(0)); //設定小數點
        pieData.setValueFormatter(new PercentFormatter(pieChart));  // ％ 顯示
        pieChart.setUsePercentValues(true);  // 轉換為百分比

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.animate();
        pieChart.setDrawHoleEnabled(false); //true = 空心圓



        pieChart.getLegend().setTextSize(14f); //圖例文字大小
        pieChart.getLegend().setFormSize(10);  //圖例大小
        pieChart.getLegend().setTextColor(Color.parseColor("#87C3C0"));//圖例顏色
        pieChart.getLegend().setFormToTextSpace(10f); //圖例與文字的間鉅
        pieChart.getLegend().setXEntrySpace(30);
//        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);//圖例水平居中

        pieChart.invalidate();
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
//            picker.show(getSupportFragmentManager(), picker.toString());
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
                    progressbar.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "positive", Toast.LENGTH_SHORT).show();
                    if (selection instanceof Pair){
                        long startDate=0, endDate=0;
                        if (Long.class.equals(((Pair) selection).first.getClass()))
                            startDate = (long) ((Pair) selection).first;
                        if (Long.class.equals(((Pair) selection).second.getClass()))
                            endDate = (long) ((Pair) selection).second;
                        if (startDate!=0 && endDate!=0){
                            try {
                                 d1 = DateConvertTool.longToString(startDate, "yyyy/MM/dd");
                                 d2 = DateConvertTool.longToString(endDate, "yyyy/MM/dd");
                                String s = "Start: "+d1+", \nEnd: "+d2;
                                //selectedText.setText(s);
                                selectedText = findViewById(R.id.date_start);
                                selectedText.setText(d1);
                                selectedText = findViewById(R.id.date_end);
                                selectedText.setText(d2);
                                mood_statistics(d1,d2);
                                pieChart();
                                suggest();

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



/* mood_statistics */
    public void mood_statistics(String startDate,String endDate){
        // String uid = sqlReturn.GetUserID;
        String uid = "2020-04-28 13:08:35";
        Map<String,String> map = new HashMap<>();
        map.put("command", "moodCaculate");
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("uid", uid);
        //map.put("uid", uid);
//        selectedText = findViewById(R.id.suggestion);
//        selectedText.append(startDate);

        new mood_statistics(this).execute((HashMap)map);

    }
    private class mood_statistics extends HttpURLConnection_AsyncTask {
        // 建立弱連結
        WeakReference<Activity> activityReference;
        mood_statistics(Activity context){
            activityReference = new WeakReference<>(context);
        }
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(String result) {
            JSONObject jsonObject = null;
            JSONArray jsonArray = null;
            boolean status = false;
            // 取得弱連結的Context
            Activity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            try {
                Log.d("error", result);
                jsonObject = new JSONObject(result);

                status = jsonObject.getBoolean("status");
                if(status){
                    sqlReturn.moodResult01 = jsonObject.getInt("心情1");
                    sqlReturn.moodResult02 = jsonObject.getInt("心情2");
                    sqlReturn.moodResult03 = jsonObject.getInt("心情3");
                    sqlReturn.moodResult04 = jsonObject.getInt("心情4");
                    sqlReturn.moodResult05 = jsonObject.getInt("心情5");
                    moodResult01 = sqlReturn.moodResult01;
                    moodResult02 = sqlReturn.moodResult02;
                    moodResult03 = sqlReturn.moodResult03;
                    moodResult04 = sqlReturn.moodResult04;
                    moodResult05 = sqlReturn.moodResult05;
                    Log.d("mood1", String.valueOf(moodResult01));
                    Log.d("mood2", String.valueOf(moodResult02));
                    Log.d("mood3", String.valueOf(moodResult03));
                    Log.d("mood4", String.valueOf(moodResult04));
                    Log.d("mood5", String.valueOf(moodResult05));
//                    moodResult01 = 0;
//                    moodResult02 = 0;
//                    moodResult03 = 1000;
//                    moodResult04 = 0;
//                    moodResult05 = 0;
                    if(moodResult01 == 0 && moodResult02 == 0 && moodResult03 == 0 && moodResult04 ==0 && moodResult05 == 0){
                        // 資料不足
                        findViewById(R.id.suggestion).setVisibility( View.INVISIBLE );
                        findViewById(R.id.recommend).setVisibility( View.INVISIBLE );
                        findViewById(R.id.pie_chart).setVisibility( View.INVISIBLE );
                        findViewById(R.id.statistics__no_text_1).setVisibility( View.VISIBLE );
                        findViewById(R.id.statistics__no_text_2).setVisibility( View.VISIBLE );
                        findViewById(R.id.statistics_no).setVisibility( View.VISIBLE );
                    }else {
                        findViewById(R.id.suggestion).setVisibility( View.VISIBLE );
                        findViewById(R.id.recommend).setVisibility( View.VISIBLE );
                        findViewById(R.id.pie_chart).setVisibility( View.VISIBLE );
                        findViewById(R.id.statistics__no_text_1).setVisibility( View.INVISIBLE );
                        findViewById(R.id.statistics__no_text_2).setVisibility( View.INVISIBLE );
                        findViewById(R.id.statistics_no).setVisibility( View.INVISIBLE );
                        pieChart();
                        suggest();
                    }
                    progressbar.setVisibility(View.INVISIBLE);
                }else {
                    Toast.makeText(activity, "失敗", Toast.LENGTH_LONG).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

}
