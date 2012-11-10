package nl.rnplus.olv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
}