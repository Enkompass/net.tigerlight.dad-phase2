package com.dad.blework;

import android.content.Context;
import android.content.Intent;
import androidx.legacy.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent myServiceIntent=new Intent(context, MyService.class);
		startWakefulService(context, myServiceIntent);
	}

}
