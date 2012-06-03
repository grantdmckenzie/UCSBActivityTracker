package edu.ucsb.geog;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class UCSBActivityTrackerActivity extends Activity implements Observer {

	//declare widgets
	private TextView mAccelerometerDisplay;
	private TextView mWifiDisplay;

	//declare variables for accelerometer
	private SensorManager mSensorManager;
	private Accelerometer accelerometer;
	private Thread accelThread;
	private Handler accelHandler = null;

	//declare variables for WIFI
	private WifiManager mWifiManager;
	private BroadcastReceiver wifiReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context ctx, Intent intent)
		{
			List <ScanResult> scanresults = mWifiManager.getScanResults();
			mWifiDisplay.setText("\nThe number of available wifi networks is: "+scanresults.size());
			mWifiDisplay.append("\n'Wifi Name'\t'Wifi signal'");
			for (ScanResult sr: scanresults)
			{
				mWifiDisplay.append("\n"+sr.SSID);
				mWifiDisplay.append("\t"+sr.level);
			}

		}
	};
	
	//declare a hashmap to store the values from sensors
	private HashMap fix;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// initiate GUI
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		
		// initiate display textviews
		mAccelerometerDisplay = (TextView)findViewById(R.id.accelerometerDisplay);
		mWifiDisplay = (TextView)findViewById(R.id.wifiDisplay);

		
	
		// initiate variables for accelerometer
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = new Accelerometer(mSensorManager, 5000); // the rate for accelerometer is 5 sec
		accelerometer.addObserver(this);
		accelThread = new Thread(accelerometer);
		// we will not be able to directly change the value of textview in another thread
	    // so I used the handler mode
		accelHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String value = msg.getData().getString("value");
				mAccelerometerDisplay.setText(value);
			}

		};  
		
		
		
		// initiate variables for wifi
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);       
		registerReceiver(wifiReceiver, filter);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		//
	}
	
	@Override
	protected void onStart() 
	{
		// TODO Auto-generated method stub
		super.onStart();
		accelerometer.startRecording();
		accelThread.start();
		mWifiManager.startScan();
	}
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}
	
	@Override
	protected void onStop() 
	{
		// TODO Auto-generated method stub
		super.onStop();
		accelerometer.stopRecording();  
		accelThread.stop();
		unregisterReceiver(wifiReceiver);
	}


	@Override
	public void update(Observable observable, Object data) 
	{
		// use fix to handle the data from all sensors
		fix  = (HashMap)data;
		
		// if the values come from accelerometer do the following actions
		if(observable instanceof Accelerometer)
		{		
			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putCharSequence("value", "Accelerometer: x:"+fix.get("accelx")+", y:"+fix.get("accely")+", z:"+fix.get("accelz"));
			message.setData(bundle);
			accelHandler.sendMessage(message);
		}	
	}
}