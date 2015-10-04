package com.camera.simplemjpeg;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.util.Log;

public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
	public static final String TAG="MJPEG";
	@Override
	protected MjpegInputStream doInBackground(String... url) {
        //TODO: if camera has authentication deal with it and don't just not work
        HttpResponse res = null;
        DefaultHttpClient httpclient = new DefaultHttpClient(); 
        HttpParams httpParams = httpclient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 5*1000);
        System.out.println("sendinggg");
        Log.d(TAG, "1. Sending http request");	
        try {
            res = httpclient.execute(new HttpGet(URI.create(url[0])));
            Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
            if(res.getStatusLine().getStatusCode()==401){
                //You must turn off camera User Access Control before this will work
                return null;
            }
            return new MjpegInputStream(res.getEntity().getContent());  
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.d(TAG, "Request failed-ClientProtocolException", e);
            //Error connecting to camera
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Request failed-IOException", e);
            //Error connecting to camera
        }
        return null;
    }

    @Override
	protected void onPostExecute(MjpegInputStream result) {
    	System.out.println("post");
//      GpioFragment.mv.setSource(result);
//      if(result!=null) result.setSkip(1);
//      GpioFragment.mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
//      GpioFragment.mv.showFps(true);
    }
}