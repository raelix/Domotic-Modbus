package newRaspiServer;

import java.net.InetAddress;
import java.util.LinkedList;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;


public class HoldingRegisterTCPMaster {
	static LinkedList<Integer> readHoldingRegisters(String ip,int id,int register,int count,int retries)   {
		LinkedList<Integer> values = new LinkedList<>();
		try {
			TCPMasterConnection con = null;
			InetAddress addr = InetAddress.getByName(ip);
			con = new TCPMasterConnection(addr);
			con.setPort(502);
			con.connect(); 							//con.setTimeout(1000);
			ReadMultipleRegistersRequest req = null; //the request
			ReadMultipleRegistersResponse res = null; //the response
			req = new ReadMultipleRegistersRequest(register-1, count);//es (3,1) = Register 0004 , 1 length
			req.setUnitID(id);
			ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
			trans.setRequest(req);
			trans.setRetries(retries);
			trans.setReconnecting(false);
			trans.execute();
			res = (ReadMultipleRegistersResponse) trans.getResponse();
			for (int n = 0; n < res.getWordCount(); n++) {
				values.add(res.getRegisterValue(n));
			}
			con.close();
		} catch (Exception e) {
			System.err.println("Cannot Connect with device ip:"+ip);
			return values;
		}

		return values;
	}

	static int readHoldingRegister(String ip,int id,int register,int retries)   {
		LinkedList<Integer> values = new LinkedList<>();
		int value = -1;
		try {
			TCPMasterConnection con = null;
			InetAddress addr = InetAddress.getByName(ip);
			con = new TCPMasterConnection(addr);
			con.setPort(502);	
			con.connect(); 							//con.setTimeout(1000);
			ReadMultipleRegistersRequest req = null; //the request
			ReadMultipleRegistersResponse res = null; //the response
			req = new ReadMultipleRegistersRequest(register-1, 1);//es (3,1) = Register 0004 , 1 length
			req.setUnitID(id);
			ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
			trans.setRequest(req);
			//			trans.setRetries( retries);
			trans.setReconnecting(false);
			trans.execute();
			res = (ReadMultipleRegistersResponse) trans.getResponse();
			for (int n = 0; n < res.getWordCount(); n++) {
				values.add(res.getRegisterValue(n));
				value = res.getRegisterValue(n);
			}
			con.close();
		} catch (Exception e) {
			Debug.print("Slave "+ip+" did not reply after 3000 millis");
			return value;
		}

		return value;
	}

	static void writeHoldingRegister(String ip,int id,int register,int value)    {
		try {
			TCPMasterConnection con = null;
			WriteSingleRegisterRequest WriteReq = new WriteSingleRegisterRequest(); 
			WriteSingleRegisterResponse WriteRes = new WriteSingleRegisterResponse();
			SimpleRegister MyReg = new SimpleRegister(0);// 0 - default
			InetAddress addr = InetAddress.getByName(ip);
			con = new TCPMasterConnection(addr);
			con.setPort(502);	
			con.connect(); 							//con.setTimeout(1000);
			WriteReq.setUnitID(id); 				//id unità 0 tutti gli id, > 1 iD specifico //in broadcast cioe 0 niente risposta, > 0 risposta valore settato 
			MyReg.setValue(value);					//valore da impostare nel registro scelto Reference - 1
			WriteReq.setReference(register-1);   	//indirizzo registro es(0) scrivo nel registro 1, es(35) scrivo nel registro 36
			WriteReq.setRegister(MyReg);
			ModbusTCPTransaction trans = new ModbusTCPTransaction(con);
			trans.setRequest(WriteReq);
			trans.setReconnecting(true);
			trans.setRetries(2);
			trans.execute();
			WriteRes.setUnitID(id);
			WriteRes = (WriteSingleRegisterResponse) trans.getResponse();
			System.out.println( WriteRes.getRegisterValue());
			con.close();
		}
		catch (Exception e) {
			System.out.println("error2 unitId:"+id+" error: "+e.getMessage());
			//			writeHoldingRegister(id+1,register,value);
		}
	}
}//class SerialAITest
