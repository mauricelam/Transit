package com.mauricelam.transit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * This class should be immutable
 */
public class Route {
	private String name;
	private Date arrival;
    private String trip;
    private boolean realtime;

	public Route(String name, Date arrival, String trip, boolean realtime) {
		this.name = name;
		this.arrival = arrival;
        this.trip = trip;
        this.realtime = realtime;
	}

	public static Route routeFromJSON(JSONObject jObj, boolean realtime) throws JSONException {
		String jName = jObj.getString("n");
		Long expectedLong = jObj.getLong("e");
		Date expected = new Date(expectedLong * 1000);
        String trip = jObj.getString("t");
        return new Route(jName, expected, trip, realtime);
	}

	public String getName() {
		return name;
	}

	public int getMins() {
		return (int) ((arrival.getTime() - new Date().getTime()) / 60000);
	}

    public String getTrip() {
        return this.trip;
    }

	public Date getArrival() {
		return arrival;
	}

    public boolean isRealTime() {
        return realtime;
    }

//	private static Drawable[] bubbles = new Drawable[19];

	private static Drawable getBubble(Context context, int id, int color, boolean shiny) {
//		if (bubbles[id] != null) {
//			return bubbles[id];
//		}

		GradientDrawable gd;
		if (!shiny) {
			Drawable d = context.getResources().getDrawable(R.drawable.route_bubble);
			gd = (GradientDrawable) d.mutate();
			gd.setColor(color);
		} else {
			gd = (GradientDrawable) context.getResources().getDrawable(color);
		}
//		bubbles[id] = gd;
		return gd;
	}

	/**
	 * Returns a drawable (a colored circle) from the name of the route
	 * 
	 * @param name Name of the route
	 * @return The colored circle drawable
	 */
	public static Drawable getRouteColor(Context context, String name) {
		if (name.contains("Illini")) {
			return getBubble(context, 0, 0xFF663399, false);
		} else if (name.contains("Yellow")) {
			return getBubble(context, 1, 0xFFFFCC33, false);
		} else if (name.contains("Green")) {
			return getBubble(context, 2, 0xFF009900, false);
		} else if (name.contains("Silver")) {
			return getBubble(context, 3, R.drawable.shiny_silver, true);
		} else if (name.contains("Gold")) {
			return getBubble(context, 4, R.drawable.shiny_gold, true);
		} else if (name.contains("Teal")) {
			return getBubble(context, 5, 0xFF008080, false);
		} else if (name.contains("Red")) {
			return getBubble(context, 6, 0xFFCC0033, false);
		} else if (name.contains("Blue")) {
			return getBubble(context, 7, 0xFF003399, false);
		} else if (name.contains("Orange")) {
			return getBubble(context, 8, 0xFFFF9933, false);
		} else if (name.contains("Brown")) {
			return getBubble(context, 9, 0xFF663300, false);
		} else if (name.contains("Navy")) {
			return getBubble(context, 10, 0xFF000066, false);
		} else if (name.contains("Ruby")) {
			return getBubble(context, 11, 0xFFDE1E64, false);
		} else if (name.contains("Lime")) {
			return getBubble(context, 12, 0xFF6FBE44, false);
		} else if (name.contains("Bronze")) {
			return getBubble(context, 13, R.drawable.shiny_bronze, true);
		} else if (name.contains("Grey")) {
			return getBubble(context, 14, 0xFF666666, false);
		} else if (name.contains("Air")) {
			return getBubble(context, 15, 0xFF99CCFF, false);
		} else if (name.contains("Lavender")) {
			return getBubble(context, 16, 0xFF9966CC, false);
		} else if (name.contains("Sport")) {
			return getBubble(context, 17, 0xFFC04D66, false);
		} else {
			return getRouteColorCaseInsensitive(context, name);
		}
	}

	private static Drawable getRouteColorCaseInsensitive(Context context, String name) {
		name = name.toUpperCase();
		if (name.contains("ILLINI")) {
			return getBubble(context, 0, 0xFF663399, false);
		} else if (name.contains("YELLOW")) {
			return getBubble(context, 1, 0xFFFFCC33, false);
		} else if (name.contains("GREEN")) {
			return getBubble(context, 2, 0xFF009900, false);
		} else if (name.contains("SILVER")) {
			return getBubble(context, 3, R.drawable.shiny_silver, true);
		} else if (name.contains("GOLD")) {
			return getBubble(context, 4, R.drawable.shiny_gold, true);
		} else if (name.contains("TEAL")) {
			return getBubble(context, 5, 0xFF008080, false);
		} else if (name.contains("RED")) {
			return getBubble(context, 6, 0xFFCC0033, false);
		} else if (name.contains("BLUE")) {
			return getBubble(context, 7, 0xFF003399, false);
		} else if (name.contains("ORANGE")) {
			return getBubble(context, 8, 0xFFFF9933, false);
		} else if (name.contains("BROWN")) {
			return getBubble(context, 9, 0xFF663300, false);
		} else if (name.contains("NAVY")) {
			return getBubble(context, 10, 0xFF000066, false);
		} else if (name.contains("RUBY")) {
			return getBubble(context, 11, 0xFFDE1E64, false);
		} else if (name.contains("LIME")) {
			return getBubble(context, 12, 0xFF6FBE44, false);
		} else if (name.contains("BRONZE")) {
			return getBubble(context, 13, R.drawable.shiny_bronze, true);
		} else if (name.contains("GREY")) {
			return getBubble(context, 14, 0xFF666666, false);
		} else if (name.contains("AIR")) {
			return getBubble(context, 15, 0xFF99CCFF, false);
		} else if (name.contains("LAVENDER")) {
			return getBubble(context, 16, 0xFF9966CC, false);
		} else if (name.contains("SPORT")) {
			return getBubble(context, 17, 0xFFC04D66, false);
		} else {
			return getBubble(context, 18, 0xFFCCCCC, false);
		}
	}
}
