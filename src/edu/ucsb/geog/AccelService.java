package edu.ucsb.geog;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class AccelService extends Service 
{
  
  private static AlarmReceiver alarmReceiver;
  private static WifiAlarmReceiver wifiAlarmReceiver;
  private GenerateUserActivityThread generateUserActivityThread;
  private ScreenOffBroadcastReceiver screenOffBroadcastReceiver;
  private boolean samplingStarted = false;
  private static AlarmManager alarmManager;
  private static AlarmManager wifiAlarmManager;

  
  public void onCreate() 
  {	 
	  showNotification();
	  // Log.v("AccelService", "onCreate");
	  //This screenOffBroadcastReceiver is responsible for turning the screen on when the user manually turned it off
	  // It is not necessary if the sensors can still work when the screen is off
	  
	  //screenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();
	 // IntentFilter screenOffFilter = new IntentFilter();
	 // screenOffFilter.addAction( Intent.ACTION_SCREEN_OFF );		
	 // registerReceiver( screenOffBroadcastReceiver, screenOffFilter );
	  
	  //--------------------------------------------------

	         
  }
  
  public int onStartCommand(Intent intent, int flags, int startId) 
  {
	  WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	  if (!wifi.isWifiEnabled())
		 wifi.setWifiEnabled(true);
		
	  if(alarmReceiver == null)
		  alarmReceiver = new AlarmReceiver();
	  if(wifiAlarmReceiver == null)
		  wifiAlarmReceiver = new WifiAlarmReceiver();
	  
	  if(alarmManager == null)
		  alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	  if(wifiAlarmManager == null)
		  wifiAlarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	  
	  alarmReceiver.SetAlarm(getApplicationContext());
	  // wifiAlarmReceiver.SetAlarm(getApplicationContext());
	  
	  samplingStarted = true;
	  // Log.v("AccelService", "onStartCommand");
	  //return super.onStartCommand(intent,flags,startId);
	  
	  
	  
	  
	 return START_STICKY;
  }

	
  	@Override
  	public void onDestroy() 
  	{
	  //Cancel alarm when the service is destroyed
	  if(alarmReceiver != null) { 
		  alarmReceiver.CancelAlarm(getApplicationContext());
		  // unregisterReceiver(alarmReceiver);
	  }
	  if(wifiAlarmReceiver != null) {
		  wifiAlarmReceiver.CancelAlarm(getApplicationContext());
		  // unregisterReceiver(wifiAlarmReceiver);
	  }
	  samplingStarted = false;
	  
	  //Unregister the screenOffreceiver when the service is destroyed
	  if(screenOffBroadcastReceiver != null)
		  unregisterReceiver( screenOffBroadcastReceiver );
	  
	  //Cancel the thread which is used to turn the screen on
	  if( generateUserActivityThread != null ) 
	  {
		  generateUserActivityThread.stopThread();
		  generateUserActivityThread = null;
	  }
	  
	  stopForeground(true); 
	  
  	}
  
  @Override
  	public IBinder onBind(Intent intent) 
  {
    return(null);
  }

	private void showNotification()
	{
		 Notification note=new Notification(R.drawable.iconnotification, getText(R.string.accel_started), System.currentTimeMillis());
	     
		 Intent notifyIntent = new Intent(Intent.ACTION_MAIN);
	     notifyIntent.setClass(getApplicationContext(), UCSBActivityTrackerActivity.class);
	     notifyIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	     
	     PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	     
	     note.setLatestEventInfo(this, getText(R.string.local_service_label), getText(R.string.accel_started), contentIntent);
	 	
	     note.flags|=Notification.FLAG_NO_CLEAR;

	     startForeground(1337, note);
	     	     
	}
	
	// This part defines the ScreenOffBroadcastReceiver---------------
	class ScreenOffBroadcastReceiver extends BroadcastReceiver 
	{
		public void onReceive(Context context, Intent intent) 
		{
			
			if(samplingStarted) 
			{
				if( generateUserActivityThread != null ) 
				{
					generateUserActivityThread.stopThread();
					generateUserActivityThread = null;
				}
				
				generateUserActivityThread = new GenerateUserActivityThread();
				generateUserActivityThread.start();
			}
		}
	}

	class GenerateUserActivityThread extends Thread 
	{
		public void run() 
		{
			try 
			{
				Thread.sleep( 2000L );
			} 
			catch( InterruptedException ex ) {}
			
			Log.d( "screenoff", "User activity generation thread started" );

			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
			userActivityWakeLock =  pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "GenerateUserActivity");
			userActivityWakeLock.acquire();
			
		}

		public void stopThread() 
		{
			userActivityWakeLock.release();
			userActivityWakeLock = null;
		}

		PowerManager.WakeLock userActivityWakeLock;
	}
	
	// AlarmReceiver inner Class
	public static class AlarmReceiver extends BroadcastReceiver implements Observer
	{
		private long msInterval = 10000;
		private Context alrmContext;

		@Override
		public void onReceive(Context context, Intent intent) 
	    {   
			// Log.v("AlarmReceiver", "onReceive"); 
			this.alrmContext = context;
	        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
	        wl.acquire();
	        
	        AcclThread acclThread = new AcclThread(context);
	        Thread thread = new Thread(acclThread);
	        thread.start();
	        acclThread.addObserver(this);
	        
	        wl.release();    
	    }

		public void SetAlarm(Context context)
		{
			// Log.v("AlarmReceiver", "setAlarm"); 
			//if(alarmManager == null)
				//alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			
			Intent i = new Intent(context, AlarmReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
			
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), msInterval, pi);
			// Log.v("AlarmReceiver", "setRepeating done"); 

		}

		public void CancelAlarm(Context context)
		{
			//if(alarmManager == null)
				//alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			
			Intent intent = new Intent(context, AlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
			alarmManager.cancel(sender);
			// Log.v("AccelService","End Cancel Alarm");
		}

		// Receives notifications from the observable (the thread)
		@Override
		public void update(Observable observable, Object data) {
			if(observable instanceof AcclThread) {
				boolean stationary = ((AcclThread) observable).stationary;
				boolean stationarityChanged = ((AcclThread) observable).stationarityChanged;
				if(!stationary && stationarityChanged) {
					Log.v("AccelService", "STARTED MOVING");
					// If we just started moving, turn on the wifiscanner
					wifiAlarmReceiver.SetAlarm(this.alrmContext);
				} else if (stationary && stationarityChanged) {
					Log.v("AccelService", "BECAME STATIONARY");
					// If the mobile device stopped moving, turn off the wifiscanner
					wifiAlarmReceiver.CancelAlarm(this.alrmContext);
				}
			}
		}

	}


	// WifiAlarmReceiver inner Class
	public static class WifiAlarmReceiver extends BroadcastReceiver implements Observer, LocationListener
	{
		private long msInterval = 60000;
		private HashMap<String, Integer> previousBSSID;
		private HashMap<String, Integer> currentBSSID;
		private LocationManager locationManager;
		private Location location;
		private SharedPreferences appSharedPrefs;
		private Editor prefsEditor;

		@Override
		public void onReceive(Context context, Intent intent) {  
			
			// Log.v("WifiAlarmReceiver", "onReceive");
			
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
	        wl.acquire();
	        
			WifiThread wifiThread = new WifiThread(context);
	        Thread thread = new Thread(wifiThread);
	        thread.start();
	        wifiThread.addObserver(this);
	        
	        wl.release();
	    }

		public void SetAlarm(Context context)
		{

				this.appSharedPrefs = context.getSharedPreferences("edu.ucsb.geog", Context.MODE_WORLD_READABLE);
			    this.prefsEditor = appSharedPrefs.edit();
			      
				Intent i = new Intent(context, WifiAlarmReceiver.class);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
				wifiAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), msInterval, pi);
				this.locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
				this.location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(this.location != null) {
					prefsEditor.putLong("gpstime", this.location.getTime());
					prefsEditor.putString("gpslat", ""+this.location.getLatitude());
					prefsEditor.putString("gpslng", ""+this.location.getLongitude());
					prefsEditor.commit();  
					double lat = (double) (this.location.getLatitude());
				    double lng = (double) (this.location.getLongitude());
				    Long time = this.location.getTime();
				}
			    // Log.v("Coordinates", "Lat:"+lat+", Lng: "+lng+", TS: "+time);
				this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60, 1, this);
		}

		public void CancelAlarm(Context context)
		{
			// Log.v("WiFiReceiver", "Cancel Alarm");
			Intent intent = new Intent(context, WifiAlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
			wifiAlarmManager.cancel(sender);
			if(this.locationManager != null)
				this.locationManager.removeUpdates(this);
		}

		@Override
		public void update(Observable observable, Object data) {
			if(observable instanceof WifiThread) {
				/* this.currentBSSID = ((WifiThread) observable).gBssids;
				if (this.previousBSSID != null) {
					Log.v("WiFiReceiver", ""+this.previousBSSID.size());
				}
				this.previousBSSID = this.currentBSSID; */
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			if(location != null) {
				prefsEditor.putLong("gpstime", location.getTime());
				prefsEditor.putString("gpslat", ""+location.getLatitude());
				prefsEditor.putString("gpslng", ""+location.getLongitude());
				prefsEditor.putString("gpsacc", ""+location.getAccuracy());
				prefsEditor.putString("gpsalt", ""+location.getAltitude());
				prefsEditor.commit(); 
				
				double lat = (double) (location.getLatitude());
			    double lng = (double) (location.getLongitude());
			    Long time = location.getTime();
			}
		    // Log.v("Coordinates", "Lat:"+lat+", Lng: "+lng+", TS: "+time);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	}

}
