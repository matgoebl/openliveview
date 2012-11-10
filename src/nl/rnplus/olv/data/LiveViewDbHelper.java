package nl.rnplus.olv.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database currently holds logs and notifications.
 * 
 * @author Xperimental
 * Renze Nicolai: Added notifications
 */
public class LiveViewDbHelper extends SQLiteOpenHelper
{

    private static final String DB_NAME = "liveview.db";

    private static final int DB_VERSION = 1;

    public LiveViewDbHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /*
     * (non-Javadoc)
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
     * .SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(LiveViewData.Log.SCHEMA);
        db.execSQL(LiveViewData.Notifications.SCHEMA);
    }

    /*
     * (non-Javadoc)
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
     * .SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion != DB_VERSION)
        {
            db.execSQL("DROP TABLE " + LiveViewData.Log.TABLE);
            db.execSQL("DROP TABLE " + LiveViewData.Notifications.TABLE);
            onCreate(db);
        }
    }

    public static void logMessage(Context context, String message)
    {
        LiveViewDbHelper helper = new LiveViewDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LiveViewData.Log.TIMESTAMP, System.currentTimeMillis());
        values.put(LiveViewData.Log.MESSAGE, message);
        db.insert(LiveViewData.Log.TABLE, LiveViewData.Log._ID, values);
        db.close();
    }
    
    public static void addNotification(Context context, String title, String content, Integer type, Long timestamp)
    {
        LiveViewDbHelper helper = new LiveViewDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LiveViewData.Notifications.TIMESTAMP, timestamp); //System.currentTimeMillis()
        values.put(LiveViewData.Notifications.TITLE, title);
        values.put(LiveViewData.Notifications.CONTENT, content);
        values.put(LiveViewData.Notifications.TYPE, type);
        db.insert(LiveViewData.Log.TABLE, LiveViewData.Log._ID, values);
        db.close();
    }

}
