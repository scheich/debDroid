package de.mangelow.debdroid.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

	private final String TAG = "dD";
	private final boolean D = false;

	private Helper mHelper = new Helper();

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		int checkpackages = mHelper.loadIntPref(context, "checkpackages", mHelper.CHECKPACKAGES_DEFAULT);
		if(checkpackages>0) {
			if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
				if(D)Log.d(TAG, "ACTION_BOOT_COMPLETED");				
				mHelper.setRTCAlarm(context, checkpackages);
				
			}
			if (action.equals(mHelper.ACTION_CHECK_PACKAGES)) {
				if(D)Log.d(TAG, "ACTION_CHECK_PACKAGES");
				
			}
		}	
	}
}