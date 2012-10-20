package edu.ucsb.geog;

import java.util.Observer;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.IBinder;

public class AccelerAdvanced extends Service
{
	private Accelerometer accelerometer;
	private SensorManager mSensorManager;
	
	
	public void addObserver(Observer observer) {
		accelerometer.addObserver(observer);
	}

	public int countObservers() {
		return accelerometer.countObservers();
	}

	public void deleteObserver(Observer observer) {
		accelerometer.deleteObserver(observer);
	}

	public void deleteObservers() {
		accelerometer.deleteObservers();
	}

	public boolean equals(Object o) {
		return accelerometer.equals(o);
	}

	public JSONObject getFix() {
		return accelerometer.getFix();
	}

	public boolean hasChanged() {
		return accelerometer.hasChanged();
	}

	public int hashCode() {
		return accelerometer.hashCode();
	}

	public void notifyObservers() {
		accelerometer.notifyObservers();
	}

	public void notifyObservers(Object data) {
		accelerometer.notifyObservers(data);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		accelerometer.onAccuracyChanged(sensor, accuracy);
	}

	public void onSensorChanged(SensorEvent event) {
		accelerometer.onSensorChanged(event);
	}

	public void startRecording() {
		accelerometer.startRecording();
	}

	public void stopRecording() {
		accelerometer.stopRecording();
	}

	public void run() {
		accelerometer.run();
	}

	public String toString() {
		return accelerometer.toString();
	}

	public void onCreate() 
	{
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = new Accelerometer(mSensorManager, 30000); 		
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onStart(Intent intent, int startid)
	{
		
	}

}
