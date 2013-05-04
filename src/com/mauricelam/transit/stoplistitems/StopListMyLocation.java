package com.mauricelam.transit.stoplistitems;

import include.GeoPoint;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mauricelam.transit.Locator;
import com.mauricelam.transit.Place;
import com.mauricelam.transit.R;

public class StopListMyLocation extends StopListPlace {
	private Context context;

	public StopListMyLocation(Context context) {
		super(context, getMyPlace());
		this.context = context;
	}

	private static Place getMyPlace() {
		GeoPoint location = Locator.getLocation();
		int lat = 40110100, lng = -88227100; // default location: union
		if (location != null) {
			lat = location.getLatitudeE6();
			lng = location.getLongitudeE6();
		}
		return new Place("My Location", "See map around current location", null, lat, lng);
	}
	
	public static void addToList(ArrayList<StopListItem> list, Context context, boolean addIfNoLocation){
		if(addIfNoLocation || Locator.getLocation() != null){
			list.add(new StopListMyLocation(context));
		}
	}
	
	@Override
	public Drawable getIcon() {
		return context.getResources().getDrawable(R.drawable.icon_mylocation);
	}
}
