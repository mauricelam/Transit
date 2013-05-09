package com.mauricelam.transit;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RouteListItem extends LinearLayout {
	
	private TextView nameLabel;
	private TextView timeLabel;
	private RouteBubble bubble;
	private Context context;
	
	public RouteListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public RouteListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public RouteListItem(Context context) {
		super(context);
		this.context = context;
	}
	
	private void loadViews () {
		if (nameLabel == null) {
			nameLabel = (TextView) this.findViewById(R.id.routeitem_name);
		}
		if (bubble == null) {
			bubble = (RouteBubble) this.findViewById(R.id.routeitem_bubble);
		}
		if (timeLabel == null) {
			timeLabel = (TextView) this.findViewById(R.id.routeitem_time);
		}
	}
	
	public void setName(String name) {
		loadViews();
		if (name != null && name.length() > 0) {
			nameLabel.setText(name);
            bubble.setColor(Route.getRouteColor(name), Route.getRouteShiny(name));
            bubble.setIStop(false);
//			bubble.setImageDrawable(Route.getRouteColor(context, name));
			bubble.setVisibility(View.VISIBLE);
		}
	}

    public void setRoute(Route route) {
        loadViews();
        if (route != null && route.getName().length() > 0) {
            nameLabel.setText(route.getName());
            bubble.setColor(route.getColor(), route.isShiny());
            bubble.setIStop(route.isIStop());
            bubble.setVisibility(View.VISIBLE);
            setTime(route.getArrival());
            setRealTime(route.isRealTime());
        }
    }

    private static int getMinsAway(Date time) {
        return (int) Math.floor((time.getTime() - new Date().getTime()) / 60000);
    }

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
	
	public void setTime(Date time) {
		loadViews();
        int mins = getMinsAway(time);
		if (mins >= 0) {
			switch (mins) {
			case 0:
				timeLabel.setText("DUE");
				break;
			case 1:
				timeLabel.setText("1 min");
				break;
			default:
                if (mins > 60) {
                    timeLabel.setText(timeFormat.format(time).toLowerCase());
                } else {
                    timeLabel.setText(mins + " mins");
                }
			}
		}
	}

    public void setRealTime(boolean realTime) {
        loadViews();
        // Avoid using alpha blending for performance
        timeLabel.setTextColor(realTime ? Color.BLACK : Color.GRAY);
        nameLabel.setTextColor(realTime ? Color.BLACK : Color.GRAY);
        bubble.clearColorFilter();
        if (!realTime) {
            bubble.setColorFilter(Color.argb(127, 255, 255, 255));
        }
    }
	
	public void setNoRouteFound() {
		loadViews();
		nameLabel.setText("No routes are found. ");
		timeLabel.setText("");
        bubble.setVisibility(View.INVISIBLE);
	}
	
}
