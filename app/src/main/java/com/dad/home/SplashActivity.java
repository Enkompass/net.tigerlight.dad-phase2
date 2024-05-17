package com.dad.home;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dad.LocationBroadcastServiceNew;
import com.dad.R;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.util.Constant;
import com.dad.util.Preference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;


public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private final int TIME_INTERVAL = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        askNotificationPermission();
        setHandler();
    }


    /**
     * Handler to start the Runnable Interface after the delay
     * of 3000 seconds
     */
    private void setHandler() {
        handler = new Handler();
        handler.postDelayed(runnable, TIME_INTERVAL);
    }

    /**
     * Starts a Runnable Interface to switch the Splash Screen
     * to Login Screen.
     */
    final Runnable runnable = () -> {
        long time = 1000 * 3;  //For repiting 30 second
        boolean isLogin = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_LOGIN, false);

        if (isLogin) {

            Intent serviceIntent = new Intent(getApplicationContext(), LocationBroadcastServiceNew.class);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1001, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntent);

//
//                Intent serviceBle = new Intent(getApplicationContext(), BleService.class);
//                PendingIntent pendingIntentBle = PendingIntent.getService(getApplicationContext(), 1001, serviceBle, PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager alarmManagerBle = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//                alarmManagerBle.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntentBle);


//                getActivity().startService(intent);
        }
        final Intent loginIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();


    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remove the runnable set with the Handler
        // if the Activity gets destroyed in any case
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void initialiseFirebase() {
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("firebase", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        storeRegistrationId(token);
                    }
                });
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(String regId) {
        final int appVersion = getAppVersion(this);
        Preference preference = Preference.getInstance();
        preference.savePreferenceData(preference.KEY_DEVICE_TOKEN, regId);
        preference.savePreferenceData(PROPERTY_APP_VERSION, appVersion);
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initialiseFirebase();
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            // FCM SDK (and your app) can post notifications.
            initialiseFirebase();
        } else {
            // Directly ask for the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}
