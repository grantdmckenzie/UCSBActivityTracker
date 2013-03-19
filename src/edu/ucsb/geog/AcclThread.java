package edu.ucsb.geog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class AcclThread extends Observable implements Runnable, SensorEventListener
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
	 private double callibrationMean;
	 private float sddif;
	 private WakeLock wakeLock;
	 public WifiManager wifiManager;
	 // private BroadcastReceiver wifiReceiver;
	 private JSONObject prevfix;
	 private double vecLength;
	 private ArrayList<Double> previousVector;
	 private int fixcount;
	 private WifiAlarmReceiver WifiAlarmReceiver;
	 private SimpleDateFormat simpleDateFormat;
	 
	  
	  public AcclThread(Context context)
	  {
		  //Log.v("AcclThread", "Constructor"); 
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
		  this.callibrationMean = appSharedPrefs.getFloat("callibrationMean", -99);
		  this.previousVector = new ArrayList<Double>(3);
		  this.vecLength = 0;
		  
		  // prevfix = null;
		  this.fixcount = 0;
		  // Log.v("AcclThread", "Constructor END"); 
		  simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
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
		  		  String dateString = simpleDateFormat.format(new Date(System.currentTimeMillis()));
		  		  fix.put("ts", dateString);
		  		  
		  	  }
		  	  catch (Exception e) 
		  	  {
		  		  e.printStackTrace();
		  	  }
		  	  
		  	 
		  	  fixes.add(fix);
	
		  	  if(fixes.size()==50) 
		  	  {
		  		mSensorManager.unregisterListener(this);
		  		
		  		BurstSD thisBurstSD = new BurstSD(fixes);
				double thisSD = thisBurstSD.getSD();
				
				boolean isStationary = true;
				double sdDiff = Math.abs(thisSD - this.callibrationSD);
				Log.v("SD difference", sdDiff+"");
				
				if(sdDiff>0.1)
					isStationary = false;
		  		
				if(isStationary)
				{
					if(appSharedPrefs.getBoolean("stationary", true))
					{
						returnStatus(false,true);
					}
					else
					{
						prefsEditor.putBoolean("stationary", true);
						returnStatus(true,true);
					}
					
					Iterator<JSONObject> fixIterator = fixes.iterator();
					while(fixIterator.hasNext())
					{
						JSONObject fixJsonObject = fixIterator.next();
						try
						{
							fixJsonObject.put("status", "stationary");
						} 
						catch (Exception e)
						{
							// TODO: handle exception
						}
						
					}
				}
				else
				{
					if(appSharedPrefs.getBoolean("stationary", true))
					{
						prefsEditor.putBoolean("stationary", false);
						returnStatus(true,false);
					}
					else
					{
						returnStatus(false,false);
					}
					
					Iterator<JSONObject> fixIterator = fixes.iterator();
					while(fixIterator.hasNext())
					{
						JSONObject fixJsonObject = fixIterator.next();
						try
						{
							fixJsonObject.put("status", "moving");
						} 
						catch (Exception e)
						{
							// TODO: handle exception
						}	
					}
					
				}
				
				writeToFile(fixes);
		  	  }
	  		
	  		
	  		
	  		
	  		
	  		
	  	  /*try 
	  	  {
	  		// Wait until we have at least 2 sensor vals
	  		if(this.previousVector.size() != 0) {			
		  	  this.vecLength = Math.sqrt(Math.pow(this.previousVector.get(0)+event.values[0], 2) + Math.pow(this.previousVector.get(1)+event.values[1], 2) + Math.pow(this.previousVector.get(2)+event.values[2], 2)); 	
		  	  Log.v("AcclThread", "Diff: "+Math.abs(this.vecLength - this.callibrationMean));
	  		 
		  	  if (Math.abs(this.vecLength - this.callibrationMean) > this.callibrationSD*2) {
		  		 Log.v("AccelThread", "Movement");
		  		  if (appSharedPrefs.getBoolean("stationary", true)) {
		  			prefsEditor.putBoolean("stationary", false);
		  			// Changed from stationary to movement and we are now moving
		  			returnStatus(true, false);
		  		  } else {
		  			// No change and we are now moving
		  			returnStatus(false, false);
		  		  }
		  	  } else if((this.fixcount == 50) && Math.abs(this.vecLength - this.callibrationMean) <= this.callibrationSD*2) {
		  		Log.v("AccelThread", "Stationary");
		  		if (!appSharedPrefs.getBoolean("stationary", true)) {
		  			prefsEditor.putBoolean("stationary", true);
		  			// Changed from movement to stationary and we are now stationary
		  			returnStatus(true, true);
		  		} else {
		  			returnStatus(false, true);
		  		}
		  	  } 
	  		}
	  		this.previousVector = new ArrayList<Double>(3);
		  	this.previousVector.add(0, (double) event.values[0]);
		  	this.previousVector.add(1, (double) event.values[1]);
		  	this.previousVector.add(2, (double) event.values[2]);
	  		this.fixcount++;
	  	  }
	  	  catch (Exception e) 
	  	  {
	  		  e.printStackTrace();
	  	  }
	  	  
	  	  */
	  } 
	  
	  	
	  private void writeToFile(Vector<JSONObject> fixVector) 
	  {
			Vector<JSONObject> fixVector2 = fixVector;
			fixVector = new Vector<JSONObject>();
			File logFile = new File("sdcard/ucsbat_"+deviceId+".log");
			Log.v("Path to file", "Path to file (service): "+logFile);
		   if (!logFile.exists()) 
		   {
		      try {
		         logFile.createNewFile();
		      } 
		      catch (IOException e) {
		         e.printStackTrace();
		      }
		   }
		   if (fixVector2.size() > 0) {
	           try 
	           {
		    	   BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		           for (int i=0; i<fixVector2.size(); i++) {
		                   buf.append(fixVector2.get(i).toString());
		                   //Log.v("logs", fixVector2.get(i).toString());
		                   buf.newLine();
		           }
		 	       buf.close();    
	            } 
	           catch (IOException e) 
	           {
	                Log.e("TAG", "Could not write file " + e.getMessage());
	            }
	           //running = true;
		   }
	  }
	  	
	  		
	  public void writeToFile(JSONObject fix, Double veclength, Double callibrationSD, String stationary) throws JSONException 
	  {
		  fixes = new Vector<JSONObject>();
		  
		  this.appSharedPrefs.getInt("ucsb_filenum", 0);
		  File logFile = new File("sdcard/UCSB_"+deviceId+".log");
		  // Log.v("Path to file", "Path to file (service): "+logFile);
		   
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
				   Iterator<?> keys = fix.keys();
							
		  			while(keys.hasNext() ){
		  				String key = (String)keys.next();
		  				JSONObject d = (JSONObject) fix.get(key);
		  				String date = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(d.getLong("ts")*1000);
	
		  				buf.append(stationary+",");
		  				buf.append(date + ",");
		  				buf.append(d.getString("ts") + ",");
		  				buf.append(veclength + ",");
		  				buf.append(callibrationSD + ",");
		  				buf.append(d.getString("BSSID") + ",");
		  				buf.append(d.getString("Signal"));
		  				buf.newLine();
	  	            	// Log.v("File Output", stationary+","+date+","+veclength+","+d.getString("BSSID"));
	  	            }
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
		
		// Input: Has stationarity changed?  Are we stationary?
		private void returnStatus(boolean changed, boolean stationary) {
			  	
			
	  		 /* if(!stationary) {
	  			 Log.v("Accel State", "Moving");
				// wifiManager.startScan();
				// writeToFile(scanWifi(), veclength, callibrationSD, "movement"); 
	  			 setChanged();
	  			 notifyObservers();
	  			// If we aren't scanning for wifi, start.
		  			/* if(!appSharedPrefs.getBoolean("wifiscan", false)) {
		  				Log.v("Wifi State", "Starting");
			  			if(WifiAlarmReceiver == null) 
			  				WifiAlarmReceiver = new WifiAlarmReceiver();
			  			WifiAlarmReceiver.SetAlarm(context);
			  			prefsEditor.putBoolean("wifiscan", true);
		  			} 
	  		  } else if (stationary && hasIt){
	  			// wifiManager.startScan();
				//writeToFile(scanWifi(), veclength, callibrationSD, "stationary"); 
	  			Log.v("Accel State", "Stationary");
	  			if(WifiAlarmReceiver != null)		  
	  				WifiAlarmReceiver.CancelAlarm(context);
	  		  } */
			
	  		Log.v("changed", ""+changed);
	  		Log.v("stationary", ""+stationary);
	  		Log.v("separetor", "--------------------------");
			
			 // store state
	  		  prefsEditor.commit();  
	  		  setChanged();
			  notifyObservers();
			
			  // unregister listener
	  		 // mSensorManager.unregisterListener(this);
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
