package com.domotica.raspimanager.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;

import com.domotica.raspimanager.AlertMessageTask;
import com.domotica.raspimanager.MainActivity;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class ConnectionManager {
	private static ConnectionManager singleton;
	private static Session session;
	private static String host;
	private static String user;
	private static String passw;
	private static Activity context;
	private static SSHConnection sshConnection;
	private static ServerRequest serverRequest;
	private static boolean waitingData = false;

	public  ConnectionManager(){
		sshConnection = new SSHConnection();
	}

	public static synchronized ConnectionManager getSingleton(){
		if(singleton == null){
			singleton = new ConnectionManager();
		}
		return singleton;
	}
//	public static void setConnection(String dstHost, String dstUser, String dstPassw){
//		host = dstHost;
//		user = dstUser;
//		passw = dstPassw;
//	}
//
	public static void setConnectionContext(Activity dstContext){
		host = MainActivity.ip;
		user = MainActivity.usr;
		passw = MainActivity.psw;
		context = dstContext;
	}
	
	public static void setConnectionContext(String hosts,String users,String passws,Activity dstContext){
		host = hosts;
		user = users;
		passw = passws;
		context = dstContext;
	}
	
	public static void stop(){
		if(session.isConnected())session.disconnect();
		
		
	}

	public static void startConnection(final String... param){
		new Thread(){
			@Override
			public void run(){
				if(host != null && user != null && passw != null)
					if(session == null || !session.isConnected()){
						//Non connesso SSH 
						sshConnection = new SSHConnection();
						if(context != null)
							sshConnection.setSessionContext(session, context);
						else sshConnection.setSession(session);
						sshConnection.execute(host, user, passw);

					}
				serverRequest = new ServerRequest();
				serverRequest.setActivity(context);
				serverRequest.executeOnExecutor(Executors.newSingleThreadExecutor() ,param);
			}
		}.start();
	}

	public static void startSSHConnection(){
		new Thread(){
			@Override
			public void run(){

				if(host != null && user != null && passw != null)
					if(session == null || !session.isConnected()){
						//Non connesso SSH 
						sshConnection = new SSHConnection();
						if(context != null)
							sshConnection.setSessionContext(session, context);
						else sshConnection.setSession(session);
						sshConnection.execute(host, user, passw);
					}
			}
		}.start();
	}

	public static Session getSession(){
		if(session != null && session.isConnected())return session;
		else{
			sshConnection = new SSHConnection();
			if(context != null)
				sshConnection.setSessionContext(session, context);
			else sshConnection.setSession(session);
			sshConnection.execute(host, user, passw);
			return session;
		}
	}

	public static void screenAlert(String alert,Activity act){
		AlertMessageTask errorConnection = new AlertMessageTask();
		errorConnection.setActivity(act);
		errorConnection.execute(alert);
	}

	public static boolean isConnected(){
		if(session != null && session.isConnected())return true;
		else return false;
	}

	public static void setWaitingData(boolean bool){
		waitingData = bool;
	}
	public static boolean getWaitingData(){
		return waitingData;
	}

	public static void setSession(Session ses){
		session = ses;
	}
}




class SSHConnection extends AsyncTask<String,Void,Integer> {
	Activity context = null;
	ProgressDialog pd = null;
	Session session = null;
	String rhost = "127.0.0.1";
	String lhost = "127.0.0.1";
	int rport = 503;	
	int lport = 9000;
	int timeout = 8000;
	private boolean fireEvent = true;
	public boolean useDialog = false;

