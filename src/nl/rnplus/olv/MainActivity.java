package nl.rnplus.olv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import nl.rnplus.olv.data.LiveViewData;
import nl.rnplus.olv.data.LiveViewDbHelper;
import nl.rnplus.olv.data.Prefs;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Prefs prefs = new Prefs(this);
        if (!prefs.getSetupCompleted()) {
            Intent myIntent = new Intent(this, ConfigWizardActivity.class);
            this.startActivity(myIntent);
            finish();
        }
    }

    public void openSettings(View view) {
        Intent myIntent = new Intent(this, LiveViewPreferences.class);
        this.startActivity(myIntent);
    }

    public void openLog(View view) {
        startActivity(new Intent(this, LogViewActivity.class));
    }

    public void openPluginManager(View view) {
        startActivity(new Intent(this, PluginManagerActivity.class));
    }
    
    public void showAllStoredNotifications(View view) {
        //For debugging the notification database
    	try {
    		LiveViewDbHelper.addNotification(this, "test_title", "test_content", 0, System.currentTimeMillis());
	    	Cursor notifications = LiveViewDbHelper.getAllNotifications(this);
	    	String notificationcontents = "";
	    	int contentcolumn = -1;
	    	notificationcontents += "L: "+LiveViewData.Notifications.CONTENT+" --- ";
	    	for (int i = 0; i < notifications.getColumnCount(); i++)
	    	{
	    		if (notifications.getColumnName(i).contains(LiveViewData.Notifications.CONTENT))
	    		{
	    			contentcolumn = i;
	    		}
	    		notificationcontents += "C: "+notifications.getColumnName(i)+" --- ";
	    	}
	    	notifications.moveToFirst();
	    	if (contentcolumn>=0)
	    	{
		    	do {
		    		notificationcontents += notifications.getString(contentcolumn)+" --- ";
		    		notifications.moveToNext();
		    	} while (notifications.isLast()==false);
	    	}
	    	else
	    	{
	    		notificationcontents += "Database error: Column not found.";
	    	}
	    	notifications.close();
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Stored notifications");
	        builder.setMessage(notificationcontents);
	        builder.setPositiveButton("Close", null);
	        builder.show();
    	} catch(Exception e) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Error");
	        builder.setMessage(e.toString());
	        builder.setPositiveButton("Close", null);
	        builder.show();   		
    	}
    }
}
