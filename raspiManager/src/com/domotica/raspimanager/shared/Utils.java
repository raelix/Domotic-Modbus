package com.domotica.raspimanager.shared;

public class Utils {
	public static int strToInt( String str ){
		int i = 0;
		int num = 0;
		boolean isNeg = false;

		//check for negative sign; if it's there, set the isNeg flag
		if( str.charAt(0) == '-') {
			isNeg = true;
			i = 1;
		}

		//process each char of the string; 
		while( i < str.length()) {
			num *= 10;
			num += str.charAt(i++) - '0'; //minus the ASCII code of '0' to get the value of the charAt(i++)
		}

		if (isNeg)
			num = -num;
		return num;
	}
}
