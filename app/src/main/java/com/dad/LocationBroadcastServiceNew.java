package com.dad;

import static com.dad.registration.util.Utills.isInternetConnected;
import static com.dad.util.CheckForeground.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.dad.recievers.BLEHelper;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.fragment.AlertFragment;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.settings.webservices.WsCallDADTest;
import com.dad.settings.webservices.WsCallSendDanger;
import com.dad.settings.webservices.WsCallUpdateLocation;
import com.dad.util.CheckForeground;
import com.dad.util.Constants;
import com.dad.util.GPSTracker;
import com.dad.util.Preference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class LocationBroadcastServiceNew extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public final String TAG = getClass().getSimpleName();

    private static final String CHANNEL_ID = "LocationServiceChannel";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5 * 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static final float DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS = 100f;

    private GoogleApiClient googleApiClient;
    private AsyncTaskUpdateLocation asyncTaskUpdateLocation;
    private float mSmallestDisplacementValue = DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BLEHelper bleHelper;
    private Handler handler;
    private int accuracy;
    private String timezoneID;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter2.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        handler = new Handler();
        registerReceiver(mBroadcastReceiver2, filter2);
        buildGoogleApiClient();
        createNotificationChannel();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothManager.getAdapter().startDiscovery();
        if (isInternetConnected(getApplicationContext())) {
            getLatLong();
            if (!Preference.getInstance().mSharedPreferences.getBoolean(Constant.ISLOGEDD_OUT, false)) {
                serchiBeaconAvailability();
            }
        }
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        timezoneID = tz.getID();
        if (isInternetConnected(getApplicationContext())) {
            getLatLong();
            if (!Preference.getInstance().mSharedPreferences.getBoolean(Constant.ISLOGEDD_OUT, false)) {
                serchiBeaconAvailability();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Your location is being tracked")
                .setSmallIcon(R.drawable.dadwhite)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        // Do your background work here
        Log.d(TAG, "onStartCommand");
        mSmallestDisplacementValue = DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS;
        if (intent != null) {
            mSmallestDisplacementValue = intent.getFloatExtra(Constants.Extras.SMALLEST_DISPLACEMENT_VALUE, DEFAULT_SMALLEST_DISPLACEMENT_DISTANCE_IN_METERS);
        }
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        stopLocationUpdates();
        unregisterReceiver(mBroadcastReceiver2);
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
        startListeningForLocationRequests();
    }

    private void startListeningForLocationRequests() {
        final LocationRequest locationRequest = createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Connection to Google API suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        sendLocationUpdate(location);
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setSmallestDisplacement(mSmallestDisplacementValue);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void stopLocationUpdates() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private void sendLocationUpdate(final Location location) {
        if (location != null) {
            callLocationUpdateService(location.getLatitude(), location.getLongitude());
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Preference.getInstance().savePreferenceData(Constant.COMMON_LATITUDE, String.valueOf(location.getLatitude()));
            Preference.getInstance().savePreferenceData(Constant.COMMON_LONGITUDE, String.valueOf(location.getLongitude()));
            Preference.getInstance().savePreferenceData(Constant.COMMON_ACCURACY, (int) location.getAccuracy());
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
        }
    }

    @SuppressLint("StaticFieldLeak")
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
                    Log.d(TAG, "Location updated: lat=" + latitude + ", long=" + longitude + ", userId=" + Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, ""));
                } else {
                    Log.e(TAG, "Location update failed");
                }
            }
        }
    }

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        serchiBeaconAvailability();
                        Log.d(TAG, "ACTION_DISCOVERY_STARTED");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        BLEHelper.IsFirst = false;
                        Log.d(TAG, "ACTION_DISCOVERY_FINISHED");
                        break;
