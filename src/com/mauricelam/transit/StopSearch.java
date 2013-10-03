package com.mauricelam.transit;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import com.mauricelam.moreviews.VerticalLoader;
import com.mauricelam.transit.StopSearchModel.StopSearchDelegate;
import com.mauricelam.transit.stoplistitems.*;
import include.GeoPoint;
import include.Helper;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Activity for typing in and displaying search results.
 * 
 * @author Maurice Lam
 * 
 */
public class StopSearch extends FragmentActivity {
	private static final String TAG = "Transit StopSearch";
	private StopSearchModel model;
	private StopListAdapter adapter;

    private static final int EMPTY = 0;
	private static final int FILTERED = 1;
	private static final int SEARCHING = 2;
	private static final int SERVER = 4;
	private static final int DONE = 8;
    private static final int NORESULT = 16;
	private int searchState = EMPTY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stopsearch);

		Intent intent = getIntent();
		if (intent.getAction() != null) {
			if (intent.getAction().equals(Intent.ACTION_VIEW)) {
				String query = intent.getStringExtra(SearchManager.QUERY);
				if (query != null) {
					String name = StopSearchModel.getStopNameFromQuery(query);
					int code = StopSearchModel.getStopCodeFromQuery(query);
					selectStop(new Stop(name, code));
					this.finish();
				}
				return;
			} else if (intent.getAction().equals(Intent.ACTION_SEND)) {
				String platformName = intent.getStringExtra("android.intent.extra.SUBJECT");
				handleMapShare(platformName);
				return;
			}
		}

        ListView list = (ListView) findViewById(R.id.stopsearch_list);
        list.setOnItemClickListener(clickListener);
        list.setOnScrollListener(hideKeyboardOnScroll);
        inflateFooter();
        list.addFooterView(footer);
        setFooter(FOOTER_NONE);

        // to prevent a weird bug where locale is null
        Configuration config = getResources().getConfiguration();
        if (config.locale == null)
            config.locale = Locale.getDefault();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        TextView searchBox = (TextView) findViewById(R.id.stopsearch_searchbox);
        searchBox.setOnEditorActionListener(searchOnReturnKey);
        searchBox.addTextChangedListener(searchOnTheFly);

        model = (StopSearchModel) getLastCustomNonConfigurationInstance();
        if (model == null) {
            model = new StopSearchModel(modelDelegate);
            Intent intent = getIntent();
            if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
                searchBox.setText(intent.getStringExtra(SearchManager.QUERY));
                search(null);
            } else {
                model.loadNearbyStops();
                model.loadRecentStops(18);
                populateList(null);
            }
        } else {
            model.setDelegate(modelDelegate);
            populateList(null);
        }
        if (savedInstanceState != null) {
            setSearchState(savedInstanceState.getInt("searchState", EMPTY));
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return model;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("searchState", searchState);
    }

    @Override
	public boolean onSearchRequested() {
		// this is the search activity, no point to search in it
		TextView searchBox = (TextView) findViewById(R.id.stopsearch_searchbox);
		searchBox.requestFocus();
		setKeyboardVisible(true);
		return false;
	}

    private void setSearchState (int searchState) {
        this.searchState = searchState;
        switch (searchState) {
            case EMPTY:
                setFooter(FOOTER_NONE);
                break;
            case FILTERED:
                setFooter(FOOTER_NONE);
                break;
            case SEARCHING:
                populateList(null);
                setFooter(FOOTER_LOADSTOP);
                break;
            case SERVER:
                setFooter(FOOTER_LOADPLACE);
                break;
            case DONE:
                setFooter(FOOTER_GOOGLE);
                break;
            case NORESULT:
                setFooter(FOOTER_NOTFOUND);
                break;
        }
    }

	public void quickSearch() {
//        Log.d("Transit", "Quick search");
		if (getQuery().length() == 0) {
			model.clearSearch(); // remove search results if empty string
		} else {
			typeSearchHandler.removeCallbacks(searchRunnable);
			typeSearchHandler.postDelayed(searchRunnable, 800);
		}
        setSearchState(FILTERED);
		populateList(null);
	}

	public void search(View view) {
//        Log.d("Transit", "Full search");
        boolean searchSuccessful = search();
		if (!searchSuccessful) {
			populateList(null);
		}

		setKeyboardVisible(false); // hide soft keyboard
		// focus to the ListView
		findViewById(R.id.stopsearch_list).requestFocus();
	}

    private boolean search () {
        String query = getQuery();
        if (query != null && query.length() > 0 && searchState == FILTERED) {
            setSearchState(SEARCHING);
            model.searchStop(query);
            Log.d("", "auto search, query: " + query);
            return true;
        }
        return false;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.searchmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startSettings();
			return true;
		case R.id.cards:
			startCards();
			return true;
		case R.id.tripplanner:
			tripPlanner();
			return true;
		}
		return false;
	}

	public void back(View view) {
		this.finish();
	}

	private void handleMapShare(String platformName) {
		new AsyncTask<String, Void, Stop>() {

			@Override
			protected Stop doInBackground(String... params) {
				return Connector.getStopByPlatformName(params[0]);
			}

			@Override
			protected void onPostExecute(Stop result) {
				super.onPostExecute(result);
                if (result != null) {
					selectStop(result);
				} else {
					Helper.createToast(StopSearch.this, "Cannot get MTD stop", true);
				}
				StopSearch.this.finish();
			}

		}.execute(platformName);
	}

	/**
	 * Start the settings activity
	 */
	private void startSettings() {
		Intent settingsIntent = new Intent(this, Settings.class);
		startActivity(settingsIntent);
	}

	private void startCards() {
		Intent cardsIntent = new Intent(this, Cards.class);
		cardsIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(cardsIntent);
		this.finish();
	}

	/**
	 * Start trip planner (Google Maps with transit directions)
	 */
	private void tripPlanner() {
		TripPlanner.startTripPlanner(this, null);
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
			displayStop.putExtra("stopLatitude", stopLocation.getLatitudeE6());
			displayStop.putExtra("stopLongitude", stopLocation.getLongitudeE6());
		}
		displayStop.putExtra("referrer", getQuery());
		startActivity(displayStop);
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
			TextView bigText = (TextView) view.findViewById(R.id.stoplist_item_bigtext);
			TextView smallText = (TextView) view.findViewById(R.id.stoplist_item_smalltext);
			ImageView icon = (ImageView) view.findViewById(R.id.stoplist_item_icon);
			StopListItem item = getItem(position);
			icon.setImageDrawable(item.getIcon());
			icon.setContentDescription(item.getIconDescription());
			bigText.setText(item.getBigText());
			smallText.setText(item.getSmallText());
			view.setClickable(false);
			return view;
		}

		public void repopulate(ArrayList<StopListItem> items) {
			setNotifyOnChange(false);
			clear();
            for (StopListItem item : items) {
                this.add(item);
            }
			notifyDataSetChanged();
			setNotifyOnChange(true);
		}

	}

	private OnItemClickListener clickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View extendedView, int position, long id) {
			if (position < 0 || position >= adapter.getCount()) {
				Log.w(TAG, "Click item out of bounds");
				return;
			}
			StopListItem item = adapter.getItem(position);
			// StopListRecent is subclass of StopListStop
			if (item instanceof StopListStop) {
				StopListStop s = (StopListStop) item;
				selectStop(s.getStop());
			} else if (item instanceof StopListPlace) {
				StopListPlace p = (StopListPlace) item;
				GeoPoint location = p.getLocation();
				String bigText = p.getBigText();
				String smallText = p.getSmallText();
				Intent placesIntent = new Intent();
				placesIntent.putExtra("source", "placesSearch");
				if (item instanceof StopListMyLocation) {
					placesIntent.putExtra("mylocation", true);
				}
				placesIntent.putExtra("latitude", location.getLatitudeE6());
				placesIntent.putExtra("longitude", location.getLongitudeE6());
				placesIntent.putExtra("bigText", bigText);
				placesIntent.putExtra("smallText", smallText);
				placesIntent.putExtra("query", getQuery());
				MapReflector.startActivity(StopSearch.this, placesIntent,
						"com.mauricelam.transit.PlacesMap",
						"com.mauricelam.transit.LocationList");
			} else {
				Log.w(TAG, "Click on unknown item type");
			}
		}

	};

	private String getQuery() {
		TextView box = (TextView) findViewById(R.id.stopsearch_searchbox);
		return box.getText().toString();
	}

	private void setKeyboardVisible(boolean visible) {
		InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		TextView searchBox = (TextView) findViewById(R.id.stopsearch_searchbox);
		if (visible)
			in.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
		else
			in.hideSoftInputFromWindow(searchBox.getApplicationWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void populateList(String query) {
		String currentString = getQuery();
		ArrayList<StopListItem> items = new ArrayList<StopListItem>();
		if (currentString.length() == 0) {
			StopListMyLocation.addToList(items, this, MapReflector.isMapAvailable());
		}
		// items.add(new StopListMyLocation(this));
		Stop[] nearbyStops = model.getLoadedNearbyStops(currentString);
		if (nearbyStops != null)
			StopListNearby.appendToItemList(items, this, nearbyStops);
		Stop[] recentStops = model.getLoadedRecentStops(currentString);
		if (recentStops != null)
			StopListRecent.appendToItemList(items, this, recentStops);

		if (query != null && currentString.equals(query))
			currentString = null; // do not filter if from server

//		Log.d(TAG, "list filter: " + currentString);

		Stop[] stops = model.getLoadedStops(currentString);
		if (stops != null)
			StopListStop.appendToItemList(items, this, stops);
		Place[] places = model.getLoadedPlaces(currentString);
		if (places != null)
			StopListPlace.appendToItemList(items, this, places);

		// show "no result" prompt when no result is found
        if (items.size() == 0 && searchState == DONE && getQuery().length() > 0) {
            setSearchState(NORESULT);
//            setFooter(FOOTER_NOTFOUND);
        }

		setAdapterItems(items);
	}

	private void setAdapterItems(ArrayList<StopListItem> list) {
		ListView listView = (ListView) findViewById(R.id.stopsearch_list);
        if (listView == null) {
            return;
        }

		if (listView.getAdapter() == null || adapter == null) {
			adapter = new StopListAdapter(this, R.layout.stoplist_item, list);
			listView.setAdapter(adapter);
		} else {
			adapter.repopulate(list);
		}
	}

	private View footer;

	private void inflateFooter() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footer = inflater.inflate(R.layout.stoplist_footer, null);
	}

	private static final int FOOTER_NONE = 0;
	private static final int FOOTER_LOADSTOP = 1;
	private static final int FOOTER_LOADPLACE = 2;
	private static final int FOOTER_GOOGLE = 3;
	private static final int FOOTER_NOTFOUND = 4;

	private void setFooter(int state) {
		if (footer == null) {
			Log.w(TAG, "List footer is null");
			return;
		}

		VerticalLoader loader = (VerticalLoader) footer.findViewById(R.id.stoplist_loader);
		TextView loadingText = (TextView) footer.findViewById(R.id.stoplist_loading_label);
		TextView poweredBy = (TextView) footer.findViewById(R.id.stoplist_poweredby);
		ImageView googleLogo = (ImageView) footer.findViewById(R.id.stoplist_google);
		ViewStub noResultStub = (ViewStub) footer.findViewById(R.id.stoplist_noresultstub);
		View noResult = footer.findViewById(R.id.stoplist_noresult);

		int progressVisible = (state == FOOTER_LOADSTOP || state == FOOTER_LOADPLACE) ? View.VISIBLE : View.GONE;
		int googleVisible = (state == FOOTER_GOOGLE) ? View.VISIBLE : View.GONE;
		int noResultsVisible = (state == FOOTER_NOTFOUND) ? View.VISIBLE : View.GONE;
		loader.setVisibility(progressVisible);
		loadingText.setVisibility(progressVisible);
		poweredBy.setVisibility(googleVisible);
		googleLogo.setVisibility(googleVisible);
		if (noResultStub != null)
			noResultStub.setVisibility(noResultsVisible);
		if (noResult != null)
			noResult.setVisibility(noResultsVisible);

		TextView tv = (TextView) footer.findViewById(R.id.stoplist_loading_label);
		if (state == FOOTER_LOADSTOP) {
			tv.setText(this.getString(R.string.loading_stops));
		} else if (state == FOOTER_LOADPLACE) {
			tv.setText(this.getString(R.string.loading_places));
		}
	}

	private OnScrollListener hideKeyboardOnScroll = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			setKeyboardVisible(false);
			findViewById(R.id.stopsearch_list).requestFocus();
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
		}
	};
	private OnEditorActionListener searchOnReturnKey = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				search(v);
			}
			return false;
		}
	};
	private TextWatcher searchOnTheFly = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			quickSearch();
		}
	};
	private Handler typeSearchHandler = new Handler();
	private Runnable searchRunnable = new Runnable() {
		@Override
		public void run() {
			search();
		}
	};
	private StopSearchDelegate modelDelegate = new StopSearchDelegate() {

		@Override
		public void searchResultsReady(String query, Stop[] stops, boolean fromServer) {
			populateList(query);
            if (fromServer) {
                setSearchState(SERVER);
                model.searchPlaces(getQuery());
            }
		}

		@Override
		public void placesReady(String query, Place[] places) {
            setSearchState(DONE);
			populateList(query);
		}

	};
}
