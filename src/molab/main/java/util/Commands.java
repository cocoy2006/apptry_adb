package molab.main.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import molab.main.java.util.hierarchyviewer.DeviceBridge;
import molab.main.java.util.hierarchyviewer.DeviceConnection;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.monkeyrunner.PhysicalButton;
import com.android.monkeyrunner.MonkeyDevice.TouchPressType;
import com.android.monkeyrunner.adb.AdbMonkeyDevice;

public class Commands {
	private static final Logger LOG = Logger.getLogger(Commands.class.getName());
	
	private static AdbMonkeyDevice device;
	
	public static synchronized String executeCommand(String serialNumber, String command) {
		device = Adb.getInstance().getMonkeyDevice(serialNumber);
		String result = "SUCCESS";
		LOG.log(Level.INFO, "Apptry Command: " + command);
		try {
			result = handler(command);
		} catch(Exception e) {
			LOG.log(Level.SEVERE, "Broken pipe.");
			try {
//				int localPort = AdbMonkeyDevice.getLocalPort(device.getIDevice());
				device = Adb.getInstance().waitForConnection(serialNumber);
				result = handler(command);
			} catch (Exception e1) {
				LOG.log(Level.SEVERE, "Broken pipe again.");
			}
		}
		return result;
	}
	
	private static synchronized String handler(String command) throws IOException {
		String result = "SUCCESS";
		switch(Action.toAction(command.split(" ")[0].toUpperCase())) {
			case CHECK:	
				result = check(command);	break;
			case WAKE:	wake(); 			break;
			case TOUCH: touch(command);		break;
			case PRESS: press(command); 	break;
			case DRAG:	drag(command);		break;
			case TYPE:	type(command);		break;
			case SHELL:	shell(command);		break;
			case REINSTALL:	
				result = reinstall(command);break;
			case UNINSTALL:
				result = uninstall(command);break;
			case START:	
				startActivity(command);		break;
			default:						break;
		}
		return result;
	}

	/**
	public String executeCommand(String command) {
		String result = "SUCCESS";
		synchronized (this) {
			LOG.log(Level.INFO, "Going to execute: " + command);
			switch(Action.toAction(command.split(" ")[0].toUpperCase())) {
				case CHECK:
					result = check(command);
					break;
				case WAKE:
					wake();
					break;
				case TOUCH:
					touch(command);
					break;
				case PRESS:
					press(command);
					break;
				case DRAG:
					drag(command);
					break;
				case TYPE:
					type(command);
					break;
				case SHELL:
					shell(command);
					break;
//				case SYNC:
//					result = sync(command);
//					break;
//				case REMOVE:
//					result = removePackage(command);
//					break;
//				case INSTALL:
//					result = installPackage(command);
//					break;
				case REINSTALL:
					result = reinstall(command);
					break;
//				case UNINSTALL:
//					result = uninstallPackage(command);
//					break;
				case START:
					startActivity(command);
					break;
//				case ERROR:
//					break;
				default:
					break;
			}
		}
		return result;
	}
	*/
	
	//CMD = CHECK PACKAGENAME
	public static String check(String cmd) {
		String[] cmds = cmd.split(" ");
		String packageName = cmds[1];
		IDevice iDevice = device.getIDevice();
		if(device.getIDevice().isOnline()) {
			java.util.Stack<String> stack = new java.util.Stack<String>();
			DeviceBridge.setupDeviceForward(iDevice);
			if(!DeviceBridge.isViewServerRunning(iDevice)) {
				try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                if (!DeviceBridge.startViewServer(iDevice)) {
                    DeviceBridge.removeDeviceForward(iDevice);
                }
                return Status.NOT_PASS;
			}
			DeviceConnection connection = null;
	        try {
	        	connection = new DeviceConnection(iDevice);
	        	connection.sendCommand("LIST");
	        	BufferedReader in = connection.getInputStream();
	        	List<String> lines = new ArrayList<String>();
	            String line;
	            while ((line = in.readLine()) != null) {
	                if ("DONE.".equalsIgnoreCase(line)) { //$NON-NLS-1$
	                    break;
	                }
	                lines.add(line);
	            	if(line.indexOf(".") > -1) {
	            		stack.push(line);
	            	}
	            }
	            if(!stack.isEmpty()) {
					String line1 = stack.pop();
					if(line1.indexOf(packageName) > -1) {
						return Status.PASS;
					}
				}
	        	
//	        	connection.sendCommand("GET_FOCUS");
//	            String line = connection.getInputStream().readLine();
//	            if(line.indexOf(packageName) > -1) {
//					return Status.PASS;
//				}
	        } catch (Exception e) {
	        	LOG.log(Level.SEVERE, e.getMessage());
	        } finally {
	            if (connection != null) {
	                connection.close();
	            }
	        }
		}
		return Status.NOT_PASS;
	}
	
