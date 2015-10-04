/***
 * Copyright 2002-2010 jamod development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***/

package net.wimpi.modbus.net;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.io.ModbusTransport;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.msg.WriteSingleRegisterResponse;

/**
 * Class implementing a handler for incoming Modbus/TCP requests.
 *
 * @author Dieter Wimberger
 * @version @version@ (@date@)
 */
public class TCPConnectionHandler implements Runnable {
	private PropertyChangeSupport propertyChangeSupport;
  private TCPSlaveConnection m_Connection;
  private ModbusTransport m_Transport;

  /**
   * Constructs a new <tt>TCPConnectionHandler</tt> instance.
   *
   * @param con an incoming connection.
   */
  public TCPConnectionHandler(TCPSlaveConnection con) {
	  propertyChangeSupport = new PropertyChangeSupport(this);
    setConnection(con);
  }//constructor

  public TCPConnectionHandler() {
	  propertyChangeSupport = new PropertyChangeSupport(this);
    
  }
  
  public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
  
  /**
   * Sets a connection to be handled by this <tt>
   * TCPConnectionHandler</tt>.
   *
   * @param con a <tt>TCPSlaveConnection</tt>.
   */
  public void setConnection(TCPSlaveConnection con) {
    m_Connection = con;
//    propertyChangeSupport = new PropertyChangeSupport(this);
    m_Transport = m_Connection.getModbusTransport();
  }//setConnection
  public TCPConnectionHandler setConnections(TCPSlaveConnection con) {
	    m_Connection = con;
//	    propertyChangeSupport = new PropertyChangeSupport(this);
	    m_Transport = m_Connection.getModbusTransport();
	    return this;
	  }//setConnection

  public void run() {
    try {
      do {
        //1. read the request
        ModbusRequest request = m_Transport.readRequest();
        //System.out.println("Request:" + request.getHexMessage());
        ModbusResponse response = null;

        //test if Process image exists
        if (ModbusCoupler.getReference().getProcessImage() == null) {
          response =
              request.createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
        } else {
          response = request.createResponse();
        }
        switch (request.getFunctionCode()) {
		case Modbus.WRITE_MULTIPLE_REGISTERS:
////			System.out.println("WRITE_MULTIPLE_REGISTERS");
			propertyChangeSupport.firePropertyChange("Property",((WriteSingleRegisterResponse) request.createResponse()).getReference(), ((WriteSingleRegisterResponse) request.createResponse()).getRegisterValue());
			break;
		case Modbus.WRITE_SINGLE_REGISTER:
////			System.out.println("WRITE_SINGLE_REGISTER");
			propertyChangeSupport.firePropertyChange("Property",((WriteSingleRegisterResponse) request.createResponse()).getReference(), ((WriteSingleRegisterResponse) request.createResponse()).getRegisterValue());
			break;
		case Modbus.WRITE_COIL:
////			System.out.println("WRITE_COIL");
			propertyChangeSupport.firePropertyChange("Property",((WriteSingleRegisterResponse) request.createResponse()).getReference(), ((WriteSingleRegisterResponse) request.createResponse()).getRegisterValue());
			break;
		case Modbus.WRITE_MULTIPLE_COILS:
////			System.out.println("WRITE_MULTIPLE_COILS");
			propertyChangeSupport.firePropertyChange("Property",((WriteSingleRegisterResponse) request.createResponse()).getReference(), ((WriteSingleRegisterResponse) request.createResponse()).getRegisterValue());
			break;
		default:
			break;
		}
        /*DEBUG*/
        if (Modbus.debug) System.out.println("Request:" + request.getHexMessage());
        if (Modbus.debug) System.out.println("Response:" + response.getHexMessage());

        //System.out.println("Response:" + response.getHexMessage());
        m_Transport.writeMessage(response);
      } while (true);
    } catch (ModbusIOException ex) {
      if (!ex.isEOF()) {
        //other troubles, output for debug
        ex.printStackTrace();
      }
    } finally {
      try {
        m_Connection.close();
      } catch (Exception ex) {
        //ignore
      }

    }
  }//run

}//TCPConnectionHandler

