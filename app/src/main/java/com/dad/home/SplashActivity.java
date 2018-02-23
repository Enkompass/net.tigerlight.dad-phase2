package com.dad.home;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dad.LocationBroadcastServiceNew;
import com.dad.R;
import com.dad.gcm.RegistrationIntentService;
import com.dad.registration.activity.MainActivity;
import com.dad.registration.util.Constant;
import com.dad.util.Preference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private final int TIME_INTERVAL = 3000;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            final Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

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
    final Runnable runnable = new Runnable() {

        @Override
        public void run() {
            long time = 1000 * 3;  //For repiting 30 second
            boolean isLogin = Preference.getInstance().mSharedPreferences.getBoolean(Constant.IS_LOGIN, false);

            if (isLogin) {

                Intent serviceIntent = new Intent(getApplicationContext(), LocationBroadcastServiceNew.class);
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1001, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
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


        }
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

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d("SplashActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
