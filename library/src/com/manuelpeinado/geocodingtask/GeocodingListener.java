package com.manuelpeinado.geocodingtask;

import java.util.ArrayList;

import android.location.Address;

public interface GeocodingListener {
    void onGeocodingSuccess(Object sender, ArrayList<Address> result);

    void onGeocodingFailure(Object geocodingTask);

    void onGeocodingCanceled(Object geocodingTask);
}