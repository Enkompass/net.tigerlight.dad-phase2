package com.dad;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
import static com.dad.util.CheckForeground.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.dad.registration.activity.MainActivity;
import com.dad.registration.fragment.ContactFragment;
import com.dad.registration.util.Constant;
import com.dad.registration.util.Utills;
import com.dad.settings.webservices.PushAlertWorker;
import com.dad.settings.webservices.TestAlertWorker;
import com.dad.settings.webservices.UpdateLocationWorker;
import com.dad.util.CheckForeground;
import com.dad.util.Constants;
import com.dad.util.Preference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;
import java.util.List;

public class LocationService extends Service implements LocationListener {

    public final String TAG = getClass().getSimpleName();
    private static final long MIN_ALERT_TIME_INTERVAL = 2 * 60 * 1000;
    private static final long SCAN_PERIOD = 1000;
    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String CHANNEL_ID = "default";

    private FusedLocationProviderClient fusedLocationClient;

    private LocationCallback locationCallback;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback leScanCallback;

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_DISCOVERY_STARTED)) {
                searchIBeaconAvailability();
                Log.d("DIS_START", "ACTION_DISCOVERY_STARTED");
            }
            if (action.equals(ACTION_DISCOVERY_FINISHED)) {
                Log.d("DIS_STOP", "ACTION_DISCOVERY_FINISHED");
            }
            if (action.equals(ACTION_SCAN_MODE_CHANGED)) {
                searchIBeaconAvailability();
            }
        }
    };

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundService();
        Log.d(TAG, "onCreate");
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "D.A.D App",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Running location service")
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent);

        startForeground(1, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        // Perform your tasks here
        performTasks();
        return START_STICKY;
    }

    private void performTasks() {
        // Your core logic here
        Log.d(TAG, "Performing tasks");
        setupLocationUpdates();
        setupBluetoothScans();
    }

    private void setupBluetoothScans() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            leScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    // Handle scan result
                    handleScanResult(result);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult result: results) {
                        handleScanResult(result);
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    // Handle scan failure
                }
            };
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction(ACTION_DISCOVERY_STARTED);
            filter2.addAction(ACTION_DISCOVERY_FINISHED);
            filter2.addAction(ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver2, filter2);
        } else {
            Log.e(TAG, "Bluetooth is not supported on this device.");
        }
    }

    private String convertBytesToHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = HEX_ARRAY[v >>> 4];
            hex[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hex);
    }

    private void handleScanResult(ScanResult result) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        StringBuilder msg = new StringBuilder();

        byte[] scanRecord = result.getScanRecord().getBytes();
        BluetoothDevice device = result.getDevice();
        int rssi = result.getRssi();
        for (byte b : scanRecord)
            msg.append(String.format("%02x ", b));

        msg = new StringBuilder(msg.toString().replaceAll("\\s+", ""));
        ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
        ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();

        Log.v(TAG, "msg:" + msg);
        Log.v(TAG, "rss:" + rssi);
        Log.v(TAG, "device:" + device);
        int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;
        Log.e(TAG, "Serial Number is " + serialNumber);
        String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
        Log.d(TAG, "UUID: " + UUIDHex);

        if (UUIDHex.equalsIgnoreCase(Constants.NEW_UUID)) {
            Log.d(TAG, "found beacon");

            //if (UUIDHex.equals(GELO_UUID)) {
            // Bytes 25 and 26 of the advertisement packet represent
            // the major value
            int major = (scanRecord[25] << 8) | (scanRecord[26]);
            //Log.e("Major", "Serial Number is " + major);


            // Bytes 27 and 28 of the advertisement packet represent
            // the minor
            int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
            Log.d("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);

            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.UUID_KEY, UUIDHex);
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MAJOR_KEY, String.valueOf(major));
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MINOR_KEY, String.valueOf(minor));

            if (getActivity() != null) //TODO:  Band-aid (per Rod) for unknown NPE
            {
                sendPushNotification();
                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
            } else {
                Log.e(TAG, "getActivity() = null");
            }
        } else if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID) && Constants.LAIRD_BEACON_LABEL.equals(device.getName())) {
            //if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID)) {

            Preference.getInstance().mSharedPreferences.getString("IsSecond", "true");


            Log.d(TAG, "found beacon");


            //if (UUIDHex.equals(GELO_UUID)) {
            // Bytes 25 and 26 of the advertisement packet represent
            // the major value
            int major = (scanRecord[25] << 8) | (scanRecord[26]);
            Log.e(TAG, "Serial Number is " + major);

            // Bytes 27 and 28 of the advertisement packet represent
            // the minor value
            int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
            Log.d(TAG, "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);

            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.UUID_KEY, UUIDHex);
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MAJOR_KEY, String.valueOf(major));
            Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MINOR_KEY, String.valueOf(minor));

            if (getActivity() != null) {
                sendPushNotification();
                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
            } else {
                Log.e(TAG, "getActivity() = null");
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
        if ((currentTimeMillis - lastLoggedTime) <= MIN_ALERT_TIME_INTERVAL) {
            return false;
        }
        Log.d("BleService", "isAMinuteOver()");

        return true;
    }

    @SuppressLint("NewApi")
    public void sendPushNotification() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.stopScan(leScanCallback);
        }
        if (!isAMinuteOver()) {
            return;
        }
        Preference.getInstance().savePreferenceData(Constant.LAST_LOG_TIME, "" + System.currentTimeMillis());
        if (CheckForeground.isInForeGround()) {
            Handler mHandler = new Handler();
            mHandler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.TAG_SENDING_ALERT), Toast.LENGTH_SHORT).show());
        }
        if (Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_TEST_MODE, false)) {
            callTestAlert();
        } else {
            callPushAlert();
            sendAlertBroadcast();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        // Clean up resources here
        try {
            if (fusedLocationClient != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
            unregisterReceiver(mBroadcastReceiver2);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("NewApi")
    private void searchIBeaconAvailability() {
        if (!Utills.isInternetConnected(this)) {
            Toast.makeText(this, getString(R.string.alert_check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothLeScanner != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Handler mHandler = new Handler();
            mHandler.postDelayed(() -> bluetoothLeScanner.stopScan(leScanCallback), 2 * 60 * SCAN_PERIOD);
            bluetoothLeScanner.startScan(leScanCallback);
        }
    }

    private void setupLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        onLocationChanged(location);
                    }
                }
            };
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        callLocationUpdateService(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "sendLocationUpdate()----------Location sent" + "lat:" + location.getLatitude() + "long:" + location.getLongitude() + ", accuracy: " + location.getAccuracy());
        Preference.getInstance().savePreferenceData(Constant.COMMON_LATITUDE, String.valueOf(location.getLatitude()));
        Preference.getInstance().savePreferenceData(Constant.COMMON_LONGITUDE, String.valueOf(location.getLongitude()));
        Preference.getInstance().savePreferenceData(Constant.COMMON_ACCURACY, (int) location.getAccuracy());
    }

    public void callLocationUpdateService(double latitude, double longitude) {
        if (Utills.isInternetConnected(this)) {
            Data inputData = new Data.Builder()
                    .putDouble("latitude", latitude)
                    .putDouble("longitude", longitude)
                    .build();

            OneTimeWorkRequest locationUpdateWorkRequest = new OneTimeWorkRequest.Builder(UpdateLocationWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(this).enqueue(locationUpdateWorkRequest);
        }
    }

    public void callTestAlert() {
        if (Utills.isInternetConnected(this)) {
            Data inputData = new Data.Builder()
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TestAlertWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(this).enqueue(workRequest);
        }
    }

    public void callPushAlert() {
        if (Utills.isInternetConnected(this)) {
            Data inputData = new Data.Builder()
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(PushAlertWorker.class)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(this).enqueue(workRequest);
        }
    }

    private void sendAlertBroadcast()
    {
        Intent intent = new Intent();
        intent.setAction(Constants.Actions.SENT_ALERT_ACTION);
        sendBroadcast(intent);
    }
}
