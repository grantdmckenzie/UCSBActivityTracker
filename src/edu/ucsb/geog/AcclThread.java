package edu.ucsb.geog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
	 private int filenum;
	 private String deviceId;
	 private TelephonyManager tm;
	 private BurstSD burstSD;
	 private double standardDeviation;
	 private SharedPreferences appSharedPrefs;
	 private float calibrationSD;
	 private float sddif;
	  
	 private WakeLock wakeLock;
	 private WakeLock wakeLock2;
	  
	  
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
		  this.calibrationSD = appSharedPrefs.getFloat("callibrationSD", -99);
		  
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
	  	  fixes.add(fix);
	  		
	  	  
	  	  if(fixes.size()==50)
	  	  {
	  		  mSensorManager.unregisterListener(this);
	  			
	  			// Calculate the Standard Deviation for the Burst
	  			//this.burstSD = new BurstSD(fixes);
				//this.standardDeviation = this.burstSD.getSD();
				//this.sddif = (float) this.standardDeviation - this.calibrationSD;
				// Log.v("Burst Standard Deviation", ""+ this.standardDeviation);
				//Log.v("SD Difference", ""+this.sddif);
				// Log.v("CallibrationSD", ""+v);
				
	  		  try 
	  		  {
	  			  writeToFile();
	  		  } 
	  		  catch (JSONException e) 
	  		  {					
					e.printStackTrace();
	  		  }
	  		  
	  		  //release the lock passed from the alarmManager
	  		  wakeLock2.release();            			
	  	  }		
	  	}
	  	
	  		
	  public void writeToFile() throws JSONException 
	  {
		  Vector<JSONObject> fixVector2 = fixes;
		  Log.v("vector size", "size: "+fixVector2.size());
		  fixes = new Vector<JSONObject>();
		  SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_WORLD_READABLE);
		  settings.getInt("ucsb_filenum", 0);
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
		  
		  
		  if (fixVector2.size() > 0) 
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
		 	       
		 	      for (int i=0; i<fixVector2.size(); i++) 
		 	      {
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
	           
		   }
		}

		@Override
		public void run() 
		{
			wakeLock.acquire();
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
			wakeLock.release();	
		}

}
