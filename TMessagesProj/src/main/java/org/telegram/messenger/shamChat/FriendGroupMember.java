package org.telegram.messenger.shamChat;

import java.util.Date;

public class FriendGroupMember {
	public static String DB_ID = "friend_group_member_id";
	public static String DB_GROUP = "friend_group_id";
	public static String DB_FRIEND = "friend_id";
	public static String DB_FRIEND_DID_JOIN = "friend_did_join";
	public static String DB_FRIEND_IS_ADMIN = "friend_is_admin";
	public static String PHONE_NUMBER = "phone_number";

	private String id;
	private String groupID;
	private String friendId;
	private String phoneNumber;	
	private boolean isAdmin;
	private boolean didJoin;
	private User user;
	
	public FriendGroupMember(String groupId, String friendId) {
		setGroupID(groupId);
		setFriendId(friendId);
	}

	public FriendGroupMember() {
	}

	public String getId() {
		return id;
	}

	public FriendGroupMember setId(String id) {
		this.id = id;
		return this;
	}

	public String getGroupID() {
		return groupID;
	}

	public FriendGroupMember setGroupID(String groupID) {
		this.groupID = groupID;
		return this;
	}

	public String getFriendId() {
		return friendId;
	}

	public FriendGroupMember setFriendId(String friendId) {
		this.friendId = friendId;
		return this;
	}

	public FriendGroupMember assignUniqueId(String ownerId) {
		id = "M" + ownerId + "_" + new Date().getTime();
		return this;
	}

	@Override
	public String toString() {
		return "Id:" + getId() + " GroupId:" + getGroupID() + " FriendId:" + getFriendId();
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean isDidJoin() {
		return didJoin;
	}

	public void setDidJoin(boolean didJoin) {
		this.didJoin = didJoin;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public FriendGroupMember setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}
	
}