	@Override
	protected Integer doInBackground(String... arg0) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		Thread.currentThread().setName("SSHConnectionThread");
		try {

				JSch jsch = new JSch();
				session = jsch.getSession( arg0[1], arg0[0], 22);
				session.setPassword( arg0[2].getBytes());
				session.setConfig("StrictHostKeyChecking", "no");
				session.connect(timeout);
				System.out.println("connesso? "+session.isConnected());
				int assinged_port = session.setPortForwardingL(lhost,lport, rhost, rport);
				session.setPortForwardingL(lhost,9001, rhost, 502);
				System.out.println("localhost:"+assinged_port+" -> "+rhost+":"+rport);
				ConnectionManager.setSession(session);
			
		}
		catch (Exception e) {
			//			System.out.println(e.getLocalizedMessage());
			//			String errore = "";
			//			if(e instanceof JSchException) errore = "Errore SSH ";
			//			else if(e instanceof UnknownHostException) errore = "Ricontrolla il dominio o la connessione internet, cè qualcosa di errato, attento agli spazi e maiscole";
			//			else if(e instanceof ConnectException) errore = "Ricontrolla i campi o la connessione cè qualcosa che non va, attento agli spazi e maiscole";
			//			else errore = "Ricontrolla user, password e dominio, cè qualcosa di errato, attento agli spazi e maiscole";
			//			if(isDialogActivated()){
			//				AlertMessageTask errorConnection = new AlertMessageTask();
			//				errorConnection.setActivity(getActivity());
			//				errorConnection.execute(errore);
			//			}
			if(fireEvent && !e.getLocalizedMessage().contains("PortForwardingL:")){
				System.out.println("non è portforwarding ma "+e.getLocalizedMessage());
				if( MainActivity.handle == null) return null;
				Message msgObj = MainActivity.handle.obtainMessage();
				Bundle ba = new Bundle();
				ba.putInt("status",0);
				msgObj.setData(ba);
				MainActivity.handle.sendMessage(msgObj);
				fireEvent = false;
			}
			return null;
		}
		if(fireEvent){
			if( MainActivity.handle == null) return null;
			Message msgObj = MainActivity.handle.obtainMessage();
			Bundle ba = new Bundle();
			ba.putInt("status", 1);
			msgObj.setData(ba);
			MainActivity.handle.sendMessage(msgObj);
			fireEvent = false;
		}
		return null;
	};

	@Override
	protected void onPreExecute() {
		if(isDialogActivated() && useDialog)
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
		if(isDialogActivated() && useDialog)
			pd.dismiss();
	}


	public void setFireEvent(boolean event){
		this.fireEvent = event;
	}
	public void setActivity(Activity act){
		this.context = act;
	}
	public Activity getActivity(){
		return this.context;
	}

	private boolean isDialogActivated(){
		return this.context != null ;
	}

	public void setSessionContext(Session session, Activity context){
		this.session = session;
		this.context = context;
	}

	public void setSession(Session session){
		this.session = session;
	}
}




class ServerRequest extends AsyncTask<String,Void,Integer> {
	String rhost="127.0.0.1";
	int lport=9000;
	String pathfile;
	String question;
	ProgressDialog pd = null;
	Activity context;
	Socket sock = null;
	DataOutputStream  dao ;


