package com.mauricelam.transit;

import include.GeoPoint;
import include.Helper;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mauricelam.moreviews.MenuButton;
import com.mauricelam.moreviews.PageControl;

/**
 * Cards is the activity that manages all the cards. The cards are arranged in a
 * ViewPager, a horizontal scrolling component.
 * 
 * @author Maurice Lam
 * 
 */
public class Cards extends FragmentActivity {
	private static final String TAG = "Transit Cards";

	private int cardCount = 0; // default value
	private PageControl pageControl;
	private ViewPager pager;
	private CardsAdapter adapter;
	private Locator locator;
	private BroadcastReceiver updateStatusReceiver;

	// Maximum number of cards allowed
	private static final int MAXSTOP = 10;

	// needs to be static to be accessed by background updater
	static ArrayList<CardModel> cardsArray;
	/**
	 * Number of instances of this class. Keep track to determine when we can
	 * dispose of some static values
	 */
	private static int instanceCount = 0;

	private boolean pendingSetStop = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cards);
		instanceCount++;

		restoreCardCount();
		Intent intent = getIntent();
		onNewIntent(intent); // initialize this intent

		if (cardCount == 0 && !pendingSetStop) {
			intent.putExtra("stopCode", 100);
			intent.putExtra("stopName", "Illini Union");
			onNewIntent(intent);
		}

		initializeCards();

		MenuButton.addMenuButton(this);
		initializePager();
		initializePageControl();
		updateStatusReceiver = new UpdateStatusReceiver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instanceCount--;
		if (instanceCount == 0)
			cardsArray = null; // free memory (it's static)
	}

	@Override
	protected void onPause() {
		super.onPause();
		Pref.setInt("com.mauricelam.transit.Cards.selectedCard", pager.getCurrentItem());
		Updater.toBackground(this);
		locator = null;
		unregisterReceiver(updateStatusReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Handler handler = new Handler();
		Runnable runLocator = new Runnable() {
			@Override
			public void run() {
				if (locator == null) {
					locator = new Locator(Cards.this);
					locator.getLocationUpdate();
				}
			}
		};
		handler.removeCallbacks(runLocator);
		handler.postDelayed(runLocator, 10000);

		int oldCardCount = cardCount;
		restoreCardCount();

		int selectedCard = Pref.getInt("com.mauricelam.transit.Cards.selectedCard", 0);
        selectedCard = Helper.clamp(selectedCard, 0, cardCount - 1);
		pager.setCurrentItem(selectedCard);

		restoreModels();
		refreshPageControl(oldCardCount);
		Updater.toForeground(this);

		IntentFilter filter = new IntentFilter(UpdateService.UPDATEDACTION);
		registerReceiver(updateStatusReceiver, filter);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
//		Log.v(TAG, "new intent: " + intent.toString());
		if (intent.hasExtra("alarm")) {
			Log.i(TAG, "onNewIntent: This intent is called from alarm exist notification");
			showAlarmDialog(intent.getStringExtra("alarmRouteName"), intent.getStringExtra("alarm"));
			intent.removeExtra("alarm");
		}
		if (intent.hasExtra("stopCode")) {
//			Log.v(TAG, "Intent has stopCode: " + intent.getIntExtra("stopCode", -1));
			if (cardCount == 0)
				setCardCount(1);
			pendingSetStop = true;
		}
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		MenuButton.onOptionMenuClosed(this);
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuButton.onPrepareOptionsMenu(this);
		MenuItem favoriteBtn = menu.getItem(3);
		if (pager.getCurrentItem() != cardCount - 1) {
			favoriteBtn.setIcon(R.drawable.ic_menu_unstar);
			favoriteBtn.setTitle("Unfavorite");
			favoriteBtn.setTitleCondensed("Unfavorite");
		} else {
			favoriteBtn.setIcon(R.drawable.ic_menu_star);
			favoriteBtn.setTitle("Favorite");
			favoriteBtn.setTitleCondensed("Favorite");
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startSettings();
			return true;
		case R.id.search:
			onSearchRequested();
			return true;
		case R.id.refresh:
			refreshAll(true);
			return true;
		case R.id.favorite:
			if (pager.getCurrentItem() == cardCount - 1)
				pinCurrentStop();
			else
				unpinCurrentStop();
			return true;
		case R.id.tripplanner:
			tripPlanner();
			return true;
		}
		return false;
	}

	@Override
	public boolean onSearchRequested() {
		if (cardCount > 0)
			pager.setCurrentItem(cardCount - 1);
		Intent searchIntent = new Intent(this, StopSearch.class);
		startActivity(searchIntent);
		return false;
	}

	public void refreshAll(boolean forced) {
		Intent intent = new Intent(this, Updater.class);
		intent.setAction("Cards refreshAll");
		intent.putExtra("forced", forced);
		sendBroadcast(intent);
	}

	/**
	 * Shows the toast to tell user how to use bookmark shortcut
	 * 
	 * @param view The view that is pressed (i.e. the bookmark icon).
	 */
	public void bookmarkHint(View view) {
		CardModel cardModel = getCardModel(pager.getCurrentItem());
		String toastString = "Long press to add to favorites";
		if (cardModel.getMode() == CardModel.MODE.normal) {
			toastString = "Long press to remove from favorites";
		}
		Helper.createToast(this, toastString, true);
	}

	public void pinCurrentStop() {
		if (adapter.getCount() >= MAXSTOP) {
			Helper.createToast(this, "You have reached the maximum of " + MAXSTOP + " stops");
			return;
		}
		CardModel cardModel = getCardModel(pager.getCurrentItem());
		int cardPos = findCard(cardModel.getModel().getStopCode());
		if (cardPos == -1) {
			// another pinned stop not found
			int position = adapter.getCount() - 1;
			// pager.setCurrentItem(position + 1, false);
			adapter.copyCard(position, cardModel);
			// pager.setCurrentItem(position, true);
		} else {
			// another pinned stop is found, go to that card
			pager.setCurrentItem(cardPos);
		}
	}

	public void unpinCurrentStop() {
		// Log.d("", "Unpin current stop");
		final int currentItem = pager.getCurrentItem();
		adapter.removeCard(currentItem);
	}

	private void initializePager() {
		pager = (ViewPager) findViewById(R.id.cards_pager);
		FragmentManager fm = getSupportFragmentManager();
		adapter = new CardsAdapter(fm);
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(setActivePageControl);
	}

	private void initializePageControl() {
		pageControl = (PageControl) findViewById(R.id.pageControl);
		pageControl.setIndicatorSize(20);
		Resources resources = this.getResources();
		Drawable activePage = resources.getDrawable(R.drawable.pagecontrol_active);
		Drawable inactivePage = resources.getDrawable(R.drawable.pagecontrol_inactive);
		pageControl.setDefaultActiveDrawable(activePage);
		pageControl.setDefaultInactiveDrawable(inactivePage);

		pageControl.addPageCount(0, cardCount);
		pageControl.setCurrentPage(0); // default value, likely overridden after setPage

		if (cardCount > 0) {
			Drawable activeSearch = resources.getDrawable(R.drawable.pagecontrol_search_active);
			Drawable inactiveSearch = resources.getDrawable(R.drawable.pagecontrol_search_inactive);
			pageControl.setActiveDrawable(activeSearch, cardCount - 1);
			pageControl.setInactiveDrawable(inactiveSearch, cardCount - 1);
		}
	}

	/**
	 * Refresh the PageControl to show the current number of pages (cards)
	 * 
	 * @param oldCardCount The old card count, so that adding and removing cards can be done in relative.
	 */
	private void refreshPageControl(int oldCardCount) {
		pageControl = (PageControl) findViewById(R.id.pageControl);
		pageControl.addPageCount(0, cardCount - oldCardCount);
	}

	private void initializeCards() {
//		Log.v(TAG, "initialize cards");
		cardCount = Math.max(cardCount, 0);
		cardsArray = new ArrayList<CardModel>(cardCount);
		for (int i = 0; i < cardCount; i++) {
			CardModel cardModel = new CardModel(i, this);
			Card.newInstance(i);
			cardsArray.add(cardModel);
		}
		if (cardCount > 0) {
			CardModel lastCard = cardsArray.get(cardCount - 1);
			lastCard.setMode(CardModel.MODE.search);
		}
	}

	private CardModel getCardModel(int position) {
		if (cardsArray == null) {
			Log.e(TAG, "cardsArray is null");
			throw new IllegalArgumentException();
			// return null;
		}
		if (position < cardsArray.size())
			return cardsArray.get(position);
		else
			return null;
	}

	private void setCardCount(int count) {
		cardCount = count;
		Pref.setInt("com.mauricelam.transit.Cards.cardCount", count);
	}

	private void restoreCardCount() {
		cardCount = Pref.getInt("com.mauricelam.transit.Cards.cardCount", 0);
	}

	private int findCard(int stopCode) {
		int currentPos = pager.getCurrentItem();
		for (int i = 0; i < cardCount; i++) {
			CardModel cardModel = getCardModel(i);
			if (cardModel.getModel().getStopCode() == stopCode && i != currentPos)
				return i;
		}
		return -1;
	}

	private void restoreModels() {
//		Log.v(TAG, "Restoring models. PendingSetStop: " + pendingSetStop);
		for (int i = 0; i < cardCount; i++) {
			CardModel cardModel = getCardModel(i);
			if (pendingSetStop && i == adapter.getCount() - 1) {
				// set stop as indicated in intent (if applicable)
				setStopByIntent(getIntent());
				pendingSetStop = false;
			} else {
				if (cardModel != null)
					cardModel.restoreModel(i);
			}
		}
	}

	/**
	 * Start trip planner (Google Maps with transit directions)
	 */
	private void tripPlanner() {
		GeoPoint from = null;
		StopwatchModel model = getCardModel(pager.getCurrentItem()).getModel();
		if (model != null)
			from = model.getStopLocation();
		TripPlanner.startTripPlanner(this, from);
	}

	/**
	 * Start the settings activity
	 */
	private void startSettings() {
		Intent settingsIntent = new Intent(this, Settings.class);
		startActivity(settingsIntent);
	}

	private void setStopByIntent(Intent intent) {
		int stopCode = intent.getIntExtra("stopCode", -1);
		Log.i(TAG, "set stop code: " + stopCode);
		if (stopCode != -1) {
			String stopName = intent.getStringExtra("stopName");
			String referrer = intent.getStringExtra("referrer");
			CardModel cardModel = getCardModel(cardsArray.size() - 1);
			GeoPoint location = null;
			if (intent.hasExtra("stopLatitude") && intent.hasExtra("stopLongitude")) {
				location = new GeoPoint(intent.getIntExtra("stopLatitude", 0), intent.getIntExtra("stopLongitude", 0));
			}
			// do not set stop again if already on that stop
			if (cardModel.getModel() == null || stopCode != cardModel.getModel().getStopCode()) {
				cardModel.setStop(stopName, stopCode);
				StopwatchModel.setStop(this, adapter.getCount() - 1, stopName, stopCode, referrer, location);
				intent.removeExtra("stopCode");
				intent.removeExtra("stopName");
				intent.removeExtra("referrer");
			}
			Log.d(TAG, "adapterCount: " + adapter.getCount());
			pager.setCurrentItem(adapter.getCount() - 1, false);
		}
	}

	private void showAlarmDialog(final String routeName, String alarmString) {
		OnClickListener onRemoveClick = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PendingIntent alarmPendingIntent = Alarm.createAlarmPendingIntent(routeName);
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				am.cancel(alarmPendingIntent);
				NotificationManager nm = (NotificationManager) Cards.this
						.getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancel(AlarmReceiver.ALARMEXISTID);
				AlarmController.sharedInstance().clearAlarm();
			}
		};
		new AlertDialog.Builder(this).setMessage(alarmString).setPositiveButton("Remove", onRemoveClick)
				.setNegativeButton("Keep", null).show();
	}

	private OnPageChangeListener setActivePageControl = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if (pageControl != null) {
				pageControl.setCurrentPage(position);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
	};

	private void onCardOrderChanged() {
		for (int i = 0; i < cardCount; i++) {
			CardModel cardModel = getCardModel(i);
			cardModel.setFragmentNumber(i);
			Card card = (Card) adapter.getItem(i);
			cardModel.setDelegate(card);
//			Log.d(TAG, "card order changed: " + i + ", " + cardCount);
			if (i < cardCount - 1) {
				cardModel.setMode(CardModel.MODE.normal);
//				Log.v(TAG, "normal mode");
			} else {
				cardModel.setMode(CardModel.MODE.search);
//				Log.v(TAG, "search mode");
			}
		}
	}

	private class CardsAdapter extends FragmentPagerAdapter {

		public CardsAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			int count = getCount();
			for (int i = 0; i < count; i++) {
				Card card = (Card) instantiateItem(pager, i);
				card.update();
			}
		}

		/**
		 * Create a copy of the card at position
		 * 
		 * @param position Position of the currnet card.
		 * @param cardToCopy
		 *            The model of the card to copy
		 */
		public void copyCard(int position, CardModel cardToCopy) {
			CardModel newCardModel = CardModel.copy(position, cardToCopy);
			if (newCardModel == null) {
				Helper.createToast(Cards.this, "Loading data from server. Please wait");
				return;
			}
			cardsArray.add(position, newCardModel);
			setCardCount(cardCount + 1);
			onCardOrderChanged();
			this.notifyDataSetChanged();
			pageControl.addPageCount(position, 1);
		}

		public void removeCard(int position) {
			setCardCount(cardCount - 1);
			cardsArray.remove(position);
			StopwatchModel.removeFromPref(cardCount);
			onCardOrderChanged();
			this.notifyDataSetChanged();
			pageControl.addPageCount(position, -1);
		}

		@Override
		public int getCount() {
			return cardCount;
		}

		@Override
		public Fragment getItem(int position) {
			return Card.newInstance(position);
		}

	}

	/**
	 * A receiver that receives the update status, and show / hide the activity
	 * indicator accordingly. 
	 * 
	 * @author Maurice Lam
	 * 
	 */
	public class UpdateStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int status = intent.getIntExtra("updateStatus", 0);
			if (status == UpdateService.UPDATESTARTED) {
				onUpdateStart();
			} else if (status == UpdateService.UPDATECOMPLETE) {
				onUpdateComplete();
			} else if (status == UpdateService.UPDATEERROR) {
				onUpdateError(intent.getBooleanExtra("forced", false));
			} else {
				Log.w(TAG, "Broadcast status not recognized");
			}
		}

		private void onUpdateStart() {
//			Log.v(TAG, "on update start broadcast");
            for (CardModel cardModel : cardsArray) {
                cardModel.onUpdateStart();
            }
		}

		private void onUpdateComplete() {
//			Log.v(TAG, "on update complete broadcast");
            for (CardModel cardModel : cardsArray) {
                cardModel.onUpdateComplete();
            }
		}

		private void onUpdateError(boolean forced) {
			if (forced) {
				Helper.createToast(Cards.this, "Cannot connect to server");
			}
			Log.w(TAG, "Update error, cannot connect to server");
		}

	}
}
