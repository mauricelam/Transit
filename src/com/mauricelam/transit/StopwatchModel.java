package com.mauricelam.transit;

import android.content.Context;
import android.util.Log;
import include.GeoPoint;
import include.Helper;

import java.util.Date;
import java.util.List;

public class StopwatchModel {
	private static final String TAG = "Transit StopwatchModel";

	private Context context;
	private Date update;
	private Stop stop;
    private Route[] routes;

    private static StopWatchDB db = new StopWatchDB(TransitApplication.getContext());

	public StopwatchModel(Context context, int stopCode, String stopName) {
		this(context, new Stop(stopName, stopCode));
	}

	public StopwatchModel(Context context, Stop stop) {
		this.stop = stop;
		this.context = context.getApplicationContext();
	}

	public static StopwatchModel copy(StopwatchModel other) {
		if (other == null)
			return null;
		if (other.routes == null)
			return null;
		StopwatchModel model = new StopwatchModel(other.context, other.stop);
		model.update = other.update;
		int otherLength = other.routes.length;
		model.routes = new Route[otherLength];
		System.arraycopy(other.routes, 0, model.routes, 0, otherLength);
		return model;
	}

	public static StopwatchModel createModelFromPack(Context context, int id) {
		StopwatchModel model = new StopwatchModel(context, -1, "");
		model.reloadFromPack(id);
		return model;
	}

	public void reloadFromPack(int id) {
        StopWatchDB.CardEntry entry = db.getCard(id);
        if (entry != null) {
            setStop(entry.stop);

            List<Route> routesList = db.getRoutes(entry.stop.getCode());
            Route[] routes = routesList.toArray(new Route[routesList.size()]);

            GeoPoint location = entry.stop.getLocation();
            loadInfo(routes, entry.update, location.getLatitudeE6(), location.getLongitudeE6());
        }
	}

    public void loadInfo(Route[] routes, Date update, int lat, int lng) {
        this.routes = routes;
        this.update = update;
        this.stop.setLocation(lat, lng);
    }

	public static void setStop(Context context, int id, String stopName, int stopCode,
			String referrer, GeoPoint location) {
//        db.clearRoutes(); // clear old data
        Stop stop = new Stop(stopName, "", stopCode, location);
        db.setCard(id, stop, null, null); // only sets the name and the code, to get the process started
        new ScheduleTask(context, stopCode, referrer, id);
        commitRefreshData(context, id, referrer, 0);
        Updater.setNeedsRefresh(context);
	}

	public static void commitRefreshData(Context context, int id, long timeTolerance) {
		commitRefreshData(context, id, null, timeTolerance);
	}

    /**
     * Commits refresh data to UpdateAggregator so it gets updated in the next update run.
     * @param context Context of the application.
     * @param id Index of the card.
     * @param referrer String used to search this stop, or null if not applicable.
     * @param timeTolerance The minimum time since last update before committing again.
     */
	public static void commitRefreshData(Context context, int id, String referrer, long timeTolerance) {
		// Log.v(TAG, "static refresh: " + id);
        StopWatchDB.CardEntry entry = db.getCard(id);
        if (entry != null) {
            long update = entry.update.getTime();
            if (update + timeTolerance <= new Date().getTime()) {
                new StopwatchUpdateTask(context, entry.stop.getCode(), referrer, id);
            } else {
                Log.w(TAG, "Model recently updated");
            }

            // Update schedule if outdated.
            Date scheduleUpdate = entry.scheduleUpdate;
            if (!Helper.toSQLDateString(scheduleUpdate, "America/Chicago").equals(Helper.toSQLDateString(new Date(), "America/Chicago"))) {
                // Tomorrow is another day
                new ScheduleTask(context, entry.stop.getCode(), referrer, id);
            }
        }
	}

    public static void saveSchedule(int id, Route[] routes, Stop stop) {
//        Log.v(TAG, "Saving schedule");
//        db.clearExpiredRoutes();
        db.updateSchedule(stop.getCode(), routes);
        db.setCard(id, stop, new Date(), new Date());
    }

	public static void saveUpdate(int id, Route[] routes, Stop stop) {
//		Log.v(TAG, "Saving update");
        db.clearExpiredRoutes();
        db.updateRoutes(stop.getCode(), routes);
        db.cardUpdated(id);
	}

	public void packup(int id) {
		if (!this.isValid()) {
            Log.w(TAG, "Model is invalid, cannot packup. ");
			return;
        }
//        db.clearRoutes(id); // clear old stop data
        db.setCard(id, stop, null, null);
        saveUpdate(id, routes, stop);
	}

	public static void removeFromPref(int id) {
        db.removeCard(id);
	}

	public boolean isValid() {
		return routes != null && update != null && stop != null && stop.getCode() != -1;
	}

	public void setStop(Stop stop) {
		this.stop = stop;
	}

	public Date getUpdateTime() {
		return update;
	}

	public Route[] getRoutes() {
		return routes;
	}

	public GeoPoint getStopLocation() {
		return stop.getLocation();
	}

	public int getStopCode() {
		return stop.getCode();
	}

	public String getStopName() {
		return stop.getName();
	}

	public Stop getStop() {
		return stop;
	}

    private static class ScheduleTask implements UpdateAggregator.UpdateCompleteHandler {
        private int fragmentNum = -1;
        private Context context;

        public ScheduleTask(Context context, int stopCode, String referrer, int fragmentNum) {
            this.fragmentNum = fragmentNum;
            this.context = context;
            UpdateAggregator.getInstance().addSchedule(stopCode, referrer, this);
        }

        @Override
        public void onPreExecute() {
        }

        @Override
        public void onUpdateComplete(boolean success, Stop stop, Route[] routes) {
            Log.v(TAG, "Update completed, success: " + success);
            if (success && stop != null && routes != null) {
                StopwatchModel.saveSchedule(fragmentNum, routes, stop);
            }
        }
    }

    private static class StopwatchUpdateTask implements UpdateAggregator.UpdateCompleteHandler {
		private int fragmentNum = -1;
		private Context context;

		public StopwatchUpdateTask(Context context, int stopCode, String referrer, int fragmentNum) {
			this.fragmentNum = fragmentNum;
			this.context = context;
			UpdateAggregator.getInstance().addStop(stopCode, referrer, this);
		}

		@Override
		public void onPreExecute() {
		}

		@Override
		public void onUpdateComplete(boolean success, Stop stop, Route[] routes) {
			Log.v(TAG, "Update completed, success: " + success);
            if (success && stop != null && routes != null) {
                StopwatchModel.saveUpdate(fragmentNum, routes, stop);
            }
		}
	}
}
