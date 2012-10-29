/*
 * Added by Renze Nicolai (RN+)
 * GetNotificationService.java
 * Receives all notifications from the OS.
 */
package nl.rnplus.olv.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

public class GetNotificationService extends AccessibilityService {

    private static final String LOG_TAG = "OLV Notification service (Pre Jelly Bean)";
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
                        Intent bci = new Intent(SHOW_NOTIFICATION);
                        Bundle bcb = new Bundle();
                        bcb.putString("contents", aNotificationList.toString());
                        bcb.putString("title", "Notification");
                        long time = System.currentTimeMillis();
                        bcb.putLong("timestamp", time);
                        bci.putExtras(bcb);
                        sendBroadcast(bci);
                        Log.w(LOG_TAG, "The notification was sent to the LiveView service.");
                    } catch (IllegalArgumentException e) {
                        Log.w(LOG_TAG, "Error while reading notifications!");
                    }
                }
                break;
            default:
                Log.w(LOG_TAG, "Error: unknown event type (" + eventType + ")");
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(LOG_TAG, "OnInterrupt() triggered in NotificationService.");
    }

}