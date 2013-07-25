package com.manuelpeinado.geocodingtask.listener;

import java.util.ArrayList;

import android.location.Address;

public interface GeocodingListener {
    void onGeocodingSuccess(Object sender, ArrayList<Address> result);

    void onGeocodingFailure(Object sender);

    void onGeocodingCanceled(Object sender);
}