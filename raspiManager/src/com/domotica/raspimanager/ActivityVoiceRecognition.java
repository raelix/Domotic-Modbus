package com.domotica.raspimanager;

import java.util.ArrayList;
import java.util.List;

import com.domotica.raspimanager.Adapter.SpeechItemAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class ActivityVoiceRecognition extends Activity implements OnInitListener{
	static ArrayList<String> list = new ArrayList<String>();
	static ListView listvoice ;
	static SpeechItemAdapter adapter;
	SpeechRecognizer speechRecognizer;
	static TextToSpeech talker;
	public static Activity activity ;
	@Override
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voicerecognition);
		activity = this;
		ImageView speech = (ImageView) findViewById(R.id.speechact);
		list = new ArrayList<String>();
		listvoice = (ListView) findViewById(R.id.listspeech);
		adapter =  new SpeechItemAdapter(this,  android.R.layout.simple_list_item_1, list);
		adapter.setActivity(this);
		listvoice.setAdapter(adapter);
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
		talker = new TextToSpeech(this, this);
		speech.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(list.size() >= 3){
					list.clear();
					adapter.notifyDataSetChanged();
					listvoice.startAnimation(AnimationUtils.loadAnimation(ActivityVoiceRecognition.this, R.anim.enter_from_down));
				}
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				PackageManager pm = getPackageManager();
				List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
				if(activities.size()<=0){
					Toast.makeText(getBaseContext(),
							"No Activity found to handle the action ACTION_RECOGNIZE_SPEECH",
							Toast.LENGTH_SHORT).show();
					return;
				}
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Jarvin..");
				startActivityForResult(intent, 1);
			}
	
				
//				list.add("camera");
//				adapter.notifyDataSetChanged();
			
		});

	}

	
	public static void ask(){
		if(list.size() >= 3){
			list.clear();
			adapter.notifyDataSetChanged();
			listvoice.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.enter_from_down));
		}
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
		if(activities.size()<=0){
			Toast.makeText(activity.getBaseContext(),
					"No Activity found to handle the action ACTION_RECOGNIZE_SPEECH",
					Toast.LENGTH_SHORT).show();
			return;
		}
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Jarvin..");
		activity.startActivityForResult(intent, 1);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK && null != data) {
				System.out.println("here");
				ArrayList<String> source = new ArrayList<String>();
				source = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				processCommand(source.get(0));
				
			}
		}
	}

	public void processCommand( String source){
	
		
		
	list.add(source);
	refreshList();
	}
	
	
	
	public static void say(String text2say){
		talker.setPitch(0.15f);
		talker.speak(text2say, TextToSpeech.QUEUE_FLUSH, null);
		talker.setPitch(0.15f);
	}
	
	@Override
	public void onInit(int status) {
		
		
	}


public static void refreshList(){
	adapter =  new SpeechItemAdapter(activity,  android.R.layout.simple_list_item_1, list);
	listvoice.setAdapter(adapter);
	adapter.notifyDataSetChanged();
}

}
