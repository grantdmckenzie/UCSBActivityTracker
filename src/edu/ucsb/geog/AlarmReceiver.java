package edu.ucsb.geog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;

public class AlarmReceiver extends BroadcastReceiver 
{
	private static long msInterval = 10000;
	private AlarmManager alarmManager;

	public void onReceive(Context context, Intent intent) 
    {   
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        
        // Log.v("alarm test", "alrm");
        AcclThread acclThread = new AcclThread(context,wl);
        Thread thread = new Thread(acclThread);
        thread.start();
        wl.release();
              
    }

	public void SetAlarm(Context context)
	{
		if(alarmManager == null)
			alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent(context, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), msInterval, pi);
		
	}

	public void CancelAlarm(Context context)
	{
		if(alarmManager == null)
			alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmManager.cancel(sender);
	}
}
