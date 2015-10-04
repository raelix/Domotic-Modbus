package com.domotica.raspimanager.activity;

import com.domotica.raspimanager.R;
import com.domotica.raspimanager.R.layout;

import android.app.Activity;
import android.os.Bundle;

public class ActivitySlave extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slave);
		}
}
