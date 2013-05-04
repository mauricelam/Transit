package com.mauricelam.transit;

import include.GeoPoint;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mauricelam.transit.LocationListModel.LocationListDelegate;
import com.mauricelam.transit.stoplistitems.StopListItem;
import com.mauricelam.transit.stoplistitems.StopListStop;

public class LocationList extends Activity implements LocationListDelegate {
	private LocationListModel model;
	private ArrayList<StopListItem> items = new ArrayList<StopListItem>(0);
	private String query;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locationlist);

		Intent intent = getIntent();
		query = intent.getStringExtra("query");
		int lat = intent.getIntExtra("latitude", 0);
		int lng = intent.getIntExtra("longitude", 0);
		GeoPoint location = new GeoPoint(lat, lng);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText("Stops near " + intent.getStringExtra("bigText"));

		model = new LocationListModel(this);
		model.getNearbyStops(location);

		ListView list = (ListView) findViewById(R.id.locationlist_list);
		list.setOnItemClickListener(relayClickListener);
	}

	private void populateStopList(Stop[] stops) {
		ListView list = (ListView) findViewById(R.id.locationlist_list);
		if (stops != null)
			StopListStop.appendToItemList(items, this, stops);

		StopListAdapter adapter = new StopListAdapter(getApplicationContext(),
				R.layout.stoplist_item, items);
		list.setAdapter(adapter);
	}

	private class StopListAdapter extends ArrayAdapter<StopListItem> {

		public StopListAdapter(Context context, int textViewResourceId,
				ArrayList<StopListItem> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.stoplist_item, null);
			}
			TextView bigText = (TextView) view
					.findViewById(R.id.stoplist_item_bigtext);
			TextView smallText = (TextView) view
					.findViewById(R.id.stoplist_item_smalltext);
			ImageView icon = (ImageView) view
					.findViewById(R.id.stoplist_item_icon);
			StopListItem item = getItem(position);
			icon.setImageDrawable(item.getIcon());
			bigText.setText(item.getBigText());
			smallText.setText(item.getSmallText());
			if (item instanceof StopListStop) {
				StopListStop s = (StopListStop) item;
				view.setOnClickListener(getNormalListener(s.getStop()));
			}
			view.setClickable(false);
			return view;
		}
	}

	private OnClickListener getNormalListener(final Stop stop) {
		OnClickListener normalClickListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				selectStop(stop);
			}
		};
		return normalClickListener;
	}

	private void selectStop(Stop stop) {
		// Log.i(TAG, "Stop selected: " + code);
		Suggester.saveRecentQuery(TransitApplication.getContext(),
				stop.getName() + " (MTD" + String.format("%04d", stop.getCode()) + ")",
				"- Recently searched");

		Intent displayStop = new Intent(this, Cards.class);
		displayStop.putExtra("stopName", stop.getName());
		displayStop.putExtra("stopCode", stop.getCode());
        GeoPoint stopLocation = stop.getLocation();
		if (stopLocation != null) {
			displayStop.putExtra("stopLat",	stopLocation.getLatitudeE6());
			displayStop.putExtra("stopLng",	stopLocation.getLongitudeE6());
		}
		displayStop.putExtra("referrer", query);
		startActivity(displayStop);
	}

	// For back button to call
	public void back(View view) {
		this.finish();
	}

	private OnItemClickListener relayClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View extendedview,
				int position, long id) {
			extendedview.performClick();
		}
	};

	public void resultsReady(Stop[] stops) {
		populateStopList(stops);
		LinearLayout loadBox = (LinearLayout) findViewById(R.id.locationlist_loadingbox);
		loadBox.setVisibility(View.GONE);
	}
}
