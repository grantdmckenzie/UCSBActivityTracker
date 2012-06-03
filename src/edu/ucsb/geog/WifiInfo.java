package edu.ucsb.geog;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
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




