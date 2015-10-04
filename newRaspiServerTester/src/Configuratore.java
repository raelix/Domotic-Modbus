

import java.io.File;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
		public  static String fileConfig = "C:/Users/gianmarco/Desktop/configurationNewRaspiServer.xml";
//	public  static String fileConfig = "/home/pi/Desktop/configurationNewRaspiServer.xml";

	public Configuratore(){
		if(new File(fileConfig).exists()){
			readFromFile();
		}
		//		else
		//			createFile();

	}
	
	
	public void readFromFile() {
		//creare mapping xml tipo type=EnergyMeter/Station/RadioController/Default/                  scope= in/out/simpleValue/gauge/graphic
		//posso passare Gpio Pin Devices Slave Register    Da qui creare due liste di oggetti Pin e Slave
		try {
			File fXmlFile = new File(fileConfig);
			slaveSize = 0;
			gpioNumber = 0;
			slaves = new LinkedList<>();
			pins = new LinkedList<>();
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
				NodeList nodo = eElement.getElementsByTagName("Version");
				counter = Integer.parseInt(nodo.item(0).getAttributes().item(0).getNodeValue());
				//RECUPERO STATO GPIO
				nodo = eElement.getElementsByTagName("Gpio");
				GpioEnable = nodo.item(0).getAttributes().item(0).getNodeValue().toLowerCase().contentEquals("true") ? true : false;
				nodo = eElement.getElementsByTagName("Pin");
//				int lunghezza =  eElement.getElementsByTagName("Pin").getLength();
				for(int i = 0; i<  nodo.getLength(); i++){
					//					System.out.println(nodo.item(i).getNodeName());
					if (nodo.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Pin pin = new Pin();
						for(int k = 0; k < nodo.item(i).getAttributes().getLength(); k++){
							//							if(debug){
							//							System.out.println(nodo.item(i).getAttributes().item(k).getNodeName());
							//							System.out.println(nodo.item(i).getAttributes().item(k).getNodeValue());}
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

							//							System.out.println("qui"+nodo.getLength());
							Slave slave = new Slave();
							slaveSize++;
							slave.setRegisters(new LinkedList<Register>());
							NamedNodeMap map = nodo.item(son).getAttributes();
							slave.setId(Integer.parseInt(map.getNamedItem("id").getNodeValue()));
							slave.setProtocol(map.getNamedItem("protocol").getNodeValue());
							slave.setAddress(map.getNamedItem("address").getNodeValue());
							slave.setBaudrate(Integer.parseInt(map.getNamedItem("baudrate").getNodeValue()));
							slave.setPolling(Integer.parseInt(map.getNamedItem("polling").getNodeValue()));
							slave.setType(map.getNamedItem("type").getNodeValue());
							slave.setName(map.getNamedItem("name").getNodeValue());
							
							//<Register address="40001" name="Idc" scope="SimpleValue" log="" rw="false"/>
							for(int f = 0; f < nodo.item(son).getChildNodes().getLength() ; f++ ){
								if (nodo.item(son).getChildNodes().item(f).getNodeType() == Node.ELEMENT_NODE) {
									map =nodo.item(son).getChildNodes().item(f).getAttributes();
									Register registro = new Register(Integer.parseInt(map.getNamedItem("log").getNodeValue()), map.getNamedItem("name").getNodeValue(),
											map.getNamedItem("scope").getNodeValue(), Integer.parseInt(map.getNamedItem("address").getNodeValue()),
											map.getNamedItem("rw").getNodeValue().toLowerCase().contentEquals("true")? true : false,map.getNamedItem("alarm").getNodeValue().toLowerCase().contentEquals("true")? true : false, Float.parseFloat(map.getNamedItem("lowAlarm").getNodeValue()),Float.parseFloat(map.getNamedItem("highAlarm").getNodeValue()));
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




}
