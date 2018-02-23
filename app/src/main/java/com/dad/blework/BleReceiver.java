package com.dad.blework;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BleReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myServiceIntent=new Intent(context, BleService.class);
		startWakefulService(context, myServiceIntent);
		System.out.println("BleReceiver.onReceive()");
	}
}
