package org.telegram.messenger.shamChat;

public class UpdateGroupMembersList {

	private String threadId;
	private String groupId;

	public UpdateGroupMembersList(String threadId, String groupId){
		this.threadId = threadId;
		this.groupId = groupId;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}	

}

