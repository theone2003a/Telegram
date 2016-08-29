package org.telegram.messenger.shamChat;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class MessageThread{
	public static final String TABLE_NAME = "chat_thread";
	public static final String THREAD_ID = "thread_id";
	public static final String FRIEND_ID = "friend_id";
	public static final String THREAD_OWNER = "thread_owner";
	public static final String LAST_UPDATED_DATETIME = "last_updated_datetime";
	public static final String THREAD_STATUS = "thread_status";
	public static final String IS_GROUP_CHAT = "is_group_chat";
	public static final String READ_STATUS = "read_status";
	public static final String LAST_MESSAGE = "last_message";
	public static final String LAST_MESSAGE_CONTENT_TYPE = "last_message_content_type";
	public static final String LAST_MESSAGE_DIRECTION = "last_message_direction";
	
	private String threadId;
	private Date lastUpdateTime;
	private boolean threadStatus;
	private boolean isGroupChat;
	private String threadOwner;
	private String friendId;

	private  String logo = "http://google.com" ;
	private String lastMessage;
	private int lastMessageDirection;
	private Date lastUpdatedDate;
	private int lastMessageMedium;
	
	private String username;
	
	
	private int messageCount;
	private String friendProfileImageUrl;

	public MessageThread() {
	}

	public MessageThread(String threadId, Date lastUpdateTime, boolean threadStatus, boolean isGroupChat,String threadOwner,String friendId) {
		this.threadId = threadId;
		this.lastUpdateTime = lastUpdateTime;
		this.threadStatus = threadStatus;
		this.isGroupChat = isGroupChat;
		this.threadOwner = threadOwner;
		this.friendId = friendId;
	}

	public MessageThread(Parcel in) {
		this.setThreadId(in.readString());
		this.setLastUpdateTime(new Date(in.readLong()));
		this.setThreadStatus(in.readInt() == 1 ? true : false);
		this.setGroupChat(in.readInt() == 1 ? true : false);
		this.setThreadOwner(in.readString());
		this.setFriendId(in.readString());

	}


	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	/**
	 * thread id will also be this one because chat thread is unique to each user
	 * @return
	 */
	public String getThreadId() {
		return threadOwner + "-" + friendId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public boolean isThreadActive() {
		return threadStatus;
	}

	public void setThreadStatus(boolean threadStatus) {
		this.threadStatus = threadStatus;
	}

	public boolean isGroupChat() {
		return isGroupChat;
	}

	public void setGroupChat(boolean isGroupChat) {
		this.isGroupChat = isGroupChat;
	}

	public static final Parcelable.Creator<MessageThread> CREATOR = new Parcelable.Creator<MessageThread>() {
		public MessageThread createFromParcel(Parcel in) {
			return new MessageThread(in);
		}

		public MessageThread[] newArray(int size) {
			return new MessageThread[size];
		}
	};

	public String getThreadOwner() {
		return threadOwner;
	}

	public void setThreadOwner(String thread_owner) {
		this.threadOwner = thread_owner;
	}

	public String getFriendId() {
		return friendId;
	}

	public void setFriendId(String friendId) {
		this.friendId = friendId;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public  void setLogo (String logo) {this.logo = logo ;}

	public  String getLogo () {return  logo ; }

	public int getLastMessageDirection() {
		return lastMessageDirection;
	}

	public void setLastMessageDirection(int lastMessageDirection) {
		this.lastMessageDirection = lastMessageDirection;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public int getLastMessageMedium() {
		return lastMessageMedium;
	}

	public void setLastMessageMedium(int lastMessageMedium) {
		this.lastMessageMedium = lastMessageMedium;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

	public String getFriendProfileImageUrl() {
		return friendProfileImageUrl;
	}

	public void setFriendProfileImageUrl(String friendProfileImageUrl) {
		this.friendProfileImageUrl = friendProfileImageUrl;
	}

	
	

}
