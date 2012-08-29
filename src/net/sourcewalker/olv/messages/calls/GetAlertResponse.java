package net.sourcewalker.olv.messages.calls;
//Added by RN+
//Tested, partially working

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.sourcewalker.olv.messages.LiveViewCall;
import net.sourcewalker.olv.messages.MessageConstants;
import net.sourcewalker.olv.messages.UShort;

public class GetAlertResponse extends LiveViewCall {
	
	private byte[] image;
	private String timestampText;
	private String headerText;
	private String bodyText;
	private final UShort totalCount;
	private final UShort unreadCount;
	private final UShort alertIndex;

    public GetAlertResponse(int totalcount, int unreadcount, int alertindex, String timestamptext, String headertext, String bodytext, byte[] image) {
        super(MessageConstants.MSG_GETALERT_RESP);
        this.image = image;
        this.timestampText = timestamptext;
        this.headerText = headertext;
        this.bodyText = bodytext;
        this.totalCount = new UShort((short) totalcount);
        this.unreadCount = new UShort((short) unreadcount);
        this.alertIndex = new UShort((short) alertindex);
        
    }

    /*
     * (non-Javadoc)
     * @see net.sourcewalker.olv.messages.LiveViewRequest#getPayload()
     */
    @Override
    protected byte[] getPayload() {
    	try {
	        byte[] timestamptextArray = timestampText.getBytes("iso-8859-1");
	    	byte[] headertextArray = headerText.getBytes("iso-8859-1");
	    	byte[] bodytextArray = bodyText.getBytes("iso-8859-1");
	      /*  int size = 18 + timestamptextArray.length + headertextArray.length + bodytextArray.length + image.length;
	        ByteBuffer buffer = ByteBuffer.allocate(size);
	        buffer.order(ByteOrder.BIG_ENDIAN);
	        buffer.put((byte) 0);
	        buffer.putShort(totalcount.getValue());//totalcount
	        buffer.putShort(unreadcount.getValue());//unreadcount
	        buffer.putShort(alertindex.getValue());//alertindex
	        buffer.put((byte) 0);
	        buffer.put((byte) 0); //0 = plaintext & 1 = bitmap
	        buffer.putShort((short) timestamptextArray.length); //timestamp text
	        buffer.put(timestamptextArray);
	        buffer.putShort((short) headertextArray.length); //header text
	        buffer.put(headertextArray);   
	        buffer.putShort((short) bodytextArray.length); //body text
	        buffer.put(bodytextArray);
	        buffer.put((byte) 0);
	        //buffer.putShort((short) image.length);   //LIVEVIEW GOES NUTS WHEN I UNCOMMENT THESE LINES
	        //buffer.put(image); 					   //WHATS WRONG?
	        
	        return buffer.array();  */
	        
	        int size = 20 + timestamptextArray.length + headertextArray.length + bodytextArray.length + image.length;
	        ByteBuffer buffer = ByteBuffer.allocate(size);
	        buffer.put((byte) 0);
	        buffer.putShort(totalCount.getValue());
	        buffer.putShort(unreadCount.getValue());
	        buffer.putShort(alertIndex.getValue());
	        buffer.put((byte) 0);
	        buffer.put((byte) 0); // 0 is for plaintext vs bitmapimage (1) strings
	        buffer.putShort((short) timestamptextArray.length); 
	        buffer.put(timestamptextArray);
	        buffer.putShort((short) headertextArray.length); 
	        buffer.put(headertextArray);
	        buffer.putShort((short) bodytextArray.length); 
	        buffer.put(bodytextArray);
	        buffer.put((byte) 0); // 1 unknown
	        buffer.put((byte) 0); // 2 unknown
	        buffer.put((byte) 0); // 3 unknown
	        buffer.putShort((short)image.length);
	        buffer.put(image);
	        return buffer.array();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not found: " + e.getMessage(),
                    e);
        }
    }

}
