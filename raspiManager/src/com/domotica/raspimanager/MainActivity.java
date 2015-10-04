package com.domotica.raspimanager;

import static com.domotica.raspimanager.GCMIntentService.SENDER_ID;

import java.io.File;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.domotica.raspimanager.activity.ActivityFirstConfigurations;
import com.domotica.raspimanager.activity.ActivityVoiceRecognitions;
import com.domotica.raspimanager.fragments.FragmentConfiguration;
import com.domotica.raspimanager.fragments.FragmentConfigurationRaspi;
import com.domotica.raspimanager.fragments.FragmentDevices;
import com.domotica.raspimanager.fragments.FragmentGpio;
import com.domotica.raspimanager.server.ConnectionManager;
import com.domotica.raspimanager.shared.FadePageTrasformer;
import com.domotica.raspimanager.shared.SystemBarTintManager;
import com.google.android.gcm.GCMRegistrar;

//CALCOLO PINZA AMPEROMETRICA emon1.current(2, 28.0); (Irms*230*1.015);
public class MainActivity extends FragmentActivity {
	public static String ip;
	public static String usr;
	public static String psw;
	public static String dir ;
	public static FadePageTrasformer fade;
	public static String lastPath;
	public static Activity activity;
	public static Configuratore conf ;
	public static Handler handle ;
	public static int readCounter ;
	public static ImageView status;
	protected SectionsPagerAdapter mSectionsPagerAdapter;
	protected ViewPager mViewPager;
	//	public static Handler handleServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		loadInfo();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) setTranslucentStatus(true);
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.statusbar_bg);
		status = (ImageView)findViewById(R.id.ledd);
		activity = this;
		ImageView speech = (ImageView) findViewById(R.id.speech);
		dir = getApplicationInfo().dataDir;
		YoYo.with(Techniques.BounceInDown)
		.duration(1800)
		.playOn(findViewById(R.id.speech));
		YoYo.with(Techniques.SlideInLeft)
		.duration(2000)
		.playOn(findViewById(R.id.principale));
		lastPath=dir+"/configurationNewRaspiServer.xml";
		dir = dir+"/";
		if(!new File(lastPath).exists() || new File(lastPath).exists() && getConf().idKey == null)	{
			startActivity(new Intent(this, ActivityFirstConfigurations.class));
			finish();
		}
		else {
			conf = new Configuratore(lastPath);
			conf.readFromFile();
			if(conf.idKey!= null && conf.idKey.contentEquals("")){
				GCMRegistrar.checkDevice(this);
				GCMRegistrar.checkManifest(this);
				String regId = GCMRegistrar.getRegistrationId(this);
				GCMRegistrar.register(this, SENDER_ID);
				regId = GCMRegistrar.getRegistrationId(this);
				if(!regId.contains("null")){
					conf.writeIdKey(lastPath, regId);
				}
			}
			MonitorResource.getInstance();
			checkConnection();
		}

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		fade	 = new FadePageTrasformer();
		mViewPager.setPageTransformer(true, fade);
		//		final ParallaxViewPager parallaxViewPager = ((ParallaxViewPager) findViewById(R.id.pager));
		//		parallaxViewPager.setBackgroundResource(R.drawable.backbg1);
		//		parallaxViewPager.setOffscreenPageLimit(6);
		//		parallaxViewPager.setOverlapPercentage(0.5f);
		//		parallaxViewPager.setAdapter(mSectionsPagerAdapter);
		//		parallaxViewPager.setBackgroundResource(R.drawable.bg3);
		//		parallaxViewPager.setPageTransformer(true, fade);
		//		setContentView(parallaxViewPager);
		//		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);//TUTTO SCHERMO
		speech.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.startActivity(new Intent(activity, ActivityVoiceRecognitions.class));
			}
		});
	} 

	public void loadInfo(){
		SharedPreferences sharedpreferences = getSharedPreferences("raspiConfiguration", Context.MODE_PRIVATE);
		ip = sharedpreferences.getString("ip", "");
		usr =  sharedpreferences.getString("usr", "");
		psw = sharedpreferences.getString("psw", "");
	}

	public void toggleHideyBar() {
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
	}

	@TargetApi(19) 
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	public void checkConnection(){
		handle = new handlerMessage();
		//		handleServer = new MonitorResourceHandler();
		ConnectionManager.getSingleton();
		ConnectionManager.setConnectionContext(activity);
		ConnectionManager.startSSHConnection();
	}

	static class handlerMessage extends Handler{
		@Override
		public void handleMessage(Message msg) {
			int aResponse = msg.getData().getInt("status");
			switch(aResponse){
			case 0:
				ConnectionManager.screenAlert("Parametri SSH errati", MainActivity.activity);
				break;
			case 1:
				checkUpdates();
				break;
			default:
				break;
			}
		}
	}

	//	static class MonitorResourceHandler extends Handler{
	//		@Override
	//		public void handleMessage(Message msg) {
	//			int aResponse = msg.getData().getInt("statusServer");
	//			switch(aResponse){
	//			case 0:
	//				//ConnectionManager.screenAlert("Server disattivo, blocco ricezione", MainActivity.activity);
	//				MonitorResource.getInstance().blockServerEmpty(true);
	//				break;
	//			case 1:
	//				//ConnectionManager.screenAlert("Server attivo, avvio ricezione", MainActivity.activity);
	//				MonitorResource.getInstance().blockServerEmpty(false);
	//				break;
	//			default:
	//				break;
	//			}
	//		}
	//	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(FadePageTrasformer.ENABLE_FADE == 0){
			FadePageTrasformer.ENABLE_FADE = 1;
		}else FadePageTrasformer.ENABLE_FADE = 0;
		//		toggleHideyBar();

		return true;
	}
	public static void setKey(){
		GCMRegistrar.checkDevice(activity);
		GCMRegistrar.checkManifest(activity);
		String regId = GCMRegistrar.getRegistrationId(activity);
		GCMRegistrar.register(activity, SENDER_ID);
		regId = GCMRegistrar.getRegistrationId(activity);
		if(!regId.contains("null"))
			conf.writeIdKey(lastPath, regId);
		checkUpdates();
	}


	public static int checkUpdates(){
		ConnectionManager.getSingleton();
		ConnectionManager.setConnectionContext(activity);
		ConnectionManager.startConnection(lastPath,"ReadCounter");
		return 0;
	}
	public static Configuratore getConf(){
		if(conf == null){
			String lastPath = activity.getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
			conf = new Configuratore(lastPath);
			conf.readFromFile();
			return conf;
		}
		else return conf;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		@Override
		public Fragment getItem(int position) {
			switch(position){
			case 1:
				Fragment fragment = new FragmentDevices();
				Bundle args = new Bundle();
				args.putInt(FragmentConfiguration.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			case 2:
				fragment = new FragmentConfiguration();
				args = new Bundle();
				args.putInt(FragmentConfiguration.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			case 3:
				fragment = new FragmentConfigurationRaspi();
				args = new Bundle();
				args.putInt(FragmentConfiguration.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			case 0:
				fragment = new FragmentGpio();
				args = new Bundle();
				args.putInt(FragmentConfiguration.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 3:
				return "Configuration Raspi";
			case 2:
				return "Configuration";
			case 1:
				return "Device";
			case 0:
				return "Gpio";
			}
			return null;
		}
	}

	public class ProgressMessageTask extends AsyncTask<String, String, String> {
		final	ProgressDialog  dialogo = ProgressDialog.show(activity, "Checking Updates...","" , true);
		@Override
		protected String doInBackground(String... params) {
			return "";
		}
		protected void OnProgressUpdate(String... values) { 
			super.onProgressUpdate("");
		}
		protected void OnPreExecute(){
			dialogo.show();
		}
		@Override
		protected void onPostExecute(String result){
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					dialogo.dismiss();
				}   
			}, 2000);
		}
	}
}