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
	
	public boolean setAlarm(Route route, final int timeAhead) {
        if (hasAlarm(route)) removeAlarm(route);

        Context context = TransitApplication.getContext();
        if (route.getMins() <= timeAhead) {
            Helper.createToast(context, "The bus is coming in less than " + timeAhead + " minutes");
            return false;
        }
		Alarm alarm = new Alarm(route, timeAhead);
        alarms.put(route.getTrip(), alarm);
        return true;
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

    public Alarm getAlarm (Route route) {
        return alarms.get(route.getTrip());
    }

}