	@Override
	protected Integer doInBackground(String... arg0)  {

		try{ 
//			if(MonitorResource.getInstance().getBlockServerEmpty())
//				setServerStatus(0);
			pathfile = arg0[0];
			question = arg0[1];
			System.out.println("chiamata server request");
			if(question.contentEquals("")){
				return -1;
			}
			//			ConnectionManager.setWaitingData(true);
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			Thread.currentThread().setName("ServerRequestThread");
			sock = new Socket("127.0.0.1", lport);
			sock.setSoTimeout(5000);
			System.out.println("Server Request "+"path "+pathfile+" question "+question);
			OutputStream os = sock.getOutputStream();
			dao = new DataOutputStream(os);
			if(question.contentEquals("ReadCounter")){
				dao.writeUTF("ReadCounter");
				return ReadCounter(sock);
			}
			else if(question.contentEquals("start")){
				startNow();
			}
			else if(question.contentEquals("stop")){
				stopNow();
			}
			else if(question.contentEquals("help")){
				startServer();
			}
			else if(question.contentEquals("reboot")){
				new Thread(){
					@Override
					public void run(){
						runCommand("reboot -h");}}.start();
			}
			else if(question.contentEquals("WriteFile")){
				dao.writeUTF("WriteFile");
				WriteFile(sock,pathfile);
			}
			else if(question.contentEquals("ReadFile")){
				dao.writeUTF("ReadFile");
				ReadFile(sock,pathfile);

			}
			else if(question.contentEquals("ReadFileFirst")){
				System.out.println("here in readfile first");
				dao.writeUTF("ReadFile");
				ReadFileFirst(sock,pathfile);
				Intent i =getActivity().getPackageManager().getLaunchIntentForPackage("com.domotica.raspimanager");
				getActivity().startActivity(i);
				getActivity().finish();	
			}
			else if(question.contentEquals("ReadDb")){
				dao.writeUTF("ReadDb");
				dao.writeUTF(arg0[2]);
				ReadDb(sock,pathfile);
			}
			else {
				System.out.println("Ricevuto in input su ServerRequest un dato non accetto");
				dao.writeUTF("ciao scusami!Dato non accetto!Hacker??");
				sock.close();
			}
		}
		catch(IOException e){
//			if(!MonitorResource.getInstance().getBlockServerEmpty())
//			setServerStatus(0);
			askStart();
			return null;
		}
//		if(MonitorResource.getInstance().getBlockServerEmpty())
//		setServerStatus(1);
		return null;
	};

//	public void setServerStatus(int k ){
//		Message msgObj = MainActivity.handleServer.obtainMessage();
//		Bundle ba = new Bundle();
//		ba.putInt("statusServer",k);
//		msgObj.setData(ba);
//		MainActivity.handleServer.sendMessage(msgObj);
//	}


	@Override
	protected void onPostExecute(Integer result) {
		if(isDialogActivated())
			pd.dismiss();
	}

