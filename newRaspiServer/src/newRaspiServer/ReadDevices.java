package newRaspiServer;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

public class ReadDevices extends Thread{
	LinkedList<Slave> slaves ;
	SimpleProcessImage spi;
	LinkedList<SerialSlave> slaveConnected ;
	Configuratore conf;
	int slaveSize;
	int gpioUsed;
	public boolean interrupt = false;
	public ReadDevices(SimpleProcessImage spi){
		super();
		this.spi = spi;
		conf = new Configuratore();
		conf.readFromFile();
		slaves = new LinkedList<Slave>();
		slaveConnected = new LinkedList<SerialSlave>();
		slaves = conf.slaves;
		gpioUsed = conf.gpioNumber;
		slaveSize = conf.slaveSize;
		for(int i = 0; i < slaves.size(); i++){
			if(slaves.get(i).getProtocol().toLowerCase().contentEquals("rtu") ){
				if(!MainServer.isWin){
					String add = slaves.get(i).getAddress();
					if(add.contains("/dev/ttyAMA")){
						String res = "/dev/ttyUSB00"+slaves.get(i).getId();
						slaves.get(i).setAddress(res);
						Runtime rt = Runtime.getRuntime();
						try {
							if(!new File(res).exists()){
								Process pr = rt.exec("sudo ln -s "+add+" "+res);}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				slaveConnected.add(new SerialSlave(slaves.get(i).getAddress(), slaves.get(i).getBaudrate(), slaves.get(i).getProtocol(), slaves.get(i).getId(),slaves.get(i).getTimeout(),slaves.get(i).getRetries()));
			}
			else slaveConnected.add(null);
		}
		Thread.currentThread().setName("ReadDevices");
		Debug.print("Thread: started "+Thread.currentThread().getName());
	}

	@SuppressWarnings("static-access")
	public void run(){
		super.run();
		conf = new Configuratore();
		slaves = conf.slaves;

		while(true && !interrupt){
			try {
				Thread.currentThread().setName("ReadDevices");
				Thread.sleep(1000);
				for(int i = 0; i < slaves.size(); i++){
					if(slaves.get(i).getProtocol().toLowerCase().contentEquals("rtu")){

						new Thread(){
							int i;
							public void setIndex(int index){
								this.i = index;
								start();
							}
							@Override
							public void run(){
								super.run();
								for(int f = 0; f < slaves.get(i).getRegisters().size(); f++){

									int position = calculatePosition(i, f);
									SerialSlave tempSlave = slaveConnected.get(i);
									if(tempSlave != null)
										if(tempSlave.con != null && !tempSlave.con.isOpen() )
											tempSlave.connect();
									tempSlave.unitid = slaves.get(i).getId();
									tempSlave.setRetries(slaves.get(i).getRetries());
									int value = -1;
									value =  (int)	(tempSlave.readHoldingRegister(slaves.get(i).getRegisters().get(f).getAddress()-40000));
									Register temp = slaves.get(i).getRegisters().get(f);
									Debug.print(slaves.get(i).getName()+" "+temp.getName()+" Value : "+value);
									if(temp.isAlarm()){
//										Debug.info(" Max Value: "+temp.getHighAlarm()+" Min Value: "+temp.getLowAlarm());
										if(value == -1 && !temp.isNotificated()){
											temp.setConnected(false);
											temp.setNotificated(true);
											new MainServer.SendAlarmString(slaves.get(i).getName(),temp.getName()+" is Disconnected",0,true).start();
											Debug.print("Alarm: "+slaves.get(i).getName()+" Disconnected!Sending notify...");
										}
										else if(value  > slaves.get(i).getRegisters().get(f).getHighAlarm() && value != -1 && !temp.isNotificated()){
											new MainServer.SendAlarmString(slaves.get(i).getName(),temp.getName(),value,true).start();
											Debug.print("Alarm: Value too high!Sending notify...");
											temp.setNotificated(true);
										}
										else if(value  < temp.getLowAlarm() && value != -1 && !temp.isNotificated()){
											new MainServer.SendAlarmString(slaves.get(i).getName(),temp.getName(),value,false).start();
											Debug.print("Alarm: Value too low!Sending notify...");
											temp.setNotificated(true);
										}
										else {
											if(!temp.isConnected() && value > 0){
												temp.setConnected(true);
												temp.setNotificated(false);
												new MainServer.SendAlarmString(slaves.get(i).getName(),temp.getName()+" is Reconnected",0,true).start();
												Debug.print("Alarm: "+slaves.get(i).getName()+" Reconnected!Sending notify...");
											}
											else if(value > 0 && value  < temp.getHighAlarm())
												temp.setNotificated(false);
										}
									}
									MainServer.spi.setRegister(position,new SimpleRegister(value));
								}
							}
						}.setIndex(i);
						Thread.sleep(slaves.get(i).getPolling());
					}
					else if(slaves.get(i).getProtocol().toLowerCase().contentEquals("tcp")){
						new Thread(){
							int i;
							public void setIndex(int index){
								this.i = index;
								start();
							}
							@Override
							public void run(){
								super.run();
								for(int f = 0; f < slaves.get(i).getRegisters().size(); f++){
									int position = calculatePosition(i, f);
									new HoldingRegisterTCPMaster();
									int value = -1;
									value = (int) ((HoldingRegisterTCPMaster.readHoldingRegister(slaves.get(i).getAddress(), slaves.get(i).getId(), slaves.get(i).getRegisters().get(f).getAddress()-40000,slaves.get(i).getRetries())));
									Register temp = slaves.get(i).getRegisters().get(f);
									Debug.print(slaves.get(i).getName()+" "+slaves.get(i).getRegisters().get(f).getName()+" Value : "+value);
									if(temp.isAlarm()){
//										Debug.info(" Max Value: "+temp.getHighAlarm()+" Min Value: "+temp.getLowAlarm());
										if(value == -1 && !temp.isNotificated()){
											temp.setConnected(false);
											temp.setNotificated(true);
											new MainServer.SendAlarmString(slaves.get(i).getName(),temp.getName()+" is Disconnected",0,true).start();
											Debug.print("Alarm: "+slaves.get(i).getName()+" Disconnected!Sending notify...");
										}
										else if(value  > slaves.get(i).getRegisters().get(f).getHighAlarm() && value != -1 && !temp.isNotificated()){
											new MainServer.SendAlarmString(slaves.get(i).getName(),temp.getName(),value,true).start();
											Debug.print("Alarm: Value too high!Sending notify...");
											temp.setNotificated(true);
										}
										else if(value  < temp.getLowAlarm() && value != -1 && !temp.isNotificated()){
											new MainServer.SendAlarmString(slaves.get(i).getName(),temp.getName(),value,false).start();
											Debug.print("Alarm: Value too low!Sending notify...");
											temp.setNotificated(true);
										}
										else {
											if(!temp.isConnected() && value > 0){
												temp.setConnected(true);
												temp.setNotificated(false);
												new MainServer.SendAlarmString(slaves.get(i).getName(),temp.getName()+" is Reconnected",0,true).start();
												Debug.print("Alarm: "+slaves.get(i).getName()+" Reconnected!Sending notify...");
											}
											else if(value > 0 && value  < temp.getHighAlarm())
												temp.setNotificated(false);
										}
									}
									MainServer.spi.setRegister(position,new SimpleRegister(value));
									Debug.info("Sleeping "+slaves.get(i).getPolling()+" ms");
									//							Thread.sleep(slaves.get(i).getPolling());
								}
							}

						}.setIndex(i);
						Thread.sleep(slaves.get(i).getPolling());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	//	public static void sendAlarm(String slave,String name,int value,boolean high){
	//		String messages = high ? "Il registro "+name+" di "+slave+" e' salito a: " : "Il registro "+name+" di "+slave+" e' sceso a: ";
	//		
	//		Message message = new Message.Builder()
	//	    .addData("message", messages)
	//	    .addData("other-parameter", "some value")
	//	    .build();
	//	try {
	//		Result result = MainServer.sender.send(message, "APA91bFEkQmPZfDc_3xnvzWD5iAIY8vfQO2mbFuHJytU-33zvMZjk-2imvjZ6CBDN4EWfOKRFVp5f3FMs-j3K_Isr548hPBtEnI_IQ_cW9OkzVAi9lPNPPKnacCdyI-GqEqW13Yf-LJ2qCb58LynI_aLRDoeQTUmwuDzq0GrymhCPNKVDxy9s7M", 3);
	//		System.out.println("sending");
	//	} catch (IOException e) {
	//		System.out.println("error sending");
	//		e.printStackTrace();
	//	}
	//	}

	public int calculatePosition(int slave , int registerNumber){
		int position = 1;
		if(conf.GpioEnable)
			position+=conf.gpioNumber;
		//			System.out.println("gpioUsed:"+conf.gpioNumber);
		//		System.out.println("1"+"position is:"+position);
		for(int i = 0; i < slave; ++i){
			position +=slaves.get(i).getRegisters().size();
		}

		position+=registerNumber;
		//				System.out.println("3"+"position is:"+position);

		return position;
	}

}
