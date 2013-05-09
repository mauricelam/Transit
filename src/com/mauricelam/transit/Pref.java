package com.mauricelam.transit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

public class Pref {
	protected static final String TAG = "Preferences";

	private static SharedPreferences getPref(Context context) {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs;
	}

	public static void setStringArray(final String key, final String[] value) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		String saveString = convertStringArrayToJSONArray(value).toString();
		editor.putString(key, saveString);
		try {
			SharedPreferences.class.getMethod("apply").invoke(editor);
		} catch (Exception e) {
			editor.commit();
		}
	}

//	public static String[] getStringArray(String key) {
//		SharedPreferences prefs = getPref(TransitApplication.getContext());
//		String saveString = prefs.getString(key, "[]");
//		try {
//			JSONArray jArray = new JSONArray(saveString);
//			int arrayLength = jArray.length();
//			String[] output = new String[arrayLength];
//			for (int i = 0; i < arrayLength; i++) {
//				output[i] = jArray.getString(i);
//			}
//			return output;
//		} catch (Exception e) {
//			Log.e("getStringArray", e.getMessage());
//			return new String[0];
//		}
//	}
//
//	public static void setLongArray(final String key, final long[] value) {
//		SharedPreferences prefs = getPref(TransitApplication.getContext());
//		SharedPreferences.Editor editor = prefs.edit();
//		String saveString = convertLongArrayToJSONArray(value).toString();
//		editor.putString(key, saveString);
//		try {
//			SharedPreferences.class.getMethod("apply").invoke(editor);
//		} catch (Exception e) {
//			editor.commit();
//		}
//	}
//
//	public static long[] getLongArray(String key) {
//		SharedPreferences prefs = getPref(TransitApplication.getContext());
//		String saveString = prefs.getString(key, "[]");
//		try {
//			JSONArray jArray = new JSONArray(saveString);
//			long[] output = new long[jArray.length()];
//			for (int i = 0; i < jArray.length(); i++) {
//				output[i] = jArray.getLong(i);
//			}
//			return output;
//		} catch (Exception e) {
//			Log.e("getStringArray", e.getMessage());
//			return new long[0];
//		}
//	}

	public static String getString(String key, String def) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		return prefs.getString(key, def);
	}

	public static void setString(final String key, final String value) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		try {
			SharedPreferences.class.getMethod("apply").invoke(editor);
		} catch (Exception e) {
			editor.commit();
		}
	}

	public static boolean getBoolean(String key, boolean def) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		return prefs.getBoolean(key, def);
	}

	public static void setBoolean(final String key, final boolean value) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		try {
			SharedPreferences.class.getMethod("apply").invoke(editor);
		} catch (Exception e) {
			editor.commit();
		}
	}

	public static int getInt(String key, int def) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		return prefs.getInt(key, def);
	}

	public static void setInt(final String key, final int value) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		try {
			SharedPreferences.class.getMethod("apply").invoke(editor);
		} catch (Exception e) {
			editor.commit();
		}
	}

	public static long getLong(String key, long def) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		return prefs.getLong(key, def);
	}

	public static void setLong(final String key, final long value) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(key, value);
		try {
			SharedPreferences.class.getMethod("apply").invoke(editor);
		} catch (Exception e) {
			editor.commit();
		}
	}

	public static void remove(String key) {
		SharedPreferences prefs = getPref(TransitApplication.getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		try {
			SharedPreferences.class.getMethod("apply").invoke(editor);
		} catch (Exception e) {
			editor.commit();
		}
	}

	private static JSONArray convertStringArrayToJSONArray(String[] stringArray) {
		JSONArray jArray = new JSONArray();
		try {
			int arrayLength = stringArray.length;
			for (int i = 0; i < arrayLength; i++) {
				jArray.put(i, stringArray[i]);
			}
		} catch (JSONException e) {
			Log.e("convertStringArrayToJSONArray", e.getMessage());
			return null;
		}
		return jArray;
	}

	private static JSONArray convertLongArrayToJSONArray(long[] longArray) {
		JSONArray jArray = new JSONArray();
		try {
			int arrayLength = longArray.length;
			for (int i = 0; i < arrayLength; i++) {
				jArray.put(i, longArray[i]);
			}
		} catch (JSONException e) {
			Log.e("convertLongArrayToJSONArray", e.getMessage());
			return null;
		}
		return jArray;
	}
}
