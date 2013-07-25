package com.manuelpeinado.geocodingtask;

import android.location.Address;

public class DefaultAddressFormatter implements AddressFormatter {
    private final static String SEPARATOR = ", ";

    @Override
    public String format(Address address) {
        return format(address, SEPARATOR);
    }

    public static String format(Address result, String separator) {
        StringBuilder builder = new StringBuilder();
        builder.append(result.getAddressLine(0));
        for (int i = 1; i <= result.getMaxAddressLineIndex(); ++i) {
            builder.append(separator);
            builder.append(result.getAddressLine(i));
        }
        return builder.toString();
    }

}
