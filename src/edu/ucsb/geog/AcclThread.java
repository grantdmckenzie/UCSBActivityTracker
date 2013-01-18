package edu.ucsb.geog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AcclThread implements Runnable, SensorEventListener
{
	
	 private Context context;
	 private SensorManager mSensorManager;
	 private Sensor mAccelerometer;
	 private Vector<JSONObject> fixes;
	  
	 private static final String PREFERENCE_NAME = "ucsbprefs";
	 private SharedPreferences appSharedPrefs;
	 private Editor prefsEditor;
	 private int filenum;
	 private String deviceId;
	 private TelephonyManager tm;
	 private BurstSD burstSD;
	 private double standardDeviation;
	 private double callibrationSD;
	 private int outside95count;
	 private float sddif;
	 private WakeLock wakeLock;
	 private WakeLock wakeLock2;
	 private WifiManager wifiManager;
	 
	  
	  public AcclThread(Context context, WakeLock wl)
	  {
		  this.context = context;
		  tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		  String tmDevice, tmSerial, androidId;
		  tmDevice = "" + tm.getDeviceId();
		  tmSerial = "" + tm.getSimSerialNumber();
		  androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		  UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		  deviceId = deviceUuid.toString();
		  		  
		  mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		  mAccelerometer = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
		  fixes = new Vector<JSONObject>();
		  wakeLock2 = wl;		  
		  if(wakeLock == null)
		  {
		       PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		       wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "tag");	        
		  }
		  
		  // Get Calibration SD from Shared Preferences
		  this.appSharedPrefs = context.getSharedPreferences("edu.ucsb.geog", Context.MODE_WORLD_READABLE);
	      this.prefsEditor = appSharedPrefs.edit();
	      
		  this.callibrationSD = appSharedPrefs.getFloat("callibrationSD", -99);
		  
		  wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	  }
	  
	   
	  @Override
	  public void onAccuracyChanged(Sensor sensor, int accuracy) 
	  {
		
	  }

	  	@Override
	  public void onSensorChanged(SensorEvent event) 
	  {
	  	  JSONObject fix = new JSONObject();
  		
	  	  try 
	  	  {
	  		  fix.put("sensor", 1.0);
	  		  fix.put("accelx", event.values[0]);
	  		  fix.put("accely", event.values[1]);
	  		  fix.put("accelz", event.values[2]);
	  		  fix.put("ts", new Long(System.currentTimeMillis()/1000));
	  	  }
	  	  catch (Exception e) 
	  	  {
	  		  e.printStackTrace();
	  	  }
<<<<<<< HEAD
	  	  
	  	 
	  	  fixes.add(fix);
	  	  if (outside95(fix)) {
	  		  this.outside95count++;
	  	  }
	  	  // This is what happens when movement IS detected
	  	  if (this.outside95count > 10) 
	  	  {
	  		mSensorManager.unregisterListener(this);	
	  		  if(appSharedPrefs.getBoolean("stationary", true)) 
	  		  {
	  			  prefsEditor.putBoolean("stationary", false);
	  			  stationarityHasChanged(true);
	  		  } 
	  		  else 
	  		  {
	  			  stationarityHasChanged(false);
	  		  }
	  		  Log.v("stationary", ""+appSharedPrefs.getBoolean("stationary", true));
	  		  
	  		try
			{
	  			if(wakeLock.isHeld())
	  			{
	  				wakeLock.release();
	  			}
			} 
	  		  catch (Exception e)
			{
				e.printStackTrace();
			}
	  		  	
	  	  } 
	  	 // This is what happens when movement is NOT detected
	  	  else if(fixes.size()==50) 
	  	  {
	  		mSensorManager.unregisterListener(this);	
	  		  if(!appSharedPrefs.getBoolean("stationary", true)) 
	  		  {
=======
	  	 
	  	  fixes.add(fix);
	  	  
	  	  if(fixes.size()==50) {
	  		  this.burstSD = new BurstSD(fixes);
	  		  this.standardDeviation = this.burstSD.getSD();
	  		  this.sddif = (float) Math.abs(this.standardDeviation - this.callibrationSD);
	  		  Log.v("sd dif", ""+this.sddif);
	  		  if(!appSharedPrefs.getBoolean("stationary", true) && this.sddif <= 0.1) {
>>>>>>> 9d8298b631f4f02153898c09ac5b7e13e0efafdc
	  			  prefsEditor.putBoolean("stationary", true);
	  			  stationarityHasChanged(true);
	  		  } 
	  		  else 
	  		  {
	  			  stationarityHasChanged(false);
	  		  }
<<<<<<< HEAD
	  		  Log.v("stationary", ""+appSharedPrefs.getBoolean("stationary", true));
	  		try
			{
	  			if(wakeLock.isHeld())
	  			{
	  				wakeLock.release();
	  			}
			} 
	  		  catch (Exception e)
			{
				e.printStackTrace();
			}	
=======
>>>>>>> 9d8298b631f4f02153898c09ac5b7e13e0efafdc
	  	  }		
	  } 
	  	
	  		
	  public void writeToFile(JSONObject fix) throws JSONException 
	  {
		  // Vector<JSONObject> fixVector2 = fixes;
		  // Log.v("vector size", "size: "+fixVector2.size());
		  fixes = new Vector<JSONObject>();
		  
		  this.appSharedPrefs.getInt("ucsb_filenum", 0);
		  File logFile = new File("sdcard/ucsbat_"+deviceId+"-"+filenum+".log");
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
		  
		  
		  if (fix.length() > 0) 
		  {
	          try 
	          {
		    	   BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		           /* for (int i=0; i<fixVector2.size(); i++) {
		        	   
		                   buf.append(fixVector2.get(i).get("ts").toString().",");
		                   //Log.v("logs", fixVector2.get(i).toString());
		                   buf.newLine();
		           } */
//		           buf.append(fixVector2.get(0).getString("ts")+","+fixVector2.get(fixVector2.size()-1).getString("ts")+","+this.sddif);
//		           Log.v("ts", fixVector2.get(0).getString("ts"));
//		           buf.newLine();
//		 	       buf.close();  
		 	       
		 	      /* for (int i=0; i<fixVector2.size(); i++) 
		 	      {
	                  buf.append(fixVector2.get(i).toString());
	                  //Log.v("logs", fixVector2.get(i).toString());
	                  buf.newLine();
		 	      } */
					Iterator<?> keys = fix.keys();
		  			while(keys.hasNext() ){
		  				String key = (String)keys.next();
		  				buf.append(fix.get(key) + ",");
	  	            	Log.v("wifi scan", ""+fix.get(key));
	  	            }
		  			buf.newLine();
		  			buf.close(); 
	           } 
	           catch (IOException e) 
	           {
	                Log.e("TAG", "Could not write file " + e.getMessage());
	           }
	           
		   }
		}

		@Override
		public void run() 
		{
			wakeLock.acquire();
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
			
		}
		
<<<<<<< HEAD
		// Check to see if the vector of x, y, z is outside the upper and lower bounds of the calibration mean +- standard deviations (3)?
		private boolean outside95(JSONObject fix) {
			double x = 0;
			double y = 0;
			double z = 0;
			try {
				x = (Double) fix.get("accelx");
				y = (Double) fix.get("accely");
				z = (Double) fix.get("accelz");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		  	double v = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
		     Log.v("cal values", ""+this.callibrationLB + "|" + v + "|" + this.callibrationUB);
		  	if (v >= this.callibrationLB && v <= this.callibrationUB) {
		  		return false;
		  	} else {
		  		return true;
		  	}
		}
=======
>>>>>>> 9d8298b631f4f02153898c09ac5b7e13e0efafdc
		
		private void stationarityHasChanged(boolean hasIt) {
			  	
//	  		  if(hasIt) {
//	  			 try {
//					writeToFile(scanWifi());
//				} catch (JSONException e) {
//					e.printStackTrace();
//				} 
//	  		  }
	  		  prefsEditor.commit();
	  		  
	  		  //wakeLock2.release();
		}
		
		public JSONObject scanWifi() throws JSONException {
		    List<ScanResult> results = wifiManager.getScanResults();
		    long timestamp = new Long(System.currentTimeMillis() / 1000);
			JSONObject fix = new JSONObject();
			for (ScanResult sr : results) {
				JSONObject wifirecord = new JSONObject();
				wifirecord.put("ts", timestamp);
				// wifirecord.put("SSID", sr.SSID);
				wifirecord.put("Signal", sr.level);
				// wifirecord.put("Frequency", sr.frequency);
				wifirecord.put("BSSID",sr.BSSID);
				// wifirecord.put("Capabilities", sr.capabilities);
				fix.put(sr.BSSID, wifirecord);
			}
		    return fix;
		}
}
