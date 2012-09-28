package net.sourcewalker.olv.service;
/*
 * @author Robert (xperimental@solidproject.de);
 * This file has been changed by RN+ (Renze Nicolai)
 * Image file loader was changed by: basty149
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.sourcewalker.olv.LiveViewPreferences;
import net.sourcewalker.olv.R;
import net.sourcewalker.olv.data.LiveViewDbHelper;
import net.sourcewalker.olv.messages.DecodeException;
import net.sourcewalker.olv.messages.LiveViewCall;
import net.sourcewalker.olv.messages.LiveViewEvent;
import net.sourcewalker.olv.messages.MessageConstants;
import net.sourcewalker.olv.messages.UShort;
import net.sourcewalker.olv.messages.calls.CapsRequest;
import net.sourcewalker.olv.messages.calls.ClearDisplay;
import net.sourcewalker.olv.messages.calls.DeviceStatusAck;
import net.sourcewalker.olv.messages.calls.DisplayBitmap;
import net.sourcewalker.olv.messages.calls.DisplayText;
import net.sourcewalker.olv.messages.calls.GetAlertResponse;
import net.sourcewalker.olv.messages.calls.GetTimeResponse;
import net.sourcewalker.olv.messages.calls.MenuItem;
import net.sourcewalker.olv.messages.calls.MessageAck;
import net.sourcewalker.olv.messages.calls.NavigationResponse;
import net.sourcewalker.olv.messages.calls.SetLed;
import net.sourcewalker.olv.messages.calls.SetMenuSize;
import net.sourcewalker.olv.messages.calls.SetScreenMode;
import net.sourcewalker.olv.messages.calls.SetVibrate;
import net.sourcewalker.olv.messages.events.CapsResponse;
import net.sourcewalker.olv.messages.events.GetAlert;
import net.sourcewalker.olv.messages.events.Navigation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import net.sourcewalker.olv.data.Prefs;

public class LiveViewThread extends Thread {
	private ThreadLocal<DateFormat> sdf = new ThreadLocal<DateFormat>()
	{
		protected DateFormat initialValue()
		{
			return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, Locale.getDefault());
		};
	};

    private static final String TAG = "LiveViewThread";
    private static final UUID SERIAL = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int SERVICE_NOTIFY = 100;
    
    private final byte[] menuImage;
    private final byte[] menuImage_notification;
    private final byte[] menuImage_phone;  
    private final byte[] menuImage_music;
    private final byte[] menuImage_left;
    private final byte[] menuImage_right;
    private final byte[] menuImage_plus;
    private final byte[] menuImage_min;
    private final byte[] menuImage_media_isplaying;
    private final byte[] menuImage_media_isnotplaying;

    private final BluetoothAdapter btAdapter;
    private BluetoothServerSocket serverSocket;
    private long startUpTime;
    private LiveViewService parentService;
    private BluetoothSocket clientSocket;
    private Notification notification;
    
    private Integer menu_state = 0;
    private byte last_menu_id = 0;
    private byte alertId = 0;
    private Integer init_state = 0; 
    
    private byte menu_button_count = 5;
    
    private byte menu_button_notifications_id = -1;
    private byte menu_button_media_next_id = -1;
    private byte menu_button_media_play_id = -1;
    private byte menu_button_media_previous_id = -1;
    private byte menu_button_findphone_id = -1;
    
    public LiveViewThread(LiveViewService parentService) {
        super("LiveViewThread");
        this.parentService = parentService;
        
        menu_state = 0;
        init_state = 0;
                
        notification = new Notification(R.drawable.icon,
                "LiveView connected...", System.currentTimeMillis());
        Context context = parentService.getApplicationContext();
        CharSequence contentTitle = parentService.getString(R.string.app_name);
        CharSequence contentText = parentService
                .getString(R.string.notify_service_running);
        Intent notificationIntent = new Intent(parentService,
                LiveViewPreferences.class);
        PendingIntent contentIntent = PendingIntent.getActivity(parentService,
                0, notificationIntent, 0);
        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
                
        
        menuImage = loadImageByteArray(parentService, "menu_blank.png");
        menuImage_notification = loadImageByteArray(parentService, "menu_notification.png");
        menuImage_phone = loadImageByteArray(parentService, "menu_phone.png");
        menuImage_music = loadImageByteArray(parentService, "menu_music.png");
        menuImage_left = loadImageByteArray(parentService, "menu_left.png");
        menuImage_right = loadImageByteArray(parentService, "menu_right.png");
        menuImage_plus = loadImageByteArray(parentService, "menu_plus.png");
        menuImage_min = loadImageByteArray(parentService, "menu_min.png");
        menuImage_media_isplaying = loadImageByteArray(parentService, "jerry_music_play_icn.png");
        menuImage_media_isnotplaying = loadImageByteArray(parentService, "jerry_music_pause_icn.png");
        
        
        /* Testing code for dynamic menu */
        menu_button_count = 0;
        
        Prefs prefs = new Prefs(parentService);
        Boolean menu_show_find_my_phone = prefs.getmenushowfindmyphone();
        Boolean menu_show_notifications = prefs.getmenushownotifications();
        Boolean menu_show_media_next = prefs.getmenushowmedianext();
        Boolean menu_show_media_play = prefs.getmenushowmediaplay();
        Boolean menu_show_media_previous = prefs.getmenushowmediaprevious();
        if (menu_show_notifications)
        {        
	        menu_button_notifications_id = menu_button_count;
	        menu_button_count += 1;
        }        
        if (menu_show_media_next)
        {
	        menu_button_media_next_id = menu_button_count;
	        menu_button_count += 1;
        }
        if (menu_show_media_play)
        {
	        menu_button_media_play_id = menu_button_count;
	        menu_button_count += 1;
        }
        if (menu_show_media_previous)
        {
	        menu_button_media_previous_id = menu_button_count;
	        menu_button_count += 1;
        }
        if (menu_show_find_my_phone)
        {
        	menu_button_findphone_id = menu_button_count;
        	menu_button_count += 1;
        }
        
        menu_state = 0;
    }
    
    /**
     * This function was added by basty149
     * Return byte array for the supplied image file
     * @param parentService
     * @param imageFileName Image file name
     * @return byte[] for the image file
     */
	private byte[] loadImageByteArray(LiveViewService parentService, String imageFileName) {
		try {
			InputStream stream = parentService.getAssets().open(imageFileName);
            ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (stream.available() > 0) {
                int read = stream.read(buffer);
                arrayStream.write(buffer, 0, read);
            }
            stream.close();
            return arrayStream.toByteArray();
            //Log.d(TAG, "Icon size: " + menuImage.length);
        } catch (IOException e) {
            String message = "Error reading icon " + imageFileName + " : " + e.getMessage();
			Log.e(TAG, message);
            throw new RuntimeException(message, e);
        }
	}

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        parentService.startForeground(SERVICE_NOTIFY, notification);

        Log.d(TAG, "Starting LiveView thread.");
        startUpTime = System.currentTimeMillis();
        serverSocket = null;
        try {
            Log.d(TAG, "Starting server...");
            serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(
                    "LiveView", SERIAL);
        } catch (IOException e) {
            Log.e(TAG, "Error starting BT server: " + e.getMessage());
            return;
        }
        
        Prefs prefs = new Prefs(parentService);
        Boolean enable_timeout = prefs.getenabletimeout();
        
        try {
            Log.d(TAG, "Listening for LV...");
            clientSocket = serverSocket.accept();
            EventReader reader = new EventReader(clientSocket.getInputStream(), enable_timeout);
            // Single connect only
            serverSocket.close();
            serverSocket = null;
            Log.d(TAG, "LV connected.");
            sendCall(new CapsRequest());
            Log.d(TAG, "Message sent.");
            do {
                try {
                    LiveViewEvent response = reader.readMessage();
	                sendCall(new MessageAck(response.getId()));
	                Log.d(TAG, "Got message: " + response);
	                processEvent(response);
                } catch (DecodeException e) {
                	if (e.getMessage()=="timeout") //This works, but should be implemented in some other way
                	{
                		
                		/*
                		 * I added this very function to test
                		 * sending messages that are not requested by the liveview
                		 * Everything added here will be executed in a certain time interval on moments when the liveview is not communicating.
                		 */
                		
                    		Log.d(TAG, "Timeout triggered!");    
                    		
                    		//sendCall(new SetLed(Color.RED,1,500)); //Flash the led.
                			//Note: Executing the timeout also triggers the notification stuff. (look down)
                	}
                	else
                	{
                		Log.e(TAG, "Error decoding message: " + e.getMessage());
                	}
                }
                
                if (parentService.getNotificationNeedsUpdate()==true)
                {
                	
                	if (init_state == 1)
                	{
                		if (menu_state == 0)
                		{
                			if (menu_button_notifications_id>0)
                			{
			                	parentService.setNotificationNeedsUpdate(false);
			                	sendCall(new MenuItem((byte) menu_button_notifications_id, true, new UShort((short) (parentService.getNotificationUnreadCount())),
			                            "Notifications", menuImage_notification));
			                	if (parentService.getNotificationUnreadCount()>0)
			                	{
				                	sendCall(new SetLed(Color.GREEN,0,1000));
				                	sendCall(new SetVibrate(0, 200));
			                	}
			                	else
			                	{
			                		sendCall(new SetLed(Color.RED,0,1000));
			                	}
                			}
                		}
                	}
                } 
            } while (true);
        } catch (IOException e) {
            String msg = e.getMessage();
            if (!msg.contains("Connection timed out")) {
                Log.e(TAG, "Error communicating with LV: " + e.getMessage());
            }
        }
        Log.d(TAG, "Stopped LiveView thread.");

        // Log runtime
        long runtime = (System.currentTimeMillis() - startUpTime) / 1000;
        long runHour = runtime / 3600;
        runtime -= runHour * 3600;
        long runMinute = runtime / 60;
        runtime -= runMinute * 60;
        String message = String.format(
                "Service runtime: %d hours %d minutes %d seconds", runHour,
                runMinute, runtime);
        Log.d(TAG, message);
        LiveViewDbHelper.logMessage(parentService, message);

        // Stop surrounding service
        ((NotificationManager) parentService
                .getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(SERVICE_NOTIFY);
        parentService.stopForeground(true);
        parentService.stopSelf();
    }

    /**
     * Process a message that was sent by the LiveView device.
     * 
     * @param event
     *            Event sent by device.
     * @throws IOException
     */
        
    private void processEvent(LiveViewEvent event) throws IOException {
        Prefs prefs = new Prefs(parentService);
        Boolean enable_media_menu = prefs.getenablemediamenu();
    	//Log.d(TAG, "PROCESSEVENT: " + event.getId());
        switch (event.getId()) {
        case MessageConstants.MSG_GETCAPS_RESP:
            CapsResponse caps = (CapsResponse) event;
            Log.d(TAG, "LV capabilities: " + caps.toString());
            Log.d(TAG, "LV Version: " + caps.getSoftwareVersion());
            sendCall(new SetMenuSize((byte) menu_button_count)); //                          CHANGE THIS ROW TO GET MORE BUTTONS
            sendCall(new SetVibrate(0, 50));
            break;
        case MessageConstants.MSG_GETTIME:
            Log.d(TAG, "Sending current time...");
            sendCall(new GetTimeResponse());
            break;
        case MessageConstants.MSG_DEVICESTATUS:
            Log.d(TAG, "Acknowledging status.");
            sendCall(new DeviceStatusAck());
            break;
        case MessageConstants.MSG_GETMENUITEMS:
            Log.d(TAG, "Sending menu items...");
            if (menu_button_notifications_id>=0)
            {
	            sendCall(new MenuItem((byte) menu_button_notifications_id, true, new UShort((short) (parentService.getNotificationUnreadCount())),
	                    "Notifications", menuImage_notification));
            }
            if (menu_button_media_next_id>=0)
            {
	            sendCall(new MenuItem((byte) menu_button_media_next_id, false, new UShort((short) 0),
	                    "Next", menuImage_right));
            }
            if (menu_button_media_play_id>=0)
            {
	            sendCall(new MenuItem((byte) menu_button_media_play_id, false, new UShort((short) 0),
	                    "Play / Pause", menuImage_music));
            }
            if (menu_button_media_previous_id>=0)
            {
	            sendCall(new MenuItem((byte) menu_button_media_previous_id, false, new UShort((short) 0),
	                    "Previous", menuImage_left));
            }
            if (menu_button_findphone_id>=0)
            {
	            sendCall(new MenuItem((byte) menu_button_findphone_id, false, new UShort((short) 0),
	                    "Find my phone", menuImage_phone));
            }
            //sendCall(new MenuItem((byte) 5, true, new UShort((short) 4),
            //        "Alerts test", menuImage));
            menu_state = 0;
            init_state = 1; //enable notification receive code
            break;
        case MessageConstants.MSG_GETALERT:
        	GetAlert alert = (GetAlert) event;
            Log.d(TAG, "Alert triggered ("+alert.getMenuItemId()+", "+alert.getAlertAction()+", "+alert.getMaxBodySize()+")");
            if (last_menu_id!=alert.getMenuItemId())
            {
            	alertId = 0;
            }
            last_menu_id = alert.getMenuItemId();
            if (alert.getAlertAction()==MessageConstants.ALERTACTION_FIRST)
            {
            	alertId = 0;
            }
            if (alert.getAlertAction()==MessageConstants.ALERTACTION_LAST)
            {
            	if (last_menu_id == 0)
            	{
            		alertId = (byte) (parentService.getNotificationTotalCount()-1);
            	}
            	else
            	{
            		alertId = 0;
            	}
            }
            if (alert.getAlertAction()==MessageConstants.ALERTACTION_NEXT)
            {
            	alertId += 1;
            	if (last_menu_id == 0)
            	{
                	if (alertId > (byte) parentService.getNotificationTotalCount()-1)
                	{
                		alertId = 0;
                	}
            	}
            	else
            	{
            		alertId = 0;
            	}
            }
            if (alert.getAlertAction()==MessageConstants.ALERTACTION_PREV)
            {
            	alertId -= 1;
            	if (last_menu_id == 0)
            	{
	            	if (alertId < 0)
	            	{
	            		alertId = (byte) (parentService.getNotificationTotalCount()-1);
	            	}
            	}
            	else
            	{
            		alertId = 0;
            	}
            }                               
            if (last_menu_id==0) //Notifications (Date and time fixed by TpmKranz)
            {
            	Log.d(TAG, "Notifications alert (ID: "+alertId+") Time:"+parentService.getNotificationTime(alertId));
            	if (parentService.getNotificationTotalCount() > 0)
            	{
            		String notificationTimeString = sdf.get().format(new Date(parentService.getNotificationTime(alertId)));
            		sendCall(new GetAlertResponse((byte) parentService.getNotificationTotalCount(), (byte) parentService.getNotificationUnreadCount(), alertId, (String) notificationTimeString, (String) parentService.getNotificationTitle(alertId), (String) parentService.getNotificationContent(alertId), menuImage_notification));            	
            	}
            	else
            	{
	            	sendCall(new GetAlertResponse(0, 0, alertId, "", "No notifications", "", menuImage_notification));
	            }
           }
            else
            {
            	Log.d(TAG, "Unknown alert. Display demo. (id: "+last_menu_id+")");
            	sendCall(new GetAlertResponse(20, 4, alertId, "ID: "+alertId, "HEADER", "01234567890123456789012345678901234567890123456789", menuImage));
            }
            //Log.d(TAG, "DEBUG E");
            parentService.setNotificationUnreadCount((byte) 0);
            break;
        case MessageConstants.MSG_NAVIGATION:
        	//These lines are saved for later use: (do not uncomment!)
        	//sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_DIM));
        	//sendCall(new SetLed(Color.GREEN,1,500));
        	//sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
            //sendCall(new SetScreenMode((byte) MessageConstants.BRIGHTNESS_DIM));
            //sendCall(new SetLed(Color.YELLOW,1,500));
            Navigation nav = (Navigation) event;
            if (nav.isInAlert())
            {
        		Log.d(TAG, "User pressed button in alert.");
        		//if (last_alert_menuitem==0)
        		//{
        			//parentService.setNotificationUnreadCount((byte) 0);
        			//parentService.setNotificationTotalCount((byte) 0);
        		//}
        		sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));   
            }
            else
            {
	            switch (nav.getNavType())
	            {
	        		case MessageConstants.NAVTYPE_SELECT:
	        			switch (nav.getNavAction())
	        			{
	        				case MessageConstants.NAVACTION_LONGPRESS:
	        					Log.d(TAG, "Long press on select key.");
	        					sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
	        	         	    if (menu_state > 0)
	        	          	    {
	        	          			menu_state = 0;
	        	          		    Log.d(TAG, "The menu is now enabled.");
	        	          		    sendCall(new SetMenuSize((byte) 5));
	        	          	     }
	        	          	     else
	        	          	     {
	        	          	    	 /* Media menu test 1 */
	        	          	    	if(enable_media_menu)
	        	          	    	{
	        	          	    	 menu_state = 1;
	        	          		 	 sendCall(new SetMenuSize((byte) 0));
	        	          		 	 sendCall(new ClearDisplay());
	        	          		 	 draw_media_menu(); 
	        	          	    	}
	        	          	     } 
	        	         	break;
	        				default:
	        					Log.d(TAG, "Navigation error: unknown action with select button!");
	        	         		sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));  
	        	         	break;
    					}	        	         	    
        				case MessageConstants.NAVTYPE_MENUSELECT:
        					switch (menu_state)
        					{
        						case 0:
        							Intent buttonIntent = null;
        							int keycode = 0;	 
        							
        							boolean hasdonesomething = false;
        							if (nav.getMenuItemId()==menu_button_findphone_id)
        							{
    									Log.d(TAG, "Find my phone: generating noise and vibration...");
    			            			Vibrator v = (Vibrator) parentService.getSystemService(Context.VIBRATOR_SERVICE);
    			            			v.vibrate(300);
    			            			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    			            			Ringtone r = RingtoneManager.getRingtone(parentService.getApplicationContext(), notification);
    			            			r.setStreamType(AudioManager.STREAM_ALARM); /* Needs to be tested first */
    			            			r.play();
    			            			Log.d(TAG, "Find my phone: done, returning to menu.");
    			            			sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL)); 
    			            			hasdonesomething = true;
        							}
        							if (nav.getMenuItemId()==menu_button_media_next_id)
        							{
        									Log.d(TAG, "MEDIA: Next");
        		            				keycode = KeyEvent.KEYCODE_MEDIA_NEXT;
        					                //Log.d(TAG, "Broadcasting ACTION_MEDIA_BUTTON with ACTION_DOWN and then ACTION_UP with " + keycode);
        					                buttonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        					                buttonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
        					                parentService.sendOrderedBroadcast(buttonIntent, null);
        					                buttonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keycode));
        					                parentService.sendOrderedBroadcast(buttonIntent, null);	            				
        		            				sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
        		            				hasdonesomething = true;
        							}
        							if (nav.getMenuItemId()==menu_button_media_play_id)
        							{
        									Log.d(TAG, "MEDIA: Play / Pause");
        									keycode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
        					                //Log.d(TAG, "Broadcasting ACTION_MEDIA_BUTTON with ACTION_DOWN and then ACTION_UP with " +	keycode);
        					                buttonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        					                buttonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
        					                parentService.sendOrderedBroadcast(buttonIntent, null);
        					                buttonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keycode));
        					                parentService.sendOrderedBroadcast(buttonIntent, null);
        				            	   sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
        				            	   hasdonesomething = true;
        							}
        							if (nav.getMenuItemId()==menu_button_media_previous_id)
        							{
        									Log.d(TAG, "MEDIA: Previous");
        									keycode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
        					                //Log.d(TAG, "Broadcasting ACTION_MEDIA_BUTTON with ACTION_DOWN and then ACTION_UP with " +	keycode);
        					                buttonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        					                buttonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
        					                parentService.sendOrderedBroadcast(buttonIntent, null);
        					                buttonIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keycode));
        					                parentService.sendOrderedBroadcast(buttonIntent, null);
        				            	   sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
        				            	   hasdonesomething = true;
        							}
    								if (hasdonesomething == false)
    								{
    										Log.d(TAG, "Navigation error: unknown menu id!");
    	    	        	         		sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
        							}
        						break;
        						case 1:
        							Log.d(TAG, "Navigation info: user navigated in media menu!");
    	        	         		sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
    	        	         	break;
        						default:
        							Log.d(TAG, "Navigation error: unknown menu state!");
    	        	         		sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));	
    	        	         	break;
	        			}
        			break;
        			case MessageConstants.NAVTYPE_DOWN:
        				Log.d(TAG, "Navigation error: no action for down button!");
        				sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
        			break;
	         	    default:
	         	    	Log.d(TAG, "Navigation error: unknown button!");
	         	    	sendCall(new NavigationResponse(MessageConstants.RESULT_CANCEL));
	         	    break;
	            }
            }           
        } 
    }
    public void draw_media_menu() throws IOException {
		  Log.d(TAG, "Drawing media menu.");         		  
    	  //sendCall(new DisplayBitmap((byte) 0, (byte) 30, menuImage_left));
    	  //sendCall(new DisplayBitmap((byte) 92, (byte) 30, menuImage_right));
          AudioManager amgr = (AudioManager) parentService.getSystemService(Context.AUDIO_SERVICE);
          boolean playing = amgr.isMusicActive();
          if (playing)
          {
        		Log.d(TAG, "A song is playing.");
        		sendCall(new DisplayBitmap((byte) 34, (byte) 34, menuImage_media_isplaying));
          }
          else
          {
        		Log.d(TAG, "Nope, there is no music playing.");
        		sendCall(new DisplayBitmap((byte) 34, (byte) 34, menuImage_media_isnotplaying));
          }  
          //sendCall(new DisplayText("Test"));  //Does not work (yet)
    }

    /**
     * Send a message to the LiveView device if one is connected.
     * 
     * @param call
     *            {@link LiveViewCall} to send to device.
     * @throws IOException
     *             If the message could not be sent successfully.
     */
    private void sendCall(LiveViewCall call) throws IOException {
        if (clientSocket == null) {
            throw new IOException("No client connected!");
        } else {
            clientSocket.getOutputStream().write(call.getEncoded());
        }
    }

    public void stopLoop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG,
                        "Error while closing server socket: " + e.getMessage());
            }
        }
    }
    
    public boolean isLooping() {
        return serverSocket != null;
    }

}
