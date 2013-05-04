package com.mauricelam.transit;

import include.GeoPoint;
import include.Helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

public class TripPlanner {

	private static final String TAG = "Transit TripPlanner";

	/**
	 * Start trip planner (Google Maps with transit directions)
	 */
	public static void startTripPlanner(Context context, GeoPoint from) {
		String mapsURL = getGoogleMapsURL(from);
		Intent tripPlannerIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(mapsURL));
		if (Pref.getBoolean("usegooglemaps", true)) {
			try {
				context.getPackageManager().getPackageInfo(
						"com.google.android.apps.maps", 0);
				tripPlannerIntent.setClassName("com.google.android.apps.maps",
						"com.google.android.maps.MapsActivity");
				Helper.createToast(context, "Select your destination");
			} catch (PackageManager.NameNotFoundException e) {
				// Log.e("Transit", e.getMessage()+"");
				Helper.createToast(context, "Could not open Google maps");
			}
		}
		context.startActivity(tripPlannerIntent);
	}

	/**
	 * Creates a google maps URL string with transit directions, starting
	 * address set to current MTD stop and date, time set to current
	 * 
	 * @return the generated google maps URL
	 */
	private static String getGoogleMapsURL(GeoPoint from) {
		String startAddress = "";
		if (from != null) {
			double lat = from.getLatitudeE6() / 1E6;
			double lng = from.getLongitudeE6() / 1E6;
			startAddress = lat + "," + lng;
		}
		String destinationAddress = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM'%2F'dd'%2F'yy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mma");
		timeFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
		dateFormat.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
		String dateString = dateFormat.format(new Date());
		String timeString = timeFormat.format(new Date());
		StringBuilder mapsURLBuilder = new StringBuilder(
				"http://maps.google.com/maps?");
		mapsURLBuilder.append("saddr=").append(startAddress);
		mapsURLBuilder.append("&daddr=").append(destinationAddress);
		mapsURLBuilder.append("&dirflg=r");
		mapsURLBuilder.append("&sll=40.10367,-88.22542");
		mapsURLBuilder.append("&date=").append(dateString);
		mapsURLBuilder.append("&time=").append(timeString);
		mapsURLBuilder.append("&near=champaign,il");
		String mapsURL = mapsURLBuilder.toString();
		Log.v(TAG, "GoogleMapsURL: " + mapsURL);
		return mapsURL;
	}

}
