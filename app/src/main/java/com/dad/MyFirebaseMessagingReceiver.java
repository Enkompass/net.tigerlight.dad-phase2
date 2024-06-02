package com.dad;

import static com.dad.registration.fragment.AlertFragment.jsonobjectToChange;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.dad.registration.activity.MainActivity;
import com.dad.registration.fragment.AlertDetailFragment;
import com.dad.registration.util.Constant;
import com.dad.util.CheckForeground;
import com.dad.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingReceiver extends WakefulBroadcastReceiver {

    private final String TAG_USER_NAME = "username";
    private String data = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (CheckForeground.isInForeGround() && !CheckForeground.isThreatScreenVisible()) {
            updateInFront(context, intent);
            return;
        } else {
            showNotification(context, intent);
        }
    }

    private void updateInFront(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        data = extras.getString("gcm.notification.data");
        if (data == null) {
            data = extras.getString("message");
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

        // Your existing logic to update the UI
    }

    private void showNotification(Context context, Intent intentData) {
        Bundle extras = intentData.getExtras();
        data = extras.getString("gcm.notification.data");
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
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

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


