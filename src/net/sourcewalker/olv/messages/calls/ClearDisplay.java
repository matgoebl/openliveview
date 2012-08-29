package net.sourcewalker.olv.messages.calls;
//Added by RN+

import net.sourcewalker.olv.messages.LiveViewCall;
import net.sourcewalker.olv.messages.MessageConstants;

public class ClearDisplay extends LiveViewCall {
	
    public ClearDisplay() {
        super(MessageConstants.MSG_CLEARDISPLAY);
    }

    /*
     * (non-Javadoc)
     * @see net.sourcewalker.olv.messages.LiveViewRequest#getPayload()
     */
    @Override
    protected byte[] getPayload() {
        return new byte[] { 0 };
    }

}
