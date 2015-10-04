

public class PinRecord {
	public String nome;
	public String numero;
	public String delay;
	public boolean uso;
	public boolean inout;
	public boolean log;
	public boolean alarm;
	public PinRecord(String nome, String numero,String delay,boolean uso, boolean inout,boolean log, boolean alarm) {

	this.nome = nome;
	this.numero = numero;
	this.delay = delay;
	this.uso = uso;
	this.inout = inout;
	this.log = log;
	this.alarm = alarm;
	
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		this.numero = numero;
	}
	public String getDelay() {
		return delay;
	}
	public void setDelay(String delay) {
		this.delay = delay;
	}
	public boolean isUso() {
		return uso;
	}
	public void setUso(boolean uso) {
		this.uso = uso;
	}
	public boolean isInout() {
		return inout;
	}
	public void setInout(boolean inout) {
		this.inout = inout;
	}
	public boolean isLog() {
		return log;
	}
	public void setLog(boolean log) {
		this.log = log;
	}
	public boolean isAlarm() {
		return alarm;
	}
	public void setAlarm(boolean alarm) {
		this.alarm = alarm;
	}
	

}