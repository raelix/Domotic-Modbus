package com.domotica.raspimanager.adapter;

import java.util.ArrayList;

import org.achartengine.GraphicalView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.domotica.raspimanager.R;
import com.domotica.raspimanager.record.PlotRecord;
import com.domotica.raspimanager.shared.LineGraph;

public class PlotItemAdapter  extends ArrayAdapter<PlotRecord>  {
	private ArrayList<PlotRecord> plotlist;
	private Context context;
	public PlotItemAdapter(Context context, int textViewResourceId, ArrayList<PlotRecord> plotlist) {
		super(context, textViewResourceId, plotlist);
		this.plotlist = plotlist;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.item_plot, null);
		     inflateOut(v, position);
				
			
		}

		return v;
	}



	public void inflateOut(View v,int position){
		PlotRecord element= plotlist.get(position);
		if (element != null) {	
			GraphicalView view;
			 LineGraph line = new LineGraph();
			 view = line.getView(context);
				ViewGroup layout = ((ViewGroup) v.findViewById(R.id.layoutplot));
				RelativeLayout.LayoutParams   lp = new RelativeLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				lp.addRule(RelativeLayout.CENTER_IN_PARENT);
				lp.width = 1550;
				lp.height =750;
				
				view.setLayoutParams(lp);
				layout.addView(view);
				for(int i =0; i < element.getList().size(); i++){
					line.addNewPoints(element.getList().get(i).doubleValue(),element.getListDate().get(i));
				}
//				view.repaint();
				
		}
	}



}