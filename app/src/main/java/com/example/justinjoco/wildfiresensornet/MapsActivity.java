package com.example.justinjoco.wildfiresensornet;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private Location mLastKnownLocation;
    private static final float DEFAULT_ZOOM = (float)18;
    LatLng mDefaultLocation = new LatLng(-33.852, 151.211);
    private boolean isSafe = true;

    private static final String REQUESTTAG = "string request first";



    private static final String TAG = "MapsActivity";

    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    private String url = "http://sensorgurusandroid.mybluemix.net/api/v1/sensors";
    private JSONArray array = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);






    }

    private void sendRequestAndPrintResponse() {
        mRequestQueue = Volley.newRequestQueue(this);

        stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.i(TAG, "Response: "+ response.toString());

                try {
                   // Log.i(TAG, "Response: "+ response.toString());
                    JSONArray array = new JSONArray(response);
                   // Log.i(TAG, "Response: "+ array);
                    drawCircles(array);


                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.i(TAG, "Error: "+ error.toString());

            }

        });


        mRequestQueue.add(stringRequest);


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
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location)task.getResult();
                            LatLng mLatLng = new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM));
                            mMap.addMarker(new MarkerOptions().position(mLatLng));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.addMarker(new MarkerOptions().position(mDefaultLocation));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    public void addHeatData(LatLng latlng, boolean isSafe, int color){
        Circle circle;
        circle = mMap.addCircle(new CircleOptions()
                .center(latlng)
                .radius(25)
                .strokeWidth(10)

                .fillColor(color)
                .clickable(true));

    }




    public JSONArray makeFakePoints(){
        JSONArray JSONObjArray = new JSONArray();

        try {
            JSONObject obj1 = new JSONObject();
            obj1.put("latitude", "42.44439870376148");
            obj1.put("longitude", "-76.4846906089224");
            obj1.put("Safe", "false");
            obj1.put("color", "#e67e22");


            JSONObject obj2 = new JSONObject();
            obj2.put("latitude", "42.44389443134966");
            obj2.put("longitude", "-76.4838558342308");
            obj2.put("Safe", "false");
            obj2.put("color", "#c0392b");

            JSONObject obj3 = new JSONObject();
            obj3.put("latitude", "42.44395156830378");
            obj3.put("longitude", "-76.48260391317308");
            obj3.put("Safe", "false");
            obj3.put("color", "#e67e22");



            JSONObject obj5 = new JSONObject();
            obj5.put("latitude", "42.44456938631949");
            obj5.put("longitude", "-76.48346400121228");
            obj5.put("Safe", "false");
            obj5.put("color", "#c0392b");

            JSONObjArray.put(obj1);
            JSONObjArray.put(obj2);
            JSONObjArray.put(obj3);

            JSONObjArray.put(obj5);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return JSONObjArray;
    }


    public void drawCircles(JSONArray JSONObjectArray){


        try{



            //List <JSONObject> JSONObjectArray= getDataPoints(url);


            //JSONArray JSONObjectArray = makeFakePoints();

            // JSONArray JSONObjectArray = arr;
            for(int i = 0; i<JSONObjectArray.length(); i++) {
                //JSONObject obj = new JSONObject(JSONObjectArray.get(i).toString());
                JSONObject obj = (JSONObject)JSONObjectArray.get(i);

                double latitude = Double.parseDouble(obj.getString("latitude"));
                double longitude = Double.parseDouble(obj.getString("longtitude"));
                boolean isSafe = Boolean.parseBoolean(obj.getString("Safe"));
                int color = Color.parseColor(obj.getString("color"));
                LatLng latLng = new LatLng(latitude, longitude);
                addHeatData(latLng, isSafe, color);
            }



        }

        catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        sendRequestAndPrintResponse();

// Access the RequestQueue through your singleton class.




}
}
