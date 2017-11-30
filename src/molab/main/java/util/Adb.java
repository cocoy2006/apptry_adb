package molab.main.java.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.monkeyrunner.adb.AdbBackend;
import com.android.monkeyrunner.adb.AdbMonkeyDevice;



public class Adb {
	private static final Logger LOG = Logger.getLogger(Adb.class.getName());
	private final int CONNECTION_ITERATION_TIMEOUT_MS = 10 * 1000; // 10 seconds
	private final int WAKE_INTERVAL = 3600 * 1000; // 1 hour
		
	private static Adb adb = null;
	private Map<String, AdbMonkeyDevice> monkeyDevices;
	private AdbBackend adbBackend;
	
	private Adb() {
		adbBackend = new AdbBackend();
		initMonkeyDevices();
		initWake();
		initDeviceChangeListener();
	}
	
	public static Adb getInstance() {
		if(adb == null) {
			synchronized(Adb.class) {
				adb = new Adb();
			}
		}
		return adb;
	}
	
	private void initMonkeyDevices() {
		monkeyDevices = new HashMap<String, AdbMonkeyDevice>();
		for(IDevice iDevice : adbBackend.getBridge().getDevices()) {
			String serialNumber = iDevice.getSerialNumber();
			waitForConnection(serialNumber);
		}
	}
	
	public AdbMonkeyDevice waitForConnection(String serialNumber) {
		return waitForConnection(Integer.MAX_VALUE, serialNumber);
	}
	
	private AdbMonkeyDevice waitForConnection(long timeoutMs, String serialNumber) {
        do {
            IDevice device = findAttacedDevice(serialNumber);
            if (device != null && device.getState() == IDevice.DeviceState.ONLINE) {
            	LOG.log(Level.INFO, "Starting @" + serialNumber + "...");
                AdbMonkeyDevice amd = new AdbMonkeyDevice(device);
                LOG.log(Level.INFO, "@" + serialNumber + " started successfully.");
                monkeyDevices.put(device.getSerialNumber(), amd);
                return amd;
            }
            try {
                Thread.sleep(CONNECTION_ITERATION_TIMEOUT_MS);
            } catch (InterruptedException e) {
                
            }
            timeoutMs -= CONNECTION_ITERATION_TIMEOUT_MS;
        } while (timeoutMs > 0);
        // Timeout.  Give up.
        return null;
    }
	
	private IDevice findAttacedDevice(String serialNumber) {
		for(IDevice iDevice : adbBackend.getBridge().getDevices()) {
			if(serialNumber.equalsIgnoreCase(iDevice.getSerialNumber())) {
				return iDevice;
			}
		}
		return null;
	}
	
	public AdbMonkeyDevice getMonkeyDevice(String serialNumber) {
		return monkeyDevices.get(serialNumber);
	}

	private void initWake() {
		new Timer().schedule(new WakeTask(), WAKE_INTERVAL, WAKE_INTERVAL);
	}
	
	class WakeTask extends TimerTask {

		@Override
		public void run() {
			try {
				for(AdbMonkeyDevice mDevice : monkeyDevices.values()) {
					String serialNumber = mDevice.getIDevice().getSerialNumber();
					Commands.executeCommand(serialNumber, "WAKE");
					LOG.log(Level.INFO, "Apptry Monkey Device: @" + serialNumber + " connect successfully.");
				}
			} catch(Exception e) {
				LOG.log(Level.SEVERE, "Wake timer task error: ", e);
			}
		}
		
	}
	
	public void initDeviceChangeListener() {
		Listener listener = new Listener();
		AndroidDebugBridge.addDeviceChangeListener(listener);
	}
	
	class Listener implements IDeviceChangeListener {

		public void deviceChanged(IDevice device, int changeMask) {
			if((changeMask & IDevice.CHANGE_STATE) != 0 && device.isOnline()) {
				try {
					LOG.log(Level.INFO, "Apptry Device Change Listener: wait 2 minutes to prepare @" + device.getSerialNumber());
					Thread.sleep(120000L);
				} catch (InterruptedException e) {
				}
				waitForConnection(device.getSerialNumber());
				LOG.log(Level.INFO, "Apptry Device Change Listener: @" + device.getSerialNumber() + " changed.");
			}
		}

		public void deviceConnected(IDevice device) {
			LOG.log(Level.INFO, "Apptry Device Change Listener: @" + device.getSerialNumber() + " connected, waiting for device change.");
		}

		public void deviceDisconnected(IDevice device) {
			String serialNumber = device.getSerialNumber();
			if(monkeyDevices.containsKey(serialNumber)) {
				monkeyDevices.remove(serialNumber);
				LOG.log(Level.INFO, "Apptry Device Change Listener: @" + serialNumber + " disconnected successfully.");
			} else {
				LOG.log(Level.SEVERE, "Apptry Device Change Listener: @" + serialNumber + " disconnected, but monkeyDevices has exception.");
			}
		}
		
	}
}
