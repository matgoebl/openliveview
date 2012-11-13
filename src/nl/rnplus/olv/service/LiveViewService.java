package nl.rnplus.olv.service;

import java.io.IOException;

import nl.rnplus.olv.content.ContentNotification;
import nl.rnplus.olv.content.manager.SMSNotificationManager;
import nl.rnplus.olv.data.LiveViewData;
import nl.rnplus.olv.data.LiveViewDbConstants;
import nl.rnplus.olv.data.LiveViewDbHelper;
import nl.rnplus.olv.messages.MessageConstants;
import nl.rnplus.olv.messages.calls.SetScreenMode;
import nl.rnplus.olv.messages.calls.SetVibrate;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * This service hosts and controls the thread communicating with the LiveView
 * device.
 **/
public class LiveViewService extends Service
{
	private static final String TAG = "LiveViewService";
	public final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";
    public static final	String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    
    final public static String SHOW_NOTIFICATION = "OLV_ADD_NOTIFICATION";
    final public static String PLUGIN_COMMAND   = "nl.rnplus.olv.plugin.command";
    
    private LiveViewService myself;

    private LiveViewThread workerThread = null;
    
    Boolean NotificationNeedsUpdate = true;
    Cursor notification_cursor = null;    
    int contentcolumn = -1;
    int titlecolumn = -1;
    int typecolumn = -1;
    int timecolumn = -1;
    int readcolumn = -1;
    
    BroadcastReceiver notification_receiver = new ShowNotificationReceiver();
    BroadcastReceiver plugin_receiver = new PluginCommandReceiver();
    
    MyPhoneStateListener phoneListener=new MyPhoneStateListener();
    
    /*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
    @Override
    public void onCreate()
    {    
    	myself = this;
    	
    	try {
	    	notification_cursor = LiveViewDbHelper.getAllNotifications(this);
	    	for (int i = 0; i < notification_cursor.getColumnCount(); i++)
	    	{
	    		if (notification_cursor.getColumnName(i).contains(LiveViewData.Notifications.CONTENT))
	    		{
	    			contentcolumn = i;
	    		}
	    	}
	    	if (contentcolumn<0)
	    	{
	    		String message = "Database error in service: " + "Database corrupt, content column not found!";
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
	    	for (int i = 0; i < notification_cursor.getColumnCount(); i++)
	    	{
	    		if (notification_cursor.getColumnName(i).contains(LiveViewData.Notifications.TITLE))
	    		{
	    			titlecolumn = i;
	    		}
	    	}
	    	if (titlecolumn<0)
	    	{
	    		String message = "Database error in service: " + "Database corrupt, title column not found!";
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
	    	for (int i = 0; i < notification_cursor.getColumnCount(); i++)
	    	{
	    		if (notification_cursor.getColumnName(i).contains(LiveViewData.Notifications.TYPE))
	    		{
	    			typecolumn = i;
	    		}
	    	}
	    	if (typecolumn<0)
	    	{
	    		String message = "Database error in service: " + "Database corrupt, type column not found!";
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
	    	for (int i = 0; i < notification_cursor.getColumnCount(); i++)
	    	{
	    		if (notification_cursor.getColumnName(i).contains(LiveViewData.Notifications.TIMESTAMP))
	    		{
	    			timecolumn = i;
	    		}
	    	}
	    	if (timecolumn<0)
	    	{
	    		String message = "Database error in service: " + "Database corrupt, time column not found!";
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
	    	for (int i = 0; i < notification_cursor.getColumnCount(); i++)
	    	{
	    		if (notification_cursor.getColumnName(i).contains(LiveViewData.Notifications.READ))
	    		{
	    			readcolumn = i;
	    		}
	    	}
	    	if (readcolumn<0)
	    	{
	    		String message = "Database error in service: " + "Database corrupt, read column not found!";
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
    	}
    	
    	registerReceiver(notification_receiver,  new IntentFilter(SHOW_NOTIFICATION));
    	registerReceiver(notification_receiver,  new IntentFilter(ACTION_RECEIVE_SMS));
    	registerReceiver(plugin_receiver, new IntentFilter(PLUGIN_COMMAND));

    	IntentFilter mediaintentfilter = new IntentFilter();
    	mediaintentfilter.addAction("com.android.music.metachanged");
    	mediaintentfilter.addAction("com.android.music.playstatechanged");
    	mediaintentfilter.addAction("com.android.music.playbackcomplete");
    	mediaintentfilter.addAction("com.android.music.queuechanged");
    	registerReceiver(media_receiver, mediaintentfilter);
    	
    	TelephonyManager telephonyManager
    	=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    	telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    	
    }
    
    @Override
    public void onDestroy()
    {    
    	unregisterReceiver(notification_receiver);
    	unregisterReceiver(media_receiver);
    	unregisterReceiver(plugin_receiver);
    	try {
    		notification_cursor.close();
    	} catch(Exception e) {
    		String message = "Database error in service (onDestroy): " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
    	}
    	
    }    

    /*
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String action = intent.getAction();
        if (ACTION_START.equals(action))
        {
            startThread();
        } else if (ACTION_STOP.equals(action))
        {
            stopThread();
        }
        return START_NOT_STICKY;
    }

    /**
     * Starts the worker thread if the thread is not already running. If there
     * is a thread running that has already been stopped then a new thread is
     * started.
     */
    private void startThread()
    {
        if (workerThread == null || !workerThread.isLooping())
        {
            workerThread = new LiveViewThread(this);
            workerThread.start();
        }
    }

