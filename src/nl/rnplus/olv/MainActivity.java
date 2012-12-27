package nl.rnplus.olv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import nl.rnplus.olv.data.LiveViewData;
import nl.rnplus.olv.data.LiveViewDbConstants;
import nl.rnplus.olv.data.LiveViewDbHelper;
import nl.rnplus.olv.data.Prefs;

public class MainActivity extends Activity {
	
	private MainActivity myself;

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
        myself = this;
    }

    public void openSettings(View view) {
        Intent myIntent = new Intent(this, LiveViewPreferences.class);
        this.startActivity(myIntent);
    }
    
    public void openExpertSettings(View view) {
        Intent myIntent = new Intent(this, ExpertConfigActivity.class);
        this.startActivity(myIntent);
    }

    public void openLog(View view) {
        startActivity(new Intent(this, LogViewActivity.class));
    }
    
    public void openNotificationList(View view) {
        startActivity(new Intent(this, NotificationViewActivity.class));
    }

    public void openPluginManager(View view) {
        startActivity(new Intent(this, PluginManagerActivity.class));
    }
    
    public void addNote(View view) {
		final EditText input = new EditText(this);
		input.setText("");
		new AlertDialog.Builder(MainActivity.this)
		.setTitle("Add note")
		.setMessage("Please enter the content of your note")
		.setView(input)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	String value = input.getText().toString();
				LiveViewDbHelper.addNotification(myself, "Note", value, LiveViewDbConstants.NTF_NOTE, System.currentTimeMillis());
				AlertDialog.Builder builder = new AlertDialog.Builder(myself);
				builder.setTitle("Info");
				builder.setMessage("Your note is added to the database.");
				builder.setPositiveButton("Close", null);
				builder.show(); 
		    }
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		        // Do nothing.
		    }
		}).show();
    }
    
    public void showAboutDialog(View view) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About OpenLiveView");
        builder.setMessage("Thank you for using OpenLiveView! This free and open source app was made possible thanks to the following people: Jan Korpegård, basty149, Exception13, Pedro Veloso and Renze Nicolai. Thank you all for your help.");
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
    		SQLiteDatabase db = LiveViewDbHelper.getReadableDb(this);
	    	Cursor notifications = LiveViewDbHelper.getAllNotifications(this, db);
	    	
	    	int contentcolumn = -1;
	    	for (int i = 0; i < notifications.getColumnCount(); i++)
	    	{
	    		if (notifications.getColumnName(i).contains(LiveViewData.Notifications.CONTENT))
	    		{
	    			contentcolumn = i;
	    		}
	    	}
	    	int readcolumn = -1;
	    	for (int i = 0; i < notifications.getColumnCount(); i++)
	    	{
	    		if (notifications.getColumnName(i).contains(LiveViewData.Notifications.READ))
	    		{
	    			readcolumn = i;
	    		}
	    	}
	    	if (readcolumn<0)
	    	{
		        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Warning");
		        builder.setMessage("Database corrupt, read column not found!");
		        builder.setPositiveButton("Close", null);
		        builder.show();
	    	}
	    	if (contentcolumn>=0)
	    	{
	    		if (notifications.getCount()>0)
	    		{
	    			notifications.moveToFirst();
	    			String notificationcontents = "";
		    		for (int i = 0; i < notifications.getCount(); i++)
			    	{
			    		notificationcontents += "("+(i+1)+"/"+notifications.getCount()+") ("+notifications.getInt(readcolumn)+")"+notifications.getString(contentcolumn)+"\n";
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
	    	LiveViewDbHelper.closeDb(db);
    	} catch(Exception e) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Error");
	        builder.setMessage(e.toString());
	        builder.setPositiveButton("Close", null);
	        builder.show();   		
    	}
    }
}
