package com.domotica.raspimanager;

import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class SSHConnection extends AsyncTask<String,Void,Integer> {
	Session session;
	int lport=9000;
	Activity context;
	ProgressDialog pd = null;
	ServerRequest request;
	
	public void setActivity(Activity act){
		this.context = act;
	}
	public Activity getActivity(){
		return this.context;
	}

	public boolean close(){
		session.disconnect();
		return session.isConnected() ? false : true;
	}

	public Session sessions(){
		return session;
	}

	public boolean isConnected(){
		return session.isConnected();
	}

	@Override
	protected void onPreExecute() {
		context.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				pd = ProgressDialog.show(context, "Please Wait",
						"Caricamento In Corso..", true);
				pd.setCancelable(true);
			}
		});
	}
	@Override
	protected void onPostExecute(Integer c) {
		pd.dismiss();

	}
	@Override
	protected Integer doInBackground(String... arg0) {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		String rhost="127.0.0.1";
		int rport=503;	
		String lhost="127.0.0.1";
		Thread.currentThread().setName("SSHConnectionThread");
		try {
			JSch jsch = new JSch();
			System.out.println("host"+arg0[0]+" user "+arg0[1]+" passw"+arg0[2]);
			session = jsch.getSession( arg0[1], arg0[0], 22);
			System.out.println("dopo get session");
			session.setPassword( arg0[2].getBytes());
			session.setConfig("StrictHostKeyChecking", "no");
			System.out.println("faccio la connect");
			session.connect(35000);
			System.out.println("connesso? "+session.isConnected());

			int assinged_port = session.setPortForwardingL(lhost,lport, rhost, rport);
			session.setPortForwardingL(lhost,9001, rhost, 502);
			System.out.println("localhost:"+assinged_port+" -> "+rhost+":"+rport);

		} 
		catch (JSchException e) {
			System.out.println(e.getLocalizedMessage());
			String errore = "";
			if(e.getLocalizedMessage().contains("Auth fail"))
				errore = "Username o password SSH errati";
				
			else if(e.getLocalizedMessage().contains("timeout:"))errore = "Connessione saltata, riprova, Descrizione: "+e.getLocalizedMessage();
			else if(e.getLocalizedMessage().contains("PortForwardingL:")){ 
//				errore = "Connessione già attiva, ma il Server sembra essere spento o non esistente, Descrizione: "+e.getLocalizedMessage();
				serverCall(arg0[1],arg0[0],arg0[2],arg0[3],arg0[4],arg0);
				return null;
			}
			else if(e.getLocalizedMessage().contains("java.net.UnknownHostException:"))errore = "Ricontrolla il dominio o la connessione internet, cè qualcosa di errato, attento agli spazi e maiscole";
			else if(e.getLocalizedMessage().contains("java.net.ConnectException:"))errore = "Ricontrolla i campi o la connessione cè qualcosa che non va, attento agli spazi e maiscole";
			else errore = "Ricontrolla user, password e dominio, cè qualcosa di errato, attento agli spazi e maiscole";
			AlertMessageTask errorConnection = new AlertMessageTask();
			errorConnection.setActivity(getActivity());
			errorConnection.execute(errore);
			return null;
		}
		serverCall(arg0[1],arg0[0],arg0[2],arg0[3],arg0[4],arg0);
		return null;
	};

	public void serverCall(String usr,String host, String psw,String j, String k,String... param){
		System.out.println("stabilita connessione con casa");
		if(request != null)
			if(request.isActive())
			if(request.byebye())System.out.println("stabilita sistema salvato cazzo!");
			else System.out.println("stabilita era attivo ma qualche errrore in byebye");
//		if(j.contentEquals("") || k.contentEquals("")){
//			return;
//		}
		System.out.println("teoricamente nuova connessione");
		 request = new ServerRequest();
		request.setActivity(getActivity());
		request.setData(usr,host, psw);
		request.setData(param);
		request.executeOnExecutor(Executors.newSingleThreadExecutor() ,j,k);
	}
	
	public ServerRequest getServerRequest(){
		return request;
	}

}

