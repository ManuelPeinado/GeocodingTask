package com.manuelpeinado.geocodingtask.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.manuelpeinado.geocodingtask.asynctask.ReverseGeocodingTask;
import com.manuelpeinado.geocodingtask.fragment.ReverseGeocodingFragment;

public class ReverseGeocodingService extends IntentService {

    public static final String RESPONSE_ACTION = "com.manuelpeinado.geocodingtask.action.REVERSE_GEOCODING_COMPLETE";
    public static final String PARAM_OUT_SUCCESS = "success";
    public static final String PARAM_IN_LAT = "lat";
    public static final String PARAM_IN_LNG = "lng";
    public static final String PARAM_OUT_ADDRESS = "address";

    public static class IntentBuilder {
        private Bundle mBundle = new Bundle();

        public IntentBuilder setLatLng(double lat, double lng) {
            mBundle.putDouble(PARAM_IN_LAT, lat);
            mBundle.putDouble(PARAM_IN_LNG, lng);
            return this;
        }

        public Intent build(Context context) {
            return new Intent(context, ReverseGeocodingService.class).putExtras(mBundle);
        }
    }

    public ReverseGeocodingService() {
        super("GeocodingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        double lat = intent.getDoubleExtra("lat", 0);
        double lng = intent.getDoubleExtra("lng", 0);
        Location location = ReverseGeocodingFragment.newLocation(lat, lng);
        ReverseGeocodingTask task = new ReverseGeocodingTask(this);
        Address result = task.executeSync(location);
        Intent resultIntent = new Intent(RESPONSE_ACTION);
        if (result != null) {
            resultIntent.putExtra(PARAM_OUT_ADDRESS, result);
            resultIntent.putExtra(PARAM_OUT_SUCCESS, true);
        } else {
            resultIntent.putExtra(PARAM_OUT_SUCCESS, false);
        }
        resultIntent.putExtra(PARAM_IN_LAT, lat);
        resultIntent.putExtra(PARAM_IN_LNG, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }
}
