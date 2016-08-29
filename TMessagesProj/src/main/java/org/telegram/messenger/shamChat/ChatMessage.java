package org.telegram.messenger.shamChat;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;



public class ChatMessage implements Parcelable {

	public static final String THREAD_ID = "thread_id";
	public static final String MESSAGE_SENDER = "message_sender";
	public static final String MESSAGE_RECIPIENT = "message_recipient";
	public static  final  String CHANNEL_ID = "channel_id"  ;
	public static final String MESSAGE_CONTENT_TYPE = "message_content_type";
	public static final String TEXT_MESSAGE = "text_message";
	public static final String BLOB_MESSAGE = "blob_message";
	public static final String MESSAGE_DATETIME = "message_datetime";
	public static final String LAST_UPDATED_DATE_TIME = "message_last_updated_datetime";
	public static final String DELIVERED_DATETIME = "delivered_datetime";

	public static final String CHANNEL_VIEW = "channel_view"; //// very important add


	public static final String MESSAGE_STATUS = "message_status";
	public static final String MESSAGE_TYPE = "message_type";

	public static final String PACKET_ID = "packet_id";

	public static final String DESCRIPTION = "description";

	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";

	public static final String SENDERS_MOBILE_NO = "senders_mobile_number";

	public static final String FILE_SIZE = "file_size";
	public static final String UPLOADED_PERCENTAGE = "uploaded_percentage";

	public static final String FILE_URL = "file_url";

	public static final String UPLOADED_FILE_URL = "uploaded_file_url";

	public static final String GROUP_ID = "group_id";

	public  static  final  String ISFORWARD =  "isforward" ;
	public  static  final  String CHANNELTITLE =  "channeltitle" ;
	public  static  final  String CHANNELHASHCODE =  "channelhashcode" ;
	public  static  final  String ORGINALPACKETID =  "orgianlpacketid" ;




	private int messageId;
	private String threadId;

	private String sender;
	private String recipient;

	private MessageContentTypeProvider.MessageContentType messageContentType;
	private String textMessage;
	private byte[] blobMessage;

	private String messageDateTime;

	private String lastUpdatedDateTime;

	private String deliveredDateTime;

	private boolean offlineSent;
	private MyMessageType incomingMessage;

	private boolean sentSeen;

	private String packetId;

	private String description;

	private double longitude;
	private double latitude;

	private  String ViewChnnel = "0" ;

	private String sendersMobileNumber;

	private long fileSize;
	private int uploadedPercentage;

	private MessageStatusType messageStatus;

	private String fileUrl;

	private String uploadedFileUrl;

	private User user;

	private boolean isForward  ;
	private  String ChannelTile , ChannelHashcode , OrginalPacketId ;


	public  void setIsforward(boolean isForward) {this.isForward  =  isForward;}
	public  void setChanneltitle(String ChannelTile) {this.ChannelTile = ChannelTile ;}
	public  void setChannelHashcode (String ChannelHashcode) {this.ChannelHashcode = ChannelHashcode ;}
	public void setOrginalPacketId (String OrginalPacketId) {this.OrginalPacketId = OrginalPacketId ;}
	public boolean getIsforward () {return  this.isForward ; }
	public  String getChanneltitle () {return  this.ChannelTile ; } ;
	public  String getChannelhashcode () {return  this.ChannelHashcode ; }
	public  String getOrginalpacketid () {return  this.OrginalPacketId ;}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(messageId);
		dest.writeString(threadId);

		dest.writeString(sender);
		dest.writeString(recipient);

		dest.writeInt(messageContentType.getType());
		dest.writeString(textMessage);
		dest.writeInt(blobMessage.length);
		dest.writeByteArray(blobMessage);

		dest.writeLong(Utils.getDateFromStringDate(messageDateTime).getTime());
		dest.writeLong(Utils.getDateFromStringDate(deliveredDateTime).getTime());

		dest.writeInt(offlineSent ? 1 : 0);
		dest.writeInt(incomingMessage.ordinal());

