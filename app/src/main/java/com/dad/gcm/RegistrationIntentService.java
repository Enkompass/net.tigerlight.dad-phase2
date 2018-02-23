package com.dad.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.dad.R;
import com.dad.util.Preference;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

/**
 * Purpose:-
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Service","Start");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Preference preferenceUtils = Preference.getInstance();
        try {
            final InstanceID instanceID = InstanceID.getInstance(this);
            final String gcmToken = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d("GCM", gcmToken);
            preferenceUtils.savePreferenceData(preferenceUtils.KEY_DEVICE_TOKEN, gcmToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Persist registration to third-party servers.
     * <p>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param gcmToken The new token.
     */
    private void sendRegistrationToServer(final String gcmToken) {
        // Add custom implementation, as needed.
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        thread.start();
    }
}
