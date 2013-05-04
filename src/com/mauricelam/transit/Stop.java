package com.mauricelam.transit;

import include.GeoPoint;

import org.json.JSONObject;

import android.util.Log;

public class Stop {
	private String name;
	private String query;
	private GeoPoint location;
	private int code;

	/**
	 * Default constructor to create a meaningless copy of a Stop For use with
	 * DBEntry
	 */
	public Stop() {
	}

	public Stop(String name, String query, int code, int lat, int lng) {
		this.name = name;
		this.query = query;
		this.code = code;
		this.location = new GeoPoint(lat, lng);
	}

	public Stop(String name, String query, int code, GeoPoint location) {
		this.name = name;
		this.query = query;
		this.code = code;
		this.location = location;
	}

	public Stop(String name, int code) {
		this.name = name;
		this.code = code;
		this.location = null;
	}
	
	public Stop(String name, String query, int code){
		this.name = name;
		this.code = code;
		this.query = query;
		this.location = null;
	}

	public static Stop stopFromJSON(JSONObject jObj) {
		try {
			String name = jObj.getString("n");
			String query = jObj.getString("q");
			int code = jObj.getInt("c");
			Stop stop = new Stop(name, query, code);
			if (jObj.has("lat")) {
				int lat = jObj.getInt("lat");
				int lng = jObj.getInt("lng");
				stop.setLocation(lat, lng);
			}
			return stop;
		} catch (Exception e) {
			Log.e("Transit Stop", e.getMessage());
			return null;
		}
	}

	public void setLocation(int lat, int lng) {
		this.location = new GeoPoint(lat, lng);
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}

    public String getQuery() {
        return query;
    }

	public String getCodeString() {
		return String.format("%04d", code);
	}

	public GeoPoint getLocation() {
		return location;
	}
}
