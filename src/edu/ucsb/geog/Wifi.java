package edu.ucsb.geog;


import java.util.Observable;
import android.net.wifi.*;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Wifi extends Observable implements Runnable {
	 
    /** Called when the activity is first created. */
 
	private WifiManager wifi;
	private JSONObject fix = new JSONObject();
	private long interval;
	private long timestamp;
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
    				 timestamp = new Long(System.currentTimeMillis()/1000);			 
    				 //determine whether the wifi signal has changed
    				 boolean flag = false; //default state: not to reset fix
    				 if(fix!=null&&(fix.length()-2==scanresults.size()))//wifi signal changed
    				{
    					 for(ScanResult sr:scanresults){
    						 if(fix.has(sr.BSSID))
    						{
    							 JSONObject wifirecord = (JSONObject)fix.get(sr.BSSID);
    							 if(!wifirecord.get("SSID").equals(sr.SSID)||
    									 (Integer)wifirecord.get("Signal")!=sr.level||
    									 (Integer)wifirecord.get("Frequency")!=sr.frequency||
    									 !wifirecord.get("Capabilities").equals(sr.capabilities)){
    								 flag = true;
    								 break;
    							 }
    						}
    					 }
    				}else if(fix!=null&&(fix.length()-2!=scanresults.size())) //number of available wifi changed
    					flag = true;
    				//only when # of wifi changed or wifi signal changes, reset fix and notify observer, otherwise, sleep and wait to check next return
    				 //Log.v("flag", flag+" (true mean need update, false means no need to update)");
    				 if(flag){
    					 fix = new JSONObject();
	    				 fix.put("sensor", 2.0);
	    		         fix.put("ts", timestamp);
	    		         for (ScanResult sr: scanresults)
	    		         {
	    		        	 JSONObject wifirecord  = new JSONObject();
	    		        	 wifirecord.put("SSID", sr.SSID);
	    		        	 wifirecord.put("Signal",sr.level);
	    		        	 wifirecord.put("Frequency", sr.frequency);
	    		        	 //wifirecord.put("BSSID",sr.BSSID);
	    		        	 wifirecord.put("Capabilities", sr.capabilities);
	    		        	 fix.put(sr.BSSID, wifirecord);
	    		         }
	    		         setChanged();
	    		         notifyObservers(fix);
    				}
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