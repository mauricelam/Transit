package com.mauricelam.transit;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.mauricelam.bettermaps.BetterMapView;
import com.mauricelam.bettermaps.OverlayList;
import com.mauricelam.bettermaps.OverlayList.Bound;
import com.mauricelam.bettermaps.OverlayListItem;
import com.mauricelam.moreviews.VerticalLoader;
import com.mauricelam.transit.LocationListModel.LocationListDelegate;
import com.mauricelam.transit.stopdatabase.StopDatabase;

public class PlacesMap extends MapActivity implements LocationListDelegate {
	private static final String TAG = "Transit PlacesMap";

	private LocationListModel model;
	private String query;
	private OverlayList stopList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.placesmap_debug);
		setContentView(R.layout.placesmap);

		BetterMapView mapView = getMapView();
		mapView.setBuiltInZoomControls(true);
		mapView.showMyLocation();

		Resources resources = this.getResources();
		BitmapDrawable stop = (BitmapDrawable) resources.getDrawable(R.drawable.icon_stop);
		stop = new BitmapDrawable(resources, Bitmap.createScaledBitmap(stop.getBitmap(), 50, 50,
				true));
		stopList = mapView.addOverlayList(stop, Bound.CENTER);
		stopList.setMinimumZoom(16);

		Intent intent = getIntent();
		query = intent.getStringExtra("query");
		int lat = intent.getIntExtra("latitude", 0);
		int lng = intent.getIntExtra("longitude", 0);
		setCenterPlace(mapView, intent, lat, lng);

		if (intent.getBooleanExtra("mylocation", false)) {
			// animate to my location
			mapView.centerMe(true);
		}

		model = new LocationListModel(this);
		model.getAllStops();
		VerticalLoader loader = (VerticalLoader) findViewById(R.id.ProgressBar01);
		loader.setVisibility(View.VISIBLE);
	}

	// For back button to call
	public void back(View view) {
		this.finish();
	}

	@Override
	public void resultsReady(final Stop[] stops) {
		Cursor cursor = StopDatabase.sharedInstance().getAllStops();
		int count = cursor.getCount();
		if (count == 0) {
			Log.w(TAG, "getNearby result is null");
			VerticalLoader loader = (VerticalLoader) findViewById(R.id.ProgressBar01);
			loader.setVisibility(View.GONE);
			return;
		}
		ArrayList<OverlayListItem> items = new ArrayList<OverlayListItem>(count);
		do {
			final Stop stop = StopDatabase.getStop(cursor);
			if (stop != null) {
				OverlayListItem item = new OverlayListItem(
						getGoogleGeoPoint(stop.getLocation()),
						stop.getName(), "MTD " + stop.getCodeString());
				item.setClickListener(new OverlayListItem.OnClickListener() {
					@Override
					public boolean onClick() {
						selectStop(stop.getName(), stop.getCode());
						return true;
					}
				});
				items.add(item);
			}
		} while (cursor.moveToNext());
		cursor.close();

		stopList.addOverlayList(items);
		getMapView().invalidate();
		VerticalLoader loader = (VerticalLoader) findViewById(R.id.ProgressBar01);
		loader.setVisibility(View.GONE);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}

	private BetterMapView getMapView() {
		return (BetterMapView) findViewById(R.id.mapview);
	}

	private void setCenterPlace(BetterMapView mapView, Intent intent, int lat, int lng) {
		OverlayList placesList = null;
		String source = intent.getStringExtra("source");
		if (source.equals("placesSearch")) {
			placesList = mapView.addOverlayList(R.drawable.flag, Bound.CENTERBOTTOM);
			placesList.setDrawShadow(true);
		} else if (source.equals("staticMaps")) {
			placesList = mapView.addOverlayList(R.drawable.icon_stop, Bound.CENTER);
		} else {
			Log.e(TAG, "Unrecognized source");
			return;
		}

		String bigText = intent.getStringExtra("bigText");
		String smallText = intent.getStringExtra("smallText");
		if (!intent.getBooleanExtra("mylocation", false)) {
			OverlayListItem overlayItem = new OverlayListItem(lat, lng, bigText, smallText);
			placesList.addOverlay(overlayItem);
		} // don't draw flag if showing my location

		mapView.getBetterController().setZoom(18);
		mapView.getBetterController().setCenter(new GeoPoint(lat, lng));

		TextView title = (TextView) findViewById(R.id.title);
		if (bigText != null && bigText != "") {
			title.setText(bigText);
		}
	}

	private void selectStop(String name, int code) {
		String routeName = name + " (MTD" + String.format("%04d", code) + ")";
		String suggestionType = "- Nearby stop";
		Suggester.saveRecentQuery(getApplicationContext(), routeName, suggestionType);

		Intent displayStop = new Intent(this, Cards.class);
		displayStop.putExtra("stopName", name);
		displayStop.putExtra("stopCode", code);
		displayStop.putExtra("referrer", query);
		startActivity(displayStop);
	}

	private static GeoPoint getGoogleGeoPoint(include.GeoPoint p) {
		if (p == null)
			return null;
		return new GeoPoint(p.getLatitudeE6(), p.getLongitudeE6());
	}

	/*
	 * private static include.GeoPoint getIncludeGeoPoint(GeoPoint p) { if (p ==
	 * null) return null; return new include.GeoPoint(p.getLatitudeE6(),
	 * p.getLongitudeE6()); }
	 */
}