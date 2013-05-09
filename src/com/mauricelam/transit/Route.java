package com.mauricelam.transit;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * This class should be immutable
 */
public class Route implements Parcelable {
	private String name;
	private Date arrival;
    private String trip;
    private boolean realtime;
    private boolean iStop;

	public Route(String name, Date arrival, String trip, boolean realtime, boolean iStop) {
		this.name = name;
		this.arrival = arrival;
        this.trip = trip;
        this.realtime = realtime;
        this.iStop = iStop;
	}

	public static Route routeFromJSON(JSONObject jObj, boolean realtime) throws JSONException {
		String jName = jObj.getString("n");
		Long expectedLong = jObj.getLong("e");
		Date expected = new Date(expectedLong * 1000);
        String trip = jObj.getString("t");
        String flags = jObj.has("f") ? jObj.getString("f") : "";
        boolean iStop = flags.contains("i");
        return new Route(jName, expected, trip, realtime, iStop);
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

    public boolean isIStop() {
        return iStop;
    }

//	private static Drawable getBubble(Context context, int color, boolean shiny) {
//        Drawable d = context.getResources().getDrawable(R.drawable.route_bubble);
//        GradientDrawable gd = (GradientDrawable) d.mutate();
//        if (!shiny) {
//			gd.setColor(color);
//		} else {
//            int brighter = Color.rgb(Color.red(color) + 50, Color.green(color) + 50, Color.blue(color) + 50);
//            gd.setColors(new int[] { color, brighter,  color});
//		}
//		return gd;
//	}

    public boolean isShiny () {
        return getRouteShiny(this.name);
    }

    public int getColor () {
        return getRouteColor(this.name);
    }

    public static boolean getRouteShiny(String name) {
        return name.contains("Silver") || name.contains("Gold") || name.contains("Bronze");
    }

    public static int getRouteColor(String name) {
        if (name.contains("Illini")) {
            return 0xff663399;
        } else if (name.contains("Yellow")) {
            return 0xffFFCC33;
        } else if (name.contains("Green")) {
            return 0xff009900;
        } else if (name.contains("Silver")) {
            return 0xffCCCCCC;
        } else if (name.contains("Gold")) {
            return 0xffC7994A;
        } else if (name.contains("Teal")) {
            return 0xFF008080;
        } else if (name.contains("Red")) {
            return 0xFFCC0033;
        } else if (name.contains("Blue")) {
            return 0xFF003399;
        } else if (name.contains("Orange")) {
            return 0xFFFF9933;
        } else if (name.contains("Brown")) {
            return 0xFF663300;
        } else if (name.contains("Navy")) {
            return 0xFF000066;
        } else if (name.contains("Ruby")) {
            return 0xFFDE1E64;
        } else if (name.contains("Lime")) {
            return 0xFF6FBE44;
        } else if (name.contains("Bronze")) {
            return 0xff9E8966;
        } else if (name.contains("Grey")) {
            return 0xFF666666;
        } else if (name.contains("Air")) {
            return 0xFF99CCFF;
        } else if (name.contains("Lavender")) {
            return 0xFF9966CC;
        } else if (name.contains("Sport")) {
            return 0xFFC04D66;
        } else {
            return 0xFFCCCCC;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeLong(arrival.getTime());
        parcel.writeString(trip);
        parcel.writeBooleanArray(new boolean[] { realtime, iStop });
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {

        @Override
        public Route createFromParcel(Parcel parcel) {
            String name = parcel.readString();
            Date arrival = new Date(parcel.readLong());
            String trip = parcel.readString();

            boolean[] bools = new boolean[2];
            parcel.readBooleanArray(bools);
            boolean realtime = bools[0];
            boolean iStop = bools[1];
            return new Route(name, arrival, trip, realtime, iStop);
        }

        @Override
        public Route[] newArray(int i) {
            return new Route[i];
        }
    };
}
