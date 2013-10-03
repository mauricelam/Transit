package com.mauricelam.transit;

import android.content.Context;
import android.util.Log;

public class CardModel {
	private static final String TAG = "Transit CardModel";

	private int fragmentNumber = -1;
	private int mode = MODE.normal;
	private StopwatchModel model;
	private CardDelegate delegate;
	// context is required because fragment might be detached from activity
	private Context context;

	public static class MODE {
		public static final int normal = 0;
		public static final int search = 1;
	}

	public interface CardDelegate {
		public void reloadList(StopwatchModel model);

		public void reloadMap();
		
		public void setListVisible(boolean visible);
		
		public void setLoadVisible(boolean visible);
		
		public void setMode(int mode);
	}

	public CardModel(int num, Context context) {
		this.fragmentNumber = num;
		this.context = context.getApplicationContext();
		restoreModel(num);
	}

	public static CardModel copy(int num, CardModel other) {
		CardModel output = new CardModel(num, other.context);
		StopwatchModel model = StopwatchModel.copy(other.getModel());
		output.setLoadVisible(false);
		if (model == null)
			return null;
		return output;
	}

	public void setDelegate(CardDelegate delegate) {
		this.delegate = delegate;
	}

	public void setMode(int mode) {
//		Log.v(TAG, "Set mode: " + mode);
		if (this.mode == mode) {
			return;
		}
		this.mode = mode;
		if (delegate != null)
			delegate.setMode(mode);
		else
			Log.w(TAG, "setMode: delegate is null");
	}

	public int getMode() {
		return mode;
	}

	/**
	 * Sets the stop shown in the current Main activity
	 * 
	 * @param name Name of the stop.
	 * @param code Code of the stop.
	 */
	public void setStop(String name, int code) {
		model = new StopwatchModel(context, code, name);
		setListVisible(false);
	}

	public void setFragmentNumber(int num) {
		this.fragmentNumber = num;
		model.packup(fragmentNumber);
	}

//	public int getFragmentNumber() {
//		return fragmentNumber;
//	}

	public StopwatchModel getModel() {
		return model;
	}

	public void restoreModel(int num) {
		if (fragmentNumber == -1)
			fragmentNumber = num;
		if (model == null) {
//			Log.v(TAG, "Restore model: " + fragmentNumber);
			model = StopwatchModel.createModelFromPack(context, fragmentNumber);
			if (model != null) {
				// model.commitRefreshData();
				if (delegate != null) {
					delegate.reloadList(model);
				}
			}
		}
		if (model == null) {
			Log.e(TAG, "Unable to restore StopwatchModel");
		}
	}

	public void onUpdateStart() {
		setLoadVisible(true);
	}

	public void onUpdateComplete() {
//		Log.d(TAG, "card on finish update");
        onUpdateNextStep();
        setLoadVisible(false);
	}

    public void onUpdateNextStep() {
//        Log.v(TAG, "Update next step " + fragmentNumber);
        if (model != null && context != null) {
            model.reloadFromPack(fragmentNumber);
            setListVisible(true);
            if (delegate != null)
                delegate.reloadList(model);
            else
                Log.w(TAG, "Cannot reload list. Model delegate " + fragmentNumber + " is null");
        } else if (model == null) {
            Log.w(TAG, "cannot finish update: model is null");
        } else {
            Log.w(TAG, "cannot finish update: context is null");
        }
    }

//	public void onUpdateError() {
//		if (model != null && context != null) {
//			setLoadVisible(false);
//			setListVisible(true);
//		} else {
//			Log.w(TAG, "cannot show error message: model is null");
//		}
//	}

	public boolean getListVisibility() {
		return listVisible;
	}

	public boolean getLoadVisibility() {
		return loadVisible;
	}

//	private String getLocationString(GeoPoint pt) {
//		if (pt == null)
//			return null;
//		double lat = ((double) pt.getLatitudeE6()) / 1E6;
//		double lng = ((double) pt.getLongitudeE6()) / 1E6;
//        return String.valueOf(lat) + "," + String.valueOf(lng);
//	}

	private boolean listVisible = true;
	private boolean loadVisible = false;

	private void setListVisible(boolean visible) {
		listVisible = visible;
		if (delegate != null) {
			delegate.setListVisible(visible);
		}
	}

	private void setLoadVisible(boolean visible) {
//		Log.d(TAG, "set load visible");
		loadVisible = visible;
		if (delegate != null) {
			delegate.setLoadVisible(visible);
		}
	}
}
