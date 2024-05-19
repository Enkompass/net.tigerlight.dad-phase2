package com.dad.recievers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.dad.LocationBroadcastServiceNew;
import com.dad.blework.BleService;
import com.dad.home.BaseFragment;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.fragment.ContactFragment;
import com.dad.util.Constants;
import com.dad.util.Preference;

import java.util.Arrays;
import java.util.UUID;

import static com.dad.util.CheckForeground.getActivity;

public class BLEHelper {

    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private String TAG = BLEHelper.class.getName();
    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_LEVEL_CHARACTER_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static final boolean IS_AUTO_CONNECT = true;
    public static boolean IsFirst = true;

    @SuppressLint("NewApi")
    public BLEHelper(final BaseFragment settingScreen, final boolean isFromSetting) {

        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {

                if (ActivityCompat.checkSelfPermission(settingScreen.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, handle accordingly
                    return;
                }

                ((Activity) settingScreen.getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "";
                        for (byte b : scanRecord)
                            msg += String.format("%02x ", b);

                        msg = msg.replaceAll("\\s+", "");
                        ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
                        ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();

                        if (msg.contains(ContactFragment.TEST_UUID)) {
                            if (isFromSetting) {
                                // restartActivity();
                            } else {
                                ((ContactFragment) settingScreen).sendPushNotification();
                            }
                        }

                        if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
                            if (isFromSetting) {
                                // restartActivity();
                            } else {
                                ((ContactFragment) settingScreen).sendPushNotification();
                            }
                        }
                    }
                });
            }
        };
    }

    @SuppressLint("NewApi")
    public BLEHelper(final BleService bleService) {

        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {

                if (ActivityCompat.checkSelfPermission(bleService, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, handle accordingly
                    return;
                }

                String msg = "";
                for (byte b : scanRecord)
                    msg += String.format("%02x ", b);

                msg = msg.replaceAll("\\s+", "");
                ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
                ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();

                int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;
                String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));

                if (UUIDHex.equalsIgnoreCase(Constants.NEW_UUID)) {
                    Log.d("tigerlight", "found");
                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);

                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.NEW_UUID_KEY, String.valueOf(UUIDHex));
                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.NEW_MAJOR_KEY, String.valueOf(major));
                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.NEW_MINOR_KEY, String.valueOf(minor));

                    if (msg.contains(ContactFragment.TEST_UUID) || msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
                        bleService.sendPushNotification();
                    }
                }

                if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID) && Constants.LAIRD_BEACON_LABEL.equals(device.getName())) {
                    Log.d("Laird iBeacon", "found");
                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);

                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.OLD_UUID_KEY, String.valueOf(UUIDHex));
                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.OLD_MAJOR_KEY, String.valueOf(major));
                    Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.OLD_MINOR_KEY, String.valueOf(minor));

                    if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS) || msg.contains(ContactFragment.TEST_UUID)) {
                        bleService.sendPushNotification();
                    }
                }
            }
        };
    }

    @SuppressLint("NewApi")
    public BLEHelper(final LocationBroadcastServiceNew LocationbleService, final boolean isSetting) {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {

                if (ActivityCompat.checkSelfPermission(LocationbleService, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, handle accordingly
                    return;
                }

                String msg = "";
                IsFirst = isSetting;

                for (byte b : scanRecord)
                    msg += String.format("%02x ", b);

                msg = msg.replaceAll("\\s+", "");
                ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
                ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();

                int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;
                String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));

                if (IsFirst) {
                    if (UUIDHex.equalsIgnoreCase(Constants.NEW_UUID)) {
                        IsFirst = false;
                        Log.d("tigerlight", "found");
                        int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                        int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);

                        Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.UUID_KEY, String.valueOf(UUIDHex));
                        Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MAJOR_KEY, String.valueOf(major));
                        Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MINOR_KEY, String.valueOf(minor));

                        if (getActivity() != null) {
                            LocationbleService.sendPushNotification();
                            getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                        } else {
                            Log.e(TAG, "getActivity() = null");
                        }
                    }
                }

                if (IsFirst) {
                    if (getActivity() != null && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (UUIDHex.equalsIgnoreCase(Constants.OLD_UUID) && Constants.LAIRD_BEACON_LABEL.equals(device.getName())) {
                        IsFirst = false;
                        Preference.getInstance().mSharedPreferences.getString("IsSecond", "true");
                        Log.d("tigerlight", "found");
                        int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                        int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);

                        Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.UUID_KEY, String.valueOf(UUIDHex));
                        Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MAJOR_KEY, String.valueOf(major));
                        Preference.getInstance().savePreferenceData(Constants.Preferences.Keys.MINOR_KEY, String.valueOf(minor));

                        if (getActivity() != null) {
                            LocationbleService.sendPushNotification();
                            getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                        } else {
                            Log.e(TAG, "getActivity() = null");
                        }
                    }
                }
            }
        };
    }

    public BluetoothAdapter.LeScanCallback getmLeScanCallback() {
        return mLeScanCallback;
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
}
