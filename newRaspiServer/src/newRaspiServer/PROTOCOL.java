package newRaspiServer;

interface PROTOCOL {
	public static final int OFF = 0;
	public static final int ON = 1;
	public static final boolean Off = false;
	public static final boolean On = true;
	public static final int OUT = 0;
	public static final int IN = 1;
	public static final int OUT_OFF = 10;
	public static final int TOGGLEOUT_OFF = 12;
	public static final int TOGGLEOUT_ON = 13;
	public static final int OUT_ON = 11;
	public static final int IN_OK = 20;
	public static final int IN_ALARM = 21;
	public static final int STATUS_OK = 1;
	public static final int GPIO_NUMBER = 14;
	public static final int STATUS_ERROR = 0;
}
