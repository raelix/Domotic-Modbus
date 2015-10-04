package com.domotica.raspimanager;

import java.util.Timer;

import com.domotica.raspimanager.fragments.FragmentDevices;
import com.domotica.raspimanager.fragments.FragmentGpio;





public class MonitorResource {
	private static MonitorResource instance;
	private   Timer timerGpio;
	private Timer timerDevices;
	private  FragmentGpio.RequestTimer requestTimerGpio;
	private  FragmentDevices.RequestTimer requestTimerDevices;
	private FragmentDevices devices;
	private FragmentGpio gpio;
	private Monitor monitor;
	
	private MonitorResource(){
		timerGpio = null;
		timerDevices = null;
		requestTimerGpio = null;
		requestTimerDevices = null;
		devices = null;
		gpio = null;
		monitor = null;
	}

	public static synchronized MonitorResource getInstance(){
		if(instance == null){
			instance = new MonitorResource();
		}
		return instance;
	}
	
	/**
	 * @param devicesFragment 
	 * @return setMonitor of Devices
	 */ 
	public  void setResourcesDevices(Timer timer, FragmentDevices.RequestTimer requestTimer, FragmentDevices devicesFragment){
		setDevices(devicesFragment);
		setTimerGpio(timer);
		setRequestTimerDevices(requestTimer);
		if(isTimerReady())startMonitor();
	}

	
	/**
	 * @return setMonitor of Gpio
	 */
	public  void setResourcesGpio(Timer timer, FragmentGpio.RequestTimer requestTimer, FragmentGpio gpioFragment) {
		setGpio(gpioFragment);
		setTimerDevices(timer);
		setRequestTimerGpio(requestTimer);
		if(isTimerReady())startMonitor();
	}
	public void blockServerEmpty(boolean bool){
		if(monitor != null)
			monitor.serverEmpty = bool;
//		else {
//			monitor.lock = true;
//			monitor = null;
//			startMonitor();
//			monitor.serverEmpty = bool;
//		}
	}
	
	public boolean getBlockServerEmpty(){
		if(monitor != null)return monitor.serverEmpty;
		else return true;
	}
	
	public boolean isReady(){
		if(isTimerReady())return true;
		else return false;
	}
	
	/**
	 * @return startMonitor!
	 */
	
	public void stopMonitor(){
		if(monitor != null && monitor.isAlive()){
			monitor.lock = true;
			monitor = null;
		}
	}
	
	public void startMonitor() {
		if(monitor == null){
			monitor = new Monitor();
			monitor.start();
		}
		else if(monitor != null && monitor.isAlive()){
			monitor.lock = true;
			monitor = new Monitor();
			monitor.start();
			System.out.println("avvio nuovo monitor al posto del vecchio");
		}
	}

	public boolean isRunning(){
		if(monitor != null)return monitor.isAlive();
		else return false;
	}
	
	public class Monitor extends Thread{
		private boolean lock = false;
		public boolean serverEmpty = false;
		@Override
		public void run(){
			super.run();
			while(true && !lock){
				Thread.currentThread().setName("MonitorThread");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(serverEmpty){
					System.out.println("In waiting....");
//					if(requestTimerGpio != null)requestTimerGpio.interrupt();
//					if(requestTimerDevices != null)requestTimerDevices.interrupt();
					continue;
				}
				requestTimerGpio =	gpio.getRequestThread();
				
				requestTimerGpio.start();
//				if(requestTimerGpio != null)
//				synchronized (requestTimerGpio) {
//					try {
//						requestTimerGpio.wait();
//					} catch (InterruptedException e) {
//						System.err.println("Timer: cannot wait");
//						e.printStackTrace();
//					}
//				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				requestTimerDevices = devices.getRequestThread();
				requestTimerDevices.start();
//				if(requestTimerDevices != null)
//				synchronized (requestTimerDevices) {
//					try {
//						requestTimerDevices.wait();
//						System.out.println("Timer: end TimerDevices ");
//					} catch (InterruptedException e) {
//						System.err.println("Timer: cannot wait");
//						e.printStackTrace();
//					}
//				}
			}
		}
	}
	
	
	/**
	 * @return true if all things are configurated
	 */
	public boolean isTimerReady() {
		return timerGpio != null &&
				timerDevices != null &&
				requestTimerDevices != null &&
				requestTimerGpio != null;
	}

	/**
	 * @return the timerGpio
	 */
	public Timer getTimerGpio() {
		return timerGpio;
	}

	/**
	 * @param timerGpio the timerGpio to set
	 */
	public void setTimerGpio(Timer timerGpio) {
		this.timerGpio = timerGpio;
	}

	/**
	 * @return the timerDevices
	 */
	public Timer getTimerDevices() {
		return timerDevices;
	}

	/**
	 * @param timerDevices the timerDevices to set
	 */
	public void setTimerDevices(Timer timerDevices) {
		this.timerDevices = timerDevices;
	}

	/**
	 * @return the requestTimerGpio
	 */
	public FragmentGpio.RequestTimer getRequestTimerGpio() {
		return requestTimerGpio;
	}

	/**
	 * @param requestTimerGpio the requestTimerGpio to set
	 */
	public void setRequestTimerGpio(FragmentGpio.RequestTimer requestTimerGpio) {
		this.requestTimerGpio = requestTimerGpio;
	}

	/**
	 * @return the requestTimerDevices
	 */
	public FragmentDevices.RequestTimer getRequestTimerDevices() {
		return requestTimerDevices;
	}

	/**
	 * @param requestTimerDevices the requestTimerDevices to set
	 */
	public void setRequestTimerDevices(FragmentDevices.RequestTimer requestTimerDevices) {
		this.requestTimerDevices = requestTimerDevices;
	}

	/**
	 * @return the devices
	 */
	public FragmentDevices getDevices() {
		return devices;
	}

	/**
	 * @param devices the devices to set
	 */
	public void setDevices(FragmentDevices devices) {
		this.devices = devices;
	}

	/**
	 * @return the gpio
	 */
	public FragmentGpio getGpio() {
		return gpio;
	}

	/**
	 * @param gpio the gpio to set
	 */
	public void setGpio(FragmentGpio gpio) {
		this.gpio = gpio;
	}


}