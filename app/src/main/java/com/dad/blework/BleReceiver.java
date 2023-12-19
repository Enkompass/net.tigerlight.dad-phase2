package com.dad.blework;

import android.content.Context;
import android.content.Intent;
import androidx.legacy.content.WakefulBroadcastReceiver;
import android.util.Log;

public class BleReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myServiceIntent=new Intent(context, BleService.class);
		startWakefulService(context, myServiceIntent);
        Log.d(this.getClass().getSimpleName(), "BleReceiver.onReceive()");
	}
}
