package com.mauricelam.transit;

import include.Helper;
import android.content.Context;

public class AlarmController {
	private static AlarmController instance;
	private Alarm alarm = null;
	
	public static AlarmController sharedInstance() {
		if (instance == null) {
			instance = new AlarmController();
		}
		return instance;
	}
	
	public void addAlarm (Route route) {
		Context context = TransitApplication.getContext();
		final int timeAhead = Pref.getInt("alarmAhead", 5);
		if (route.getMins() <= timeAhead) {
			Helper.createToast(context, "We give alarms " + timeAhead + " minutes ahead :-)");
			return;
		}
		boolean oldAlarmExists = (alarm != null);
		alarm = new Alarm(route, oldAlarmExists);
	}
	
	public Alarm getAlarm () {
		return alarm;
	}
	
	public void clearAlarm () {
		alarm = null;
	}
}
