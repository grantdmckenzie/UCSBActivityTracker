package edu.ucsb.geog;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class UCSBActivityTrackerActivity extends Activity implements OnClickListener {

	private Button buttonDoSomething;
	private SharedPreferences settings;
	private boolean trackeron;
	private Intent serviceIntent;
	
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// initiate GUI
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		settings = getPreferences(MODE_PRIVATE);
		
		trackeron = settings.getBoolean("ucsb_tracker", false);
		
		buttonDoSomething = (Button) findViewById(R.id.btn1);
		buttonDoSomething.setOnClickListener(this);
		
		serviceIntent = new Intent(this, ActivityTrackerService.class);
	    
	    if (trackeron) {
	    	buttonDoSomething.setText("Turn Tracker OFF");
	    } else {
	    	buttonDoSomething.setText("Turn Tracker ON");
	    }
		
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
		  SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		  SharedPreferences.Editor editor = preferences.edit(); 
		  editor.putBoolean("ucsb_tracker", trackeron);
		  editor.commit();
	  }

	@Override
	public void onClick(View src) {
		  if (src.getId() == R.id.btn1) {
			  buttonDoSomething.setEnabled(false);
			  if (!trackeron) {
				  	startService(serviceIntent);
					trackeron = true;
					buttonDoSomething.setText("Turn Tracker OFF");
			  } else {
				    stopService(serviceIntent);
					trackeron = false;
					buttonDoSomething.setText("Turn Tracker ON");
			  }
			  buttonDoSomething.setEnabled(true);
			  SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			  SharedPreferences.Editor editor = preferences.edit(); 
			  editor.putBoolean("ucsb_tracker", trackeron);
			  editor.commit();
		  } 
	}

	



}