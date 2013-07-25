package com.manuelpeinado.geocodingtask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.manuelpeinado.geocodingtask.listener.GeocodingListener;

public class AddressListDialog extends DialogFragment {
    private GeocodingListener mListener;
    private AddressFormatter mAddressFormatter = new DefaultAddressFormatter();

    public static AddressListDialog newInstance(ArrayList<Address> addressList) {
        AddressListDialog result = new AddressListDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("addressList", addressList);
        result.setArguments(args);
        return result;
    }

    public void setListener(GeocodingListener listener) {
        this.mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<Address> addressList = getArguments().getParcelableArrayList("addressList");
        String[] items = buildAddressList(addressList);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.gt__address_disambiguation_dialog_title);
        builder.setCancelable(false);
        builder.setItems(items, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onGeocodingSuccess(this, new ArrayList<Address>(Arrays.asList(addressList.get(which))));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onGeocodingCanceled(this);
            }
        });
        return builder.create();
    }

    private String[] buildAddressList(List<Address> results) {
        String[] result = new String[results.size()];
        for (int i = 0; i < results.size(); ++i) {
            result[i] = mAddressFormatter.format(results.get(i));
        }
        return result;
    }
}