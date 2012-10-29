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
        Boolean setupcompleted = prefs.getSetupCompleted();
        if (setupcompleted == false) {
            Intent myIntent = new Intent(this, ConfigWizardActivity.class);
            this.startActivity(myIntent);
            finish();
        }
    }

    public void open_settings(View view) {
        Intent myIntent = new Intent(this, LiveViewPreferences.class);
        this.startActivity(myIntent);
    }

    public void open_log(View view) {
        startActivity(new Intent(this, LogViewActivity.class));
    }

    public void open_plugin_manager(View view) {
        startActivity(new Intent(this, PluginManagerActivity.class));
    }
}
