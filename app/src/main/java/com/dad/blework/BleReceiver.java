package com.dad.blework;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import androidx.legacy.content.WakefulBroadcastReceiver;

import android.os.Build;
import android.util.Log;

public class BleReceiver extends WakefulBroadcastReceiver {

	@SuppressLint("ObsoleteSdkInt")
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			Intent myServiceIntent=new Intent(context, BleService.class);
			startWakefulService(context, myServiceIntent);
			Log.d(this.getClass().getSimpleName(), "BleReceiver.onReceive()");
		}
	}
}
