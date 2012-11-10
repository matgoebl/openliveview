package nl.rnplus.olv.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import nl.rnplus.olv.R;

/**
 * Provides access to the preferences of this application.
 *
 * @author Robert &lt;xperimental@solidproject.de&gt;
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
        return preferences.getBoolean("menu.notificationsvisible", false);
    }

    public Boolean getMenuShowMediaNext() {
        return preferences.getBoolean("menu.medianextvisible", false);
    }

    public Boolean getMenuShowMediaPlay() {
        return preferences.getBoolean("menu.mediaplayvisible", false);
    }

    public Boolean getMenuShowMediaPrevious() {
        return preferences.getBoolean("menu.mediapreviousvisible", false);
    }

    public Boolean getenablenotificationbuzzer() {
        return preferences.getBoolean("system.enablenotificationbuzzer", false);
    }

    public Boolean getEnableStandbyAccess() {
        return preferences.getBoolean("system.enablestandbyaccess", false);
    }

    public Boolean getEnableMediaMenu() {
        return preferences.getBoolean("media.enablemenu", false);
    }

    public Boolean getMenuShowBatteryStatus() {
        return preferences.getBoolean("menu.batterystatusvisible", false);
    }

    public Boolean getSetupCompleted() {
        return preferences.getBoolean("system.setupcompleted", false);
    }

    public Boolean getUpdateUnreadCountWhenMenuOpens() {
        return preferences.getBoolean("system.updateunreadcountwhenmenuopens", false);
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
        return Integer.parseInt(preferences.getString("menu.menuupdownaction", "0"));
    }
}