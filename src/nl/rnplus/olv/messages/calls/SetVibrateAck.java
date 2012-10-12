package nl.rnplus.olv.messages.calls;

/* Renze Nicolai
 * Added 5-10-2012
 * SetVibrateAck.java
 * Not checked: maybe not complete.
 */

import nl.rnplus.olv.messages.LiveViewCall;
import nl.rnplus.olv.messages.MessageConstants;

public class SetVibrateAck extends LiveViewCall {

    public SetVibrateAck() {
        super(MessageConstants.MSG_SETVIBRATE_ACK);
    }

    /*
     * (non-Javadoc)
     * @see net.sourcewalker.olv.messages.LiveViewRequest#getPayload()
     */
    @Override
    protected byte[] getPayload() {
        return new byte[] { MessageConstants.RESULT_OK };
    }

}
