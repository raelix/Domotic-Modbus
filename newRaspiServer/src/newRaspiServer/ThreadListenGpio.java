package newRaspiServer;


import java.io.IOException;

import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;




	public class ThreadListenGpio extends Thread implements PROTOCOL {
		private GpioPinDigitalInput input;
		private SimpleProcessImage spi;
		private int position;
		private  GpioPinListenerDigital listener ;
		private boolean alarm;
		private String name;
		private Pin pin;
		private String scope;
		public ThreadListenGpio (GpioPinDigitalInput input, SimpleProcessImage spi, int position) {
			super();
			this.spi = spi;
			this.input = input;
			this.position = position;
			
		};
		
		public ThreadListenGpio (GpioPinDigitalInput input, SimpleProcessImage spi, int position,Pin pin) {
			super();
			this.spi = spi;
			this.input = input;
			this.position = position;
			this.pin = pin;
			if(pin.isAlarm())
			this.alarm  = true;
			else this.alarm = false;
			this.name = pin.getName();
			this.scope = pin.getScope();
		};

		@Override
		public void run () {
listener = new GpioPinListenerDigital() {
				
				@Override
				public void handleGpioPinDigitalStateChangeEvent(
						GpioPinDigitalStateChangeEvent event) {
					System.out.println("Il sensore ha cambiato stato : " + event.getPin() + " = " + event.getState());
					spi.setRegister(position+1, new SimpleRegister(IN_ALARM));
					if(alarm){
						
						Message message = new Message.Builder()
					    .addData("message", name+" ha cambiato stato")
					    .addData("other-parameter", "some value")
					    .build();
					try {
						Result result = MainServer.sender.send(message, new Configuratore().idKey, 3);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					}
					
				}
			};
			input.addListener(listener);

			/* keep program running until user aborts (CTRL-C)
			for (;;) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.out.println("Thread sleep pause error");
				}
			}*/

		};

	public  void removeListener(){
		input.removeListener(listener);
		input.removeAllListeners();
		input.removeAllTriggers();
		
	}

}
