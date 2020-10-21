package com.example.recommend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.daimajia.swipe.util.Attributes;
import com.example.recommend.adpter.util.ListViewAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import com.daimajia.swipe.SwipeLayout;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Context mContext = this;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static GoogleMap map;
    private UiSettings ui;
    private CameraPosition cameraPosition;

    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final LatLng defaultLocation = new LatLng(25.0418903,121.5256203);
    private static final int DEFAULT_ZOOM = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private static final String KEY_LOCATION = "location";

    // GetSurroundingFeatures
    private final int defaultRadius = 1000;
    private int radius = -1;
    private final String defaultSearchType = "restaurant";
    private String searchType;
    // ---------------------------------
    private static JSONObject jObject = new JSONObject();
    // TabLayout
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private ArrayList<String> tab_ch = new ArrayList<>();
    private ArrayList<String> tab_type = new ArrayList<>();
    // Views
    private Button search_around_btn;
    // SwipeLayout
    private SwipeLayout swipeLayout;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private Map<String, Bitmap[]> bitmaps = new HashMap<>();

    private BottomSheetBehavior bottomSheetBehavior;

    private GoogleAPIResponse googleAPIResponse;
    public MapsActivity() {
    }

    // ---------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // get the bottom sheet view
        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);


        // change the state of the bottom sheet
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // set the peek height
        bottomSheetBehavior.setPeekHeight(250);

        // set hideable or not
        bottomSheetBehavior.setHideable(false);

        Log.d(TAG, "onCreate: "+bottomSheetBehavior.getState());


        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        setTabList(null);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        for (String s : tab_ch) tabLayout.addTab(tabLayout.newTab().setText(s));
        // set Tab Seleceted Listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                map.clear();
                // add JSONObject from google map api
                Log.d(TAG, "onTabSelected: ");
                String tab_name = (String) tab.getText();
                if (!jObject.has(tab_name) && jObject.isNull(tab_name)){
                    if (tab.getText() != null){
//                        InitListView((String) tab.getText());
                        googleAPIResponse = new GoogleAPIResponse(getResources().getString(R.string.google_maps_key_web), lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        googleAPIResponse.setTabText(tab_name).start();
                        mListView = findViewById(R.id.features_listView);
                        mAdapter = new ListViewAdapter(mContext, googleAPIResponse.googleAPIResponseDataInterface());
                        Log.d(TAG, mAdapter.toString());
                        mListView.setAdapter(mAdapter);
                        mAdapter.setMode(Attributes.Mode.Single);

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ((SwipeLayout)(mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
                            }
                        });
                        mListView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                Log.e("ListView", "OnTouch");
                                return false;
                            }
                        });
                        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                Toast.makeText(mContext, "OnItemLongClickListener", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                        });
                        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(AbsListView view, int scrollState) {
                                Log.e("ListView", "onScrollStateChanged");
                            }

                            @Override
                            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                            }
                        });

                        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Log.e("ListView", "onItemSelected:" + position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                Log.e("ListView", "onNothingSelected:");
                            }
                        });
                        for (int i=0; i<googleAPIResponse.getItemsCount(); i++){
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(googleAPIResponse.getLat(i),googleAPIResponse.getLng(i))));
                        }
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        ui = map.getUiSettings();
        ui.setZoomControlsEnabled(true);

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                Log.d(TAG, "NiCe: "+lastKnownLocation.getLatitude()+" - "+lastKnownLocation.getLongitude());
//                                new GoogleAPIResponse(getResources().getString(R.string.google_maps_key_web), lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()).setTabText(("咖啡廳")).start();
                            }

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void InitListView(@NonNull String tab_text) {
        StringBuilder sb = new StringBuilder();
        searchType = tab_type.get(tab_ch.indexOf(tab_text));
        if (tab_type.size() != tab_ch.size() || tab_text.isEmpty()){
            return;
        }
        if (radius == -1) radius = defaultRadius;
        if (searchType == null) searchType = defaultSearchType;
        // Toast.makeText(mContext, tab.getText(), Toast.LENGTH_SHORT).show();
        // Get JSON data from map api
        sb.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
                .append("location=").append(lastKnownLocation.getLatitude()).append(",").append(lastKnownLocation.getLongitude()).append("&")
                .append("radius=").append(radius).append("&")
                .append("types=").append(searchType).append("&")
                .append("sensor=").append(true).append("&")
                .append("key=").append(getResources().getString(R.string.google_maps_key_web));
        String ListApi = sb.toString();
        Log.d(TAG, "onClick: " + ListApi);
        GetJSONObjectFromAPI getJSONObjectFromAPI = new GetJSONObjectFromAPI(tab_text, getResources().getString(R.string.google_maps_key_web),this);
        getJSONObjectFromAPI.execute(ListApi);
    }
    private static class GetJSONObjectFromAPI extends AsyncTask<String, Void, String>{
        private String tab_text;
        private String key;
        WeakReference<MapsActivity> activityReference;

        // setting variables
        private final int list_item_image_width = 300;
        //private final int list_item_image_height = 225;
        // ----------------------------------------------
        GetJSONObjectFromAPI(@NonNull String tab_text, String key, MapsActivity context){
            this.tab_text = tab_text;
            this.key = key;
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... urls) {
            String s = GET(urls[0]);
            Log.d(TAG, "doInBackground: " +s);
            return s;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result==null) {
                Log.d(TAG, "onPostExecute: null");
                return;
            }
            final MapsActivity activity = activityReference.get();
            try {
                JSONObject jObject = new JSONObject(result);
                MapsActivity.jObject.put(this.tab_text, jObject);
//                for(int i = 0; i< jObject.getJSONArray("results").length(); i++){
//                    Log.i("results name", jObject.getJSONArray("results").getJSONObject(i).getString("name"));
//                }
                int count = 0;
                Log.d(TAG, "1");
                // do some swipeLayout stuff
                Log.d(TAG, "2");
                Log.d(TAG, MapsActivity.jObject.has(tab_text) +String.valueOf(!MapsActivity.jObject.isNull(tab_text)));
                if (MapsActivity.jObject.has(tab_text) & !MapsActivity.jObject.isNull(tab_text)){
                    Log.d(TAG, "3");
                    count = MapsActivity.jObject.getJSONObject(tab_text).getJSONArray("results").length();
                    Log.d(TAG, String.valueOf(MapsActivity.jObject.has(tab_text)));
                }else{
                    return;
                }
                Log.d(TAG, "4: count=" + count);
                if (count > 0){
                    getPictures();
                    for (int i=0; i<count; i++){
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(MapsActivity.jObject.getJSONObject(tab_text).getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                                        MapsActivity.jObject.getJSONObject(tab_text).getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng"))));

                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        private String GET(String url){
            HttpURLConnection connection;
            try{
                URL u = new URL(url);
                connection = (HttpURLConnection) u.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
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
                        return sb.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        private void getPictures() {
            try {
                String[] urls = new String[MapsActivity.jObject.getJSONObject(tab_text).getJSONArray("results").length()];
                for (int i=0; i<MapsActivity.jObject.getJSONObject(tab_text).getJSONArray("results").length(); i++){
                    StringBuilder sb = new StringBuilder();
                    sb.append("https://maps.googleapis.com/maps/api/place/photo?")
                            .append("photoreference=").append(MapsActivity.jObject.getJSONObject(tab_text).getJSONArray("results").getJSONObject(i).getJSONArray("photos").getJSONObject(0).getString("photo_reference")).append("&")
                            .append("key=").append(key).append("&")
                            .append("maxwidth=").append(list_item_image_width); //.append("&")
                            //.append("maxheight=").append(list_item_image_height);
                    urls[i] = sb.toString();
                    Log.d(TAG, "getPictures: " + urls[i]);
                }
                GetPictures gp = new GetPictures(MapsActivity.jObject.getJSONObject(tab_text).getJSONArray("results").length(), tab_text, activityReference);
                gp.execute(urls);
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
        private static class GetPictures extends AsyncTask<String, Void, Bitmap[]>{
            int count = -1;
            String tab_text;
            WeakReference<MapsActivity> activityReference;
            GetPictures(int count, String tab_text, WeakReference<MapsActivity> activityReference){
                this.count = count;
                this.tab_text = tab_text;
                this.activityReference = activityReference;
            }
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            protected Bitmap[] doInBackground(String... urls) {
                Bitmap[] bitmaps = new Bitmap[urls.length];
                for (int i=0; i<urls.length; i++){
                    bitmaps[i] = getBitmapFromURL(urls[i]);
                }
                return bitmaps;
            }
            @SuppressLint("ClickableViewAccessibility")
            @Override
            protected void onPostExecute(Bitmap[] bitmaps){
                final MapsActivity activity = activityReference.get();
                activity.bitmaps.put(tab_text, bitmaps);
                // Initailize Adapter and ListView
                Log.d(TAG, "5");
            }
            private void getDistances(){

            }
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
    }
    private void setTabList(@Nullable String TAB){
        String[] tab = null;
        if (TAB != null) {
            switch (TAB){
                case "美食":
                    tab = new String[]{"restaurant", "cafe", "bar"};
                    break;
                case "購物":
                    tab = new String[]{"shopping_mall", "store", "supermarket"};
                    break;
                case "戀愛":
                    tab = new String[]{"cafe", "movie_theater"};
                    break;
                case "旅遊":
                    tab = new String[]{"amusement_park", "tourist_attraction"};
                    break;
                case "休閒":
                    tab = new String[]{"movie_theater", ""};
                    break;
                default:
                    tab = null;
            }
        }
        if (tab == null || tab.length == 0){
            String[] a = new String[]{"餐廳", "咖啡廳", "酒吧"};
            String[] b = new String[]{"restaurant", "cafe", "bar"};
            Collections.addAll(tab_ch, a);
            Collections.addAll(tab_type, b);
        }else{
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("restaurant", "餐廳")
                        .put("cafe", "咖啡廳")
                        .put("bar", "酒吧")
                        .put("shopping_mall", "購物中心")
                        .put("store", "商店")
                        .put("supermarket", "超市")
                        .put("cafe", "咖啡廳")
                        .put("movie_theater", "電影院")
                        .put("amusement_park", "遊樂園")
                        .put("tourist_attraction", "旅遊景點");

                String[] a = new String[tab.length];
                for (int i=0; i<tab.length; i++){
                    a[i] = jsonObject.getString(tab[i]);
                }
                Collections.addAll(tab_ch, a);
                Collections.addAll(tab_type, tab);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
