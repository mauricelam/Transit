package com.mauricelam.transit;

import include.GeoPoint;

import org.json.JSONObject;

import android.util.Log;

public class Place {
	private String title;
	private String street;
	private String city;
	private int lat, lng;
	
	public Place(String title, String street, String city, int lat, int lng) {
		this.title = title;
		this.street = street;
		this.city = city;
		this.lat = lat;
		this.lng = lng;
	}
	
	public static Place placeFromJSON(JSONObject jObj) {
		try {
			String title = jObj.getString("title");
			String street = jObj.getString("street");
			String city = jObj.getString("city");
			int lat = jObj.getInt("latitude");
			int lng = jObj.getInt("longitude");
            return new Place(title, street, city, lat, lng);
		} catch (Exception e) {
			Log.e("Transit Place", e.getMessage());
			return null;
		}
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getCity(){
		return city;
	}
	
	public GeoPoint getLocation(){
		return new GeoPoint(lat, lng);
	}
	
	public String getStreet(){
		return street;
	}
}
