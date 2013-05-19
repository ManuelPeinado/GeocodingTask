package com.manuelpeinado.geocodingtask;

import java.util.ArrayList;

import android.app.Activity;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class GeocodingFragment extends Fragment implements GeocodingListener {
    private GeocodingTask mTask;
    private GeocodingListener mListener;

    public static GeocodingFragment newInstance(String addressText) {
        GeocodingFragment result = new GeocodingFragment();
        Bundle args = new Bundle();
        args.putString("addressText", addressText);
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
            mTask = new GeocodingTask(null);
            mTask.setListener(this);
        }

        mTask.setActivity((FragmentActivity)activity);
        mListener = (GeocodingListener) activity;

        if (firstTime) {
            String addressText = getArguments().getString("addressText");
            mTask.execute(addressText);
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
    public void onGeocodingSuccess(Object sender, ArrayList<Address> result) {
        if (mListener != null) {
            mListener.onGeocodingSuccess(this, result);
        }
    }

    @Override
    public void onGeocodingFailure(Object sender) {
        if (mListener != null) {
            mListener.onGeocodingFailure(this);
        }
    }

    @Override
    public void onGeocodingCanceled(Object sender) {
        if (mListener != null) {
            mListener.onGeocodingCanceled(this);
        }
    }
}
