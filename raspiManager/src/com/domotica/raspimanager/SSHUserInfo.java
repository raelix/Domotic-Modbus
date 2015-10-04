package com.domotica.raspimanager;

import com.jcraft.jsch.UserInfo;

public class SSHUserInfo implements UserInfo {  
	private String password;  

	SSHUserInfo(String password) {  
		this.password = password;  
	}  

	@Override
	public String getPassphrase() {  
		return null;  
	}  

	@Override
	public String getPassword() {  
		return password;  
	}  

	@Override
	public boolean promptPassword(String arg0) {  
		return true;  
	}  

	@Override
	public boolean promptPassphrase(String arg0) {  
		return true;  
	}  

	@Override
	public boolean promptYesNo(String arg0) {  
		return true;  
	}  

	@Override
	public void showMessage(String arg0) {  
		System.out.println(arg0);  
	}  
}  