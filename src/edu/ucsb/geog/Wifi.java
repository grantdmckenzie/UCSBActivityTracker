package edu.ucsb.geog;


import java.util.HashMap;
import java.util.Observable;
import android.net.wifi.*;
import java.util.ArrayList;
import java.util.List;

public class Wifi extends Observable implements Runnable {
	 
    /** Called when the activity is first created. */
 
	private WifiManager wifi;
	private ArrayList<String[]> values = new ArrayList<String[]>();
	private HashMap fix = new HashMap();
	private long interval;
	
    public Wifi(WifiManager wifi) {
    	 this.wifi = wifi; 
    }

    public Wifi(WifiManager wifi, long msIntervall) {
   	 this.interval = msIntervall; 
   	 this.wifi = wifi;
   }
    public void run(){

    		while(true)
   		 {
		       	 
    			 try {
    				 List <ScanResult> scanresults = wifi.getScanResults();
    		         fix.put("sensor", 2.0);
    		         for (ScanResult sr: scanresults)
    		         {
    		         	fix.put(sr.SSID, sr);
    		         }
		         setChanged();
		         notifyObservers(fix);
		         Thread.sleep(interval);
    			 }
    			 catch(InterruptedException e){
    				 e.printStackTrace();
    			 }

    		}
   		 
    }
    public HashMap getFix()
    {
    	return fix;
    }
}