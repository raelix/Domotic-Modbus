package com.domotica.raspimanager;

import java.util.ArrayList;

import com.domotica.raspimanager.Adapter.PinItemAdapter;
import com.domotica.raspimanager.Record.PinRecord;
import com.domotica.raspimanager.shared.Titanic;
import com.domotica.raspimanager.shared.TitanicTextView;
import com.domotica.raspimanager.shared.Typefaces;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public  class FragmentConfiguration extends Fragment {
	public static PinItemAdapter adapter ;
	EditText host ;EditText user;EditText passw ;TextView version;Button checkupdate;CheckBox gpioUsed;CheckBox deviceUsed;Button savechanges;Button idkey;
//	Button manageslave;
	ProgressDialog  dialogo;
	public static ArrayList<PinRecord> logsList = new ArrayList<PinRecord>();
	public static final String ARG_SECTION_NUMBER = "section_number";


	//	  lv.setAdapter(new MessageItemAdapter(this.getActivity(), android.R.layout.simple_list_item_1, logsList));
	public FragmentConfiguration() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		View rootView = inflater.inflate(R.layout.fragment_configuration,
				container, false);


//		Button editGpio =(Button) rootView.findViewById(R.id.imageButton1);
		host = (EditText) rootView.findViewById(R.id.hostValue);
		user = (EditText) rootView.findViewById(R.id.usrValue);
		passw = (EditText) rootView.findViewById(R.id.pswValue);
		version = (TextView) rootView.findViewById(R.id.versionText);
		checkupdate = (Button) rootView.findViewById(R.id.button2);
//		manageslave = (Button) rootView.findViewById(R.id.button1);
		gpioUsed = (CheckBox) rootView.findViewById(R.id.gpioEnableValue);
		deviceUsed = (CheckBox) rootView.findViewById(R.id.checkBox1);
		savechanges = (Button) rootView.findViewById(R.id.button3);
		idkey =  (Button) rootView.findViewById(R.id.idKey);
		//		Version:  
		setData();
//		manageslave.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				getActivity().overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
//				getActivity().startActivity(new Intent(getActivity(), ActivitySlave.class));	
//			}
//		});
idkey.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {//RESET IDKEY
				new AlertDialog.Builder(getActivity())
				.setTitle("Save")
				.setMessage("Are you sure want to remove idKey? Use only if notify doesn't work")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { 
						getConf().writeIdKey("", "");
						MainActivity.setKey();
						setData();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { 
						
					}
				})
				.show();
				
			}
		});
		savechanges.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(getActivity())
				.setTitle("Save")
				.setMessage("Are you sure want to save changes?")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { 
						writeConfig();
//						GpioFragment.refresh();
//						DevicesFragment.refresh();
//						PlotFragment.refresh();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { 
						
					}
				})
				.show();
				
				
				
			}
		});
		checkupdate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			 MainActivity.checkUpdates();
			 setData();
				

			}
		});
//		editGpio.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//
//				getActivity().overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
//				getActivity().startActivity(new Intent(getActivity(), ActivityGpio.class));	
//			}
//		});
		return rootView;
	}

	
	public void writeConfig(){
		getConf().writeConf( gpioUsed.isChecked(), deviceUsed.isChecked());
		 SharedPreferences sharedpreferences = getActivity().getSharedPreferences("raspiConfiguration", Context.MODE_PRIVATE);
		 Editor editor = sharedpreferences.edit();
		 editor.putString("ip", host.getText().toString());
		 editor.putString("usr", user.getText().toString());
		 editor.putString("psw", passw.getText().toString());
		 editor.commit();
		MainActivity.checkUpdates();
		setData();
	}
	


	
//	@Override
//	public void onResume(){
//		super.onResume();
//		setData();
//	}

	public void setData(){
		float value = (float) (getConf().counter*0.100);
		version.setText("Version: "+value);
		Configuratore conf = getConf();
		host.setText(MainActivity.ip);
		user.setText(MainActivity.usr);
		passw.setText(MainActivity.psw);
		gpioUsed.setChecked(conf.GpioEnable);
		deviceUsed.setChecked(conf.DevicesEnable);
	}	

	public Configuratore getConf(){
		String lastPath=getActivity().getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
		Configuratore conf = new Configuratore(lastPath);
		conf.readFromFile();
		return conf;
	}
}