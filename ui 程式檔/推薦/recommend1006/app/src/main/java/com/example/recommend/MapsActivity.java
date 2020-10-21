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
import android.content.res.Resources;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.util.Attributes;
import com.example.recommend.adpter.util.ListViewAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


import com.google.android.gms.maps.model.MapStyleOptions;
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
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import com.daimajia.swipe.SwipeLayout;
import com.google.maps.android.ui.IconGenerator;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static android.widget.ImageView.ScaleType.FIT_CENTER;
import static android.widget.ImageView.ScaleType.FIT_END;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{
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
    private ArrayList<Marker> markerList;
    private Object lastClickedMarker;
    private GoogleAPIResponse googleAPIResponse;

    private DisplayMetrics dm = new DisplayMetrics();
    private static boolean click_default;
    public MapsActivity() {
    }

    // ---------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // get the bottom sheet view
        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
        // set bottom sheet height in percentlBottomSheet.getLayoutParams();
        llBottomSheet.getLayoutParams().height = dm.heightPixels*7/9;
//        params.height = dm.heightPixels*7/9;
//        llBottomSheet.setLayoutParams(params);
//        ViewGroup.LayoutParams params = l

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

//        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
//        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        setTabList("美食");
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        for (String s : tab_ch) tabLayout.addTab(tabLayout.newTab().setText(s));
        tabLayout.setTabTextColors(getResources().getColor(R.color.colorTabText),getResources().getColor(R.color.colorTabTextSelected));
        // set Stepping_Stones
        TextView maps_stepping_stone = findViewById(R.id.map_stepping_stones);
        maps_stepping_stone.setHeight(tabLayout.getHeight());
        // set Tab Seleceted Listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
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
                        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
                        mAdapter.setMode(Attributes.Mode.Single);

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                //((SwipeLayout)(mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
                                map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(markerList.get(position).getPosition().latitude,markerList.get(position).getPosition().longitude), map.getCameraPosition().zoom,0f, 0f)));
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                        addMarker();
                        //addMarker_v2();
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
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.hiding_map_features));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        this.map = googleMap;
        ui = map.getUiSettings();
        ui.setZoomControlsEnabled(false);

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (lastClickedMarker != null && lastClickedMarker instanceof Integer)
                    markerList.get((int)lastClickedMarker).setZIndex(1);
                marker.setZIndex(2);
                lastClickedMarker = marker.getTag();

                float bias=0;
                boolean ret = true, isbottomed = false;
                if (lastClickedMarker != null && lastClickedMarker instanceof Integer){
                    switch(googleAPIResponse.getItemsCount()-(int)lastClickedMarker){
                        case 1:
                            isbottomed = true;
                            bottomSheetBehavior.setHalfExpandedRatio(1f);
                            break;
                        case 2:
                            isbottomed = true;
                            bias = (float)dm.heightPixels/3;
                            bottomSheetBehavior.setHalfExpandedRatio(0.8f);
                            break;
                        case 3:
                            isbottomed = true;
                            bias = (float)dm.heightPixels/6;
                            bottomSheetBehavior.setHalfExpandedRatio(0.6f);
                            break;
                        case 4:
                            isbottomed = true;
                            bias = (float)dm.heightPixels/10;
                            bottomSheetBehavior.setHalfExpandedRatio(0.4f);
                            break;
                        default:
                            ret = false;
                            bottomSheetBehavior.setHalfExpandedRatio(0.365f);
                            break;
                    }
                    mListView.smoothScrollToPositionFromTop((int)lastClickedMarker, 0, 200);
                    if (isbottomed && bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_COLLAPSED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    if (bottomSheetBehavior.getHalfExpandedRatio()!=0.365f || bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_HALF_EXPANDED)
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }
                if (ret){
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(marker.getPosition().latitude,marker.getPosition().longitude),DEFAULT_ZOOM,0f, 0f)));
                    map.moveCamera(CameraUpdateFactory.scrollBy(0, bias));
                }
                return ret;
            }
        });
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addMarker_v2(){
        markerList = new ArrayList<>();
        for (int i=0; i<googleAPIResponse.getItemsCount(); i++){
            LatLng latLng = new LatLng(googleAPIResponse.getLat(i), googleAPIResponse.getLng(i));

            TextView text = new TextView(getApplicationContext());
            text.setText(googleAPIResponse.getPlaceName(i));
            text.setTextAppearance(R.style.PlaceNameInfoOverlayText);
            text.setMaxWidth(500);
            IconGenerator generator = new IconGenerator(getApplicationContext());
            generator.setContentView(text);
            generator.setStyle(IconGenerator.STYLE_DEFAULT);
            generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
            Bitmap textIcon = generator.makeIcon();

            Bitmap photo = ImageHelper.getBitmap(getApplicationContext(), R.drawable.ic_pin);
            int photoWidth = photo.getWidth(), photoHeight= photo.getHeight()
                    , textWidth = textIcon.getWidth(), textHeight = textIcon.getHeight();
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            int max_height = Math.max(textHeight, photoHeight);
            Bitmap bmp = Bitmap.createBitmap(textWidth+photoWidth+3, max_height, conf);

            Canvas canvas = new Canvas(bmp);

            // paint defines the text color, stroke width and size
            Paint color = new Paint();
            color.setTextSize(36);
            color.setColor(Color.RED);
            int k = 1, photo_bias = max_height/k;
            while(max_height - photo_bias < photoHeight){
                k++;
                photo_bias = max_height/k;
                //Log.d(TAG, "addMarker_v2: "+k);
            }
            canvas.drawBitmap(photo, 0, (float)(photo_bias), color);
            canvas.drawBitmap(textIcon, (float)photoWidth+2, 0, color);

            markerList.add(map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    .anchor(0.06f,0.95f)));
            markerList.get(markerList.size()-1).setTag(i);
        }
    }
    private int dp2px(float dip){
        Resources r = getResources();
        return (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }
    private void addMarker(){
        markerList = new ArrayList<>();
        for (int i=0; i<googleAPIResponse.getItemsCount(); i++){
//            Marker marker = map.addMarker(new MarkerOptions()
//                    .position(new LatLng(googleAPIResponse.getLat(i),googleAPIResponse.getLng(i)))
//                    .title(googleAPIResponse.getPlaceName(i)));
//            marker.setTag(i);
            LatLng latLng = new LatLng(googleAPIResponse.getLat(i),googleAPIResponse.getLng(i));
            TextView text = new TextView(getApplicationContext());
            text.setText(googleAPIResponse.getPlaceName(i));
            text.setTextSize(16);
            text.setTextColor(getResources().getColor(R.color.colorTabText));
            //text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 9));
            ImageView imageView_tap = new ImageView(getApplicationContext());
            imageView_tap.setImageResource(R.mipmap.btn_tap_foreground);
            imageView_tap.setScaleX(1.65f);
            imageView_tap.setScaleY(1.65f);
            //imageView_tap.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setWeightSum(20);
            linearLayout.setPadding(dp2px(2), dp2px(1), dp2px(2), dp2px(1));
//            TextView fooText = new TextView(getApplicationContext());
//            fooText.setText(googleAPIResponse.getPlaceName(i));
//            fooText.setTextSize(18);
//            IconGenerator ig = new IconGenerator(getApplicationContext());
//            ig.setStyle(IconGenerator.STYLE_DEFAULT);
//            ig.setContentView(fooText);
//            Bitmap b = ig.makeIcon();
            linearLayout.addView(imageView_tap, new LinearLayout.LayoutParams(dp2px(25), LinearLayout.LayoutParams.MATCH_PARENT, 1));
            linearLayout.addView(text, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 19));
            IconGenerator generator = new IconGenerator(getApplicationContext());
//            generator.setBackground(getApplicationContext().getDrawable(R.drawable.pin));
            generator.setContentView(linearLayout);
            Bitmap textIcon = generator.makeIcon();

            //Bitmap photo = BitmapFactory.decodeResource(getApplication().getResources(),R.drawable.pin);
            Bitmap pin = ImageHelper.getBitmap(getApplicationContext(), R.drawable.ic_pin);
            int pinWidth = pin.getWidth(), pinHeight= pin.getHeight()
                    , textWidth = textIcon.getWidth(), textHeight = textIcon.getHeight();
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(textWidth, textHeight+pinHeight+3, conf);

            Canvas canvas = new Canvas(bmp);

            // paint defines the text color, stroke width and size
            Paint color = new Paint();
            //color.setTextSize(36);
            color.setColor(Color.RED);

//            Rect src = new Rect(0, 0, photo.getWidth(), photo.getHeight());//创建一个指定的新矩形的坐标
//            Rect dst = new Rect(0, 0, width, height);//创建一个指定的新矩形的坐标
//            canvas.drawBitmap(photo, src, dst, color);//将photo 缩放或则扩大到 dst使用的填充区photoPaint
            //canvas.drawText(googleAPIResponse.getPlaceName(i), 0, 36, color);//绘制上去字，开始未知x,y采用那只笔绘制
            canvas.drawBitmap(textIcon, 0, 0, color);
            canvas.drawBitmap(pin, (float)((textWidth/3)), (float)textHeight+2, color);
            // modify canvas
//            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
//                    R.drawable.pin), 0,0, color);
//            canvas.drawText(googleAPIResponse.getPlaceName(i), 30, 40, color);
            markerList.add(map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    .anchor(0.41f,1f)));
            markerList.get(markerList.size()-1).setTag(i);
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
