package edu.ucsb.geog;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class Accelerometer extends TextView implements SensorEventListener
{

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	public Accelerometer(Context context, SensorManager mSensorManager) {
		super(context);
				
		this.mSensorManager = mSensorManager;
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);			
		
	}
	
	public void startRecording()
	{
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	public void stopRecording()
	{
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
		
		// TODO Auto-generated method stub
	    this.setText(event.values[0]+","+event.values[1]+","+event.values[2]);
	}

}
