package com.atni.droid.cartrak;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Set;

import static com.atni.droid.cartrak.PreferencesHelper.SP_BLUETOOTH_DEVICES_ENABLED;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_ALTITUDE;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_LATITUDE;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_LONGTITUDE;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_RESET;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_TIME;


public class BluetoothBroadcastReceiver
        extends
        BroadcastReceiver
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static String TAG = "BBroadcastReceiver";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private LocationRequest mLocationRequest;

    boolean recordLocation = false;

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                Log.i(TAG, "ACTION_ACL_CONNECTED");
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                Log.i(TAG, "ACTION_ACL_DISCONNECTED");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Set<String> bdeviceTracked = PreferencesHelper.getStringSet(SP_BLUETOOTH_DEVICES_ENABLED, context);

                if (bdeviceTracked.contains(device.getName())) {
                    buildGoogleApiClient();
                    Log.i(TAG, "GoogleApiClient is connecting : " + mGoogleApiClient.isConnecting());
                    Log.i(TAG, "GoogleApiClient is connected : " + mGoogleApiClient.isConnected());
                    recordLocation = true;
                }
                break;
        }
    }


    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        mGoogleApiClient = new GoogleApiClient.Builder(this.context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "On Connected");
        if (recordLocation) {
            Log.i(TAG, "Requesting Location Updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        if (recordLocation) {
            Log.i(TAG, "onLocationChanged to record new location update");
            Toast.makeText(this.context, "CarPark location recorded !!", Toast.LENGTH_SHORT).show();

            PreferencesHelper.putDouble(SP_LOCATION_LATITUDE, location.getLatitude(), context);
            PreferencesHelper.putDouble(SP_LOCATION_LONGTITUDE, location.getLongitude(), context);
            PreferencesHelper.putDouble(SP_LOCATION_ALTITUDE, location.getAltitude(), context);
            PreferencesHelper.putLong(SP_LOCATION_TIME, location.getTime(), context);
            PreferencesHelper.putBoolean(SP_LOCATION_RESET, true, context);

            mGoogleApiClient.disconnect();
            recordLocation = false;
        }
    }

}
