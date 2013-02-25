package edu.ucsb.geog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	 private float sddif;
	 private WakeLock wakeLock;
	 public WifiManager wifiManager;
	 // private BroadcastReceiver wifiReceiver;
	 private JSONObject prevfix;
	 private int fixcount;
	 private WifiAlarmReceiver WifiAlarmReceiver;
	 
	  
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
		  		// Log.v("vector length", veclength +"");
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
	  	   	  
	  	  if (veclength > (this.callibrationSD*5)) {
	  		  if (appSharedPrefs.getBoolean("stationary", true)) {
	  			//Log.v("Vector Length:", veclength + " > " + this.callibrationSD*5); 
	  			// Log.v("Stationarity:", "stationary to movement");
	  			prefsEditor.putBoolean("stationary", false);
	  			stationarityHasChanged(true, veclength, this.callibrationSD, false);
	  		  } else {
	  			stationarityHasChanged(false, veclength, this.callibrationSD, false);
	  		  }
	  	  } else if(fixcount >= 50 && veclength <= (this.callibrationSD*5)) {
	  		if (!appSharedPrefs.getBoolean("stationary", true)) {
	  			//Log.v("Vector Length:", veclength + " <= " + this.callibrationSD*5); 
	  			// Log.v("Stationarity:", "movement to stationary");
	  			prefsEditor.putBoolean("stationary", true);
	  			stationarityHasChanged(true, veclength, this.callibrationSD, true);
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
		
		
		private void stationarityHasChanged(boolean hasIt, Double veclength, Double callibrationSD, boolean stationary) {
			  	
	  		  if(!stationary) {
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
		  			} */
	  		  } else if (stationary && hasIt){
	  			// wifiManager.startScan();
				//writeToFile(scanWifi(), veclength, callibrationSD, "stationary"); 
	  			Log.v("Accel State", "Stationary");
	  			if(WifiAlarmReceiver != null)		  
	  				WifiAlarmReceiver.CancelAlarm(context);
	  		  }
	  		  // Log.v("changed", ""+hasIt);
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
