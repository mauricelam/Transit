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
	private Place[] places;
	private StopSearchDelegate delegate;

	public interface StopSearchDelegate {
		public void searchResultsReady(String query, Stop[] stops, boolean fromServer);

		public void placesReady(String query, Place[] places);
	}

	public StopSearchModel(StopSearchDelegate delegate) {
		this.delegate = delegate;
	}

    public void setDelegate (StopSearchDelegate delegate) {
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
        delegate.searchResultsReady(query, stopList, false);
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
		Uri uri = Uri.parse("content://com.mauricelam.transit.Suggester/suggestions");
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

	public Stop[] getLoadedNearbyStops(String filter) {
		if (filter == null)
			return nearbyStops;
		if (nearbyStops == null)
			return null;
		ArrayList<Stop> stops = new ArrayList<Stop>();
        for (Stop nearbyStop : nearbyStops) {
            if (stringFilter(nearbyStop.getName(), filter)) {
                stops.add(nearbyStop);
            }
        }
        return stops.toArray(new Stop[stops.size()]);
	}

	public Stop[] getLoadedStops(String filter) {
		if (filter == null)
			return stopList;
		if (stopList == null)
			return null;
		ArrayList<Stop> stops = new ArrayList<Stop>();
        for (Stop stop : stopList) {
            if (stringFilter(stop.getName(), filter)) {
                stops.add(stop);
            }
        }
		return stops.toArray(new Stop[stops.size()]);
	}

	public Place[] getLoadedPlaces(String filter) {
		if (filter == null)
			return places;
		if (places == null)
			return null;
		ArrayList<Place> output = new ArrayList<Place>();
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

	public void clearSearch() {
		stopList = null;
		places = null;
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
			Stop[] stops = Connector.getStopsByName(escQuery);
            if (stops != null) stopList = stops;
			return params[0];
		}

		@Override
		protected void onPostExecute(String query) {
			delegate.searchResultsReady(query, stopList, true);
		}
	}

	private class PlacesTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String escQuery = Uri.encode(params[0]);
			places = Connector.getPlaces(escQuery);
			return params[0];
		}

		@Override
		protected void onPostExecute(String query) {
			delegate.placesReady(query, places);
		}
	}
}
