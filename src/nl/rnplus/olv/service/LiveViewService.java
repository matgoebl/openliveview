package nl.rnplus.olv.service;

import java.io.IOException;

import nl.rnplus.olv.content.ContentNotification;
import nl.rnplus.olv.content.manager.SMSNotificationManager;
import nl.rnplus.olv.messages.MessageConstants;
import nl.rnplus.olv.messages.calls.SetScreenMode;
import nl.rnplus.olv.messages.calls.SetVibrate;
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
 **/
public class LiveViewService extends Service
{

	public final			String ACTION_RECEIVE_SMS	= "android.provider.Telephony.SMS_RECEIVED";
    public static final	String ACTION_START 		= "start";
    public static final	String ACTION_STOP 			= "stop";
    
    final public static String SHOW_NOTIFICATION = "OLV_ADD_NOTIFICATION";
    final public static String STANDBY_COMMAND   = "OLV_STANDBY_COMMAND";

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
    BroadcastReceiver standby_receiver = new StandbyCommandReceiver();

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
    	for (int cid = 0; cid < 100; cid++)
    	{
    		NotificationContent[100-cid] = "Empty";
    		NotificationTitle[100-cid] = "Empty";
    	}
    	registerReceiver(notification_receiver,  new IntentFilter(SHOW_NOTIFICATION));
    	registerReceiver(notification_receiver,  new IntentFilter(ACTION_RECEIVE_SMS));
    	registerReceiver(standby_receiver, new IntentFilter(STANDBY_COMMAND));
    	
    	/* Added in version 1.0.0.3: Mediaplayer information receiver */
    	IntentFilter mediaintentfilter = new IntentFilter();
    	mediaintentfilter.addAction("com.android.music.metachanged");
    	mediaintentfilter.addAction("com.android.music.playstatechanged");
    	mediaintentfilter.addAction("com.android.music.playbackcomplete");
    	mediaintentfilter.addAction("com.android.music.queuechanged");
    	registerReceiver(media_receiver, mediaintentfilter);
    	
    }
    
    @Override
    public void onDestroy()
    {    
    	unregisterReceiver(notification_receiver);
    	unregisterReceiver(media_receiver);
    	unregisterReceiver(standby_receiver);
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
        /*if (standbyThread == null || !standbyThread.isLooping())
        {
        	standbyThread = new LiveViewStandbyThread(this);
        	standbyThread.start();
        }*/
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
        /*if (standbyThread != null && standbyThread.isAlive())
        {
            standbyThread.stopLoop();
        }*/
        stopSelf();
    }
    
    
    //byte NotificationUnreadCount     = 0;
    //byte NotificationTotalCount      = 0;
    //String [] NotificationTitle     = new String [101];
    //String [] NotificationContent   = new String [101];
    //Boolean NotificationNeedsUpdate = true;

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
    	String action = intent.getAction();
    	String cmd = intent.getStringExtra("command");
    	//Log.d("mIntentReceiver.onReceive ", action + " / " + cmd);
    	MediaInfoArtist = intent.getStringExtra("artist");
    	MediaInfoAlbum = intent.getStringExtra("album");
    	MediaInfoTrack = intent.getStringExtra("track");
    	MediaInfoNeedsUpdate = true;
    	Log.d("Music",MediaInfoArtist+":"+MediaInfoAlbum+":"+MediaInfoTrack);
    	}
    	};
    	
        public class StandbyCommandReceiver extends BroadcastReceiver  
        {  
            @Override  
            public void onReceive(Context context, Intent intent)  
            {  
            	if (workerThread.getLiveViewStatus()==0)
            	{
	            	//if (intent.getAction().equals(ACTION_RECEIVE_SMS))
		    		try
		    		{
		    			Log.w("DEBUG", intent.getExtras().getString("command"));
		    			if (intent.getExtras().getString("command").contains("vibrate"))
		    			{
		    				Log.w("DEBUG", "DID VIBRATE");
		    				int vdelay = intent.getExtras().getInt("delay");
		    				int vtime = intent.getExtras().getInt("time");
		    				workerThread.sendCall(new SetVibrate(vdelay, vtime));
		    			}
		    			else
		    			{
		    				if (intent.getExtras().getString("command").contains("awaken"))
		    				{
		    					Log.w("DEBUG", "Awaken.");
		    					workerThread.sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_MAX));
		    					workerThread.draw_battery_status();
		    				}
		    				else
		    				{
		    					Log.w("DEBUG", "Unknown command.");
		    				}
		    			}
		    		}
		    		catch (IOException e)
		            {
		                Log.e("StandbyCommandReceiver", "ERROR: " + e.getMessage());
		                return;
		            }
            	}
            	Log.w("StandbyCommandReceiver", "Executed command.");
            }  
        }      	
}
