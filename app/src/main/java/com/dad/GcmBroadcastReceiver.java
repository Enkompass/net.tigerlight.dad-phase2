package com.dad;

import com.dad.registration.activity.MainActivity;
import com.dad.registration.fragment.AlertDetailFragment;
import com.dad.registration.util.Constant;
import com.dad.util.CheckForeground;
import com.dad.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import static com.dad.registration.fragment.AlertFragment.jsonobjectToChange;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    /*
      Sample intent data:
      Bundle[
  {
    google.sent_time=1523459319099,
    google.ttl=3600,
    gcm.notification.alert=Test User 1 is in Danger at 123 Main St, City Name, ST 12345, USA http://maps.google.com/?q=34.77957,-119.0335347&zoom=17 estimated accuracy is 20 meters.,
    gcm.notification.badge=1,
    gcm.notification.sound=default,
    from=32989397760,
    google.message_id=0:1523459319105669%230ce0ddf9fd7ecd,
    gcm.notification.data={"datetime":null,"alertType":"0","address":"123 Main St, City Name, City Name, ST 12345, USA","phone":"1115551212","latitude":"34.77957","testStatus":"false","userid":"1234","email":"TestUser1@test.com","longitude":"-119.0335347","username":"Test User 1","status":"0"}}]

     */
    private final String TAG_USER_NAME = "username";
    private String data = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (CheckForeground.isInForeGround() && !CheckForeground.isThreatScreenVisible()) {
            updateInFront(context, intent);
//            showNotification(context, intent);
            return;
        } else {

            showNotification(context, intent);
        }

        //        if (CheckForeground.isInForeGround() && !CheckForeground.isThreatScreenVisible()) {
//            updateInFront(context, intent);
//            return;
//        }
//        showNotification(context, intent);


    }

    private void updateInFront(Context context, Intent intent) {
//        int alertCount = Preference.getInstance().mSharedPreferences.getInt("alert_count", 0);
//        alertCount += 1;
//        Preference.getInstance().savePreferenceData("alert_count", alertCount);

        Bundle extras = intent.getExtras();
        data = extras.getString("gcm.notification.data");
        if (data == null) {
            data = extras.getString("message");
        }
        if (data == null) {
            data = extras.getString("gcm.notification.alert");
        }
        if (data == null) {
            return;
        }
        try {
            jsonobjectToChange = null;
            jsonobjectToChange = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final AlertDetailFragment alertDetailFragment = new AlertDetailFragment();
        final Bundle bundle = new Bundle();
        final String jsonObject = jsonobjectToChange.toString();
        bundle.putString(Constant.JSON_OBJECT, jsonObject);
        alertDetailFragment.setArguments(bundle);
        CheckForeground.getActivity().getFragmentManager()
                .beginTransaction().add(
                R.id.activity_registartion_fl_container, alertDetailFragment, alertDetailFragment.getClass().getSimpleName())
                .addToBackStack(alertDetailFragment.getClass().getSimpleName()).commit();
        //addFragment(new AlertDetailFragment());

        // Intent intent5 = new Intent("parth");
        //LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);


    }

    private void showNotification(Context context, Intent intentData) {
//        int alertCount = Preference.getInstance().mSharedPreferences.getInt("alert_count", 0);
//        alertCount += 1;
//        Preference.getInstance().savePreferenceData("alert_count", alertCount);

        Bundle extras = intentData.getExtras();
        data = extras.getString("gcm.notification.data");
        //String message = extras.getString("message");
        String sound = extras.getString("gcm.notification.sound");

        if (data == null) {
            return;
        }
        String userName = "";
        String safeDangerString = " is in danger!";
        try {
            jsonobjectToChange = null;
            jsonobjectToChange = new JSONObject(data);
            userName = jsonobjectToChange.optString(TAG_USER_NAME);
            if (jsonobjectToChange.optInt("status") == 1) {
                safeDangerString = userName + " is safe.";
            } else {
                safeDangerString = userName + " is in danger!";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String jsonObject = jsonobjectToChange.toString();
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Constant.JSON_OBJECT, jsonObject);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("D.A.D.")
                .setContentText(safeDangerString);
        mBuilder.setContentIntent(contentIntent);

        if (!"default".equals(sound)) {
            mBuilder.setSound(Uri.parse(("android.resource://" + context.getPackageName() + "/" + Util.getResourceId(context, sound))));
        } else {
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        }

        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }
}


