package com.domotica.raspimanager.shared;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;

public class PieGraph {
	private static int[] COLORS = new int[] { Color.GREEN, Color.BLUE,Color.MAGENTA, Color.CYAN };
	private GraphicalView view;
	static XYMultipleSeriesDataset distributionSeries = new XYMultipleSeriesDataset();
	XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();   // collection multiple values for one renderer or series
	XYSeriesRenderer defaultRenderer  = new XYSeriesRenderer();
	
	public PieGraph(){
		defaultRenderer  = new XYSeriesRenderer();
		distributionSeries  = new XYMultipleSeriesDataset();
		mRenderer = new XYMultipleSeriesRenderer();   // collection multiple values for one renderer or series
		//		getData();
	}

	public GraphicalView getView(Context context) {

		view =  ChartFactory.getBarChartView(context, distributionSeries, mRenderer, Type.DEFAULT);
		return view;
	}

	public void getData(){
		mRenderer.setChartTitle("Demo Graph");
		//	      mRenderer.setXTitle("xValues");
		mRenderer.setYTitle("Rupee");
		mRenderer.setZoomButtonsVisible(true);    mRenderer.setShowLegend(true);
		mRenderer.setShowGridX(true);      // this will show the grid in  graph
		mRenderer.setShowGridY(true);        
		
		mRenderer.setAxisTitleTextSize(16);
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(35);
		mRenderer.setLegendTextSize(35);
//		mRenderer.setBarSpacing(1);
		//	      mRenderer.setAntialiasing(true);
//		mRenderer.setBarSpacing(1);   // adding spacing between the line or stacks
//		mRenderer.setApplyBackgroundColor(true);
//		mRenderer.setBackgroundColor(Color.BLACK);
		mRenderer.setXAxisMin(0);
		//	      mRenderer.setYAxisMin(.5);
//		mRenderer.setXAxisMax(5);
		mRenderer.setYAxisMin(0);
		//    
//		mRenderer.setXLabels(0);
		mRenderer.addXTextLabel(1,"Income");
		mRenderer.addXTextLabel(2,"Saving");
		mRenderer.addXTextLabel(3,"Expenditure");
		mRenderer.addXTextLabel(4,"NetIncome");
		mRenderer.setPanEnabled(true, true);    // will fix the chart position
		////		defaultRenderer = new XYSeriesRenderer();
		////		 distributionSeries = new XYMultipleSeriesDataset();
		//		 
		//	        mRenderer.setChartTitle("Energy Graph");
		////	        mRenderer.setXTitle("xValues");
		//	        mRenderer.setYTitle("Rupee");
		//	        mRenderer.setZoomButtonsVisible(true);    mRenderer.setShowLegend(true);
		//	        mRenderer.setShowGridX(true);      // this will show the grid in  graph
		//	        mRenderer.setShowGridY(true);              
		////	        mRenderer.setAntialiasing(true);
			        mRenderer.setBarSpacing(65);   // adding spacing between the line or stacks
			        mRenderer.setBarWidth(30);
		//	        mRenderer.setApplyBackgroundColor(true);
		//	        mRenderer.setBackgroundColor(Color.BLACK);
		//	        mRenderer.setXAxisMin(0);
		////	        mRenderer.setYAxisMin(.5);
		////	        mRenderer.setXAxisMax(5);
		//	        mRenderer.setYAxisMax(50);
		//	//    
		////	        mRenderer.setXLabels(0);
		////	        mRenderer.addXTextLabel(1,"Income");
		////	        mRenderer.addXTextLabel(2,"Saving");
		////	        mRenderer.addXTextLabel(3,"Expenditure");
		////	        mRenderer.addXTextLabel(4,"NetIncome");
		//	        mRenderer.setPanEnabled(true, true);    // will fix the chart position
		return ;
	}


	public  void addElement(String name, double value){
		defaultRenderer  = new XYSeriesRenderer();

		CategorySeries series = new CategorySeries(name);
		series.add(value);
		distributionSeries.addSeries(series.toXYSeries());
		defaultRenderer.setColor(new Random().nextFloat() > 0.4f ? Color.RED : Color.BLUE);
		defaultRenderer.setDisplayChartValues(true);
		defaultRenderer.setChartValuesSpacing((float) 25.5d);
		defaultRenderer.setChartValuesTextSize(50);
		defaultRenderer.setLineWidth((float) 10.5d);
		//	 mRenderer.addXTextLabel(value,name);
		mRenderer.addSeriesRenderer(defaultRenderer);
		System.out.println("serie :"+distributionSeries.getSeriesCount());
		System.out.println("render :"+mRenderer.getSeriesRendererCount());
	}

}
