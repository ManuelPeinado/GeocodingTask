package com.manuelpeinado.geocodingtask;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

public class GeocodingTask extends AsyncTask<String, Void, ArrayList<Address>> {

    private static final int MAX_RESULTS = 10;
    private Activity mActivity;
    private GeocodingListener mListener;
    private String mAddressText;
    private boolean mockSlowProgress = false;

    public GeocodingTask() {
        this(null);
    }

    public GeocodingTask(Activity activity) {
        this.mActivity = activity;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void setListener(GeocodingListener listener) {
        this.mListener = listener;
    }

    public String getAddressText() {
        return mAddressText;
    }

    @Override
    protected ArrayList<Address> doInBackground(String... params) {
        if (mockSlowProgress) {
            sleep(3000);
        }
        Geocoder geocoder = new Geocoder(mActivity);
        try {
            mAddressText = params[0];
            return new ArrayList<Address>(geocoder.getFromLocationName(mAddressText, MAX_RESULTS));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Address> results) {
        if (mListener == null) {
            return;
        }
        if (results == null || results.size() == 0) {
            mListener.onGeocodingFailure(this);
        } else {
            mListener.onGeocodingSuccess(this, results);
        }
    }

    public void cancel() {
        cancel(true);
    }
    
    @Override
    protected void onCancelled(ArrayList<Address> result) {
        mListener.onGeocodingCanceled(this);
    }
}
