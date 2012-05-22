package edu.ucsb.geog;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;

public class UCSBActivityTrackerActivity extends Activity {
	
	
	private SensorManager mSensorManager; 
	private Accelerometer accelerometer;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = new Accelerometer(this, mSensorManager);
        
        setContentView(accelerometer);
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	accelerometer.startRecording();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	accelerometer.stopRecording();
    	
    }
}