package com.mauricelam.transit.stoplistitems;

import android.graphics.drawable.Drawable;

public interface StopListItem {
//	public OnClickListener getClickListener();
	public Drawable getIcon();
	public String getIconDescription();
	public String getBigText();
	public String getSmallText();
}
