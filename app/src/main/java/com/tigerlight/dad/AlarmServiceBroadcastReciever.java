package com.tigerlight.dad;

import static com.tigerlight.dad.util.AlarmUtils.cancelPeriodicService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tigerlight.dad.util.AlarmUtils;

/**
 * Created by indianic on 03/03/17.
 */

public class AlarmServiceBroadcastReciever extends BroadcastReceiver {

    private static final String TAG = AlarmServiceBroadcastReciever.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        cancelPeriodicService(context);
        AlarmUtils.setupPeriodicService(context);
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
