/*
 * @author: Grant McKenzie
 * @project: UCSBActivityTracker
 * @date: May 2012
 * UCSB Geography Department
 */

package edu.ucsb.geog;
import java.util.HashMap;
import java.util.Observable;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class Coordinates extends Observable implements LocationListener, Runnable, Fix {
	private double latitude;
	private double longitude;
	private double timestamp;
	private HashMap<String, Double> fix;
	
	@Override
	public void onLocationChanged(Location loc) {
		Long l = new Long(System.currentTimeMillis()/1000);
		timestamp = l.doubleValue();
		latitude = loc.getLatitude();
		longitude = loc.getLongitude();
		
		// When the location changes, add the new location to the fix object
		fix =  new HashMap<String, Double>();
		fix.put("sensor", 3.0);
		fix.put("lat", latitude);
		fix.put("lng", longitude);
		fix.put("ts", timestamp);
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

	@Override
	public void run() {
		while(true)
		{		
			// after 60 seconds, send the fix back to the observer
			setChanged();
			notifyObservers(fix);
			try 
			{
				Thread.sleep(60000);             
			} 
			catch (InterruptedException ex) 
			{
				// nothing to see here.  Move along.
			}			
		}
	}

	@Override
	public HashMap<String, Double> getFix() {
		// Send that fix back yo
		return fix;
	}
}
