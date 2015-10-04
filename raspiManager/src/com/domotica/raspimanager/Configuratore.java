package com.domotica.raspimanager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;

import com.domotica.raspimanager.record.PinRecord;
import com.domotica.raspimanager.shared.Optional;
import com.domotica.raspimanager.shared.Pin;
import com.domotica.raspimanager.shared.Register;
import com.domotica.raspimanager.shared.Slave;

@SuppressLint("DefaultLocale")
public class Configuratore {
	public  int counter;
	public  boolean GpioEnable;
	public  boolean DevicesEnable;
	public  boolean PushEnable;
	public static String nameFile = "configurationNewRaspiServer.xml";
	public LinkedList<Pin> pinsUsed = new LinkedList<Pin>();
	public LinkedList<Pin> pins = new LinkedList<Pin>();
	public LinkedList<Slave> slaves = new LinkedList<Slave>();
	public  int slaveSize = 0;
	public  int gpioNumber;
	public 	String telephone;
	public 	String idKey;
	public 	String mail;
	public 	String message;
//	public String ip;
//	public String usr;
//	public String psw;
	public  static String fileConfig ;


	public Configuratore(String fileConfig){
		Configuratore.fileConfig = fileConfig;
		if(new File(fileConfig).exists()){
			readFromFile();
		}
		//				else
		//		//			createFile();
		//		try {
		//			Runtime.getRuntime().exec("raelixx.ns0.it:81/raspiConfiguration.xml");
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	}

	public boolean exist(){
		return new File(fileConfig+"/configurationNewRaspiServer.xml").exists();
	}
	public boolean exist(String fileConfig){
		return new File(fileConfig+"/configurationNewRaspiServer.xml").exists();
	}

	public boolean delete(){
		return new File(fileConfig).delete();
	}

	public void writeIdKey(String filepath,String idKey) {
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
				NodeList nodo = eElement.getElementsByTagName("Version");
				counter = Integer.parseInt(nodo.item(0).getAttributes().item(0).getNodeValue());
				counter +=1;
				nodo.item(0).getAttributes().item(0).setNodeValue(""+counter);
				PushEnable = eElement.getElementsByTagName("Push").item(0).getAttributes().getNamedItem("enable").getNodeValue().toLowerCase().contentEquals("true") ? true : false;
				if(PushEnable){
					nodo = eElement.getElementsByTagName("Information");
					message = nodo.item(0).getChildNodes().item(0).getNodeValue();
					for(int son = 0; son < nodo.getLength() ; son++){
						if (nodo.item(son).getNodeType() == Node.ELEMENT_NODE) {
							NamedNodeMap map = nodo.item(son).getAttributes();
							map.getNamedItem("idKey").setNodeValue(idKey);
							//							idKey = map.getNamedItem("idKey").getNodeValue();
						}
					}
				}
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



	public void writeConf(boolean gpioUsed,boolean deviceUsed) {
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
				NodeList nodo = eElement.getElementsByTagName("Version");
				counter = Integer.parseInt(nodo.item(0).getAttributes().item(0).getNodeValue());
				counter +=1;
				nodo.item(0).getAttributes().item(0).setNodeValue(""+counter);


				nodo  = eElement.getElementsByTagName("Gpio");
				nodo.item(0).getAttributes().getNamedItem("enable").setNodeValue(""+gpioUsed);
				nodo  = eElement.getElementsByTagName("Devices");
				nodo.item(0).getAttributes().getNamedItem("enable").setNodeValue(""+deviceUsed);
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

	public void writePin(ArrayList<PinRecord> pinEdited) {
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
				NodeList nodo = eElement.getElementsByTagName("Version");
				counter = Integer.parseInt(nodo.item(0).getAttributes().item(0).getNodeValue());
				counter +=1;
				nodo.item(0).getAttributes().item(0).setNodeValue(""+counter);

				Node node = eElement.getElementsByTagName("Gpio").item(0);
				while (node.hasChildNodes())
					node.removeChild(node.getFirstChild());

				//					  <Pin alarm="true"    >0</Pin>
				for(int i = 0 ; i < pinEdited.size();i++){
					Element pin = doc.createElement("Pin");
					pin.setAttribute("name", pinEdited.get(i).getNome());
					pin.setAttribute("number", pinEdited.get(i).getNumero());
					pin.setAttribute("use", ""+pinEdited.get(i).isUso());
					pin.setAttribute("scope", pinEdited.get(i).isInout() ? "in" : "out");
					pin.setAttribute("log", pinEdited.get(i).isLog() ? "1" : "0");
					pin.setAttribute("alarm", ""+pinEdited.get(i).isAlarm());
					System.out.println("delay: "+pinEdited.get(i).getDelay());
					pin.setAttribute("delay", ""+pinEdited.get(i).getDelay());
					node.appendChild(pin);
				}
				//					 node.appendChild()
				//					message = nodo.item(0).getChildNodes().item(0).getNodeValue();
				//					for(int son = 0; son < nodo.getLength() ; son++){
				//						if (nodo.item(son).getNodeType() == Node.ELEMENT_NODE) {
				//							NamedNodeMap map = nodo.item(son).getAttributes();
				//							 map.getNamedItem("idKey").setNodeValue(idKey);
				////							idKey = map.getNamedItem("idKey").getNodeValue();
				//						
				//					}
				//				}
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileConfig));
			transformer.transform(source, result);
			pinEdited.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}



