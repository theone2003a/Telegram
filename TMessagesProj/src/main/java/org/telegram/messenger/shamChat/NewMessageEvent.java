package org.telegram.messenger.shamChat;

public class NewMessageEvent {
	
	private String threadId;
	private int direction;
	private String jsonMessageString;
	private String packetId=null;
	public boolean consumed = false; 
	
	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public String getPacketId() {
		return packetId;
	}

	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}
	
	public String getJsonMessageString() {
		return jsonMessageString;
	}

	public void setJsonMessageString(String jsonMessageString) {
		this.jsonMessageString = jsonMessageString;
	}
	
	public boolean getConsumed() {
		return consumed;
	}

	public void setConsumed(boolean consumed) {
		this.consumed = consumed;
	}
	

}
