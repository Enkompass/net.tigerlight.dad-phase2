package com.dad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by indianic on 03/03/17.
 */

public class AlarmServiceBroadcastReciever extends BroadcastReceiver {

    private static final String TAG = AlarmServiceBroadcastReciever.class.getSimpleName();

    private void setupLocationService(Context context, long INTERVAL) {
        Intent serviceIntent = new Intent(context, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                context, 0, serviceIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pendingIntent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        long time = 1000 * 30;  // For repeating every 30 seconds

        setupLocationService(context, time);
//        try {
//            Log.d(TAG, "Starting AlarmServiceBroadcastReciever");
//            Intent serviceIntent = new Intent(context, LocationBroadcastServiceNew.class);
//            PendingIntent pendingIntent = PendingIntent.getService(context, 1001, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntent);
//
//
////            context.startService(serviceIntent);
//        } catch (Exception e) {
//            Intent serviceIntent = new Intent(context, LocationBroadcastServiceNew.class);
//            context.startService(serviceIntent);
//            Log.d("Exception", "Exception===" + e.getMessage());
//        }


    }

}
