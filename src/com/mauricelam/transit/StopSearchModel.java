package com.mauricelam.transit;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.mauricelam.transit.stopdatabase.StopDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for stop searching, facilitates all non-UI computations for stop
 * searching.
 * 
 * @author Maurice Lam
 * 
 */

public class StopSearchModel {
	private Stop[] recentStops;
	private Stop[] nearbyStops;
	private Stop[] stopList;
//	private String stopQuery;
	private Place[] places;
//	private String placesQuery;
	private StopSearchDelegate delegate;

	public interface StopSearchDelegate {
		public void searchResultsReady(String query, Stop[] stops);

		public void placesReady(String query, Place[] places);
	}

	public StopSearchModel(StopSearchDelegate delegate) {
		this.delegate = delegate;
	}

	/**
	 * Searches for stops given the text.
	 */
	public void searchStop(String query) {
		if (query.length() == 0)
			return;
        List<Stop> stops = StopDatabase.sharedInstance().searchStop(query);
        stopList = stops.toArray(new Stop[stops.size()]);
        delegate.searchResultsReady(query, stopList);
        new LoadStopTask().execute(query);
	}

	/**
	 * Search for places given the text
	 */
	public void searchPlaces(String query) {
		if (query.length() == 0)
			return;
		new PlacesTask().execute(query);
	}

	/**
	 * Get a list of recently accessed stops.
	 * 
	 * @return
	 */
	public Stop[] loadRecentStops(int numResults) {
		Uri uri = Uri
				.parse("content://com.mauricelam.transit.Suggester/suggestions");
		try {
			Cursor cursor = Suggester.currentProvider.query(uri, null, null,
					null, "date DESC");
			recentStops = new Stop[cursor.getCount()];
			cursor.moveToFirst();
			for (int i = 0; i < recentStops.length; i++) {
				if (i >= numResults)
					break;
				String query = cursor.getString(cursor.getColumnIndex("query"));
				String stopName = getStopNameFromQuery(query);
				int stopCode = getStopCodeFromQuery(query);
				recentStops[i] = new Stop(stopName, stopCode);
				cursor.moveToNext();
			}
			cursor.close();
		} catch (SQLiteException e) {
			Log.e("", "SQLiteException");
			e.printStackTrace();
		}
		return recentStops;
	}

	public static String getStopNameFromQuery(String query) {
		int pos = query.indexOf("(MTD");
        return query.substring(0, pos).trim();
	}

	public static int getStopCodeFromQuery(String query) {
		int pos = query.indexOf("(MTD");
		String codeString;
		try {
			codeString = query.substring(pos + 4, pos + 8);
		} catch (StringIndexOutOfBoundsException e) {
			codeString = query.substring(pos + 4);
		}
		int closepos = codeString.indexOf(")");
		if (closepos != -1) {
			codeString = codeString.substring(0, closepos);
		}
        return Integer.valueOf(codeString);
	}

	/**
	 * Get a list of nearby stops from the current GPS location
	 * 
	 * @return
	 */
	public Stop[] loadNearbyStops() {
		// should include param for numResults?
		nearbyStops = Locator.getNearbyStops();
		return nearbyStops;
	}

	public Stop[] getLoadedRecentStops(String filter) {
		if (filter == null)
			return recentStops;
		if (recentStops == null)
			return null;
		ArrayList<Stop> stops = new ArrayList<Stop>(recentStops.length);
        for (Stop recentStop : recentStops) {
            if (recentStop != null
                    && stringFilter(recentStop.getName(), filter)) {
                stops.add(recentStop);
            }
        }
		return stops.toArray(new Stop[stops.size()]);
	}

//	public Stop[] getLoadedRecentStops() {
//		return recentStops;
//	}

	public Stop[] getLoadedNearbyStops(String filter) {
		if (filter == null)
			return nearbyStops;
		if (nearbyStops == null)
			return null;
		ArrayList<Stop> stops = new ArrayList<Stop>(0);
        for (Stop nearbyStop : nearbyStops) {
            if (stringFilter(nearbyStop.getName(), filter)) {
                stops.add(nearbyStop);
            }
        }
        return stops.toArray(new Stop[stops.size()]);
	}

//	public Stop[] getLoadedNearbyStops() {
//		return nearbyStops;
//	}

	public Stop[] getLoadedStops(String filter) {
		if (filter == null)
			return stopList;
		if (stopList == null)
			return null;
		ArrayList<Stop> stops = new ArrayList<Stop>(0);
        for (Stop stop : stopList) {
            if (stringFilter(stop.getName(), filter)) {
                stops.add(stop);
            }
        }
		return stops.toArray(new Stop[stops.size()]);
	}

//	public Stop[] getLoadedStops() {
//		return stopList;
//	}

	public Place[] getLoadedPlaces(String filter) {
		if (filter == null)
			return places;
		if (places == null)
			return null;
		ArrayList<Place> output = new ArrayList<Place>(0);
        for (Place place : places) {
            try { // patch that weird (concurrency?) bug
                if (stringFilter(place.getTitle(), filter)
                        || stringFilter(place.getStreet(), filter)) {
                    output.add(place);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
		return output.toArray(new Place[output.size()]);
	}

//	public Place[] getLoadedPlaces() {
//		return places;
//	}
//
//	public String getStopQuery(){
//		return stopQuery;
//	}
//
//	public String getPlacesQuery(){
//		return placesQuery;
//	}

	public void clearSearch() {
		stopList = null;
//		stopQuery = null;
		places = null;
//		placesQuery = null;
	}

	/**
	 * 
	 * @param haysack
	 * @param needleList
	 * @return whether the haysack pass the filter
	 */
	private boolean stringFilter(String haysack, String needleList) {
		String[] words = haysack.trim().toLowerCase().split(" ");
		String[] needles = needleList.trim().toLowerCase().split(" ");
		outer:
        for (String needle : needles) {
            for (String word : words) {
                if (word.startsWith(needle))
                    continue outer;
            }
            return false;
        }
		return true;
	}

	private class LoadStopTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String escQuery = Uri.encode(params[0]);
			stopList = Connector.getStopsByName(escQuery);
//			stopList = StopDatabase.sharedInstance().getStopsByName(params[0]);
//			stopQuery = params[0];
			return params[0];
		}

		@Override
		protected void onPostExecute(String query) {
			delegate.searchResultsReady(query, stopList);
		}
	}

	private class PlacesTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String escQuery = Uri.encode(params[0]);
			places = Connector.getPlaces(escQuery);
//			placesQuery = params[0];
			return params[0];
		}

		@Override
		protected void onPostExecute(String query) {
			delegate.placesReady(query, places);
		}
	}
}
