package nl.rnplus.olv;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import nl.rnplus.olv.ExecuteAsRoot;
import nl.rnplus.olv.service.EventReader;

public class HackActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack);
        //ExecuteAsRoot = new ExecuteAsRoot();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_hack, menu);
        return true;
    }
    
    public void test(View view)
    {
    	//if (ExecuteAsRoot.canRunRootCommands())
    	
    }
}
