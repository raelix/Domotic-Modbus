package com.domotica.raspimanager.shared;

import java.net.InetAddress;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;
import android.os.AsyncTask;

public class writeHR extends AsyncTask<Integer, Void, Void> {
	
	@Override
	protected Void doInBackground(Integer... params) {
		try {
			TCPMasterConnection con = null;
			WriteSingleRegisterRequest WriteReq = new WriteSingleRegisterRequest(); 
			WriteSingleRegisterResponse WriteRes = new WriteSingleRegisterResponse();
			SimpleRegister MyReg = new SimpleRegister(0);// 0 - default
			InetAddress addr = InetAddress.getByName("127.0.0.1");
			con = new TCPMasterConnection(addr);
			con.setPort(9001);	

			con.connect(); 							//con.setTimeout(1000);
			WriteReq.setUnitID(1); 				//id unità 0 tutti gli id, > 1 iD specifico //in broadcast cioe 0 niente risposta, > 0 risposta valore settato 
			MyReg.setValue(params[1]);					//valore da impostare nel registro scelto Reference - 1
			WriteReq.setReference(params[0]);   	//indirizzo registro es(0) scrivo nel registro 1, es(35) scrivo nel registro 36
			WriteReq.setRegister(MyReg);
			ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
			trans.setRequest(WriteReq);
			trans.setReconnecting(true);
			trans.setRetries(1);
			trans.execute();
			WriteRes.setUnitID(1);
			WriteRes = (WriteSingleRegisterResponse) trans.getResponse();
//			System.out.println( "Write value"+WriteRes.getRegisterValue());
			con.close();
		} 
		
		catch (Exception e) {
			System.out.println("error write "+e.getMessage());
			//			writeHoldingRegister(id+1,register,value);
		}
		return null;
}
}