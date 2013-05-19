package com.manuelpeinado.geocodingtask.demo;

import java.util.ArrayList;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.manuelpeinado.geocodingtask.AddressListDialog;
import com.manuelpeinado.geocodingtask.DefaultAddressFormatter;
import com.manuelpeinado.geocodingtask.fragment.GeocodingFragment;
import com.manuelpeinado.geocodingtask.listener.GeocodingListener;

public class GeocodingFragmentActivity extends SherlockFragmentActivity implements GeocodingListener {

    private static final String GEOCODING_FRAGMENT = "geocoding";
    private static final String ADDRESS_LIST_DIALOG = "addressListDialog";
    private EditText mEditText;
    private Button mButton;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_direct_geocoding);
        mEditText = (EditText) findViewById(R.id.editText);
        mButton = (Button) findViewById(R.id.button);
        initButtonText();
        initAddressListDialog();
    }

    protected void initAddressListDialog() {
        if (mFragmentManager.findFragmentByTag(ADDRESS_LIST_DIALOG) != null) {
            AddressListDialog dlg = (AddressListDialog) mFragmentManager.findFragmentByTag(ADDRESS_LIST_DIALOG);
            dlg.setListener(this);
        }
    }

    private void initButtonText() {
        Fragment geocodingFragment = getGeocodingFragment();
        if (geocodingFragment == null) {
            mButton.setText(R.string.geocode_address);
            setProgressBarIndeterminateVisibility(false);
        } else {
            mButton.setText(android.R.string.cancel);
            setProgressBarIndeterminateVisibility(true);
        }
    }

    private GeocodingFragment getGeocodingFragment() {
        return (GeocodingFragment) mFragmentManager.findFragmentByTag(GEOCODING_FRAGMENT);
    }

    public void geocodeAddress(View v) {
        Fragment geocodingFragment = getGeocodingFragment();
        if (geocodingFragment != null) {
            cleanUpAfterGeocoding();
        } else {
            hideSoftKeyboard();
            String address = mEditText.getText().toString();
            geocodingFragment = GeocodingFragment.newInstance(address);
            attach(geocodingFragment, GEOCODING_FRAGMENT);
            setProgressBarIndeterminateVisibility(true);
            mButton.setText(android.R.string.cancel);
        }
    }

    protected void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
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

    private void showDisambiguationDialog(ArrayList<Address> addressList) {
        AddressListDialog dlg = AddressListDialog.newInstance(addressList);
        dlg.setListener(this);
        dlg.show(mFragmentManager, ADDRESS_LIST_DIALOG);
    }

    @Override
    public void onGeocodingFailure(Object geocodingTask) {
        Toast.makeText(this, R.string.geocoding_error_toast, Toast.LENGTH_LONG).show();
        cleanUpAfterGeocoding();
    }

    @Override
    public void onGeocodingCanceled(Object geocodingTask) {
        Toast.makeText(this, R.string.geocoding_canceled_toast, Toast.LENGTH_LONG).show();
        cleanUpAfterGeocoding();
    }

    private void cleanUpAfterGeocoding() {
        Fragment geocodingFragment = getGeocodingFragment();
        detach(geocodingFragment);
        mButton.setText(R.string.geocode_address);
        setProgressBarIndeterminateVisibility(false);
    }

    private void attach(Fragment fragment, String tag) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(fragment, tag);
        ft.commit();
    }

    private void detach(Fragment fragment) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }
}
