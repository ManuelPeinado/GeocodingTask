package com.manuelpeinado.geocodingtask.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.manuelpeinado.geocodingtask.DefaultAddressFormatter;
import com.manuelpeinado.geocodingtask.service.GeocodingService;
import com.manuelpeinado.geocodingtask.service.ReverseGeocodingService;

public class ReverseGeocodingServiceActivity extends SherlockFragmentActivity implements OnMapClickListener,
        OnMyLocationChangeListener {

    private GoogleMap mMap;
    private Marker mMarker;
    private boolean mFirstFixReceived;
    private Double mLastQueryLat;
    private double mLastQueryLng;
    private LocalBroadcastManager mLocalBroadcastManager;
    private ReverseGeocodingReceiver mGeocodingReceiver = new ReverseGeocodingReceiver();

    private class ReverseGeocodingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double queryLat = intent.getDoubleExtra(ReverseGeocodingService.PARAM_IN_LAT, 0);
            double queryLng = intent.getDoubleExtra(ReverseGeocodingService.PARAM_IN_LNG, 0);
            if (mLastQueryLat == null || queryLat != mLastQueryLat || queryLng != mLastQueryLng) {
                return;
            }
            boolean success = intent.getBooleanExtra(ReverseGeocodingService.PARAM_OUT_SUCCESS, false);
            if (success) {
                Address result = intent.getParcelableExtra(ReverseGeocodingService.PARAM_OUT_ADDRESS);
                mMarker.setTitle(DefaultAddressFormatter.format(result, ", "));
                String fmt = "Lat: %s, lon: %s";
                String snippet = String.format(fmt, result.getLatitude(), result.getLongitude());
                mMarker.setSnippet(snippet);
                mMarker.showInfoWindow();
            } else {
                Toast.makeText(getApplicationContext(), R.string.geocoding_error_toast, Toast.LENGTH_LONG).show();
                mMarker.remove();
                mMarker = null;
            }
            cleanUpAfterGeocoding();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        setContentView(R.layout.activity_reverse_geocoding);
        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        if (!ensureMapIsReady()) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(this);
        mMap.setOnMapClickListener(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("markerPosition")) {
                LatLng markerPos = savedInstanceState.getParcelable("markerPosition");
                moveMarkerTo(markerPos);
                mFirstFixReceived = true;
                String title = savedInstanceState.getString("infoWindowTitle");
                if (title != null) {
                    mMarker.setTitle(title);
                    mMarker.setSnippet(savedInstanceState.getString("infoWindowSnippet"));
                    if (savedInstanceState.getBoolean("isInfoWindowShown", false)) {
                        mMarker.showInfoWindow();
                    }
                }
            }
            if (savedInstanceState.containsKey("lastQueryLat")) {
                mLastQueryLat = savedInstanceState.getDouble("lastQueryLat");
                mLastQueryLng = savedInstanceState.getDouble("lastQueryLng");
                setProgressBarIndeterminateVisibility(true);
            }
        }
    }

    private boolean ensureMapIsReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.could_not_load_map, Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
    }

    @Override
    public void onMyLocationChange(Location newLocation) {
        if (!mFirstFixReceived) {
            mFirstFixReceived = true;
            LatLng latLng = newLatLng(newLocation);
            centerMapAt(latLng);
        }
    }

    private LatLng newLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onMapClick(LatLng point) {
        moveMarkerTo(point);
        centerMapAt(point);
        mLastQueryLat = point.latitude;
        mLastQueryLng = point.longitude;
        startGeocodingService();
        setProgressBarIndeterminateVisibility(true);
    }

    private void moveMarkerTo(LatLng position) {
        if (mMarker != null) {
            mMarker.remove();
        }
        mMarker = mMap.addMarker(new MarkerOptions().position(position));
        mMarker.setPosition(position);
    }

    private void startGeocodingService() {
        ReverseGeocodingService.IntentBuilder intentBuilder = new ReverseGeocodingService.IntentBuilder();
        intentBuilder.setLatLng(mLastQueryLat, mLastQueryLng);
        Intent intent = intentBuilder.build(this);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ReverseGeocodingService.RESPONSE_ACTION);
        mLocalBroadcastManager.registerReceiver(mGeocodingReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mGeocodingReceiver);
    }

    private void cleanUpAfterGeocoding() {
        mLastQueryLat = null;
        setProgressBarIndeterminateVisibility(false);
    }

    private void centerMapAt(LatLng latLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
        mMap.animateCamera(update);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMarker != null) {
            outState.putParcelable("markerPosition", mMarker.getPosition());
            outState.putBoolean("isInfoWindowShown", mMarker.isInfoWindowShown());
            if (mMarker.getTitle() != null) {
                outState.putString("infoWindowTitle", mMarker.getTitle());
                outState.putString("infoWindowSnippet", mMarker.getSnippet());
            }
            if (mLastQueryLat != null) {
                outState.putDouble("lastQueryLat", mLastQueryLat);
                outState.putDouble("lastQueryLng", mLastQueryLng);
            }
        }
    }

}
