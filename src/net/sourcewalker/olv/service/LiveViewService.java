package net.sourcewalker.olv.service;

import net.sourcewalker.olv.content.ContentNotification;
import net.sourcewalker.olv.content.manager.SMSNotificationManager;
import net.sourcewalker.olv.messages.calls.SetVibrate;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * This service hosts and controls the thread communicating with the LiveView
 * device.
 * 
 * @author Robert (xperimental@solidproject.de);
 */
public class LiveViewService extends Service {

	public final			String ACTION_RECEIVE_SMS	= "android.provider.Telephony.SMS_RECEIVED";
    public static final	String ACTION_START 		= "start";
    public static final	String ACTION_STOP 			= "stop";
    
    final public static String SHOW_NOTIFICATION = "SHOW_NOTIFICATION";

    private LiveViewThread workerThread = null;
    
    byte NotificationUnreadCount     = 0;
    byte NotificationTotalCount      = 0;
    String [] NotificationTitle     = new String [101];
    String [] NotificationContent   = new String [101];
    long [] NotificationTime       = new long [101];
    Boolean NotificationNeedsUpdate = true;
    
    BroadcastReceiver notification_receiver = new ShowNotificationReceiver();

    /*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void onCreate() {    
    	for (int cid = 0; cid < 100; cid++)
    	{
    		NotificationContent[100-cid] = "Empty";
    		NotificationTitle[100-cid] = "Empty";
    	}
    	registerReceiver(notification_receiver,  new IntentFilter(SHOW_NOTIFICATION));
    	//IntentFilter intentFilter = new IntentFilter(android.Manifest.permission.RECEIVE_SMS);
    	IntentFilter intentFilter = new IntentFilter(ACTION_RECEIVE_SMS);
    	registerReceiver(notification_receiver,  intentFilter);
    }
    
    @Override
    public void onDestroy() {    
    	unregisterReceiver(notification_receiver);
    }    

    /*
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            startThread();
        } else if (ACTION_STOP.equals(action)) {
            stopThread();
        }
        return START_NOT_STICKY;
    }

    /**
     * Starts the worker thread if the thread is not already running. If there
     * is a thread running that has already been stopped then a new thread is
     * started.
     */
    private void startThread() {
        if (workerThread == null || !workerThread.isLooping()) {

            workerThread = new LiveViewThread(this);
            workerThread.start();
        }
    }

    /**
     * Signals the current worker thread to stop itself. If no worker thread is
     * available then nothing is done.
     */
    private void stopThread() {
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.stopLoop();
        }
        stopSelf();
    }
    
    
    //byte NotificationUnreadCount     = 0;
    //byte NotificationTotalCount      = 0;
    //String [] NotificationTitle     = new String [101];
    //String [] NotificationContent   = new String [101];
    //Boolean NotificationNeedsUpdate = true;

    public String getNotificationContent(int id) {
        return NotificationContent[id];
    }

    public void setNotificationContent(int id, String NotificationContents) {
        this.NotificationContent[id] = NotificationContents;
    }
    
    public String getNotificationTitle(int id) {
        return NotificationTitle[id];
    }

    public void setNotificationTitle(int id, String NotificationTitleVal) {
        this.NotificationTitle[id] = NotificationTitleVal;
    } 
    
    public long getNotificationTime(int id) {
        return NotificationTime[id];
    }

    public void setNotificationTime(int id, int NotificationTimeVal) {
        this.NotificationTime[id] = NotificationTimeVal;
    }    

    public Boolean getNotificationNeedsUpdate() {
        return NotificationNeedsUpdate;
    }

    public void setNotificationNeedsUpdate(Boolean NotificationNeedsUpdate) {
        this.NotificationNeedsUpdate = NotificationNeedsUpdate;
    }

    public int getNotificationUnreadCount() {
        return NotificationUnreadCount;
    }

    public void setNotificationUnreadCount(byte NotificationUnreadCount) {
        this.NotificationUnreadCount = NotificationUnreadCount;
    }
    
    public int getNotificationTotalCount() {
        return NotificationTotalCount;
    }

    public void setNotificationTotalCount(byte NotificationTotalCount) {
        this.NotificationTotalCount = NotificationTotalCount;
    }
    
    public class ShowNotificationReceiver extends BroadcastReceiver  
    {  
        @Override  
        public void onReceive(Context context, Intent intent)  
        {  
        	NotificationNeedsUpdate = true;
        	NotificationUnreadCount += 1;
        	for (int cid = 0; cid < 100; cid++)
        	{
        		NotificationContent[100-cid] = NotificationContent[99-cid];
        		NotificationTitle[100-cid] = NotificationTitle[99-cid];
        		NotificationTime[100-cid] = NotificationTime[99-cid];
        	}
        	if (NotificationTotalCount < 100)
        	{
        		NotificationTotalCount += 1;
        	}
        	if (intent.getAction().equals(ACTION_RECEIVE_SMS))
        	{
            	ContentNotification notification = SMSNotificationManager.getNotificationContent(context, intent);
            	NotificationContent[0] = notification.getContent();
            	NotificationTitle[0] = notification.getTitle();
            	NotificationTime[0]=notification.getTimestamp();
        	}
        	else
        	{
	        	NotificationContent[0] = intent.getExtras().getString("contents");
	        	NotificationTitle[0] = intent.getExtras().getString("title");
	        	NotificationTime[0] = intent.getExtras().getInt("timestamp");
        	}
        	Log.w("ShowNotificationReceiver", "Added new notification.");
        }  
    }    
/*
    Intent i = new Intent(passing.this, received.class);
    Bundle b = new Bundle();
    b.putString("keyvalue", "yourprefixvalue");
    i.putExtras(b);
*/
    
}
