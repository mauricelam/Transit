package com.mauricelam.transit;

import java.util.Date;
import java.util.Formatter;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * A Model and Controller class for bus alarms, notifying the user of
 * 
 * @author Maurice Lam
 * 
 */
public class Alarm {

	/**
	 * Constructor for an alarm. The factory method createAlarm should be called
	 * instead of this.
	 * 
	 * @param route
	 *            The route the alarm is for
	 * @param oldAlarmExist
	 *            Whether an old alarm exists
	 */
	public Alarm(Route route, boolean oldAlarmExist) {
		Context context = TransitApplication.getContext();
		final int timeAhead = Pref.getInt("alarmAhead", 5);
		// intent to do when alarm is invoked
		PendingIntent alarmPendingIntent = createAlarmPendingIntent(route.getName());
		long triggerTime = route.getArrival().getTime() - (timeAhead * 60000);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, triggerTime, alarmPendingIntent);
		StringBuilder sb = new StringBuilder("Alarm ");
		if (oldAlarmExist)
			sb.append("changed to ");
		else
			sb.append("set in ");
		sb.append(route.getMins() - timeAhead).append(" minutes");
		createNotification(sb.toString(), route.getName(), route.getMins());
	}

	static PendingIntent createAlarmPendingIntent(String routeName) {
		Context context = TransitApplication.getContext();
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		alarmIntent.setAction(Intent.ACTION_INSERT);
		alarmIntent.putExtra("routeName", routeName);
        return PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
	}

	/**
	 * Creates a notification (in the notification tray) for the alarm set.
	 * 
	 * @param shortString
	 * @param routeName
	 * @param mins
	 */
	private void createNotification(String shortString, String routeName, int mins) {
		Context context = TransitApplication.getContext();
		// Get the formatted string of the time of the route
		Date arrive = new Date();
		arrive.setMinutes(arrive.getMinutes() + mins);
		Formatter formatter = new Formatter();
		String arriveTime = formatter.format("%tH:%tM", arrive, arrive).toString();
		formatter.close();

		Notification notification = new Notification(R.drawable.notify_alarm, shortString, System.currentTimeMillis());

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// set up intent when the notification is tapped on
		Intent reopenIntent = new Intent(context, Cards.class);
		// reopenIntent.setAction("android.intent.action.MAIN");
		// reopenIntent.addCategory("android.intent.category.LAUNCHER");
		// reopenIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		reopenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		reopenIntent.putExtra("alarm", "Alarm set for " + routeName + " on " + arriveTime);
		reopenIntent.putExtra("alarmRouteName", routeName);
		PendingIntent reopenPendingIntent = PendingIntent.getActivity(context, 0, reopenIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// configure the notification
		String longString = "for " + routeName;
		notification.setLatestEventInfo(context, "Bus arriving on " + arriveTime, longString, reopenPendingIntent);
		notification.when = 0; // hide the time next to the notification
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		nm.notify(AlarmReceiver.ALARMEXISTID, notification);
	}
}
