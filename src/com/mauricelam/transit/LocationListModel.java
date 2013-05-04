package com.mauricelam.transit;

import include.GeoPoint;
import android.os.AsyncTask;

import com.mauricelam.transit.stopdatabase.StopDatabase;

public class LocationListModel {
	private static final String TAG = "Transit LocationListModel";
	private LocationListDelegate delegate;
	private Stop[] stopList;

	public LocationListModel(LocationListDelegate delegate) {
		this.delegate = delegate;
	}

	public interface LocationListDelegate {
		public void resultsReady(Stop[] stops);
	}

	public void getNearbyStops(GeoPoint location) {
		new LoadStopTask().execute(location);
	}

	private void loadNearbyStops(GeoPoint location) {
		stopList = Connector.getNearbyStops(location, 20);
	}

	public void getAllStops() {
		new LoadStopTask().execute();
	}

	private class LoadStopTask extends AsyncTask<GeoPoint, Void, GeoPoint> {
		@Override
		protected GeoPoint doInBackground(GeoPoint... params) {
			if (params.length == 0 || params[0] == null) {
				StopDatabase.sharedInstance().updateFromServer();
				return null;
			} else {
				loadNearbyStops(params[0]);
				return params[0];
			}
		}

		@Override
		protected void onPostExecute(GeoPoint query) {
			delegate.resultsReady(stopList);
		}
	}
}
