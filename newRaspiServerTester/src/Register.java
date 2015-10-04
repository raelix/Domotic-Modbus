
//<Register log="" name="Idc" scope="SimpleValue" address="40001" rw="false">40001</Register>
public class Register {
	private int log;
	private String name;
	private String scope;
	private int address;
	private boolean rw;
	private boolean alarm;
	private float lowAlarm;
	private float highAlarm;


	public Register(int log, String name,String scope, int address, boolean rw, boolean alarm, float lowAlarm, float highAlarm){
		this.log = log;
		this.name = name;
		this.scope = scope;
		this.address = address;
		this.rw = rw;
		this.alarm = alarm;
		this.lowAlarm = lowAlarm;
		this.highAlarm = highAlarm;
	}

	public boolean isAlarm() {
		return alarm;
	}

	public void setAlarm(boolean alarm) {
		this.alarm = alarm;
	}

	public float getLowAlarm() {
		return lowAlarm;
	}

	public void setLowAlarm(float lowAlarm) {
		this.lowAlarm = lowAlarm;
	}

	public float getHighAlarm() {
		return highAlarm;
	}

	public void setHighAlarm(float highAlarm) {
		this.highAlarm = highAlarm;
	}

	public int getLog() {
		return log;
	}
	public void setLog(int log) {
		this.log = log;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public int getAddress() {
		return this.address;
	}
	public void setAddress(int address) {
		this.address = address;
	}

	public boolean isRw() {
		return rw;
	}

	public void setRw(boolean rw) {
		this.rw = rw;
	}




}
