/*
 * Added by Renze Nicolai (RN+)
 * GetNotificationServiceJB.java
 * Receives all notifications from the OS.
 * (Jelly Bean version, only difference with GetNotificationService.java is the name...)
 * Its needed because of the new permissions Jelly Bean uses for accessibility services.
 */
package nl.rnplus.olv.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

import nl.rnplus.olv.data.LiveViewDbConstants;
import nl.rnplus.olv.data.LiveViewDbHelper;
import nl.rnplus.olv.data.Prefs;

public class GetNotificationServiceJB extends AccessibilityService {

    private static final String LOG_TAG = "OLV Notification service";
    final public static String SHOW_NOTIFICATION = "OLV_ADD_NOTIFICATION";
    private String PLUGIN_COMMAND = "nl.rnplus.olv.plugin.command";

    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.d(LOG_TAG, "Service started! (JB)");

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> notificationList = event.getText();
                for (CharSequence aNotificationList : notificationList) {
                    //Log.d(LOG_TAG, "The notification: " + aNotificationList);
                    try {
                    	Long time = event.getEventTime();//System.currentTimeMillis();
                    	/*
                        Intent bci = new Intent(SHOW_NOTIFICATION);
                        Bundle bcb = new Bundle();
                        bcb.putString("contents", aNotificationList.toString());
                        bcb.putString("title", "Notification");
                        bcb.putInt("type", LiveViewDbConstants.NTF_ANDROID);
                        long time = System.currentTimeMillis();
                        bcb.putLong("timestamp", time);
                        bci.putExtras(bcb);
                        sendBroadcast(bci);
                        */
                        LiveViewDbHelper.addNotification(this, "Notification", aNotificationList.toString(), LiveViewDbConstants.NTF_ANDROID, time);
        		        Prefs prefs = new Prefs(this);
        		        Boolean enable_notification_buzzer2 = prefs.getenablenotificationbuzzer2();
        		        if (enable_notification_buzzer2)
        		        {
        		            Intent bci2 = new Intent(PLUGIN_COMMAND);
        		            Bundle bcb2 = new Bundle();
        		            bcb2.putString("command", "notify");
        		            bcb2.putInt("delay", 0);
        		            bcb2.putInt("time", 600);
        		            bcb2.putLong("timestamp", time);
        		            bcb2.putInt("displaynotification", 1);
        		            bcb2.putString("line1", "Notification");
        		            bcb2.putString("line2", aNotificationList.toString());
        		            bcb2.putInt("icon_type", 1);
        		            bci2.putExtras(bcb2);
        		            sendBroadcast(bci2);
        		        }
                        String message = "Notification sent to LiveView: "+aNotificationList.toString();
                        Log.d(LOG_TAG, message);
                        //LiveViewDbHelper.logMessage(this, message);
                    } catch (IllegalArgumentException e) {
                        String message = "Error while reading notifications!";
                        Log.e(LOG_TAG, message);
                        LiveViewDbHelper.logMessage(this, message);
                    }
                }
                break;
            default:
                String message = "Error: unknown event type (" + eventType + ")";
                Log.e(LOG_TAG, message);
                //LiveViewDbHelper.logMessage(this, message);                
        }
    }

    @Override
    public void onInterrupt() {
        String message = "OnInterrupt() triggered in NotificationService.";
        Log.v(LOG_TAG, message);
        //LiveViewDbHelper.logMessage(this, message);              
    }

}