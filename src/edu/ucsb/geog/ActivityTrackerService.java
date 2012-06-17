package edu.ucsb.geog;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class ActivityTrackerService extends Service implements Observer {

	private SensorManager mSensorManager;
	private Accelerometer accelerometer;
	private Coordinates coordinate;
	private NetworkCoords networkcoords;
	private LocationManager locationManager;
	private LocationManager locationManager2;
	private Vector<JSONObject> fixVector;
	private JSONObject fix;
	private WifiManager wifiManager;
	private Wifi wifi;
	private Thread wifithread;
	private Thread accelThread;
	private Thread coordthread;
	private Thread vectorthread;
	private Thread networkthread;
	private boolean running = true;
	private String URL = "http://geogrant.com/UCSB/ucsbactivitytracker/insert.php";
	private TelephonyManager tm;
	private ConnectivityManager connectivity;
    private String deviceId;
    private NotificationManager mNM;
    private SharedPreferences settings;
	private int filenum;
	private static final String PREFERENCE_NAME = "ucsbprefs";
    
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
		
		// Coordinates (GPS)
		locationManager2 = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		networkcoords = new NetworkCoords(locationManager2);
		networkcoords.addObserver(this);
		networkthread = new Thread(networkcoords);
		
		// Wi-Fi
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi = new Wifi(wifiManager, 10000);
        // IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifi.addObserver(this);   
        wifithread = new Thread(wifi);
        
        // Start runnable thread for sending fixes to database
        vectorthread = new Thread() {
		    @Override
		    public void run() {
		    	Looper.prepare();
		    	while(running) {
					if(fixVector.size() == 8) {
				        Log.v("Size match", fixVector.size()+"");
				        running = false;
				        // serializeFixVector();
				        Long ts = new Long(System.currentTimeMillis()/1000);
				        writeToFile(ts);
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
		networkcoords.startRecording();
		networkthread.start();
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
		networkcoords.stopRecording();
		networkthread = null;
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
		} 
		else if (observable instanceof Wifi){
			fix = wifi.getFix();
		}
		else if (observable instanceof NetworkCoords){
			fix = networkcoords.getFix();
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
	
	private void writeToFile(Long ts) {
		Vector<JSONObject> fixVector2 = fixVector;
		fixVector = new Vector<JSONObject>();
		SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		filenum = settings.getInt("ucsb_filenum", 0);
		File logFile = new File("sdcard/ucsbat_"+deviceId+"-"+filenum+".log");
		Log.v("Path to file", "Path to file (service): "+logFile);
	   if (!logFile.exists()) {
	      try {
	         logFile.createNewFile();
	      } 
	      catch (IOException e) {
	         e.printStackTrace();
	      }
	   }
	   if (fixVector2.size() > 0) {
           try {
	    	   BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	           for (int i=0; i<fixVector2.size(); i++) {
	                   buf.append(fixVector2.get(i).toString());
	                   buf.newLine();
	           }
	 	       buf.close();    
            } catch (IOException e) {
                Log.e("TAG", "Could not write file " + e.getMessage());
            }
           running = true;
	   }
	}

}
