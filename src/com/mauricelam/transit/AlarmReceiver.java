package com.mauricelam.transit;

import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Receive broadcast when the an alarm is invoked, that is, a bus in coming in
 * five mins
 * 
 * @author Maurice Lam
 * 
 */
public class AlarmReceiver extends BroadcastReceiver {

	static final int ALARMID = 1;

	/**
	 * Perform subsequent actions when the broadcast for this alarm is received.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("AlarmReceiver", "Alarm received");
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		String action = intent.getAction();
		if (action.equals(Intent.ACTION_DELETE)) {
            // Called by this class to stop notification automatically when the bus arrives
			removeNotification(nm);
		} else if (action.equals(Intent.ACTION_INSERT)) {
            // Called by Alarm class when an alarm is fired
			removeNotification(nm);
            String routeTrip = intent.getStringExtra("routeTrip");
			String routeName = intent.getStringExtra("routeName");
			if (Pref.getBoolean("obtrusive", false))
				startObtrusiveAlert(context, routeName);
			createNotification(context, nm, routeName);
            AlarmController.sharedInstance().removeAlarm(routeTrip);
			setRemoveNotification(context);
		}
	}

	/**
	 * Starts the activity of the popup dialog to make sure the user does not
	 * miss the bus.
	 */
	private void startObtrusiveAlert(Context context, String routeName) {
		Intent intent = new Intent(context, AlarmNotification.class);
		intent.putExtra("routeName", routeName);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		context.startActivity(intent);
	}

	/**
	 * Removes the notification from the status bar (because the bus has gone)
	 */
	private void removeNotification(NotificationManager nm) {
		nm.cancel(ALARMID);
	}

	/**
	 * Set the notification to be destroyed once the bus arrived, a.k.a. 5
	 * minutes after the broadcast is received. This is done by sending another
	 * broadcast to this class with ACTION_DELETE.
	 */
	private void setRemoveNotification(Context context) {
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		alarmIntent.setAction(Intent.ACTION_DELETE);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, alarmIntent,
				0);
		long triggerTime = new Date().getTime() + 60000 * Pref.getInt("alarmAhead", 5);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
	}

	/**
	 * Creates the notification on the status bar for the alarm.
	 */
	private void createNotification(Context context, NotificationManager nm, String routeName) {
		int timeAhead = Pref.getInt("alarmAhead", 5);

		Notification notification = new Notification(R.drawable.notify_alert,
				"Bus arriving in " + timeAhead + " minutes",
				System.currentTimeMillis());
		String contentTitle = "Bus coming in " + timeAhead + " mins";
		String contentText = routeName;

		// create the intent when the notification is clicked
		Intent notificationIntent = new Intent(context, Cards.class);
		notificationIntent.setAction("android.intent.action.MAIN");
		notificationIntent.addCategory("android.intent.category.LAUNCHER");
		int flags = PendingIntent.FLAG_CANCEL_CURRENT;
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, flags);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		// set the flags
		// notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if (Pref.getBoolean("insist", true))
			notification.flags |= Notification.FLAG_INSISTENT;
		else
			notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

		// set the notification to play a custom sound
		notification.sound = Uri.parse("android.resource://com.maurice.transit/" + R.raw.alert);
		// set the notification to vibrate
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		// notification.defaults |= Notification.DEFAULT_LIGHTS;

		nm.notify(ALARMID, notification);
	}

}
