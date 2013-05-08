package com.mauricelam.transit;

import com.mauricelam.transit.stopdatabase.StopDatabase;
import include.GeoPoint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class Locator {
	private static final String TAG = "Transit Locator";
	private static final int INTERVAL = 5; // number of minutes
	private static final int REQUESTINTERVAL = 60000;
	private LocationManager locationManager;
	private static Stop[] nearbyStops;
	private static GeoPoint location;
	private static long lastUpdate = 0;
	private static long lastRequest = 0;

	public Locator(Context context) {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void getLocationUpdate() {
		// Log.v(TAG, "Get location updates");
		if (Pref.getBoolean("gps", true)) {
			if (System.currentTimeMillis() - lastUpdate < INTERVAL * 60000
					|| System.currentTimeMillis() - lastRequest < REQUESTINTERVAL) {
				Log.d("Transit Locator", "Interval not reached yet, no need to update");
				return;
			}
			// suggest through best fix if nothing is available yet
			if (nearbyStops == null) {
				GeoPoint bestFix = locationToGeoPoint(getBestHistory());
				if (bestFix != null) {
					suggestStops(bestFix);
				} else
					Log.w(TAG, "best fix is null");
			}
			try {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
						locationListener);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "Network not found?");
			}
			lastRequest = System.currentTimeMillis();
		}
	}

	private void suggestStops(GeoPoint location) {
		if (location != null) {
			if (!location.equals(Locator.location)) {
				new SuggestTask().execute(location);
				Locator.location = location;
			} else {
				Log.d(TAG, "Stops suggested at same location");
			}
		}
	}

	private class SuggestTask extends AsyncTask<GeoPoint, Void, Stop[]> {

		@Override
		protected Stop[] doInBackground(GeoPoint... locations) {
            StopDatabase db = StopDatabase.sharedInstance();
            db.updateFromServer();
            return db.getNearby(locations[0], 2);
		}

		@Override
		protected void onPostExecute(Stop[] nearbyStops) {
			if (nearbyStops == null || nearbyStops.length == 0) {
				Log.w(TAG, "No stops found nearby");
				return;
			}
			Locator.nearbyStops = nearbyStops;
			super.onPostExecute(nearbyStops);
		}

	}

	public static Stop[] getNearbyStops() {
		return nearbyStops;
	}

	public static GeoPoint getLocation() {
		return location;
	}

	private GeoPoint locationToGeoPoint(Location l) {
		if (l == null)
			return null;
		int lat = (int) (l.getLatitude() * 1E6);
		int lng = (int) (l.getLongitude() * 1E6);
		return new GeoPoint(lat, lng);
	}

	private Location getBestHistory() {
		Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location netLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (gpsLocation == null)
			return netLocation;
		if (netLocation == null)
			return gpsLocation;
		if (System.currentTimeMillis() - gpsLocation.getTime() < 300000) {
			return gpsLocation;
		} else {
			return netLocation;
		}
	}

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			suggestStops(point);
			locationManager.removeUpdates(this);
			lastUpdate = System.currentTimeMillis();
		}
	};
}
