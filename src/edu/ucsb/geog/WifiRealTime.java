package edu.ucsb.geog;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.net.wifi.*;
import android.content.*;
import java.util.*;
public class WifiRealTime extends Activity {

    /** Called when the activity is first created. */
 
private TextView tv;
private WifiManager wifi;
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {
      @Override
      public void onReceive(Context ctx, Intent intent)
      {
          List <ScanResult> scanresults = wifi.getScanResults();
          tv.setText("\nThe number of available wifi networks is: "+scanresults.size());
          tv.append("\n'Wifi Name'\t'Wifi signal'");
          for (ScanResult sr: scanresults)
          {
           tv.append("\n"+sr.SSID);
           tv.append("\t"+sr.level);
          }
              
       }
    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv = new TextView(this);
        setContentView(tv);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, filter);
        wifi.startScan();
    }

}