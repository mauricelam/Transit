package com.mauricelam.transit.stoplistitems;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.mauricelam.transit.R;
import com.mauricelam.transit.Stop;

import java.util.ArrayList;

public class StopListRecent extends StopListStop {
	private Context context;

	public StopListRecent(Context context, Stop s) {
		super(context, s);
		this.context = context;
	}

	@Override
	public Drawable getIcon() {
		return context.getResources().getDrawable(R.drawable.icon_recent);
	}
	
	@Override
	public String getIconDescription() {
		return context.getResources().getString(R.string.s_recent);
	}

	public static void appendToItemList(ArrayList<StopListItem> list, Context context, Stop[] stops){
		list.ensureCapacity(list.size() + stops.length);
		for(int i=0; i<stops.length; i++){
			list.add(new StopListRecent(context, stops[i]));
		}
	}

}
