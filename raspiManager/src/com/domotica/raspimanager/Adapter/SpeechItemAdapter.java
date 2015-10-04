package com.domotica.raspimanager.adapter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.camera.simplemjpeg.DoRead;
import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;
import com.domotica.raspimanager.Configuratore;
import com.domotica.raspimanager.R;
import com.domotica.raspimanager.activity.ActivityVoiceRecognitions;
import com.domotica.raspimanager.record.GpioRecord;
import com.domotica.raspimanager.shared.Pin;
import com.domotica.raspimanager.shared.WORD_CONSTANTS;
import com.domotica.raspimanager.shared.writeHR;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class SpeechItemAdapter  extends ArrayAdapter<String>  implements WORD_CONSTANTS {
	private ArrayList<String> plotlist;
	private ArrayList<GpioRecord> gpioList;
	private static Context context;
	private Activity act;
	public SpeechItemAdapter(Context context, int textViewResourceId, ArrayList<String> plotlist) {
		super(context, textViewResourceId, plotlist);
		this.plotlist = plotlist;
		SpeechItemAdapter.context = context;
	}

	public void setActivity(Activity act){
		this.act = act;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.item_speech, null);
			inflate(v, position);


		}

		return v;
	}



	public void inflate(View v,int position){
		RelativeLayout relative = (RelativeLayout) v.findViewById(R.id.layoutinterno);


		String element= plotlist.get(position);
		//POSSIBILITA DI SPOSTARLO ALTROVE PER NON APPESANTIRE LA GETVIEW

		Configuratore conf = getConf();
//		SSHConnection	connection = new SSHConnection();
//		connection.setActivity(act);
//		conf = getConf();
//		connection.execute(conf.ip,conf.usr,conf.psw,"st","");
//		for(int i = 0; i < conf.pins.size(); i++){
//			gpioList.add(new GpioRecord(conf.pins.get(i).getName(),""+(i+1),false,conf.pins.get(i).getScope().contentEquals("in") ? true : false));
//		}
		LinkedList<Pin> pin = conf.pinsUsed;
		String response = "";
		Random rand = new Random();
		boolean waitResponse = false;
		LinkedList<String> wordelement = new LinkedList<String>();
		try{
			StringTokenizer token = new StringTokenizer(element, " ");
			while(token.hasMoreTokens()){
				wordelement.add(token.nextToken());
			}

			for (int i = 0 ; i < wordelement.size() ; i++){

				if(wordelement.get(i).matches(offesa)){
					System.out.println(rispostaOffesa[getInt(rispostaOffesa.length+1)]);
					response+=rispostaOffesa[getInt(rispostaOffesa.length+1)];
				}

				if(wordelement.get(i).matches(sentimento)){
					response+="Entrato in Sentimento ";

				}
				else if(wordelement.get(i).matches(giusto)){
					response+="Entrato in Giusto ";
				}
				else if(wordelement.get(i).matches(sbagliato)){
					response+="Entrato in Sbagliato ";
				}
				else if(wordelement.get(i).matches(off)){
					response+="Entrato in Off ";
				}
				else if(wordelement.get(i).matches(movimento)){
					response+="Entrato in mostrami la cam ";
					addJpeg(relative);
				}
				else if(wordelement.get(i).matches(on)){
					//				response+="Entrato in On ";
					for(int f = 0 ; f < wordelement.size(); f++){
						for(int q = 0 ; q < pin.size() ; q++){
							if(pin.get(q).getName().toUpperCase(Locale.ITALY).contentEquals(wordelement.get(f).toUpperCase(Locale.ITALY))){
								response+= " Vuoi che accenda "+pin.get(q).getName()+" per te ? ";
								new writeHR().execute(q+1, 11);
							}
						}
					}
				}

				//ATTIVAZIONI IN PARALLELO CICLICHE POSSIBILITA DI PRONUNCIARE 1  o PIU NOMI
			}
			ActivityVoiceRecognitions.say(response+" ");
			addText(v,position);
		}
		catch(Exception e){
			System.err.println("Errore tokenize! No words");
		}







	}

	public static int getInt(int limitRandom){
		Random rand = new  Random();
		int result = (int)(rand.nextFloat()*limitRandom*0.9f);
		return result;
	}


	//		if (element != null) {	
	//			if(element.contentEquals("camera")){
	//addJpeg(relative);
	//				}
	//			else if(!element.contentEquals("camera")){
	//				addText(v,position);
	//								}

	//				}
	//				view.repaint();


	public static Configuratore getConf(){
		String lastPath= context.getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
		Configuratore conf = new Configuratore(lastPath);
		conf.readFromFile();
		return conf;
	}	

	public void addText(View view,int position){
		TextView text = new TextView(context);
		text.setText(plotlist.get(position));
		RelativeLayout.LayoutParams   lp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		//			lp.addRule(RelativeLayout.ALIGN_TOP , R.id.imageView12);
		//			lp.addRule(RelativeLayout.ALIGN_LEFT , R.id.imageView12);
		//			lp.addRule(RelativeLayout.ALIGN_RIGHT , R.id.imageView12);
		//			lp.addRule(RelativeLayout.BELOW , R.id.interno);
		//			 lp.addRule(RelativeLayout.CENTER_VERTICAL);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		//			lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		//			lp.bottomMargin = 250;
		text.setLayoutParams(lp);
		//			relat.addView(mv);
		((ViewGroup) view).addView(text);

	}


	@SuppressLint("NewApi")
	public void addJpeg(View view){

		MjpegView mv = new MjpegView(context); 

		//		 RelativeLayout rel = (RelativeLayout) view.findViewById(R.id.relat);
		//		 rel.removeAllViewsInLayout();

		//		 android:layout_width="match_parent"
		//			        android:layout_height="0dp"
		//			        android:layout_weight="0.5"

		//		 RelativeLayout relat = (RelativeLayout) view.findViewById(R.id.relat);
		RelativeLayout.LayoutParams   lp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		//		lp.addRule(RelativeLayout.ALIGN_TOP , R.id.imageView12);
		//		lp.addRule(RelativeLayout.ALIGN_LEFT , R.id.imageView12);
		//		lp.addRule(RelativeLayout.ALIGN_RIGHT , R.id.imageView12);
		//		lp.addRule(RelativeLayout.BELOW , R.id.interno);
		lp.addRule(RelativeLayout.CENTER_VERTICAL);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		//		lp.bottomMargin = 250;
		lp.width = 640;
		lp.height =480;
		if(mv != null){
			mv.setResolution(640,480);
		}
		mv.setLayoutParams(lp);
		//		relat.addView(mv);
		((ViewGroup) view).addView(mv);


		LayoutTransition lt = new LayoutTransition();
		lt.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
		lt.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
		((ViewGroup) view).setLayoutTransition(lt);
		mv.setAnimation(AnimationUtils.loadAnimation(context, R.anim.enter_from_down));
		MjpegInputStream result = null;
		try {
			result = new DoRead().execute("http://raelixx.ns0.it:443/videostream.cgi?user=raelix&pwd=Enrico1952&resolution=32&rate=0").get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("post");
		mv.setSource(result);
		if(result!=null) result.setSkip(1);
		mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		mv.showFps(true);
	}



}