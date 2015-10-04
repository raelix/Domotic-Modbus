package com.domotica.raspimanager.fragments;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.Executors;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.domotica.raspimanager.Configuratore;
import com.domotica.raspimanager.MainActivity;
import com.domotica.raspimanager.MonitorResource;
import com.domotica.raspimanager.R;
import com.domotica.raspimanager.adapter.DevicesItemAdapter;
import com.domotica.raspimanager.adapter.GpioItemAdapter;
import com.domotica.raspimanager.record.RegisterRecord;
import com.domotica.raspimanager.record.SlaveRecord;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public  class FragmentDevices extends Fragment   {
	public static  GpioItemAdapter adapter ;
	public static  DevicesItemAdapter adapterexpandable ;
	public static   LinkedList<SlaveRecord> slaveList = new LinkedList<SlaveRecord>();
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static int registerCounter;
	public static int registerStarter;
	Runnable mUpdateResults;
	Handler mHandler;
	public static RequestTimer requestTimer;
	public static ListView list;
	public static ExpandableListView listexpand;
	static Activity activity;
	public static 	Timer	timer;
	boolean send = false;
	public static boolean error = false;
	LinkedList<Float> dataResult = new LinkedList<Float>();

	public FragmentDevices() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		View rootView = inflater.inflate(R.layout.fragment_devices,
				container, false);
		listexpand = (ExpandableListView) rootView.findViewById(R.id.listaespandibile);
		Configuratore conf = getConf();
		activity = this.getActivity();
		slaveList = new LinkedList<SlaveRecord>();
		mHandler = new Handler();
		//AGGIUNGERE IL CASO ION CUI I DEVICE SIANO DISABILITATI 
		int counterRegister = 1;
		if(conf.GpioEnable)counterRegister+=conf.gpioNumber;
		for(int i = 0; i < conf.slaves.size(); i++){
			SlaveRecord childItem = new SlaveRecord(conf.slaves.get(i).getName(), conf.slaves.get(i).getType());
			for(int k = 0 ; k < conf.slaves.get(i).getRegisters().size(); k++){
				childItem.addList(new RegisterRecord(conf.slaves.get(i).getRegisters().get(k).getName(), 
						counterRegister, conf.slaves.get(i).getRegisters().get(k).getScope(), 
						conf.slaves.get(i).getRegisters().get(k).isAlarm(), conf.slaves.get(i).getRegisters().get(k).getHighAlarm(),
						conf.slaves.get(i).getRegisters().get(k).getLowAlarm(),conf.slaves.get(i).getRegisters().get(k).getOptional()));
				counterRegister++;
			}
			slaveList.add(childItem);
		}
		adapterexpandable = new DevicesItemAdapter(getActivity(), slaveList);
		listexpand.setAdapter(adapterexpandable);
		listexpand.setDivider(null);
		listexpand.setDividerHeight(0);
		//		listexpand.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		timer = new Timer();
		requestTimer = new RequestTimer();
		MonitorResource.getInstance().setResourcesDevices(timer,requestTimer,this);

		mUpdateResults = new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				for(int i = 0; i < slaveList.size() &&  counter < dataResult.size(); i++){
					SlaveRecord slave = slaveList.get(i);
					for(int f = 0 ; f < slave.getList().size();f++){
						RegisterRecord record = slave.getList().get(f);
						record.setValue(dataResult.get(counter));
						slaveList.set(i, slave);
						counter++;
						//						System.out.println("DevicesFragment: Slave :"+slave.getName()+" Register:"+record.getName()+" "+dataResult.get(f));
					}
				}
				adapterexpandable.notifyDataSetChanged();
			}
		};
		// METODO PER RECUPERARE I PRIMI VALORI CALCOLANDOSI I NUMERI DI REGISTRI + OK SUCCESSIVAMENTE CHIAMO LA NOTIFY ; USO UN TIMER PER AGGIORNARE!
		return rootView;
	}


	public RequestTimer getRequestThread(){
		return new RequestTimer() ;
	}

	public class RequestTimer extends Thread{
		@Override
		public void run() {
			Thread.currentThread().setName("DevicesTimer");
			Configuratore conf = null;

			try {
				//				synchronized (this) {
				conf = getConf();
				int counterRegister = 1;
				if(conf.GpioEnable)counterRegister+=conf.gpioNumber;
				registerCounter = getRegisterCount();
				registerStarter = counterRegister;
				dataResult = new LinkedList<Float>();
				new readHR().executeOnExecutor(Executors.newSingleThreadExecutor() );
				//					synchronized (reading) {
				//						reading.wait();
				//				}
				//					notify();
				//				}

			} catch (Exception e) {
				System.out.println("Timer: errore lettura da device");
				e.printStackTrace();
			} 


		}
	}


	public int getRegisterCount(){
		Configuratore conf= getConf();
		int counterRegister = 0;
		for(int i = 0; i < conf.slaves.size(); i++){

			for(int k = 0 ; k < conf.slaves.get(i).getRegisters().size(); k++){
				counterRegister++;
			}
		}
		return counterRegister;
	}



	@SuppressLint("NewApi")
	public class readHR extends AsyncTask<Void, Void, Void> {
		int id = 0;
		ReadMultipleRegistersRequest req = null; //the request
		@SuppressLint("NewApi")
		ReadMultipleRegistersResponse res = null; //the response
		@Override
		protected Void doInBackground(Void... params) {
			//			synchronized (this) {

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			Thread.currentThread().setName("DevicesFragmentReadRegisterThread");
			LinkedList<Float> lista = new LinkedList<Float>();
			try {

				InetAddress addr = InetAddress.getByName("127.0.0.1");
				TCPMasterConnection	con = new TCPMasterConnection(addr);
				con.setPort(9001);
				//				con.setTimeout(5000);
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
					lista.add((float) res.getRegisters()[i].toShort());
				}
				con.close();
				error = false;
			} catch (Exception e) {
				//				e.printStackTrace();
				if(e.getLocalizedMessage().contains("ECONNREFUSED")) {
					System.out.println("ECONNREFUSED IN DEVICE FRAGMENT");
				}

				error = true;
				//				notify();
				return null;
			} 						
			dataResult = lista;
			mHandler.post(mUpdateResults);
			//			notify();
			//			}
			return null;
		} 
	}
	//	@Override
	//	public void onResume(){
	//		super.onResume();
	//		System.out.println("resume in GpioFragment");
	//		SSHConnection connection = new SSHConnection();
	//		connection.setActivity(getActivity());
	//		connection.execute(getConf().ip,getConf().usr,getConf().psw,"","");
	//	}

	public Configuratore getConf(){
		if(MainActivity.conf == null){
			String lastPath=getActivity().getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
			MainActivity.conf = new Configuratore(lastPath);
			MainActivity.conf.readFromFile();
			return 	MainActivity.conf;
		}
		else return MainActivity.conf;

	}
}