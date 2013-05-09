package com.mauricelam.transit;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import include.GeoPoint;
import org.json.JSONObject;

public class Stop implements Parcelable {
	private String name;
	private String query;
	private GeoPoint location;
	private int code;

	public Stop() {}

	public Stop(String name, String query, int code, int lat, int lng) {
		this.name = name;
		this.query = query;
		this.code = code;
		this.location = new GeoPoint(lat, lng);
	}

	public Stop(String name, String query, int code, GeoPoint location) {
		this.name = name;
		this.query = query;
		this.code = code;
		this.location = location;
	}

	public Stop(String name, int code) {
		this.name = name;
		this.code = code;
		this.location = null;
	}
	
	public Stop(String name, String query, int code){
		this.name = name;
		this.code = code;
		this.query = query;
		this.location = null;
	}

	public static Stop stopFromJSON(JSONObject jObj) {
		try {
			String name = jObj.getString("n");
			String query = jObj.getString("q");
			int code = jObj.getInt("c");
			Stop stop = new Stop(name, query, code);
			if (jObj.has("lat")) {
				int lat = jObj.getInt("lat");
				int lng = jObj.getInt("lng");
				stop.setLocation(lat, lng);
			}
			return stop;
		} catch (Exception e) {
			Log.e("Transit Stop", e.getMessage());
			return null;
		}
	}

	public void setLocation(int lat, int lng) {
		this.location = new GeoPoint(lat, lng);
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}

    public String getQuery() {
        return query;
    }

	public String getCodeString() {
		return String.format("%04d", code);
	}

	public GeoPoint getLocation() {
		return location;
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(query);
        parcel.writeInt(code);
        if (location != null) {
            parcel.writeInt(location.getLatitudeE6());
            parcel.writeInt(location.getLongitudeE6());
        } else {
            parcel.writeInt(0);
            parcel.writeInt(0);
        }
    }

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel parcel) {
            String name = parcel.readString();
            String query = parcel.readString();
            int code = parcel.readInt();
            int lat = parcel.readInt();
            int lng = parcel.readInt();
            if (lat == 0 && lng == 0) {
                return new Stop(name, query, code);
            } else {
                return new Stop(name, query, code, lat, lng);
            }
        }

        @Override
        public Stop[] newArray(int i) {
            return new Stop[i];
        }
    };
}
