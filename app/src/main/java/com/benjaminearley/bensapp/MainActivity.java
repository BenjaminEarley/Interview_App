package com.benjaminearley.bensapp;

import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends ActionBarActivity implements LocationListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private GoogleMap myMap;
    LocationManager locationManager;
    private double latitude = 0D;
    private double longitude = 0D;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                return true;
            }
        });

        toolbar.inflateMenu(R.menu.menu_main);


        if (checkPlayServices()) {
            FragmentManager myFragmentManager = getFragmentManager();
            MapFragment myMapFragment
                    = (MapFragment) myFragmentManager.findFragmentById(R.id.map);
            myMap = myMapFragment.getMap();
            myMap.setMyLocationEnabled(true);

            UiSettings settings = myMap.getUiSettings();
            settings.setAllGesturesEnabled(false);
            settings.setMyLocationButtonEnabled(false);
            settings.setZoomControlsEnabled(false);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (checkPlayServices()) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(location.getLatitude(),location.getLongitude())).zoom(16).build();

            myMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("Google Play Services", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        if (checkPlayServices()) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude,longitude)).zoom(16).build();

            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
