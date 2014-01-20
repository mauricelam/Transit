package com.mauricelam.transit;

import android.util.Log;
import include.GeoPoint;
import include.Helper;
import include.Http;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

public class Connector {
	private static final String TAG = "Transit Connector";
    private static final int API_VERSION = 4;
    public static final String API_ADDRESS = "http://transit.mauricelam.com/mtd" + API_VERSION + "/";
    public static final String DEPARTURES_ADDRESS = "http://transitdepartures.mauricelam.com/mtd" + API_VERSION + "/";

	// The number of trials before using fallback server
	private static final int FALLBACK_TRIALS = 3;
	// Counter for the number of failures
	private static int numberOfFailure = 0;
	// When to stop using the fallback and try the main server again
	private static long fallbackExpire = 0;
	// The duration to use the fallback server when connection to main server
	// failed. In milliseconds.
	private static final long FALLBACKDURATION = 3600000;

	/**
	 * Call this when the alternate connection is successful. Should only be
	 * called by Helper.
	 */
	private static void alternateSuccess() {
		if (fallbackExpire != 0 && new Date().getTime() > fallbackExpire) {
			fallbackExpire = 0;
			numberOfFailure = 0;
			Log.w(TAG, "Normal server available, use normal server");
			return;
		}
		numberOfFailure++;
		if (numberOfFailure >= FALLBACK_TRIALS) {
			Log.w(TAG, "Use fallback server for 1 hour");
			fallbackExpire = new Date().getTime() + FALLBACKDURATION;
		}
	}

	public static String getAllStops(String hash) {
		String address = "allstops.php";
		String params = "hash=" + hash + "&source=map";
		return Http.restString(address, params);
	}

    public static JSONObject getSchedule(int stopcode, String refererrer) {
        String address = "getschedule.php";
        String params = "c=" + stopcode + "&r=" + refererrer;
        return Http.restJSONObject(address, params);
    }

	static Stop[] getNearbyStops(GeoPoint location, int limit) {
		String address = "getnearby.php";
		String params = "lat=" + location.getLatitudeE6() + "&lng=" + location.getLongitudeE6()
				+ "&limit=" + limit;
		JSONArray jArray = Http.restJSONArray(address, params);
		if (jArray == null)
			return null;
		int arrayLength = jArray.length();
		Stop[] stops = new Stop[arrayLength];
		for (int i = 0; i < arrayLength; i++) {
			try {
				JSONObject jObj = jArray.getJSONObject(i);
				stops[i] = Stop.stopFromJSON(jObj);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return stops;
	}

	static Stop[] getStopsByName(String query) {
		String address = "getstops.php";
		String postString = "text=" + query;
		JSONArray jArray = Http.restJSONArray(address, postString);
		if (jArray == null)
			return null;
		int arrayLength = jArray.length();
		Stop[] stops = new Stop[arrayLength];
		for (int i = 0; i < arrayLength; i++) {
			try {
				JSONObject jObj = jArray.getJSONObject(i);
				stops[i] = Stop.stopFromJSON(jObj);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
            }
		}
		return stops;
	}

    static Trip getTripInfo(String tripId, Stop stop) {
        if (tripId == null) {
            return null;
        }
        String address = "trip.php";
        try {
            String params = "trip=" + URLEncoder.encode(tripId, "UTF-8") + "&stop=" + URLEncoder.encode(stop.getQuery(), "UTF-8");
            JSONObject jObj = Http.restJSONObject(address, params);
            if (jObj == null)
                return null;
            return Trip.tripFromJSON(jObj);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Unsupported encoding " + e.getMessage());
            return null;
        } catch (JSONException e) {
            Log.w(TAG, "JSON exception " + e.getMessage());
            return null;
        }
    }

	static Stop getStopByPlatformName(String platformName) {
		String address = "decodemap.php";
		try {
			String params = "text=" + URLEncoder.encode(platformName, "UTF-8");
			JSONObject jObj = Http.restJSONObject(address, params);
			if (jObj == null)
				return null;
            return Stop.stopFromJSON(jObj);
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, "Unsupported encoding");
			return null;
		}
	}

    public static class UpdateData {
        public int stopCode;
        public String referrer;

        public UpdateData(int stopCode, String referrer) {
            this.stopCode = stopCode;
            this.referrer = referrer;
        }
    }

	static JSONObject getStopInfoByCodes(ArrayList<? extends UpdateData> data) {
		if (data == null)
			return null;
		String address = "getinfo.php";
        StringBuilder codes = new StringBuilder();
        StringBuilder referrers = new StringBuilder();

        boolean firstOne = true;
        for (UpdateData datum : data) {
            if (datum != null) {
                if (!firstOne) {
                    codes.append(',');
                    referrers.append(',');
                }
                firstOne = false;
                codes.append(String.valueOf(datum.stopCode));
                if (datum.referrer != null) {
                    referrers.append(Helper.urlEncode(datum.referrer, ""));
                }
            }
        }

		String params = "c=" + codes.toString() + "&r=" + referrers.toString();
        return Http.restJSONObject(DEPARTURES_ADDRESS + address, params);
    }

	static Place[] getPlaces(String query) {
		String address = "getplaces.php";
		String postString = "place=" + query;
		JSONArray jArray = Http.restJSONArray(address, postString);
		if (jArray == null)
			return null;
		int arrayLength = jArray.length();
		Place[] stops = new Place[arrayLength];
		for (int i = 0; i < arrayLength; i++) {
			try {
				JSONObject jObj = jArray.getJSONObject(i);
				stops[i] = Place.placeFromJSON(jObj);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return stops;
	}

}
