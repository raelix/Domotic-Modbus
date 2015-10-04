package com.domotica.raspimanager.shared;

import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

public class LineGraph {

	private GraphicalView view;
	
	private TimeSeries dataset = new TimeSeries("Value"); 
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	
	private XYSeriesRenderer renderer = new XYSeriesRenderer(); // This will be used to customize line 1
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(2); // Holds a collection of XYSeriesRenderer and customizes the graph
	
	public LineGraph()
	{
		// Add single dataset to multiple dataset
		
		mDataset.addSeries(dataset);
//		renderer.setDisplayChartValues(true);
		// Customization time for line 1!
		renderer.setColor(Color.BLUE);
//		renderer.setChartValuesTextSize(55);
		renderer.setPointStyle(PointStyle.CIRCLE);

//		renderer.setPointStrokeWidth(12);
		renderer.setLineWidth(5.5f);
		renderer.setFillPoints(true);
		renderer.setDisplayBoundingPoints(true);
//		renderer.setXRoundedLabels(false);
		// Enable Zoom
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setXTitle("");
		mRenderer.setYTitle("Value");
//		DisplayMetrics metrics = DynamicGraphActivity.getContext().getResources().getDisplayMetrics();
//		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, metrics);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
		mRenderer.setBackgroundColor(Color.TRANSPARENT);
		mRenderer.setMargins(new int[]{100, 100, 100, 100});
//		mRenderer.setYLabelsAlign(Align.LEFT,0);
//		mRenderer.setYLabelsAlign(Align.RIGHT,0);
		mRenderer.setYAxisAlign(Align.LEFT, 0);
//		mRenderer.setClickEnabled(true);
		mRenderer.setLabelsTextSize(30);
		mRenderer.setLegendTextSize(30);
		// Add single renderer to multiple renderer
		mRenderer.setYLabelsPadding(30);
		mRenderer.setPointSize(8);
		
		mRenderer.setYAxisMin(dataset.getMinY()-10); 
		mRenderer.setXAxisMin(dataset.getMinX()-10); 
//		mRenderer.addXTextLabel(dataset.getItemCount() - 1, "0");
		mRenderer.setYAxisMax(dataset.getMaxY()+10);
		mRenderer.setXAxisMax(dataset.getMaxX()+10);
		
		mRenderer.addSeriesRenderer(renderer);	
//		mRenderer.setChartValuesTextSize(55);
//		mRenderer.setChartTitleTextSize(55);
//		mRenderer.setZoomEnabled(false, false);
//		mRenderer.setPanEnabled(false, false);
	}
	
	public GraphicalView getView(Context context) 
	{
		view =  ChartFactory.getTimeChartView(context, mDataset,mRenderer, new Date().toString());
		return view;
	}
	
	public void addNewPoints(Double p,Date date)
	{ 
		
		dataset.add(date, p);
//		mRenderer.initAxesRangeForScale(0) ;
		mRenderer.setYAxisAlign(Align.LEFT, 0);
//		mRenderer.setYAxisAlign(Align.RIGHT, 1);
		mRenderer.setYAxisMin(dataset.getMinY()-10); 
		mRenderer.setXAxisMin(dataset.getMinX()-10); 
//		mRenderer.addXTextLabel(dataset.getItemCount() - 1, "0");
		mRenderer.setYAxisMax(dataset.getMaxY()+10);
		mRenderer.setXAxisMax(dataset.getMaxX()+10);
	}
	public  XYMultipleSeriesRenderer getmRenderer(){
		return mRenderer;
	}
}
