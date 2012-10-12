package nl.rnplus.olv.messages.calls;

/* Renze Nicolai
 * Added 6-10-2012
 * DisplayPanelAck.java
 * Not checked: maybe not complete.
 */

import nl.rnplus.olv.messages.LiveViewCall;
import nl.rnplus.olv.messages.MessageConstants;

public class DisplayPanelAck extends LiveViewCall {

    public DisplayPanelAck() {
        super(MessageConstants.MSG_DISPLAYPANEL_ACK);
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
