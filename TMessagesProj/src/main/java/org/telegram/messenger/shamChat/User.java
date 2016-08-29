package org.telegram.messenger.shamChat;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;



import org.json.JSONObject;

import java.text.Collator;


public class User implements Comparable<User>, Parcelable {

	private String userId;
	private String username;
	private String chatId;
	private String mobileNo;
	private String email;
	private String gender;
	private String profileImage;
	private int onlineStatus;
	private String myStatus;
	private String newMessageAlert;
	private String inAppAlert;
	private String emailVerificationStatus;
	private String tmpUserId;
	private String cityOrRegion;
	private String coverPhoto;
	private String jabberdResource;
	private BooleanStatus findMeByPhoneNo;
	private BooleanStatus findMeByShamId;
	private Boolean isMentionChecked = false;
	private String profileImageUrl;
	private BooleanStatus isInChat;
	private String dbRowId;

	private String NULL = "null";
	private boolean isBlocked;

	// Not saved on db, temporal values:
	public boolean takenFromPhoneContacts = false;
	public boolean shamMyContactUser = false;
	// for checkbox views
	public boolean checked;
	
	private int statusMode;
	
	private boolean isVCardDownloaded;
	private boolean isAddedToRoster;


	public User() {

	}

	public User(JSONObject userJsonObject) throws Exception {
		//reza_ak
		/*

		if (userJsonObject.has(UserConstants.USER_ID)) {
			this.userId = "" + userJsonObject.getInt(UserConstants.USER_ID);
		}
		if (userJsonObject.has(UserConstants.USERNAME)) {

			this.username = !userJsonObject.getString(UserConstants.USERNAME).equals(NULL) ? userJsonObject.getString(UserConstants.USERNAME) : "";
		}
		if (userJsonObject.has(UserConstants.CHAT_ID)) {

			this.chatId = !userJsonObject.getString(UserConstants.CHAT_ID).equals(NULL) ? userJsonObject.getString(UserConstants.CHAT_ID) : "";
		}
		if (userJsonObject.has(UserConstants.MOBILE_NO)) {
			this.mobileNo = !userJsonObject.getString(UserConstants.MOBILE_NO).equals(NULL) ? userJsonObject.getString(UserConstants.MOBILE_NO)
					: "";

		}
		if (userJsonObject.has(UserConstants.EMAIL)) {

			this.email = !userJsonObject.getString(UserConstants.EMAIL).equals(NULL) ? userJsonObject.getString(UserConstants.EMAIL) : "";
		}
		if (userJsonObject.has("profileImageUrl")) {
			this.profileImage = "";
			this.profileImageUrl = !userJsonObject.getString("profileImageUrl").equals(NULL) ? userJsonObject
					.getString("profileImageUrl") : "";
			Thread imageDownloader = new Thread() {
				@Override
				public void run() {
					if (profileImageUrl != null && profileImageUrl.length() > 0
							&& !profileImageUrl.equalsIgnoreCase("exception")
							&& !profileImageUrl.equalsIgnoreCase("null")) {
						byte[] blobMessage = new Utils().downloadImageFromUrl(profileImageUrl);
						if (blobMessage != null)
							profileImage = Base64.encodeToString(blobMessage, Base64.DEFAULT);
					}
					super.run();
				}
			};
			imageDownloader.start();
			imageDownloader.join();

		}
		if (userJsonObject.has(UserConstants.TEMP_USER_ID)) {
			this.tmpUserId = !userJsonObject.getString(UserConstants.TEMP_USER_ID).equals(NULL) ? userJsonObject
					.getString(UserConstants.TEMP_USER_ID) : "";

		}
		if (userJsonObject.has(UserConstants.IN_APP_ALERT)) {
			this.inAppAlert = !userJsonObject.getString(UserConstants.IN_APP_ALERT).equals(NULL) ? userJsonObject
					.getString(UserConstants.IN_APP_ALERT) : "";

		}
		if (userJsonObject.has(UserConstants.EMAIL_VERIFICATION_STATUS)) {

			this.emailVerificationStatus = !userJsonObject.getString(UserConstants.EMAIL_VERIFICATION_STATUS).equals(NULL) ? userJsonObject
					.getString(UserConstants.EMAIL_VERIFICATION_STATUS) : "";
		}
		if (userJsonObject.has(UserConstants.NEW_MESSAGE_ALERT)) {

			this.newMessageAlert = !userJsonObject.getString(UserConstants.NEW_MESSAGE_ALERT).equals(NULL) ? userJsonObject
					.getString(UserConstants.NEW_MESSAGE_ALERT) : "";
		}
		if (userJsonObject.has(UserConstants.GENDER)) {

			this.gender = !userJsonObject.getString(UserConstants.GENDER).equals(NULL) ? userJsonObject.getString(UserConstants.GENDER) : "";
		}
//		if (userJsonObject.has(UserConstants.ONLINE_STATUS)) {
//
//			this.onlineStatus = !userJsonObject.getString(UserConstants.ONLINE_STATUS).equals(NULL) ? userJsonObject
//					.getString(UserConstants.ONLINE_STATUS) : "Offline";
//		}
		if (userJsonObject.has(UserConstants.MY_STATUS)) {

			this.myStatus = !userJsonObject.getString(UserConstants.MY_STATUS).equals(NULL) ? userJsonObject.getString(UserConstants.MY_STATUS)
					: "";
			System.out.println("User status in user class "+this.myStatus);
		}

		if (userJsonObject.has(UserConstants.CITY_OR_REGION)) {

			this.cityOrRegion = !userJsonObject.getString(UserConstants.CITY_OR_REGION).equals(NULL) ? userJsonObject
					.getString(UserConstants.CITY_OR_REGION) : "";
		}
		
		this.isInChat= BooleanStatus.TRUE;*/

	}

