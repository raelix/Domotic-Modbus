package com.domotica.raspimanager.record;

import java.util.LinkedList;

public class SlaveRecord {
private String name;
private String type;
private LinkedList<RegisterRecord> list;
	
	
	public SlaveRecord(String name,String type){
		this.name= name;
		this.type= type;
		list = new LinkedList<RegisterRecord>();
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



	/**
	 * @return the list
	 */
	public LinkedList<RegisterRecord> getList() {
		return this.list;
	}

public void addList(RegisterRecord register){
	this.list.add(register);
}

	/**
	 * @param list the list to set
	 */
	public void setList(LinkedList<RegisterRecord> list) {
		this.list = list;
	}
	
	
}
