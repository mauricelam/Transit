package com.mauricelam.transit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

public class Updater extends BroadcastReceiver {
	private static final String TAG = "Transit Updater";
	static boolean inBackground = false;
    private static PendingIntent pi;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Update broadcast received");
		Log.d(TAG, "Update is forced: " + intent.getBooleanExtra("forced", false));
		boolean noConnectivity = intent.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		boolean forced = intent.getBooleanExtra("forced", false);
		if (forced || !noConnectivity) {
			Intent serviceIntent = new Intent(context, UpdateService.class);
			serviceIntent.putExtra("forced", forced);
			context.startService(serviceIntent);
			// updateModel(forced);
		} else {
			Log.w(TAG, "No connectivity, update aborted");
			Intent broadcastIntent = getStatusIntent(UpdateService.UPDATEERROR);
			broadcastIntent.putExtra("forced", forced);
			context.sendBroadcast(broadcastIntent);
		}
	}

	private Intent getStatusIntent(int statusCode) {
		Intent broadcastIntent = new Intent(UpdateService.UPDATEDACTION);
		broadcastIntent.putExtra("updateStatus", statusCode);
		return broadcastIntent;
	}

	static void toBackground(Context context) {
		// Log.v(TAG, "to Background");
		inBackground = true;
		if (Pref.getBoolean("backgroundupdate", false)) {
			setRepeatingAlarm(context);
		} else {
			cancelRepeatingAlarm(context);
		}
	}

	static void toForeground(Context context) {
		// Log.v(TAG, "to Foreground");
		inBackground = false;
		setRepeatingAlarm(context);
	}

    static void setNeedsRefresh(Context context) {
        Intent alarmIntent = new Intent(context, Updater.class);
        alarmIntent.putExtra("fireAlarm", true);
        context.sendBroadcast(alarmIntent);
    }

	private static void setRepeatingAlarm(Context context) {
		cancelRepeatingAlarm(context);
		// Log.v(TAG, "Set repeating alarm");
		Intent alarmIntent = new Intent(context, Updater.class);
		alarmIntent.putExtra("fireAlarm", true);
//		Log.v(TAG, "New pending intent created");
		pi = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		long refreshInterval = getRefreshInterval();
//		Log.v(TAG, "refresh interval: " + refreshInterval);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (inBackground) {
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime(), refreshInterval, pi);
		} else {
			am.setRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime(), refreshInterval, pi);
		}
		Log.v(TAG, "repeating alarm set");
	}

	private static void cancelRepeatingAlarm(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
		pi = null;
		// Log.d(TAG, "Alarms cancelled");
	}

	private static long getRefreshInterval() {
		if (inBackground) {
			String bgInterval = Pref.getString("backgroundInterval", "30");
			return Integer.valueOf(bgInterval) * 60000;
		} else {
			return Pref.getInt("foregroundInterval", 1) * 60000;
		}
	}
}
