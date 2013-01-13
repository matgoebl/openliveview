package nl.rnplus.olv.service;

import java.io.IOException;

import nl.rnplus.olv.content.ContentNotification;
import nl.rnplus.olv.content.manager.SMSNotificationManager;
import nl.rnplus.olv.data.LiveViewData;
import nl.rnplus.olv.data.LiveViewDbConstants;
import nl.rnplus.olv.data.LiveViewDbHelper;
import nl.rnplus.olv.data.Prefs;
import nl.rnplus.olv.messages.MessageConstants;
import nl.rnplus.olv.messages.calls.SetLed;
import nl.rnplus.olv.messages.calls.SetScreenMode;
import nl.rnplus.olv.messages.calls.SetVibrate;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
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
    
    SQLiteDatabase globdb = null;
    
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
    		SQLiteDatabase db = LiveViewDbHelper.getReadableDb(this);
	    	notification_cursor = LiveViewDbHelper.getAllNotifications(this, db);
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
	    	LiveViewDbHelper.closeDb(db);
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
    
    public int getIdOfNotification(int notification, int type){
    	int id = -1;
    	int counter = 0;
    	Log.w("DEBUG", "getIdOfNotification");
    	refreshNotificationCursor();
    	if (type==LiveViewDbConstants.ALL_NOTIFICATIONS){
    		return notification;
    	}
    	try {
			if (notification_cursor.getCount()>0){
				notification_cursor.moveToFirst();
	    		for (int i = 0; i < notification_cursor.getCount(); i++){
	    			if (type==notification_cursor.getInt(typecolumn)){
	    				if (counter==notification)
	    				{
	    					id = notification_cursor.getPosition();
	    				}
	    				counter++;
	    			}
		    		notification_cursor.moveToNext();
		    	}
			} else {
				id = -1;
			}
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(myself, message);    
	        return LiveViewDbConstants.NTF_GENERIC;
    	}
    	return id;
    }
    
    public String getNotificationContent(int notification, int type)
    {
    	String content = "";
    	Log.d("DEBUG", "getNotificationContent (for notification "+notification+" of type "+type+")");
    	try {
    		int id = 0;
    		if (type==LiveViewDbConstants.ALL_NOTIFICATIONS)
    		{
    			id = notification;
    		}else{
    			id = getIdOfNotification(notification, type); 
    		}
    		LiveViewDbHelper.setAllNotificationsRead(myself, type);
    		if (id!=-1)
    		{
		    	notification_cursor.moveToPosition(id);
		        content = notification_cursor.getString(contentcolumn);
		        return content;
    		}else{
    			return "Could not find content.";
    		}
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
	        return "No data.";
    	}
    }
    
    public String getNotificationTitle(int notification, int type)
    {
    	Log.d("DEBUG", "getNotificationTitle (for notification "+notification+")");
    	try {
    		int id = 0;
    		if (type==LiveViewDbConstants.ALL_NOTIFICATIONS)
    		{
    			id = notification;
    		}else{
    			id = getIdOfNotification(notification, type); 
    		}
    		if (id!=-1)
    		{
		    	notification_cursor.moveToPosition(id);
		        return notification_cursor.getString(titlecolumn);
    		}else{
    			return "Could not find title.";
    		}
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
	        return "No data.";
    	}
    }
    
    public long getNotificationTime(int notification, int type) {
    	Log.d("DEBUG", "getNotificationTime (for notification: "+notification+")");
    	try {
    		int id = 0;
    		if (type==LiveViewDbConstants.ALL_NOTIFICATIONS)
    		{
    			id = notification;
    		}else{
    			id = getIdOfNotification(notification, type); 
    		}
    		if (id!=-1)
    		{
    			notification_cursor.moveToPosition(id);
		        return notification_cursor.getLong(timecolumn);
    		}else{
    			return 0;
    		}
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(this, message);    
	        return 0;
    	}
    }
 

    public int getNotificationType(int id)
    {
    	Log.d("DEBUG", "getNotificationType (for ID: "+id+")");
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

    public int getNotificationUnreadCount(int type)
    {
    	Log.w("DEBUG", "getNotificationUnreadCount");
    	refreshNotificationCursor();
    	int unreadCount = 0;
    	try {
			if (notification_cursor.getCount()>0){
				notification_cursor.moveToFirst();
	    		for (int i = 0; i < notification_cursor.getCount(); i++){
		    		if (notification_cursor.getInt(readcolumn)==0){
		    			if (type==LiveViewDbConstants.ALL_NOTIFICATIONS){
		    			    unreadCount++;
		    			}else{
		    				if (type==notification_cursor.getInt(typecolumn)){
		    					unreadCount++;
		    				}
		    			}
		    		}
		    		notification_cursor.moveToNext();
		    	}
			} else {
				unreadCount = 0;
			}
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(myself, message);    
	        return LiveViewDbConstants.NTF_GENERIC;
    	}
    	Log.w("DEBUG", "unreadCount = "+unreadCount);
        return unreadCount;
    }
    
    public int getNotificationTotalCount(int type)
    {
    	Log.w("DEBUG", "getNotificationTotalCount");
    	refreshNotificationCursor();
    	int totalCount = 0;
    	if (type==LiveViewDbConstants.ALL_NOTIFICATIONS){
    		return notification_cursor.getCount();
    	}
    	try {
			if (notification_cursor.getCount()>0){
				notification_cursor.moveToFirst();
	    		for (int i = 0; i < notification_cursor.getCount(); i++){
	    			if (type==LiveViewDbConstants.ALL_NOTIFICATIONS){
	    			    totalCount++;
	    			}else{
	    				if (type==notification_cursor.getInt(typecolumn)){
	    					totalCount++;
	    				}
	    			}
		    		notification_cursor.moveToNext();
		    	}
			} else {
				totalCount = 0;
			}
    	} catch(Exception e) {
    		String message = "Database error in service: " + e.getMessage();
	        Log.e(TAG, message);
	        LiveViewDbHelper.logMessage(myself, message);    
	        return LiveViewDbConstants.NTF_GENERIC;
    	}
    	Log.w("DEBUG", "totalCount = "+totalCount);
        return totalCount;
    }
    
    /* public int getNotificationTotalCount()
    {
    	Log.w("DEBUG", "getNotificationTotalCount (NO TYPE)");
    	refreshNotificationCursor();
        return notification_cursor.getCount();
    } */
        
    public class ShowNotificationReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
        	if (intent.getAction().equals(ACTION_RECEIVE_SMS)) {
            	NotificationNeedsUpdate = true;
            	ContentNotification notification = SMSNotificationManager.getNotificationContent(context, intent);
            	LiveViewDbHelper.addNotification(myself, notification.getTitle(), notification.getContent(), LiveViewDbConstants.NTF_SMS, notification.getTimestamp());
        	}
        	else {
        		String notificationContentFilter = (new Prefs(myself)).getNotificationFilter();  // Added by jkorp
            	if (!notificationContentFilter.contains(intent.getExtras().getString("contents"))) {
            		NotificationNeedsUpdate = true;
            		LiveViewDbHelper.addNotification(myself, intent.getExtras().getString("title"), intent.getExtras().getString("contents"), intent.getExtras().getInt("type"),  intent.getExtras().getLong("timestamp"));
            		Log.w("ShowNotificationReceiver", "Added new notification (1).");
            	}
            	else {
            		Log.w("ShowNotificationReceiver", "Notification not added because of filter (1). Content: "+intent.getExtras().getString("contents"));
            	}
            }
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
		    		    			if (intent.getExtras().getString("command").contains("notify"))
		    		    			{
		    		    				Log.w("PLUGIN DEBUG", "Sent vibration & blink.");
		    		    				int vdelay = intent.getExtras().getInt("delay");
		    		    				int vtime = intent.getExtras().getInt("time");
		    		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
		    		    				workerThread.sendCall(new SetLed(Color.GREEN, vdelay, vtime));
		    		    				if (intent.getExtras().getInt("displaynotification")==1)
		    		    				{
		    		    					String line1 = intent.getExtras().getString("line1");
		    		    					String line2 = intent.getExtras().getString("line2");
		    		    					if (line1==null) line1 = "";
		    		    					if (line2==null) line2 = "";
		    		    					int icon_type = intent.getExtras().getInt("icon_type");
		    		    					if (icon_type>2)
		    		    					{
		    		    						icon_type=2;
		    		    					}
		    		    					byte[] img = intent.getExtras().getByteArray("icon");
		    		    					workerThread.showNewAlert(line1, line2, icon_type, img);
		    		    				}
		    		    			}
		    		    			else
		    		    			{
		    		    				if (intent.getExtras().getString("command").contains("awaken"))
		    		    				{
		    		    					Log.w("PLUGIN DEBUG", "Sent awaken.");
		    		    					workerThread.sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_MAX));
		    		    					workerThread.openMenuFromStandby();
		    		    				}
		    		    				else
		    		    				{
		    		                        String message = "Error: Plugin command receiver: Unknown command.";
		    		                        Log.e(TAG, message);
		    		                        LiveViewDbHelper.logMessage(myself, message);
		    		    	                return;
		    		    				}
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
    		    			if (intent.getExtras().getString("command").contains("panel"))
    		    			{
    		    				Log.w("PLUGIN DEBUG", "Show panel.");
    		    				String top_string = intent.getExtras().getString("top_string");
    		    				String bottom_string = intent.getExtras().getString("bottom_string");
    		    				boolean isAlert = intent.getExtras().getBoolean("isAlert");
    		    				boolean useImage = intent.getExtras().getBoolean("useImage");
    		    				byte[] img = intent.getExtras().getByteArray("image");
    		    				workerThread.showPanel(top_string, bottom_string, isAlert, useImage, img);
    		    				Log.e("DEBUG", top_string);
    		    				Log.e("DEBUG", bottom_string);
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
        		try {
        		if (incomingNumber.length()>0) Log.d("PhoneCallStateNotified", "Incoming number "+incomingNumber);
		        	if (state == TelephonyManager.CALL_STATE_RINGING)
		        	{
		        		if (workerThread.getLiveViewStatus()==MessageConstants.DEVICESTATUS_OFF)
		        		{
								workerThread.showIncomingCallScreen(state, "Incoming call", getContactByAddr(myself, incomingNumber));
		        		}
		        		Log.d("PhoneCallStateNotified", "Status: RINGING");
		        	}
		        	if (state == TelephonyManager.CALL_STATE_IDLE)
		        	{
		        		Log.d("PhoneCallStateNotified", "Status: IDLE");
		        	}
		        	if (state == TelephonyManager.CALL_STATE_OFFHOOK)
		        	{
		        		Log.d("PhoneCallStateNotified", "Status: OFFHOOK");
		        	} 
				} catch (Exception e) {
	                String message = "Exception in incoming call receiver: " + e.getMessage();
	                Log.e(TAG, message);
	                LiveViewDbHelper.logMessage(myself, message);
					//e.printStackTrace();
				}	
        	}
        } 
        
        public void refreshNotificationCursor(){
        	Log.w("DEBUG", "refreshNotificationCursor");
        	try {
        		notification_cursor.close();
	    	} catch(Exception e) {
	    		String message = "Database error in service (refresh: close): " + e.getMessage();
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
    		try {
    			if (globdb!=null)
    			{
    				LiveViewDbHelper.closeDb(globdb);
    				Log.i(TAG, "Database closed!");
    			}
    		} catch(Exception e) {
    			Log.e(TAG, "Database could not be closed!");
    		}
        	try {
        		globdb = LiveViewDbHelper.getReadableDb(this);
        		notification_cursor = LiveViewDbHelper.getAllNotifications(myself, globdb);
	    	} catch(Exception e) {
	    		String message = "Database error in service (refresh: open): " + e.getMessage();
		        Log.e(TAG, message);
		        LiveViewDbHelper.logMessage(this, message);    
	    	}
        }
        
        
        private static String getContactByAddr(Context context, String phoneNumber) {
    		Uri personUri = null;
    		Cursor cur = null;

    		try {
    			personUri = Uri.withAppendedPath(
    					ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
    					phoneNumber);
    			cur = context.getContentResolver()
    					.query(personUri,
    							new String[] { PhoneLookup.DISPLAY_NAME }, null,
    							null, null);
    			if (cur.moveToFirst()) {
    				int nameIdx = cur.getColumnIndex(PhoneLookup.DISPLAY_NAME);
    				return cur.getString(nameIdx);
    			}
    			return phoneNumber;
    		} finally {
    			if (cur != null) {
    				cur.close();
    			}
    		}
    	}
}
