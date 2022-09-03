package com.example.nazanin.finalproject.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.nazanin.finalproject.AppDatabase;
import com.example.nazanin.finalproject.R;
import com.example.nazanin.finalproject.model.DTO.GSM;
import com.example.nazanin.finalproject.model.DTO.Lte;
import com.example.nazanin.finalproject.model.DTO.UMTS;
import com.example.nazanin.finalproject.model.DTO.cellIdentity;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MainActivity extends AppCompatActivity  implements OnMapReadyCallback {

    private Button startBtn,upBtn,downBtn;
    private LinearLayout infolayout,cellInfolayout;
    private TextView powerTxt,plmnTxt,cellIdTxt,visibleTxt,httpresTxt,timeTxt;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);

    //DTO obj
    GSM gsm = new GSM();
    Lte lte = new Lte();
    UMTS umts = new UMTS();
    cellIdentity cellidentity = new cellIdentity();

    public static int PERMISSION_READ_STATE = 1;
    private static int PERMISSION_COURSE_STATE = 2;
    private static int PERMISSION_FINE_STATE = 3;
    private int counter,coverageColor,compareCoverage;
    private TelephonyManager tm;
    public static final int GROUP_PERMISSION = 1;
    private int coverage;
    private String time;
    private ArrayList<String> permissionsNeeded = new ArrayList<>();
    private ArrayList<String> permissionsAvailable = new ArrayList<>();
    public List<Point> routeCoordinates = new ArrayList<>();
    private AppDatabase myDatabase;
    private long startTime;
    int cellId;
    String gen;
    int power;
    String PLMN;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        myDatabase = Room.databaseBuilder(this, AppDatabase.class, AppDatabase.NAME).fallbackToDestructiveMigration().build();
        setContentView(R.layout.activity_main);
        infolayout = findViewById(R.id.infolayer);
        cellInfolayout = findViewById(R.id.celllayer);
        startBtn = findViewById(R.id.navBtn);
        upBtn = findViewById(R.id.ulink);
        downBtn = findViewById(R.id.dlink);
        httpresTxt = findViewById(R.id.httpres);
        timeTxt = findViewById(R.id.timeTxt);
        plmnTxt = findViewById(R.id.plmnTxt);
        powerTxt = findViewById(R.id.powerTxt);
        cellIdTxt = findViewById(R.id.cellIdTxt);
        visibleTxt = findViewById(R.id.visibleCellTxt);
        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        permissionsAvailable.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsAvailable.add(Manifest.permission.READ_PHONE_STATE);
        permissionsAvailable.add(Manifest.permission.ACCESS_FINE_LOCATION);
        for (String permission : permissionsAvailable){
            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(permission);
            }
        }
        //permissions
        if(permissionsNeeded.size()>0){
            RequestPermission(permissionsNeeded);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_STATE);
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COURSE_STATE);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_STATE);
            }

            else {

            }
        }

    }

    private void RequestPermission(ArrayList<String> permissions){
        String[] permissionList = new String[permissions.size()];
        permissions.toArray(permissionList);
        ActivityCompat.requestPermissions(this,permissionList,GROUP_PERMISSION);
    }

    private String getCurrentTime(){
        String date = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            date = df.format(Calendar.getInstance().getTime());
        }
        return date;
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.DARK,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull final Style style) {
                        enableLocationComponent(style);

                        coverageColor = decideLteColor(coverage);
                        // we add the line here
                        LineString lineString = LineString.fromLngLats(routeCoordinates);

                        FeatureCollection featureCollection = FeatureCollection.fromFeature(Feature.fromGeometry(lineString));

                        style.addSource(new GeoJsonSource("source"+counter, featureCollection,
                                new GeoJsonOptions().withLineMetrics(true)));

                        style.addLayer(new LineLayer("linelayer"+counter, "source"+counter).withProperties(
                                lineCap(Property.LINE_CAP_ROUND),
                                lineJoin(Property.LINE_JOIN_ROUND),
                                lineColor(coverageColor),
                                lineWidth(14f)
                        ));
                    }
                });

    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initializeLocationEngine();
        } else {

        }
    }


    @SuppressLint("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startScanning(View view) {

        infolayout.setVisibility(View.GONE);
        startBtn.setVisibility(View.GONE);
        cellInfolayout.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.VISIBLE);
        mapView.getMapAsync(this);

        startScan();

    }
    private class GetHttpResponseTime extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            URL url = null;
            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try {
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String content = "", line;
            return content;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            httpresTxt.setText("http response time: "+elapsedTime+" milliseconds");
          //  Toast.makeText(getApplicationContext(),"res time:  "+elapsedTime,Toast.LENGTH_SHORT).show();

        }
    }

    private class MainActivityLocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;


        MainActivityLocationCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            MainActivity activity = activityWeakReference.get();
            ConnectivityManager connectivityManager = (ConnectivityManager)activity.getSystemService(CONNECTIVITY_SERVICE);
            NetworkCapabilities nc = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                //   }
                int downSpeed = nc.getLinkDownstreamBandwidthKbps();
                double dbtnd = (((double)downSpeed/8)/1000)/8;
                int upSpeed = nc.getLinkUpstreamBandwidthKbps();
                double ubtnd = ((((double)upSpeed/8)/1000)/8);
                downBtn.setText(dbtnd+" Mb/s");
                upBtn.setText(ubtnd+" Mb/s");
                time = getCurrentTime();
