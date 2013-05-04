package com.mauricelam.transit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class About extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}
	
	public void flags(View view){
		Intent intent = new Intent(this, Flags.class);
		startActivity(intent);
	}
}
