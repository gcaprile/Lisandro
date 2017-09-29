package com.project.myapplication.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.myapplication.R;
import com.project.myapplication.bus.BusProvider;
import com.project.myapplication.bus.NewLocationEvent;
import com.project.myapplication.db.DatabaseManager;
import com.project.myapplication.model.UserLocation;
import com.project.myapplication.service.LocationService;
import com.project.myapplication.util.PreferenceManager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{

    public static final int PERMISSION_LOCATION_REQUEST = 12;

    @BindView(R.id.startTrackerButton)
    Button mBtnStartTracker;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (checkAndRequestPermissions()) {
            startLocationService();
        }

        if(isLocationServiceEnabled()){
            mBtnStartTracker.setText(R.string.stop_tracker);
        }else{
            mBtnStartTracker.setText(R.string.start_tracker);
        }
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
        mMap = googleMap;
        showSavedLocationOnMap();
    }

    private  boolean checkAndRequestPermissions() {
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        //int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_LOCATION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService();

                } else {
                    Toast.makeText(MapsActivity.this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startLocationService(){
        if(isLocationServiceEnabled()){
            startService(new Intent(this, LocationService.class));
        }
    }

    private void stopLocationService(){
        stopService(new Intent(this, LocationService.class));
    }

    @OnClick(R.id.startTrackerButton)
    public void clickStartTracker(){
        if(isLocationServiceEnabled()){
            PreferenceManager.getInstance(this).setIsServiceEnabled(false);
            mBtnStartTracker.setText(getString(R.string.start_tracker));
            stopLocationService();
        }else{
            PreferenceManager.getInstance(this).setIsServiceEnabled(true);
            mBtnStartTracker.setText(getString(R.string.stop_tracker));
            startLocationService();
        }
    }

    private void showSavedLocationOnMap(){
        List<UserLocation> locations = DatabaseManager.getInstance().getUserLocationList();

        for(UserLocation location : locations){
            if (mMap != null) {
                mMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            }
        }
    }

    private boolean isLocationServiceEnabled(){
        return PreferenceManager.getInstance(this).isServiceEnabled();
    }

    @OnClick(R.id.viewHistoryButton)
    public void openHistoryActivity(){
        startActivity(new Intent(MapsActivity.this, HistoryActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void getNewLocation(NewLocationEvent newLocationEvent){
        Log.d("LOCATION LAT:" , String.valueOf(newLocationEvent.getLat()));
        Log.d("LOCATION LON:" , String.valueOf(newLocationEvent.getLon()));

        if (mMap != null) {
            mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(newLocationEvent.getLat(), newLocationEvent.getLon()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(newLocationEvent.getLat(), newLocationEvent.getLon()), 17));
        }
    }
}