    /**
     * Signals the current worker thread to stop itself. If no worker thread is
     * available then nothing is done.
     */
    private void stopThread()
    {
        if (workerThread != null && workerThread.isAlive())
        {
            workerThread.stopLoop();
        }
        stopSelf();
    }
    
    public String getNotificationContent(int id)
    {
    	try {
	    	notification_cursor.moveToPosition(id);
	    	LiveViewDbHelper.setNotificationRead(myself, id);
	        return notification_cursor.getString(contentcolumn);
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
	        return "No data.";
    	}
    }
    
    public String getNotificationTitle(int id)
    {
    	try {
	    	notification_cursor.moveToPosition(id);
	        return notification_cursor.getString(titlecolumn);
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
	        return "No data.";
    	}
    }
    
    public long getNotificationTime(int id) {
    	try {
	    	notification_cursor.moveToPosition(id);
	        return notification_cursor.getLong(timecolumn);
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
	        return 0;
    	}
    }
 

    public int getNotificationType(int id)
    {
    	try {
	    	notification_cursor.moveToPosition(id);
	        return notification_cursor.getInt(typecolumn);
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
	        return LiveViewDbConstants.NTF_GENERIC;
    	}
    }

    
    /*
    public Boolean getNotificationNeedsUpdate()
    {
        return NotificationNeedsUpdate;
    }
    */

    /*
    public void setNotificationNeedsUpdate(Boolean NotificationNeedsUpdate)
    {
        this.NotificationNeedsUpdate = NotificationNeedsUpdate;
    }
    */

    public int getNotificationUnreadCount()
    {
    	refreshNotificationCursor();
    	int unreadCount = 0;
    	try {
			if (notification_cursor.getCount()>0)
			{
				notification_cursor.moveToFirst();
				String notificationcontents = "";
	    		for (int i = 0; i < notification_cursor.getCount(); i++)
		    	{
		    		if (notification_cursor.getInt(readcolumn)==0)
		    		{
		    			unreadCount++;
		    		}
		    		notification_cursor.moveToNext();
		    	}
		        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Stored notifications");
		        builder.setMessage(notificationcontents);
		        builder.setPositiveButton("Close", null);
		        builder.show();
			}
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
	        return LiveViewDbConstants.NTF_GENERIC;
    	}
        return unreadCount;
    }
    
    public int getNotificationTotalCount()
    {
    	refreshNotificationCursor();
        return notification_cursor.getCount();
    }
        
    public class ShowNotificationReceiver extends BroadcastReceiver  
    {  
        @Override  
        public void onReceive(Context context, Intent intent)  
        {  
        	NotificationNeedsUpdate = true;
        	if (intent.getAction().equals(ACTION_RECEIVE_SMS))
        	{
            	ContentNotification notification = SMSNotificationManager.getNotificationContent(context, intent);
            	LiveViewDbHelper.addNotification(myself, notification.getTitle(), notification.getContent(), LiveViewDbConstants.NTF_SMS, notification.getTimestamp());
        	}
        	else
        	{
            	LiveViewDbHelper.addNotification(myself, intent.getExtras().getString("title"), intent.getExtras().getString("contents"), intent.getExtras().getInt("type"),  intent.getExtras().getLong("timestamp"));
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
    
    /* Media receiver */
    
    boolean MediaInfoNeedsUpdate = false;
    String MediaInfoArtist = "";
    String MediaInfoTrack  = "";
    String MediaInfoAlbum  = "";
    
    public Boolean getMediaInfoNeedsUpdate()
    {
        return MediaInfoNeedsUpdate;
    }

    public void setMediaInfoNeedsUpdate(Boolean NotificationNeedsUpdate)
    {
        this.MediaInfoNeedsUpdate = NotificationNeedsUpdate;
    }
    
    public String getMediaInfoArtist()
    {
        return MediaInfoArtist;
    }
    
    public String getMediaInfoTrack()
    {
        return MediaInfoTrack;
    }
    
    public String getMediaInfoAlbum()
    {
        return MediaInfoAlbum;
    }
    
    private BroadcastReceiver media_receiver = new BroadcastReceiver()
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    	//String action = intent.getAction();
    	//String cmd = intent.getStringExtra("command");
    	//Log.d("mIntentReceiver.onReceive ", action + " / " + cmd);
    	MediaInfoArtist = intent.getStringExtra("artist");
    	MediaInfoAlbum = intent.getStringExtra("album");
    	MediaInfoTrack = intent.getStringExtra("track");
    	MediaInfoNeedsUpdate = true;
    	Log.d("OLV Music","Artist: "+MediaInfoArtist+", Album: "+MediaInfoAlbum+" and Track: "+MediaInfoTrack);
    	}
    	};
    	
