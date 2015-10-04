package com.domotica.raspimanager.adapter;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;
import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.camera.simplemjpeg.DoRead;
import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;
import com.domotica.raspimanager.Configuratore;
import com.domotica.raspimanager.R;
import com.domotica.raspimanager.activity.ActivityHistoryGraphs;
import com.domotica.raspimanager.activity.ActivityPlotRealTimes;
import com.domotica.raspimanager.record.RegisterRecord;
import com.domotica.raspimanager.record.SlaveRecord;
import com.domotica.raspimanager.shared.ChromeHelpPopup;
import com.domotica.raspimanager.shared.PROTOCOL;
import com.domotica.raspimanager.shared.Slave;
import com.domotica.raspimanager.shared.Thermometer;

public class DevicesItemAdapter  extends BaseExpandableListAdapter  implements PROTOCOL  {
	private Activity context;
	public static boolean isFocused= false;
	public HashMap<Integer, View> map = new HashMap<Integer, View>();
	private LinkedList<SlaveRecord> listDataHeader; // header titles
	// child data in format of header title, child title
	private static int TYPE = 3;
	private static int TYPE_IN = 0;
	private static int TYPE_OUT = 1;
	LayoutInflater inflater;
	public DevicesItemAdapter(Activity context, LinkedList<SlaveRecord> listDataHeader) {
		super();
		this.context = context;
		this.listDataHeader = listDataHeader;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getChildType(final int groupPosition, final int childPosition){
		return listDataHeader.get(groupPosition).getList().get(childPosition).getScope().contains("out") ?  TYPE_OUT :  TYPE_IN;
	}
	@Override
	public int getChildTypeCount(){return TYPE;}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub

		//		System.out.println("recupero ogg "+(listDataHeader.get(groupPosition).getList().get(childPosition).getName()));
		return listDataHeader.get(groupPosition).getList().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		//		System.out.println("sono qui "+((int)listDataHeader.get(groupPosition).getList().get(childPosition).getAddress()-getConf().gpioNumber - 1));
		return listDataHeader.get(groupPosition).getList().get(childPosition).getAddress()-getConf().gpioNumber - 1;
	}
	public Configuratore getConf(){
		String lastPath=context.getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
		Configuratore conf = new Configuratore(lastPath);
		conf.readFromFile();
		return conf;
	}
	@Override
	public View getChildView(int groupPosition , int childPosition , boolean isLastChild , View convertView ,
			ViewGroup parent) {
		RegisterRecord record  =((SlaveRecord) getGroup(groupPosition)).getList().get(childPosition);

		if(record.getScope().contains("default") && (convertView == null || convertView.getId() != R.id.defaults)){
			convertView = inflater.inflate(R.layout.item_register, parent,false);
			convertView.setId(R.id.defaults);
		}
		else if(record.getScope().contains("in") && (convertView == null || convertView.getId() != R.id.in)){
			convertView =  inflater.inflate(R.layout.item_pin_in, parent,false);
			convertView.setId(R.id.in);
		}
		else if(record.getScope().contains("out")  && (convertView == null || convertView.getId() != R.id.out)){
			convertView = inflater.inflate(R.layout.item_pin_out, parent,false);
			convertView.setId(R.id.out);
		}
		else if(record.getScope().contains("gauge")  && (convertView == null || convertView.getId() != R.id.relativelayout)){
			convertView =  inflater.inflate(R.layout.item_gauge, parent,false);
			convertView.setId(R.id.relativelayout);
			initGaugeView(convertView, record);
		}


		if(record.getScope().contains("default") ){
			setDefault(groupPosition, childPosition, isLastChild, convertView, parent,record);
		}

		else if(record.getScope().contains("in") ){
			setIn(groupPosition, childPosition, isLastChild, convertView, parent,record);
		}
		else if(record.getScope().contains("out") ){
			setOut(groupPosition, childPosition, isLastChild, convertView, parent,record);
		}

		else if(record.getScope().contains("gauge")){
			initGaugeView(convertView, record);
			setGauge(groupPosition, childPosition, isLastChild, convertView, parent,record);
		}

		return convertView ;
	}

