package com.mauricelam.transit;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class UpdateService extends IntentService {
	private static final String TAG = "Transit UpdateService";

	public static final int UPDATESTARTED = 1;
	public static final int UPDATECOMPLETE = 2;
	public static final int UPDATEERROR = 3;
	public static final String UPDATEDACTION = "com.mauricelam.transit.actions.UPDATED";

	public UpdateService() {
		super("Transit UpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean forced = intent.getBooleanExtra("forced", false);
        Log.v(TAG, "Update service intent received, forced: " + forced);
        updateModel(forced);
	}

	private void updateModel(boolean forced) {
		Log.v(TAG, "Updating models");

        Log.e(TAG, "Update model through service");

		/** Wifi is not availble **/
		if (Updater.inBackground && !isWifiAvailable()) {
			Log.w(TAG, "Wifi not available. Update aborted");
			Intent intent = getStatusIntent(UPDATEERROR);
			intent.putExtra("forced", forced);
			sendBroadcast(intent);
			return; // only update on WiFi
		}

		/** Wifi is available **/
		int cardCount = Pref.getInt("com.mauricelam.transit.Cards.cardCount", 0);
		long refreshInterval = getRefreshInterval();
		long timeTolerance = (forced) ? 0 : refreshInterval / 2;
		for (int i = 0; i < cardCount; i++) {
			StopwatchModel.commitRefreshData(this, i, timeTolerance);
		}
		sendBroadcast(getStatusIntent(UPDATESTARTED));
		UpdateAggregator.update();
		sendBroadcast(getStatusIntent(UPDATECOMPLETE));
	}

	private Intent getStatusIntent(int statusCode) {
		Intent broadcastIntent = new Intent(UPDATEDACTION);
		broadcastIntent.putExtra("updateStatus", statusCode);
		return broadcastIntent;
	}

	private boolean isWifiAvailable() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	private static long getRefreshInterval() {
		if (Updater.inBackground) {
			String bgInterval = Pref.getString("backgroundInterval", "30");
			return Integer.valueOf(bgInterval) * 60000;
		} else {
			return Pref.getInt("foregroundInterval", 1) * 60000;
		}
	}

}
