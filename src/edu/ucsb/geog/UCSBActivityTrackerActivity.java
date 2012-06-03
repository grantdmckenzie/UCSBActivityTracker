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
import android.widget.TextView;

public class UCSBActivityTrackerActivity extends Activity implements Observer {

	private TextView mAccelerometerDisplay;
	private TextView mWifiDisplay;

	private SensorManager mSensorManager;
	private Accelerometer accelerometer;

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
	
	private HashMap fix;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// initiate display textviews
		mAccelerometerDisplay = (TextView)findViewById(R.id.accelerometerDisplay);
		mWifiDisplay = (TextView)findViewById(R.id.wifiDisplay);

		// initiate variables for accelerometer
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = new Accelerometer(mSensorManager, 500);
		accelerometer.addObserver(this);

		// initiate variables for wifi
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);       
		registerReceiver(wifiReceiver, filter);
		mWifiManager.startScan();


	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		accelerometer.startRecording();
		//
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		accelerometer.stopRecording();   
	}

	@Override
	public void update(Observable observable, Object data) 
	{
		if(observable instanceof Accelerometer)
		{
			fix  = ((Accelerometer)observable).getFix();
			mAccelerometerDisplay.setText("Accelerometer: "+fix.get("accelx")+" "+fix.get("accely")+" "+fix.get("accelz"));
			
		}
		
		
		
	}
}