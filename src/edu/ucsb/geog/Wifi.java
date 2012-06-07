package edu.ucsb.geog;


import java.util.HashMap;
import java.util.Observable;

import android.location.LocationManager;
import android.net.wifi.*;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Wifi extends Observable implements Runnable {
	 
    /** Called when the activity is first created. */
 
	private WifiManager wifi;
	private ArrayList<String[]> values = new ArrayList<String[]>();
	private JSONObject fix = new JSONObject();
	private long interval;
	private boolean running = true;
	
    public Wifi(WifiManager wifi) {
    	 this.wifi = wifi; 
    }

    public Wifi(WifiManager wifi, long msIntervall) {
   	 this.interval = msIntervall; 
   	 this.wifi = wifi;
   }
    
	public void startRecording()
	{
		running = true;
	}
	
	public void stopRecording()
	{
		running = false;
	}
    
    
    public void run(){

    	while(running)
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
    			 } catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

    		}
   		 
    }
    public JSONObject getFix()
    {
    	return fix;
    }
}