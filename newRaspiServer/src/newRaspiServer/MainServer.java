package newRaspiServer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.StringTokenizer;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;
import newRaspiServer.MainServer.MainThread;
import newRaspiServer.MainServer.SendAlarm;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
public class MainServer  implements PROTOCOL{
	public static String getterUrl = "http://raelixx.ns0.it:81/";
	public static Sender sender = new Sender("AIzaSyCzsHHb_rYYuKvl3B0XvadQAX5UGLd3mEc");//KEY FOR Key for server applications
	static Configuratore conf; 
	static PinState HIGH = PinState.HIGH;
	static PinState LOW = PinState.LOW;
	static  GpioController gpio;
	static LinkedList<GpioPinDigitalOutput> gpioList = new LinkedList<GpioPinDigitalOutput>(); //0-14
	static LinkedList<GpioPinDigitalInput> gpioListInput = new LinkedList<GpioPinDigitalInput>(); //0-14
	static LinkedList<com.pi4j.io.gpio.Pin> gpioPin = new LinkedList<com.pi4j.io.gpio.Pin>();
	static LinkedList<Boolean> gpioStatus = new LinkedList<Boolean>();
	static LinkedList<ThreadListenGpio> threadActive = new LinkedList<>();
	public static ModbusTCPListener listener = null;
	static SimpleProcessImage spi = null;
	static int port = 502;
	static ReadDevices reader;
	static DBConnection logger;
	public static boolean isWin;
	public static Listener listeners ;
	public static void main(String[] args) throws ClassNotFoundException {
		MainThread firstThread = new MainThread();
		conf = new Configuratore();
		new ThreadInfo(firstThread,conf).start();
	}

	public static void exit(){
		System.exit(5);
		}
	

