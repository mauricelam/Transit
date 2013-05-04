package com.mauricelam.transit;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * The class for obtrusive notification. This uses the dialog theme to create an
 * activity that closely mimics a normal dialog. Showing of this notificaiton is
 * disabled by default.
 * 
 * @author Maurice Lam
 * 
 */
public class AlarmNotification extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.alarm);

		Intent intent = getIntent();
		String routeName = intent.getStringExtra("routeName");
		setDialogText(routeName);
	}

	/**
	 * Removes the notification of the bus when either button is pressed
	 */
	private void removeNotification() {
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(AlarmReceiver.ALARMID);
	}

	private void setDialogText(String routeName) {
		TextView textview = (TextView) findViewById(R.id.alarm_dialogtext);
		String text = routeName + " is coming in " + Pref.getInt("alarmAhead", 5) + " minutes";
		textview.setText(text);
	}

	/**
	 * When open is clicked
	 * 
	 * @param view
	 */
	public void startApp(View view) {
		removeNotification();
		Intent appIntent = new Intent(this, Cards.class);
		appIntent.setAction("android.intent.action.MAIN");
		appIntent.addCategory("android.intent.category.LAUNCHER");
		startActivity(appIntent);
		this.finish();
	}

	/**
	 * When cancel is clicked
	 * 
	 * @param view
	 */
	public void dismiss(View view) {
		removeNotification();
		this.finish();
	}
}
