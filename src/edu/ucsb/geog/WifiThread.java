package edu.ucsb.geog;

import java.util.List;
import java.util.Observable;

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
import android.util.Log;

public class WifiThread extends Observable implements Runnable
{
	public WifiManager wifiManager;
	private Context context;

	public WifiThread(Context context) {
		this.context = context;
	}
	
	@Override
	public void run() {
		doScan(this.context);
	}	
	
	private void doScan(Context context) {
		wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
		Log.v("WiFiThread", "Do Scan");
		// List<ScanResult> results = wifiManager.getScanResults();
	    // long timestamp = new Long(System.currentTimeMillis() / 1000);
		/* JSONObject fix = new JSONObject();
		for (ScanResult sr : results) {
			JSONObject wifirecord = new JSONObject();
			try {
				wifirecord.put("ts", timestamp);
				// wifirecord.put("SSID", sr.SSID);
				wifirecord.put("Signal", sr.level);
				// wifirecord.put("Frequency", sr.frequency);
				wifirecord.put("BSSID",sr.BSSID);
				// wifirecord.put("Capabilities", sr.capabilities);
				fix.put(sr.BSSID, wifirecord);
				Log.v("WiFi", ""+sr.BSSID);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}       */ 	
	}



}
