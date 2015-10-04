package com.domotica.raspimanager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class StartServer extends  AsyncTask<String,String,String>{
	Session session;
	String rhost="127.0.0.1";
	int lport=9000;
	String pathfile;
	String question;
	ProgressDialog pd = null;
	Activity context;
	String usr;
	String host;
	String psw;


	public void setSession(Session session){
		this.session = session;
	}
	@Override
	protected void onPreExecute() {
		context.runOnUiThread(new Runnable() {

			@Override
			public void run() {
//			if(pd == null || !pd.isShowing())
				pd = ProgressDialog.show(context, "Please Wait",
						"Contatto il server..", true);
//				pd.setCancelable(true);
			}
		});
	}
	
	
	@Override
	protected String doInBackground(String... params) {
		StringBuilder sb=new StringBuilder();
		JSch jsch = new JSch();
		try {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			if(host == null )return "";
						session = jsch.getSession( usr, host, 22);
						System.out.println("dopo get session");
						session.setPassword( psw.getBytes());
						session.setConfig("StrictHostKeyChecking", "no");
						System.out.println("faccio la connect");
						session.connect(5000);
						System.out.println("connesso? "+session.isConnected());
			Channel channel = session.openChannel("exec");
			channel.setInputStream(null);
			channel.setOutputStream(System.out);
			((ChannelExec) channel).setCommand(params[0]);
			((ChannelExec) channel).setPty(false);
			channel.connect();
			InputStream in = channel.getInputStream();
//			byte[] tmp = new byte[1024];
			while (true) {
				InputStreamReader is = new InputStreamReader(in);

				BufferedReader br = new BufferedReader(is);
				String read = br.readLine();
				while(read != null) {
					System.out.println(read);
					sb.append(read);
					read =br.readLine();

				}
				if (channel.isClosed()) {
					System.out.println(sb.toString());
					System.out.println("exit-status:"+channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
			session.disconnect();
		} catch (Exception e) {

			e.printStackTrace();
			return "error";
		}

		if (pd!= null) {
			pd.dismiss();
			pd= null;
		}
		return sb.toString();
	}



	@Override
	protected void onPostExecute(String result) {
		context.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (pd!= null) {
					pd.dismiss();
				    pd= null;
				}
			}
			});
	}

	public void screenAlert(String alert){
		AlertMessageTask errorConnection = new AlertMessageTask();
		errorConnection.setActivity(getActivity());
		errorConnection.execute(alert);
	}
	public void setActivity(Activity act){
		this.context = act;
	}
	public Activity getActivity(){
		return this.context;
	}	
	public void setData(String usr, String host, String psw) {
		this.usr = usr;
		this.psw = psw;
		this.host = host;

	}

}
