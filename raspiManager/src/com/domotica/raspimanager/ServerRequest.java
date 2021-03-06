package com.domotica.raspimanager;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.jcraft.jsch.Session;


public class ServerRequest extends AsyncTask<String,Void,Integer> {
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
	StartServer startServer;
	boolean first = false;
	String[] param;
	Socket sock = null;
	DataOutputStream  dao ;

	@Override
	protected void onPreExecute() {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				pd = ProgressDialog.show(context, "Please Wait","Caricamento In Corso..", true);
				pd.setCancelable(true);
			}
		});
	}

	public boolean isActive(){
		return  sock.isConnected();
	}
	public boolean byebye(){
		try {
			if(dao != null)
				if(!sock.isClosed()){
					dao.writeUTF("ReadCounter");
					 ReadCounter(sock);
				
			}
		}catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	@Override
	protected Integer doInBackground(String... arg0)  {
		pathfile = arg0[0];
		question = arg0[1];
		if(question.contentEquals(""))return -1;

		try{ 
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			Thread.currentThread().setName("ServerRequestThread");
			sock = new Socket("127.0.0.1", lport);
//			sock.setSoTimeout(5000);
			System.out.println("Server Request "+"path "+pathfile+" question "+question);
			OutputStream os = sock.getOutputStream();
			dao = new DataOutputStream(os);
			System.out.println("connesso al server? "+sock.isConnected());
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
				sock.setSoTimeout(2000);
				ReadFileFirst(sock,pathfile);
			}
			else if(question.contentEquals("ReadDb")){
				dao.writeUTF("ReadDb");
				if(arg0.length>2)
					dao.writeUTF(arg0[2]);
				else dao.writeUTF(param[5]);
				ReadDb(sock,pathfile);
			}
			else {
				System.out.println("Ricevuto in input su ServerRequest un dato non accetto");
				dao.writeUTF("ciao scusami!Dato non accetto!Hacker??");
				sock.close();
			}
		}
		catch(IOException e){
			System.out.println("errore ma quale tipo di errore?spesso il server � attivo "+e.getMessage());
			screenAlert("Connessione SSH attiva, ma il Server sembra essere spento o non esistente");
			//					+ " Descrizione: "+e.getLocalizedMessage());
			askStart();
			//			try {
			//				sock.close();
			//			} catch (IOException e1) {
			//				e1.printStackTrace();
			//			}
		}
//		try {
//			sock.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return null;
	};

	public void screenAlert(String alert){
		AlertMessageTask errorConnection = new AlertMessageTask();
		errorConnection.setActivity(getActivity());
		errorConnection.execute(alert);
	}


	@Override
	protected void onPostExecute(Integer result) {
		pd.dismiss();

	}

	public void askStart(){
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
//														startNow();
						}
					}.start();
					return;
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { 

				}
			})
			.show();
			}
		});
	}



	public  void configNew(){
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); 
				builder.setMessage("La tua configurazione e' piu' recente rispetto a quella del Server.Spedire al Server i cambiamenti?").setCancelable(false); 
				builder.setPositiveButton("Si", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int id) {
						ServerRequest request = new ServerRequest();
						request.setActivity(getActivity());
						request.setData(usr, host, psw);
						request.setPathFile(pathfile);
						request.execute(pathfile,"WriteFile");
						return;
					}
				});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) { 
						dialog.cancel();   
						return;
					} 
				});
				AlertDialog alert = builder.create();  
				alert.show();
			}
		});
	}


	public  void configDeprecate(){
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); 
				builder.setMessage("E' disponibile una nuova configurazione.E' necessario scaricarla").setCancelable(false); 
				builder.setPositiveButton("Scarica", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int id) {
						ServerRequest request = new ServerRequest();
						request.setActivity(getActivity());
						request.setData(usr, host, psw);
						request.setPathFile(pathfile);
						request.first = false;
						request.execute(pathfile,"ReadFile");
						System.out.println("new request read file");
						return;
					}});

				builder.setNegativeButton("Esci", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) { 
						dialog.cancel();      
						return;
					} 
				});
				AlertDialog alert = builder.create();  
				alert.show();
			}});
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
					//QUI HO TUTTO DEVO SOLO AVVIARE IL SERVER
				}
				else {
					// QUI DEVO CREARE CARTELLA E RECUPERARE TUTTI I FILE 1 CONFIGURAZIONE
					String know;
					know = runCommand("mkdir /root/server");
					know += runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");		
					know += runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");		
					know += runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/raspiConfiguration.xml > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");	
					know += runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/libsqlitejdbc.so > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
					screenAlert(know.contains("error") ? "Riprovare piu' tardi il server sembra offline." : "Installato il server correttamente.");
				}
				data = runCommand("ls /root/server/");
				if(data.contains("raspiConfiguration.xml") && data.contains("libsqlitejdbc.so") && data.contains("raspiServer.jar") && data.contains("starter.sh") ){
					//tutto configurato
					//					ask("posso avviare il Server?", 4);
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

					}
					else{
//						runCommand("nohup java -jar /root/server/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						runCommand("nohup chmod +x /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
						runCommand("nohup /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null &");
						screenAlert("Il Server � stato avviato");
						//						ServerRequest req = new ServerRequest();
						//						req.setActivity(context);
						//						req.execute(pathfile,"ReadFileFirst");



					} 
				}}
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

				}
				else{
//					runCommand("nohup java -jar /root/server/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
					runCommand("nohup chmod +x /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
					runCommand("nohup /root/server/starter.sh > /root/server/foo.out 2> /root/server/foo.err < /dev/null &");
					screenAlert("Il Server � stato avviato");
					//				ServerRequest req = new ServerRequest();
					//				req.setActivity(context);
					//				req.execute(pathfile,"ReadFileFirst");


				} 
			}

		}.start();
	}
	public void stopNow(){
		new Thread(){
			@Override
			public void run(){
				//tutto configurato
				//					ask("posso avviare il Server?", 4);
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
					alertMessage("Il server e' gia' disattivo!");


				} 
			}}.start();}

	public String runCommand(String params){
		try {
			StartServer	startServer = new StartServer();
			startServer.setActivity(context);
			startServer.setData(usr, host, psw);
			startServer.setSession(session);
			return startServer.execute(params).get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "empty";
		}

	}
	public void setPathFile(String path){
		this.pathfile = path;
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


	public void setActivity(Activity act){
		this.context = act;
	}
	public Activity getActivity(){
		return this.context;
	}	
	public  int ReadCounter(Socket sock) throws IOException{
		int counter = -1;
		System.out.println("in read COunter");
		DataInputStream dai = new DataInputStream( sock.getInputStream());
		counter = Integer.parseInt(dai.readUTF());
		System.out.println("Version Server is :"+counter);
		dai.close();
		sock.close();
		System.out.println("version Server counter is "+counter);
		System.out.println("Version client counter is"+MainActivity.readCounter);
		if(MainActivity.conf.counter < counter){
			System.out.println("config deprecate");
			configDeprecate();
			return -1;
		}
		else if(MainActivity.conf.counter > counter){
			System.out.println("config new");
			configNew();
			return 1;
		}
		else if(MainActivity.conf.counter == counter){
			alertMessage("Non ci sono aggiornamenti disponibili");
		}
		return counter;
	}


	public void alertMessage(final String alertMessage){
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String message = alertMessage;
				new AlertDialog.Builder(getActivity())
				.setTitle("Update")
				.setMessage(message)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { 
					}
				})
				.show();
			}
		});
	}

	public  void ReadFile(Socket sock,String path) throws IOException{
		FileOutputStream out = new FileOutputStream(path);
		InputStream in = sock.getInputStream();
		int totalBytesRead = 0;
		byte[] buf = new byte[8192]; //buffer
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len); //write buffer
		}

		out.close(); //clean up
		in.close();
	}
	public  void ReadFileFirst(Socket sock,String path) throws IOException{
		System.out.println("in read file first");
		DataInputStream dis = new DataInputStream(sock.getInputStream()); //get the socket's input stream
//		int size = dis.readInt(); //get the size of the file.
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
		Intent i =getActivity().getPackageManager().getLaunchIntentForPackage("com.domotica.raspimanager");
		getActivity().startActivity(i);
		getActivity().finish();	

	}
	public int  ReadDb(Socket sock,String path) throws IOException{
		DataInputStream dis = new DataInputStream(sock.getInputStream()); //get the socket's input stream
		String c = dis.readUTF();
		if(c.contains("ok")){
			System.out.println("file esistente sul server");
		}
		else if(c.contains("notfound")){
			alertMessage("Periodo di tempo non esistente sul server");
			System.out.println("file non esistente sul server");
			dis.close();
			sock.close();
			return 0;
		}
		int size = dis.readInt(); //get the size of the file.
		InputStream in = sock.getInputStream(); 
		FileOutputStream out = new FileOutputStream(path);
		int totalBytesRead = 0;
		byte[] buf = new byte[80192]; //buffer
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len); //write buffer
			System.out.println("reading "+len);
		}

		//	        out.flush();
		dis.close();
		out.close(); //clean up
		in.close();
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


	public void setData( String host,String usr, String psw) {
		this.usr = usr;
		this.psw = psw;
		this.host = host;

	}

	public void setData(String... param){
		this.param = param;
		this.host = param[0];
		this.usr = param[1];
		this.psw = param[2];
	}
}






