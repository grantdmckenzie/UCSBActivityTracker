package edu.ucsb.geog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.json.JSONObject;

import android.R.anim;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;

public class AccelCalibration implements SensorEventListener
{
	private static final String APP_SHARED_PREFS = "edu.ucsb.geog";
    private SharedPreferences appSharedPrefs;
    private Editor prefsEditor;
    
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Vector<JSONObject> fixVector;
	private JSONObject fix;
	private double accelx = 0;
	private double accely = 0;
	private double accelz = 0;
	private long timestamp;
	private TextView textView;
	private TextView textViewSD;
	private Button calibrationButton;
	private boolean firstFlag = false;
	private int count = 0;
	private BurstSD callibrationSD = null;
	private double avgSD = 0;
	private double mean = 0;

	public AccelCalibration(SensorManager mSensorManager, TextView textView, TextView textViewSD, Button calibrationButton, Context context) 
	{
		this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, UCSBActivityTrackerActivity.MODE_WORLD_READABLE);
        this.prefsEditor = appSharedPrefs.edit();
        
		this.mSensorManager = mSensorManager;
		this.textView = textView;
		this.textViewSD = textViewSD;
		this.calibrationButton = calibrationButton;
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		fixVector = new Vector<JSONObject>();
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
	public double getCallibrationSD() {
		return this.avgSD;
	}
	
	public void startCaliberation()
	{
		this.count = 0;
		this.mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);	
	}
	
	public void stopCaliberation()
	{
		this.mSensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
		
		try 
		{
			accelx = event.values[0];
			accely = event.values[1];
			accelz = event.values[2];	
			timestamp = new Long(System.currentTimeMillis()/1000);
			
			this.textView.setText("x:"+accelx+"\ny:"+accely+"\nz:"+accelz);
			this.textViewSD.setText("Calculating Standard Deviation...");
			
			fix = new JSONObject();
			fix.put("sensor", 1.0);
			fix.put("accelx", accelx);
			fix.put("accely", accely);
			fix.put("accelz", accelz);
			fix.put("ts", timestamp);
			
			fixVector.add(fix);
			int size = fixVector.size();
			
			// Log.v("Vector Size", "Vector Size: "+ size);
			if(size >= 60)
			{

					// Create new BURSTSD object and calculate the Standard Deviation
					this.callibrationSD = new BurstSD(fixVector);
					this.avgSD = this.callibrationSD.getSD();
					this.mean = this.callibrationSD.getMean();
					// double lowerbound = this.mean - (this.avgSD * 4);
					// double upperbound = this.mean + (this.avgSD * 4);
					
					
					// prefsEditor.putFloat("callibrationLB", (float) lowerbound);
					// prefsEditor.putFloat("callibrationUB", (float) upperbound);
					prefsEditor.putFloat("callibrationSD", (float) this.avgSD);
					//this.textViewSD.setText("Callibration SD: "+this.avgSD + "\nCallibration Mean: " + this.mean + "\nCallibration LB: " + lowerbound + "\nCallibration UB: " + upperbound);
					this.textViewSD.setText("Callibration SD: "+this.avgSD + "\nCallibration Mean: " + this.mean);
					
			        prefsEditor.commit();
			        
					this.mSensorManager.unregisterListener(this);
					this.calibrationButton.setEnabled(true);
					this.calibrationButton.setText("Start Calibration");
					this.textView.setText("Calibration complete");

			}
			//Log.v("acceleration", fix.getDouble("accelx")+";"+fix.getDouble("accely")+";"+fix.getDouble("accelz"));			
			
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
		}	
	}
	
	
	private void writeToFile() {
		Vector<JSONObject> fixVector2 = fixVector;
		fixVector = new Vector<JSONObject>();
		
		File logFile = new File("sdcard/ucsbat_accel_caliberation.log");
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
	
	

}
