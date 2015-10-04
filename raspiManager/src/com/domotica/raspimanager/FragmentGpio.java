package com.domotica.raspimanager;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.Executors;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.camera.simplemjpeg.DoRead;
import com.camera.simplemjpeg.MjpegView;
import com.domotica.raspimanager.Adapter.GpioItemAdapter;
import com.domotica.raspimanager.Record.GpioRecord;
import com.domotica.raspimanager.shared.FadePageTrasformer;
import com.domotica.raspimanager.shared.PROTOCOL;
import com.domotica.raspimanager.shared.Titanic;
import com.domotica.raspimanager.shared.TitanicTextView;
import com.domotica.raspimanager.shared.Typefaces;

public  class FragmentGpio extends Fragment implements PROTOCOL {

	public static GpioItemAdapter adapter ;
	public static ArrayList<GpioRecord> gpioList = new ArrayList<GpioRecord>();
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static int registerCounter;
	public static int registerStarter;
	public static int k = 0;
	public static RequestTimer requestTimer;
	public static MjpegView mv;
	public static boolean use = true;
	public static Timer	timer;
	public static boolean error = false;
	public static ListView list;
	static Activity activity;
	boolean send = false;
	Runnable mUpdateResults;
	Handler mHandler;
	RelativeLayout.LayoutParams   lp;
	String	URL = "http://192.168.0.8:443/videostream.cgi?user=raelix&pwd=Enrico1952&resolution=32&rate=0";
	LinkedList<Boolean> dataResult = new LinkedList<Boolean>();

	public FragmentGpio() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_gpio,container, false);
		list = (ListView) rootView.findViewById(R.id.listviewgpio);
		
		gpioList = new ArrayList<GpioRecord>();
		mHandler = new Handler();
		Configuratore conf = getConf();
		if(conf == null)return rootView;
		activity = this.getActivity();
		for(int i = 0; i < conf.pins.size(); i++)
			gpioList.add(new GpioRecord(conf.pins.get(i).getName(),""+(i+1),
					false,conf.pins.get(i).getScope().contentEquals("in") ? true : false,
							conf.pins.get(i).getOptional()));
		
		adapter = new GpioItemAdapter(this.getActivity(), android.R.layout.simple_list_item_1, gpioList);
		list.setAdapter(adapter);
		registerForContextMenu(list);
		timer = new Timer();
		requestTimer = new RequestTimer();
		MonitorResource.getInstance().setResourcesGpio(timer,requestTimer,this);
		list.setDivider(null);
		list.setDividerHeight(0);
//		TitanicTextView tv = (TitanicTextView) rootView.findViewById(R.id.textView1gpio);
//
//	    // set fancy typeface
//		tv.setTypeface(Typefaces.get(getActivity(), "Satisfy-Regular.ttf"));
//	    // start animation
//	    new Titanic().start(tv);
		mUpdateResults = new Runnable() {
			@Override
			public void run() {
				if(error){
					MainActivity.status.setImageResource(R.drawable.led_red);
					return;
				}
				else 
					MainActivity.status.setImageResource(R.drawable.led_green);
				for(int i = 0; i < gpioList.size() &&  i < dataResult.size(); i++){
					GpioRecord record = gpioList.get(i);
					record.setStato(dataResult.get(i));
					gpioList.set(i, record);
				}
				if(use){
					adapter.notifyDataSetChanged();
				}
			}
		};
		return rootView;
	}
