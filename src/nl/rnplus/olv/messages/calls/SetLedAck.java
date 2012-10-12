package nl.rnplus.olv.messages.calls;

/* Renze Nicolai
 * Added 5-10-2012
 * SetLedAck.java
 * Not checked: maybe not complete.
 */

import nl.rnplus.olv.messages.LiveViewCall;
import nl.rnplus.olv.messages.MessageConstants;

public class SetLedAck extends LiveViewCall {

    public SetLedAck() {
        super(MessageConstants.MSG_SETLED_ACK);
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
