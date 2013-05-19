package com.manuelpeinado.geocodingtask.demo;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.manuelpeinado.geocodingtask.AddressListDialog;
import com.manuelpeinado.geocodingtask.DefaultAddressFormatter;
import com.manuelpeinado.geocodingtask.listener.GeocodingListener;
import com.manuelpeinado.geocodingtask.service.GeocodingService;

public class GeocodingServiceActivity extends SherlockFragmentActivity implements GeocodingListener {

    private static final String ADDRESS_LIST_DIALOG = "addressListDialog";
    private EditText mEditText;
    private Button mButton;
    private FragmentManager mFragmentManager;
    private String mLastQuery;
    private LocalBroadcastManager mLocalBroadcastManager;
    private GeocodingReceiver mGeocodingReceiver = new GeocodingReceiver();

    private class GeocodingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(GeocodingService.PARAM_OUT_SUCCESS, false);
            if (success) {
                ArrayList<Address> addressList = intent
                        .getParcelableArrayListExtra(GeocodingService.PARAM_OUT_ADDRESS_LIST);
                onGeocodingSuccess(null, addressList);
            } else {
                onGeocodingFailure(null);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLastQuery = savedInstanceState.getString("lastQuery");
        }
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mFragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_direct_geocoding);
        mEditText = (EditText) findViewById(R.id.editText);
        mButton = (Button) findViewById(R.id.button);
        initButtonText();
        initAddressListDialog();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("lastQuery", mLastQuery);
    }

    protected void initAddressListDialog() {
        if (mFragmentManager.findFragmentByTag(ADDRESS_LIST_DIALOG) != null) {
            AddressListDialog dlg = (AddressListDialog) mFragmentManager.findFragmentByTag(ADDRESS_LIST_DIALOG);
            dlg.setListener(this);
        }
    }

    private void initButtonText() {
        if (mLastQuery != null) {
            mButton.setText(android.R.string.cancel);
            setProgressBarIndeterminateVisibility(true);
        } else {
            mButton.setText(R.string.geocode_address);
            setProgressBarIndeterminateVisibility(false);
        }
    }

    public void geocodeAddress(View v) {
        if (mLastQuery != null) {
            cleanUpAfterGeocoding();
        } else {
            hideSoftKeyboard();
            mLastQuery = mEditText.getText().toString();
            startGeocodingService(mLastQuery);
            setProgressBarIndeterminateVisibility(true);
            mButton.setText(android.R.string.cancel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(GeocodingService.RESPONSE_ACTION);
        mLocalBroadcastManager.registerReceiver(mGeocodingReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mGeocodingReceiver);
    }

    private void startGeocodingService(String address) {
        GeocodingService.IntentBuilder intentBuilder = new GeocodingService.IntentBuilder();
        intentBuilder.setAddressText(address);
        Intent intent = intentBuilder.build(this);
        startService(intent);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private void showDisambiguationDialog(ArrayList<Address> addressList) {
        AddressListDialog dlg = AddressListDialog.newInstance(addressList);
        dlg.setListener(this);
        dlg.show(mFragmentManager, ADDRESS_LIST_DIALOG);
    }

    private void cleanUpAfterGeocoding() {
        mLastQuery = null;
        mButton.setText(R.string.geocode_address);
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onGeocodingSuccess(Object sender, ArrayList<Address> result) {
        cleanUpAfterGeocoding();
        if (result.size() == 1) {
            String fmt = "Lat: %s, lon: %s";
            Address address = result.get(0);
            String text = String.format(fmt, address.getLatitude(), address.getLongitude());
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            mEditText.setText(DefaultAddressFormatter.format(address, ", "));
        } else {
            showDisambiguationDialog(result);
        }
    }

    @Override
    public void onGeocodingFailure(Object sender) {
        Toast.makeText(this, R.string.geocoding_error_toast, Toast.LENGTH_LONG).show();
        cleanUpAfterGeocoding();
    }

    @Override
    public void onGeocodingCanceled(Object sender) {
        Toast.makeText(this, R.string.geocoding_canceled_toast, Toast.LENGTH_LONG).show();
        cleanUpAfterGeocoding();
    }
}
