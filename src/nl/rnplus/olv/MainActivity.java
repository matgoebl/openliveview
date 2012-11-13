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
    
    public void addTestNotification(View view) {
    	LiveViewDbHelper.addNotification(this, "test_title", "test_content", 0, System.currentTimeMillis());
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setMessage("Added test notification to the database.");
        builder.setPositiveButton("Close", null);
        builder.show(); 
    }
    public void deleteAllNotifications(View view) {
    	try {
	    	LiveViewDbHelper.deleteAllNotifications(this);
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Info");
	        builder.setMessage("All stored notifications deleted.");
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
    
    public void showAllStoredNotifications(View view) {
        //For debugging the notification database
    	try {
	    	Cursor notifications = LiveViewDbHelper.getAllNotifications(this);
	    	
	    	int contentcolumn = -1;
	    	for (int i = 0; i < notifications.getColumnCount(); i++)
	    	{
	    		if (notifications.getColumnName(i).contains(LiveViewData.Notifications.CONTENT))
	    		{
	    			contentcolumn = i;
	    		}
	    	}
	    	if (contentcolumn>=0)
	    	{
	    		if (notifications.getCount()>0)
	    		{
	    			notifications.moveToFirst();
	    			String notificationcontents = "";
		    		for (int i = 0; i < notifications.getCount(); i++)
			    	{
			    		notificationcontents += "("+(i+1)+"/"+notifications.getCount()+") "+notifications.getString(contentcolumn)+"\n";
			    		notifications.moveToNext();
			    	}
			        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			        builder.setTitle("Stored notifications");
			        builder.setMessage(notificationcontents);
			        builder.setPositiveButton("Close", null);
			        builder.show();
	    		}
	    		else
	    		{
			        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			        builder.setTitle("Database empty");
			        builder.setMessage("There are currently no notifications in the database.");
			        builder.setPositiveButton("Close", null);
			        builder.show();
	    		}
	    	}
	    	else
	    	{
		        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Database error");
		        builder.setMessage("Column not found.");
		        builder.setPositiveButton("Close", null);
		        builder.show();
	    	}
	    	notifications.close();
    	} catch(Exception e) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Error");
	        builder.setMessage(e.toString());
	        builder.setPositiveButton("Close", null);
	        builder.show();   		
    	}
    }
}
