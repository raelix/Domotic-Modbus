package com.domotica.raspimanager;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.domotica.raspimanager.Adapter.PinItemAdapter;
import com.domotica.raspimanager.Record.PinRecord;
import com.domotica.raspimanager.shared.Pin;
import com.domotica.raspimanager.shared.SwipeDismissListViewTouchListener;

public class ActivityGpio extends Activity{
	public  PinItemAdapter adapter ;
	public   ArrayList<PinRecord> logsList = new ArrayList<PinRecord>();
	public static final String ARG_SECTION_NUMBER = "section_number";
	ListView list;
	Configuratore conf ;
	LinkedList<Pin> pins = new LinkedList<Pin>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gpio);
		logsList = new ArrayList<PinRecord>();
		pins = new LinkedList<Pin>();
		final String lastPath=getApplicationInfo().dataDir+"/configurationNewRaspiServer.xml";
		 conf = new Configuratore(lastPath);
		conf.readFromFile();
		final ListView list = (ListView) findViewById(R.id.listView);
		ImageView addGpio = (ImageView) findViewById(R.id.addGpio);
		Button discard = (Button) findViewById(R.id.discard);
		Button save = (Button) findViewById(R.id.save);
		pins = conf.pins;
		for(int i = 0; i < pins.size(); i++){
			logsList.add(new PinRecord(pins.get(i).getName(), ""+pins.get(i).getNumber(), ""+pins.get(i).getDelay(),pins.get(i).isUse(), pins.get(i).getScope().contains("in") ? true : false, pins.get(i).getLog() == 0 ? false : true, pins.get(i).isAlarm()));
		}
		 adapter = new PinItemAdapter(this, android.R.layout.simple_list_item_1, logsList);
		list.setAdapter(adapter);

		  registerForContextMenu(list);
		 SwipeDismissListViewTouchListener touchListener =
	                new SwipeDismissListViewTouchListener(
	                		list,
	                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
	                            @Override
	                            public boolean canDismiss(int position) {
	                                return true;
	                            }

	                            @Override
	                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
	                                for (int position : reverseSortedPositions) {
	                                    adapter.remove(adapter.getItem(position));
//	                                    logsList.remove(position);
	                                }
	                                adapter.notifyDataSetChanged();
	                            }
	                        });
		 
		 discard.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				discard();
				logsList = new ArrayList<PinRecord>();
				
			}
		});
		 save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				for(int i = 0; i < logsList.size();i++){
//					System.out.println("nome: "+logsList.get(i).nome);
//					System.out.println("numero: "+logsList.get(i).numero);
//					System.out.println("delay: "+logsList.get(i).delay);
//				}
				
				conf.writePin(logsList);
				MainActivity.checkUpdates();
				
				finish();
				overridePendingTransition(R.anim.exit_on_right, R.anim.enter_from_left);
				logsList = new ArrayList<PinRecord>();
			}
		});
		 addGpio.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				adapter.add(new PinRecord("Name", "14", "1", true, true, true, true));
				adapter.notifyDataSetChanged();
//				list.invalidate();
				  
//				  adapter.setMessage(logsList);
//				  adapter.notifyDataSetChanged();
//				adapter.notifyDataSetInvalidated();
//				  adapter = new PinItemAdapter(GpioActivity.this, android.R.layout.simple_list_item_1, logsList);
//				  adapter.notifyDataSetChanged();
				  return;
//				  adapter.notifyDataSetInvalidated();
			}
		});
		 
		 list.setOnTouchListener(touchListener);
		 list.setOnScrollListener(touchListener.makeScrollListener());
		 
		}
	
	public void discard(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		builder.setMessage("Are you sure want discard changes?").setCancelable(false); 
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				finish();
				 }});
		
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int id) { 
			dialog.cancel();      
			} 
		});
		AlertDialog alert = builder.create();  
		alert.show();
		 
	}
	
}
