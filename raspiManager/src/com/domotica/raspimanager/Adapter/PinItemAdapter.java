package com.domotica.raspimanager.adapter;

import java.util.ArrayList;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.domotica.raspimanager.R;
import com.domotica.raspimanager.record.PinRecord;
import com.domotica.raspimanager.shared.Utils;

public class PinItemAdapter  extends ArrayAdapter<PinRecord>  {
	private ArrayList<PinRecord> message;
	WheelView NumberValue;
	WheelView ScopeValue;
	WheelView LogValue ;
	WheelView AlarmValue;
	WheelView DelayValue;
	 boolean wheelScrolled = false;
	String[] lista1 = new String[]{"0","500","1000","2000","3000","4000","5000","6000","7000","8000","9000","10000","11000","12000","13000","14000","15000"};
	public PinItemAdapter(Context context, int textViewResourceId, ArrayList<PinRecord> message) {
		super(context, textViewResourceId, message);
		this.message = message;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.item_gpio, null);
		}
		NumberValue = (WheelView) v.findViewById(R.id.NumberValue);
		ScopeValue = (WheelView) v.findViewById(R.id.ScopeValue);
		LogValue = (WheelView) v.findViewById(R.id.LogValue);
		AlarmValue = (WheelView) v.findViewById(R.id.AlarmValue);
		DelayValue = (WheelView) v.findViewById(R.id.DelayValue);
		EditText name = (EditText) v.findViewById(R.id.nameValue);
		if(position >= message.size())return v;
		PinRecord user = message.get(position);
		if (user != null) {
			System.out.println(position);
			final int pos = position;
			final PinRecord record= user;
			
			NumberValue.setVisibleItems(1);
			NumberValue.setViewAdapter(new NumericWheelAdapter(this.getContext(), 0, 14));   
			
			
			ScopeValue.setVisibleItems(1);
			String[] lista2 = new String[]{"IN","OUT"};
			ArrayWheelAdapter<String> adapter2 =
					new ArrayWheelAdapter<String>(getContext(), lista2);
			adapter2.setTextSize(18);
			ScopeValue.setViewAdapter(adapter2);
			
			LogValue.setVisibleItems(1);
			String[] lista3 = new String[]{"OFF","ON"};
			ArrayWheelAdapter<String> adapter3 =
					new ArrayWheelAdapter<String>(getContext(), lista3);
			adapter3.setTextSize(18);
			LogValue.setViewAdapter(adapter3);   
			
			AlarmValue.setVisibleItems(1);
			String[] lista4 = new String[]{"OFF","ON"};
			ArrayWheelAdapter<String> adapter4 =
					new ArrayWheelAdapter<String>(getContext(), lista4);
			adapter4.setTextSize(18);
			AlarmValue.setViewAdapter(adapter4);  
			
			DelayValue.setVisibleItems(1);
			
			ArrayWheelAdapter<String> adapter5 =
					new ArrayWheelAdapter<String>(getContext(), lista1);
			adapter5.setTextSize(18);
			DelayValue.setViewAdapter(adapter5);   
			name.setText(user.getNome());
			final EditText names = name;
			
			System.out.println();
			int number = Utils.strToInt(record.getNumero());
			NumberValue.setCurrentItem(number, false);
			ScopeValue.setCurrentItem(record.isInout() ? 0 : 1, false);
			LogValue.setCurrentItem(record.isLog() ? 1:0 ,  false);
			AlarmValue.setCurrentItem(record.isAlarm() ? 1:0 ,  false);
			int delay = Utils.strToInt(record.getDelay());
			int curItem = 0;
			if(delay ==0 )curItem = 0;
			if(delay ==500 )curItem = 1;
			if(delay ==1000 )curItem = 2;
			if(delay ==2000 )curItem = 3;
			if(delay ==3000 )curItem = 4;
			if(delay ==4000 )curItem = 5;
			if(delay ==5000 )curItem = 6;
			DelayValue.setCurrentItem(curItem ,  false);
			  OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
			        @Override
					public void onScrollingStarted(WheelView wheel) {
			            wheelScrolled = true;
			        }
			        @Override
					public void onScrollingFinished(WheelView wheel) {
			            wheelScrolled = false;
			        }
			    };
			    NumberValue.addScrollingListener(scrolledListener);
			NumberValue.addChangingListener(new OnWheelChangedListener() {
				
				@Override
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					if(wheelScrolled)
					if(message.size()>= pos){
						record.setNome(names.getText().toString());
						record.setNumero(""+newValue);	
						record.setAlarm(LogValue.getCurrentItem()==0? false : true);
						message.set(pos, record);
					}
					System.out.println("position "+pos);
				}
			});
			ScopeValue.addScrollingListener(scrolledListener);
ScopeValue.addChangingListener(new OnWheelChangedListener() {
				
				@Override
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					if(wheelScrolled)
					if(message.size()>= pos){
						record.setInout(newValue==0? true : false);
						message.set(pos, record);
					}
					System.out.println("position "+pos);
				}
			});

AlarmValue.addScrollingListener(scrolledListener);
AlarmValue.addChangingListener(new OnWheelChangedListener() {
	
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
if(wheelScrolled)
		if(message.size()>= pos){
			record.setAlarm(newValue==0? false : true);message.set(pos, record);
		}
	
		System.out.println("position "+pos);
	}
});
LogValue.addScrollingListener(scrolledListener);
LogValue.addChangingListener(new OnWheelChangedListener() {
	
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if(wheelScrolled)
		if(message.size()>= pos){
			record.setLog(newValue==0? false : true);message.set(pos, record);
		}
		System.out.println("position "+pos);
	}
});

DelayValue.addScrollingListener(scrolledListener);
DelayValue.addChangingListener(new OnWheelChangedListener() {
	
	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
//		System.out.println("SETTING DELAY"+lista1[newValue]);
		lista1 = new String[]{"0","500","1000","2000","3000","4000","5000","6000","7000","8000","9000","10000","11000","12000","13000","14000","15000"};
		if(wheelScrolled)
		if(message.size()>= pos){
			record.setDelay(lista1[newValue]);message.set(pos, record);
		}
	}
});

		}

		return v;
	}
	
	public void setMessage(ArrayList<PinRecord> l){
		message =l;
	}
//
//	@Override
//	public void onChanged(WheelView wheel, int oldValue, int newValue) {
////		WheelView NumberValue;
////		WheelView ScopeValue;
////		WheelView LogValue ;
////		WheelView AlarmValue;
////		WheelView DelayValue;
//		if(wheel ==NumberValue ){
//			record.setNumero(""+newValue);
//			message.set(positionElement, record);
//		}
//		else if(wheel ==ScopeValue ){
//			record.setInout(newValue==0? true : false);
//			message.set(positionElement, record);
//		}
//		else if(wheel ==LogValue ){
//			record.setLog(newValue==0? false : true);
//			message.set(positionElement, record);
//		}
//		else if(wheel ==AlarmValue ){
//			record.setLog(newValue==0? false : true);
//			message.set(positionElement, record);
//		}
//		else if(wheel ==DelayValue ){
//			System.out.println("SETTING DELAY"+lista1[newValue]);
//			lista1 = new String[]{"0","500","1000","2000","3000","4000","5000","6000","7000","8000","9000","10000","11000","12000","13000","14000","15000"};
//			record.setDelay(lista1[newValue]);
//			message.set(positionElement, record);
//		}
//	}
}