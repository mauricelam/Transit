package com.mauricelam.transit;

import android.content.Context;
import include.Helper;

import java.util.HashMap;
import java.util.Map;

public class AlarmController {
	private static AlarmController instance;
    private Map<String, Alarm> alarms = new HashMap<String, Alarm>();
	
	public static AlarmController sharedInstance() {
		if (instance == null) {
			instance = new AlarmController();
		}
		return instance;
	}
	
	public void addAlarm (Route route) {
        if (hasAlarm(route)) return;

        Context context = TransitApplication.getContext();
        final int timeAhead = Pref.getInt("alarmAhead", 5);
        if (route.getMins() <= timeAhead) {
            Helper.createToast(context, "We give alarms " + timeAhead + " minutes ahead :-)");
            return;
        }
		Alarm alarm = new Alarm(route);
        alarms.put(route.getTrip(), alarm);
	}

    public void removeAlarm (Route route) {
        removeAlarm(route.getTrip());
    }

    public void removeAlarm (String trip) {
        Alarm alarm = alarms.get(trip);
        if (alarm != null)
            alarm.remove();
        alarms.remove(trip);
    }

    public boolean hasAlarm (Route route) {
        return alarms.containsKey(route.getTrip());
    }
	
//	public Alarm getAlarm () {
//		return alarm;
//	}
	
//	public void clearAlarm () {
//		alarm = null;
//	}
}