	//CMD = WAKE
	public static void wake() throws IOException {
		device.wake();
	}
	
	//CMD = TOUCH X Y DOWN/DOWN_AND_UP
	public static void touch(String cmd) throws IOException {
		String[] cmds = cmd.split(" ");
		int x = Integer.parseInt(cmds[1]), y = Integer.parseInt(cmds[2]);
		if(cmds[3].equalsIgnoreCase("DOWN")) {
			device.touch(x, y, TouchPressType.DOWN);
		} else {
			device.touch(x, y, TouchPressType.DOWN_AND_UP);
		}
	}
	
	//CMD = PRESS HOME/MENU DOWN/DOWN_AND_UP
	// 		PRESS BACK/SEARCH/UP/DOWN/LEFT/RIGHT/ENTER/CENTER
	//		PRESS ...
	public static void press(String cmd) {
		String[] cmds = cmd.split(" ");
		try {
			if(cmds[1].equalsIgnoreCase("HOME") || cmds[1].equalsIgnoreCase("MENU")
					|| cmds[1].equalsIgnoreCase("CALL") || cmds[1].equalsIgnoreCase("ENDCALL")) {
				if(cmds[2].equalsIgnoreCase("DOWN")) {
					device.press("KEYCODE_" + cmds[1], TouchPressType.DOWN);
				} else {
					device.press("KEYCODE_" + cmds[1], TouchPressType.DOWN_AND_UP);
				}
			} else {
				if(cmds[1].equalsIgnoreCase("BACK")) {
					device.getManager().press("KEYCODE_" + PhysicalButton.BACK);
				} else if(cmds[1].equalsIgnoreCase("SEARCH")) {
					device.getManager().press("KEYCODE_" + PhysicalButton.SEARCH);
				} else if(cmds[1].equalsIgnoreCase("UP")) {
					device.getManager().press("KEYCODE_" + PhysicalButton.DPAD_UP);
				} else if(cmds[1].equalsIgnoreCase("DOWN")) {
					device.getManager().press("KEYCODE_" + PhysicalButton.DPAD_DOWN);
				} else if(cmds[1].equalsIgnoreCase("LEFT")) {
					device.getManager().press("KEYCODE_" + PhysicalButton.DPAD_LEFT);
				} else if(cmds[1].equalsIgnoreCase("RIGHT")) {
					device.getManager().press("KEYCODE_" + PhysicalButton.DPAD_RIGHT);
				} else if(cmds[1].equalsIgnoreCase("CENTER")) {
					device.getManager().press("KEYCODE_" + PhysicalButton.DPAD_CENTER);
				} else if(cmds[1].equalsIgnoreCase("ENTER")) {
					device.getManager().press("KEYCODE_" + PhysicalButton.ENTER);
				} else {
					device.getManager().press("KEYCODE_" + cmds[1]);
				}
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage());
		}
	}
	
	//CMD = DRAG STARTX STARTY ENDX ENDY STEPS MS
	public static void drag(String cmd) throws IOException {
		String[] cmds = cmd.split(" ");
		int startx = Integer.parseInt(cmds[1]), starty = Integer.parseInt(cmds[2]),
			endx = Integer.parseInt(cmds[3]), endy = Integer.parseInt(cmds[4]),
			steps = Integer.parseInt(cmds[5]);
		long ms = Long.parseLong(cmds[6]);
		device.drag(startx, starty, endx, endy, steps, ms);
	}
	
	//CMD = TYPE CMD
	public static void type(String cmd) throws IOException {
		String[] cmds = cmd.split(" ");
//		device.type(cmd.replaceFirst(cmds[0], ""));
		if("SPACE".equalsIgnoreCase(cmds[1])) {
			device.type(" ");
		} else {
			device.type(cmds[1]);
		}
	}
	
