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
	 private float sddif;
	 private WakeLock wakeLock;
	 private WifiManager wifiManager;
	 private JSONObject prevfix;
	 private int fixcount;
	 
	  
	  public AcclThread(Context context)
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
		  prevfix = null;
		  fixcount = 0;
	  }
	  
	   
	  @Override
	  public void onAccuracyChanged(Sensor sensor, int accuracy) 
	  {
		
	  }

	  	@Override
	  public void onSensorChanged(SensorEvent event) 
	  {
	  	  JSONObject fix = new JSONObject();
	  	  double veclength = 0;
	  	  
	  	  try 
	  	  {
	  		  if(this.prevfix != null) {
		  		double prevx = (Double) prevfix.get("accelx");
				double prevy = (Double) prevfix.get("accely");
				double prevz = (Double) prevfix.get("accelz");
		  		veclength = Math.sqrt(Math.pow(prevx-event.values[0], 2) + Math.pow(prevy-event.values[1], 2) + Math.pow(prevz-event.values[2], 2));  
		  	  }
	  		  fix.put("sensor", 1.0);
	  		  fix.put("accelx", event.values[0]);
	  		  fix.put("accely", event.values[1]);
	  		  fix.put("accelz", event.values[2]);
	  		  fix.put("ts", new Long(System.currentTimeMillis()/1000));
	  		  this.prevfix = fix;
	  		  fixcount++;
	  		  // fixes.add(fix);
	  	  }
	  	  catch (Exception e) 
	  	  {
	  		  e.printStackTrace();
	  	  }
	  	 	  	  
	  	  if (veclength > (this.callibrationSD*10)) {
	  		  if (appSharedPrefs.getBoolean("stationary", true)) {
	  			Log.v("vector length", "stationary to movement");
	  			prefsEditor.putBoolean("stationary", false);
	  			stationarityHasChanged(true);
	  		  }
	  	  } else if(fixcount >= 50) {
	  		if (!appSharedPrefs.getBoolean("stationary", true)) {
	  			Log.v("vector length", "movement to stationary");
	  			prefsEditor.putBoolean("stationary", true);
	  			stationarityHasChanged(true);
	  		} else {
	  			mSensorManager.unregisterListener(this);
	  		    try {
	  			  if(wakeLock.isHeld())
	  			  {
	  				wakeLock.release();
	  			  }
	  		    } catch (Exception e) {
	  		    	e.printStackTrace();
	  		    }
	  		}
	  	  }
	  	  
	  	 /*  if(fixes.size()==50) 
	  	  {
	  		  mSensorManager.unregisterListener(this);
	  		  this.burstSD = new BurstSD(fixes);
	  		  this.standardDeviation = this.burstSD.getSD();
	  		  this.sddif = (float) Math.abs(this.standardDeviation - this.callibrationSD);
	  		  Log.v("sd dif", ""+this.sddif);
	  		  if(!appSharedPrefs.getBoolean("stationary", true) && this.sddif <= 0.1)
	  		  {
	  			  prefsEditor.putBoolean("stationary", true);
	  			  stationarityHasChanged(true);
	  		  }
	  		  else if(appSharedPrefs.getBoolean("stationary", true) && this.sddif > 0.1)
	  		  {
	  			  prefsEditor.putBoolean("stationary", false);
	  			  stationarityHasChanged(true);
	  		  }
	  		  else 
	  		  {
	  			  stationarityHasChanged(false);
	  		  }
	  		  
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
	  	  }		*/
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
	  	            	// Log.v("wifi scan", ""+fix.get(key));
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
		
		
		private void stationarityHasChanged(boolean hasIt) {
			  	
	  		  if(hasIt) {
	  			 try {
					writeToFile(scanWifi());
				} catch (JSONException e) {
					e.printStackTrace();
				} 
	  		  }
	  		  Log.v("changed", ""+hasIt);
	  		  prefsEditor.commit();  
	  		  mSensorManager.unregisterListener(this);
	  		  try {
	  			  if(wakeLock.isHeld()) {
	  				wakeLock.release();
	  			  }
	  		    } catch (Exception e) {
	  		    	e.printStackTrace();
	  		    }
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
