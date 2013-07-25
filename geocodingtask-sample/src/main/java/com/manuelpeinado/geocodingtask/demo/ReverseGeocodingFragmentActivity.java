package com.manuelpeinado.geocodingtask.demo;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.manuelpeinado.geocodingtask.fragment.ReverseGeocodingFragment;
import com.manuelpeinado.geocodingtask.listener.ReverseGeocodingListener;

public class ReverseGeocodingFragmentActivity extends SherlockFragmentActivity implements OnMapClickListener,
        ReverseGeocodingListener, OnMyLocationChangeListener {

    private static final String REVERSE_GEOCODING_FRAGMENT = "reverseGeocoding";
    private GoogleMap mMap;
    private Marker mMarker;
    private boolean mFirstFixReceived;

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_geocoding);
        mFragmentManager = getSupportFragmentManager();

        SupportMapFragment mapFragment = (SupportMapFragment) mFragmentManager.findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        if (!ensureMapIsReady()) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(this);
        mMap.setOnMapClickListener(this);
        
        if (savedInstanceState != null && savedInstanceState.containsKey("markerPosition")) {
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
        Fragment geocodingFragment = getReverseGeocodingFragment();
        setProgressBarIndeterminateVisibility(geocodingFragment != null);
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
        ReverseGeocodingFragment fragment = getReverseGeocodingFragment();
        if (fragment != null) {
            detach(fragment);
        }
        fragment = ReverseGeocodingFragment.newInstance(newLocation(point));
        attach(fragment, REVERSE_GEOCODING_FRAGMENT);
        setProgressBarIndeterminateVisibility(true);
    }

    private void moveMarkerTo(LatLng position) {
        if (mMarker != null) {
            mMarker.remove();
        }
        mMarker = mMap.addMarker(new MarkerOptions().position(position));
        mMarker.setPosition(position);
    }

    @Override
    public void onReverseGeocodingResultReady(Object sender, Address result) {
        if (result != null) {
            mMarker.setTitle(DefaultAddressFormatter.format(result, ", "));
            String fmt = "Lat: %s, lon: %s";
            String snippet = String.format(fmt, result.getLatitude(), result.getLongitude());
            mMarker.setSnippet(snippet);
            mMarker.showInfoWindow();
        } else {
            Toast.makeText(this, R.string.geocoding_error_toast, Toast.LENGTH_LONG).show();
            mMarker.remove();
            mMarker = null;
        }
        cleanUpAfterGeocoding();
    }

    private void cleanUpAfterGeocoding() {
        Fragment geocodingFragment = getReverseGeocodingFragment();
        detach(geocodingFragment);
        setProgressBarIndeterminateVisibility(false);
    }

    private void attach(Fragment fragment, String tag) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(fragment, tag);
        ft.commit();
    }

    private void detach(Fragment fragment) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    private ReverseGeocodingFragment getReverseGeocodingFragment() {
        return (ReverseGeocodingFragment) mFragmentManager.findFragmentByTag(REVERSE_GEOCODING_FRAGMENT);
    }

    private Location newLocation(LatLng point) {
        Location result = new Location("");
        result.setLatitude(point.latitude);
        result.setLongitude(point.longitude);
        return result;
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
        }
    }

}
