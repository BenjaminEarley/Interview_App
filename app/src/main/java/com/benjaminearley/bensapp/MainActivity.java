package com.benjaminearley.bensapp;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.net.URI;
import java.util.HashMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.daimajia.slider.library.SliderLayout;

public class MainActivity extends ActionBarActivity
        implements LocationListener, BaseSliderView.OnSliderClickListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    HashMap<String,Integer> file_maps = new HashMap<>();
    private GoogleMap myMap;
    LocationManager locationManager;
    private SliderLayout mDemoSlider;
    private double latitude = 0D;
    private double longitude = 0D;

    int images[]= {R.drawable.a, R.drawable.b, R.drawable.c,
            R.drawable.d, R.drawable.e, R.drawable.f, R.drawable.g,
            R.drawable.h, R.drawable.z};

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
        mDemoSlider = (SliderLayout)findViewById(R.id.slider);



        if (checkPlayServices()) {
            FragmentManager myFragmentManager = getFragmentManager();
            MapFragment myMapFragment
                    = (MapFragment) myFragmentManager.findFragmentById(R.id.map);
            myMap = myMapFragment.getMap();
            myMap.setMyLocationEnabled(true);

            UiSettings settings = myMap.getUiSettings();
            settings.setAllGesturesEnabled(false);
            settings.setTiltGesturesEnabled(true);
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

        moveCamera(location);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);

        file_maps.put("image a", R.drawable.a);
        file_maps.put("image b", R.drawable.b);
        file_maps.put("image c", R.drawable.c);
        file_maps.put("image d", R.drawable.d);
        file_maps.put("image e", R.drawable.e);
        file_maps.put("image f", R.drawable.f);
        file_maps.put("image g", R.drawable.g);
        file_maps.put("image h", R.drawable.h);
        file_maps.put("image z", R.drawable.z);


        for(String name : file_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);


            //add your extra information
            textSliderView.getBundle()
                    .putInt("extra", file_maps.get(name));


            mDemoSlider.addSlider(textSliderView);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDemoSlider.removeAllSliders();
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

        moveCamera(location);

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

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {

        final Dialog d = new Dialog(this,R.style.CustomDialogTheme);
        d.setContentView(R.layout.custom_dialog);
        d.show();

        ImageView image = (ImageView) d.findViewById(R.id.imageView);
        image.setImageDrawable(getResources().getDrawable(baseSliderView.getBundle().getInt("extra")));

        ImageButton close_btn = (ImageButton) d.findViewById(R.id.close_button);
        close_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                d.dismiss();
            }
        });

    }

    public void moveCamera(Location location) {
        if (checkPlayServices()) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(location.getLatitude(),location.getLongitude())).zoom(17).build();

            myMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
}
