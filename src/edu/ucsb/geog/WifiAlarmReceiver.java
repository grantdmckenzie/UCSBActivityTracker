package edu.ucsb.geog;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class WifiAlarmReceiver extends BroadcastReceiver 
{
	private static long msInterval = 60000;
	private AlarmManager wifiAlarmManager;
	public WifiManager wifiManager;

	public void onReceive(Context context, Intent intent) {  
		doScan(context);
    }

	public void SetAlarm(Context context)
	{
		Log.v("WiFiReceiver", "Set Alarm");
		// doScan(context);
		if(wifiAlarmManager == null)
			wifiAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent(context, WifiAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		
		wifiAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), msInterval, pi);
		// wifiAlarmManager.set(AlarmManager.RTC_WAKEUP, msInterval, pi);
	}

	public void CancelAlarm(Context context)
	{
		Log.v("WiFiReceiver", "Cancel Alarm");
		if(wifiAlarmManager == null)
			wifiAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		// Intent intent = new Intent(context, AlarmReceiver.class);
		// PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		// wifiAlarmManager.cancel(sender);
	}
	
	private void doScan(Context context) {
		wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
		Log.v("WiFiReceiver", "Do Scan");
		List<ScanResult> results = wifiManager.getScanResults();
	    long timestamp = new Long(System.currentTimeMillis() / 1000);
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
