package com.dad.gcm;

import com.google.android.gms.gcm.GcmListenerService;

import com.dad.R;
import com.dad.home.SplashActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose:- GCM service to receive push notification send by server and generate notification.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static ArrayList<String> arrayList;


    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("clear");
        registerReceiver(mMessageReceiver, filter);
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        final String messageTmp = data.getString("message");
        Log.d("Bundle", data + "");
        Log.d("messageTmp", messageTmp + "");

        if (data.getString("others") != null) {
            Log.d("Elsecondition", messageTmp + "");

            boolean isActivityFound = false;
            //        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

            if (services.get(0).topActivity.getPackageName().equalsIgnoreCase(this.getPackageName())) {
                isActivityFound = true;
            }

            if (!isActivityFound) {
                final String message = data.getString("message");
                if (arrayList == null) {
                    arrayList = new ArrayList<>();
                }
                arrayList.add(message);
                try {
                    if (data.getString("others") != null) {
                        JSONObject jsonObject = new JSONObject(data.getString("others"));
                        generateNotification(message, jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                final Intent intent = new Intent("notification");
                final String message = data.getString("message");
                intent.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }

    private void generateNotification(String message, JSONObject jsonObject) {
        Intent resultIntent = new Intent(this, SplashActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra("notification", true);
        if (jsonObject != null) {
            resultIntent.putExtra("redirection_type", jsonObject.optString("redirection_type"));
            resultIntent.putExtra("job_id", jsonObject.optString("job_id"));
            resultIntent.putExtra("timesheet_id", jsonObject.optString("timesheet_id"));
            resultIntent.putExtra("employer_id", jsonObject.optString("employer_id"));
            resultIntent.putExtra("nd_payment_id", jsonObject.optString("payment_id"));
            resultIntent.putExtra("nd_jobseeker_id", jsonObject.optString("jobseeker_id"));
        }
        PendingIntent piResult = PendingIntent.getActivity(this, (int) (Math.random() * 100), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /**
         * Set Custom Notification Icon according
         * to the Device's Android Versions
         */
        boolean whiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        int notification_small_icon = whiteIcon ? R.drawable.app_icon : R.drawable.app_icon;

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(notification_small_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(piResult);

        Notification.InboxStyle notification = new Notification.InboxStyle(builder)
                .setBigContentTitle("Notifications");

        // Cancel the notification after its selected
        Notification notification1 = notification.build();
        notification1.flags |= Notification.FLAG_AUTO_CANCEL;

        if (arrayList.size() > 1) {
            for (int i = arrayList.size() - 1; i >= 0; i--) {
                notification.addLine(arrayList.get(i));
                if (arrayList.size() > 5 && i == (arrayList.size() - 5)) {
                    break;
                }
            }
            if ((arrayList.size() - 5) > 0) {
                notification.setSummaryText("+" + (arrayList.size() - 5) + " " + "More");
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(121, notification1);
    }

//    private int getNotificationIcon() {
//        return R.drawable.ic_launcher;
//    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            arrayList.clear();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }
}
