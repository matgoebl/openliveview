package nl.rnplus.olv;

// WILL BE REMOVED IN MY NEXT COMMIT
// NOT USED

import nl.rnplus.olv.data.LiveViewData;
import android.app.ListActivity;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

/**
 * Displays the notifications from the database to the user.
 * 
 * @author Renze Nicolai
 */
public class NotificationViewActivity extends ListActivity {

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

		Cursor cursor = managedQuery(LiveViewData.Notifications.CONTENT_URI, null, null,
                null, null);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item, cursor, new String[] {
                        LiveViewData.Notifications.TITLE, LiveViewData.Notifications.CONTENT },
                new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(adapter);
    }

}
