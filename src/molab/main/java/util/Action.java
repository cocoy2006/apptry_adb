package molab.main.java.util;

public enum Action {
	
	CHECK, WAKE, TOUCH, PRESS, DRAG, TYPE, SHELL, SYNC, REMOVE, INSTALL, REINSTALL, UNINSTALL, START, UPLOAD, RESERVE, ERROR;
	
	public static Action toAction(String action) {
		try {
			return valueOf(action);
		} catch(Exception e) {
			return ERROR;
		}
	}
}