package com.example.recommend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class GoogleAPIResponse implements NotifyInterface, GoogleAPIResponseDataInterface{
    private final String TAG = "NiCe";
    private String api_key;
    private double lat, lng;
    private String tabText;
    private JSONObject jObject = new JSONObject();

    private ArrayList<String> tab_ch = new ArrayList<>();
    private ArrayList<String> tab_type = new ArrayList<>();

    // GetSurroundingFeatures
    private final int defaultRadius = 1000;
    private int radius = -1;
    private final String defaultSearchType = "restaurant";
    private String searchType;
    //----------------------
    // GetPlacePhoto
    private final int image_max_width = 300;
    private Map<String, Bitmap[]> place_photos = new HashMap<>();
    //----------------------
    // GetDistanceToPlace
    private Map<String, ArrayList<Integer>> place_distance = new HashMap<>();
    //----------------------
    // count finished thread
    private int finished_thread_count;

    private final Object mLock = new Object();
    GoogleAPIResponse(String api_key, double lat, double lng){
        this.api_key = api_key;
        this.lat = lat;
        this.lng = lng;
    }
    public GoogleAPIResponse setLocation(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
        return this;
    }
    public GoogleAPIResponse setTabText(String tabText){
        this.tabText = tabText;
        return this;
    }
    private void setTabList(@Nullable String[] tab){
        if (tab == null){
            String[] a = new String[]{"餐廳", "咖啡廳", "酒吧"};
            String[] b = new String[]{"restaurant", "cafe", "bar"};
            Collections.addAll(tab_ch, a);
            Collections.addAll(tab_type, b);
        }else{

        }
    }
    public void start(){
        setTabList(null);
        if (tab_type.size() != tab_ch.size() || tabText.isEmpty()){
            return;
        }
        searchType = tab_type.get(tab_ch.indexOf(tabText));
        if (radius == -1) radius = defaultRadius;
        if (searchType == null) searchType = defaultSearchType;

        Thread getSurroundingFeatures = new Thread(new GetSurroundingFeatures());
        getSurroundingFeatures.start();
        synchronized(this){
            try{
                this.wait();
                Log.d("NiCe", "OK_1");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        Log.d("NiCe", "OK_2");
        finished_thread_count = 0;
        Thread getPlacePhoto = new Thread(new GetPlacePhoto(this));
        Thread getDistanceToPlace = new Thread(new GetDistanceToPlace(this));
        getPlacePhoto.start();
        getDistanceToPlace.start();
        Log.d("NiCe", "OK_3");
        synchronized (this){
            while(true){
                if (finished_thread_count >= 2) break;
                try{
                    this.wait();
                    Log.d("NiCe", "OK_4");
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            Log.d("NiCe", "OK_5");
        }
    }
    @Override
    public void runEnd() {
        synchronized (this) {
            ++finished_thread_count;
            Log.d(TAG, "runEnd: "+ finished_thread_count);
            this.notify();
        }
    }
    private class GetSurroundingFeatures implements Runnable{
        private String getSurroundingFeature_api;
        GetSurroundingFeatures(){
            StringBuilder sb = new StringBuilder();
            sb.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
                    .append("location=").append(lat).append(",").append(lng).append("&")
                    .append("radius=").append(radius).append("&")
                    .append("types=").append(searchType).append("&")
                    .append("sensor=").append(true).append("&")
                    .append("key=").append(api_key);
            this.getSurroundingFeature_api = sb.toString();
        }
        @Override
        public void run() {
            synchronized(GoogleAPIResponse.this) {
                Log.d("NiCe", this.getSurroundingFeature_api);
                HttpURLConnection connection;
                try{
                    URL url = new URL(getSurroundingFeature_api);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(true);
                    connection.setAllowUserInteraction(false);
                    connection.setDoInput(true);
                    connection.connect();
                    int status = connection.getResponseCode();
                    switch (status){
                        case 200:
                        case 201:
                            BufferedReader br =new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();
                            jObject.put(tabText, new JSONObject(sb.toString()));
                            Log.d(TAG, sb.toString());
                            GoogleAPIResponse.this.notify();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class GetPlacePhoto implements Runnable{
        private NotifyInterface mInterface = null;
        private String[] GetPlacePhoto_api;
        GetPlacePhoto(NotifyInterface iface) {
            this.mInterface = iface;
            if (jObject.has(tabText) && !jObject.isNull(tabText)){
                try{
                    if (jObject.getJSONObject(tabText).getJSONArray("results").length() > 0){
                        GetPlacePhoto_api = new String[jObject.getJSONObject(tabText).getJSONArray("results").length()];
                        for (int i=0; i<jObject.getJSONObject(tabText).getJSONArray("results").length(); i++){
                            try{
                                StringBuilder sb = new StringBuilder();
                                GetPlacePhoto_api[i] =
                                        sb.append("https://maps.googleapis.com/maps/api/place/photo?")
                                                .append("photoreference=").append(jObject.getJSONObject(tabText).getJSONArray("results").getJSONObject(i).getJSONArray("photos").getJSONObject(0).getString("photo_reference")).append("&")
                                                .append("key=").append(api_key).append("&")
                                                .append("maxwidth=").append(image_max_width).toString();
                                Log.d(TAG, i+"GetPlacePhoto: "+GetPlacePhoto_api[i]);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
        private void notifyEnd() {
            if (this.mInterface != null)
                this.mInterface.runEnd();
        }
        @Override
        public void run() {
            if (GetPlacePhoto_api != null){
                Bitmap[] bitmaps = new Bitmap[GetPlacePhoto_api.length];
                for (int i=0; i<GetPlacePhoto_api.length; i++){
                    bitmaps[i] = getBitmapFromURL(GetPlacePhoto_api[i]);
                }
                place_photos.put(tabText, bitmaps);
                this.notifyEnd();
            }
        }
        private Bitmap getBitmapFromURL(String url){
            Bitmap bitmap;
            HttpURLConnection connection;
            try{
                URL u = new URL(url);
                connection = (HttpURLConnection) u.openConnection();
                // connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                connection.setAllowUserInteraction(false);
                connection.setDoInput(true);
                connection.connect();
                int status = connection.getResponseCode();
                switch (status){
                    case 200:
                    case 201:
                        InputStream input = connection.getInputStream();
                        bitmap = BitmapFactory.decodeStream(input);
                        return ImageHelper.getRoundedCornerBitmap(bitmap, 20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private class GetDistanceToPlace implements Runnable{
        private NotifyInterface mInterface = null;
        private String[] GetDistanceToPlace_api;
        GetDistanceToPlace(NotifyInterface iface){
            this.mInterface = iface;
            if (jObject.has(tabText) && !jObject.isNull(tabText)) {
                try {
                    int sent_count = (int) Math.ceil(jObject.getJSONObject(tabText).getJSONArray("results").length() / 25.0);
                    GetDistanceToPlace_api = new String[sent_count];
                    Log.d(TAG, "GetDistanceToPlace: " + sent_count);
                    for (int i = 0; i < sent_count; i++) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("https://maps.googleapis.com/maps/api/distancematrix/json?")
                                .append("key=").append(api_key).append("&")
                                .append("mode=").append("walking").append("&")
                                .append("origins=").append(lat).append(",").append(lng).append("&")
                                .append("destinations=");
                        for (int j = i * 25; j < jObject.getJSONObject(tabText).getJSONArray("results").length(); j++) {
                            if (j == 25 + i * 25) break;
                            sb.append(jObject.getJSONObject(tabText).getJSONArray("results").getJSONObject(j).getJSONObject("geometry").getJSONObject("location").getDouble("lat"))
                                    .append(",")
                                    .append(jObject.getJSONObject(tabText).getJSONArray("results").getJSONObject(j).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                            if (j != jObject.getJSONObject(tabText).getJSONArray("results").length()-1)
                                sb.append("|");
                        }
                        GetDistanceToPlace_api[i] = sb.toString();
                        Log.d(TAG, "GetDistanceToPlace: " + GetDistanceToPlace_api[i]);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        private void notifyEnd() {
            if (this.mInterface != null)
                this.mInterface.runEnd();
        }
        @Override
        public void run() {
            if (GetDistanceToPlace_api != null){
                ArrayList<Integer> arrayList = new ArrayList<>();;
                for (String api : GetDistanceToPlace_api) {
                    try {
                        HttpURLConnection connection;
                        URL url = new URL(api);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setUseCaches(false);
                        connection.setAllowUserInteraction(false);
                        connection.setDoInput(true);
                        connection.connect();
                        int status = connection.getResponseCode();
                        Log.d(TAG, "status: "+status);
                        switch (status) {
                            case 200:
                            case 201:
                                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    sb.append(line).append("\n");
                                }
                                br.close();
                                JSONObject jsonObject = new JSONObject(sb.toString());
                                for (int i = 0; i < jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").length(); i++) {
                                    arrayList.add(jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(i).getJSONObject("distance").getInt("value"));
                                    Log.d(TAG, "run: "+jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(i).getJSONObject("distance").getInt("value"));
                                }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                place_distance.put(tabText, arrayList);
                this.notifyEnd();
            }
        }
    }

    @Override
    public String getPlaceName(int position) {
        String placeName = null;
        try{
            if (this.jObject.has(tabText) && !this.jObject.isNull(tabText))
                placeName = this.jObject.getJSONObject(tabText).getJSONArray("results").getJSONObject(position).getString("name");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return placeName;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public Bitmap getPlacePhoto(int position){
        return (Objects.requireNonNull(this.place_photos.get(tabText)))[position];
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int getPlaceDistance(int position){
        return (this.place_distance.get(tabText)).get(position);
    }
    @Override
    public int getItemsCount(){
        int itemsCount = 0;
        try{
            if (this.jObject.has(tabText) && !this.jObject.isNull(tabText))
                itemsCount = this.jObject.getJSONObject(tabText).getJSONArray("results").length();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return itemsCount;
    }
    @Override
    public double getLng(int position){
        double lng = 0;
        try{
            lng = jObject.getJSONObject(tabText).getJSONArray("results").getJSONObject(position).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
        }catch (JSONException e){
            e.printStackTrace();
        }

        return lng;
    }
    @Override
    public double getLat(int position){
        double lat = 0;
        try{
            lat = jObject.getJSONObject(tabText).getJSONArray("results").getJSONObject(position).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
        }catch (JSONException e){
            e.printStackTrace();
        }

        return lat;
    }
    public GoogleAPIResponseDataInterface googleAPIResponseDataInterface(){
        return this;
    }
}
