package net.sourcewalker.olv.service;

//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
import java.util.Calendar;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
//import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
//import android.widget.RemoteViews;

public class GetNotificationService extends AccessibilityService {

	private static final String LOG_TAG = "OLV Notification service";
	final public static String SHOW_NOTIFICATION = "SHOW_NOTIFICATION";
	int time;
	
	//private LiveViewService parentService;
	
	@Override
	public void onServiceConnected() {
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		// Set the type of events that this service wants to listen to.  Others
	    // won't be passed to this service.
	    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;

	    // Set the type of feedback your service will provide.
	    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

	    // Default services are invoked only if no package-specific ones are present
	    // for the type of AccessibilityEvent generated.  This service *is*
	    // application-specific, so the flag isn't necessary.  If this was a
	    // general-purpose service, it would be worth considering setting the
	    // DEFAULT flag.

	    // info.flags = AccessibilityServiceInfo.DEFAULT;

	    info.notificationTimeout = 100;

	    this.setServiceInfo(info);
	    Log.w(LOG_TAG, "Service started!");

	}
	
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        String eventText = null;
        switch(eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                eventText = "Focused: ";
                //Log.w(LOG_TAG, "Notify: It works! (" + eventType + ")");
                //Hier start de simpele notificatiesniffer
                List<CharSequence> notificationList = event.getText();
                for (int i = 0; i < notificationList.size(); i++) {
                    //Toast.makeText(this.getApplicationContext(), notificationList.get(i), 1).show();
                	Log.w(LOG_TAG, "The notification: " + notificationList.get(i));
                	try
                	{
	                	Intent bci = new Intent(SHOW_NOTIFICATION);
	                	Bundle bcb = new Bundle();
	                	bcb.putString("contents", notificationList.get(i).toString());
	                	bcb.putString("title", "Notification");
	                	time = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
	                    time += Calendar.getInstance().get(Calendar.ZONE_OFFSET)/1000;
	                    time += Calendar.getInstance().get(Calendar.DST_OFFSET) / 1000;
	                	bcb.putInt("timestamp", time);
	                	bci.putExtras(bcb);
	                	sendBroadcast(bci);  
	                	Log.w(LOG_TAG, "The notification was sent to the lvservice.");
                	}
                	catch (IllegalArgumentException e) {
                		Log.w(LOG_TAG, "Error!");
                	}
                }
                //Hier start de uitgebreide notificatiesniffer (uitgeschakeld tot hij nodig is)
                /*
                Notification notification = (Notification) event.getParcelableData();
                RemoteViews views = notification.contentView;
                Class secretClass = views.getClass();

                try {
                    Map<Integer, String> text = new HashMap<Integer, String>();

                    Field outerFields[] = secretClass.getDeclaredFields();
                    for (int i = 0; i < outerFields.length; i++) {
                        if (!outerFields[i].getName().equals("mActions")) continue;

                        outerFields[i].setAccessible(true);

                        ArrayList<Object> actions = (ArrayList<Object>) outerFields[i]
                                .get(views);
                        for (Object action : actions) {
                            Field innerFields[] = action.getClass().getDeclaredFields();

                            Object value = null;
                            Integer type = null;
                            Integer viewId = null;
                            for (Field field : innerFields) {
                                field.setAccessible(true);
                                if (field.getName().equals("value")) {
                                    value = field.get(action);
                                } else if (field.getName().equals("type")) {
                                    type = field.getInt(action);
                                } else if (field.getName().equals("viewId")) {
                                    viewId = field.getInt(action);
                                }
                            }

                            if (type == 9 || type == 10) {
                                text.put(viewId, value.toString());
                            }
                        }

                        Log.w(LOG_TAG, "title is: " + text.get(16908310));
                        Log.w(LOG_TAG, "info is: " + text.get(16909082));
                        Log.w(LOG_TAG, "text is: " + text.get(16908358));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } */
                break;
            default:
            	Log.w(LOG_TAG, "Error: unknown event type (" + eventType + ")");
        }
    }

    @Override
    public void onInterrupt() {
    	Log.w(LOG_TAG, "OnInterrupt() triggered");
    }

}