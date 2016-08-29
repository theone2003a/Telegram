package org.telegram.messenger.shamChat;

public class Constant {

	private Constant(){}
	
	public static final String AppTOKEN					= "e622c330c77a17c8426e638d7a85da6c2ec9f455";
	
	public static final String Domain					= "http://social.rabtcdn.com";
	public static final String Media					= Domain+"/media/";
	public static final String UserProfile				= Domain+"/pin/api/profile/profile/?token=";
	public static final String UserProfileByName		= Domain+"/pin/api/profile/profile/?user__username=";
	public static final String Follow					= Domain+"/pin/d/follow/";
	public static final String Posts					= Domain+"/pin/api/post/";
	public static final String Stream					= Domain+"/pin/api/friends_post/";
	public static final String Hasgtag					= Domain+"/pin/api/hashtag/";
	public static final String NewPost					= Domain+"/pin/d_send/";
	public static final String NewPostVideo				= Domain+"/pin/d/video/upload/?token=";
	public static final String Like						= Domain+"/pin/d_like2/?token=";
	public static final String PostDetails				= Domain+"/pin/api/post/";
	public static final String PostDelete				= Domain+"/pin/d/post/delete/";
	public static final String PostUpdate				= Domain+"/pin/d/post/update/";
	public static final String PostReport				= Domain+"/pin/d_post_report/?token=";
	public static final String Comments					= Domain+"/pin/api/com/comments/?object_pk=";
	public static final String PostComment				= Domain+"/pin/d_post_comment/?token=";
	public static final String PostSearch				= Domain+"/pin/api/search/posts/?offset=0&q=";
	public static final String UserSearch				= Domain+"/pin/api/search/?q=";
	public static final String PostLikers				= Domain+"/pin/api/like/likes/?post_id=";
	public static final String UserFollowers			= Domain+"/pin/api/follower/";
	public static final String UserFollowing			= Domain+"/pin/api/following/";
	
	public static final String Register					= Domain+"/api/user/register/";
	public static final String Login					= Domain+"/api/user/login/";
	public static final String UserAvailability			= Domain+"/api/v4/user/check/username/";
	
	public static final String ProfileUpdate			= Domain+"/profile/d_change/?token=";
	
	public static final String NotificationCount		= Domain+"/pin/api/notif/count/?token=";
	public static final String Notifications			= Domain+"/pin/api/notif/notify/?api_key=";
	
	public static final String UserExistVerify			= Domain+"/api/v4/user/salam/is/user/registered/";
	public static final String UpdateJaberID			= Domain+"/api/v4/user/salam/change/profile/jid/?token=";

	public static final String MqttTcpPort				= "1883";
//	public static final String MqttTcpHost				= "iot.eclipse.org";
	public static final String MqttTcpHost				= "sp.rabtcdn.com";
	
	public static final String MqttTopicCreation		= Domain+"/groups/api/v1/topics/create/";
	public static final String MqttTopicsList			= Domain+"/groups/api/v1/topics/user/";
	public static final String MqttDeleteTopic			= Domain+"/groups/api/v1/topics/delete/";
	public static final String MqttKickFromTopic		= Domain+"/groups/api/v1/topics/kick/";
	public static final String MqttInviteToTopic		= Domain+"/groups/api/v1/topics/invite/";
	public static final String MqttLeftFromTopic		= Domain+"/groups/api/v1/topics/left/";

	public static final String MqttChannelCreation		= Domain+"/groups/api/v1/channels/create/";
	public static final String MqttUpdateInfo			= Domain+"/groups/api/v1/channels/update/info/";
	public static final String MqttUserChannels			= Domain+"/groups/api/v1/channels/user/channels/";
	public static final String MqttChannelDetails		= Domain+"/groups/api/v1/channels/details/";
	public static final String MqttChannelDelete		= Domain+"/groups/api/v1/channels/delete/";
	public static final String MqttChannelLeft			= Domain+"/groups/api/v1/channels/left/";
	public static final String MqttChannelInvite		= Domain+"/groups/api/v1/channels/invite/";
	public static final String MqttChannelKick			= Domain+"/groups/api/v1/channels/kick/";
	public static final String MqttChannelGetPosts		= Domain+"/groups/api/v1/channels/posts/";
	public static final String MqttChannelSendPost		= Domain+"/groups/api/v1/channels/post/send/";
	public static final String MqttChannelDeletePost	= Domain+"/groups/api/v1/channels/post/delete/";
	public static final String SyncContacts				= Domain+"/groups/api/v1/contact/sync/";
	public static final String RegisterPhoneNumber		= Domain+"/groups/api/v1/auth/register/phone/";
	public static final String RegisterPhoneVerify		= Domain+"/groups/api/v1/auth/register/verify/";
	public static final String RegisterWhitPhone		= Domain+"/groups/api/v1/auth/register/user/with/phone/";
	public static final String RegisterPhoneResend		= Domain+"/groups/api/v1/auth/resend/verify/";
	public static final String RenameTopic		= Domain+"/groups/api/v1/topics/change/name/";
	public static final String DeleteTopic		= Domain+"/groups/api/v1/topics/delete/";






}