//                    case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
//                        serchiBeaconAvailability();
//                        break;
                }
            }
        }
    };

    @SuppressLint("NewApi")
    private void serchiBeaconAvailability() {
        if (!Utills.isInternetConnected(this)) {
            Toast.makeText(this, getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
            bleHelper = new BLEHelper(this, true);
            mHandler = new Handler();

            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                return;
            }

            scanLeDevice();
        }
    }

    private static final long SCAN_PERIOD = 10000;  // Adjust scan period as needed

    @SuppressLint("NewApi")
    private void scanLeDevice() {
        mHandler.postDelayed(() -> {
            if (mBluetoothAdapter != null) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
            }
        }, SCAN_PERIOD);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED && mBluetoothAdapter != null) {
            mBluetoothAdapter.startLeScan(bleHelper.getmLeScanCallback());
        }
    }

    private Double latitude;
    private Double longitude;
    private AsyncTaskSendPush asyncTaskSendPush;
    private AsyncTaskTestMode asyncTaskTestMode;
    private AsyncTestMode asyncTestMode;

    private static final long MIN_ALERT_TIME_INTERVEL = 2 * 60 * 1000;

    private class PushForReciever extends Thread {
        @Override
        public void run() {
            String userId = Preference.getInstance().mSharedPreferences.getString(Constant.USER_ID, "");
//            new ServerResponseHelper().requestToPush(, longitude, userId, timezoneID);
            callSenDangerServiceRecievingListScreen();
        }

    }

    private class PushForCrowdAlert extends Thread {
        @Override
        public void run() {
            callSenDangerServiceRecievingListScreen();

        }
    }


    private class TestModeService extends Thread {
        @Override
        public void run() {
            callTestMode();
        }
    }


    @SuppressLint("NewApi")
    public void sendPushNotification() {
        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false)) {

            if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ON_SETTING, false)) {
                return;
            }

            if (!isAMinuteOver()) {
                return;
            }

            Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, String.valueOf(System.currentTimeMillis()));

            new TestModeService().start();
//            new PushForCrowdAlert().start();

            if (mBluetoothAdapter != null) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
                }

            }

            if (!CheckForeground.isInForeGround()) {
                return;
            }
            handler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show());

        } else {

            if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_ON_SETTING, false)) {
                return;
            }

            if (!isAMinuteOver()) {
                return;
            }

            Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, "" + System.currentTimeMillis());

            new PushForReciever().start();
//            new PushForCrowdAlert().start();

            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.stopLeScan(bleHelper.getmLeScanCallback());
            }

            if (!CheckForeground.isInForeGround()) {
                return;
            }
            handler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show());
        }


    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskTestMode extends AsyncTask<Void, Void, Void> {
        private WsCallDADTest wsCallDADTest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wsCallDADTest = new WsCallDADTest(getActivity());
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallDADTest.executeService();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallDADTest.isSuccess()) {
                    // From here do further logic
                    //Toast.makeText(getActivity(), "Successfully ON Test Mode ", Toast.LENGTH_SHORT).show();
                    Utills.displayDialog(getActivity(), getString(R.string.app_name), getString(R.string.TAG_TEST_MODE_ON), getString(R.string.ok), "", false, false);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.TAG_SOME_WENT_WRONG_MSG), Toast.LENGTH_SHORT).show();
                }
            }
        }


    }


    private boolean isAMinuteOver() {
        long currentTimeMillis = System.currentTimeMillis();
        String lastLoggedTimeString = Preference.getInstance().mSharedPreferences.getString(Constant.LAST_LOG_TIME, "");
        long lastLoggedTime = 0;
        try {
            lastLoggedTime = Long.parseLong(lastLoggedTimeString);
        } catch (Exception ignored) {
        }
        if ((currentTimeMillis - lastLoggedTime) <= MIN_ALERT_TIME_INTERVEL) {
            return false;
        }
        Log.d("BleService", "isAMinuteOver()");

        return true;
    }


    private void callSenDangerServiceRecievingListScreen() {

//        asyncTaskSendPush = new AsyncTaskSendCrowdAlert();

        if (asyncTaskSendPush != null && asyncTaskSendPush.getStatus() == AsyncTask.Status.PENDING) {
            asyncTaskSendPush.execute();
        } else if (asyncTaskSendPush == null || asyncTaskSendPush.getStatus() == AsyncTask.Status.FINISHED) {
            asyncTaskSendPush = new AsyncTaskSendPush();
            asyncTaskSendPush.execute();
        }
    }

    private void callTestMode() {

//        asyncTaskSendPush = new AsyncTaskSendCrowdAlert();

        if (asyncTestMode != null && asyncTestMode.getStatus() == AsyncTask.Status.PENDING) {
            asyncTestMode.execute();
        } else if (asyncTestMode == null || asyncTestMode.getStatus() == AsyncTask.Status.FINISHED) {
            asyncTestMode = new AsyncTestMode();
            asyncTestMode.execute();
        }
    }

    private void playAlarmSound() {
        final AssetFileDescriptor audioFile = getActivity().getResources().openRawResourceFd(R.raw.tigerlightsound);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioFile.getFileDescriptor(), audioFile.getStartOffset(), audioFile.getLength());

                    mediaPlayer.prepare();
                    mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void getLatLong() {
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            accuracy = (int) gpsTracker.getAccuracy();
            Log.d(TAG, String.valueOf(accuracy));
        }
    }

    @SuppressLint("StaticFieldLeak")
    class AsyncTaskSendPush extends AsyncTask<Void, Void, Void> {

        private WsCallSendDanger wsCallSendDanger;
        String lat = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LATITUDE, "0.01");
        String log = Preference.getInstance().mSharedPreferences.getString(Constant.COMMON_LONGITUDE, "0.01");
        int accuracy = Preference.getInstance().mSharedPreferences.getInt(Constant.COMMON_ACCURACY, 0);
