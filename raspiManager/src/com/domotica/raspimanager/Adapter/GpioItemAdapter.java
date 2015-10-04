package com.domotica.raspimanager.adapter;

import java.net.InetAddress;
import java.util.ArrayList;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.camera.simplemjpeg.DoRead;
import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;
import com.domotica.raspimanager.Configuratore;
import com.domotica.raspimanager.MainActivity;
import com.domotica.raspimanager.R;
import com.domotica.raspimanager.record.GpioRecord;
import com.domotica.raspimanager.shared.ChromeHelpPopup;
import com.domotica.raspimanager.shared.FadePageTrasformer;
import com.domotica.raspimanager.shared.Utils;


public class GpioItemAdapter  extends ArrayAdapter<GpioRecord>  {
	private ArrayList<GpioRecord> gpiolist;
	private Context context;
	public GpioItemAdapter(Context context, int textViewResourceId, ArrayList<GpioRecord> gpiolist) {
		super(context, textViewResourceId, gpiolist);
		this.gpiolist = gpiolist;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			if(gpiolist.get(position).inout){	
				convertView = inflater.inflate(R.layout.item_pin_in, parent, false);
			}
			else {
				convertView = inflater.inflate(R.layout.item_pin_out, parent, false);
			}
			Animation animation = new AlphaAnimation(0.0f,1.0f);  
			animation.setFillAfter(true);  
			animation.setDuration(150);  
			animation.setStartOffset(position * 100);  
			convertView.startAnimation(animation);  
			//slideToTop(convertView);
		}
		if(gpiolist.get(position).inout){	
			inflateIn(convertView, position);
		}
		else {
			inflateOut(convertView, position);
		}
		return convertView;
	}


	public void inflateIn(View v,int position){
		GpioRecord gpioPin = gpiolist.get(position);
		if (gpioPin != null) {
			TextView nome = (TextView) v.findViewById(R.id.textView1);
			final CheckBox button = (CheckBox) v.findViewById(R.id.checkBox1);
			final int indirizzo =  Utils.strToInt(gpioPin.getIndirizzo());
			nome.setText(gpioPin.getNome());//false 0 , true 1
			button.setOnCheckedChangeListener(null);
			button.setChecked(gpioPin.isStato());
			Configuratore conf = getConf();
			final String helper = "Gpio number "+conf.pinsUsed.get(Utils.strToInt(gpioPin.getIndirizzo())).getNumber()+" address 4000"+gpioPin.getIndirizzo();
			nome.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ChromeHelpPopup chromeHelpPopup = new ChromeHelpPopup(context,helper);
					chromeHelpPopup.show(v);
				}
			});
			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					new writeHR().execute(indirizzo, button.isChecked() ? 21 : 20);
				}
			});
		}
	}

	public void inflateOut(View vi,int position ){
		GpioRecord gpioPin = gpiolist.get(position);
		final GpioRecord rec = gpioPin;
		if (gpioPin != null) {	
			final RelativeLayout relative = (RelativeLayout) vi.findViewById(R.id.layoutinterno0);
			TextView nome = (TextView) vi.findViewById(R.id.textView2);
			final CheckBox button = (CheckBox) vi.findViewById(R.id.checkBox2);
			final int indirizzo =  Utils.strToInt(gpioPin.getIndirizzo());
			nome.setText(gpioPin.getNome());
			button.setOnCheckedChangeListener(null);
			button.setChecked(gpioPin.isStato());
			FadePageTrasformer.addObject(button);
			FadePageTrasformer.addObject(nome);
			Configuratore conf = getConf();
			final String helper = "Gpio number "+conf.pinsUsed.get(Utils.strToInt(gpioPin.getIndirizzo())).getNumber()+" address 4000"+gpioPin.getIndirizzo();
			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					new writeHR().execute(indirizzo, button.isChecked() ? 11 : 10);
					Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotate_center_point);
					button.startAnimation(animation);
					if(rec.getOptional() != null && button.isChecked()){
						String ip=	rec.getOptional().getValue("ip");
						String user = rec.getOptional().getValue("user");
						String password = rec.getOptional().getValue("password");
						if(!rec.optionalIsShow){
							addJpeg(relative, ip,  user,  password,rec);
							rec.optionalIsShow = true;
						}
					}
					else if(rec.getOptional() != null && !button.isChecked()){
						if(rec.optionalIsShow ){
							removeJpeg(relative);
							rec.optionalIsShow  = false;
						}
					}
				}
			});
			nome.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ChromeHelpPopup chromeHelpPopup = new ChromeHelpPopup(context,helper);
					chromeHelpPopup.show(v);
				}
			});
		}
	}

	public Configuratore getConf(){
		if(MainActivity.conf == null){
			String lastPath= context.getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
			MainActivity.conf = new Configuratore(lastPath);
			MainActivity.conf.readFromFile();
			return 	MainActivity.conf;
		}
		else return MainActivity.conf;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	public void slideToBottom( View view){
		TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
		animate.setDuration(1000);
		animate.setFillAfter(true);
		view.startAnimation(animate);
	}

	public void slideToTop(final View view){
		TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
		animate.setDuration(1000);
		animate.setFillAfter(true);
		view.startAnimation(animate);
		//		view.setVisibility(View.GONE);
		animate.setAnimationListener(new AnimationListener()  {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override public void onAnimationEnd(Animation animation)  {  }
				//				((ViewGroup) view.getParent()).removeView(view);
		});
	}

	public void removeJpeg(View view){
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.exit_from_up);
		final View vi = view;
		((ViewGroup) view).getChildAt(0).startAnimation(anim);
		anim.setAnimationListener(new AnimationListener()  {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationEnd(Animation animation)  {
				((RelativeLayout)vi).removeViewAt(0);
			}
		});
	}

	@SuppressLint("NewApi")
	public void addJpeg(View view,String ip, String user, String password,final GpioRecord gpioPin ){
		//		final SwipeDetector swipeDetector = new SwipeDetector();
		//		view.setOnTouchListener(swipeDetector);
		MjpegView mv = new MjpegView(context); 
		RelativeLayout.LayoutParams   lp = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		lp.width = 640;
		lp.height =480;
		if(mv != null) mv.setResolution(640,480);
		mv.setLayoutParams(lp);
		((ViewGroup) view).addView(mv);
		final View clicked = view;
		final MjpegView mvclicked = mv;
		mv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((RelativeLayout)clicked).removeView(mvclicked);
				gpioPin.optionalIsShow = false;
			}
		});
		//		slideToBottom(mv);
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.enter_from_up);
		mv.startAnimation(anim);
		MjpegInputStream result = null;
		try {
			result = new DoRead().execute("http://raelixx.ns0.it:443/videostream.cgi?user=raelix&pwd=Enrico1952&resolution=32&rate=0").get();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		mv.setSource(result);
		if(result!=null) result.setSkip(1);
		mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		mv.showFps(true);
	}

	public class writeHR extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			try {
				TCPMasterConnection con = null;
				WriteSingleRegisterRequest WriteReq = new WriteSingleRegisterRequest(); 
				WriteSingleRegisterResponse WriteRes = new WriteSingleRegisterResponse();
				SimpleRegister MyReg = new SimpleRegister(0);// 0 - default
				InetAddress addr = InetAddress.getByName("127.0.0.1");
				con = new TCPMasterConnection(addr);
				con.setPort(9001);	
				con.connect(); 							//con.setTimeout(1000);
				WriteReq.setUnitID(1); 				//id unità 0 tutti gli id, > 1 iD specifico //in broadcast cioe 0 niente risposta, > 0 risposta valore settato 
				MyReg.setValue(params[1]);					//valore da impostare nel registro scelto Reference - 1
				WriteReq.setReference(params[0]);   	//indirizzo registro es(0) scrivo nel registro 1, es(35) scrivo nel registro 36
				WriteReq.setRegister(MyReg);
				ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
				trans.setRequest(WriteReq);
				trans.setReconnecting(true);
				trans.setRetries(1);
				trans.execute();
				WriteRes.setUnitID(1);
				WriteRes = (WriteSingleRegisterResponse) trans.getResponse();
				con.close();
			} 
			catch (Exception e) {
				System.out.println("error write "+e.getMessage());
			}
			return null;
		}
	}




}