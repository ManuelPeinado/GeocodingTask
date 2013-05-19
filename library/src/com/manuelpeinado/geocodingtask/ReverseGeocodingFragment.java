package com.manuelpeinado.geocodingtask;

import android.app.Activity;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ReverseGeocodingFragment extends Fragment implements ReverseGeocodingListener {
    private ReverseGeocodingTask mTask;
    private ReverseGeocodingListener mListener;

    public static ReverseGeocodingFragment newInstance(Location location) {
        ReverseGeocodingFragment result = new ReverseGeocodingFragment();
        Bundle args = new Bundle();
        args.putDouble("lat", location.getLatitude());
        args.putDouble("lng", location.getLongitude());
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create and execute the background task, only the first time onAttach is called.
        boolean firstTime = mTask == null;
        if (firstTime) {
            mTask = new ReverseGeocodingTask(activity.getApplicationContext());
            mTask.setListener(this);
        }

        mListener = (ReverseGeocodingListener) activity;

        if (firstTime) {
            double lat = getArguments().getDouble("lat");
            double lng = getArguments().getDouble("lng");
            mTask.execute(newLocation(lat, lng));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Set the callback to null so we don't accidentally leak the activity instance.
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTask != null) {
            mTask.cancel();
        }
    }

    @Override
    public void onReverseGeocodingResultReady(Object sender, Address result) {
        if (mListener != null) {
            mListener.onReverseGeocodingResultReady(this, result);
        }
    }

    private static Location newLocation(double lat, double lng) {
        Location result = new Location("");
        result.setLatitude(lat);
        result.setLongitude(lng);
        return result;
    }

}
