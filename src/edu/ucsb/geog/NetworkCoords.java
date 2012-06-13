/*
 * @author: Grant McKenzie
 * @project: UCSBActivityTracker
 * @date: May 2012
 * UCSB Geography Department
 */

package edu.ucsb.geog;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class NetworkCoords extends Observable implements Runnable, Fix {
	private double latitude;
	private double longitude;
	private double prevlat;
	private double prevlng;
	private double timestamp;
	private double accuracy;
	private double speed;
	private double altitude;
	private JSONObject fix;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private boolean running = true;
	
	public NetworkCoords(LocationManager locationManager) {
		
		this.locationManager = locationManager;
		this.locationListener = new MyLocationListener();
		prevlat = 0;
		prevlng = 0;
	}
	
	public void startRecording()
	{
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, locationListener);
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
			accuracy = loc.getAccuracy();
			speed = loc.getSpeed();
			altitude = loc.getAltitude();

			// When the location changes, add the new location to the fix object
			
			try {
				fix =  new JSONObject();
				fix.put("sensor", 4);
				fix.put("lat", latitude);
				fix.put("lng", longitude);
				fix.put("ts", timestamp);
				fix.put("accuracy", accuracy);
				fix.put("speed", speed);
				fix.put("altitude",altitude);
				
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
			if (fix != null && latitude != prevlat && longitude != prevlng) {
				setChanged();
				notifyObservers(fix);
				try 
				{
					prevlat = latitude;
					prevlng = longitude;
					Thread.sleep(60000);  
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
