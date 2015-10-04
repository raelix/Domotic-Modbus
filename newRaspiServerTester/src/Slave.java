

import java.util.LinkedList;
//-<Slave name="GaugeMetro" type="EnergyMeter" polling="2000" baudrate="9600" address="COM2" protocol="rtu" id="1">
public class Slave {
	private String name;
	private String type;
	private int polling;
	private int baudrate;
	private String address;
	private String protocol;
	private int id;
	private LinkedList<Register> registers;


public Slave(){
	this.name = "empty";
	this.type = "empty";
	this.polling = -1;
	this.baudrate = -1;
	this.address = "empty";
	this.protocol = "empty";
	this.id = -1;
	this.registers = new LinkedList<Register>();
}

public Slave(String name,String type,int polling,int baudrate,String address,String protocol,int id){
	this.name = name;
	this.type = type;
	this.polling = polling;
	this.baudrate = baudrate;
	this.address = address;
	this.protocol = protocol;
	this.id = id;
	this.registers = new LinkedList<Register>();
}

public Slave(String name,String type,int polling,int baudrate,String address,String protocol,int id,LinkedList<Register> registers){
	this.name = name;
	this.type = type;
	this.polling = polling;
	this.baudrate = baudrate;
	this.address = address;
	this.protocol = protocol;
	this.id = id;
	this.registers = registers;
}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getPolling() {
		return polling;
	}
	public void setPolling(int polling) {
		this.polling = polling;
	}
	public int getBaudrate() {
		return baudrate;
	}
	public void setBaudrate(int baudrate) {
		this.baudrate = baudrate;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public LinkedList<Register> getRegisters() {
		return registers;
	}

	public Register getRegister(int index) {
		return index > this.registers.size()/*-1*/ ? this.registers.get(index) : null;
	}

	public void setRegisters(LinkedList<Register> registers) {
		this.registers = registers;
	}

	public void setRegisters(int index,Register element) {
		this.registers.set(index, element);
	}

	public void addRegister(Register element){
		this.registers.add(element);
	}
}
