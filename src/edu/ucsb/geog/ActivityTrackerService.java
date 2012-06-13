package edu.ucsb.geog;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class ActivityTrackerService extends Service implements Observer {

	private SensorManager mSensorManager;
	private Accelerometer accelerometer;
	private Coordinates coordinate;
	private LocationManager locationManager;
	private Vector<JSONObject> fixVector;
	private JSONObject fix;
	private WifiManager wifiManager;
	private Wifi wifi;
	private Thread wifithread;
	private Thread accelThread;
	private Thread coordthread;
	private Thread vectorthread;
	private boolean running = true;
	private String URL = "http://geogrant.com/UCSB/ucsbactivitytracker/insert.php";
	private TelephonyManager tm;
	private ConnectivityManager connectivity;
    private String deviceId;
    private NotificationManager mNM;
    
	private int NOTIFICATION = R.string.local_service_started;
	
	
	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		showNotification();
		
		fixVector = new Vector<JSONObject>();
		
		// For defining unique device id
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    deviceId = deviceUuid.toString();

		// Accelerometer
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = new Accelerometer(mSensorManager, 5000); // the rate for accelerometer is 5 sec
		accelerometer.addObserver(this);
		accelThread = new Thread(accelerometer);

		// Coordinates (GPS)
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		coordinate = new Coordinates(locationManager);
		coordinate.addObserver(this);
		coordthread = new Thread(coordinate);
		
		// Wi-Fi
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi = new Wifi(wifiManager, 10000);
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifi.addObserver(this);   
        wifithread = new Thread(wifi);
        
        // Start runnable thread for sending fixes to database
        vectorthread = new Thread() {
		    @Override
		    public void run() {
		    	Looper.prepare();
		    	while(running) {
					if(fixVector.size() == 100) {
				        Log.v("Size match", fixVector.size()+"");
				        running = false;
				        serializeFixVector();
				    }
				}
		    	Looper.loop();
		    }
		};
		
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		accelThread.start();
		wifithread.start();
		wifi.startRecording();
		coordinate.startRecording();
		coordthread.start();
		vectorthread.start();
	}
	
	@Override
	public void onDestroy() {
		// mNM.cancel(NOTIFICATION);
		accelerometer.running = false;
		accelThread = null;
		wifithread = null;
		wifi.stopRecording();
		coordinate.stopRecording();
		coordthread = null;
		vectorthread = null;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Observable observable, Object data) {
		// use fix to handle the data from all sensors
		fix  = new JSONObject();

		if(observable instanceof Accelerometer)
		{		
			fix = accelerometer.getFix();  
		}	
		else if(observable instanceof Coordinates) {
			fix = coordinate.getFix();
			// Log.v("Coordinates", "Coordinates");
		} 
		else if (observable instanceof Wifi){
			fix = wifi.getFix();
		}
		
		// Add the fix to the vector
		fixVector.add(fix);
		Log.v("Vector Size", "Vector Size: "+fixVector.size());
	}
	
	private void serializeFixVector() {
		// Create duplicate vector
		Vector<JSONObject> fixVector2 = fixVector;
		
		// empty original bucket
		fixVector = new Vector<JSONObject>();
		
		// Post data to database along with device id
		DefaultHttpClient hc=new DefaultHttpClient();  
		ResponseHandler <String> res=new BasicResponseHandler();  
        try{
			HttpPost postMethod=new HttpPost(URL);  
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);    
			nameValuePairs.add(new BasicNameValuePair("data", fixVector2.toString()));    
			nameValuePairs.add(new BasicNameValuePair("devid", deviceId));    
			postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));    
			String response=hc.execute(postMethod,res);
			Toast.makeText( getApplicationContext(),"Database insert successful: "+response,Toast.LENGTH_SHORT ).show();
        }
        catch(Exception e){
            e.printStackTrace();
        }
		running = true;
	}

	private void showNotification() {
	      // In this sample, we'll use the same text for the ticker and the expanded notification
	      CharSequence text = getText(R.string.local_service_started);
	
	      // Set the icon, scrolling text and timestamp
	      Notification notification = new Notification(R.drawable.iconnotification, text, System.currentTimeMillis());
	
	      Intent notifyIntent = new Intent(Intent.ACTION_MAIN);
	      notifyIntent.setClass(getApplicationContext(), UCSBActivityTrackerActivity.class);
	      notifyIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	      
	      // The PendingIntent to launch our activity if the user selects this notification
	      PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	
	      // Set the info for the views that show in the notification panel.
	      notification.setLatestEventInfo(this, getText(R.string.local_service_label), text, contentIntent);
	
	      notification.flags|=Notification.FLAG_NO_CLEAR;
	      // Send the notification.
	      // mNM.notify(NOTIFICATION, notification);
	      startForeground(1337, notification);
	}
	
}