//	public void ask(final String title,final int number){
//		context.runOnUiThread(new Runnable() {
//
//			@Override
//			public void run() {	new AlertDialog.Builder(getActivity())
//			.setTitle("Server")
//			.setMessage(title)
//			.setPositiveButton("Si", new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					switch(number){
//					case 1:
//						new Thread(){
//							public void run(){
//								runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/raspiConfiguration.xml > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
//							}}.start();
//							break;
//					case 2:
//						new Thread(){
//							public void run(){
//								runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/libsqlitejdbc.so > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");
//							}}.start();
//							break;
//					case 3:
//						new Thread(){
//							public void run(){
//								runCommand("wget -P /root/server/ http://raelixx.ns0.it:81/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");	
//							}}.start();
//							break;
//					case 4:
//						new Thread(){
//							public void run(){//ask("posso avviare il Server?", 4);
//								runCommand("nohup java -jar /root/server/raspiServer.jar > /root/server/foo.out 2> /root/server/foo.err < /dev/null & ");		
//							}}.start();
//							screenAlert("Server Avviato");
//
//
//							break;
//					default:
//						break;
//					}
//				}
//			})
//			.setNegativeButton("No", new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) { 
//
//				}
//			})
//			.show();
//			}
//		});
//	}


//	public String runCommand(String command,Session session) {
//		context.runOnUiThread(new Runnable() {
//
//			@Override
//			public void run() {
//				pd = ProgressDialog.show(context, "Please Wait",
//						"Caricamento In Corso..", true);
//				pd.setCancelable(true);
//			}
//		});
//		StringBuilder sb=new StringBuilder();
//
//		JSch jsch = new JSch();
//		try {
//			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//			StrictMode.setThreadPolicy(policy);
//			session = jsch.getSession( usr, host, 22);
//			System.out.println("dopo get session");
//			session.setPassword( psw.getBytes());
//			session.setConfig("StrictHostKeyChecking", "no");
//			System.out.println("faccio la connect");
//			session.connect(5000);
//			System.out.println("connesso? "+session.isConnected());
//			Channel channel = session.openChannel("exec");
//			channel.setInputStream(null);
//			channel.setOutputStream(System.out);
//			((ChannelExec) channel).setCommand(command);
//			((ChannelExec) channel).setPty(false);
//			channel.connect();
//			InputStream in = channel.getInputStream();
//			byte[] tmp = new byte[1024];
//			while (true) {
//				InputStreamReader is = new InputStreamReader(in);
//
//				BufferedReader br = new BufferedReader(is);
//				String read = br.readLine();
//				while(read != null) {
//					System.out.println(read);
//					sb.append(read);
//					read =br.readLine();
//
//				}
//				if (channel.isClosed()) {
//					System.out.println(sb.toString());
//					System.out.println("exit-status:"+channel.getExitStatus());
//					break;
//				}
//				try {
//					Thread.sleep(1000);
//				} catch (Exception ee) {
//				}
//			}
//			channel.disconnect();
//			session.disconnect();
//		} catch (Exception e) {
//
//			e.printStackTrace();
//			return "error";
//		}
//		
//		pd.dismiss();
//		return sb.toString();
//	}
//



