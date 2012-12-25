package nl.rnplus.olv.data;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Provides transparent device-wide access to the OpenLiveView database.
 * 
 * @author Xperimental
 * 
 * (Now limited to access from olv itself by placing android:exported="false" in AndroidManifest.xml)
 */
public class LiveViewDataProvider extends ContentProvider
{

    private static final int MATCH_LOG = 1;
    private static final int MATCH_NOTIFICATIONS = 2;

    private static final UriMatcher uriMatcher;
    private static final HashMap<String, String> logProjection;
    private static final HashMap<String, String> notificationsProjection;

    static
    {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(LiveViewData.AUTHORITY, "log", MATCH_LOG);
        uriMatcher.addURI(LiveViewData.AUTHORITY, "notifications", MATCH_NOTIFICATIONS);

        logProjection = new HashMap<String, String>();
        for (String column : LiveViewData.Log.DEFAULT_PROJECTION)
        {
            logProjection.put(column, column);
        }
        
        notificationsProjection = new HashMap<String, String>();
        for (String column : LiveViewData.Log.DEFAULT_PROJECTION)
        {
        	notificationsProjection.put(column, column);
        }
    }

    private LiveViewDbHelper dbHelper;

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#delete(android.net.Uri,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
        case MATCH_LOG:
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int count = db.delete(LiveViewData.Log.TABLE, null, null);
            db.close();
            getContext().getContentResolver().notifyChange(
                    LiveViewData.Log.CONTENT_URI, null);
            return count;
        case MATCH_NOTIFICATIONS:
            SQLiteDatabase db2 = dbHelper.getWritableDatabase();
            int count2 = db2.delete(LiveViewData.Notifications.TABLE, null, null);
            db2.close();
            getContext().getContentResolver().notifyChange(
                    LiveViewData.Notifications.CONTENT_URI, null);
            return count2;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case MATCH_LOG:
            return LiveViewData.Log.CONTENT_TYPE;
        case MATCH_NOTIFICATIONS:
            return LiveViewData.Notifications.CONTENT_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri,
     * android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
        case MATCH_LOG:
            if (values.containsKey(LiveViewData.Log.MESSAGE) == false) {
                throw new IllegalArgumentException("No message!");
            }
            if (values.containsKey(LiveViewData.Log.TIMESTAMP) == true) {
                throw new IllegalArgumentException(
                        "Timestamp will be set automatically!");
            }
            if (values.containsKey(LiveViewData.Log.DATETIME) == true) {
                throw new IllegalArgumentException(
                        "Datetime is no valid column!");
            }
            values.put(LiveViewData.Log.TIMESTAMP, System.currentTimeMillis());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.insert(LiveViewData.Log.TABLE, LiveViewData.Log._ID, values);
            db.close();
            getContext().getContentResolver().notifyChange(
                    LiveViewData.Log.CONTENT_URI, null);
            return LiveViewData.Log.CONTENT_URI;
        case MATCH_NOTIFICATIONS:
            if (values.containsKey(LiveViewData.Notifications.CONTENT) == false) {
                throw new IllegalArgumentException("No content!");
            }
            if (values.containsKey(LiveViewData.Notifications.TIMESTAMP) == true) {
                throw new IllegalArgumentException(
                        "Timestamp will be set automatically!");
            }
            if (values.containsKey(LiveViewData.Notifications.DATETIME) == true) {
                throw new IllegalArgumentException(
                        "Datetime is no valid column!");
            }
            values.put(LiveViewData.Notifications.TIMESTAMP, System.currentTimeMillis());
            SQLiteDatabase db2 = dbHelper.getWritableDatabase();
            db2.insert(LiveViewData.Notifications.TABLE, LiveViewData.Notifications._ID, values);
            db2.close();
            getContext().getContentResolver().notifyChange(
                    LiveViewData.Notifications.CONTENT_URI, null);
            return LiveViewData.Notifications.CONTENT_URI;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        dbHelper = new LiveViewDbHelper(getContext());
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#query(android.net.Uri,
     * java.lang.String[], java.lang.String, java.lang.String[],
     * java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
        case MATCH_LOG:
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(LiveViewData.Log.TABLE,
                    LiveViewData.Log.DEFAULT_PROJECTION, null, null, null,
                    null, LiveViewData.Log.DEFAULT_ORDER);
            c.setNotificationUri(getContext().getContentResolver(),
                    LiveViewData.Log.CONTENT_URI);
            return c;
        case MATCH_NOTIFICATIONS:
            SQLiteDatabase db2 = dbHelper.getReadableDatabase();
            Cursor c2 = db2.query(LiveViewData.Notifications.TABLE,
                    LiveViewData.Notifications.DEFAULT_PROJECTION, null, null, null,
                    null, LiveViewData.Notifications.DEFAULT_ORDER);
            c2.setNotificationUri(getContext().getContentResolver(),
                    LiveViewData.Notifications.CONTENT_URI);
            return c2;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#update(android.net.Uri,
     * android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }

}
