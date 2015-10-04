package newRaspiServer;

import java.util.LinkedList;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteCoilResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.SerialParameters;

public class SerialSlave {
	static SerialConnection con = null;
	static ModbusSerialTransaction trans = null;
	static ReadMultipleRegistersRequest req = null;
	static ReadMultipleRegistersResponse res = null;
	static LinkedList<Integer> values = new LinkedList<>(); 
	private String portname= ""; 
	int unitid; 
	private int ref = 0; 
	private int count = 0;
	private int baudRate = 9600;
	String encoding = "rtu";
	private int timeout = 1000;
	//	int repeat = 10; 
	private int retries = 1;
	
	public SerialSlave(){
		
	}
	public void setTimeout(int timeout){
		this.timeout = timeout;
	}
	public void setRetries(int retries){
		this.retries = retries;
	}
	public  SerialSlave(String portname,int baudRate,String encoding,int unitid){
		this.portname = portname;
		this.baudRate = baudRate;
		this.encoding = encoding;
		this.unitid = unitid;
		try{
			
			//			con.setReceiveTimeout(5000);//Se riceve info nei ms richiesti non tarda per la successiva richiesta quindi il distacco è 0
			ModbusCoupler.getReference().setUnitID(1);
			//			ModbusCoupler.getReference().setMaster(true);
			SerialParameters params = new SerialParameters();
			params.setPortName(portname);
			params.setBaudRate(baudRate);
			params.setDatabits(8);
			params.setParity("None");
			params.setStopbits(1);
			params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
			params.setEcho(false);//opzione true è buggata
			con = new SerialConnection(params);
		
		}
		catch (Exception ex){
			con.close();
			ex.printStackTrace();
		}

	}
	public  SerialSlave(String portname,int baudRate,String encoding,int unitid,int timeout,int retries){
		this.portname = portname;
		this.baudRate = baudRate;
		this.encoding = encoding;
		this.unitid = unitid;
		try{
			
			//			con.setReceiveTimeout(5000);//Se riceve info nei ms richiesti non tarda per la successiva richiesta quindi il distacco è 0
			ModbusCoupler.getReference().setUnitID(1);
			//			ModbusCoupler.getReference().setMaster(true);
			SerialParameters params = new SerialParameters();
			params.setPortName(portname);
			params.setBaudRate(baudRate);
			params.setDatabits(8);
			params.setParity("None");
			params.setStopbits(1);
			params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
			params.setEcho(false);//opzione true è buggata
			con = new SerialConnection(params);
		this.timeout = timeout;
		this.retries = retries;
		}
		catch (Exception ex){
//			con.close();
			ex.printStackTrace();
		}

	}
	public void connect(){
		try {
			con.open();
		} catch (Exception e) {
			return;
		}
	}
		
