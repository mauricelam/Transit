package com.mauricelam.transit.stoplistitems;

import include.GeoPoint;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mauricelam.transit.Place;
import com.mauricelam.transit.R;

public class StopListPlace implements StopListItem {
	private Context context;
	private String title;
	private String street;
	private GeoPoint location;

	public StopListPlace(Context context, Place place) {
		this.context = context;
		title = place.getTitle();
		street = place.getStreet();
		location = place.getLocation();
	}

	@Override
	public Drawable getIcon() {
		return context.getResources().getDrawable(R.drawable.icon_place);
	}

	@Override
	public String getBigText() {
		return title;
	}

	@Override
	public String getSmallText() {
		return street;
	}
	
	public GeoPoint getLocation(){
		return location;
	}
	
	public static void appendToItemList(ArrayList<StopListItem> list, Context context, Place[] places){
		list.ensureCapacity(list.size() + places.length);
		for(int i=0; i<places.length; i++){
			list.add(new StopListPlace(context, places[i]));
		}
	}

	@Override
	public String getIconDescription() {
		return context.getResources().getString(R.string.s_place);
	}

}
