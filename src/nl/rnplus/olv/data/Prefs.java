package nl.rnplus.olv.data;

import nl.rnplus.olv.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

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
    
    public Boolean getmenushowfindmyphone()
    {
    	return preferences.getBoolean("menu.findmyphonevisible", true);
    }
    
    public Boolean getmenushownotifications()
    {
    	return preferences.getBoolean("menu.notificationsvisible", false);
    }
    
    public Boolean getmenushowmedianext()
    {
    	return preferences.getBoolean("menu.medianextvisible", false);
    }
    
    public Boolean getmenushowmediaplay()
    {
    	return preferences.getBoolean("menu.mediaplayvisible", false);
    }  
    
    public Boolean getmenushowmediaprevious()
    {
    	return preferences.getBoolean("menu.mediapreviousvisible", false);
    }    
    
    public Boolean getenablenotificationbuzzer()
    {
    	return preferences.getBoolean("system.enablenotificationbuzzer", false);
    }
    
    public Boolean getenablestandbyaccess()
    {
    	return preferences.getBoolean("system.enablestandbyaccess", false);
    }

    public Boolean getenablemediamenu()
    {
    	return preferences.getBoolean("media.enablemenu", false);
    }  
    
    public Boolean getsetupcompleted()
    {
    	return preferences.getBoolean("system.setupcompleted", false);
    } 
    
    public Boolean getupdateunreadcountwhenmenuopens()
    {
    	return preferences.getBoolean("system.updateunreadcountwhenmenuopens", false);
    } 
    
    public void setsetupcompleted(Boolean value) {
        Editor editor = preferences.edit();
        editor.putBoolean("system.setupcompleted", value);
        editor.commit();
    }
    
    public void setDeviceAddress(String address) {
        Editor editor = preferences.edit();
        editor.putString(keyDeviceAddress, address);
        editor.commit();
    }
}
