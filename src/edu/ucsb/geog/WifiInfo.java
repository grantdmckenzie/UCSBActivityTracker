package edu.ucsb.geog;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.net.wifi.*;
import android.content.*;

import java.util.*;
public class WifiInfo extends Activity {
	 
    /** Called when the activity is first created. */
 
	private WifiManager wifi;
	private ArrayList<String[]> values = new ArrayList<String[]>();
    public WifiInfo(WifiManager wifi) {
         
    	 this.wifi = wifi;
    	 
    }
    public ArrayList<String[]> getValues(){
    	 List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
         List <ScanResult> scanresults = wifi.getScanResults();
         for (ScanResult sr: scanresults)
         {
         	String []tmpArr = {sr.SSID, Integer.toString(sr.level)};
         	values.add(tmpArr);
         }
         return values;
    }
}