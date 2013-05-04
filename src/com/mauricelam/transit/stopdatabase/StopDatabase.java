package com.mauricelam.transit.stopdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.mauricelam.transit.Connector;
import com.mauricelam.transit.Pref;
import com.mauricelam.transit.Stop;
import com.mauricelam.transit.TransitApplication;
import include.GeoPoint;
import include.Helper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StopDatabase extends SQLiteOpenHelper {

	private static final String TAG = "StopDatabase";
	private static final String HASH_KEY = "transit_allstop_update_data_hash";
	private static final String STOPS_TABLE = "stops";
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "transit_stops";

	private static StopDatabase instance = null;

	public StopDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static StopDatabase sharedInstance() {
		if (instance == null) {
			instance = new StopDatabase(TransitApplication.getContext());
		}
		return instance;
	}

	public void addStopsFromJSON(JSONArray jArray, String hash) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		try {
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
                ContentValues cv = new ContentValues();
                cv.put("name", jObj.getString("n"));
                cv.put("code", jObj.getInt("c"));
                cv.put("query", jObj.getString("q"));
                cv.put("lat", jObj.getInt("lat"));
                cv.put("lng", jObj.getInt("lng"));
                db.insert(STOPS_TABLE, null, cv);
			}
			db.setTransactionSuccessful();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		Pref.setString(HASH_KEY, hash);
		Log.w(TAG, "add stops complete");
	}

	public void updateFromServer() {
		String hash = Pref.getString(HASH_KEY, "");
		String stopString = Connector.getAllStops(hash);
		if (stopString.equals("cache_unchanged")) {
			// cache is unchanged, no need to update
			return;
		}
		String newHash = Helper.md5(stopString);
		JSONArray stops = Helper.toJSONArray(stopString);
		if (stops != null) {
			addStopsFromJSON(stops, newHash);
		}
	}

	public Cursor getAllStops() {
		SQLiteDatabase db = this.getReadableDatabase();

		String selectQuery = "SELECT * FROM " + STOPS_TABLE;
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		return cursor;
	}

    private static float COSLAT = 0.644257f; // cos(approx latitude of Champaign)

    public Stop[] getNearby(GeoPoint location, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();

        int lat = location.getLatitudeE6(), lng = location.getLongitudeE6();
        String distance = "(lng - " + lng + ") * (lng - " + lng + ") * " + COSLAT + " + (lat - " + lat + ") * (lat - " + lat + ")";
        String selectQuery = "SELECT name, query, code, lat, lng, (" + distance + ") AS distance FROM " + STOPS_TABLE + " ORDER BY distance LIMIT " + limit;
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<Stop> stops = new ArrayList<Stop>(limit);
        if (cursor.moveToFirst()) {
            do {
                stops.add(new Stop(cursor.getString(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4)));
            } while (cursor.moveToNext());
        }
        return stops.toArray(new Stop[stops.size()]);
    }

    public List<Stop> searchStop(String query) {
        SQLiteDatabase db = this.getReadableDatabase();

        // LIKE doesn't like being in selectArgs, so put it directly in WHERE.
        Cursor cursor = db.query(STOPS_TABLE,
                new String[]{"name", "query", "code", "lat", "lng"},
                "name LIKE '%" + query + "%'",
                null,
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            List<Stop> stops = new ArrayList<Stop>(cursor.getCount());
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                stops.add(getStop(cursor));
            }
            return stops;
        }
        return new ArrayList<Stop>(0);
    }

	public static Stop getStop(Cursor cursor) {
        return new Stop(cursor.getString(cursor.getColumnIndex("name")),
                cursor.getString(cursor.getColumnIndex("query")),
                cursor.getInt(cursor.getColumnIndex("code")),
                cursor.getInt(cursor.getColumnIndex("lat")),
                cursor.getInt(cursor.getColumnIndex("lng"))
                );
	}

//	public Stop getStop(int code) {
//		SQLiteDatabase db = this.getReadableDatabase();
//
//		Cursor cursor = db.query(STOPS_TABLE,
//				new String[] { "name", "code", "query", "lat", "lng" }, "code=?",
//				new String[] { String.valueOf(code) }, null, null, null, null);
//		if (cursor != null)
//			cursor.moveToFirst();
//
//		return getStop(cursor);
//	}
//
//	public Stop getStop(String query) {
//		SQLiteDatabase db = this.getReadableDatabase();
//
//		Cursor cursor = db.query(STOPS_TABLE,
//				new String[] { "name", "code", "query", "lat", "lng" }, "query=?",
//				new String[] { String.valueOf(query) }, null, null, null, null);
//		if (cursor != null)
//			cursor.moveToFirst();
//
//		return getStop(cursor);
//	}
//
//	public Stop[] getStopsByName(String needle) {
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(
//				"SELECT name, code, query, lat, lng, (name LIKE '% ' || ? || ' %') AS score FROM "
//						+ STOPS_TABLE
//						+ " WHERE name LIKE '%' || ? || '%' ORDER BY score DESC LIMIT 30",
//				new String[] { needle, needle });
//
//		// Cursor cursor = db.query(STOPS_TABLE,
//		// new String[] { "name", "code", "query", "lat", "lng",
//		// "(name LIKE ') as score" },
//		// "name LIKE '%' || ? || '%'",
//		// new String[] { needle }, null, null, null, null);
//		if (cursor == null || cursor.getCount() == 0) {
//			Log.w(TAG, "Cursor is null");
//			return null;
//		}
//
//		cursor.moveToFirst();
//		int count = cursor.getCount();
//		Stop[] stops = new Stop[count];
//		for (int i = 0; i < count; i++, cursor.moveToNext()) {
//			stops[i] = StopDatabase.getStop(cursor);
//		}
//		cursor.close();
//
//		return stops;
//	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder createTable = new StringBuilder("CREATE TABLE ").append(STOPS_TABLE).append(
				"(");
		createTable.append("id INTEGER PRIMARY KEY").append(',');
		createTable.append("name TEXT").append(',');
		createTable.append("code INTEGER").append(',');
		createTable.append("query TEXT").append(',');
		createTable.append("lat INTEGER").append(',');
		createTable.append("lng INTEGER");
		createTable.append(")");
		db.execSQL(createTable.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + STOPS_TABLE);

		// Create tables again
		onCreate(db);
	}

}
