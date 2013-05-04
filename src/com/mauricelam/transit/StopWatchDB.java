package com.mauricelam.transit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import include.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: mauricelam
 * Date: 19/3/13
 * Time: 1:43 AM
 */
public class StopWatchDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "transit";
    private static final String STOPWATCH_TABLE = "stopwatch";
    private static final String CARDS_TABLE = "cards";

    StopWatchDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public List<Route> getRoutes(int stopcode) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(STOPWATCH_TABLE,
                new String[] { "name", "arrival", "trip", "realtime" },
                "stopcode=? and arrival >= ?",
                new String[] { String.valueOf(stopcode), String.valueOf(new Date().getTime()) },
                null, null, "arrival ASC", "40");


        List<Route> routes = new ArrayList<Route>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                routes.add(new Route(cursor.getString(0), new Date(cursor.getLong(1)), cursor.getString(2), cursor.getInt(3) > 0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return routes;
    }

    public void updateSchedule(int stopcode, Route[] routes) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        for (Route route : routes) {
            try {
                db.insertOrThrow(STOPWATCH_TABLE, null, routeCV(route, stopcode));
            } catch (SQLiteException e) {
                Log.w("Transit DB", e.getMessage() + "");
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void updateRoutes(int stopcode, Route[] routes) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        for (Route route : routes) {
            db.replace(STOPWATCH_TABLE, null, routeCV(route, stopcode));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

//    public void updateRoute(int cardNumber, Route route) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.replace(STOPWATCH_TABLE, null, routeCV(route, cardNumber));
//    }

    private static ContentValues routeCV(Route route, int stopcode) {
        ContentValues cv = new ContentValues();
        cv.put("stopcode", stopcode);
        cv.put("name", route.getName());
        cv.put("arrival", route.getArrival().getTime());
        cv.put("trip", route.getTrip());
        cv.put("realtime", route.isRealTime() ? 1 : 0);
        return cv;
    }

    public void clearExpiredRoutes() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(STOPWATCH_TABLE, "arrival < ?", new String[]{ String.valueOf(new Date().getTime()) });
    }

    public void clearRoutes(int stopcode) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(STOPWATCH_TABLE, "stopcode=?", new String[]{ String.valueOf(stopcode) });
    }

    // Cards methods

    public void setCard(int cardNumber, Stop stop, Date update, Date scheduleUpdated) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("cardIndex", cardNumber);
        if (update != null)
            cv.put("updated", update.getTime());
        if (scheduleUpdated != null)
            cv.put("scheduleUpdated", scheduleUpdated.getTime());
        cv.put("stopname", stop.getName());
        cv.put("stopcode", stop.getCode());
        cv.put("stopquery", stop.getQuery());

        GeoPoint location = stop.getLocation();
        if (location != null) {
            cv.put("stoplat", location.getLatitudeE6());
            cv.put("stoplng", location.getLongitudeE6());
        }

        db.replace(CARDS_TABLE, null, cv);
    }

    public void cardUpdated(int cardNumber) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("updated", new Date().getTime());

        db.update(CARDS_TABLE, cv, "cardIndex=?", new String[]{ String.valueOf(cardNumber) });
    }

    public void removeCard(int cardNumber) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(CARDS_TABLE, "cardIndex=?", new String[]{String.valueOf(cardNumber)});
    }

    public static class CardEntry {
        public Stop stop;
        public Date update;
        public Date scheduleUpdate;

        public CardEntry(Stop stop, Date update, Date scheduleUpdate) {
            this.stop = stop;
            this.update = update;
            this.scheduleUpdate = scheduleUpdate;
        }

        public CardEntry(Stop stop, long update, long scheduleUpdate) {
            this(stop, new Date(update), new Date(scheduleUpdate));
        }
    }

    public CardEntry getCard(int cardNumber) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(CARDS_TABLE,
                new String[] { "updated", "stopname", "stopcode", "stopquery", "stoplat", "stoplng", "scheduleUpdated" },
                "cardIndex=?",
                new String[] { String.valueOf(cardNumber) },
                null, null, null, null);

        try {
            if (cursor.moveToFirst()) {
                Stop stop = new Stop(cursor.getString(1), cursor.getString(3), cursor.getInt(2), cursor.getInt(4), cursor.getInt(5));
                long updated = cursor.getLong(0);
                long scheduleUpdated = cursor.getLong(6);
                return new CardEntry(stop, updated, scheduleUpdated);
            }
        } catch (NullPointerException e) {
            return null;
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_STOPWATCH_SQL = "CREATE TABLE " + STOPWATCH_TABLE + " (" +
                "id INTEGER PRIMARY KEY, " +
                "trip TEXT, " +
                "stopcode INTEGER, " +
                "name TEXT, " +
                "arrival INTEGER, " +
                "realtime INTEGER, " +
                "UNIQUE(stopcode, trip))";
        db.execSQL(CREATE_STOPWATCH_SQL);

        final String CREATE_CARDS_SQL = "CREATE TABLE " + CARDS_TABLE + " (" +
                "id INTEGER PRIMARY KEY, " +
                "cardIndex INTEGER UNIQUE, " +
                "updated INTEGER, " +
                "scheduleUpdated INTEGER, " +
                "stopname TEXT, " +
                "stopcode INTEGER, " +
                "stopquery TEXT, " +
                "stoplat INTEGER, " +
                "stoplng INTEGER)";
        db.execSQL(CREATE_CARDS_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + STOPWATCH_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CARDS_TABLE);
        onCreate(db);
    }

    @Override
    public void close() {
        super.close();
        Log.d("Transit db", "close!");
    }
}
