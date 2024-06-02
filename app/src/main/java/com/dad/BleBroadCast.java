package com.dad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dad.blework.BleService;

/**
 * Created by indianic on 17/04/17.
 */

public class BleBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        long time = 30000 * 5;  //For repiting 30 second

        try {

            Intent serviceIntent = new Intent(context, BleService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 1001, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pendingIntent);
//            context.startService(serviceIntent);
        } catch (Exception e) {
            Intent serviceIntent = new Intent(context, BleService.class);
            context.startService(serviceIntent);
            Log.d("Exception", "Exception===" + e.getMessage());
        }


    }

}

