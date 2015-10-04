package newRaspiServer;

import java.io.File;
import java.util.LinkedList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Configuratore {
	public  int counter;
	public  boolean GpioEnable;
	public  boolean DevicesEnable;
	public  boolean PushEnable;
	public LinkedList<Pin> pinsUsed = new LinkedList<>();
	public LinkedList<Pin> pins = new LinkedList<>();
	public LinkedList<Slave> slaves = new LinkedList<>();
	public  int slaveSize = 0;
	public  int gpioNumber;
	public 	String telephone;
	public 	String idKey;
	public 	String mail;
	public 	String message;
	public boolean serialEnable = false;
	public static int debug = 1;
	public  static String fileConfig ;
	public  static String database ;


	public Configuratore(){
		if(System.getProperty ("os.name").toLowerCase(Locale.ITALY).contains("win")){
			MainServer.isWin = true;
			fileConfig = "C:/Users/gianmarco/Desktop/raspiConfiguration.xml";
			database = "C:/Users/gianmarco/Desktop/";
		}
		else{
			MainServer.isWin = false;
			fileConfig = "/root/server/raspiConfiguration.xml";
			database = "/root/server/";
		}
		while(!new File(fileConfig).exists()){
			System.out.println("waiting configuration file!");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
//		if(MainServer.isWin){
//			writeConf(false);
//		}
//		else {
//			writeConf(true);
//		}
		if(new File(fileConfig).exists()){
			readFromFile();
		}
	}


	public  void readFromFile() {
		//creare mapping xml tipo type=EnergyMeter/Station/RadioController/Default/                  scope= in/out/simpleValue/gauge/graphic
		//posso passare Gpio Pin Devices Slave Register    Da qui creare due liste di oggetti Pin e Slave
		try {
			File fXmlFile = new File(fileConfig);
			slaveSize = 0;
			gpioNumber = 0;
			pinsUsed = new LinkedList<Pin>();
			slaves = new LinkedList<>();
			pins = new LinkedList<>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Server");
			Node nNode = nList.item(0);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				//RECUPERO VERSIONE FILE
				NodeList nodo = eElement.getElementsByTagName("Version");
				counter = Integer.parseInt(nodo.item(0).getAttributes().item(0).getNodeValue());
				//RECUPERO STATO GPIO
				nodo = eElement.getElementsByTagName("Gpio");
				GpioEnable = nodo.item(0).getAttributes().item(0).getNodeValue().toLowerCase().contentEquals("true") ? true : false;
				nodo = eElement.getElementsByTagName("Pin");
				//				int lunghezza =  eElement.getElementsByTagName("Pin").getLength();
				for(int i = 0; i<  nodo.getLength(); i++){
					if (nodo.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Pin pin = new Pin();
						for(int k = 0; k < nodo.item(i).getAttributes().getLength(); k++){

							switch(nodo.item(i).getAttributes().item(k).getNodeName().toLowerCase()){
							case "delay":
								pin.setDelay(Integer.parseInt(nodo.item(i).getAttributes().item(k).getNodeValue()));
								break;
							case "log":
								pin.setLog(Integer.parseInt(nodo.item(i).getAttributes().item(k).getNodeValue()));
								break;
							case "name":
								pin.setName(nodo.item(i).getAttributes().item(k).getNodeValue());
								break;
							case "number":
								pin.setNumber(Integer.parseInt(nodo.item(i).getAttributes().item(k).getNodeValue()));
								break;
							case "scope":
								pin.setScope(nodo.item(i).getAttributes().item(k).getNodeValue());
								break;
							case "alarm":
								pin.setAlarm(nodo.item(i).getAttributes().item(k).getNodeValue().toLowerCase().contentEquals("true") ? true : false);
								break;
							case "use":
								pin.setUse(nodo.item(i).getAttributes().item(k).getNodeValue().toLowerCase().contentEquals("true") ? true : false);
								if(pin.isUse())
									gpioNumber+=1;
								break;
							}
						}
						NodeList nodoOptional = nodo.item(i).getChildNodes();
						for(int f = 0;  f < nodoOptional.getLength(); f++){
							if (nodoOptional.item(f).getNodeType() == Node.ELEMENT_NODE) {
								if(nodoOptional.item(f).getNodeName().contentEquals("Optional")){
									String name = nodoOptional.item(f).getAttributes().getNamedItem("name").getNodeValue();
									String type = nodoOptional.item(f).getAttributes().getNamedItem("type").getNodeValue();
									Optional optional = new Optional(name, type);
									NodeList nodoOptionalInfo = nodoOptional.item(f).getChildNodes();
									for(int z = 0 ; z < nodoOptionalInfo.getLength(); z++){
										if (nodoOptionalInfo.item(z).getNodeType() == Node.ELEMENT_NODE) {
											String key = nodoOptionalInfo.item(z).getAttributes().getNamedItem("name").getNodeValue();
											String value = nodoOptionalInfo.item(z).getAttributes().getNamedItem("value").getNodeValue();
											optional.setKeyValue(key, value);
										}
									}
									pin.setOptional(optional);
								}
							}
						}
						if(pin.isUse())
							pinsUsed.add(pin);
						pins.add(pin);
					}
				}//<Slave id="1" protocol="rtu" address="COM2" baudrate="9600" polling="2000" type="EnergyMeter" name="GaugeMetro">
				DevicesEnable = eElement.getElementsByTagName("Devices").item(0).getAttributes().getNamedItem("enable").getNodeValue().toLowerCase().contentEquals("true") ? true : false;
				if(DevicesEnable){
					nodo = eElement.getElementsByTagName("Slave");
					for(int son = 0; son < nodo.getLength() ; son++){

						if (nodo.item(son).getNodeType() == Node.ELEMENT_NODE) {
							Slave slave = new Slave();
							slaveSize++;
							slave.setRegisters(new LinkedList<Register>());
							NamedNodeMap map = nodo.item(son).getAttributes();
							slave.setId(Integer.parseInt(map.getNamedItem("id").getNodeValue()));
							slave.setProtocol(map.getNamedItem("protocol").getNodeValue());
							if(slave.getProtocol().contentEquals("rtu"))serialEnable = true;
							slave.setAddress(map.getNamedItem("address").getNodeValue());
							slave.setBaudrate(Integer.parseInt(map.getNamedItem("baudrate").getNodeValue()));
							slave.setPolling(Integer.parseInt(map.getNamedItem("polling").getNodeValue()));
							slave.setTimeout(Integer.parseInt(map.getNamedItem("timeout").getNodeValue()));
							slave.setRetries(Integer.parseInt(map.getNamedItem("retries").getNodeValue()));
							slave.setType(map.getNamedItem("type").getNodeValue());
							slave.setName(map.getNamedItem("name").getNodeValue());
							//<Register address="40001" name="Idc" scope="SimpleValue" log="" rw="false"/>
							for(int f = 0; f < nodo.item(son).getChildNodes().getLength() ; f++ ){
								if (nodo.item(son).getChildNodes().item(f).getNodeType() == Node.ELEMENT_NODE) {
									map =nodo.item(son).getChildNodes().item(f).getAttributes();

									Register registro = new Register(Integer.parseInt(map.getNamedItem("log").getNodeValue()), map.getNamedItem("name").getNodeValue(),
											map.getNamedItem("scope").getNodeValue(), Integer.parseInt(map.getNamedItem("address").getNodeValue()),
											map.getNamedItem("rw").getNodeValue().toLowerCase().contentEquals("true")? true : false,map.getNamedItem("alarm").getNodeValue().toLowerCase().contentEquals("true")? true : false, Float.parseFloat(map.getNamedItem("lowAlarm").getNodeValue()),Float.parseFloat(map.getNamedItem("highAlarm").getNodeValue()),Float.parseFloat(map.getNamedItem("multiplies").getNodeValue()));
									//									System.out.println(nodo.item(son).getChildNodes().item(f).getChildNodes());
									if(nodo.item(son).getChildNodes().item(f).getChildNodes() != null){
										NodeList nodoOptional = nodo.item(son).getChildNodes().item(f).getChildNodes();
										for(int fs = 0;  fs < nodoOptional.getLength(); fs++){
											if (nodoOptional.item(fs).getNodeType() == Node.ELEMENT_NODE) {
												if(nodoOptional.item(fs).getNodeName().contentEquals("Optional")){
													String name = nodoOptional.item(fs).getAttributes().getNamedItem("name").getNodeValue();
													String type = nodoOptional.item(fs).getAttributes().getNamedItem("type").getNodeValue();
													Optional optional = new Optional(name, type);
													NodeList nodoOptionalInfo = nodoOptional.item(fs).getChildNodes();
													for(int z = 0 ; z < nodoOptionalInfo.getLength(); z++){
														if (nodoOptionalInfo.item(z).getNodeType() == Node.ELEMENT_NODE) {
															String key = nodoOptionalInfo.item(z).getAttributes().getNamedItem("name").getNodeValue();
															String value = nodoOptionalInfo.item(z).getAttributes().getNamedItem("value").getNodeValue();
															optional.setKeyValue(key, value);
														}
													}
													registro.setOptional(optional);
												}
											}
										}
									}
									slave.addRegister(registro);
								}
							}	slaves.add(slave);
						}

					}
				}
				PushEnable = eElement.getElementsByTagName("Push").item(0).getAttributes().getNamedItem("enable").getNodeValue().toLowerCase().contentEquals("true") ? true : false;
				if(PushEnable){
					nodo = eElement.getElementsByTagName("Information");
					message = nodo.item(0).getChildNodes().item(0).getNodeValue();
					for(int son = 0; son < nodo.getLength() ; son++){
						if (nodo.item(son).getNodeType() == Node.ELEMENT_NODE) {
							NamedNodeMap map = nodo.item(son).getAttributes();
							telephone = map.getNamedItem("telephone").getNodeValue();
							mail = map.getNamedItem("mail").getNodeValue();
							idKey = map.getNamedItem("idKey").getNodeValue();
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	//	public void writeConfCounters() {
	//		//creare mapping xml tipo type=EnergyMeter/Station/RadioController/Default/                  scope= in/out/simpleValue/gauge/graphic
	//		//posso passare Gpio Pin Devices Slave Register    Da qui creare due liste di oggetti Pin e Slave
	//		try {
	//			File fXmlFile = new File(fileConfig);
	//			slaveSize = 0;
	//			gpioNumber = 0;
	//			
	//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	//			Document doc = dBuilder.parse(fXmlFile);
	//			doc.getDocumentElement().normalize();
	//			NodeList nList = doc.getElementsByTagName("Server");
	//			//Parto da server padre e senza fratelli
	//			Node nNode = nList.item(0);
	//			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	//				Element eElement = (Element) nNode;
	//				//RECUPERO VERSIONE FILE
	//				NodeList nodo = eElement.getElementsByTagName("Version");
	//				counter = Integer.parseInt(nodo.item(0).getAttributes().item(0).getNodeValue());
	//				counter +=1;
	//				nodo.item(0).getAttributes().item(0).setNodeValue(""+counter);
	//				
	//				
	//				nodo  = eElement.getElementsByTagName("Gpio");
	//			}
	//			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	//			Transformer transformer = transformerFactory.newTransformer();
	//			DOMSource source = new DOMSource(doc);
	//			StreamResult result = new StreamResult(new File(fileConfig));
	//			transformer.transform(source, result);
	//		
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//		
	//	}

	public void writeConf(boolean gpioUsed) {
		//creare mapping xml tipo type=EnergyMeter/Station/RadioController/Default/                  scope= in/out/simpleValue/gauge/graphic
		//posso passare Gpio Pin Devices Slave Register    Da qui creare due liste di oggetti Pin e Slave
		try {
			File fXmlFile = new File(fileConfig);


			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Server");
			//Parto da server padre e senza fratelli
			Node nNode = nList.item(0);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				//RECUPERO VERSIONE FILE
				//				NodeList nodo = eElement.getElementsByTagName("Version");
				//				counter = Integer.parseInt(nodo.item(0).getAttributes().item(0).getNodeValue());
				//				counter +=1;
				//				nodo.item(0).getAttributes().item(0).setNodeValue(""+counter);


				NodeList	nodo  = eElement.getElementsByTagName("Gpio");
				nodo.item(0).getAttributes().getNamedItem("enable").setNodeValue(""+gpioUsed);
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileConfig));
			transformer.transform(source, result);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
//<?xml version="1.0" encoding="UTF-8" standalone="no"?><Server>
//<Version counter="1375" ip="raelixx.ns0.it" psw="enrico" usr="root">
//<Gpio enable="false">
// <Pin alarm="true" delay="1000" log="0" name="Rele" number="14" scope="out" use="true">
// <Optional name="Camera Rele" type="ipcam">
// <Data name="ip" value="192.168.0.10"/>
// <Data name="user" value="192.168.0.10"/>
// <Data name="password" value="192.168.0.10"/>
// </Optional> 
// </Pin>
//  <Pin alarm="true" delay="1000" log="0" name="Acqua" number="0" scope="toggleout" use="true"/>
//  <Pin alarm="false" delay="0" log="0" name="Pippo" number="3" scope="out" use="true"/>
//  <Pin alarm="false" delay="0" log="0" name="Citofono" number="2" scope="out" use="true"/>
//</Gpio>
//<Devices enable="true">
//<Slave address="192.168.0.11" baudrate="9600" id="1" name="GaugeMetro" polling="2000" protocol="tcp" type="EnergyMeter">
//<Register address="40001" alarm="true" highAlarm="1850" log="1" lowAlarm="0" name="Vdc" rw="false" scope="gauge"/>
//<Register address="40004" alarm="false" highAlarm="1" log="0" lowAlarm="0" name="cancelloEsterno" rw="true" scope="out"/>
//<Register address="40005" alarm="false" highAlarm="1" log="0" lowAlarm="0" name="cancelloInterno" rw="true" scope="out"/>
//<Register address="40006" alarm="false" highAlarm="1" log="0" lowAlarm="0" name="corridoio" rw="true" scope="out"/>
//</Slave>
//</Devices>
//<Push enable="true">
//<Information idKey="APA91bHw4Y6RcCjodAKgMltFwTXqIq8l7tWpcPFqJE90ECBjRZlXRnHVBvCdRatgM-JOHd-IG9FCUi-daZbAyiJQq5XiCMywFxvUGLaaeHNyYuUHKUTrJ30_qy0VM9Bok0Rs48k1KRrYgOVQHDu1EUEgaV2LjvFnRqnAFpo4AtvfDtKxecAiPlA" mail="@" telephone="3332233222">Notification: Alert Message!</Information>
//</Push>
//</Version>
//</Server> 
