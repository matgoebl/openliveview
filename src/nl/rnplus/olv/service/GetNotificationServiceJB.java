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
                        Intent bci = new Intent(SHOW_NOTIFICATION);
                        Bundle bcb = new Bundle();
                        bcb.putString("contents", aNotificationList.toString());
                        bcb.putString("title", "Notification");
                        long time = System.currentTimeMillis();
                        bcb.putLong("timestamp", time);
                        bci.putExtras(bcb);
                        sendBroadcast(bci);
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
                //This code can get more info out of a notification, but it is very unstable:
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