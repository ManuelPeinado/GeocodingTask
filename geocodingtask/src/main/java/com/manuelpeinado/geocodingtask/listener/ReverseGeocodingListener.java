package com.manuelpeinado.geocodingtask.listener;

import android.location.Address;

public interface ReverseGeocodingListener {
    void onReverseGeocodingResultReady(Object sender, Address result);
}