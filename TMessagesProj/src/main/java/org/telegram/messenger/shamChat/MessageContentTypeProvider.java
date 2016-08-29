package org.telegram.messenger.shamChat;

public class MessageContentTypeProvider {

	public enum MessageContentType {
		  TEXT(0), IMAGE(1) , STICKER(2), VOICE_RECORD(3), FAVORITE(4), MESSAGE_WITH_IMOTICONS(5), LOCATION(6), INCOMING_CALL(
		    7), OUTGOING_CALL(8) , VIDEO(9) ,MISSED_CALL(10), GROUP_INFO(11);

		  private int type;

		  MessageContentType(int type) {
		   this.setType(type);
		  }

		  public int getType() {
		   return type;
		  }

		  private void setType(int type) {
		   this.type = type;
		  }

		 }

}
