package nl.rnplus.olv.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import nl.rnplus.olv.R;

/**
 * Provides access to the preferences of this application.
 */
public class Prefs {

    private final SharedPreferences preferences;
    private final String keyDeviceAddress;

    public Prefs(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        keyDeviceAddress = context.getString(R.string.prefs_deviceaddress_key);
    }

    public String getDeviceAddress() {
        return preferences.getString("device.address", null);
    }

    public Boolean getMenuShowFindMyPhone() {
        return preferences.getBoolean("menu.findmyphonevisible", true);
    }

    public Boolean getMenuShowNotifications() {
        return preferences.getBoolean("menu.notificationsvisible", true);
    }

    public Boolean getMenuShowMediaNext() {
        return preferences.getBoolean("menu.medianextvisible", true);
    }

    public Boolean getMenuShowMediaPlay() {
        return preferences.getBoolean("menu.mediaplayvisible", true);
    }

    public Boolean getMenuShowMediaPrevious() {
        return preferences.getBoolean("menu.mediapreviousvisible", true);
    }

    public Boolean getenablenotificationbuzzer() {
        return preferences.getBoolean("system.enablenotificationbuzzer", false);
    }
    
    public Boolean getenablenotificationbuzzer2() {
        return preferences.getBoolean("system.enablenotificationbuzzer2", true);
    }

    public Boolean getEnableMediaMenu() {
        return preferences.getBoolean("media.enablemenu", true);
    }

    public Boolean getMenuShowBatteryStatus() {
        return preferences.getBoolean("menu.batterystatusvisible", true);
    }

    public Boolean getSetupCompleted() {
        return preferences.getBoolean("system.setupcompleted", false);
    }

    public Boolean getUpdateUnreadCountWhenMenuOpens() {
        return preferences.getBoolean("system.updateunreadcountwhenmenuopens", true);
    }
    
    public int getMenuVibrationTime() {
        return preferences.getInt("menu.menuvibrationtime", 0);
    }
    
    public int getInitialMenuItemId() {
        return preferences.getInt("menu.initialmenuitemid", 0);
    }
    
    public Boolean getMenuShowMediaMenuStatus() {
        return preferences.getBoolean("menu.mediamenuvisible", false);
    }    
    
    public void setMenuVibrationTime(int value) {
        Editor editor = preferences.edit();
        editor.putInt("menu.menuvibrationtime", value);
        editor.commit();
    }
    
    public void setInitialMenuItemId(int value) {
        Editor editor = preferences.edit();
        editor.putInt("menu.initialmenuitemid", value);
        editor.commit();
    }
    
    public void setSetupCompleted(Boolean value) {
        Editor editor = preferences.edit();
        editor.putBoolean("system.setupcompleted", value);
        editor.commit();
    }

    public void setDeviceAddress(String address) {
        Editor editor = preferences.edit();
        editor.putString(keyDeviceAddress, address);
        editor.commit();
    }

    public int getMenuUpDownAction() {
        return Integer.parseInt(preferences.getString("menu.menuupdownaction", "1"));
    }
}
