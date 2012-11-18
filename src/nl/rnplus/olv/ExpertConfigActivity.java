package nl.rnplus.olv;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import nl.rnplus.olv.data.Prefs;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExpertConfigActivity extends Activity {

	EditText editinitialmenuitemid;
	EditText editmenuvibrationtime;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_config);
        Prefs prefs = new Prefs(this);
    	
    	editinitialmenuitemid = (EditText) findViewById(R.id.editinitialmenuitemid);
    	editmenuvibrationtime = (EditText) findViewById(R.id.editvibrationtime);
    	
    	editinitialmenuitemid.setText(String.valueOf(prefs.getInitialMenuItemId()));
    	editmenuvibrationtime.setText(String.valueOf(prefs.getMenuVibrationTime()));
    }

    public void save(View view) {
    	try{
    		Prefs prefs = new Prefs(this);
    		prefs.setInitialMenuItemId(Integer.valueOf(editinitialmenuitemid.getText().toString()));
    		prefs.setMenuVibrationTime(Integer.valueOf(editmenuvibrationtime.getText().toString()));
    		this.finish();
    	} catch(Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Could not save your changes!");
            builder.setMessage("Did you input text in a place that should contain a number?");
            builder.setPositiveButton("OK", null);
            builder.show();
    	}
    }

    public void cancel(View view) {
        this.finish();
    }
}
