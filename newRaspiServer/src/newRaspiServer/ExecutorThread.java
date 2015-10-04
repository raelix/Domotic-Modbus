package newRaspiServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import newRaspiServer.MainServer.MainThread;

public class ExecutorThread extends Thread {
	Socket sock = null;
	InputStream in = null;
	OutputStream out = null;
	DataInputStream dis = null;
	DataOutputStream dos = null;
	File file = null;
	byte[] buf = null;
	Configuratore conf;
	MainThread main;
	ExecutorThread(Socket sock,Configuratore conf,MainThread main){
		try {
			this.sock = sock;
			this.main = main;
			out = sock.getOutputStream();
			in = sock.getInputStream();
			dis = new DataInputStream(in);
			dos = new DataOutputStream(out);
			this.conf = conf;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run(){
		super.run();
		try{
			switch(dis.readUTF()){
			case "ReadCounter":
				ReadCounter();
				break;
			case"ReadFile":
				readFile();
				break;
			case"ReadDb":
				readDb();
				break;
			case"WriteFile":
				writeFile();
			case"Reboot":
				MainServer.exit();
				break;
			}
		}
		catch(Exception e){		
		}
	}

	public void ReadDb() throws IOException{
		String db_asked = dis.readUTF();
		if(fileExist(Configuratore.database+db_asked)){
			dos.writeUTF("ok");
			dos.flush();
			file = new File(Configuratore.database+db_asked);
			dos.writeInt((int) file.length()); //write in the length of the file
			buf = new byte[1024]; //create buffer
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len); //write buffer
			}
			close();
			return;
		}
		else{
			System.out.println("non esiste il db richiesto con nome: "+db_asked);
			dos.writeUTF("notfound");
			close();
			return;
		}
	}

	public void readDb(){
		try{
			String packet1 = dis.readUTF();
			if(new File(Configuratore.database+packet1).exists()){
				dos.writeUTF("ok");
				dos.flush();
				Debug.print("esiste il db richiesto con nome: "+packet1);
				file = new File(Configuratore.database+packet1);
				dos.writeInt((int) file.length()); //write in the length of the file
				in = new FileInputStream(file); //create an inputstream from the file
				buf = new byte[8192]; //create buffer
				int len = 0;
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len); //write buffer
				}
				in.close(); 
				dos.close();
				out.close();
				sock.close();
				return;
			}
			else{
				System.out.println("non esiste il db richiesto con nome: "+packet1);
				dos.writeUTF("notfound");
				dos.close();
				out.close();
				sock.close();
				return;
			}
		}catch(Exception e){
		}
	}

	public void ReadCounter() throws Exception{
		System.out.println("read counter server: "+conf.counter);
		conf = new Configuratore();
		dos.writeUTF(""+conf.counter);
		dos.flush();
		close();


	}

	public boolean fileExist(String file){
		if(new File(file).exists())return true;
		else return false;
	}

	public void close() throws IOException{
		dos.close();
		dis.close();
		in.close();
		out.close();
		sock.close();
	}

	public void readFile() throws Exception{
		File myFile = new File(Configuratore.fileConfig);
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

	public void writeFile() throws Exception{
		file = new File(Configuratore.fileConfig);
		//		int size = dis.readInt(); //get the size of the file.
		FileOutputStream out = new FileOutputStream(file);
		int totalBytesRead = 0;
		byte[] buf = new byte[8192]; //buffer
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len); //write buffer
		}
		dis.close();
		out.close(); //clean up
		in.close();
		MainServer.exit();

	}



}