	//CMD = SHELL CMD(PARAM1 PARAM2 PARAM3...)
	public static String shell(String cmd) {
//		String[] cmds = cmd.split(" ");
//		return device.shell(cmds[1]);
		String newCmd = cmd.substring(cmd.indexOf(" ") + 1);
		return device.shell(newCmd);
	}
	
	//CMD = SYNC LOCALFILEPATH REMOTEPATH
//	public String sync(String cmd) {
//		String[] cmds = cmd.split(" ");
//		String result = null;
//		try {
//			idevice.syncPackageToDevice(cmds[1], cmds[2]);
//		} catch (SyncException e) {
//			result = "SyncException";
//			System.out.println(e.getMessage());
//		} catch (TimeoutException e) {
//			result = "TimeoutException";
//			System.out.println(e.getMessage());
//		} catch (AdbCommandRejectedException e) {
//			result = "AdbCommandRejectedException";
//			System.out.println(e.getMessage());
//		} catch (IOException e) {
//			result = "IOException";
//			System.out.println(e.getMessage());
//		}
//		if(result == null) result = "SYNC_SUCCESS";
//		return result;
//	}
	
	//CMD = REMOVE PACKAGENAME
//	public String removePackage(String cmd) {
//		String[] cmds = cmd.split(" ");
//		try {
//			idevice.removeRemotePackage(cmds[1]);
//			return "REMOVE_SUCCESS";
//		} catch (InstallException e) {
//			System.out.println(e.getMessage());
//			return "InstallException";
//		}
//	}
	
	//CMD = INSTALL LOCALFILEPATH REMOTEPATH
//	public String installPackage(String cmd) {
//		IDevice iDevice = device.getIDevice();
//		String[] cmds = cmd.split(" ");
//		String result = null;
//		try {
//			if(cmds.length == 2) {
//				result = iDevice.installPackage(cmds[1], "/data/local/tmp/", false);
//			} else if(cmds.length == 3) {
//				result = iDevice.installPackage(cmds[1], cmds[2], false);
//			}
//		} catch (InstallException e) {
//			result = "InstallException";
//			System.out.println(e.getMessage());
//		}
//		if(result == null) result = "INSTALL_SUCCESS";
//		return result;
//	}
	
	//CMD = REINSTALL LOCALFILEPATH REMOTEPATH
	public static String reinstall(String cmd) {
		IDevice iDevice = device.getIDevice();
		String[] cmds = cmd.split(" ");
		String result = null;
		try {
			if(cmds.length == 2) {
				result = iDevice.installPackage(cmds[1], "/data/local/tmp/", true);
			} else if(cmds.length == 3) {
				result = iDevice.installPackage(cmds[1], cmds[2], true);
			}
		} catch (InstallException e) {
			result = "InstallException";
			LOG.log(Level.SEVERE, e.getMessage());
		}
		if(result == null) result = "INSTALL_SUCCESS";
		return result;
	}
	
	//CMD = UNINSTALL PACKAGE_NAME
	public static String uninstall(String cmd) {
		IDevice iDevice = device.getIDevice();
		String[] cmds = cmd.split(" ");
		String result = null;
		try {
			result = iDevice.uninstallPackage(cmds[1]);
		} catch (InstallException e) {
			result = "InstallException";
			LOG.log(Level.SEVERE, e.getMessage());
		}
		if(result == null) result = "UNINSTALL_SUCCESS";
		return result;
	}
	
	//CMD = START PACKAGE STARTACTIVITY
	/**
	 * component = package/startActivity */
	public static void startActivity(String cmd) {
		String[] cmds = cmd.split(" ");
		String component = cmds[1] + "/" + cmds[2];
		device.startActivity("", "", "", "", new HashSet<String>(), new HashMap<String, Object>(), component, 0);
	}
	
	//CMD = BROADCASTINTENT URI ACTION DATA MIMETYPE EXTRAS COMPONENT FLAGS
//	public void broadcastIntent(String cmd) {}
	
	//CMD = INSTRUMENT PACKAGENAME ARGS
//	public Map<String, Object> instrument(String cmd) {
//		return null;
//	}
	
}
