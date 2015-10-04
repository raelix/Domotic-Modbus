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
import java.net.UnknownHostException;
public class Client {
 



  public static void main(String[] argv) throws UnknownHostException, IOException  {
    Socket sock = new Socket("192.168.0.10", 503);
    
    OutputStream os = sock.getOutputStream();
    DataOutputStream  dao = new DataOutputStream(os);
//    dao.writeUTF("ReadCounter");
//    ReadCounter(sock);
//    dao.writeUTF("WriteFile");
    dao.writeUTF("ReadFile");
    ReadFile(sock);
//   WriteFile(sock);
  }
  
  public static int ReadCounter(Socket sock){
	  int counter = 0;
	  try {
		DataInputStream dai = new DataInputStream( sock.getInputStream());
		counter = Integer.parseInt(dai.readUTF());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  System.out.println("Version Server is :"+counter);
	return counter;
  }
  
  public static void ReadFile(Socket sock) throws IOException{

		DataInputStream dis = new DataInputStream(sock.getInputStream()); //get the socket's input stream
		InputStream in = sock.getInputStream(); 
		FileOutputStream out = new FileOutputStream("C:/Users/gianmarco/Desktop/raspiConfigurationsasr.xml");
		byte[] buf = new byte[1024]; //buffer
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len); //write buffer
		}
		out.close(); //clean up
		in.close();
		dis.close();
  }
  
  public static void WriteFile(Socket sock){

	try {
		  File myFile = new File("C:/Users/gianmarco/Desktop/raspiConfiguration.xml");
		  byte[] mybytearray = new byte[(int) myFile.length()];
	      OutputStream os;
			os = sock.getOutputStream();
		  BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
		  bis.read(mybytearray, 0, mybytearray.length);
		  os.write(mybytearray, 0, mybytearray.length);
	      os.flush();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

  }

}