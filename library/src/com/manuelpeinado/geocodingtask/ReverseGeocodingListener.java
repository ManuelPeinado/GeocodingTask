package com.manuelpeinado.geocodingtask;

import android.location.Address;

public interface ReverseGeocodingListener {
    void onReverseGeocodingResultReady(Object sender, Address result);
}