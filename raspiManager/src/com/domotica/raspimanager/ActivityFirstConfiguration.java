package com.domotica.raspimanager;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ActivityFirstConfiguration  extends Activity{
	public static String dir ;
	EditText ipValue ;
	EditText userValue;
	EditText passwordValue ;
	 SSHConnection connection;
	Button fetchFileButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_firstconfiguration);
		dir = getApplicationInfo().dataDir;
		 ipValue = (EditText) findViewById(R.id.ipValue);
		 userValue = (EditText) findViewById(R.id.userValue);
		 passwordValue = (EditText) findViewById(R.id.passwordValue);
		 fetchFileButton = (Button) findViewById(R.id.fetchFile);
		 final Activity act = this;
		 final String pathFile = dir+"/"+Configuratore.nameFile;
		fetchFileButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			
				 String ipValues = ipValue.getText().toString();
				 String userValues = userValue.getText().toString();
				 String passwordValues = passwordValue.getText().toString();
				 SharedPreferences sharedpreferences = getSharedPreferences("raspiConfiguration", Context.MODE_PRIVATE);
				 Editor editor = sharedpreferences.edit();
				 editor.putString("ip", ipValues);
				 editor.putString("usr", userValues);
				 editor.putString("psw", passwordValues);
				 editor.commit();
				  connection = new SSHConnection();
				 connection.setActivity(act);
				 connection.execute(ipValues,userValues,passwordValues,pathFile,"ReadFileFirst");
				 fetchFileButton.setText("Importa Configurazione");
				
//				 ServerRequest request = new ServerRequest();
//				 request.setActivity(act);
//				 request.execute();
//				startProgram();
				
			}
		});
		}
	public void startProgram(){
	
	String 	lastPath=getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
	while(!new File(lastPath).exists());
	if(getConf().idKey != null){
		 startActivity(new Intent(this, MainActivity.class));
		 finish();}
	
	}
	
	public void onDestroy(){
		super.onDestroy();
		
	}
	
	public Configuratore getConf(){
		String lastPath=getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
		Configuratore conf = new Configuratore(lastPath);
		conf.readFromFile();
		return conf;
	}
}
