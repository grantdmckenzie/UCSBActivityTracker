package edu.ucsb.geog;

import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class Accelerometer extends Observable implements SensorEventListener, Runnable, Fix
{

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private long msInterval; 
	private JSONObject fix;
	private double accelx = 0;
	private double accely = 0;
	private double accelz = 0;
	private long timestamp;
	public boolean running = true;
	

	public Accelerometer(SensorManager mSensorManager, long msInterval) {
					
		this.mSensorManager = mSensorManager;
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		this.msInterval = msInterval;
		fix =  new JSONObject();		
	}
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
		accelx = event.values[0];
		accely = event.values[1];
		accelz = event.values[2];	
		timestamp = new Long(System.currentTimeMillis()/1000);
		
		try 
		{
			fix.put("sensor", 1.0);
			fix.put("accelx", accelx);
			fix.put("accely", accely);
			fix.put("accelz", accelz);
			fix.put("ts", timestamp);
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		setChanged();
		notifyObservers(fix);
		
		mSensorManager.unregisterListener(this);
		
	}
	

	
	@Override
	public void run() 
	{		
		while(running)
		{
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
			try 
			{
				Thread.sleep(msInterval);             
			} 
			catch (InterruptedException ex) 
			{
			}			
		}		
	}

	@Override
	public JSONObject getFix() {
		return fix;
	}
}