	static	class MainThread extends Thread{
		public MainThread(){
			try{
//				System.out.println("Java library path: "+System.getProperty("java.library.path"));
				System.setProperty( "java.library.path", "librxtxSerial.so" );
				Thread.currentThread().setName("MainThread");
				Debug.print("Thread: started "+Thread.currentThread().getName());
				printVersion();
				Debug.info("Thread: main avviato. Thread count: "+Thread.activeCount());
				listeners = new Listener();
				Debug.print("Modbus Server is running...");
				conf= new Configuratore();
				int client = 3;
				listener = new ModbusTCPListener(client);
				listener.setPort(port);
				listener.start();
				loadLibrary();
				Debug.info("Thread: "+(client+1)+" Client ModbusTCPListener avviati. Thread count: "+Thread.activeCount());
				spi = createRegisters(spi,conf);
				connectionThread(spi,conf);
				sender = new Sender("AIzaSyCzsHHb_rYYuKvl3B0XvadQAX5UGLd3mEc");
				if(listener.connectionHandler != null) 
					listener.connectionHandler.addPropertyChangeListener(listeners);
				GarbageCollector.callGC();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		public void loadLibrary(){
			try{
			String path = System.getProperty("java.library.path");
			StringTokenizer str = new StringTokenizer(path, ":");
			LinkedList<String> content = new LinkedList<String>();
			if(!isWin && conf.serialEnable){
				Runtime rt = Runtime.getRuntime();
				Process pr = null;
				if(new File("/usr/bin/rpi-serial-console").exists()){
					pr = rt.exec("sudo rpi-serial-console disable");
					Debug.info("UART enable TX/RX");
				}
				else {
					pr = rt.exec("sudo wget "+getterUrl+"rpi-serial-console -O /usr/bin/rpi-serial-console && sudo chmod +x /usr/bin/rpi-serial-console");
					pr = rt.exec("sudo rpi-serial-console disable");
					Debug.info("Downloaded UART script enable TX/RX");
				}
				while(str.hasMoreTokens()){
					content.add(str.nextToken());
				}
				if(!new File("/root/server/librxtxSerial.so").exists()){
					pr = rt.exec("wget "+getterUrl+"librxtxSerial.so");
					Debug.info("Downloaded librxtxSerial.so Shared Object");
				}
				if(!new File("/root/server/libsqlitejdbc.so").exists()){
					pr = rt.exec("wget "+getterUrl+"libsqlitejdbc.so");
					Debug.info("Downloaded libsqlitejdbc.so Shared Object");
				}
				if(!new File("/root/server/RXTXcomm.jar").exists()){
					pr = rt.exec("wget "+getterUrl+"RXTXcomm.jar");
					Debug.info("Downloaded RXTXcomm.jar Library Object");
				}
				for(int i = 0 ; i < content.size() ; i++){
					if(!new File(content.get(i)+"/librxtxSerial.so").exists()){
						rt = Runtime.getRuntime();
						pr = rt.exec("sudo cp librxtxSerial.so "+content.get(i)+"/");
						Debug.info("Coping librxtxSerial.so in "+content.get(i)+"/");
					}
					if(!new File(content.get(i)+"/RXTXcomm.jar").exists()){
						rt = Runtime.getRuntime();
						pr = rt.exec("sudo cp RXTXcomm.jar "+content.get(i)+"/");
						Debug.info("Coping RXTXcomm.jar in "+content.get(i)+"/");
					}
					if(!new File(content.get(i)+"/libsqlitejdbc.so").exists()){
						rt = Runtime.getRuntime();
						pr = rt.exec("sudo cp libsqlitejdbc.so "+content.get(i)+"/");
						Debug.info("Coping libsqlitejdbc.so in "+content.get(i)+"/");
					}
					else {
						continue;

					}
				}
			}
			}catch(Exception e){
				System.err.println("Error loading Shared Object!");
			}
		}
		
		public void printVersion(){
			String s  = "name: " + System.getProperty ("os.name");
			s += ", version: " + System.getProperty ("os.version");
			s += ", arch: " + System.getProperty ("os.arch");
			s += ", path: " + System.getProperty("user.dir");
			Debug.print("System " + s);
		}

		public static void close(){
			listener.stop();
			reader.interrupt = true;
			logger.interrupt = true;
			GarbageCollector.callGC();
			if(conf.GpioEnable){
				for(int i = 0 ; i < gpioList.size() ; i++)
					if(gpioList.get(i) != null){
						gpioList.get(i).unexport();
						GpioFactory.getInstance().unprovisionPin(gpioList.get(i));
					}
				for(int i = 0; i < gpioListInput.size() ; i++){
					if(gpioListInput.get(i) != null){

						gpioListInput.get(i).unexport();
						GpioFactory.getInstance().unprovisionPin(gpioListInput.get(i));
					}
				}
				GpioFactory.getInstance().unexportAll();
				GpioFactory.getInstance().removeAllListeners();
				GpioFactory.getInstance().shutdown();
				gpio.shutdown();
				gpioList = new LinkedList<>();	
				gpioListInput = new LinkedList<GpioPinDigitalInput>(); //0-14
				threadActive = new LinkedList<>();
				conf = null;
				gpioPin.clear();
				gpio = null;
				GarbageCollector.callGC();
			}
		}
	}


	public static SimpleProcessImage  createRegisters(SimpleProcessImage spi,Configuratore conf){
		spi = new SimpleProcessImage();
		spi.addRegister(new SimpleRegister(STATUS_OK));
		if(conf.GpioEnable){
			gpio = GpioFactory.getInstance(); 
			addGpio();
			for(int k = 0; k < conf.pins.size() ; k++){
				Pin pin = conf.pins.get(k);
				if(pin.isUse()){
					if(pin.getScope().toLowerCase().contentEquals("in")){   
						spi.addRegister(new SimpleRegister(IN_OK));
						GpioPin c = GpioFactory.getInstance().getProvisionedPins().iterator().next();
						GpioPinDigitalInput resulted = null;
						if(c.getName().contains("GPIO "+conf.pins.get(k).getNumber())){
							resulted = (GpioPinDigitalInput) c;
							resulted.setMode(PinMode.DIGITAL_INPUT);
							resulted.setPullResistance(PinPullResistance.PULL_DOWN);						
						}
						else  resulted = GpioFactory.getInstance().provisionDigitalInputPin(gpioPin.get(conf.pins.get(k).getNumber()), PinPullResistance.PULL_DOWN);
						ThreadListenGpio threadUsed = new ThreadListenGpio(resulted, spi,k,conf.pins.get(k));
						threadUsed.start();
						threadActive.add(threadUsed);
						gpioList.add(null);
						gpioListInput.add(resulted);
					}
					else if(pin.getScope().toLowerCase().contentEquals("out")){
						spi.addRegister(new SimpleRegister(OUT_OFF));		 //OUT CASE
						GpioPinDigitalOutput resulted = gpio.provisionDigitalOutputPin(gpioPin.get(conf.pins.get(k).getNumber()), HIGH);
						gpioList.add(resulted);	
					}
				}
			}
		}
		if(conf.DevicesEnable){
			for(int k = 0; k < conf.slaves.size();k++){
				for(int f = 0; f < conf.slaves.get(k).getRegisters().size(); f++){
					spi.addRegister(new SimpleRegister(0));
				}
			}
		}
		ModbusCoupler.getReference().setProcessImage(spi);
		ModbusCoupler.getReference().setMaster(false);
		ModbusCoupler.getReference().setUnitID(1);
		return spi;

	}



	public static void createRegister( SimpleProcessImage spi){
		spi = new SimpleProcessImage();
		spi.addInputRegister(new SimpleInputRegister(45));
		ModbusCoupler.getReference().setProcessImage(spi);
		ModbusCoupler.getReference().setMaster(false);
		ModbusCoupler.getReference().setUnitID(1);
	}

	public static class Listener implements PropertyChangeListener {
		@SuppressWarnings({ "static-access", "unused" })
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			//			System.out.println("address: "+((int)event.getOldValue() + 40001)+" new value: "+event.getNewValue().toString());
			conf.readFromFile();
			int gpioSize = 0;
			if(conf.GpioEnable)
				gpioSize=conf.gpioNumber + STATUS_OK;
			else gpioSize = 1;
			int register = (int)event.getOldValue() ;
			if(register < gpioSize && register > 0 && conf.GpioEnable){

				if(spi.getRegister(register ).getValue() == OUT_OFF ){ 
					System.out.println("Spengo");
					gpioList.get(register - 1 ).high();			
					if(conf.pins.get(register -1 ).isAlarm()){
						new SendAlarm(false, conf.pins.get(register -1 ).getName()).start();
					}
				}
				else if(spi.getRegister(register ).getValue() == OUT_ON ){
					System.out.println("Accendo");
					if(conf.pins.get(register -1 ).isAlarm()){
						new SendAlarm(true, conf.pins.get(register -1 ).getName()).start();
					}
					if(conf.pinsUsed.get(register - 1).getDelay() != 0){
						spi.setRegister(register , new SimpleRegister(OUT_ON));
						new ThreadSleeping(spi, gpioList.get(register - 1), conf.pinsUsed.get(register - 1).getDelay(), register,conf.pinsUsed.get(register - 1).isAlarm(),conf.pinsUsed.get(register - 1).getName() ).start();
						spi.setRegister(register , new SimpleRegister(OUT_OFF));
					}
					else{
						gpioList.get(register-1).low();		
					}
				}
				else if((int)event.getNewValue() != OUT_ON || (int)event.getNewValue() != OUT_OFF||(int)event.getNewValue() != OUT_ON|| (int)event.getNewValue() != IN_OK || (int)event.getNewValue() != IN_ALARM){
					if(!conf.pins.get(register -1 ).getScope().contentEquals("in")){
						if(gpioList.get(register - 1).isLow() || gpioList.get(register - 1).isHigh()){
							gpioList.get(register - 1).high();					//RESETTO IL PIN PER COMANDO ERRATO
							spi.setRegister(register , new SimpleRegister( OUT_OFF));
						}
					}
					else {
						if(conf.pinsUsed.get(register - 1).isAlarm())
							//							sendAlarmReset(conf.pinsUsed.get(register - 1).getName());
							spi.setRegister(register , new SimpleRegister( IN_OK));//CASO DI RESET ALLARME SU SENSORE IN
					}
				}																
			}
			else if(register >= 0 ){
				int slave= 0;
				int myregister = (int)register-gpioSize;
				if(myregister > 0){
					while(myregister != 0){
						int allSize = 0;
						for(int i = 0; i < conf.slaves.size() ; i++){
							allSize +=conf.slaves.get(i).getRegisters().size();
							if(myregister > allSize){
								slave +=1;
							}
							if(myregister < allSize){
								int division = allSize - myregister;
								int result = conf.slaves.get(i).getRegisters().size() - division;
								if(conf.slaves.get(i).getProtocol().contentEquals("tcp")){
									new HoldingRegisterTCPMaster().writeHoldingRegister(conf.slaves.get(i).getAddress(), 
											conf.slaves.get(i).getId(), (int)conf.slaves.get(i).getRegisters().get(result).getAddress() - 40000,
											(int)event.getNewValue());
									return;
								}
								else {
									SerialSlave ss = null;
									if(reader != null)
										for(int q = 0; 	reader.slaveConnected != null && q < reader.slaveConnected.size();q++ )
											if(reader.slaveConnected.get(q).getPortname().contentEquals("/dev/ttyUSB00"+conf.slaves.get(i).getId()))
												ss = reader.slaveConnected.get(q);
									if(ss != null)
										if(!ss.con.isOpen())
											ss.connect();
									ss.writeHoldingMultipleRegister((int)conf.slaves.get(i).getRegisters().get(result).getAddress() - 40000, (int)event.getNewValue());
									return;
								}
							}
						}
					}
				}
				else{//dev/ttyUSB00+id
					new HoldingRegisterTCPMaster().writeHoldingRegister(conf.slaves.get(0).getAddress(), conf.slaves.get(0).getId(), (int)conf.slaves.get(0).getRegisters().get(0).getAddress() - 40000,(int)event.getNewValue());	
				}
			}
		}
	}


