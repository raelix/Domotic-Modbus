package newRaspiServer;
//<Pin log="0" delay="0" name="rele" scope="out" use="true" number="0">0</Pin>
public class Pin {
	private int log;
	private int delay;
	private String name;
	private String scope;
	private boolean use;
	private int number;
	private boolean alarm;
	private Optional optional;
	public Pin(){
		this.log = 0;
		this.delay = 0;
		this.name = "empty";
		this.scope = "empty";
		this.use = false;
		this.number = -1;
		this.optional = null;
	}
	public Pin(int log, int delay, String name,String scope,boolean use,int number,boolean alarm){
		this.log = log;
		this.delay = delay;
		this.name = name;
		this.scope = scope;
		this.use = use;
		this.number = number;
		this.alarm = alarm;
		this.optional = null;
	}
public void setOptional(Optional opt) {
	this.optional = opt;
}
	public Optional getOptional(){
		return this.optional;
	}
	public boolean isAlarm() {
		return alarm;
	}
	public void setAlarm(boolean alarm) {
		this.alarm = alarm;
	}
	public int getLog() {
		return log;
	}
	public void setLog(int log) {
		this.log = log;
	}
	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
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
	public boolean isUse() {
		return use;
	}
	public void setUse(boolean use) {
		this.use = use;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}



}
