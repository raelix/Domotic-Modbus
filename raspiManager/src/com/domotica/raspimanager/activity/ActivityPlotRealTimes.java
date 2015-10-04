package com.domotica.raspimanager.activity;

import java.net.InetAddress;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

import org.achartengine.GraphicalView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.domotica.raspimanager.Configuratore;
import com.domotica.raspimanager.R;
import com.domotica.raspimanager.shared.LineGraph;

public class ActivityPlotRealTimes extends Activity {
	Timer	timer;
	 RequestTimer requestTimer ;
	private static GraphicalView view;
	private LineGraph line = new LineGraph();
	private static Thread thread;
	public static String name;
	public static int indirizzo;
	public static boolean acceso = true;
	ImageButton pausa ;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plot_realtime);
		//		 System.out.println("ecco a te i valori "+Integer.toString(getIntent().getExtras().getInt(
		//					"indirizzo")));
		indirizzo = getIntent().getExtras().getInt(
				"indirizzo");
		name = getIntent().getExtras().getString(
				"nome");
			

		timer = new Timer();
		  requestTimer = new RequestTimer(indirizzo);
		timer.scheduleAtFixedRate(requestTimer,0,2000);
	}
	

	@Override
	protected void onStart() {
		super.onStart();

		view = line.getView(this);
		ViewGroup layout = ((ViewGroup) findViewById(R.id.plot_Activity));
		RelativeLayout.LayoutParams   lp = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		lp.addRule(RelativeLayout.ALIGN_LEFT , R.id.imageViewplot);
		lp.addRule(RelativeLayout.ALIGN_RIGHT , R.id.imageViewplot);
		lp.addRule(RelativeLayout.ALIGN_TOP , R.id.imageViewplot);
		lp.addRule(RelativeLayout.ALIGN_BOTTOM , R.id.imageViewplot);
		
//		 pausa = (Button) findViewById(R.id.pausa);
		
//		lp.width = 1550;
//		lp.height = 550;
		pausa = new ImageButton(this);
		RelativeLayout.LayoutParams   lp1 = new RelativeLayout.LayoutParams(155,
				135);
//		lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		lp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp1.addRule(RelativeLayout.ALIGN_LEFT , R.id.imageViewplot);
		lp1.addRule(RelativeLayout.ALIGN_BOTTOM , R.id.imageViewplot);
		
		pausa.setLayoutParams(lp1);
		view.setLayoutParams(lp);
		pausa.setScaleType(ScaleType.FIT_XY);
		pausa.setImageResource(R.drawable.paused);
		layout.addView(view);
		layout.addView(pausa);
pausa.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
if(acceso){
	timer.cancel();
	timer = null;
	acceso = false;
	pausa.setImageResource(R.drawable.play);
	
	System.out.println("cancellato");
	return;
}
else{
	timer = new Timer();
	  requestTimer = new RequestTimer(indirizzo);
	timer.scheduleAtFixedRate(requestTimer,0, 2000);	
	acceso = true;
	pausa.setImageResource(R.drawable.paused);
	System.out.println("avviato");
	return;
}
				
			}
		});
		//		setContentView(view);
	}


	public class RequestTimer extends TimerTask{
		int indirizzo;
		public RequestTimer(int indirizzo){
//			super();
			this.indirizzo = indirizzo;
		}

		@Override
		public void run() {
								System.out.println("Timer!!");
			
			//					synchronized(this){}
		new readHR().execute(indirizzo);
		return;
		
		}
		}  
	public class readHR extends AsyncTask<Integer, Void,Float> {
		int id = 0;
		ReadMultipleRegistersRequest req = null; //the request
		ReadMultipleRegistersResponse res = null; //the response
		@SuppressLint("NewApi")
		@Override
		protected Float doInBackground(Integer... params) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

			try {
				InetAddress addr = InetAddress.getByName("127.0.0.1");
				TCPMasterConnection	con = new TCPMasterConnection(addr);
				con.setPort(9001);
				//				con.setTimeout(5000);
				con.connect();
				req = new ReadMultipleRegistersRequest(params[0], 1);//es (3,1) = Register 0004 , 1 length
				req.setUnitID(id);
				ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
				trans.setRequest(req);
				trans.setRetries(1);
				trans.setReconnecting(true);
				trans.execute();
				res = (ReadMultipleRegistersResponse) trans.getResponse();

								System.out.println("readed "+(float) res.getRegisters()[0].toShort());
				line.addNewPoints((double) res.getRegisters()[0].toShort(), new Date()); // Add it to our graph
				view.repaint();
				//					view.repaint();

				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			} 							//con.setTimeout(1000);


			return (float) res.getRegisters()[0].toShort();
		} 
	}
	public Configuratore getConf(){
		String lastPath=getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
		Configuratore conf = new Configuratore(lastPath);
		conf.readFromFile();
		return conf;
	}

}