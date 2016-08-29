package org.telegram.messenger.shamChat;

import android.content.Context;
import android.database.Cursor;



import java.util.Date;

public class FriendGroup {
	public static String DB_ID = "friend_group_id";
	public static String DB_NAME = "friend_group_name";
	public static String DB_RECORD_OWNER = "record_owner_id";
	public static String CHAT_ROOM_NAME = "chat_room_name";
	public static String DID_JOIN_ROOM = "did_join_room";
	public static String DID_LEAVE = "did_leave";
	public static String IS_MUTE = "is_mute";
	public static String DB_DESCRIPTION = "friend_group_description";
	public static String DB_LINK_NAME = "friend_group_link_name";
	public static String CHANNEL_LOGO = "logo" ;

	private String id;
	private String name;
	private String recordOwnerId;
	private String chatRoomName;
	private String dbRecordId;

	private String groupDescription;
	private String groupLinkName;

	public FriendGroup(String name, String ownerId) {
		setName(name);
		setRecordOwnerId(ownerId);
	}

	public FriendGroup() {
	}

	public String getId() {
		return id;
	}

	public FriendGroup setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public FriendGroup setName(String name) {
		this.name = name;
		return this;
	}

	public String getRecordOwnerId() {
		return recordOwnerId;
	}

	public FriendGroup setRecordOwnerId(String recordOwnerId) {
		this.recordOwnerId = recordOwnerId;
		return this;
	}

	public FriendGroup assignUniqueId() {
//		setId("G" + DatabaseTest.getUniqueId());
		setId("g" + recordOwnerId + "_" + Utils.formatDate(new Date().getTime(),"yyyyMMddHHmmss"));
		return this;
	}

	@Override
	public String toString() {
		return "Id:" + getId() + " Name:" + getName() + " Owner:" + getRecordOwnerId();
	}
//reza_ak
	/*public static String getNextAvailableGroupName(Context context) {
		int i;

		Cursor cursor = context.getContentResolver().query(UserProvider.CONTENT_URI_GROUP, new String [] {FriendGroup.DB_ID}, null, null, null);

		i=cursor.getCount()+1;

		cursor.close();

		return "Group " + i;
	}*/
//reza_ak
	/*public static String getNextAvailableChannelName(Context context) {
		int i;

		Cursor cursor = context.getContentResolver().query(UserProvider.CONTENT_URI_GROUP, new String [] {FriendGroup.DB_ID}, null, null, null);

		i=cursor.getCount()+1;

		cursor.close();

		return "Channel " + i;
	}*/


	public String getChatRoomName() {
		return chatRoomName;
	}

	public void setChatRoomName(String chatRoomName) {
		this.chatRoomName = chatRoomName;
	}





	public String getGroupDescription() {
		return groupDescription;
	}
	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public String getGroupLinkName() {
		return groupLinkName;
	}
	public void setGroupLinkName(String groupLinkName) {
		this.groupLinkName = groupLinkName;
	}

	public String getDbRecordId() {
		return dbRecordId;
	}

	public void setDbRecordId(String dbRecordId) {
		this.dbRecordId = dbRecordId;
	}
}
