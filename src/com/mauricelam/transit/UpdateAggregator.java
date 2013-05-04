package com.mauricelam.transit;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdateAggregator {
	private static final String TAG = "Transit UpdateAggregator";
    ArrayList<UpdateRequest> data;
    ArrayList<UpdateRequest> schedules;

    private static class UpdateRequest extends Connector.UpdateData {
        public UpdateAggregator.UpdateCompleteHandler callback;

        public UpdateRequest(int stopCode, String referrer, UpdateCompleteHandler callback) {
            super(stopCode, referrer);
            this.callback = callback;
        }
    }

	public interface UpdateCompleteHandler {
		public void onPreExecute();

		public void onUpdateComplete(boolean success, Stop stop, Route[] routes);
	}

	private static UpdateAggregator instance;

	public static UpdateAggregator getInstance() {
		if (instance == null)
			instance = new UpdateAggregator();
		return instance;
	}

	public UpdateAggregator() {
		// Log.v(TAG, "Update Aggregator created");
        data = new ArrayList<UpdateRequest>();
        schedules = new ArrayList<UpdateRequest>();
	}

	public void addStop(int stopCode, String referrer, UpdateCompleteHandler callback) {
		// Log.v(TAG, "Add stop: " + stopCode + " referrer: " + referrer);
        data.add(new UpdateRequest(stopCode, referrer, callback));
    }

    public void addSchedule(int stopCode, String referrer, UpdateCompleteHandler callback) {
        schedules.add(new UpdateRequest(stopCode, referrer, callback));
    }

	public static boolean update() {
		if (instance != null) {
			instance.execute();
			return true;
		} else {
			Log.w(TAG, "update failed: instance is null");
			return false;
		}
	}

	private void destroy() {
        data = null;
		instance = null;
		// Log.i(TAG, "Update aggregator destroyed");
	}

	private void execute() {
		// pre execute
		for (UpdateRequest request : data) {
			request.callback.onPreExecute();
		}

		// do in background
        JSONObject jObj;
        if (data.size() > 0) {
            jObj = Connector.getStopInfoByCodes(data);
        } else {
            Log.w(TAG, "No stop is provided to update");
            jObj = null;
        }

        if (jObj != null) {
            // post execute
            for (UpdateRequest request : data) {
                try {
                    JSONObject stopObj = jObj.getJSONObject(String.valueOf(request.stopCode));
                    Stop stop = getStopFromJSON(stopObj);
                    Route[] routes = getRoutesFromJSON(stopObj, true);
                    request.callback.onUpdateComplete(true, stop, routes);
                } catch (Exception e) {
                    request.callback.onUpdateComplete(false, null, null);
                    e.printStackTrace();
                }
            }
        } else {
            Log.w(TAG, "jObj is null");
        }

        // schedules
        for (UpdateRequest request : schedules) {
            try {
                JSONObject scheduleObj = Connector.getSchedule(request.stopCode, request.referrer);
                if (scheduleObj != null) {
                    Stop stop = getStopFromJSON(scheduleObj);
                    Route[] routes = getRoutesFromJSON(scheduleObj, false);
                    request.callback.onUpdateComplete(true, stop, routes);
                } else {
                    request.callback.onUpdateComplete(false, null, null);
                }
            } catch (JSONException e) {
                request.callback.onUpdateComplete(false, null, null);
                e.printStackTrace();
            }
        }

		destroy();
	}

	private Stop getStopFromJSON(JSONObject jObj) throws JSONException {
        return Stop.stopFromJSON(jObj.getJSONObject("s"));
	}

	private Route[] getRoutesFromJSON(JSONObject jObj, boolean realtime) throws JSONException{
        JSONArray jArray = jObj.getJSONArray("r");
        int routeLength = jArray.length();
        Route[] routes = new Route[routeLength];
        for (int i = 0; i < routeLength; i++) {
            routes[i] = Route.routeFromJSON(jArray.getJSONObject(i), realtime);
        }
        return routes;
	}
}
