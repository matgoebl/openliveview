package nl.rnplus.olv.service;

import java.io.IOException;

import nl.rnplus.olv.content.ContentNotification;
import nl.rnplus.olv.content.manager.SMSNotificationManager;
import nl.rnplus.olv.data.LiveViewDbHelper;
import nl.rnplus.olv.messages.MessageConstants;
import nl.rnplus.olv.messages.calls.SetScreenMode;
import nl.rnplus.olv.messages.calls.SetVibrate;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	public final			String ACTION_RECEIVE_SMS	= "android.provider.Telephony.SMS_RECEIVED";
    public static final	String ACTION_START 		= "start";
    public static final	String ACTION_STOP 			= "stop";
    
    final public static String SHOW_NOTIFICATION = "OLV_ADD_NOTIFICATION";
    final public static String PLUGIN_COMMAND   = "nl.rnplus.olv.plugin.command";
    
    private LiveViewService myself;

    private LiveViewThread workerThread = null;
    //private LiveViewStandbyThread standbyThread = null;
    
    byte NotificationUnreadCount     = 0;
    byte NotificationTotalCount      = 0;
    String [] NotificationTitle     = new String [101];
    String [] NotificationContent   = new String [101];
    String [] NotificationType      = new String [101];
    long [] NotificationTime       = new long [101];
    Boolean NotificationNeedsUpdate = true;
    
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
    	for (int cid = 0; cid < 100; cid++)
    	{
    		NotificationContent[100-cid] = "Empty";
    		NotificationTitle[100-cid] = "Empty";
    	}
    	registerReceiver(notification_receiver,  new IntentFilter(SHOW_NOTIFICATION));
    	registerReceiver(notification_receiver,  new IntentFilter(ACTION_RECEIVE_SMS));
    	registerReceiver(plugin_receiver, new IntentFilter(PLUGIN_COMMAND));
    	
    	/* Added in version 1.0.0.3: Mediaplayer information receiver */
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
        return NotificationContent[id];
    }

    public void setNotificationContent(int id, String NotificationContents)
    {
        this.NotificationContent[id] = NotificationContents;
    }
    
    public String getNotificationTitle(int id)
    {
        return NotificationTitle[id];
    }

    public void setNotificationTitle(int id, String NotificationTitleVal)
    {
        this.NotificationTitle[id] = NotificationTitleVal;
    } 
    
    public long getNotificationTime(int id) {
        return NotificationTime[id];
    }

    public void setNotificationTime(int id, int NotificationTimeVal)
    {
        this.NotificationTime[id] = NotificationTimeVal;
    }    

    public String getNotificationType(int id)
    {
        return NotificationType[id];
    }

    public void setNotificationType(int id, String NotificationTypeVal)
    {
        this.NotificationType[id] = NotificationTypeVal;
    }    
    
    public Boolean getNotificationNeedsUpdate()
    {
        return NotificationNeedsUpdate;
    }

    public void setNotificationNeedsUpdate(Boolean NotificationNeedsUpdate)
    {
        this.NotificationNeedsUpdate = NotificationNeedsUpdate;
    }

    public int getNotificationUnreadCount()
    {
        return NotificationUnreadCount;
    }

    public void setNotificationUnreadCount(byte NotificationUnreadCount)
    {
        this.NotificationUnreadCount = NotificationUnreadCount;
    }
    
    public int getNotificationTotalCount()
    {
        return NotificationTotalCount;
    }

    public void setNotificationTotalCount(byte NotificationTotalCount)
    {
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
        		NotificationType[100-cid] = NotificationType[99-cid];
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
            	NotificationType[0]= "SMS";
        	}
        	else
        	{
	        	NotificationContent[0] = intent.getExtras().getString("contents");
	        	NotificationTitle[0] = intent.getExtras().getString("title");
	        	NotificationTime[0] = intent.getExtras().getLong("timestamp");
	        	NotificationType[0]= "GENERIC";
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
}
