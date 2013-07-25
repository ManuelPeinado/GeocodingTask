package com.manuelpeinado.geocodingtask.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.manuelpeinado.geocodingtask.asynctask.GeocodingTask;

public class GeocodingService extends IntentService {

    public static final String RESPONSE_ACTION = "com.manuelpeinado.geocodingtask.action.GEOCODING_COMPLETE";
    public static final String PARAM_OUT_SUCCESS = "success";
    public static final String PARAM_IN_ADDRESS_TEXT = "addressText";
    public static final String PARAM_OUT_ADDRESS_LIST = "addressList";

    public static class IntentBuilder {
        private Bundle mBundle = new Bundle();

        public IntentBuilder setAddressText(String addressText) {
            mBundle.putString(PARAM_IN_ADDRESS_TEXT, addressText);
            return this;
        }

        public Intent build(Context context) {
            return new Intent(context, GeocodingService.class).putExtras(mBundle);
        }
    }

    public GeocodingService() {
        super("GeocodingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String addressText = intent.getStringExtra("addressText");
        GeocodingTask task = new GeocodingTask(this);
        ArrayList<Address> result = task.executeSync(addressText);
        Intent resultIntent = new Intent(RESPONSE_ACTION);
        if (result != null) {
            resultIntent.putParcelableArrayListExtra(PARAM_OUT_ADDRESS_LIST, result);
            resultIntent.putExtra(PARAM_OUT_SUCCESS, true);
        } else {
            resultIntent.putExtra(PARAM_OUT_SUCCESS, false);
        }
        resultIntent.putExtra(PARAM_IN_ADDRESS_TEXT, addressText);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }
}