	public static void connectionThread(SimpleProcessImage spi,Configuratore conf){
		if(reader != null){
			reader.interrupt = true;
		}
		if(logger != null){
			logger.interrupt = true;
		}
		reader = new ReadDevices(spi);
		reader.start();
		logger = new DBConnection(spi);
		logger.start();
		Debug.info("Thread: Poll devices and Log started. Thread count: "+Thread.activeCount());
	}

	public static void addGpio(){
		gpioPin.add(RaspiPin.GPIO_00);
		gpioPin.add(RaspiPin.GPIO_01);
		gpioPin.add(RaspiPin.GPIO_02);
		gpioPin.add(RaspiPin.GPIO_03);
		gpioPin.add(RaspiPin.GPIO_04);
		gpioPin.add(RaspiPin.GPIO_05);
		gpioPin.add(RaspiPin.GPIO_06);
		gpioPin.add(RaspiPin.GPIO_07);
		gpioPin.add(RaspiPin.GPIO_08);
		gpioPin.add(RaspiPin.GPIO_09);
		gpioPin.add(RaspiPin.GPIO_10);
		gpioPin.add(RaspiPin.GPIO_11);
		gpioPin.add(RaspiPin.GPIO_12);
		gpioPin.add(RaspiPin.GPIO_13);
		gpioPin.add(RaspiPin.GPIO_14);
		for(int i = 0; i < gpioStatus.size() ; i++){
			gpioStatus.set(i, false);
		}

	}