	public View setOut(int groupPosition , int childPosition , boolean isLastChild , View convertView ,
			ViewGroup parent  ,RegisterRecord record ){

		if(record != null){
			//		final RelativeLayout relative = (RelativeLayout) convertView.findViewById(R.id.layoutinterno0);
			TextView nome = (TextView) convertView.findViewById(R.id.textView2);
			CheckBox buttons = (CheckBox) convertView.findViewById(R.id.checkBox2);
			nome.setOnClickListener(null);


			final String helper = "Device register "+record.getAddress();
			nome.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ChromeHelpPopup chromeHelpPopup = new ChromeHelpPopup(context,helper);
					chromeHelpPopup.show(v);

				}
			});
			nome.setText(record.getName());
			buttons.setTag(record);
			buttons.setOnCheckedChangeListener(null);
			buttons.setChecked(record.getValue() == PROOUT_OFF ? false:true);
			buttons.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					RegisterRecord record = ((RegisterRecord) buttonView.getTag());
					new writeHR().executeOnExecutor(Executors.newSingleThreadExecutor() ,record.getAddress(),isChecked ? PROOUT_ON : PROOUT_OFF);
					System.out.println("premuto bottone! "+record.getName());
					if(record.getOptional() != null && isChecked){
						System.out.println("Pressed record nn nullo button check");
						String ip=	record.getOptional().getValue("ip");
						String user = record.getOptional().getValue("user");
						String password = record.getOptional().getValue("password");
						if(!record.optionalIsShow){
							addJpeg(((View) buttonView.getParent()).findViewById(R.id.layoutinterno0), ip,  user,  password,record);
							record.optionalIsShow = true;
						}
					}
					else if(record.getOptional() != null && !isChecked){
						if(record.optionalIsShow ){
							removeJpeg(((View) buttonView.getParent()).findViewById(R.id.layoutinterno0));
							record.optionalIsShow  = false;
						}
					}
				}
			});
		}
		return convertView;
	}

	public View setIn(int groupPosition , int childPosition , boolean isLastChild , View convertView ,
			ViewGroup parent  ,RegisterRecord record ){

		if(record != null){
			//			final RelativeLayout relative = (RelativeLayout) convertView.findViewById(R.id.layoutinterno1);
			TextView nome = (TextView) convertView.findViewById(R.id.textView1);
			CheckBox buttons = (CheckBox) convertView.findViewById(R.id.checkBox1);
			nome.setOnClickListener(null);
			nome.setText(record.getName());
			int trueaddress;
			Configuratore c = getConf();
			trueaddress= record.getAddress() - c.gpioNumber - STATUS_OK;
			final String helper = "Device register "+trueaddress;
			buttons.setTag(record);
			buttons.setOnCheckedChangeListener(null);
			buttons.setChecked(record.getValue() == PROIN_OK ? false:true);
			nome.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ChromeHelpPopup chromeHelpPopup = new ChromeHelpPopup(context,helper);
					chromeHelpPopup.show(v);

				}
			});
			buttons.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					RegisterRecord record = ((RegisterRecord) buttonView.getTag());
					new writeHR().executeOnExecutor(Executors.newSingleThreadExecutor() ,record.getAddress(),isChecked ? PROIN_OK : PROIN_OK);
					if(record.getOptional() != null && isChecked){
						String ip=	record.getOptional().getValue("ip");
						String user = record.getOptional().getValue("user");
						String password = record.getOptional().getValue("password");
						if(!record.optionalIsShow){
							addJpeg(((View) buttonView.getParent()).findViewById(R.id.layoutinterno1), ip,  user,  password,record);
							record.optionalIsShow = true;
						}
					}
					else if(record.getOptional() != null && !isChecked){
						if(record.optionalIsShow ){
							removeJpeg(((View) buttonView.getParent()).findViewById(R.id.layoutinterno1));
							record.optionalIsShow  = false;
						}
					}
				}
			});
		}
		return convertView;
	}

	public View setDefault(int groupPosition , int childPosition , boolean isLastChild , View convertView ,ViewGroup parent ,RegisterRecord record ){
		final RegisterRecord recordfinal = record;
		if(record != null){
			//			final RelativeLayout relative = (RelativeLayout) convertView.findViewById(R.id.layoutinterno);
			TextView gruppotext = (TextView) convertView.findViewById(R.id.titoloregistro);
			EditText values = (EditText) convertView.findViewById(R.id.valore);
			ImageButton button = (ImageButton) convertView.findViewById(R.id.imageButton1);
			final  EditText value = values;
			gruppotext.setText(record.getName());
			button.setOnClickListener(null);
			gruppotext.setOnClickListener(null); 
			int trueaddress;
			Configuratore c = getConf();
			trueaddress = record.getAddress() - c.gpioNumber - STATUS_OK;
			int counter = 0;
			int valuet = 0;
			for(int i = 0 ; i < c.slaves.size(); i++){
				Slave reg = c.slaves.get(i);
				for(int f = 0; f < reg.getRegisters().size(); f++){
					if(counter == trueaddress)valuet = reg.getRegisters().get(f).getAddress(); 
					counter++;
				}
			}
			if(!value.isFocused())
				value.setText(""+record.getValue());
			final int position = record.getAddress();
			final String helper = "Device register "+valuet;
			gruppotext.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ChromeHelpPopup chromeHelpPopup = new ChromeHelpPopup(context,helper);
					chromeHelpPopup.show(v);

				}
			});
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new writeHR().execute(position,(int)Float.parseFloat(value.getText().toString()));
					//					if(recordfinal.getOptional() != null  && !recordfinal.getOptionalIsShow()){
					//						String ip=	recordfinal.getOptional().getValue("ip");
					//						String user = recordfinal.getOptional().getValue("user");
					//						String password = recordfinal.getOptional().getValue("password");
					//						if(!recordfinal.optionalIsShow){
					//							addJpeg(relative, ip,  user,  password,recordfinal);
					//							recordfinal.optionalIsShow = true;
					//						}
					//					}
					//					else if(recordfinal.getOptional() != null && recordfinal.getOptionalIsShow()){
					//						removeJpeg(relative);
					//						recordfinal.optionalIsShow  = false;
					//
					//					}
				}
			});
		}
		return convertView;
	}


	public void removeJpeg(View view){
		((RelativeLayout)view).removeViewAt(0);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void addJpeg(View view,String ip, String user, String password,final RegisterRecord registerPin ){
		//		final SwipeDetector swipeDetector = new SwipeDetector();
		//		view.setOnTouchListener(swipeDetector);

		MjpegView mv = new MjpegView(context); 
		RelativeLayout.LayoutParams   lp = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		lp.width = 640;
		lp.height =480;
		if(mv != null){
			mv.setResolution(640,480);
		}
		mv.setLayoutParams(lp);
		((ViewGroup) view).addView(mv);
		final View clicked = view;
		final MjpegView mvclicked = mv;
		mv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((RelativeLayout)clicked).removeView(mvclicked);
				registerPin.optionalIsShow = false;
			}
		});
		LayoutTransition lt = new LayoutTransition();
		lt.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
		lt.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
		((ViewGroup) view).setLayoutTransition(lt);
		mv.setAnimation(AnimationUtils.loadAnimation(context, R.anim.enter_from_up));
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
	public void initGaugeView(View convertView, RegisterRecord record){


		//			Thermometer term = new Thermometer(context);
		Thermometer term = (Thermometer) convertView.findViewById(R.id.termomether);
		//			View termo = term.getView();
		term.scaleCenterValue = (int) (record.getHighAlarm()/2); // the one in the top center (12 o'clock)
		term.scaleMinValue = 0;
		term.scaleMaxValue = (int) record.getHighAlarm();
		term.totalNotches = term.scaleMaxValue;
		term.degreesPerNotch = 360.0f / term.totalNotches;	
		term.incrementPerLargeNotch =  term.scaleMaxValue / 10;
		term.handPosition = record.getValue();
		term.change(record.getValue());
		RelativeLayout.LayoutParams   lp = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		lp.topMargin = 0;
		lp.width = 650;
		lp.height = 650;
		lp.bottomMargin=50;
		term.scaleUpperTitle = record.getName();
		term.scaleLowerTitle = ""+(int)record.getValue();
		term.setLayoutParams(lp);
		((ViewGroup) convertView).removeView(term);
		((ViewGroup) convertView).addView(term);



	}
	public View setGauge(int groupPosition , int childPosition , boolean isLastChild , View convertView ,
			ViewGroup parent,RegisterRecord record ){

		final RegisterRecord recordfinal = record;


		ImageView realtime = (ImageView) convertView.findViewById(R.id.realtimelog);
		ImageView historylog = (ImageView) convertView.findViewById(R.id.historylog);

		historylog.setOnClickListener(null);
		realtime.setOnClickListener(null);

		final String nome = record.getName();
		final float address = record.getAddress();
		historylog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), ActivityHistoryGraphs.class);
				Bundle bundle = new Bundle();
				bundle.putString("nome", nome);
				bundle.putInt("indirizzo", (int) address);
				// Here key is just the "Reference Name" and txt1 is the EditText value
				intent.putExtras(bundle);               
				context. startActivity(intent);

			}
		});
		realtime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), ActivityPlotRealTimes.class);
				Bundle bundle = new Bundle();
				bundle.putString("nome", nome);
				bundle.putInt("indirizzo", (int) address);
				// Here key is just the "Reference Name" and txt1 is the EditText value
				intent.putExtras(bundle);               
				context. startActivity(intent);
				//				context.startActivity(new Intent(context, PlotActivity.class));	

			}
		});
		return convertView;
	}
	@Override
	public int getChildrenCount(int groupPosition) {

		return listDataHeader.get(groupPosition).getList().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

		if (convertView == null) {

			convertView = inflater.inflate(R.layout.item_slave,parent,false);

		}
		TextView gruppotext = (TextView) convertView.findViewById(R.id.titolo);
		TextView tipologiatext = (TextView) convertView.findViewById(R.id.tipologia);
		gruppotext.setText( ((SlaveRecord) getGroup(groupPosition)).getName());
		tipologiatext.setText( ((SlaveRecord) getGroup(groupPosition)).getType());
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
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
				//				System.out.println( "Write value"+WriteRes.getRegisterValue());
				con.close();
			} 

			catch (Exception e) {
				System.out.println("error write "+e.getMessage());
				//			writeHoldingRegister(id+1,register,value);
			}
			return null;
		}
	}

}