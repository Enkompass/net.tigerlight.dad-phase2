package com.dad;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.dad.recievers.BLEHelper;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.settings.webservices.WsCallUpdateLocation;
import com.dad.util.Constants;
import com.dad.util.Preference;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;

/**
 * Created on 23/11/16.
 */

public class LocationBroadcastServiceNew extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public final String TAG = getClass().getSimpleName();
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5 * 1000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The smallest distance to check for a new location
     */
    public static final float DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS = 100f;

    private GoogleApiClient googleApiClient;
    private AsyncTaskUpdateLocation asyncTaskUpdateLocation;

    private float mSmallestDisplacementValue = DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS;

    //private SocketClient socketClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_DISCOVERY_STARTED);
        filter2.addAction(ACTION_DISCOVERY_FINISHED);
        filter2.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, filter2);
        buildGoogleApiClient();

        //socketClient = new SocketClient();
        //socketClient.initializeSocket();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "Service has been started ", this);
        mSmallestDisplacementValue = DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS;
        if (intent != null)
        {
            mSmallestDisplacementValue = intent.getFloatExtra(Constants.Extras.SMALLEST_DISPLACEMENT_VALUE, DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "onDestroy");
        stopLocationUpdates();
        unregisterReceiver(mBroadcastReceiver2);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved");
    }

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle connectionHint) {

//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "onConnected ", this);
        startListeningForLocationRequests();
    }

    private void startListeningForLocationRequests()
    {
        final LocationRequest locationRequest = createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "onConnectionSuspended", this);
        Log.d(TAG, "Connection to Google API suspended");
        googleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "onConnectionFailed", this);
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        sendLocationUpdate(location);
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
//        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(mSmallestDisplacementValue);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /*Log.d(TAG, String.format(Locale.US, "createLocationRequest: smallestDisplacement = %1$f, interval = %2$d", locationRequest.getSmallestDisplacement(), locationRequest.getInterval()));*/

        return locationRequest;
    }


    private void stopLocationUpdates() {
//        Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "stopLocationUpdates", this);
        if (googleApiClient.isConnected())
        {
            //Log.d(TAG, "Stopping Location Updates");
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private void sendLocationUpdate(final Location location) {
        if (location != null) {
            callLocationUpdateService(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "sendLocationUpdate()----------Location sent" + "lat:" + location.getLatitude() + "long:" + location.getLongitude() + ", accuracy: " + location.getAccuracy());
//            final String cName = getCountryName(location.getLatitude(), location.getLongitude());
//            Preference.getInstance().savePreferenceData(Constant.C_CODE, cName);

            Preference.getInstance().savePreferenceData(Constant.COMMON_LATITUDE, String.valueOf(location.getLatitude()));
            Preference.getInstance().savePreferenceData(Constant.COMMON_LONGITUDE, String.valueOf(location.getLongitude()));
            Preference.getInstance().savePreferenceData(Constant.COMMON_ACCURACY, (int) location.getAccuracy());

//            String cName = Utills.getCountryName(this, Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, ""), Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, ""));
//            Preference.getInstance().savePreferenceData(Constant.C_CODE, cName);

        } else {
            Log.e(TAG, "sendLocationUpdate()----------Location Null");
        }
    }

    public void callLocationUpdateService(double latitude, double longitude) {
        if (Utills.isInternetConnected(this)) {


            if (asyncTaskUpdateLocation != null && asyncTaskUpdateLocation.getStatus() == AsyncTask.Status.PENDING) {
                asyncTaskUpdateLocation.execute();
            } else if (asyncTaskUpdateLocation == null || asyncTaskUpdateLocation.getStatus() == AsyncTask.Status.FINISHED) {
                asyncTaskUpdateLocation = new AsyncTaskUpdateLocation(latitude, longitude);
                asyncTaskUpdateLocation.execute();
            }
        } else {
//            Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "Internet is not enabled ", this);

//            Toast.makeText(this, "Internet is not connected", Toast.LENGTH_SHORT).show();
        }
    }


    private class AsyncTaskUpdateLocation extends AsyncTask<Void, Void, Void> {
        private WsCallUpdateLocation wsCallUpdateLocation;
        double latitude;
        double longitude;


        public AsyncTaskUpdateLocation(double latitude, double longitude) {

            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallUpdateLocation = new WsCallUpdateLocation(LocationBroadcastServiceNew.this);
            wsCallUpdateLocation.executeService(latitude, longitude);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallUpdateLocation.isSuccess()) {
                    Log.d("@@@", "" + latitude + "long:" + longitude + "userid:" + Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, ""));
//                    Utills.writeFile("\n\n" + "AT " + new Date() + "   " + "updated service:lat=" + latitude + "log=" + longitude, LocationBroadcastServiceNew.this);

                } else {
                    Log.e("update", "Not Updated");

                }
            }
        }
    }


    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(ACTION_DISCOVERY_STARTED)) {


                serchiBeaconAvailability();
                Log.d("DIS_START", "ACTION_DISCOVERY_STARTED");
            }
            if (action.equals(ACTION_DISCOVERY_FINISHED)) {


                BLEHelper.IsFirst = false;
                Log.d("DIS_STOP", "ACTION_DISCOVERY_FINISHED" + BLEHelper.IsFirst);

            }


        }
    };

    private boolean isBleSupported;
    private BLEHelper bleHelper;

    /**
     * @author Ambujesh Tripathi - To be used to check the availability of BLE
     */
    @SuppressLint("NewApi")
    private void serchiBeaconAvailability() {
        if (!Utills.isInternetConnected(this)) {
            Toast.makeText(this, getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isBleSupported = false;
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        isBleSupported = true;
        bleHelper = new BLEHelper(this, true);
        mHandler = new Handler();


        if (mBluetoothAdapter != null && isBleSupported && !mBluetoothAdapter.isEnabled()) {
            return;
        }

        scanLeDevice(true);
    }

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1000;
    private static final long MIN_ALERT_TIME_INTERVEL = 2 * 60 * 1000;
//    public static String TEST_UUID = "E2C56DB5DFFB48D2B060D0F5A71096E0";

    @SuppressLint("NewApi")
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period. Please Note that
            // this period should be same time as it is defined in Recieving
            // list screen for bleReciever.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
                    }
                }

            }, 2 * 60 * SCAN_PERIOD);
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.startLeScan(bleHelper.getmLeScanCallback());
            }
        } else {
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
            }
        }
    }


}
