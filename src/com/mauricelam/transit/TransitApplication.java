package com.mauricelam.transit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Extension of Application to store the context and easily retrieve by modules.
 * 
 * @author Maurice Lam
 * 
 */
public class TransitApplication extends Application {
	private static final String PREFKEY_VERSION = "transit-version";
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();

		// test for upgrade
		int currentVersion = getCurrentVersion();
		int savedVersion = getSavedVersion();
		if (currentVersion != savedVersion) {
			onUpgrade(savedVersion, currentVersion);
		}

	}

	public static Context getContext() {
		return context;
	}

	public int getCurrentVersion() {
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
		}
		return -1;
	}

	public int getSavedVersion() {
		return Pref.getInt(PREFKEY_VERSION, -1);
	}

	protected void onUpgrade(int fromVersion, int toVersion) {

		// upgrade Pref system to not use scope
		if (fromVersion < 13 && toVersion >= 13) {
			// check that the preference is not there
			if (Pref.getInt("modelpack0_stopCode", -1) == -1) {
				SharedPreferences prefs = context.getSharedPreferences("modelpack",
						Context.MODE_PRIVATE);
				int cardCount = Pref.getInt("com.mauricelam.transit.Cards.cardCount", 0);
				for (int id = 0; id < cardCount; id++) {
					int stopCode = prefs.getInt("modelpack" + id + '_' + "stopCode", 100);
					String stopName = prefs.getString("modelpack" + id + '_' + "stopName", "");
					Pref.setInt("modelpack" + id + '_' + "stopCode", stopCode);
					Pref.setString("modelpack" + id + '_' + "stopName", stopName);
				}
			}
		}

		Pref.setInt(PREFKEY_VERSION, toVersion);

	}
}
