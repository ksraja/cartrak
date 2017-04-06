package com.atni.droid.cartrak;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atni.droid.cartrak.PreferencesHelper.SP_BLUETOOTH_DEVICES_ENABLED;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_ALTITUDE;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_LATITUDE;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_LONGTITUDE;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_RESET;
import static com.atni.droid.cartrak.PreferencesHelper.SP_LOCATION_TIME;
import static com.atni.droid.cartrak.R.id.map;

public class CarLocatorActivity
        extends AppCompatActivity
        implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final String TAG = "MainActivity";

    public static final Set<String> DEFAULT_BTOOTH_SET = new HashSet<>(Arrays.asList("CAR AUDIO", "VIZIO SB4051"));

    private GoogleMap mMap;

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_locator);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this,
                new String[]{"android.permission.ACCESS_FINE_LOCATION"},
                1);

        ActivityCompat.requestPermissions(this,
                new String[]{"android.permission.BLUETOOTH"},
                2);

        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        String bName = "";
        if(pairedDevices!=null && !pairedDevices.isEmpty()) {
             bName = pairedDevices.iterator().next().getName();
        }

        PreferencesHelper.registerOnSharedPreferenceChangeListener(this, this);

        if (!PreferencesHelper.contains(SP_BLUETOOTH_DEVICES_ENABLED, this)) {
            HashSet<String> defaultDevice = new HashSet<String>(Arrays.asList(bName));
            PreferencesHelper.putStringSet(SP_BLUETOOTH_DEVICES_ENABLED, defaultDevice, this);
            Toast.makeText(this,"CarTracker bluetooth devices - "+DEFAULT_BTOOTH_SET, Toast.LENGTH_SHORT);
        } else {
            Set<String> selectedDevices = PreferencesHelper.getStringSet(SP_BLUETOOTH_DEVICES_ENABLED, this);
            Toast.makeText(this,"CarTracker bluetooth devices -- "+selectedDevices, Toast.LENGTH_SHORT);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showBluetoothDevicesList(view, pairedDevices);

            }
        });
    }

    @Override
    protected void onDestroy() {
        PreferencesHelper.unregisterOnSharedPreferenceChangeListener(this, this);
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        resetMapMarker();
    }

    private void resetMapMarker() {

        LatLng usa = new LatLng(37.09024, -95.712891);

        if (PreferencesHelper.contains(SP_LOCATION_LATITUDE, this) &&
                PreferencesHelper.contains(SP_LOCATION_LONGTITUDE, this)) {

            Toast.makeText(this, "New CarPark location remarked in map", Toast.LENGTH_SHORT);

            String markerTitle = "Car parked here";
            double latitude = PreferencesHelper.getDouble(SP_LOCATION_LATITUDE, this);
            double longtitude = PreferencesHelper.getDouble(SP_LOCATION_LONGTITUDE, this);

            Log.i(TAG, "recorded latitude : " + latitude);
            Log.i(TAG, "recorded longtitude : " + longtitude);

            if (PreferencesHelper.contains(SP_LOCATION_TIME, this)) {
                long recordedTime = PreferencesHelper.getLong(SP_LOCATION_TIME, this);
                if (DateUtils.isToday(recordedTime)) {
                    markerTitle += " today at " + new SimpleDateFormat("h:mm a").format(new Date(recordedTime));
                } else if (isYesterday(recordedTime)) {
                    markerTitle += " yesterday at " + new SimpleDateFormat("h:mm a").format(new Date(recordedTime));
                } else {
                    markerTitle += " at " + new SimpleDateFormat("EEE, MMM d h:mm a").format(new Date(recordedTime));
                }

                Log.i(TAG, "recorded title : " + markerTitle);
            }
            if (PreferencesHelper.contains(SP_LOCATION_ALTITUDE, this)) {
                double altitude = PreferencesHelper.getDouble(SP_LOCATION_ALTITUDE, this);
                //FIXME: Check for current location, if near to 100 meters and do a diff and show in map
                Log.i(TAG, "recorded altitude : " + altitude);
            }

            LatLng carLoc = new LatLng(latitude, longtitude);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(carLoc).title(markerTitle)).setVisible(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLoc, 15));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
        } else {
            Toast.makeText(this, "No car parking location recorded yet", Toast.LENGTH_SHORT);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i(TAG, "Permission granted to ACCESS_COARSE_LOCATION");
                    //Toast.makeText(this, "Permission granted to ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i(TAG, "Permission denied to ACCESS_COARSE_LOCATION");
                    //Toast.makeText(this, "Permission denied to ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission granted to BLUETOOTH");
                } else {
                    Log.i(TAG, "Permission denied to BLUETOOTH");
                }
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if(key==SP_LOCATION_RESET && PreferencesHelper.getBoolean(key, this)) {
            resetMapMarker();
            PreferencesHelper.putBoolean(key, false, this);
        }
    }

    private static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE, -1);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    private void showBluetoothDevicesList(final View view, final Set<BluetoothDevice> bDevices) {

        final Set<String> selectedDevices = PreferencesHelper.getStringSet(SP_BLUETOOTH_DEVICES_ENABLED, this);

        final List<Integer> mSelectedDevices = new ArrayList();
        final String[] allAvailableDevices = new String[bDevices.size()];
        boolean[] selectedIndexes = new boolean[bDevices.size()];

        int i = 0;
        for (BluetoothDevice d : bDevices) {
            selectedIndexes[i] = selectedDevices.contains(d.getName());
            allAvailableDevices[i] = d.getName();
            if(selectedIndexes[i]) mSelectedDevices.add(i);
            i++;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Pick Car Bluetooths");

        alertDialog.setMultiChoiceItems(allAvailableDevices,
                selectedIndexes,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            mSelectedDevices.add(which);
                        } else if (mSelectedDevices.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedDevices.remove(Integer.valueOf(which));
                        }
                    }
                });

        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                selectedDevices.clear();
                for (Integer sIndex:mSelectedDevices) {
                    selectedDevices.add(allAvailableDevices[sIndex]);
                }
                PreferencesHelper.putStringSet(SP_BLUETOOTH_DEVICES_ENABLED, selectedDevices, CarLocatorActivity.this);

                Snackbar.make(view, "CarTracker bluetooth device is reset  "+selectedDevices, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Snackbar.make(view, "CarTracker bluetooth device list is unaltered", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        alertDialog.show();
    }

}
