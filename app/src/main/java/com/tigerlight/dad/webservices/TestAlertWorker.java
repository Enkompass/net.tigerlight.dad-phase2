package com.tigerlight.dad.settings.webservices;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TestAlertWorker extends Worker {
    private static final String TAG = "TestAlertWorker";
    private Context mContext;

    public TestAlertWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Perform the location update
        boolean success = makeServiceCall();

        // Return the result
        return success ? Result.success() : Result.failure();
    }

    private boolean makeServiceCall() {
        // Your logic to update the location
        Log.d(TAG, "Sending test alert");
        // Simulate network call or database operation
        WsCallDADTest wsCall = new WsCallDADTest(mContext);
        wsCall.executeService();
        return wsCall.isSuccess();
    }
}