//                Toast.makeText(activity,"time:"+time+"     mobile downSpeed :" + (((double)downSpeed/8)/1000)/8 + " Mb/s  and upSpeed : " + ((((double)upSpeed/8)/1000)/8) +" Mb/s",
//                        Toast.LENGTH_SHORT).show();
            }

            startTime = System.currentTimeMillis();
            new GetHttpResponseTime().execute("https://www.google.com/");
//            String latency = getLatency();
//            Toast.makeText(activity,latency,
//                        Toast.LENGTH_SHORT).show();

            if (activity != null) {
                final Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // we add new coordinates to our list and update the line we created at first
                routeCoordinates.add(Point.fromLngLat(location.getLongitude(), location.getLatitude()));

                if(cellidentity.getGeneration()!= null){
                    switch(cellidentity.getGeneration()){
                        case "gsm": gsm.setLat(location.getLatitude());gsm.setLon(location.getLongitude());
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    // Insert Data
                                    myDatabase.gsmDao().insert(gsm);
                                    //get data
                                    // List<GSM> mylist = myDatabase.gsmDao().getAll();
                                }
                            });
                            break;
                        case "umts": umts.setLat(location.getLatitude());umts.setLon(location.getLongitude());
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    // Insert Data
                                    myDatabase.umtsDao().insert(umts);
                                    //get data
                                    // List<UMTS> mylist = myDatabase.umtsDao().getAll();
                                }
                            });
                            break;
                        case "lte": lte.setLat(location.getLatitude());lte.setLon(location.getLongitude());
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    // Insert Data
                                    myDatabase.lteDao().insert(lte);
                                    //get data
                                    //  List<Lte> mylist = myDatabase.lteDao().getAll();

                                }
                            });
                            break;
                        default:
                    }
                }
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        if (coverageHasChanged(coverage)){
                            routeCoordinates.add(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
                            createLineColorSegment(style,coverage,routeCoordinates);
                        }
                        else {
                            drawTheSameColor(style,counter,routeCoordinates);
                        }

                    }
                });

                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }




        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startScan() {


        tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        //get data every 5 seconds
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                @SuppressLint("MissingPermission") List<CellInfo> cellInfoList = tm.getAllCellInfo();
                for (CellInfo info : cellInfoList) {
                    if (info instanceof CellInfoGsm) {
                        gen = "2G";
                        if (info.isRegistered()) {
                            final CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                            cellId = identityGsm.getCid();
                            int LAC = identityGsm.getLac();
                            PLMN = String.valueOf(identityGsm.getMcc()) + String.valueOf(identityGsm.getMnc()); //plmn = mcc+mnc
                            cellidentity.setGeneration("gsm");
                            cellidentity.setPlmn(PLMN);
                            cellidentity.setCell_id(cellId);
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    // Insert Data
                                    myDatabase.cellIdentityDao().insert(cellidentity);
                                }
                            });
                            gsm.setLac(LAC);
                        }
                        giveGSMPowerParameters(info);
                    }

                    // get lte data
                    else if (info instanceof CellInfoLte) {

                        gen = "4G";
                        //serving cell information
                        if (info.isRegistered()) {
                            final CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                            cellId = identityLte.getCi(); //cell identity
                            int TAC = identityLte.getTac(); //tracking area code
                            PLMN = String.valueOf(identityLte.getMcc()) + String.valueOf(identityLte.getMnc()); //plmn = mcc+mnc
//                    Toast.makeText(context, TAC+" tac"+PLMN+"  plmn",
//                            Toast.LENGTH_SHORT).show();
                            cellidentity.setGeneration("lte");
                            cellidentity.setPlmn(PLMN);
                            cellidentity.setCell_id(cellId);
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    // Insert Data
                                    myDatabase.cellIdentityDao().insert(cellidentity);
                                }
                            });
                            lte.setTac(TAC);
                        }

                        giveLtePowerParameters(info);


                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                            && info instanceof CellInfoWcdma) {
                        gen = "3G";
                        if (info.isRegistered()) {
                            final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();
                            cellId = identityWcdma.getCid();
                            int LAC = identityWcdma.getLac();
                            PLMN = String.valueOf(identityWcdma.getMcc()) + String.valueOf(identityWcdma.getMnc());
                            cellidentity.setGeneration("umts");
                            cellidentity.setPlmn(PLMN);
                            cellidentity.setCell_id(cellId);
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    // Insert Data
                                    myDatabase.cellIdentityDao().insert(cellidentity);
                                }
                            });
                            umts.setLac(LAC);
                        }
                        giveUMTSPowerParameters(info);

                    } else {


                    }
                }
                timeTxt.setText("At time: "+time);
                visibleTxt.setText("Visible Cells: "+cellInfoList.size());
                cellIdTxt.setText("Cell Id: "+cellId);
                powerTxt.setText(power+"dBm  "+gen );
                plmnTxt.setText("PLMN: "+PLMN);

