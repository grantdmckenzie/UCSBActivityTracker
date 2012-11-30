package edu.ucsb.geog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AcclThread implements Runnable, SensorEventListener
{
	
	 private SensorManager mSensorManager;
	 private Sensor mAccelerometer;
	 private Vector<JSONObject> fixes;
	  
	 private static final String PREFERENCE_NAME = "ucsbprefs";
	 private int filenum;
	 private String deviceId;
	 private TelephonyManager tm;
	 private Timer acclTimer;
	 private BurstSD burstSD;
	 private double standardDeviation;
	 private SharedPreferences appSharedPrefs;
	 private float calibrationSD;
	 private float sddif;
	  
	 private WakeLock wakeLock;
	 private AlarmReceiver alarmReceiver;
	 private WakeLock wakeLock2;
	  
	  
	  public AcclThread(Context context, WakeLock wl)
	  {
		  mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		  mAccelerometer = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
		  fixes = new Vector<JSONObject>();
		  wakeLock2 = wl;
		  
		  if(wakeLock == null)
		  {
		       PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		       wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "tag");	        
		  }
		  
	  }
	  
	  
	  
//	  public void onCreate() 
//	  {	 
//		  showNotification();
//		  
//		  	tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//			String tmDevice, tmSerial, androidId;
//			tmDevice = "" + tm.getDeviceId();
//			tmSerial = "" + tm.getSimSerialNumber();
//			androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
//			UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
//			deviceId = deviceUuid.toString();
//			  	  
//			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//			mAccelerometer = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
//		    fixes = new Vector<JSONObject>();
//		    
//		    // Get Calibration SD from Shared Preferences
//		    this.appSharedPrefs = getSharedPreferences("edu.ucsb.geog", MODE_WORLD_READABLE);
//		    this.calibrationSD = appSharedPrefs.getFloat("callibrationSD", -99);
//		    
//		    //timer = new Timer();
//		    handler = new Handler();
//		    if(wakeLock == null)
//		    {
//		       PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		       wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "tag");	        
//		    }
//		    
//		    
//		        
//	  }
	  
//	  public int onStartCommand(Intent intent, int flags, int startId) 
//	  {
//		  alarmReceiver = new AlarmReceiver();
//		  alarmReceiver.SetAlarm(getApplicationContext());
		 
		  
		  //handler.postAtTime(this,SystemClock.elapsedRealtime()+10000);
		  
		 //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
		 
		  //Thread thisThread = new Thread(this);
		  //thisThread.start();
		  
		  
//		  acclTimer = new Timer();
//	      TimerTask doThis;
	//
//	      int delay = 0;   // delay for 0 sec.
//	      int period = 60000;  // repeat every 60 sec.
//	      doThis = new TimerTask() {
//	        public void run() {
	        	//Accelerometer accelerometer = new Accelerometer(mSensorManager, 30000, AccelService.this, calibrationSD); 		
	    		//Thread accelThread = new Thread(accelerometer);
	    		//accelThread.start();
//	        }
//	      };
//	      acclTimer.schedule(doThis, delay, period);
//		  
//		 return START_STICKY;
//	  }
	  
	  

	  

//	  public void onDestroy() 
//	  {
//		  mSensorManager.unregisterListener(this);
//		  //acclTimer.cancel();
//		  alarmReceiver.CancelAlarm(getApplicationContext());
//		  stopForeground(true); 
//		  
//		  
//	  }
//	  
//	  @Override
//	  public IBinder onBind(Intent intent) 
//	  {
//	    return(null);
//	  }
	   
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
	  		// Log.v("Vector Size", "Vector Size: "+ fixes.size());
	  		
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
				
	  			try {
					writeToFile();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	  			
	  			wakeLock2.release();
	  			
	  			//handler.postAtTime(this,SystemClock.elapsedRealtime()+10000);
	  			
	  			
	  						
	  			
	  			
//	  			timer.schedule(new TimerTask() {
//					public void run() {
//						mSensorManager.registerListener(AccelService.this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);					
//					}
//				}, msInterval);
	  			                 			
	  		}
	  		
		
	  	}
	  	
	  	
//		private void showNotification()
//		{
//			 Notification note=new Notification(R.drawable.iconnotification, getText(R.string.accel_started), System.currentTimeMillis());
//		     
//			 Intent notifyIntent = new Intent(Intent.ACTION_MAIN);
//		     notifyIntent.setClass(getApplicationContext(), UCSBActivityTrackerActivity.class);
//		     notifyIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//		     
//		     PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//		     
//		     note.setLatestEventInfo(this, getText(R.string.local_service_label), getText(R.string.accel_started), contentIntent);
//		 	
//		     note.flags|=Notification.FLAG_NO_CLEAR;
//
//		     startForeground(1337, note);
//		     	     
//		}
		
		public void writeToFile() throws JSONException {
			Vector<JSONObject> fixVector2 = fixes;
			Log.v("vector size", "size: "+fixVector2.size());
			fixes = new Vector<JSONObject>();
			//SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, Context.MODE_WORLD_READABLE);
			filenum = 1; //settings.getInt("ucsb_filenum", 0);
			File logFile = new File("sdcard/ucsbat_"+deviceId+"-"+filenum+".log");
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
		           /* for (int i=0; i<fixVector2.size(); i++) {
		        	   
		                   buf.append(fixVector2.get(i).get("ts").toString().",");
		                   //Log.v("logs", fixVector2.get(i).toString());
		                   buf.newLine();
		           } */
//		           buf.append(fixVector2.get(0).getString("ts")+","+fixVector2.get(fixVector2.size()-1).getString("ts")+","+this.sddif);
//		           Log.v("ts", fixVector2.get(0).getString("ts"));
//		           buf.newLine();
//		 	       buf.close();  
		 	       
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


		@Override
		public void run() 
		{
			wakeLock.acquire();
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);	
			wakeLock.release();
			
			
			
		}



}
