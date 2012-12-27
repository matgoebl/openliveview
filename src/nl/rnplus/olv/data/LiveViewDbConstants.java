package nl.rnplus.olv.data;

/**
 * Database constants
 * Currently contains only the notification types
 * 
 * @author Renze Nicolai
 */
public final class LiveViewDbConstants
{
	public static final int ALL_NOTIFICATIONS		= -1; //Use only when getting notifications from the database
    public static final int NTF_GENERIC 			= 0; //Other notifications
    public static final int NTF_ANDROID		 		= 1; //Notifications received by the accessibility service
    public static final int NTF_SMS			 		= 2; //SMS messages
    public static final int NTF_NOTE				= 3; //Notes
}
