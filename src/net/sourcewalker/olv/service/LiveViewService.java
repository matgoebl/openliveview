package net.sourcewalker.olv.service;

import net.sourcewalker.olv.messages.calls.SetVibrate;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * This service hosts and controls the thread communicating with the LiveView
 * device.
 * 
 * @author Robert &lt;xperimental@solidproject.de&gt;
 */
public class LiveViewService extends Service {

    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    
    final public static String SHOW_NOTIFICATION = "SHOW_NOTIFICATION";

    private LiveViewThread workerThread = null;
    
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
    	registerReceiver(notification_receiver,  new IntentFilter(SHOW_NOTIFICATION));
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
    
    String NotificationContents = "";

    public String getNotificationContents() {
        return NotificationContents;
    }

    public void setNotificationContents(String NotificationContents) {
        this.NotificationContents = NotificationContents;
    }
    
    Boolean NotificationNeedsUpdate = true;

    public Boolean getNotificationNeedsUpdate() {
        return NotificationNeedsUpdate;
    }

    public void setNotificationNeedsUpdate(Boolean NotificationNeedsUpdate) {
        this.NotificationNeedsUpdate = NotificationNeedsUpdate;
    }
    
    int NotificationUnreadCount = 0;

    public int getNotificationUnreadCount() {
        return NotificationUnreadCount;
    }

    public void setNotificationReady(int NotificationUnreadCount) {
        this.NotificationUnreadCount = NotificationUnreadCount;
    }
    
    public class ShowNotificationReceiver extends BroadcastReceiver  
    {  
        @Override  
        public void onReceive(Context context, Intent intent)  
        {  
        	NotificationNeedsUpdate = true;
        	NotificationUnreadCount += 1;
        	NotificationContents += intent.getExtras().getString("contents") + (char)10;
        	Log.w("ShowNotificationReceiver", "Unread:" + NotificationUnreadCount + " Text: " + NotificationContents);
        }  
    }    
/*
    Intent i = new Intent(passing.this, received.class);
    Bundle b = new Bundle();
    b.putString("keyvalue", "yourprefixvalue");
    i.putExtras(b);
*/
    
}
