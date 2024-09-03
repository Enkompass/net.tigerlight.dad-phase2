package net.tigerlight.dad.webservices;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class PushAlertWorker extends Worker {
    private static final String TAG = "LocationUpdateWorker";
    private Context mContext;

    public PushAlertWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the input data
        double latitude = getInputData().getDouble("latitude", 0.0);
        double longitude = getInputData().getDouble("longitude", 0.0);
        String timezoneId = getInputData().getString("timezoneId");
        int accuracy = getInputData().getInt("accuracy", 0);

        // Perform the location update
        boolean success = makeServiceCall(latitude, longitude, timezoneId, accuracy);

        // Return the result
        return success ? Result.success() : Result.failure();
    }

    private boolean makeServiceCall(double latitude, double longitude, String timezoneId, int accuracy) {
        // Your logic to update the location
        Log.d(TAG, "Sending push alert: lat=" + latitude + ", long=" + longitude);
        // Simulate network call or database operation
        WsCallSendDanger wsCall = new WsCallSendDanger(mContext);
        wsCall.executeService(latitude, longitude, timezoneId, accuracy);
        return wsCall.isSuccess();
    }
}