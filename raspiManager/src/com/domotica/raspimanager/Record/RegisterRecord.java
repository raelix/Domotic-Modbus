package com.domotica.raspimanager.record;

import com.domotica.raspimanager.shared.Optional;

public class RegisterRecord {
private String name;
private int address;
private String scope;
private float highAlarm;
private float lowAlarm;
private boolean isAlarm;
public Optional optional;
public boolean optionalIsShow ;

	private float value;
	public RegisterRecord(String name,int address,String scope,boolean isAlarm,float highAlarm,float lowAlarm){
		this.name = name;
		this.address = address;
		this.scope = scope;
		this.isAlarm = isAlarm;
		this.highAlarm = highAlarm;
		this.lowAlarm = lowAlarm;
		this.value = 0;
		this.optionalIsShow = false;
}
	public RegisterRecord(String name,int address,String scope,boolean isAlarm,float highAlarm,float lowAlarm,Optional opt){
		this.name = name;
		this.address = address;
		this.scope = scope;
		this.isAlarm = isAlarm;
		this.highAlarm = highAlarm;
		this.lowAlarm = lowAlarm;
		this.value = 0;
		this.optional = opt;
		this.optionalIsShow = false;
}
	public void setOptionalIsShow(boolean isShow){
		this.optionalIsShow = isShow;
	}
	
	public boolean getOptionalIsShow( ){
		return this.optionalIsShow;
	}
	public void setOptional(Optional opt){
		this.optional = opt;
	}
	public Optional getOptional(){
		return optional;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public float getHighAlarm() {
		return highAlarm;
	}

	public void setHighAlarm(float highAlarm) {
		this.highAlarm = highAlarm;
	}

	public boolean isAlarm() {
		return isAlarm;
	}

	public void setAlarm(boolean isAlarm) {
		this.isAlarm = isAlarm;
	}

	public float getLowAlarm() {
		return lowAlarm;
	}

	public void setLowAlarm(float lowAlarm) {
		this.lowAlarm = lowAlarm;
	}
	public void setValue(float value) {
		this.value = value;
	}
	public float getValue() {
		return this.value;
	}
}