        public class PluginCommandReceiver extends BroadcastReceiver  
        {  
            @Override  
            public void onReceive(Context context, Intent intent)  
            {  
            	Log.w("PLUGIN DEBUG", "Received intent, current LiveView status is: "+workerThread.getLiveViewStatus());
            	Log.w("PLUGIN DEBUG", "Command: "+intent.getExtras().getString("command"));
	    		try
	    		{
	    			switch (workerThread.getLiveViewStatus()){
	            		case MessageConstants.DEVICESTATUS_OFF:
	    		    			if (intent.getExtras().getString("command").contains("vibrate"))
	    		    			{
	    		    				Log.w("PLUGIN DEBUG", "Sent vibration.");
	    		    				int vdelay = intent.getExtras().getInt("delay");
	    		    				int vtime = intent.getExtras().getInt("time");
	    		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
	    		    			}
	    		    			else
	    		    			{
	    		    				if (intent.getExtras().getString("command").contains("awaken"))
	    		    				{
	    		    					Log.w("PLUGIN DEBUG", "Sent awaken. -->THIS COMMAND WILL BE CHANGED / REMOVED <--");
	    		    					workerThread.sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_MAX));
	    		    					workerThread.draw_plugin_loading();
	    		    				}
	    		    				else
	    		    				{
	    		                        String message = "Error: Plugin command receiver: Unknown command.";
	    		                        Log.e(TAG, message);
	    		                        LiveViewDbHelper.logMessage(myself, message);
	    		    	                return;
	    		    				}
	    		    			}
	            			break;
	            		case MessageConstants.DEVICESTATUS_ON:
    		    			if (intent.getExtras().getString("command").contains("vibrate"))
    		    			{
    		    				Log.w("PLUGIN DEBUG", "Sent vibration.");
    		    				int vdelay = intent.getExtras().getInt("delay");
    		    				int vtime = intent.getExtras().getInt("time");
    		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
    		    			}
	            			break;
	            		case MessageConstants.DEVICESTATUS_MENU:
    		    			if (intent.getExtras().getString("command").contains("vibrate"))
    		    			{
    		    				Log.w("PLUGIN DEBUG", "Sent vibration.");
    		    				int vdelay = intent.getExtras().getInt("delay");
    		    				int vtime = intent.getExtras().getInt("time");
    		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
    		    			}
	            			break;
	            		default:
	                        String message = "Error: Unknown device state!";
	                        Log.e(TAG, message);
	                        LiveViewDbHelper.logMessage(myself, message);
	            			break;
	            	}
	    		}
	    		catch (IOException e)
	            {
                    String message = "Error: IOException in plugin command receiver: " + e.getMessage();
                    Log.e(TAG, message);
                    LiveViewDbHelper.logMessage(myself, message);
                    e.printStackTrace();
	                return;
	            }
            }  
        }  
        
        public class MyPhoneStateListener extends PhoneStateListener {
        	Context context;
        	@Override
        	public void onCallStateChanged(int state,String incomingNumber){
	        	Log.e("PhoneCallStateNotified", "Incoming number "+incomingNumber);
	        	try {
		        	if (state == TelephonyManager.CALL_STATE_RINGING)
		        	{
		        		if (workerThread.getLiveViewStatus()==MessageConstants.DEVICESTATUS_OFF)
		        		{
								workerThread.sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_MAX));
								workerThread.drawCallStatus(state, "Incoming call", incomingNumber);
		        		}
		        	}
		        	if (state == TelephonyManager.CALL_STATE_IDLE)
		        	{
		        		Log.e("PhoneCallStateNotified", "Opgehangen / Geweigerd of gemist.");
		        	}
		        	if (state == TelephonyManager.CALL_STATE_OFFHOOK)
		        	{
		        		Log.e("PhoneCallStateNotified", "Opgenomen, er is een gesprek bezig.");
		        	} 
				} catch (IOException e) {
	                String message = "Error: IOException in incoming call receiver: " + e.getMessage();
	                Log.e(TAG, message);
	                LiveViewDbHelper.logMessage(myself, message);
					e.printStackTrace();
				}	
        	}
        } 
        
        public void refreshNotificationCursor(){
        	try {
        		notification_cursor.close();
	    	} catch(Exception e) {
	    		String message = "Database error in service (refresh: close): " + e.getMessage();
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
        	try {
        		notification_cursor = LiveViewDbHelper.getAllNotifications(myself);
	    	} catch(Exception e) {
	    		String message = "Database error in service (refresh: open): " + e.getMessage();
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
        }
}
