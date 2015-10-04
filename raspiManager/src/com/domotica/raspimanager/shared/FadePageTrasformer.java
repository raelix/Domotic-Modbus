package com.domotica.raspimanager.shared;

import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;


//   	fade	 = new FadePageTrasformer();
//	mViewPager.setPageTransformer(true, new FadePageTrasformer());
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("NewApi")
public class FadePageTrasformer implements ViewPager.PageTransformer {

	public static int ENABLE_FADE = 0;// 0 FALSE USE OBJECT - 1 TRUE NOT USE OBJECT
	public static LinkedList<Object> objects  = new LinkedList<Object>();

	public static void addObject(Object obj){
		if(objects == null)objects = new LinkedList<Object>();
		if(!objects.contains(obj))
			objects.add(obj);
	}


	public static void clearObject(){
		if(objects != null){
			objects.clear();
			objects = null;
		}
	}


	@Override
	public void transformPage(View view, float position) {
		if(ENABLE_FADE == 1){
			fadeEffect(view, position);
			return;
		}
		else if(ENABLE_FADE == 0){
			scrollEffect(view, position);
		}
	}

	public void scrollEffect(View view , float position){
		if(objects != null){
//			System.out.println(position);
			if (position <= -1) {
				for(int i = 0 ; i < objects.size(); i++){
					if(i >= objects.size() /2)
						((View) objects.get(i)).setTranslationX(( position) * 1.2f * view.getWidth());
					else ((View) objects.get(i)).setTranslationX(( position) * 1.8f * view.getWidth());
				}
			}
			else if (position <= 1) { 

				if (position < 0) {
					for(int i = 0 ; i < objects.size(); i++){
						if(i >= objects.size() /2)
							((View) objects.get(i)).setTranslationX(( position) * 1.2f * view.getWidth());
						else ((View) objects.get(i)).setTranslationX(( position) * 1.8f * view.getWidth());
					}
				}
				else if(position == 0)  {
					for(int i = 0 ; i < objects.size(); i++){
						if(i >= objects.size() /2)
							((View) objects.get(i)).setTranslationX(-( position) * 1.2f * view.getWidth());
						else ((View) objects.get(i)).setTranslationX(-(position) * 1.8f * view.getWidth());
					}
				}
			}
			else {
			}
		}
	}

	public void fadeEffect(View view, float position){
		int pageWidth = view.getWidth();
		if (position <= -1) {
			// [-Infinity,-1)
			view.setTranslationX(0);
			view.setAlpha(0);
		}
		else if (position <= 1) { 
			// [-1,1]	
			//			GpioItemAdapter.nome.setTranslationX((float) (-(1 - position) * 1.7 * pageWidth)); 
			view.setAlpha(Math.max(0.3f, 1 - Math.abs(position)));
			if (position < 0) {

				view.setTranslationX(  ( -position) * pageWidth);
				view.setAlpha(Math.max(0.0f, 1 - Math.abs(position)));
			}
			else {
				view.setTranslationX(  -position * pageWidth);
				view.setAlpha(Math.max(0.0f, 1 - Math.abs(position)));
			}
		} 
		else { 
			// (1,+Infinity]
			view.setAlpha(0);
			view.setTranslationX(0);
		}
	}
}