		dest.writeInt(sentSeen ? 1 : 0);

		dest.writeString(packetId);

		dest.writeString(description);

		dest.writeString(sendersMobileNumber);
		dest.writeLong(fileSize);
		dest.writeInt(uploadedPercentage);

	}

	public ChatMessage(Parcel in) {

		setMessageId(in.readInt());
		setThreadId(in.readString());

		setSender(in.readString());
		setRecipient(in.readString());
//reza_ak
		//setMessageContentType(readMessageContentType(in.readInt()));
		setTextMessage(in.readString());
		byte[] temp = new byte[in.readInt()];
		in.readByteArray(temp);
		setBlobMessage(temp);

		setMessageDateTime(Utils.formatDate(in.readLong(),
				"yyyy/MM/dd HH:mm:ss"));
		setDeliveredDateTime(Utils.formatDate(in.readLong(),
				"yyyy/MM/dd HH:mm:ss"));
		setOfflineSent(in.readInt() == 1 ? true : false);
		setIncomingMessage(readMessageType(in.readInt()));

		setSentSeen(in.readInt() == 1 ? true : false);

		setPacketId(in.readString());
		setDescription(in.readString());

		setSendersMobileNumber(in.readString());

		setFileSize(in.readLong());
		setUploadedPercentage(in.readInt());
	}

	private MyMessageType readMessageType(int type) {
		MyMessageType messageType = MyMessageType.HEADER_MSG;
		switch (type) {
			case 0:
				messageType = MyMessageType.OUTGOING_MSG;
				break;
			case 1:
				messageType = MyMessageType.INCOMING_MSG;
				break;

		}
		return messageType;
	}
//reza_ak
/*	private MessageContentType readMessageContentType(int type) {
		MessageContentType messageContentType = MessageContentType.TEXT;
		switch (type) {
			case 1:
				messageContentType = MessageContentType.IMAGE;
				break;
			case 2:
				messageContentType = MessageContentType.STICKER;
				break;
			case 3:
				messageContentType = MessageContentType.VOICE_RECORD;
				break;
			case 4:
				messageContentType = MessageContentType.FAVORITE;
				break;
			case 5:
				messageContentType = MessageContentType.MESSAGE_WITH_IMOTICONS;
				break;
			case 6:
				messageContentType = MessageContentType.LOCATION;
				break;
			case 7:
				messageContentType = MessageContentType.INCOMING_CALL;
				break;
			case 8:
				messageContentType = MessageContentType.OUTGOING_CALL;
				break;
			case 9:
				messageContentType = MessageContentType.VIDEO;
				break;
			case 11:
				messageContentType = MessageContentType.GROUP_INFO;
				break;

		}

		return messageContentType;
	}*/

	public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
		public ChatMessage createFromParcel(Parcel in) {
			return new ChatMessage(in);
		}

		public ChatMessage[] newArray(int size) {
			return new ChatMessage[size];
		}
	};
