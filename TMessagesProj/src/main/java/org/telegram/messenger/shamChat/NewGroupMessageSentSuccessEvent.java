package org.telegram.messenger.shamChat;


import org.telegram.messenger.shamChat.MessageContentTypeProvider.MessageContentType;

import org.json.JSONException;
import org.json.JSONObject;

public class NewGroupMessageSentSuccessEvent {
	
	private String jsonMessageString;

	public String threadId = null;
	
	public JSONObject SampleMsg = null;
	public  String packetId = null;	
	public  String from = null;	
	public String to=null;	
	public String messageTypeDesc = null;
	int messageType = 0;
	String messageBody = null;
	String latitude=null;
	String timestamp=null;	
	String longitude=null;
	String packetType=null;	
	//String groupOwnerId = null;
	int isGroupChat = 0; 
	
	public NewGroupMessageSentSuccessEvent(String jsonMessage)
	{
			
		this.jsonMessageString = jsonMessage;
			try {
				SampleMsg = new JSONObject(jsonMessageString);
				packetId = SampleMsg.getString("packetId");
				packetType = SampleMsg.getString("packet_type");					
				from = SampleMsg.getString("from");					
				to = SampleMsg.getString("to");	
				messageTypeDesc = SampleMsg.getString("messageTypeDesc");
				timestamp = SampleMsg.getString("timestamp");
				messageType = SampleMsg.getInt("messageType");
				
				if (messageType ==  MessageContentType.LOCATION.ordinal())
				{
					latitude = SampleMsg.getString("latitude");
					longitude = SampleMsg.getString("longitude");					
				}
				
				messageBody = SampleMsg.getString("messageBody");
			//groupOwnerId = SampleMsg.getString("groupOwnerId");					
			isGroupChat = SampleMsg.getInt("isGroupChat");					
			//reza_ak
			String threadOwner = "6";
			threadId = threadOwner + "-" + to; 
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		
	}

	public String getThreadId() {
		return threadId;
	}
	
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	
	public String getPacketId() {
		return packetId;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}
	
	public String getHashCode() {
		return to;
	}

	public String getMssageTypeDesc() {
		return messageTypeDesc;
	}

	public String getTimeStamp() {
		return timestamp;
	}
	
	public int getMessageType() {
		return messageType;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getJsonMessageString(){
		return jsonMessageString;	
	}
	
	public String getPacketType(){
		return packetType;	
	}
	

}
