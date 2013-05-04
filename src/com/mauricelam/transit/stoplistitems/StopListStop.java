package com.mauricelam.transit.stoplistitems;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mauricelam.transit.R;
import com.mauricelam.transit.Stop;

public class StopListStop implements StopListItem {
	private Context context;
	private Stop stop;
	private String stopName;
	private int stopCode;

	public StopListStop(Context context, Stop s) {
		stopName = s.getName();
		stopCode = s.getCode();
		this.stop = s;
		this.context = context;
	}

	@Override
	public Drawable getIcon() {
		return context.getResources().getDrawable(R.drawable.icon_stop);
	}

	@Override
	public String getBigText() {
		return stopName;
	}

	@Override
	public String getSmallText() {
		return "MTD" + String.format("%4d", stopCode);
	}
	
	public Stop getStop() {
		return stop;
	}

	public static void appendToItemList(ArrayList<StopListItem> list,
			Context context, Stop[] stops) {
		list.ensureCapacity(list.size() + stops.length);
		for (int i = 0; i < stops.length; i++) {
			if (stops[i] != null)
				list.add(new StopListStop(context, stops[i]));
		}
	}

	@Override
	public String getIconDescription() {
		return context.getResources().getString(R.string.s_stop);
	}

}