//reza_ak
	/*public ChatMessage(int messageId, String threadId,


					   MessageContentType messageContentType, String textMessage,
					   byte[] blobMessage, String messageDateTime,
					   String deliverdDateTime, MyMessageType messageType,
					   String packetId, String sender, String recipient,
					   String description, double longitude, double latitude,
					   String sendersMobileNumber, long fileSize, int uploadedPercentage,
					   String fileUrl, MessageStatusType messageStatus,
					   String lastUpdatedDateTime, String uploadedFileUrl , String ViewChnnel , boolean isForwarded , String ChannelTitle , String ChannelHashcode , String OrginalPacketId ) {

		this.ViewChnnel  = ViewChnnel ;


		setIsforward(isForwarded) ;

		Log.d("isForwardedisForwarded" , isForwarded + " ss")  ;




		if (isForwarded) {
			setChanneltitle(ChannelTitle);
			setChannelHashcode(ChannelHashcode);
			setOrginalPacketId(OrginalPacketId);
		}




		setMessageId(messageId);
		setThreadId(threadId);
		setMessageContentType(messageContentType);
		setTextMessage(textMessage);
		setBlobMessage(blobMessage);

		setMessageDateTime(messageDateTime);
		setDeliveredDateTime(deliverdDateTime);

		setOfflineSent(offlineSent);
		setIncomingMessage(messageType);

		setSentSeen(sentSeen);

		setPacketId(packetId);
		setSender(sender);
		setRecipient(recipient);

		setDescription(description);

		setLongitude(longitude);
		setLatitude(latitude);

		setSendersMobileNumber(sendersMobileNumber);
		setFileSize(fileSize);
		setUploadedPercentage(uploadedPercentage);

		setFileUrl(fileUrl);

		setMessageStatus(messageStatus);

		setLastUpdatedDateTime(lastUpdatedDateTime);

		setUploadedFileUrl(uploadedFileUrl);
	}*/

	public ChatMessage() {

	}

	public enum MessageStatusType {
		QUEUED(0), SENDING(1), SENT(2), DELIVERED(3) , SEEN(4), FAILED(5);

		private int type ;

		MessageStatusType(int type) {
			this.setType(type);
		}

		public int getType() {
			return type;
		}

		private void setType(int type) {
			this.type = type;
		}


	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public String getDeliveredDateTime() {
		return deliveredDateTime;
	}

	public void setDeliveredDateTime(String deliveredDateTime) {
		this.deliveredDateTime = deliveredDateTime;
	}

	/***
	 * get message received time or sent time
	 *
	 * @return
	 */
	public String getMessageDateTime() {
		return messageDateTime;
	}

	public void setMessageDateTime(String message_date) {
		this.messageDateTime = message_date;
	}

	public byte[] getBlobMessage() {
		return blobMessage;
	}

	/**
	 * set message content(like images, sound files, stickers),
	 *
	 * @warning: message MessageType is directly effect on this
	 * @param blobMessage
	 */
	public void setBlobMessage(byte[] blobMessage) {
		this.blobMessage = blobMessage;
	}

	public MessageContentTypeProvider.MessageContentType getMessageContentType() {
		return messageContentType;
	}

	public void setMessageContentType(MessageContentTypeProvider.MessageContentType messageContentType) {
		this.messageContentType = messageContentType;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public boolean isOfflineSent() {
		return offlineSent;
	}

	/***
	 * message sent when
	 *
	 * @param offlineSent
	 */
	public void setOfflineSent(boolean offlineSent) {
		this.offlineSent = offlineSent;
	}

	public MyMessageType getMessageType() {
		return incomingMessage;
	}

	public void setIncomingMessage(MyMessageType messageType) {
		this.incomingMessage = messageType;
	}

	public String getPacketId() {
		return packetId;
	}

	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}

	public boolean isSentSeen() {
		return sentSeen;
	}

	public void setSentSeen(boolean sentSeen) {
		this.sentSeen = sentSeen;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getDescription() {
		return description;
	}

	/***
	 * you can set a description to a message
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getSendersMobileNumber() {
		return sendersMobileNumber;
	}

	public void setSendersMobileNumber(String sendersMobileNumber) {
		this.sendersMobileNumber = sendersMobileNumber;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getUploadedPercentage() {
		return uploadedPercentage;
	}

	public void setUploadedPercentage(int uploadedPercentage) {
		this.uploadedPercentage = uploadedPercentage;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public MessageStatusType getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(MessageStatusType messageStatus) {
		this.messageStatus = messageStatus;
	}

	public String getLastUpdatedDateTime() {
		return lastUpdatedDateTime;
	}

	public void setLastUpdatedDateTime(String lastUpdatedDateTime) {
		this.lastUpdatedDateTime = lastUpdatedDateTime;
	}

	public String getUploadedFileUrl() {
		return uploadedFileUrl;
	}

	public void setUploadedFileUrl(String uploadedFileUrl) {
		this.uploadedFileUrl = uploadedFileUrl;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}


	public String getChannelView () {
		return  this.ViewChnnel ;
	}

}