//        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallSendDanger = new WsCallSendDanger(getApplicationContext());
            wsCallSendDanger.executeService(Double.parseDouble(lat), Double.parseDouble(log), timezoneID, accuracy);
            playAlarmSound();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallSendDanger.isSuccess())
                {
                    // From here do further logic

                    if (getActivity() != null) //TODO:  Band-aid (per Rod) for unknown NPE
                    {
                        final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogTheme);
                        dialog.setContentView(R.layout.custom_progress_layout);
                        final TextView tvTitlee = (TextView) dialog.findViewById(R.id.dialog_tvTitlee);
                        final TextView tvMessagee = (TextView) dialog.findViewById(R.id.dialog_tvMessagee);
                        final TextView tvMsgLeve = (TextView) dialog.findViewById(R.id.dialog_tvMsgLevel);
                        final TextView tvPosButtonn = (TextView) dialog.findViewById(R.id.dialog_tvPosButtonn);

                        tvTitlee.setText(getString(R.string.custom_progess_dialog_tv_title));
                        tvMessagee.setText(getString(R.string.custom_progess_dialog_tv_msg));
                        tvPosButtonn.setText(getString(R.string.custom_progess_dialog_tv_ok));

                        Log.d("Al_UUID", Preference.getInstance().mSharedPreferences.getString("UUIDHex", ""));
                        Preference preference = Preference.getInstance();
                        if (preference != null)
                        {
                            if (Preference.getInstance().mSharedPreferences.getString(Constants.Preferences.Keys.NEW_UUID_KEY, "").equalsIgnoreCase(Constants.NEW_UUID))
                            {

                                final String major = preference.mSharedPreferences.getString(Constants.Preferences.Keys.NEW_MAJOR_KEY, "");
                                final String minor = preference.mSharedPreferences.getString(Constants.Preferences.Keys.NEW_MINOR_KEY, "");

                                if (!minor.equals("") && !major.equals(""))
                                {
                                    double doubleminor = Double.parseDouble(minor) / 1000;
                                    double doublemajor = Double.parseDouble(major) / 1000;

                                    if (doubleminor >= -3.0 && doublemajor >= 2.5)
                                    {

                                        tvMsgLeve.setText(getString(R.string.battery_level_good));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);

                                    }
                                    else if (doubleminor >= -2.499 && doublemajor >= 2.0)
                                    {

                                        tvMsgLeve.setText(getString(R.string.battery_level_low));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_yello));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    }
                                    else if (doubleminor < 2.0 && doublemajor < 2.0)
                                    {
                                        tvMsgLeve.setText(getString(R.string.battery_level_replace));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_alert_red));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    }
                                    else
                                    {
                                        tvMsgLeve.setText(getString(R.string.battery_level_good));
                                        tvMsgLeve.setBackgroundColor(getResources().getColor(R.color.color_green));
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_UUID_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MAJOR_KEY);
                                        preference.clearPreferenceItem(Constants.Preferences.Keys.NEW_MINOR_KEY);
                                    }


                                }
                            }
                        }

                        tvPosButtonn.setOnClickListener(view -> {
                            dialog.dismiss();
                            sendAlertBroadcast();

                        });

                        dialog.show();
                    }
                }
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    class AsyncTestMode extends AsyncTask<Void, Void, Void> {

        private WsCallDADTest wsCallDADTest;
//        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            playAlarmSound();
        }

        @Override
        protected Void doInBackground(Void... params) {
            wsCallDADTest = new WsCallDADTest(getApplicationContext());
            wsCallDADTest.executeService();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                if (wsCallDADTest.isSuccess()) {
                    // From here do further logic
                    sendAlertBroadcast();
                }
            }
        }

    }

    private void sendAlertBroadcast()
    {
        Intent intent = new Intent();
        intent.setAction(Constants.Actions.SENT_ALERT_ACTION);
        sendBroadcast(intent);
    }
}
