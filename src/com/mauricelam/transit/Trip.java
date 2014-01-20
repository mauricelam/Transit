package com.mauricelam.transit;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.mauricelam.transit.stopdatabase.StopDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * User: mauricelam
 * Date: 4/5/13
 * Time: 11:49 PM
 */
public class Trip implements Parcelable {

    private String headsign;
    private Route route;
    private List<Stop> stops;

    public Trip(String headsign, List<Stop> stops) {
        this.headsign = headsign;
        this.stops = stops;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public static Trip tripFromJSON(JSONObject jObj) throws JSONException {
        String headsign = jObj.getString("h");
        JSONArray jArr = jObj.getJSONArray("s");
        int arrayLength = jArr.length();
        List<Stop> stops = new ArrayList<Stop>(arrayLength);
        for (int i = 0; i < arrayLength; i++) {
            try {
                stops.add(StopDatabase.sharedInstance().getStop(jArr.getString(i)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                // ignore
            }
        }
        return new Trip(headsign, stops);
    }

    public String getHeadSign() {
        return this.headsign;
    }

    public List<Stop> getStops() {
        return this.stops;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(headsign);
        parcel.writeTypedList(stops);
        if (route != null)
            parcel.writeParcelable(route, flags);
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel parcel) {
            String headsign = parcel.readString();
            List<Stop> stops = new ArrayList<Stop>();
            parcel.readTypedList(stops, Stop.CREATOR);
            Route route = parcel.readParcelable(Route.class.getClassLoader());
            Trip output = new Trip(headsign, stops);
            output.setRoute(route);
            return output;
        }

        @Override
        public Trip[] newArray(int i) {
            return new Trip[i];
        }
    };
}