	@Override
	protected void onPreExecute() {
		if(isDialogActivated())
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					pd = ProgressDialog.show(context, "Please Wait","Caricamento In Corso..", true);
					pd.setCancelable(true);
				}
			});
	}

	public  void config(int config){
		//0 = old , 1 = new
		if(!isDialogActivated())return;
		final String message;
		final int configuration = config;
		if(configuration == 0) message = "E' disponibile una nuova configurazione.E' necessario scaricarla";
		else message = "La tua configurazione e' piu' recente rispetto a quella del Server.Spedire al Server i cambiamenti?"; 
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); 
				builder.setMessage(message).setCancelable(false); 
				builder.setPositiveButton("Si", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int id) {

						if(configuration == 0){
							ServerRequest request = new ServerRequest();
							request.setActivity(getActivity());
							request.setPathFile(pathfile);
							request.executeOnExecutor(Executors.newSingleThreadExecutor() ,pathfile,"ReadFile");
						}
						else {
							ServerRequest request = new ServerRequest();
							request.setPathFile(pathfile);
							request.executeOnExecutor(Executors.newSingleThreadExecutor() ,pathfile,"WriteFile");
						}
					}
				});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) { 
						dialog.cancel();      
					} 
				});
				AlertDialog alert = builder.create();  
				alert.show();
			}
		});
	}

	public void askStart(){
		if(!isDialogActivated())return;
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {	new AlertDialog.Builder(getActivity())
			.setTitle("Scarica Server")
			.setMessage("Vuoi che provo ad avviare il server?")
			.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new Thread(){
						@Override
						public void run(){
							startServer();
						}
					}.start();
					return;
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//NEL CASO IN CUI SCELGO DI NON AVVIARLO I THREAD CONTINUERANNO AD ASPETTARE L'AVVIO DEL SERVER
//					if(MonitorResource.getInstance().getBlockServerEmpty())
//						setServerStatus(1);
					//NON VA BENE XK DOPO DUE ENTRATE RICHIEDE DI AVVIARE IL SERVER
				}
			})
			.show();
			}
		});
	}



	public void startServer(){
		new Thread(){
			@Override
			public void run(){
				String data  = "empty";
				if(runCommand("ls /root/").contains("server")){
					data = runCommand("ls /root/server/");
					if(!data.contains("empty")) {
						if(!data.contains("starter.sh")){
							//E' necessaria la configurazione
							//					screenAlert("E' necessario scaricare la configurazione per far funzionare il server");
							//					ask("Scaricare Configurazione?", 1);
							runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						}
						if(!data.contains("raspiConfiguration.xml")){
							//E' necessaria la configurazione
							//					screenAlert("E' necessario scaricare la configurazione per far funzionare il server");
							//					ask("Scaricare Configurazione?", 1);
							runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/raspiConfiguration.xml > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						}
						if(!data.contains("libsqlitejdbc.so")) {
							//E' necessaria libreria (posso inibire)
							//					ask("Scaricare libreria di sistema?", 2);
							runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/libsqlitejdbc.so > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						}
						if(! data.contains("raspiServer.jar")){
							// E' necessario il server
							//					ask("Scaricare il Server?", 3);
							runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");	
						}
					}
				}
				else {
					String know;
					know = runCommand("mkdir /root/server");
					know += runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");		
					know += runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");		
					know += runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/raspiConfiguration.xml > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");	
					screenAlert(know.contains("error") ? "Riprovare piu' tardi il server sembra offline." : "Installato il server correttamente.");
				}
				//FINE CHECK DI BASE ESISTENZA FILE
				data = runCommand("ls /root/server/");
				if(data.contains("raspiConfiguration.xml") && data.contains("raspiServer.jar") ){
					String pid = "empty";
					pid = runCommand("ps aux | awk '/raspiServer/ && !/awk/ { print $2 }'");	
					int value;
					try{
						value = Integer.parseInt(pid);
					}catch(NumberFormatException e){
						value = 0;
					}

					if(pid != null && value > 0){
						runCommand("nohup kill -9 "+pid);
//						runCommand("nohup java -jar /root/server/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						runCommand("nohup chmod +x /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						runCommand("nohup /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null &");
						screenAlert("Server Riavviato");
//						if(MonitorResource.getInstance().getBlockServerEmpty())
//						setServerStatus(1);
					}
					else{
//						runCommand("nohup java -jar /root/server/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						runCommand("nohup chmod +x /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						runCommand("nohup /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null &");
						screenAlert("Il Server è stato avviato");
//						if(MonitorResource.getInstance().getBlockServerEmpty())
//							setServerStatus(1);
					}		
				}	
			}
		}.start();
	}

	public void startNow(){
		new Thread(){
			@Override
			public void run(){
				String pid = "empty";
				pid = runCommand("ps aux | awk '/raspiServer/ && !/awk/ { print $2 }'");	
				int value;
				try{
					value = Integer.parseInt(pid);
				}catch(NumberFormatException e){
					value = 0;
				}

				if(pid != null && value > 0){
					runCommand("nohup kill -9 "+pid);
//					runCommand("nohup java -jar /root/server/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
					runCommand("nohup chmod +x /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
					runCommand("nohup /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null &");
					screenAlert("Server Riavviato");
//					if(MonitorResource.getInstance().getBlockServerEmpty())
//						setServerStatus(1);
				}
				else{
//					runCommand("nohup java -jar /root/server/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
					runCommand("nohup chmod +x /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
					runCommand("nohup /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null &");
					screenAlert("Il Server è stato avviato");
//					if(MonitorResource.getInstance().getBlockServerEmpty())
//						setServerStatus(1);
				} 
			}
		}.start();
	}

	public void stopNow(){
		new Thread(){
			@Override
			public void run(){
				String pid = "empty";
				pid = runCommand("ps aux | awk '/raspiServer/ && !/awk/ { print $2 }'");	
				int value;
				try{
					value = Integer.parseInt(pid);
				}catch(NumberFormatException e){
					value = 0;
				}
				if(pid != null && value > 0){
					runCommand("nohup kill -9 "+pid);
					screenAlert("Server Fermato");
				}
				else{
					screenAlert("Il server e' gia' disattivo!");
				} 
			}
		}.start();
	}

	public String runCommand(String params){
		try {
			StringBuilder sb=new StringBuilder();
			Channel channel = ConnectionManager.getSession().openChannel("exec");
			channel.setInputStream(null);
			channel.setOutputStream(System.out);
			((ChannelExec) channel).setCommand(params);
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
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "empty";
		}
	}

	public  int ReadCounter(Socket sock) throws IOException{
		int counter = -1;
		DataInputStream dai = new DataInputStream( sock.getInputStream());
		counter = Integer.parseInt(dai.readUTF());
		dai.close();
		sock.close();
//		if(MonitorResource.getInstance().getBlockServerEmpty())
//			setServerStatus(1);
//		System.out.println("version Server counter is "+counter);
//		System.out.println("Version client counter is"+MainActivity.readCounter);
		if(MainActivity.conf.counter < counter){
			config(0);
			return -1;
		}
		else if(MainActivity.conf.counter > counter){
			config(1);
			return 1;
		}
		else if(MainActivity.conf.counter == counter){
			screenAlert("Non ci sono aggiornamenti disponibili");
		}
		return counter;
	}
	public void screenAlert(String alert){
		if(!isDialogActivated())return;
		AlertMessageTask errorConnection = new AlertMessageTask();
		errorConnection.setActivity(getActivity());
		errorConnection.execute(alert);
	}

	public  void ReadFile(Socket sock,String path) throws IOException{
		DataInputStream dis = new DataInputStream(sock.getInputStream()); //get the socket's input stream
		int size = dis.readInt(); //get the size of the file.
		InputStream in = sock.getInputStream(); 
		FileOutputStream out = new FileOutputStream(path);
		int totalBytesRead = 0;
		byte[] buf = new byte[8192]; //buffer
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len); //write buffer
		}
		out.close(); //clean up
		in.close();
		dis.close();
	}

	public  void ReadFileFirst(Socket sock,String path) throws IOException{
		System.out.println("in read file first");
		DataInputStream dis = new DataInputStream(sock.getInputStream()); //get the socket's input stream
		int size = dis.readInt(); //get the size of the file.
		InputStream in = sock.getInputStream(); 
		FileOutputStream out = new FileOutputStream(path);
		int totalBytesRead = 0;
		byte[] buf = new byte[8192]; //buffer
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len); //write buffer
		}
		out.close(); //clean up
		in.close();
		dis.close();

	}
	public int  ReadDb(Socket sock,String path) throws IOException{
		DataInputStream dis = new DataInputStream(sock.getInputStream()); //get the socket's input stream
		String c = dis.readUTF();
		if(c.contains("ok")){
			System.out.println("file esistente sul server");
		}
		else if(c.contains("notfound")){
			screenAlert("Periodo di tempo non esistente sul server");
			System.out.println("file non esistente sul server");
			dis.close();
			sock.close();
			return 0;
		}
		InputStream in = sock.getInputStream(); 
		FileOutputStream out = new FileOutputStream(path);
		byte[] buf = new byte[80192]; //buffer
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len); //write buffer
			System.out.println("reading "+len);
		}
		dis.close();
		out.flush();
		out.close(); 
		in.close();
		sock.close();
		return 1;
	}

	public static void WriteFile(Socket sock,String path) throws IOException{
		File myFile = new File(path);
		byte[] mybytearray = new byte[(int) myFile.length()];
		OutputStream os;
		os = sock.getOutputStream();
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
		bis.read(mybytearray, 0, mybytearray.length);
		os.write(mybytearray, 0, mybytearray.length);
		os.flush();
		os.close();
		bis.close();
	}

	public void setPathFile(String path){
		this.pathfile = path;
	}

	private boolean isDialogActivated(){
		return this.context != null ;
	}

	public void setActivity(Activity act){
		this.context = act;
	}
	public Activity getActivity(){
		return this.context;
	}	


}