	public static class SendAlarm extends Thread{
		boolean acceso; String name;
		public SendAlarm(boolean acceso, String name){
			super();
			this.acceso = acceso;
			this.name = name;
		}
		public void run(){
			String status = acceso ? "acceso" : "spento";
			Message message = new Message.Builder()
			.addData("message", name+" "+status)
			.addData("other-parameter", "some value")
			.build();
			try {
				if(MainServer.conf != null)
					if(MainServer.conf.idKey != null){
						MainServer.sender.send(message,  MainServer.conf.idKey, 3) ;
					}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}


	public static class SendAlarmString extends Thread{
		String name;String slave;int value;boolean high;
		public SendAlarmString(String name,String slave,int value,boolean high){
			super();
			this.slave = slave;
			this.name = name;
			this.value = value;
			this.high = high;
		}
		public void run(){
			String messages = high ? "Il registro "+slave+" di "+name+" ha raggiunto: "+value : "Il registro "+name+" di "+slave+" e' sceso: "+value;
			Message message = new Message.Builder()
			.addData("message", messages)
			.addData("other-parameter", "some value")
			.build();
			try {
				if(MainServer.conf != null)
					if(MainServer.conf.idKey != null){
						MainServer.sender.send(message,  MainServer.conf.idKey, 3) ;
					}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class ThreadSleeping extends Thread implements PROTOCOL{
	SimpleProcessImage spi;GpioPinDigitalOutput pin; int delay;int registerPosition; String name;boolean alarm;
	public ThreadSleeping(SimpleProcessImage spi,GpioPinDigitalOutput pin, int delay,int registerPosition,boolean alarm,String name){
		super();
		this.spi = spi;
		this.pin = pin;
		this.delay = delay;
		this.registerPosition = registerPosition;
		this.name = name;
		this.alarm = alarm;
	}
	public void run(){
		pin.low();
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("Cannot Delay");
		}
		pin.high();
		if(alarm)
			new SendAlarm(false,name);
	}
}

 class ThreadInfo extends Thread{
	private static final int SERVER_PORT = 503;
	MainThread main ;
	Configuratore conf;
	
	public ThreadInfo(MainThread main, Configuratore conf){
		this.main = main;
		this.conf = conf;
	}
	@Override
	public void run(){
		super.run();
		ServerSocket servsock ;
		try {
			main.start();
			servsock = new ServerSocket(SERVER_PORT);
			while (true) {
				Thread.currentThread().setName("ServerThread");
				System.out.println("ServerHelper started listen on port: "+SERVER_PORT);//Questo server si occuperà anche di riavviare i gpio in caso di modifica del file
				Socket sock = servsock.accept();
				new ExecutorThread(sock,conf,main).start();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void exit(){
		System.exit(1);
	}
}


//class ThreadInfo extends Thread{
//	MainThread thread;
//	ServerSocket servsock;
//	public ThreadInfo(MainThread thread){
//		this.thread = thread;
//		this.thread.start();
//		try {
//			servsock = new ServerSocket(503);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	public void run(){
//		while (true) {
//			try {
//				Thread.currentThread().setName("ServerThread");
//				Debug.print("Thread: started "+Thread.currentThread().getName());
//				System.out.println("Server configuratore avviato");//Questo server si occuperà anche di riavviare i gpio in caso di modifica del file
//				Socket sock = servsock.accept();
//				sock.setSoTimeout(1000);
//				if (sock != null){
//					Debug.info("Accetto Client in Connessione");
//				}
//				else return;
//				OutputStream os = sock.getOutputStream();
//				InputStream is = sock.getInputStream();
//				DataInputStream dis = new DataInputStream(is);
//				DataOutputStream dos = new DataOutputStream(os);
//				String packet = "";
//				packet = dis.readUTF();
//				System.out.println(packet);
//				int len;
//				FileInputStream in ;
//				byte[] buf;
//				switch(packet){
//				case "ReadCounter":
//					Configuratore conf = new Configuratore();
//					conf.readFromFile();
//					dos.writeUTF(""+conf.counter);
//					dos.flush();
//					dos.close();
//					break;
//				case"ReadFile":
//					File myFile = new File(Configuratore.fileConfig);
//					dos = new DataOutputStream(sock.getOutputStream()); //get the output stream of the socket
//					dos.writeInt((int) myFile.length()); //write in the length of the file
//					in = new FileInputStream(myFile); //create an inputstream from the file
//					buf = new byte[8192]; //create buffer
//					len = 0;
//					while ((len = in.read(buf)) != -1) {
//						os.write(buf, 0, len); //write buffer
//					}
//					in.close(); //clean up
//					os.close();
//					dos.close();
//					sock.close();
//					break;
//				case"ReadDb":
//					dis = new DataInputStream(is);
//					String packet1 = dis.readUTF();
//					int	currentMonth = Calendar.getInstance().get(Calendar.MONTH);
//					int	currentYear =  Calendar.getInstance().get(Calendar.YEAR);
//					String	database = "data_"+currentMonth+"_"+currentYear+".db";
//					if(new File(Configuratore.database+packet1).exists()){
//						dos = new DataOutputStream(os);
//						dos.writeUTF("ok");
//						dos.flush();
//						Debug.print("esiste il db richiesto con nome: "+packet1);
//						myFile = new File(Configuratore.database+packet1);
//						dos = new DataOutputStream(sock.getOutputStream()); //get the output stream of the socket
//						dos.writeInt((int) myFile.length()); //write in the length of the file
//						in = new FileInputStream(myFile); //create an inputstream from the file
//						buf = new byte[8192]; //create buffer
//						len = 0;
//						while ((len = in.read(buf)) != -1) {
//							os.write(buf, 0, len); //write buffer
//						}
//						in.close(); 
//						dos.close();
//						os.close();;
//						sock.close();
//						break;
//					}
//					else{
//						System.out.println("non esiste il db richiesto con nome: "+packet1);
//						dos = new DataOutputStream(os);
//						dos.writeUTF("notfound");
//						dos.close();
//						os.close();;
//						sock.close();
//						break;
//					}
//				case"WriteFile":
//					byte[] mybytearrays = new byte[4096];
//					FileOutputStream fos = new FileOutputStream(""+Configuratore.fileConfig);
//					BufferedOutputStream bos = new BufferedOutputStream(fos);
//					int bytesRead = is.read(mybytearrays, 0, mybytearrays.length);
//					bos.write(mybytearrays, 0, bytesRead);
//					bos.close();
//					this.thread.interrupt();
//					System.gc();
//					this.thread = new MainThread();
//					this.thread.start();
//					break;
//				}
//
//				sock.close();
//			}
//			catch (IOException e) {
//				e.printStackTrace();
//				Debug.err("Kernel kill pid!");
//				MainThread.close();
//				System.exit(0);
//
//			}
//		}
//	}
//}

