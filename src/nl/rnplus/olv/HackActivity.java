package nl.rnplus.olv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import nl.rnplus.olv.ExecuteAsRoot;
import nl.rnplus.olv.data.Prefs;
import nl.rnplus.olv.service.EventReader;

public class HackActivity extends Activity
{	
	private TextView t;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack);
        //ExecuteAsRoot root = new ExecuteAsRoot;
        t = (TextView)findViewById(R.id.resultView); 
        t.setText("Not tested yet.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_hack, menu);
        return true;
    }
    
    public void test(View view)
    {
    	if (ExecuteAsRoot.canRunRootCommands()==true)
    	{
    		t.setText("PASS");
    	}
    	else
    	{
    		t.setText("FAIL");
    	}
    }
    
    public void get(View view)
    {
    	Prefs prefs = new Prefs(this);
    	//t.setText(ExecuteAsRoot.RunAsRoot("cat /data/misc/bluetoothd/"+prefs.getDeviceAddress()+"/linkkeys"));
            Process process;            
            try {
				process = Runtime.getRuntime().exec("cat /data/misc/bluetoothd/"+prefs.getDeviceAddress()+"/linkkeys");
				BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String o = "";
				String line = "";
				while ((line = in.readLine()) != null)
				{
					o = o +" + "+ line;
				}
				t.setText(o);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
}
