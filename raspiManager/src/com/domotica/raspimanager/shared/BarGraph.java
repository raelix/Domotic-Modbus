package com.domotica.raspimanager.shared;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
public class BarGraph {

	XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();  // collection of series under one object.,there could any
	XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); 
	XYSeriesRenderer renderer = new XYSeriesRenderer();     // one renderer for one series
	CategorySeries series = new CategorySeries("Bar1");
	int counter = 0;
	public void getData(){
		//        counter = 0;

		dataSet.addSeries(series.toXYSeries());                            // number of series

		//customization of the chart
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.DKGRAY);
		mRenderer.setMarginsColor(Color.GRAY);
//		mRenderer.setBackgroundColor(getResources().getColor(R.color.ilink_blue));
//	    mRenderer.setMarginsColor(getResources().getColor(R.color.ilink_blue));
		mRenderer.setZoomEnabled(true);
		renderer.setDisplayChartValues(true);
		renderer.setChartValuesTextSize(25);

		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setFillPoints(true);
		renderer.setColor(Color.WHITE);
		// collection multiple values for one renderer or series
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.setChartTitle("Demo Graph");
		//        mRenderer.setXTitle("xValues");
		mRenderer.setYTitle("Rupee");

		mRenderer.setBarSpacing(.5);   
		mRenderer.setXAxisMin(0);
		//        mRenderer.setYAxisMin(.5);

		mRenderer.setXLabels(0);
		   mRenderer.setShowCustomTextGrid(true);
		    mRenderer.setAntialiasing(true);
		    mRenderer.setPanEnabled(true, true);
		    mRenderer.setZoomEnabled(true, true);
		    mRenderer.setZoomButtonsVisible(false);
		    mRenderer.setXLabelsColor(Color.WHITE);
		    mRenderer.setYLabelsColor(0, Color.WHITE);
		    mRenderer.setXLabelsAlign(Align.CENTER);
		    mRenderer.setXLabelsPadding(10);
		    mRenderer.setXLabelsAngle(-30.0f);
		    mRenderer.setYLabelsAlign(Align.RIGHT);
		    mRenderer.setPointSize(3);
		    mRenderer.setInScroll(true);
		    mRenderer.setShowLegend(false);
		    mRenderer.setMargins(new int[] {0, 50, 10, 10});
//		mRenderer.setLegendHeight(300);
		mRenderer.setShowGrid(true);
		mRenderer.setXAxisMin(0);
		mRenderer.setYAxisMin(0);
	    mRenderer.setLabelsTextSize(22);
		//		mRenderer.setXLabels(0);
//		mRenderer.setPanEnabled(true, true);    // will fix the chart position


	}
	public GraphicalView getView(Context context, int type){
		GraphicalView intent = null;
		switch(type){
		case 0:
			intent =  ChartFactory.getBarChartView(context, dataSet, mRenderer, Type.DEFAULT);
			break;
		case 1:
			intent =  ChartFactory.getLineChartView(context, dataSet, mRenderer);
			break;
		case 2:
			XYMultipleSeriesRenderer temp = new XYMultipleSeriesRenderer();
			for(int i = 0; i < series.getItemCount(); i++){
				XYSeriesRenderer defaultRenderer = new XYSeriesRenderer();
				defaultRenderer.setColor(randomColor());
				temp.addSeriesRenderer(defaultRenderer);
			}
			intent =  ChartFactory.getPieChartView(context, series, temp);
			break;
		case 3:
			intent =  ChartFactory.getBubbleChartView(context,dataSet, mRenderer);
			break;
		}
		return intent;
	}

	public int randomColor(){
		switch(new Random().nextInt(6)){
		case 0:return Color.BLACK;
		case 1:return Color.WHITE;
		case 2:return Color.BLUE;
		case 3:return Color.CYAN;
		case 4:return Color.RED;
		case 5:return Color.YELLOW;
		case 6:return Color.DKGRAY;
		default:return Color.GREEN;
		}
	}
	public void addElement(String name, double value){
		series.add(""+name,value);
		mRenderer.addXTextLabel(counter+1,name);
		counter++;
	}
}
