package com.mauricelam.transit;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Date;
import java.util.Formatter;

/**
 * A Model and Controller class for bus alarms, notifying the user of
 * 
 * @author Maurice Lam
 * 
 */
public class Alarm {

    private static final int ALARMEXISTID = 2;

    private PendingIntent alarmPendingIntent;
    private Route route;

	/**
	 * Constructor for an alarm. The factory method createAlarm should be called
	 * instead of this.
	 * 
	 * @param route
	 *            The route the alarm is for
	 */
	public Alarm(Route route) {
        this.route = route;
		Context context = TransitApplication.getContext();
		final int timeAhead = Pref.getInt("alarmAhead", 5);
		// intent to do when alarm is invoked
        alarmPendingIntent = createAlarmPendingIntent(route);
        long triggerTime = route.getArrival().getTime() - (timeAhead * 60000);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, triggerTime, alarmPendingIntent);
		String alarmString = "Alarm set in " + (route.getMins() - timeAhead) + " minutes";
		createNotification(alarmString, route);
	}

    public void remove() {
        Context context = TransitApplication.getContext();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(alarmPendingIntent);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(route.getTrip(), ALARMEXISTID);
    }

	private static PendingIntent createAlarmPendingIntent(Route route) {
		Context context = TransitApplication.getContext();
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		alarmIntent.setAction(Intent.ACTION_INSERT);
        alarmIntent.putExtra("routeName", route.getName());
        alarmIntent.putExtra("routeTrip", route.getTrip());
        return PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
	}

	/**
	 * Creates a notification (in the notification tray) for the alarm set.
	 * 
	 * @param shortString
	 * @param route
	 */
	private void createNotification(String shortString, Route route) {
		Context context = TransitApplication.getContext();
		// Get the formatted string of the time of the route
		Date arrive = new Date();
		arrive.setMinutes(arrive.getMinutes() + route.getMins());
		Formatter formatter = new Formatter();
		String arriveTime = formatter.format("%tH:%tM", arrive, arrive).toString();
		formatter.close();

		Notification notification = new Notification(R.drawable.notify_alarm, shortString, System.currentTimeMillis());

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// set up intent when the notification is tapped on
		Intent reopenIntent = new Intent(context, Cards.class);
		reopenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		reopenIntent.putExtra("alarm", "Alarm set for " + route.getName() + " on " + arriveTime);
        reopenIntent.putExtra("alarmRouteTrip", route.getTrip());
		PendingIntent reopenPendingIntent = PendingIntent.getActivity(context, 0, reopenIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// configure the notification
		String longString = "for " + route.getName();
		notification.setLatestEventInfo(context, "Bus arriving on " + arriveTime, longString, reopenPendingIntent);
		notification.when = 0; // hide the time next to the notification
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		nm.notify(route.getTrip(), ALARMEXISTID, notification);
	}
}
