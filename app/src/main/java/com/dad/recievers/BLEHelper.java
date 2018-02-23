package com.dad.recievers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

import com.dad.LocationBroadcastServiceNew;
import com.dad.blework.BleService;
import com.dad.home.BaseFragment;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.fragment.ContactFragment;
import com.dad.util.Preference;

import java.util.Arrays;
import java.util.UUID;

import static com.dad.util.CheckForeground.getActivity;

public class BLEHelper {

    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    // private LeDeviceListAdapter mLeDeviceListAdapter;
    private String TAG = BLEHelper.class.getName();
    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static final UUID BATTERY_SERVICE_UUID =
            UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_LEVEL_CHARACTER_UUID =
            UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static final boolean IS_AUTO_CONNECT = true;
    public static final double magor = 0;
    public static final double minor = 0;
    public static boolean IsFirst = true;


    @SuppressLint("NewApi")
    public BLEHelper(final BaseFragment settingScreen, final boolean isFromSetting) {


        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {


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
//                                restartActivity();
                            } else {
                                ((ContactFragment) settingScreen).sendPushNotification();
                            }
                        }

                        if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
                            if (isFromSetting) {
//                                restartActivity();
//                                ((SettingScreen) settingScreen).bleFound();
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

                String msg = "";

                for (byte b : scanRecord)
                    msg += String.format("%02x ", b);

                msg = msg.replaceAll("\\s+", "");
                ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
                ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();


                // Log.v("rss", "" + String.valueOf(rssi));

                // Log.v("Device", "" + device);
                int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;

                //Log.e("serial number", "Serial Number is " + serialNumber);

                String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
                Log.d("UUID", UUIDHex);


                if (UUIDHex.equalsIgnoreCase("FD8C0AA6D40411E5AB30625662870761")) {


                    Log.d("tigerlight", "found");

                    //if (UUIDHex.equals(GELO_UUID)) {
                    // Bytes 25 and 26 of the advertisement packet represent
                    // the major value
                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                    //Log.e("Major", "Serial Number is " + major);


                    // Bytes 27 and 28 of the advertisement packet represent
                    // the minor
                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
                    Log.e("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);
                    Preference.getInstance().savePreferenceData("new_uuid", String.valueOf(UUIDHex));
                    Preference.getInstance().savePreferenceData("new_major", String.valueOf(major));
                    Preference.getInstance().savePreferenceData("new_minor", String.valueOf(minor));


                    if (msg.contains(ContactFragment.TEST_UUID) || msg.contains(ContactFragment.TEST_UUID)) {
                        bleService.sendPushNotification();
                        Log.d("ss", "2");

                    }


                }

                if (UUIDHex.equalsIgnoreCase("E2C56DB5DFFB48D2B060D0F5A71096E0")) {

                    Log.d("tigerlight", "found");
                    //if (UUIDHex.equals(GELO_UUID)) {
                    // Bytes 25 and 26 of the advertisement packet represent
                    // the major value
                    int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                    //Log.e("Major", "Serial Number is " + major);
                    // Bytes 27 and 28 of the advertisement packet represent
                    // the minor value
                    int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
                    Log.e("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);
                    Preference.getInstance().savePreferenceData("old_uuid", String.valueOf(UUIDHex));
                    Preference.getInstance().savePreferenceData("old_major", String.valueOf(major));
                    Preference.getInstance().savePreferenceData("old_minor", String.valueOf(minor));

                    if (msg.contains(ContactFragment.TEST_UUID_PREVIOUS) || msg.contains(ContactFragment.TEST_UUID_PREVIOUS)) {
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


                String msg = "";
                IsFirst = isSetting;

                for (byte b : scanRecord)
                    msg += String.format("%02x ", b);

                msg = msg.replaceAll("\\s+", "");
                ContactFragment.TEST_UUID = ContactFragment.TEST_UUID.toLowerCase();
                ContactFragment.TEST_UUID_PREVIOUS = ContactFragment.TEST_UUID_PREVIOUS.toLowerCase();


                // Log.v("rss", "" + String.valueOf(rssi));

                // Log.v("Device", "" + device);
                int serialNumber = (scanRecord[25] & 0xFF) << 24 | (scanRecord[26] & 0xFF) << 16 | (scanRecord[27] & 0xFF) << 8 | scanRecord[28] & 0xFF;

                //Log.e("serial number", "Serial Number is " + serialNumber);

                String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));
                Log.d("UUID", UUIDHex);


                if (IsFirst) {
                    if (UUIDHex.equalsIgnoreCase("FD8C0AA6D40411E5AB30625662870761")) {
                        IsFirst = false;


                        Log.d("tigerlight", "found");

                        //if (UUIDHex.equals(GELO_UUID)) {
                        // Bytes 25 and 26 of the advertisement packet represent
                        // the major value
                        int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                        //Log.e("Major", "Serial Number is " + major);


                        // Bytes 27 and 28 of the advertisement packet represent
                        // the minor
                        int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
                        Log.e("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);

                        Preference.getInstance().savePreferenceData("uuid", String.valueOf(UUIDHex));
                        Preference.getInstance().savePreferenceData("major", String.valueOf(major));
                        Preference.getInstance().savePreferenceData("minor", String.valueOf(minor));

                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));


                    }
                }

                if (IsFirst) {

                    if (UUIDHex.equalsIgnoreCase("E2C56DB5DFFB48D2B060D0F5A71096E0")) {

                        IsFirst = false;
                        Preference.getInstance().mSharedPreferences.getString("IsSecond", "true");


                        Log.d("tigerlight", "found");


                        //if (UUIDHex.equals(GELO_UUID)) {
                        // Bytes 25 and 26 of the advertisement packet represent
                        // the major value
                        int major = (scanRecord[25] << 8) | (scanRecord[26] << 0);
                        //Log.e("Major", "Serial Number is " + major);

                        // Bytes 27 and 28 of the advertisement packet represent
                        // the minor value
                        int minor = ((scanRecord[27] & 0xFF) << 8) | (scanRecord[28] & 0xFF);
                        Log.e("TAG", "device" + device + " Serial Number is " + serialNumber + " major" + major + " minor" + minor + " rssi" + rssi);

                        Preference.getInstance().savePreferenceData("uuid", String.valueOf(UUIDHex));
                        Preference.getInstance().savePreferenceData("major", String.valueOf(major));
                        Preference.getInstance().savePreferenceData("minor", String.valueOf(minor));

                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));


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
