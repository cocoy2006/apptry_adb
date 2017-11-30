package molab.main.java.util;

public class Status {
	
	/**
	 * common states*/
	public static final int SUCCESS = 1;
	public static final int FAILURE = -1;
	public static final int ERROR = -1;
	public static final int ERROR_SESSION_ATTR_LOST = 97;
	public static final int ERROR_SESSION_LOST = 98;
	public static final int ERROR_SYSTEM = 99;
	
	/**
	 * check states*/
	public static final String PASS = "PASS";
	public static final String NOT_PASS = "NOT_PASS";
	
	/**
	 * websockify states*/
	public static final int IDLE = 1;
	public static final int BUSY = 2;
	public static final int OTHER = 9;
}