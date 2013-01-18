package edu.ucsb.geog;

import java.util.List;
import java.util.Observable;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

public class Accelerometer extends Observable implements SensorEventListener, Runnable, Fix
{

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private long msInterval; 
	private JSONObject fix;
	private Vector<JSONObject> fixes;
	private double accelx = 0;
	private double accely = 0;
	private double accelz = 0;
	private long timestamp;
	public boolean running = false;
	
	private long recordStartTime;
	
	private AccelService accelService;
	private float calibrationSD;
	private BurstSD burstSD;
	private double standardDeviation;	  
	private float sddif;
	
	

	public Accelerometer(SensorManager mSensorManager, long msInterval, AccelService accelService, float calibrationSD) {
					
		this.mSensorManager = mSensorManager;
		//mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//List<Sensor> accels = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		mAccelerometer = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
		fixes = new Vector<JSONObject>();
		this.msInterval = msInterval;
		this.accelService = accelService;
		this.calibrationSD = calibrationSD;
		
		fix =  new JSONObject();
		try
        {
			fix.put("accelx", 0.0);
			fix.put("accely", 0.0);
			fix.put("accelz", 0.0);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
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
			fix = new JSONObject();
			fix.put("sensor", 1.0);
			fix.put("accelx", accelx);
			fix.put("accely", accely);
			fix.put("accelz", accelz);
			fix.put("ts", timestamp);
			} 
		catch (Exception e) 
			{
				// TODO: handle exception
			}
		fixes.add(fix);
		
		
		if(fixes.size()==50)
  		{
  			mSensorManager.unregisterListener(this);
  			
  			// Calculate the Standard Deviation for the Burst
  			this.burstSD = new BurstSD(fixes);
			this.standardDeviation = this.burstSD.getSD();
			this.sddif = (float) this.standardDeviation - this.calibrationSD;
			// Log.v("Burst Standard Deviation", ""+ this.standardDeviation);
			Log.v("SD Difference", ""+this.sddif);
			// Log.v("CallibrationSD", ""+v);
			Log.v("fix size1:", ""+fixes.size());
//			this.accelService.setFixes(fixes);
//  			try 
//  			{
//  				this.accelService.writeToFile();
//			} 
//  			catch (JSONException e) 
//  			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
  		}
  			
		
		
		
		
		/*if(fixes.size()>=50){
			mSensorManager.unregisterListener(this);
			setChanged();
			notifyObservers(fixes);
			fixes = new Vector<JSONObject>();
			
			SystemClock.sleep(msInterval);
			
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		}*/
		
			//Log.v("acceleration", fix.getDouble("accelx")+";"+fix.getDouble("accely")+";"+fix.getDouble("accelz"));		
//			if((timestamp - recordStartTime)>=3)
//			{
//				mSensorManager.unregisterListener(this);
//			}

		
		
		
		/*accelx = event.values[0];
		accely = event.values[1];
		accelz = event.values[2];	
		timestamp = new Long(System.currentTimeMillis()/1000);
		try 
		{
			if(accelx!=fix.getDouble("accelx")||accely!=fix.getDouble("accely")||accelz!=fix.getDouble("accelz"))
			{
				fix = new JSONObject();
				fix.put("sensor", 1.0);
				fix.put("accelx", accelx);
				fix.put("accely", accely);
				fix.put("accelz", accelz);
				fix.put("ts", timestamp);
				//Log.v("acceleration", fix.getDouble("accelx")+";"+fix.getDouble("accely")+";"+fix.getDouble("accelz"));
				setChanged();
				notifyObservers(fix);
			}
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		mSensorManager.unregisterListener(this);*/
		
	}
	
	public void startRecording()
	{
		
		running = true;
	}
	
	public void stopRecording()
	{		
		running = false;
	}
	
	@Override
	public void run() 
	{		
		//while(running)
		//{
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
			//recordStartTime = new Long(System.currentTimeMillis()/1000);
			//try 
			//{
			//	Thread.sleep(msInterval);             
			//} 
			//catch (InterruptedException ex) 
			//{
			//}			
		//}		
	}

	@Override
	public JSONObject getFix() {
		return fix;
	}
	
	
	
}
