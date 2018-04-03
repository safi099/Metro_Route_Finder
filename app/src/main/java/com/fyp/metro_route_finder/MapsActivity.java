package com.fyp.metro_route_finder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fyp.metro_route_finder.Model.Station;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karan.churi.PermissionManager.PermissionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

@SuppressWarnings("deprecation")
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    ArrayList<Station> stations_array;
    LatLng latLng;
    SupportMapFragment mFragment;
    Marker currLocationMarker;
    private double longitude;
    private double latitude;
    private PermissionManager permission;
    BitmapDescriptor person_icon;
    private String TAG = "check";
    private String station_lat = null;
    private String station_lon = null;
    private BitmapDescriptor bus_icon;
    private LatLng mCenterLatLong;
    private String station_id;
    private String station_name;
    private String statio_lat;
    private String station_distance;
    private String statio_lon;

    TextView sname;
    TextView sdistance;
    Button navigate_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        buildGoogleApiClient();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sname = (TextView) findViewById(R.id.stationName);
        sdistance = (TextView) findViewById(R.id.stationdistance);
        navigate_btn = (Button) findViewById(R.id.navigate);
        person_icon = BitmapDescriptorFactory.fromResource(R.drawable.man);
        bus_icon = BitmapDescriptorFactory.fromResource(R.drawable.bus);
        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(MapsActivity.this);
        stations_array = new ArrayList<>();
        navigate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = statio_lat + "," + statio_lon;
                String url = "https://www.google.com/maps/dir/?api=1&destination=" + location + "&travelmode=driving";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
        getStation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mCenterLatLong = cameraPosition.target;

            }
        });
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (mCenterLatLong != null) {
                    getNearestStation(mCenterLatLong.latitude, mCenterLatLong.longitude);
                }
            }
        });
    }

    private void getNearestStation(final double latitude, final double longitude) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.NEAR_STATION_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Station station = new Station();
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                        station_id = jsonObject1.get("station_id").toString();
                        station_name = jsonObject1.get("station_name").toString();
                        statio_lat = jsonObject1.get("station_lat").toString();
                        station_distance = jsonObject1.get("distance").toString();
                        statio_lon = jsonObject1.get("station_lon").toString();
                        //stations_array.add(station);
                    }
                    Log.i(TAG, "Nearest Station = " + station_name);
                    sname.setText(station_name);
                    sdistance.setText(station_distance);
                    /*if (stations_array.size() > 0) {
                        plotMapStations();
                    } else {
                        Toast.makeText(MapsActivity.this, "No Stations Found", Toast.LENGTH_LONG).show();
                    }*/
                } catch (JSONException e) {
                    Log.i(TAG, "Error JSONException=" + e.toString());
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Erorr Response =" + error.toString());
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_latitude", String.valueOf(latitude));
                params.put("user_longitide", String.valueOf(longitude));
                return params;
            }
        };
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);

    }


    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected Method");
        getStation();
        getCurrentLocation();
    }

    private void moveMap() {
        mMap.clear();
        //String to display current latitude and longitude
        String msg = latitude + ", " + longitude;
        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);
        //Adding marker to map

        mMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(false) //Making the marker draggable
                .title("Your's Location")
                .icon(person_icon)
                .visible(true)); //Adding a title
        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f));
        //Displaying current coordinates in toast
        Log.i(TAG, "Mesaage= " + msg);
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    //Getting current location
    private void getCurrentLocation() {
        //Creating a location object
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            //moving the map to location
            moveMap();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("check", "OnConnected Susupended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("check", "OnConnected Failed");
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        getCurrentLocation();
        getStation();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        permission.checkResult(requestCode, permissions, grantResults);
        Toast.makeText(MapsActivity.this, "Reopen the Application to work properly", Toast.LENGTH_SHORT).show();
    }

    private void getStation() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.STATION_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    stations_array.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Station station = new Station();
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        station.setStation_id(jsonObject1.get("station_id").toString());
                        station.setStation_name(jsonObject1.get("station_name").toString());
                        station.setStation_lat(jsonObject1.get("station_lat").toString());
                        station.setStation_long(jsonObject1.get("station_lon").toString());
                        stations_array.add(station);
                    }
                    Log.i(TAG, "Stations Found= " + stations_array.size());
                    if (stations_array.size() > 0) {
                        plotMapStations();
                    } else {
                        Toast.makeText(MapsActivity.this, "No Stations Found", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.i(TAG, "Error JSONException=" + e.toString());
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Erorr Response =" + error.toString());
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);

    }

    private void plotMapStations() {
        for (int i = 0; i < stations_array.size(); i++) {
            station_lat = stations_array.get(i).getStation_lat();
            station_lon = stations_array.get(i).getStation_long();
            LatLng latLng = new LatLng(Double.parseDouble(station_lat), Double.parseDouble(station_lon));
            //Adding marker to map
            mMap.addMarker(new MarkerOptions()
                    .position(latLng) //setting position
                    .draggable(false) //Making the marker draggable
                    .title(stations_array.get(i).getStation_name())
                    .icon(bus_icon)
                    .visible(true));

        }
    }
}
