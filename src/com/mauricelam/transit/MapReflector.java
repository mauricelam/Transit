package com.mauricelam.transit;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MapReflector {
	private static final String TAG = "Transit MapReflector";

	public static void startActivity(Context context, Intent intent, String withMap, String withoutMap) {
		boolean useMap = isMapAvailable();
		if (useMap) {
			try {
				intent.setClass(context, Class.forName(withMap));
			} catch (ClassNotFoundException e) {
				useMap = false;
			}
		}
		if (!useMap) {
			try {
				intent.setClass(context, Class.forName(withoutMap));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		context.startActivity(intent);
	}

	public static void startActivity(Context context, Intent intent, String withMap) {
		if (isMapAvailable()) {
			try {
				intent.setClass(context, Class.forName(withMap));
				context.startActivity(intent);
			} catch (ClassNotFoundException e) {
				Log.d(TAG, "Map is not available on device");
			}
		}
	}

	public static boolean isMapAvailable() {
		try {
			Class.forName("com.google.android.maps.MapActivity");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
