package com.mauricelam.transit;

import android.support.v4.app.FragmentManager;
import include.AutowidthTextView;
import include.GeoPoint;
import include.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mauricelam.moreviews.StopMapImageView;

public class Card extends ListFragment implements CardModel.CardDelegate {
	/**
	 * the frequency in which the list is reloaded. Note that this is different
	 * from update frequency in that there is no data pulled from server for
	 * this. In terms of milliseconds
	 */
	private static final int RELOAD_FREQUENCY = 30000;
	private static final String TAG = "Transit Card";
	private CardModel cardModel;

	private Handler mHandler = new Handler();

	/**
	 * Context is required because fragment might be detached from activity This
	 * is the activity context. Do not leak beyond the activity lifecycle.
	 */
	private Context context;
	private View root;

	private RouteAdapter adapter;

	private ListView list;

	// Not good programming style to access Cards, which is an ancestor, here.
	private static CardModel retrieveModel(int position) {
		if (Cards.cardsArray == null) {
			Log.e(TAG, "cardsArray is null");
			return null;
		}
		if (position < 0 || position >= Cards.cardsArray.size()) {
			Log.e(TAG, "position out of bounds, pos: " + position);
			return null;
		}
		return Cards.cardsArray.get(position);
	}

	/**
	 * This empty constructor is required for Fragment re-instantiation by the
	 * Fragments framework
	 */
	public Card() {
		super();
		// Log.v(TAG, "Card constructor");
	}

	/**
	 * Factory method for creating a new Card instance. Use this instead of the
	 * constructor.
	 * 
	 * @param num The index of this card
	 * @return The newly created instance of Card
	 */
	public static Card newInstance(int num) {
		Card card = new Card();
		Bundle args = new Bundle();
		args.putInt("fragmentNumber", num);
		card.setArguments(args);
		return card;
	}

	@Override
	public void onAttach(Activity activity) {
		// Log.v(TAG, "card attach");
		super.onAttach(activity);
		this.context = activity;
	}

