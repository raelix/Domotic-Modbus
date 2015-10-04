
import java.io.*;
import java.net.*;


public class Serv {
 
	 public static void main(String[] args) throws IOException {
		    ServerSocket servsock = new ServerSocket(1234);
		    File myFile = new File("C:/Users/gianmarco/Desktop/configurationNewRaspiServe.xml");
		    while (true) {
		      Socket sock = servsock.accept();
		      byte[] mybytearray = new byte[(int) myFile.length()];
		      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
		      bis.read(mybytearray, 0, mybytearray.length);
		      OutputStream os = sock.getOutputStream();
		      os.write(mybytearray, 0, mybytearray.length);
		      os.flush();
		      sock.close();
		    }
		  }
}