	public  LinkedList<Integer> readHoldingRegisters(int ref,int count) throws Exception{
		this.ref = ref;
		try {
			
			req = new ReadMultipleRegistersRequest(ref-1,count);
			req.setUnitID(unitid);
			//			req.setHeadless();
			trans = new ModbusSerialTransaction(con);
			trans.setRequest(req);
			trans.setTransDelayMS(timeout);//ritarda sempre per il tempo specificato
			trans.setRetries(1);//numero di tentativi 
			values = new LinkedList<>(); 
			trans.execute();
			res = (ReadMultipleRegistersResponse) trans.getResponse();
			for (int n = 0; n < res.getWordCount(); n++) {
				values.add(res.getRegisterValue(n));
			}

		} catch (ModbusIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModbusSlaveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModbusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return values;
	}
	 void writeHoldingMultipleRegister(int register,int value)    {
			try {
				if(con!= null)
					if(!con.isOpen())
						connect();
				System.out.println("Writing to id:"+unitid+" register: "+register+" value:"+value+" retries: "+retries+" timeout: "+timeout);
				WriteMultipleRegistersRequest WriteReq = new WriteMultipleRegistersRequest(); 
				WriteMultipleRegistersResponse WriteRes = new WriteMultipleRegistersResponse();
				SimpleRegister MyReg = new SimpleRegister(0);// 0 - default
				WriteReq.setUnitID(unitid); 				//id unità 0 tutti gli id, > 1 iD specifico //in broadcast cioe 0 niente risposta, > 0 risposta valore settato 
				MyReg.setValue(value);					//valore da impostare nel registro scelto Reference - 1
				WriteReq.setReference(register-1);   	//indirizzo registro es(0) scrivo nel registro 1, es(35) scrivo nel registro 36
				WriteReq.setRegisters(new net.wimpi.modbus.procimg.Register[]{MyReg});
				trans = new ModbusSerialTransaction(con);
				trans.setRequest(WriteReq);
				trans.setTransDelayMS(timeout);//ritarda sempre per il tempo specificato
				trans.setRetries(retries);//numero di tentativi 					//con.setTimeout(1000);
				trans.execute();
				WriteRes = (WriteMultipleRegistersResponse) trans.getResponse();
				System.out.println( WriteRes.getWordCount());
				con.close();
			} 
			
			catch (Exception e) {
				System.out.println("error2 unitId: error: "+e.getMessage());
				//			writeHoldingRegister(id+1,register,value);
			}
		}
	 void writeHoldingRegister(int register,int value)    {
		try {
			if(con!= null)
				if(!con.isOpen())
					connect();
			System.out.println("Writing to id:"+unitid+" register: "+register+" value:"+value+" retries: "+retries+" timeout: "+timeout);
			WriteSingleRegisterRequest WriteReq = new WriteSingleRegisterRequest(); 
			WriteSingleRegisterResponse WriteRes = new WriteSingleRegisterResponse();
			SimpleRegister MyReg = new SimpleRegister(0);// 0 - default
			WriteReq.setUnitID(unitid); 				//id unità 0 tutti gli id, > 1 iD specifico //in broadcast cioe 0 niente risposta, > 0 risposta valore settato 
			MyReg.setValue(value);					//valore da impostare nel registro scelto Reference - 1
			WriteReq.setReference(register-1);   	//indirizzo registro es(0) scrivo nel registro 1, es(35) scrivo nel registro 36
			WriteReq.setRegister(MyReg);
			trans = new ModbusSerialTransaction(con);
			trans.setRequest(WriteReq);
			trans.setTransDelayMS(timeout);//ritarda sempre per il tempo specificato
			trans.setRetries(retries);//numero di tentativi 					//con.setTimeout(1000);
			trans.execute();
//			WriteRes = (WriteSingleRegisterResponse) trans.getResponse();
//			System.out.println( WriteRes.getRegisterValue());
			con.close();
		} 
		
		catch (Exception e) {
			System.out.println("error2 unitId: error: "+e.getMessage());
			//			writeHoldingRegister(id+1,register,value);
		}
	}
	 void writeCoilRegister(int register,int value)    {
			try {
				if(con!= null)
					if(!con.isOpen())
						connect();
				System.out.println("Writing to id:"+unitid+" register: "+register+" value:"+value+" retries: "+retries+" timeout: "+timeout);
				WriteCoilRequest WriteReq = new WriteCoilRequest(); 
				WriteCoilResponse WriteRes = new WriteCoilResponse();
				SimpleRegister MyReg = new SimpleRegister(0);// 0 - default
				WriteReq.setUnitID(unitid); 				//id unità 0 tutti gli id, > 1 iD specifico //in broadcast cioe 0 niente risposta, > 0 risposta valore settato 
				MyReg.setValue(value);					//valore da impostare nel registro scelto Reference - 1
				WriteReq.setReference(register-1);   	//indirizzo registro es(0) scrivo nel registro 1, es(35) scrivo nel registro 36
//				WriteReq.setRegister(MyReg);
				trans = new ModbusSerialTransaction(con);
				trans.setRequest(WriteReq);
				trans.setTransDelayMS(timeout);//ritarda sempre per il tempo specificato
				trans.setRetries(retries);//numero di tentativi 					//con.setTimeout(1000);
				trans.execute();
				WriteRes = (WriteCoilResponse) trans.getResponse();
				System.out.println( WriteRes.getReference());
				con.close();
			} 
			
			catch (Exception e) {
				System.out.println("error2 unitId: error: "+e.getMessage());
				//			writeHoldingRegister(id+1,register,value);
			}
		}
	public  int readHoldingRegister(int ref){
		this.ref = ref;
		int value= 0;
		
			req = new ReadMultipleRegistersRequest(ref-1,1);
			req.setUnitID(unitid);
			//			req.setHeadless();
			trans = new ModbusSerialTransaction(con);
			trans.setRequest(req);
			trans.setTransDelayMS(timeout);//ritarda sempre per il tempo specificato
			trans.setRetries(retries);//numero di tentativi 
		
			try {
				trans.execute();
			} catch (Exception e) {
//				e.printStackTrace();
				System.err.println("Serial device not reply after "+timeout+" Ms"+"\n");
				return -1;
			}
			res = (ReadMultipleRegistersResponse) trans.getResponse();
			if(res != null)
			for (int n = 0; n < res.getWordCount(); n++) {
				values.add(res.getRegisterValue(n));
				value = res.getRegisterValue(n);
			}
		return value;
	}
	
	
	public void close(){
		con.close();
	}


	public static SerialConnection getCon() {
		return con;
	}


	public static void setCon(SerialConnection con) {
		SerialSlave.con = con;
	}


	public static ModbusSerialTransaction getTrans() {
		return trans;
	}


	public static void setTrans(ModbusSerialTransaction trans) {
		SerialSlave.trans = trans;
	}


	public static ReadMultipleRegistersRequest getReq() {
		return req;
	}


	public static void setReq(ReadMultipleRegistersRequest req) {
		SerialSlave.req = req;
	}


	public static ReadMultipleRegistersResponse getRes() {
		return res;
	}


	public static void setRes(ReadMultipleRegistersResponse res) {
		SerialSlave.res = res;
	}


	public static LinkedList<Integer> getValues() {
		return values;
	}


	public static void setValues(LinkedList<Integer> values) {
		SerialSlave.values = values;
	}


	public String getPortname() {
		return portname;
	}


	public void setPortname(String portname) {
		this.portname = portname;
	}


	public int getUnitid() {
		return unitid;
	}


	public void setUnitid(int unitid) {
		this.unitid = unitid;
	}


	public int getRef() {
		return ref;
	}


	public void setRef(int ref) {
		this.ref = ref;
	}


	public int getCount() {
		return count;
	}


	public void setCount(int count) {
		this.count = count;
	}


	public int getBaudRate() {
		return baudRate;
	}


	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}


	public String getEncoding() {
		return encoding;
	}


	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}


}