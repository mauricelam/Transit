package com.mauricelam.transit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class About extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
        TextView googlePlay = (TextView) findViewById(R.id.googlePlay);
        googlePlay.setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
    }
	
	public void flags(View view){
		Intent intent = new Intent(this, Flags.class);
		startActivity(intent);
	}
}