@Override
public void onStart(){
	super.onStart();

}


	public class RequestTimer extends Thread{
		@Override
		public void run() {
			Thread.currentThread().setName("GpioTimer");
			try {	
				Configuratore conf = getConf();
				if(conf == null)return;
				dataResult = new LinkedList<Boolean>();
				registerCounter = conf.gpioNumber;
				registerStarter = 1;
				new readHR().executeOnExecutor(Executors.newSingleThreadExecutor());
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	public RequestTimer getRequestThread(){
		return new RequestTimer() ;
	}


	public void remove(View view){
		((ViewGroup) view).removeView(mv);
		RelativeLayout interno = (RelativeLayout) view.findViewById(R.id.interno);
		RelativeLayout.LayoutParams  lv = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,	LayoutParams.WRAP_CONTENT);
		lv.addRule(RelativeLayout.ALIGN_TOP , R.id.imageView12);
		lv.addRule(RelativeLayout.ALIGN_LEFT , R.id.imageView12);
		lv.addRule(RelativeLayout.ALIGN_RIGHT , R.id.imageView12);
		lv.addRule(RelativeLayout.ALIGN_BOTTOM , R.id.imageView12);
		interno.setLayoutParams(lv);
	}


	public void addJpeg(View view){
		//BISOGNA CREARE UNA LINKED LIST DI MJPEGVIEW
		mv = new MjpegView(getActivity()); 
		RelativeLayout relat = (RelativeLayout) view.findViewById(R.id.relat);
		lp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.ALIGN_BOTTOM , R.id.imageView12);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		lp.bottomMargin = 250;
		lp.width = 640;
		lp.height =480;
		if(mv != null) mv.setResolution(640,480);
		mv.setLayoutParams(lp);
		relat.addView(mv);
		RelativeLayout interno = (RelativeLayout) view.findViewById(R.id.interno);
		RelativeLayout.LayoutParams  lv = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lv.addRule(RelativeLayout.ALIGN_TOP , R.id.imageView12);
		lv.addRule(RelativeLayout.ALIGN_LEFT , R.id.imageView12);
		lv.addRule(RelativeLayout.ALIGN_RIGHT , R.id.imageView12);
		lv.addRule(RelativeLayout.ABOVE , R.id.relat);
		interno.setLayoutParams(lv);
		//		LayoutTransition lt = new LayoutTransition();
		//		lt.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
		//		lt.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
		//		((ViewGroup) view).setLayoutTransition(lt);
		//		MjpegInputStream input = null;
//		mv.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.enter_from_down));
//		YoYo.with(Techniques.BounceInLeft)
//	    .duration(2000)
//	    .playOn(findViewById(R.id.pager));
		new DoRead().execute("http://192.168.0.8:443/videostream.cgi?user=raelix&pwd=Enrico1952&resolution=32&rate=0");
	}



	public class readHR extends AsyncTask<Void, Void, Void> {
		int id = 0;
		ReadMultipleRegistersRequest req = null; //the request
		ReadMultipleRegistersResponse res = null; //the response
		@Override
		protected Void doInBackground(Void... params) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			Thread.currentThread().setName("GpioFragmentReadRegisterThread");
			LinkedList<Boolean> lista = new LinkedList<Boolean>();
			try {
				InetAddress addr = InetAddress.getByName("127.0.0.1");
				TCPMasterConnection	con = new TCPMasterConnection(addr);
				con.setPort(9001);
				con.connect();
				req = new ReadMultipleRegistersRequest(registerStarter, registerCounter);//es (3,1) = Register 0004 , 1 length
				req.setUnitID(id);
				ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
				trans.setRequest(req);
				trans.setRetries(0);
				trans.setReconnecting(false);
				trans.execute();
				res = (ReadMultipleRegistersResponse) trans.getResponse();
				for(int i = 0 ; i < res.getRegisters().length ; i++){
					lista.add((res.getRegisterValue(i) == IN_OK || res.getRegisterValue(i) == OUT_OFF) ? false : true);
				}

				con.close();
				error = false;
			} catch (Exception e) {
				error = true;
				if(e.getLocalizedMessage().contains("ECONNREFUSED")) {
					System.out.println("ECONNREFUSED in GpioFragment");
				}
				//				if(!MonitorResource.getInstance().getBlockServerEmpty() && k == 1){
				//					MonitorResource.getInstance().blockServerEmpty(true);
				////					MonitorResource.getInstance().stopMonitor();
				//				MainActivity.checkUpdates();
				//				if(k==1)k=0;}
				//				else k++;

				mHandler.post(mUpdateResults);
				return null;
			} 	
			k = 0;
			dataResult = lista;
			mHandler.post(mUpdateResults);
			return null;
		} 
	}


	public Configuratore getConf(){
		Configuratore conf =null;
		try{
			if(MainActivity.conf == null){
				String lastPath=getActivity().getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
				conf = new Configuratore(lastPath);
				conf.readFromFile();
			}
			else return MainActivity.conf;
		}
		catch(Exception e){
			return null;
		}

		return conf;
	}

	@Override
	public void onResume(){
		super.onResume();
		System.out.println("onResume");
		MonitorResource.getInstance().startMonitor();
		System.out.println("Avvio Monitor!");
	}

	@Override
	public void onPause(){
		super.onPause();
		System.out.println("onPause");
		FadePageTrasformer.clearObject();
	}

	@Override
	public void onDestroyView(){	
		super.onDestroyView();
		System.out.println("onDestroyView");
		FadePageTrasformer.clearObject();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		System.out.println("onDestroy");
		FadePageTrasformer.clearObject();
	}

	@Override
	public void onDetach(){
		super.onDetach();
		System.out.println("onDetach");
		FadePageTrasformer.clearObject();
	}


}