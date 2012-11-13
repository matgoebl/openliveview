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

public class GetNotificationServiceJB extends AccessibilityService {

    private static final String LOG_TAG = "OLV Notification service (Jelly Bean)";
    final public static String SHOW_NOTIFICATION = "OLV_ADD_NOTIFICATION";

    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.w(LOG_TAG, "Service started!");

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> notificationList = event.getText();
                for (CharSequence aNotificationList : notificationList) {
                    Log.w(LOG_TAG, "The notification: " + aNotificationList);
                    try {
                    	Long time = System.currentTimeMillis();
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
                        String message = "Notification sent to LiveView: "+aNotificationList.toString();
                        Log.v(LOG_TAG, message);
                        LiveViewDbHelper.logMessage(this, message);
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
                LiveViewDbHelper.logMessage(this, message);                
        }
    }

    @Override
    public void onInterrupt() {
        String message = "OnInterrupt() triggered in NotificationService.";
        Log.v(LOG_TAG, message);
        LiveViewDbHelper.logMessage(this, message);              
    }

}