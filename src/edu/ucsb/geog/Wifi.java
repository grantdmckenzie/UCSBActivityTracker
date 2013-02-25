package edu.ucsb.geog;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;

public class Wifi extends BroadcastReceiver {
  AcclThread wifi;

  public Wifi(AcclThread wifiDemo) {
    super();
    this.wifi = wifiDemo;
  }

  @Override
  public void onReceive(Context c, Intent intent) {
    List<ScanResult> results = wifi.wifiManager.getScanResults();
    long timestamp = new Long(System.currentTimeMillis() / 1000);
	JSONObject fix = new JSONObject();
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
	}
 }
}