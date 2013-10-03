package com.mauricelam.transit;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;

public class UpdateService extends IntentService {
	private static final String TAG = "Transit UpdateService";

	public static final int UPDATESTARTED = 1;
	public static final int UPDATECOMPLETE = 2;
	public static final int UPDATEERROR = 3;
    public static final int UPDATENEXTSTEP = 4;
	public static final String UPDATEDACTION = "com.mauricelam.transit.actions.UPDATED";

	public UpdateService() {
		super("Transit UpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        int cardIndex = intent.getIntExtra("cardIndex", -1);
        if (cardIndex == -1) {
            boolean forced = intent.getBooleanExtra("forced", false);
            Log.v(TAG, "Update service intent received, forced: " + forced);
            updateAllModels(forced);
        } else {
            Log.v(TAG, "Update single card");
            updateSingleModel(cardIndex);
        }
	}

	private void updateAllModels(boolean forced) {
		Log.v(TAG, "Updating models through service");

		/** Wifi is not availble **/
		if (Updater.inBackground && !isWifiAvailable()) {
			Log.w(TAG, "Wifi not available. Update aborted");
			sendBroadcast(getStatusIntent(UPDATEERROR, -1, forced));
			return; // only update on WiFi
		}

		/** Wifi is available **/
		int cardCount = Pref.getInt("com.mauricelam.transit.Cards.cardCount", 0);
		long refreshInterval = getRefreshInterval();
		long timeTolerance = (forced) ? 0 : refreshInterval * 3 / 4;
		for (int i = 0; i < cardCount; i++) {
			StopwatchModel.commitRefreshData(this, i, timeTolerance);
		}
		sendBroadcast(getStatusIntent(UPDATESTARTED, -1));
		UpdateAggregator.update();
        sendBroadcast(getStatusIntent(UPDATENEXTSTEP, -1));
//        SystemClock.sleep(1000);
        UpdateAggregator.updateSchedule();
        UpdateAggregator.destroyInstance();
		sendBroadcast(getStatusIntent(UPDATECOMPLETE, -1));
	}

    private void updateSingleModel(int cardIndex) {
        StopwatchModel.commitRefreshData(this, cardIndex, 0);
        sendBroadcast(getStatusIntent(UPDATESTARTED, cardIndex));
        UpdateAggregator.update();
        sendBroadcast(getStatusIntent(UPDATENEXTSTEP, cardIndex));
        UpdateAggregator.updateSchedule();
        UpdateAggregator.destroyInstance();
        sendBroadcast(getStatusIntent(UPDATECOMPLETE, cardIndex));
    }

    private Intent getStatusIntent(int statusCode, int cardIndex) {
        return getStatusIntent(statusCode, cardIndex, false);
    }

	private Intent getStatusIntent(int statusCode, int cardIndex, boolean forced) {
		Intent broadcastIntent = new Intent(UPDATEDACTION);
		broadcastIntent.putExtra("updateStatus", statusCode);
        broadcastIntent.putExtra("cardIndex", cardIndex);
        broadcastIntent.putExtra("forced", forced);
		return broadcastIntent;
	}

	private boolean isWifiAvailable() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	private static long getRefreshInterval() {
		return Pref.getInt("foregroundInterval", 1) * 60000;
	}

}
