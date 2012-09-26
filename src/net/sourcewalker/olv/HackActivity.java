package net.sourcewalker.olv;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class HackActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_hack, menu);
        return true;
    }
}
