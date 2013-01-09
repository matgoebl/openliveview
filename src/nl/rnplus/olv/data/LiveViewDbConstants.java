package nl.rnplus.olv.data;

/**
 * Database constants
 * Currently contains only the notification types
 * 
 * @author Renze Nicolai
 */
public final class LiveViewDbConstants
{
  //Database
  public static final String DATABASE_NAME = "olv.db";
  public static final int DATABASE_VERSION = 1;
  
  //Tables
  public static final String TABLE_ALERT_ITEMS = "alert_items";
  public static final String TABLE_ALERT_FILTER = "alert_filter";
  public static final String TABLE_LOG = "log";
  public static final String TABLE_MENU_ITEMS = "menu_items";
  
  //Columns for TABLE_ALERT_ITEMS
  public static final String COLUMN_ALERT_ITEMS_CONTENT = "content";
  
  
  //Alert types
  public static final int ALL_NOTIFICATIONS = -1; //Use only when getting notifications from the database
  public static final int NTF_GENERIC = 0; //Other notifications
  public static final int NTF_ANDROID = 1; //Notifications received by the accessibility service
  public static final int NTF_SMS = 2; //SMS messages
  public static final int NTF_NOTE = 3; //Notes
}
