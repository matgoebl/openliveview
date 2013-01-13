package nl.rnplus.olv.receiver;
 
import nl.rnplus.olv.data.LiveViewDbHelper2;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
 
public class ActionReceiver extends BroadcastReceiver {
	private final String LOG_TAG = "EventReceiver";
	private final String appname = "nl.rnplus.olv";
	private final String action_alert_add = appname+".add.alert";
	public final String action_receive_sms = "android.provider.Telephony.SMS_RECEIVED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_TAG, "Received broadcast: '"+intent.getAction()+"'.");
		Boolean broadcastHandled = false;
		
		if (intent.getAction().equals(action_alert_add)) {
			String title = intent.getExtras().getString("title");
			String contents = intent.getExtras().getString("contents");
			int type = intent.getExtras().getInt("type");			
			long timestamp = intent.getExtras().getLong("timestamp");
			LiveViewDbHelper2 dbHelper;
			dbHelper = new LiveViewDbHelper2(context);	
			dbHelper.openToRead();
			dbHelper.insertAlert(title, contents, type, timestamp);
			dbHelper.close();
			Log.i(LOG_TAG, "An alert was added to the database.");
			broadcastHandled = true;
		}

		if (broadcastHandled == false) {
			Log.e(LOG_TAG, "Error: Broadcast type unknown.");
		}
	}
 
}