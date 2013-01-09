package nl.rnplus.olv;

import nl.rnplus.olv.R;
import nl.rnplus.olv.data.Prefs;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class FilterEditor extends Activity {
	static Button btn;
	static Prefs prefs;
	ProgressDialog loadingDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter_editor);
		btn = (Button) this.findViewById(R.id.btnAddAppToFilter);
		
		loadingDialog = new ProgressDialog(this);
		loadingDialog.setMessage(getString(R.string.prefs_filtereditor_btn_wait));
		
	}

	@Override
	protected void onResume(){
		super.onResume();
		prefs = new Prefs(this);
		PackageManager pm = getPackageManager();
		ListView listView = (ListView) findViewById(R.id.mylist);
		String[] values = new String[prefs.getNumberOfFilters()];
		for(int i = 0;i< prefs.getNumberOfFilters();i++){
			String appName;
			try{
				appName = pm.getApplicationLabel(pm.getApplicationInfo(prefs.getFilterString(i),0)).toString();
			}catch(Exception e){
				appName="";
			}
			values[i] = appName; 
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				prefs.removeFilterString(position);
				onResume();
			}
		});
	}
		
	public void addAppToFilter(View view){
		//btn.setText(R.string.prefs_filtereditor_btn_wait);
		loadingDialog.show();
		Intent intent = new Intent(this, AppDialog.class);
		startActivityForResult(intent, 0);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode==42) prefs.addFilterString(data.getStringExtra("pkgName"));
		//btn.setText(R.string.prefs_filtereditor_btn_start);
		loadingDialog.dismiss();
	}
	
	public void selectFilterMode(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.message_select_filter_mode);
		builder.setNegativeButton("None", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				prefs.setFilterMode(0);
				
			}
		});
		builder.setNeutralButton("Toast messages", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				prefs.setFilterMode(1);
				
			}
		});
		builder.setPositiveButton("Statusbar notifications", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				prefs.setFilterMode(2);
				
			}
		});
		builder.show();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
}
