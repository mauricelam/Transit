package com.mauricelam.transit;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

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
            Context context = TransitApplication.getContext();
            int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
            boolean googleplay = available == ConnectionResult.SUCCESS;

            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ConfigurationInfo config = manager.getDeviceConfigurationInfo();
            boolean gl2 = config.reqGlEsVersion >= 0x20000;
            return googleplay && gl2;
        } catch (Exception e) {
			return false;
		}
	}
}
