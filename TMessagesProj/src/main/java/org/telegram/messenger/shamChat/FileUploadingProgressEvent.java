package org.telegram.messenger.shamChat;


public class FileUploadingProgressEvent {

	private String threadId;
	private String packetId;
	private int uploadedPercentage;

	
	public String getPacketId() {
		return packetId;
	}
	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	public int getUploadedPercentage() {
		return uploadedPercentage;
	}
	public void setUploadedPercentage(int uploadedPercentage) {
		this.uploadedPercentage = uploadedPercentage;
	}

}

