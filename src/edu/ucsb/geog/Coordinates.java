/*
 * @author: Grant McKenzie
 * @project: UCSBActivityTracker
 * @date: May 2012
 * UCSB Geography Department
 */

package edu.ucsb.geog;
import java.util.HashMap;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class Coordinates extends Observable implements Runnable, Fix {
	private double latitude;
	private double longitude;
	private double timestamp;
	private JSONObject fix;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private boolean running = true;
	
	public Coordinates(LocationManager locationManager) {
		
		this.locationManager = locationManager;
		this.locationListener = new MyLocationListener();
	}
	
	public void startRecording()
	{
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		running = true;
	}
	
	public void stopRecording()
	{
		locationManager.removeUpdates(locationListener);
		running = false;
	}

	
	public class MyLocationListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			
			Long l = new Long(System.currentTimeMillis()/1000);
			timestamp = l.doubleValue();
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
			
			// When the location changes, add the new location to the fix object
			
			try {
				fix =  new JSONObject();
				fix.put("sensor", 3.0);
				fix.put("lat", latitude);
				fix.put("lng", longitude);
				fix.put("ts", timestamp);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	
		@Override
		public void onProviderDisabled(String provider) {
			// Toast.makeText( getApplicationContext(),"Gps Disabled",Toast.LENGTH_SHORT ).show();
		}
	
		@Override
		public void onProviderEnabled(String provider) {
			// Toast.makeText( getApplicationContext(),"Gps Enabled",Toast.LENGTH_SHORT).show();
		}
	
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Toast.makeText( getApplicationContext(),"Location Service changed to "+provider,Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void run() {
		while(running)
		{		
			// after 60 seconds, send the fix back to the observer
			if (fix != null) {
				setChanged();
				notifyObservers(fix);
				try 
				{
					Thread.sleep(10000);  
				} 
				catch (InterruptedException ex) 
				{
					// nothing to see here.  Move along.
				}			
			} else {
				// Log.v("Coordinates", "Coordinates are null");
			}
		}
	}

	@Override
	public JSONObject getFix() {
		// Send that fix back yo
		return fix;
	}
}