	@SuppressLint("DefaultLocale")
	public void readFromFile() {
		//creare mapping xml tipo type=EnergyMeter/Station/RadioController/Default/                  scope= in/out/simpleValue/gauge/graphic
		//posso passare Gpio Pin Devices Slave Register    Da qui creare due liste di oggetti Pin e Slave
		try {
			File fXmlFile = new File(fileConfig);
			slaveSize = 0;
			gpioNumber = 0;
			slaves = new LinkedList<Slave>();
			pins = new LinkedList<Pin>();
//			pinsUsed	= new LinkedList<Pin>();
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
//				ip  = nodo.item(0).getAttributes().getNamedItem("ip").getNodeValue();
//				usr = nodo.item(0).getAttributes().getNamedItem("usr").getNodeValue();
//				psw = nodo.item(0).getAttributes().getNamedItem("psw").getNodeValue();
				//RECUPERO STATO GPIO
				nodo = eElement.getElementsByTagName("Gpio");
				GpioEnable = nodo.item(0).getAttributes().item(0).getNodeValue().toLowerCase().contentEquals("true") ? true : false;
				nodo = eElement.getElementsByTagName("Pin");
				for(int i = 0; i<  nodo.getLength(); i++){
					if (nodo.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Pin pin = new Pin();
						for(int k = 0; k < nodo.item(i).getAttributes().getLength(); k++){
							if(nodo.item(i).getAttributes().item(k).getNodeName().toLowerCase().contains("delay")){
								pin.setDelay(Integer.parseInt(nodo.item(i).getAttributes().item(k).getNodeValue()));
							}
							if(nodo.item(i).getAttributes().item(k).getNodeName().toLowerCase().contains("log")){
								pin.setLog(Integer.parseInt(nodo.item(i).getAttributes().item(k).getNodeValue()));
							}
							if(nodo.item(i).getAttributes().item(k).getNodeName().toLowerCase().contains("name")){
								pin.setName(nodo.item(i).getAttributes().item(k).getNodeValue());
							}
							if(nodo.item(i).getAttributes().item(k).getNodeName().toLowerCase().contains("number")){
								pin.setNumber(Integer.parseInt(nodo.item(i).getAttributes().item(k).getNodeValue()));
							}
							if(nodo.item(i).getAttributes().item(k).getNodeName().toLowerCase().contains("scope")){
								pin.setScope(nodo.item(i).getAttributes().item(k).getNodeValue());
							}
							if(nodo.item(i).getAttributes().item(k).getNodeName().toLowerCase().contains("alarm")){
								pin.setAlarm(nodo.item(i).getAttributes().item(k).getNodeValue().toLowerCase().contentEquals("true") ? true : false);
							}
							if(nodo.item(i).getAttributes().item(k).getNodeName().toLowerCase().contains("use")){
								pin.setUse(nodo.item(i).getAttributes().item(k).getNodeValue().toLowerCase().contentEquals("true") ? true : false);
								if(pin.isUse())
									gpioNumber+=1;
							}

						}
						NodeList nodoOptional = nodo.item(i).getChildNodes();
						for(int f = 0;  f < nodoOptional.getLength(); f++){
							if (nodoOptional.item(f).getNodeType() == Node.ELEMENT_NODE) {
								if(nodoOptional.item(f).getNodeName().contentEquals("Optional")){
									String name = nodoOptional.item(f).getAttributes().getNamedItem("name").getNodeValue();
									String type = nodoOptional.item(f).getAttributes().getNamedItem("type").getNodeValue();
									com.domotica.raspimanager.shared.Optional optional = new Optional(name, type);
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
							slave.setAddress(map.getNamedItem("address").getNodeValue());
							slave.setBaudrate(Integer.parseInt(map.getNamedItem("baudrate").getNodeValue()));
							slave.setTimeout(Integer.parseInt(map.getNamedItem("timeout").getNodeValue()));
							slave.setRetries(Integer.parseInt(map.getNamedItem("retries").getNodeValue()));
							slave.setPolling(Integer.parseInt(map.getNamedItem("polling").getNodeValue()));
							slave.setType(map.getNamedItem("type").getNodeValue());
							slave.setName(map.getNamedItem("name").getNodeValue());
							for(int f = 0; f < nodo.item(son).getChildNodes().getLength() ; f++ ){
								if (nodo.item(son).getChildNodes().item(f).getNodeType() == Node.ELEMENT_NODE) {
									map =nodo.item(son).getChildNodes().item(f).getAttributes();
									Register registro = new Register(Integer.parseInt(map.getNamedItem("log").getNodeValue()), map.getNamedItem("name").getNodeValue(),
											map.getNamedItem("scope").getNodeValue(), Integer.parseInt(map.getNamedItem("address").getNodeValue()),
											map.getNamedItem("rw").getNodeValue().toLowerCase().contentEquals("true")? true : false,
													map.getNamedItem("alarm").getNodeValue().toLowerCase().contentEquals("true")? true : false,
															Float.parseFloat(map.getNamedItem("lowAlarm").getNodeValue()),
															Float.parseFloat(map.getNamedItem("highAlarm").getNodeValue()),
															Float.parseFloat(map.getNamedItem("multiplies").getNodeValue()));
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
}
