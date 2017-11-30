package molab.test.java.util;

import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import molab.main.java.util.hierarchyviewer.DeviceBridge;
import molab.main.java.util.hierarchyviewer.DeviceConnection;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.hierarchyviewerlib.device.WindowUpdater.IWindowChangeListener;
import com.android.monkeyrunner.adb.AdbBackend;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AdbBackend adbBackend = new AdbBackend();
		IDevice device = adbBackend.getBridge().getDevices()[0];
		System.out.println(device.getSerialNumber());
		
//		TestReceiver tr = new TestReceiver();
//		try {
//			device.executeShellCommand("input keyevent 82", tr);
//			System.out.println("1:" + System.currentTimeMillis());
//			device.executeShellCommand("input keyevent 82", tr);
//			System.out.println("2:" + System.currentTimeMillis());
//			device.executeShellCommand("input keyevent 82", tr);
//			System.out.println("3:" + System.currentTimeMillis());
//			device.executeShellCommand("input keyevent 82", tr);
//			System.out.println("4:" + System.currentTimeMillis());
//		} catch (TimeoutException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (AdbCommandRejectedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ShellCommandUnresponsiveException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		Test test = new Test();
//		Listener listener = test.new Listener();
//		WindowUpdater.startListenForWindowChanges(listener, device);
		
		
		DeviceBridge.setupDeviceForward(device);
		if(!DeviceBridge.isViewServerRunning(device)) {
			try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            if (!DeviceBridge.startViewServer(device)) {
                DeviceBridge.removeDeviceForward(device);
            }
		}
//		
//		list(device);
		System.out.println(getFocusedWindow(device));
		
		System.exit(0);
	}
	
	public static int getFocusedWindow(IDevice device) {
		System.out.println("------function getFocusedWindow------");
        DeviceConnection connection = null;
        try {
            connection = new DeviceConnection(device, 12345);
//            connection.sendCommand("GET_FOCUS");
            connection.sendCommand("monkey --port 12345");
            String line = connection.getInputStream().readLine();
            System.out.println(line);
            if (line == null || line.length() == 0) {
                return -1;
            }
            return (int) Long.parseLong(line.substring(0, line.indexOf(' ')), 16);
        } catch (Exception e) {
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return -1;
    }
	
	class Listener implements IWindowChangeListener {

		public void focusChanged(IDevice device) {
			// TODO Auto-generated method stub
			
		}

		public void windowsChanged(IDevice device) {
			// TODO Auto-generated method stub
			getFocusedWindow(device);
		}
		
	}
	
	private static final class TestReceiver extends MultiLineReceiver {

        private static final String SUCCESS_OUTPUT = "Success"; //$NON-NLS-1$
        private static final Pattern FAILURE_PATTERN = Pattern.compile("Failure\\s+\\[(.*)\\]"); //$NON-NLS-1$

        private String mErrorMessage = null;

        public TestReceiver() {
        }

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                if (line.length() > 0) {
                    if (line.startsWith(SUCCESS_OUTPUT)) {
                        mErrorMessage = null;
                    } else {
                        Matcher m = FAILURE_PATTERN.matcher(line);
                        if (m.matches()) {
                            mErrorMessage = m.group(1);
                        }
                    }
                }
            }
        }

        public boolean isCancelled() {
            return false;
        }

        public String getErrorMessage() {
            return mErrorMessage;
        }
    }

	private static void list(IDevice device) {
		System.out.println("------function list------");
		DeviceConnection connection = null;
        try {
        	connection = new DeviceConnection(device);
        	connection.sendCommand("LIST");
        	BufferedReader in = connection.getInputStream();
            String line;
            while ((line = in.readLine()) != null) {
                if ("DONE.".equalsIgnoreCase(line)) { //$NON-NLS-1$
                    break;
                }
                System.out.println(line);
            }
        } catch (Exception e) {
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
	}

}
