package com.benjaminearley.bensapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.daimajia.slider.library.SliderLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity
        implements LocationListener, BaseSliderView.OnSliderClickListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    HashMap<Integer, String> photo_maps = new HashMap<>();
    private GoogleMap myMap;
    LocationManager locationManager;
    private ProgressBar spinner;
    private SliderLayout mDemoSlider;
    private double latitude = 0D;
    private double longitude = 0D;
    private ArrayList<Photo> photos = new ArrayList<>();

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
                switch(item.getItemId())
                {
                    case R.id.action_refresh:
                        mDemoSlider.removeAllSliders();
                        photos.clear();
                        setUpSlideshow();

                }
                return false;
            }
        });

        toolbar.inflateMenu(R.menu.menu_main);
        mDemoSlider = (SliderLayout)findViewById(R.id.slider);
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);


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
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        if (isLocationEnabled(this)) {
            moveCamera(location);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);

        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Please Turn On Location Services and Restart App");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        setUpSlideshow();


    }

    @Override
    protected void onPause() {
        super.onPause();
        mDemoSlider.removeAllSliders();
        photos.clear();
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
        image.setImageBitmap(getBitmapFromURL(photo_maps.get(baseSliderView.getBundle().getInt("dialog"))));


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

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }

    private void getFlickrPhotoData() {

        String urlJsonObj = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=5cc8cfbfccfc519295cb0d86ef63ae77&sort=interestingness-desc&accuracy=16&content_type=1&media=photos&has_geo=1&radius=1&format=json&nojsoncallback=1&per_page=15";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj + "&lat=" + latitude + "&lon=" + longitude, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("JSON Request", response.toString());
                int _size;
                try {
                    // Parsing json object response

                    JSONObject JSONPhotos = response.getJSONObject("photos");
                    int numberOfPhotos = JSONPhotos.getInt("total");

                    if (numberOfPhotos > 9)
                        _size = 9;
                    else
                        _size = numberOfPhotos;

                    JSONArray JSONPhotoList = JSONPhotos.getJSONArray("photo");

                    for (int i = 0; i < _size; i++) {
                        Photo photo = new Photo();

                        JSONObject JSONPhoto = (JSONObject) JSONPhotoList.get(i);
                        photo.id = JSONPhoto.getString("id");
                        photo.owner = JSONPhoto.getString("owner");
                        photo.secret = JSONPhoto.getString("secret");
                        photo.server = JSONPhoto.getString("server");
                        photo.farm = JSONPhoto.getString("farm");
                        photos.add(photo);


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Volley Error", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }



    private void setFlickrSlideshowPhotos() {

        String imageURL;

        for (int i = 0; i < photos.size(); i++ ){

            imageURL = "https://farm" + photos.get(i).farm + ".staticflickr.com/" + photos.get(i).server + "/" + photos.get(i).id + "_" + photos.get(i).secret + "_z.jpg";

            photo_maps.put(i, imageURL);

            DefaultSliderView sliderView = new DefaultSliderView(this);

            sliderView
                    .image(imageURL)
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                    .setOnSliderClickListener(this);

            sliderView.getBundle()
                    .putInt("dialog", i);


            mDemoSlider.addSlider(sliderView);

        }


    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            Log.e("src",src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.e("Bitmap","returned");
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception",e.getMessage());
            return null;
        }
    }

    public void setUpSlideshow() {

        getFlickrPhotoData();
        spinner.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
                setFlickrSlideshowPhotos();
            }
        }, 5000);

    }

    public class Photo {
        public String id = "";
        public String owner = "";
        public String secret = "";
        public String server = "";
        public String farm = "";

    }

}
