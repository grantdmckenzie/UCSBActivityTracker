package edu.ucsb.geog;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UCSBActivityTrackerActivity extends Activity implements OnClickListener {

	private Button buttonDoSomething;
	private Button buttonSendData;
	private Button buttonCalibrate;
	private TextView textCaliberation;
	private TextView textCaliberationSD;
	private SharedPreferences settings;
	private boolean trackeron;
	private int filenum;
	private Intent serviceIntent;
	private TelephonyManager tm;
	private ConnectivityManager connectivity;
	private String deviceId;
	private static final String PREFERENCE_NAME = "ucsbprefs";
	
	private PowerManager powerManager; 
    private WakeLock wakeLock; 
	
	private SensorManager mSensorManager;
	private AccelCalibration accelCalibrater = null;
	
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// initiate GUI
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		settings = getSharedPreferences(PREFERENCE_NAME, MODE_WORLD_READABLE);
		
		// For defining unique device id
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    deviceId = deviceUuid.toString();
		
		trackeron = settings.getBoolean("ucsb_tracker", false);
		filenum = settings.getInt("ucsb_filenum", 0);
		
		buttonDoSomething = (Button) findViewById(R.id.btn1);
		buttonDoSomething.setOnClickListener(this);
		
		buttonSendData = (Button) findViewById(R.id.btn2);
		buttonSendData.setOnClickListener(this);
		
		buttonCalibrate = (Button) findViewById(R.id.btn3);
		buttonCalibrate.setOnClickListener(this);
		
		textCaliberation = (TextView)findViewById(R.id.text);
		textCaliberationSD = (TextView)findViewById(R.id.textCalSD);
		
		//serviceIntent = new Intent(this, ActivityTrackerService.class);
		serviceIntent = new Intent(this, AccelService.class);
	    
	    if (trackeron) {
	    	buttonDoSomething.setText("Turn Tracker OFF");
	    } else {
	    	buttonDoSomething.setText("Turn Tracker ON");
	    }
	    
	    powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);

		
	    //loop();
	}
	
	 private void loop() {
//		while(true)
//		{
//			SystemClock.currentThreadTimeMillis()
//		}
		
	}

	@Override
	  protected void onPause() {
	      super.onPause();
	      saveState();
	  }
	  public void onSaveInstanceState(Bundle savedInstanceState) {
		  super.onSaveInstanceState(savedInstanceState);
		  saveState();
	  }
	 
	  @Override
	  public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    settings = PreferenceManager.getDefaultSharedPreferences(this);
	  }
	  private void saveState() {
		  SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, MODE_WORLD_READABLE);
		  SharedPreferences.Editor editor = preferences.edit(); 
		  editor.putBoolean("ucsb_tracker", trackeron);
		  editor.commit();
	  }

	@Override
	public void onClick(View src) {
		  SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, MODE_WORLD_READABLE);
		  SharedPreferences.Editor editor = preferences.edit(); 
		  if (src.getId() == R.id.btn1) {
			  buttonDoSomething.setEnabled(false);
			  if (!trackeron) {
				  	
//				Intent thisIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
//				PendingIntent recurringIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, thisIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//				AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//				alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(),
//				            60000, recurringIntent);
				  			  
				  	startService(serviceIntent);
					trackeron = true;
					buttonDoSomething.setText("Turn Tracker OFF");
			  } else {
				    stopService(serviceIntent);
					trackeron = false;
					buttonDoSomething.setText("Turn Tracker ON");
					
			  }
			  buttonDoSomething.setEnabled(true);
			  editor.putBoolean("ucsb_tracker", trackeron);
			  editor.commit();
		  } else if (src.getId() == R.id.btn2) {
			  buttonSendData.setEnabled(false);
			  new DownloadDataTask().execute();
		  }
		  else if(src.getId() == R.id.btn3)
		  {
			  if (buttonCalibrate.getText().equals("Start Calibration"))
			  {
				  if(accelCalibrater ==  null)
				  {
					  mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
					  accelCalibrater = new AccelCalibration(mSensorManager, textCaliberation, textCaliberationSD, buttonCalibrate, getApplicationContext());	    
				  }
				  
				  accelCalibrater.startCaliberation();				  
				  buttonCalibrate.setText("Calibrating...");
				  buttonCalibrate.setEnabled(false);
				
			  }
			  else 
			  {
				  accelCalibrater.stopCaliberation();
				  buttonCalibrate.setText("Start Calibration");
			  }
		  }
	}
	
	private class DownloadDataTask extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(UCSBActivityTrackerActivity.this);
	     
	     protected void onPreExecute() {
	    	 this.dialog.setTitle("UCSB Activity Tracker");
	    	 this.dialog.setMessage("Uploading Log File...");
	         this.dialog.show();
	         // Start writing to new file
	         SharedPreferences settings = getSharedPreferences(PREFERENCE_NAME, MODE_WORLD_READABLE);
 		     SharedPreferences.Editor editor = settings.edit(); 
 		     filenum++;
 		     editor.putInt("ucsb_filenum", filenum);
 		     editor.commit();
	     }

	     protected void onPostExecute(String response) {
	    	 if (response != "error") {
		    	 if(isNetworkAvailable()) {
				       try {
							// delete old file
				    	   int oldFileNum = filenum - 1;
				    	   String pathToOurFile = "/sdcard/ucsbat_"+deviceId+"-"+oldFileNum+".log";
				 		   File file = new File(pathToOurFile);
				 		   boolean deleted = file.delete();
				 		   
				       } catch(Exception e) {
				    	   // Log.v("tag", "test: "+e);
				    	   this.dialog.cancel();
						   errorDialog("Sorry, there was an error deleting the log file.  Please try again.");
					   }
				       this.dialog.cancel();
		    	 } else {
		    		 this.dialog.cancel();
					 errorDialog("Sorry, there was an error connecting to the database.  Please check your network connection and try again.");
		    	 }
	    	 } else {
	    		 this.dialog.cancel();
				 errorDialog("Sorry, there was an error connecting to the database.  Please check your network connection and try again.");
	    	 }
	    	 buttonSendData.setEnabled(true);
	     }

		@Override
		protected String doInBackground(String... d) {
			HttpURLConnection connection = null;
			DataOutputStream outputStream = null;
			int oldFileNum = filenum - 1;
			String pathToOurFile = "/sdcard/ucsbat_"+deviceId+"-"+oldFileNum+".log";
			Log.v("Path to file", "Path to file: "+pathToOurFile);
			String urlServer = "http://geogrant.com/UCSB/ucsbactivitytracker/receivefile.php";
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary =  "*****";

			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1*1024*1024;

			try {
				FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
	
				URL url = new URL(urlServer);
				connection = (HttpURLConnection) url.openConnection();
	
				// Allow Inputs & Outputs
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);
	
				// Enable POST method
				connection.setRequestMethod("POST");
	
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
	
				outputStream = new DataOutputStream( connection.getOutputStream() );
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
				outputStream.writeBytes(lineEnd);
	
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];
	
				// Read file
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	
				while (bytesRead > 0)
				{
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
	
				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	
				// Responses from the server (code and message)
				int serverResponseCode = connection.getResponseCode();
				String serverResponseMessage = connection.getResponseMessage();
	
				fileInputStream.close();
				outputStream.flush();
				outputStream.close();
				// Log.v("stuff", serverResponseCode+"");
				return serverResponseMessage;
			}
			catch (Exception ex)
			{
				Log.v("error", ex.getMessage());
				return ex.getMessage();
			//Exception handling
			}
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public void errorDialog(String msg) {
		AlertDialog.Builder adb=new AlertDialog.Builder(UCSBActivityTrackerActivity.this);
        adb.setTitle("UCSB GeoTracker");
        adb.setMessage(msg);
        adb.setNegativeButton("OK", new DialogInterface.OnClickListener() {  
  	      public void onClick(DialogInterface dialog, int which) {  
  	    	finish();
  	        return;  
  	   } });
        adb.show(); 
	}

}