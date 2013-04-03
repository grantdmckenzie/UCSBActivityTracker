package edu.ucsb.geog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WifiThread extends Observable implements Runnable
{
	public WifiManager wifiManager;
	private Context context;
	private String deviceId;
	private TelephonyManager tm;
	public HashMap<String, Integer> gBssids;

	public WifiThread(Context context) {
		  this.context = context;
		  tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		  String tmDevice, tmSerial, androidId;
		  tmDevice = "" + tm.getDeviceId();
		  tmSerial = "" + tm.getSimSerialNumber();
		  androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		  UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		  deviceId = deviceUuid.toString();
	}
	
	@Override
	public void run() {
		long start = new Long(System.currentTimeMillis());
		gBssids = new HashMap<String, Integer>();
		for(int i=0;i<100;i++) {
			ArrayList<String> bssids = doScan(this.context, i);
			for (String s : bssids) {
				boolean match = false;
				Iterator iBssids = gBssids.keySet().iterator();
				while(iBssids.hasNext()) {
					String key=(String)iBssids.next();
					if (s.equals(key)) {
			        	Integer value=(Integer)gBssids.get(key);
			        	gBssids.put(key, value+1);
			        	match = true;
			        }
				}
				if (!match) {
			    	gBssids.put(s, 1);
			    }
			}
		}
		Log.v("WifiThread", ""+gBssids.toString());
		writeToFile(gBssids, start);
		setChanged();
		notifyObservers();
	}	
	
	private ArrayList<String> doScan(Context context, int i) {
		wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
		// Log.v("WiFiThread", "Do Scan");
		List<ScanResult> results = wifiManager.getScanResults();
	    // long timestamp = new Long(System.currentTimeMillis() / 1000);
	    ArrayList<String> bssids = new ArrayList<String>();
		for (ScanResult sr : results) {
			bssids.add(sr.BSSID);
			// Log.v("WiFi", i+": "+sr.BSSID);
		}     
		return bssids;
	}

	public void writeToFile(HashMap<String, Integer> bssids, long start) {

		  File logFile = new File("sdcard/UCSB_"+deviceId+".wifi");
		  Log.v("Path to file", "Path to file (service): "+logFile);
		   
		  if (!logFile.exists()) 
		  {
		      try 
		      {
		         logFile.createNewFile();
		      } 
		      catch (IOException e) 
		      {
		         e.printStackTrace();
		      }
		  }
		  BufferedWriter buf;
		  try {
			  buf = new BufferedWriter(new FileWriter(logFile, true));
			  Iterator iBssids = bssids.keySet().iterator();
			  while(iBssids.hasNext()) {
				  String key = (String)iBssids.next();
				  Integer value = (Integer)bssids.get(key);
				  String date = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(start);
				  buf.append(start+",");
				  buf.append(date+",");
				  buf.append(key+",");
				  buf.append(value+",");
				  buf.newLine();
			  }
			  buf.close(); 
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  } 

	}

}