	private ViewGroup listHeader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Log.d("card", "create view");
		root = inflater.inflate(R.layout.card, container, false);
		// Cannot use getListView() yet. It's not yet populated
		list = (ListView) root.findViewById(android.R.id.list);
		listHeader = (ViewGroup) inflater.inflate(R.layout.cardmap, list, false);
		list.addHeaderView(listHeader, null, false);
		return root;
	}

	private int oldWidth, oldHeight;

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int measuredHeight = view.getMeasuredHeight();
				int measuredWidth = view.getMeasuredWidth();
				if (measuredHeight != oldHeight || measuredWidth != oldWidth) {
					setMapHeightRatio();
					oldWidth = measuredWidth;
					oldHeight = measuredHeight;
				}
			}
		});
	}

	@Override
	public void onStart() {
		// Log.v(TAG, "Card onStart");
		super.onStart();
		if (cardModel == null) {
			int fragmentNumber = this.getArguments().getInt("fragmentNumber");
			reloadModel(fragmentNumber);
		}
		reloadAllViewStates();
		if (cardModel != null) {
			reloadList(cardModel.getModel());
		}
	}

	@Override
	public void onStop() {
		// Log.v(TAG, "Card onStop");
		super.onStop();
		if (cardModel != null) {
			// free memory, since cardModel will likely stick around
			cardModel.setDelegate(null);
			cardModel = null;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// StopMapImageView map = (StopMapImageView)
		// root.findViewById(R.id.map);
		// int visibility = (Pref.getBoolean(this.context, "showmaps", true)) ?
		// View.VISIBLE
		// : View.GONE;
		// map.setVisibility(visibility);
		mHandler.removeCallbacks(updateTask);
		mHandler.postDelayed(updateTask, RELOAD_FREQUENCY);
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(updateTask);
	}

	/**
	 * Updates the listview in the activity with new information from model
	 */
	@Override
	public void reloadList(StopwatchModel model) {
		if (model == null || root == null) {
			return;
		}
		Log.v(TAG, "Reload list");
		AutowidthTextView textview = (AutowidthTextView) root.findViewById(R.id.main_stopname);
		textview.setText(model.getStopName());

		Route[] tempRoutes = model.getRoutes();
		if (tempRoutes == null || tempRoutes.length == 0)
			tempRoutes = new Route[1];

		if (getListAdapter() == null) {
			adapter = new RouteAdapter(context, R.layout.routelist_item, tempRoutes);
			setListAdapter(adapter);
		} else {
			adapter.repopulate(tempRoutes);
		}
		list.setOnItemClickListener(alarmListener);

		if (defaultSelector == null)
			defaultSelector = list.getSelector();
		Drawable selector = (Pref.getBoolean("alarm", true)) ? defaultSelector : new ColorDrawable(0);
		list.setSelector(selector);

		reloadMap();
		refreshUpdatedOnMessage(); // get update time from model and refresh
	}

	public void update() {
		int position = getArguments().getInt("fragmentNumber");
		reloadModel(position);
		reloadAllViewStates();
		reloadList(cardModel.getModel());
	}

//	public CardModel getModel() {
//		return cardModel;
//	}

	@Override
	public void reloadMap() {
		if (root == null || cardModel == null) {
			Log.w("refresh views", "root or model is null");
			return;
		}
		ShadowMapImageView map = (ShadowMapImageView) root.findViewById(R.id.map);

		if (map == null || !Pref.getBoolean("showmaps", true)) {
			return; // don't show map at all
		}

		map.setOnClickListener(mapClickListener);

		StopwatchModel model = cardModel.getModel();
		if (model != null) {
			GeoPoint stopLocation = model.getStopLocation();
			if (stopLocation != null && !stopLocation.equals(map.getCenter())) {
				int lat = stopLocation.getLatitudeE6();
				int lng = stopLocation.getLongitudeE6();
				if (lat == 0 && lng == 0) {
					return;
				}
				map.setCenter(lat, lng);
				map.load();
			}
		}
	}

	@Override
	public void setListVisible(boolean visible) {
		refreshListVisibility();
		refreshLoadBoxVisibility();
		refreshUpdatedOnMessage();
	}

	@Override
	public void setLoadVisible(boolean visible) {
		refreshLoadWheelVisibility();
		refreshLoadBoxVisibility();
	}

	@Override
	public void setMode(int mode) {
		refreshMode();
	}

	private final OnClickListener mapClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent mapIntent = new Intent();
			Stop stop = cardModel.getModel().getStop();
            if (stop == null || stop.getLocation() == null) {
                return;
            }
			mapIntent.putExtra("source", "staticMaps");
			mapIntent.putExtra("latitude", stop.getLocation().getLatitudeE6());
			mapIntent.putExtra("longitude", stop.getLocation().getLongitudeE6());
			mapIntent.putExtra("bigText", stop.getName());
			mapIntent.putExtra("smallText", "MTD " + stop.getCodeString());
			MapReflector.startActivity(context, mapIntent, "com.mauricelam.transit.PlacesMap");
		}
	};

	private void reloadModel(int position) {
		cardModel = retrieveModel(position);
		if (cardModel != null)
			cardModel.setDelegate(this);
		else
			Log.w("Card attach", "unable to get cardModel");
	}

	// Update the list with correct minutes, does not poll the server
	private Runnable updateTask = new Runnable() {
		@Override
		public void run() {
			// Log.v(TAG, "run update task");
			reloadList(cardModel.getModel());
			mHandler.removeCallbacks(updateTask);
			mHandler.postDelayed(updateTask, RELOAD_FREQUENCY);
		}
	};

	private class RouteAdapter extends ArrayAdapter<Route> {
		private Context context;

		public RouteAdapter(Context context, int textViewResourceId, Route[] objects) {
			super(context, textViewResourceId, new ArrayList<Route>(Arrays.asList(objects)));
			this.context = context;
			setNotifyOnChange(false);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RouteListItem listItem = (RouteListItem) convertView;
			if (listItem == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				listItem = (RouteListItem) inflater.inflate(R.layout.routelist_item, null);
			}
			Route obj = getItem(position);
			if (obj != null) {
				listItem.setName(obj.getName());
				listItem.setTime(obj.getArrival());
                listItem.setRealTime(obj.isRealTime());
//                listItem.setAlpha(obj.isRealTime() ? 1.0f : 0.5f);
			} else {
				listItem.setNoRouteFound();
			}
			return listItem;
		}

		public void repopulate(Route[] items) {
			clear();
			addAllItems(items);
		}

		public void addAllItems(Route[] items) {
			for (Route item : items) {
				this.add(item);
			}
			notifyDataSetChanged();
		}

		// Note: notify on change is disabled
	}

	private boolean pressBookmark(int direction) {
		if (getActivity() != null) {
			Cards cards = (Cards) getActivity();
			int mode = cardModel.getMode();
			if (direction <= 0 && mode == CardModel.MODE.normal) {
				cards.unpinCurrentStop();
				return true;
			} else if (direction >= 0 && mode == CardModel.MODE.search) {
				cards.pinCurrentStop();
				return true;
			}
		}
		return false;
	}

	private void refreshMode() {
		int res = R.drawable.card_background;
		int bookmarkRes = R.drawable.bookmark;
		if (cardModel.getMode() == CardModel.MODE.search) {
			res = R.drawable.card_background_search;
			bookmarkRes = R.drawable.bookmark_inactive;
		}
		// View layout = root.findViewById(R.id.card_linearlayout);
		ImageView background = (ImageView) root.findViewById(R.id.background);
		background.setImageResource(res);
		ImageView bookmark = (ImageView) root.findViewById(R.id.bookmark);
		bookmark.setImageResource(bookmarkRes);
		bookmark.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				pressBookmark(0);
				return true;
			}
		});

		bookmark.setOnTouchListener(bookmarkTouchListener);
	}

	private final OnTouchListener bookmarkTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			bookmarkGesture.onTouchEvent(event);
			return false;
		}
	};

	private final GestureDetector bookmarkGesture = new GestureDetector(this.context,
			new SimpleOnGestureListener() {
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
						float distanceY) {
					if (e1 == null || e2 == null || e1.getY() == -1)
						return false;
					float scrollDist = e2.getY() - e1.getY();
					// Log.d(TAG, "scrollDist: " + scrollDist);
					int direction = (int) Math.signum(scrollDist);
					if (Math.abs(scrollDist) > 50) {
						if (pressBookmark(direction)) {
							Vibrator vibrator = (Vibrator) getActivity().getSystemService(
									Context.VIBRATOR_SERVICE);
							vibrator.vibrate(10);
						}
						e1.setLocation(-1, -1);
					}
					return true;
				}
			});

	private void reloadAllViewStates() {
		if (root == null || cardModel == null) {
			Log.w("refresh views", "root or model is null");
			return;
		}
		refreshLoadBoxVisibility();
		refreshLoadWheelVisibility();
		refreshListVisibility();
		refreshUpdatedOnMessage();
		refreshMode();
	}

	private void refreshUpdatedOnMessage() {
		TextView updateText = (TextView) root.findViewById(R.id.main_updated);
		if (!cardModel.getListVisibility()) {
			updateText.setText("");
		} else if (cardModel.getModel() != null) {
			Date update = cardModel.getModel().getUpdateTime();
			if (update == null || update.getTime() == 0) {
				updateText.setText("Update error");
				return;
			}

			Formatter formatter = new Formatter();
			String dateText = formatter.format("%tH:%tM", update, update).toString();
			formatter.close();
			String connector = dayReference(new Date(), update);
			updateText.setText("Updated " + connector + " " + dateText);
		}
	}

	private void refreshListVisibility() {
		int visibility = (cardModel.getListVisibility()) ? View.VISIBLE : View.GONE;
		if (list == null) {
			list = getListView();
		}
		list.setVisibility(visibility);
	}

	private void refreshLoadBoxVisibility() {
		int visibility = (cardModel.getLoadVisibility() && !cardModel.getListVisibility()) ? View.VISIBLE : View.GONE;
		LinearLayout box = (LinearLayout) root.findViewById(R.id.loadingbox);
		box.setVisibility(visibility);
	}

	private void refreshLoadWheelVisibility() {
		int visibility = (cardModel.getLoadVisibility()) ? View.VISIBLE : View.GONE;
		ProgressBar bar = (ProgressBar) root.findViewById(R.id.main_loadwheel);
		bar.setVisibility(visibility);
	}

	private void setMapHeightRatio() {
		StopMapImageView map = (StopMapImageView) root.findViewById(R.id.map);
		if (map == null) {
			return;
		}
		int height = root.getMeasuredHeight();
		int width = root.getMeasuredWidth();
		float ratio = (float) height / (float) width;
		ratio *= 0.2f; // the map takes up 20% of the card height
		if (ratio < 0.1f) {
			listHeader.removeView(map);
			// Log.d(TAG, "map ratio too small, removing view");
		} else {
			if (ratio > 0.3f)
				ratio = 0.3f;
			map.setHeightRatio(ratio);
		}
	}

	/**
	 * Loads the route list to show information
	 */
	private Drawable defaultSelector;

	/**
	 * Create a human-readable, relative reference for a date.
	 * 
	 * @param today The date in which the representation is relative to (the reference point). Usually its new Date().
	 * @param update The date to be displayed in relative form.
	 * @return A string denoting the date it was updated. "on" for today,
     * "yesterday" for yesterday and "Nov 18" for the rest.
	 */
	private static String dayReference(Date today, Date update) {
		Calendar todayCal = Calendar.getInstance(), updateCal = Calendar.getInstance();
		todayCal.setTime(today);
		updateCal.setTime(update);
		int diff = Math.abs(updateCal.get(Calendar.DAY_OF_YEAR)
				- todayCal.get(Calendar.DAY_OF_YEAR));
		diff = Math.abs(diff);
		if (diff < 1)
			return "on";
		else if (diff < 2)
			return "yesterday";

		Formatter formatter = new Formatter();
		String reference = formatter.format("%tb %td", update, update).toString() + ",";
		formatter.close();
		return reference;
	}

	private OnItemClickListener alarmListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (!Pref.getBoolean("alarm", true))
				return;

			if (cardModel != null && cardModel.getModel() != null) {
				Route[] routes = cardModel.getModel().getRoutes();
				if (position < 0 || position > routes.length) {
//					Helper.createToast(context, "Cannot create alarm");
					return;
				}
                Route route = routes[position - 1];
                FragmentManager fm = getFragmentManager();
                RouteDialog routeDialog = RouteDialog.newInstance(route, cardModel.getModel().getStop());
                routeDialog.show(fm, "fragment_route");
			} else {
				Log.w(TAG, "cardModel or cardModel getModel is null");
				if (context != null)
					Helper.createToast(context, "Unable to create alarm.");
			}
		}
	};

}