//                }
//                else {
//                  //  ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_PHONE_STATE}, MainActivity.PERMISSION_READ_STATE);
//                }

                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(runnable, 5000);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void giveLtePowerParameters(final CellInfo info){

        CellSignalStrengthLte lteInfo = ((CellInfoLte) info).getCellSignalStrength();
        lte.setRsrp(lteInfo.getRsrp());  //RSRP
        lte.setRsrq(lteInfo.getRsrq());  //RSRQ
        lte.setSinr(lteInfo.getRssnr()); //SINR
//        if (Build.VERSION.SDK_INT >= 29) {
//            lte.setRssi(lteInfo.getRssi()); //RSSI
//        }

        coverage = (lte.getRsrp()+lte.getRsrq()+lte.getSinr())/3;
        power = lte.getRsrp();
        //  MainActivity.coverage = coverage;


//        Toast.makeText(this, "rsrp"+lte.getRsrp()+"  "+lte.getRsrq()+"  "+lte.getSinr()
//                        +"ave  "+coverage,
//                Toast.LENGTH_SHORT).show();

    }

    private void giveGSMPowerParameters(final CellInfo info){
        CellSignalStrengthGsm gsmInfo = ((CellInfoGsm) info).getCellSignalStrength();
        gsm.setRvlex(gsmInfo.getDbm()); // rxlev

        power = gsm.getRvlex();
        coverage = power;
//        Toast.makeText(this, "rsrp"+gsmInfo.getDbm()+"  "+gsmInfo.getLevel()+"  "+
//                        "   "+gsmInfo.getAsuLevel(),
//                Toast.LENGTH_SHORT).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void giveUMTSPowerParameters(final CellInfo info){
        CellSignalStrengthWcdma umtsInfo = ((CellInfoWcdma) info).getCellSignalStrength();
        umts.setRscp(umtsInfo.getDbm()); //RSCP
        power = umts.getRscp();
//        if (Build.VERSION.SDK_INT >= 30) {
//            umtsInfo.getEcNo(); //EC/N0
//        }

        Toast.makeText(this, "rscp"+power,
                Toast.LENGTH_SHORT).show();
    }
    public void drawTheSameColor(Style style,int id,List<Point> coords){
        GeoJsonSource source = style.getSourceAs("source"+id);

        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(coords)));
            style.getLayerAs("linelayer"+id).setProperties(
                    lineColor(coverageColor),
                    lineCap(Property.LINE_CAP_ROUND),
                    lineJoin(Property.LINE_JOIN_ROUND),
                    lineColor(coverageColor),
                    lineWidth(14f)
            );
        }
    }

    public void createLineColorSegment(Style style,int coverage,List<Point> coords){
        counter++;
        switch (gen){
            case "2G":
                coverageColor = decideGsmColor(coverage);
                break;
            case "3G":
                coverageColor = decideUmtsColor(coverage);
                break;
            case "4G":
                coverageColor = decideLteColor(coverage);
                break;
        }
        //coverageColor = decideColor(coverage);
        LineString lineString = LineString.fromLngLats(coords);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(Feature.fromGeometry(
                lineString));
        style.addSource(new GeoJsonSource("source"+String.valueOf(counter), featureCollection,
                new GeoJsonOptions().withLineMetrics(true)));
        style.addLayer(new LineLayer("linelayer"+String.valueOf(counter),
                "source"+String.valueOf(counter)).withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineColor(coverageColor),
                lineWidth(14f)
        ));
    }

    public boolean coverageHasChanged(int coverage){
        boolean coverageChanged = false;

        // Toast.makeText(getApplicationContext(), compareCoverage+"   has  "+coverage, Toast.LENGTH_SHORT).show();
        if (Math.abs(compareCoverage-coverage)>3){
            coverageChanged = true;
            routeCoordinates.clear();
            compareCoverage = coverage;
        }

        return coverageChanged;
    }
    public int decideLteColor(int coverage){

        int color = Color.DKGRAY;
        if(coverage<=-29 && coverage>=-32) {
            color = Color.rgb(103, 235, 52);
        }
        else if (coverage<=-33 && coverage>=-35) {
            color = Color.rgb(255, 255, 102);
        }
        else if (coverage<=-36 && coverage>=-40) {
            color = Color.rgb(255, 153, 0);

        }
        else if (coverage>=-31){
            color = Color.rgb(51, 204, 51);
        }
        else if (coverage<=-41){
            color = Color.rgb(230, 48, 48);
        }
        return color;
    }
    public int decideUmtsColor(int coverage){

        int color = Color.DKGRAY;
        if(coverage<=-60 && coverage>=-75) {
            color = Color.rgb(103, 235, 52);
        }
        else if (coverage<=-76 && coverage>=-85) {
            color = Color.rgb(255, 255, 102);
        }
        else if (coverage<=-86 && coverage>=-95) {
            color = Color.rgb(255, 153, 0);

        }
        else if (coverage>=-60 && coverage<=0){
            color = Color.rgb(51, 204, 51);
        }
        else if (coverage<=-96 && coverage>=-124){
            color = Color.rgb(230, 48, 48);
        }
        return color;
    }
    public int decideGsmColor(int coverage){

        int color = Color.DKGRAY;
        if(coverage<=-65 && coverage>=-75) {
            color = Color.rgb(103, 235, 52);
        }
        else if (coverage<=-75 && coverage>=-95) {
            color = Color.rgb(255, 255, 102);
        }
        else if (coverage<=-96 && coverage>=-105) {
            color = Color.rgb(255, 153, 0);

        }
        else if (coverage<=-10 && coverage>=-66){
            color = Color.rgb(51, 204, 51);
        }
        else if (coverage<=-105){
            color = Color.rgb(230, 48, 48);
        }
        return color;
    }

    // we override life cycles here because map and android both have their life cycles
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    // we should end location updates and mapview to avoid memory leak
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}