	public String getUserId() {
		return userId;
	}

	public User setUserId(String userId) {
		this.userId = userId;

		return this;
	}

	public String getChatId() {
		return chatId;
	}

	public User setChatId(String chatId) {
		this.chatId = chatId;

		return this;
	}

	public String getUsername() {
		return username;
	}

	public User setUsername(String username) {
		this.username = username;

		return this;
	}

	public String getGender() {
		return gender;
	}

	public User setGender(String gender) {
		this.gender = gender;

		return this;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public User setProfileImage(String profileImage) {
		this.profileImage = profileImage;

		return this;
	}


	public int getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(int onlineStatus) {
		this.onlineStatus = onlineStatus;

	}

	public String getMyStatus() {
		return myStatus;
	}

	public void setMyStatus(String status) {
		this.myStatus = status;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public User setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;

		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNewMessageAlert() {
		return newMessageAlert;
	}

	public void setNewMessageAlert(String newMessageAlert) {
		this.newMessageAlert = newMessageAlert;
	}

	public String getInAppAlert() {
		return inAppAlert;
	}

	public void setInAppAlert(String inAppAlert) {
		this.inAppAlert = inAppAlert;
	}

	public String getEmailVerificationStatus() {
		return emailVerificationStatus;
	}

	public void setEmailVerificationStatus(String emailVerificationStatus) {
		this.emailVerificationStatus = emailVerificationStatus;
	}

	public String getTmpUserId() {
		return tmpUserId;
	}

	public void setTmpUserId(String tmpUserId) {
		this.tmpUserId = tmpUserId;
	}

	

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (this == o)
			return true;

		if (!(o instanceof User))
			return false;

		User other = (User) o;

		if (getUserId().length() == 0 && other.getUserId().length() == 0)
			return false;

		return other.getUserId().equals(getUserId());
	}
	
	public enum BooleanStatus {
		FALSE(0), TRUE(1);
		private int status;

		BooleanStatus(int status) {
			this.setStatus(status);
		}

		public int getStatus() {
			return status;
		}

		private void setStatus(int status) {
			this.status = status;
		}
	}

	@Override
	public int compareTo(User another) {
		return Collator.getInstance().compare(username.toUpperCase(), another.username.toUpperCase());
	}

	@Override
	public String toString() {
		return "Id:" + getUserId() + " Name:" + getUsername();
	}

	public String getCityOrRegion() {
		return cityOrRegion;
	}

	public void setCityOrRegion(String cityOrRegion) {
		this.cityOrRegion = cityOrRegion;
	}

	public String getCoverPhoto() {
		return coverPhoto;
	}

	public void setCoverPhoto(String coverPhoto) {
		this.coverPhoto = coverPhoto;
	}

	public Boolean getIsMentionChecked() {
		return isMentionChecked;
	}

	public void setIsMentionChecked(Boolean isMentionChecked) {
		this.isMentionChecked = isMentionChecked;
	}

	public String getJabberdResource() {
		return jabberdResource;
	}

	public void setJabberdResource(String jabberdResource) {
		this.jabberdResource = jabberdResource;
	}

	public BooleanStatus getFindMeByPhoneNoStatus() {
		return findMeByPhoneNo;
	}

	public void setFindMeByPhoneNoStatus(BooleanStatus findMeByPhoneNo) {
		this.findMeByPhoneNo = findMeByPhoneNo;
	}

	public BooleanStatus getFindMeByShamIdStatus() {
		return findMeByShamId;
	}

	public void setFindMeByShamIdStatus(BooleanStatus findMeByShamId) {
		this.findMeByShamId = findMeByShamId;
	}

	public String getprofileImageUrl() {
		return profileImageUrl;
	}

	public void setprofileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public BooleanStatus getIsInChat() {
		return isInChat;
	}

	public void setIsInChat(BooleanStatus isInChat) {
		this.isInChat = isInChat;
	}

	public String getDbRowId() {
		return dbRowId;
	}

	public void setDbRowId(String dbRowId) {
		this.dbRowId = dbRowId;
	}
	
	private User(Parcel in) {
		userId = in.readString();
		username = in.readString();
		chatId = in.readString();
		mobileNo = in.readString();
		email = in.readString();
		gender = in.readString();
		profileImage = in.readString();
		onlineStatus = in.readInt();
		myStatus = in.readString();
		newMessageAlert = in.readString();
		inAppAlert = in.readString();
		emailVerificationStatus = in.readString();
		tmpUserId = in.readString();
		cityOrRegion = in.readString();
		coverPhoto = in.readString();
		jabberdResource = in.readString();
		profileImageUrl = in.readString();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static final Creator<User> CREATOR = new Creator<User>() {
		@Override
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
	};

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(userId);
		out.writeString(username);
		out.writeString(chatId);
		out.writeString(mobileNo);
		out.writeString(email);
		out.writeString(gender);
		out.writeString(profileImage);
		out.writeInt(onlineStatus);
		out.writeString(myStatus);
		out.writeString(newMessageAlert);
		out.writeString(inAppAlert);
		out.writeString(emailVerificationStatus);
		out.writeString(tmpUserId);
		out.writeString(cityOrRegion);
		out.writeString(coverPhoto);
		out.writeString(jabberdResource);
		out.writeString(profileImageUrl);
		
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	public int getStatusMode() {
		return statusMode;
	}

	public void setStatusMode(int statusMode) {
		this.statusMode = statusMode;
	}

	public boolean isVCardDownloaded() {
		return isVCardDownloaded;
	}

	public void setVCardDownloaded(boolean isVCardDownloaded) {
		this.isVCardDownloaded = isVCardDownloaded;
	}

	public boolean isAddedToRoster() {
		return isAddedToRoster;
	}

	public void setAddedToRoster(boolean isAddedToRoster) {
		this.isAddedToRoster = isAddedToRoster;
	}


}
