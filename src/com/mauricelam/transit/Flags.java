package com.mauricelam.transit;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Flags extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.flags);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
//
//		try {
//			Log.d("Flags changed",
//					"(int): " + key + ": " + Pref.getInt(key, 0));
//		} catch (Exception e) {
//		}
//		try {
//			Log.d("Flags changed",
//					"(bool): " + key + ": " + Pref.getBoolean(key, false));
//		} catch (Exception e) {
//		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		if (!Pref.getBoolean("alarm", true))
			findPreference("obtrusive").setEnabled(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

}
