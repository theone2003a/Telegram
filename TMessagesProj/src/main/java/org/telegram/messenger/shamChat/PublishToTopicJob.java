package org.telegram.messenger.shamChat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.widget.Toast;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.mqtt.Connections;
import org.telegram.messenger.mqtt.NotifySimple;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

public class PublishToTopicJob extends Job {

	private static final long serialVersionUID = 1L;

	public static final int PRIORITY = 1000;

	boolean DEBUG = false;

	boolean isRetry = false;
	private static final AtomicInteger jobCounter = new AtomicInteger(0);
	private final int id;

	private String jsonMessageString;
	private String topicName;

	public String threadId = null;


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



	public PublishToTopicJob(String jsonMessageString, String topicName) {
		super(new Params(PRIORITY).persist().requireNetwork());
		id = jobCounter.incrementAndGet();

		initClass(jsonMessageString, topicName);

	}

	public PublishToTopicJob(String jsonMessageString, String topicName, boolean isRetry) {
		super(new Params(PRIORITY).persist().requireNetwork());
		id = jobCounter.incrementAndGet();

		this.isRetry = isRetry;

		initClass(jsonMessageString, topicName);

	}

	public void initClass(String jsonMessageString, String topicName) {


		this.jsonMessageString = jsonMessageString;
		this.topicName = topicName;

		JSONObject SampleMsg = null;
		try {
			SampleMsg = new JSONObject(jsonMessageString);
			packetId = SampleMsg.getString("packetId");
			packetType = SampleMsg.getString("packet_type");
			from = SampleMsg.getString("from");
			to = SampleMsg.getString("to");
			messageTypeDesc = SampleMsg.getString("messageTypeDesc");
			timestamp = SampleMsg.getString("timestamp");
			messageType = SampleMsg.getInt("messageType");

			if (messageType == MessageContentTypeProvider.MessageContentType.LOCATION.ordinal())
			{
				latitude = SampleMsg.getString("latitude");
				longitude = SampleMsg.getString("longitude");
			}

			messageBody = SampleMsg.getString("messageBody");
			//groupOwnerId = SampleMsg.getString("groupOwnerId");
			isGroupChat = SampleMsg.getInt("isGroupChat");

			String threadOwner = "user102015";
			threadId = threadOwner + "-" + to;

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	@Override
	public void onAdded() {
		// Job has been saved to disk.
		// This is a good place to dispatch a UI event to indicate the job will
		// eventually run.
		// In this example, it would be good to update the UI with the newly
		// posted tweet.
/*
		//if this is text message or sticker add it to database first
		//if this is not a retry operation we add message to database
		if (isRetry== false)
		{
			if (messageType == MessageContentTypeProvider.MessageContentType.TEXT.ordinal() || messageType == MessageContentTypeProvider.MessageContentType.STICKER.ordinal())
				addChatMessageToDB(MyMessageType.OUTGOING_MSG.ordinal(), ChatMessage.MessageStatusType.SENDING.ordinal(), MessageContentTypeProvider.MessageContentType.TEXT.ordinal(), jsonMessageString);
		}*/

	}

	@Override
	protected void onCancel() {
		// Job has exceeded retry attempts or shouldReRunOnThrowable() has
		// returned false.

	}

	@Override
	public void onRun() throws Throwable {
		// Job logic goes here.

		/*if (id != jobCounter.get()) {
			// looks like other fetch jobs has been added after me. no reason to
			// keep fetching
			// many times, cancel me, let the other one fetch tweets.
			return;
		}*/

		RokhPref Session;
		String clientHandle = null;
		//reza_ak
		//Session = new RokhPref(SHAMChatApplication.getInstance().getApplicationContext());
		clientHandle = "user102015";

		int qos = 1;
		boolean retained = false;

		String[] args = new String[2];
		args[0] = jsonMessageString;
		args[1] = topicName + ";qos:" + qos + ";retained:" + retained;


		JSONObject jss = new JSONObject(jsonMessageString);
//reza_ak
		/*try {
			if (jss.getInt(IncomingChannelMsg.ISFORWARDED) == 0 || jss.getInt(IncomingChannelMsg.ISFORWARDED) == 1) {
			}
		} catch (JSONException e) {



			try {
				if (!threadId.substring(threadId.indexOf("-") + 1).startsWith("s")) {
					boolean bb = jss.getBoolean(IncomingChannelMsg.ISFORWARDED);
					jss.remove(IncomingChannelMsg.ISFORWARDED);
					jss.put(IncomingChannelMsg.ISFORWARDED, bb ? 1 : 0);
					jsonMessageString = jss.toString();
				}
			}catch (Exception e12)
			{}

		}
*/
		MqttAndroidClient client = Connections.getInstance(ApplicationLoader.getInstance().getApplicationContext()).getConnection(clientHandle).getClient();
		
		client.publish(topicName, jsonMessageString.getBytes(), qos, retained, null, new IMqttActionListener() {

			@Override
			public void onSuccess(IMqttToken arg0) {
			}

			@Override
			public void onFailure(IMqttToken arg0, Throwable arg1) {

			}
		});

		client.close();

	}

	private boolean isOnline()
	{
		ConnectivityManager cm = (ConnectivityManager)ApplicationLoader.getInstance().getSystemService(ApplicationLoader.applicationContext.CONNECTIVITY_SERVICE);
		if(cm.getActiveNetworkInfo() != null &&
				cm.getActiveNetworkInfo().isAvailable() &&
				cm.getActiveNetworkInfo().isConnected())
		{
			return true;
		}

		return false;
	}

	/**
	 *
	 * @param direction  put 0 for outgoing message, 1 for incoming message OR
	 * 				  MyMessageType.INCOMING_MSG.ordinal(), MyMessageType.OUTGOING_MSG.ordinal()
	 * @param messageStatus queued = 0, sending = 1, sent = 2, delivered = 3, seen =4, failed = 5
	 * @param messageContentType  text = 0, image = 1, sticker = 2, voice record = 3, favorite = 4,
	 *  						  MESSAGE_WITH_IMOTICONS = 5, LOCATION = 6, INCOMING_CALL = 7, OUTGOING_CALL = 8, VIDEO = 9, GROUP_INFO = 10
	 //* @param message
	 * @return
	 */

	public boolean addChatMessageToDB(int direction, int messageStatus, int messageContentType,
									  final String jsonMessageString) {

		boolean isExistingMessage = false;

		ContentResolver mContentResolver = ApplicationLoader.getInstance().getContentResolver();

		//mast - sample message to handle sent message and insert to db
		//String jsonSampleMsg = "{\"to\": \"/groups/testgroup\",\"from\": \"wizgoonId\",\"messageBody\": \"hello everyone!\",\"messageType\": 1,\"timestamp\": \"2014-03-07T00:00:00.000Z\",\"groupAlias\": \"Good friends group\"\"packetId\": \"userId_timestamp\"}";
		JSONObject SampleMsg=null;
		String packetId = null;
		String from = null;
		int fromUserId = 0;
		String to=null;
		String messageTypeDesc = null;
		int messageType = 0;
		String messageBody = null;
		String timestamp = null;
		//String groupOwnerId = null;
		int isGroupChat = 0;


		try {
			SampleMsg = new JSONObject(jsonMessageString);
			packetId = SampleMsg.getString("packetId");
			from = SampleMsg.getString("from");
			fromUserId = SampleMsg.getInt("from_userid");
			to = SampleMsg.getString("to");
			messageTypeDesc = SampleMsg.getString("messageTypeDesc");
			messageType = SampleMsg.getInt("messageType");
			messageBody = SampleMsg.getString("messageBody");
			//groupOwnerId = SampleMsg.getString("groupOwnerId");
			isGroupChat = SampleMsg.getInt("isGroupChat");

			timestamp = SampleMsg.getString("timestamp");



		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

//reza_ak
		String threadOwner = "102015";

		//String threadId = threadId;
		//String groupId = null;
		String friendId = null;

		// Out going message
		//threadId = message.getThread();
		//groupId = to;
		friendId = to;


		//mast - just for now to focus on adding just chat message - later we will update thread table too

		boolean threadSaveOrUpdateSuccess = saveOrUpdateThread(threadId, jsonMessageString, messageType, friendId, direction);
//
		if (threadSaveOrUpdateSuccess) {

			Cursor chatCursor = null;
//reza_ak
/*
			try {
				//check to see if current message exists previously - ic_search using packetId of message

				chatCursor = mContentResolver.query(
						ChatProviderNew.CONTENT_URI_CHAT,
						new String[] { MessageConstants._ID },
						ChatMessage.PACKET_ID + "=?",
						new String[] { packetId }, null);

				if (chatCursor != null && chatCursor.getCount() > 0) {

					isExistingMessage = true;
				}

				// If this is a totally new message
				if (!isExistingMessage) {

					ContentValues values = new ContentValues();

					values.put(ChatMessage.MESSAGE_RECIPIENT, to);
					//mast - put 0 for outgoing , 1 for incoming message
					values.put(ChatMessage.MESSAGE_TYPE, direction);
					values.put(ChatMessage.PACKET_ID, packetId);
					values.put(ChatMessage.THREAD_ID, threadId);

					values.put(ChatMessage.DESCRIPTION, messageTypeDesc);
					values.put(ChatMessage.MESSAGE_CONTENT_TYPE, messageType);
					values.put(ChatMessage.MESSAGE_STATUS, messageStatus);

					values.put(ChatMessage.MESSAGE_DATETIME, timestamp);
					values.put(ChatMessage.LAST_UPDATED_DATE_TIME, timestamp);

					if (isGroupChat == 1) {
						values.put(ChatMessage.GROUP_ID, to);
					}

					//mast - this is for incoming message
								*//*LocationDetails locDetails = (LocationDetails) message
										.getExtension(LocationDetails.NAMESPACE);

								if (locDetails != null) {
									values.put(ChatMessage.LATITUDE,
											locDetails.getLatitude());
									values.put(ChatMessage.LONGITUDE,
											locDetails.getLongitude());
								}*//*

								*//*if (message.getType() == Message.Type.groupchat
										&& direction == MyMessageType.INCOMING_MSG
												.ordinal()) { // We
									// handle
									// Group messages slightly different compared to
									// single chat
									// We add "ABC says: " text in to certain messages
									// group chat
									// Then we need to manage the friends in the group

									System.out
											.println("processMessage addChatMessageToDB message.getType() == Message.Type.groupchat && direction == MyMessageType.INCOMING_MSG.ordinal()");
									String fromGroup = message.getFrom();

									String userId = fromGroup.substring(
											fromGroup.indexOf("/") + 1,
											fromGroup.indexOf("-"));

									String username = getUsernameToDisplayForGroup(
											userId, fromGroup);

									// This is the actual sender, from value is
									// the room name not the individual who sent it
									values.put(ChatMessage.MESSAGE_SENDER, userId);

									if (messageContentType.getMessageContentType() != MessageContentType.GROUP_INFO) {
										// Group, incoming, not group info
										System.out
												.println("processMessage addChatMessageToDB messageContentType.getMessageContentType() != MessageContentType.GROUP_INFO");

										String formattedMessage = null;
										if (messageContentType.getMessageContentType() == MessageContentType.TEXT) {
											System.out
													.println("processMessage addChatMessageToDB messageContentType.getMessageContentType() == MessageContentType.TEXT");

											formattedMessage = username
													+ " "
													+ R.string.says
													+ " \n" + message.getBody();
										} else {
											System.out
													.println("processMessage addChatMessageToDB messageContentType.getMessageContentType() != MessageContentType.TEXT");

											formattedMessage = username
													+ " "
													+ R.string.sent;

											String body = message.getBody();
											values.put(ChatMessage.UPLOADED_FILE_URL,
													body);

											if (body != null
													&& body.contains("http://")) {
												try {
													URL u = new URL(body);
													int size = Utils.getFileSize(u);

													values.put(ChatMessage.FILE_SIZE,
															size);

												} catch (Exception e) {
													e.printStackTrace();
												}
											}

										}
										values.put(ChatMessage.TEXT_MESSAGE,
												formattedMessage);

									} else { //

									}

								} else { //Group chat out going message*//*

					System.out
							.println("processMessage addChatMessageToDB Single chat, both directions, Group chat outgoing");
					values.put(ChatMessage.MESSAGE_SENDER, from);
					values.put(ChatMessage.TEXT_MESSAGE, messageBody);

					if (threadId.indexOf("-ch") != -1) {
						values.put(ChatMessage.CHANNEL_ID  , "1");
						values.put(ChatMessage.CHANNEL_VIEW  , "1");
					}



					try {


						if 	(SampleMsg.getBoolean(IncomingChannelMsg.ISFORWARDED)) {
							values.put(ChatMessage.ISFORWARD , true ? 1 : 0);
							values.put(ChatMessage.CHANNELTITLE , SampleMsg.getString(IncomingChannelMsg.CHANNELTITLE));
							values.put(ChatMessage.CHANNELHASHCODE , SampleMsg.getString(IncomingChannelMsg.CHANNELHASHCODE));
							values.put(ChatMessage.ORGINALPACKETID , SampleMsg.getString(IncomingChannelMsg.ORGINALPACKETID));
						}


					} catch (JSONException e) {
						e.printStackTrace();
					}


					//}


					Uri uri = mContentResolver.insert(
							ChatProviderNew.CONTENT_URI_CHAT, values);

					Cursor c = null;
					int dbId = 0;
					try {
						c = mContentResolver.query(uri, null, null, null,
								null);

						c.moveToFirst();

						dbId = c.getInt(c.getColumnIndex("_id"));

					} finally {
						if (c != null) {
							c.close();
						}
					}


					NewMessageEvent newMessageEvent = new NewMessageEvent();
					newMessageEvent.setThreadId(threadId);
					newMessageEvent.setJsonMessageString(jsonMessageString);
					if (direction == MyMessageType.OUTGOING_MSG.ordinal()) {

						newMessageEvent.setDirection(MyMessageType.OUTGOING_MSG.ordinal());
					} *//*else {

									newMessageEvent
											.setDirection(MyMessageType.INCOMING_MSG
													.ordinal());
								}*//*

					EventBus.getDefault().postSticky(newMessageEvent);



				} else {
					//if this message exists previously we add code required below (if required)
				}

			} finally {
				chatCursor.close();
			}*/
		}

		return isExistingMessage;

	}

	private boolean saveOrUpdateThread(String threadId, String receivedJsonMessage, int messageContentType, String friendId, int direction) {

		System.out.println("processMessage addChatMessageToDB saveOrUpdateThread");

		ContentResolver mContentResolver = ApplicationLoader.getInstance().getContentResolver();

		// Pars Json String

		JSONObject SampleMsg=null;
		String packetId = null;
		String from = null;
		int fromUserId = 0;
		String to=null;
		String messageTypeDesc = null;
		int messageType = 0;
		String messageBody = null;
		//String groupOwnerId = null;
		int isGroupChat = 0;


		try {
			SampleMsg		= new JSONObject(receivedJsonMessage);
			packetId		= SampleMsg.getString("packetId");
			from			= SampleMsg.getString("from");
			fromUserId		= SampleMsg.getInt("from_userid");
			to				= SampleMsg.getString("to");
			messageTypeDesc	= SampleMsg.getString("messageTypeDesc");
			messageType		= SampleMsg.getInt("messageType");
			messageBody		= SampleMsg.getString("messageBody");
			//groupOwnerId	= SampleMsg.getString("groupOwnerId");
			isGroupChat		= SampleMsg.getInt("isGroupChat");

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// End Pars
		//reza_ak
/*
		Cursor threadCursor = null;
		try {
			threadCursor = mContentResolver.query(
					ChatProviderNew.CONTENT_URI_THREAD,
					new String[] { MessageThread.THREAD_ID },
					MessageThread.THREAD_ID + "=?", new String[] { threadId },
					null);

			boolean isExistingThread = false;
			if (threadCursor != null && threadCursor.getCount() > 0) {

				System.out
						.println("processMessage addChatMessageToDB saveOrUpdateThread isExisting thread");
				isExistingThread = true;

			}

			String lastMessage = messageBody;
			boolean isRead = false;


			if (messageContentType != MessageContentType.GROUP_INFO.ordinal()) {

				System.out.println("processMessage addChatMessageToDB saveOrUpdateThread NOT group info");

				switch (Utils.readMessageContentType(messageType)) {
					case TEXT:

						int limit;
						if (messageBody.length()>70) limit = 70;
						else limit = messageBody.length();

						lastMessage =  SHAMChatApplication.getInstance().getApplicationContext().getResources().getString(R.string.sent)+" "+
								messageBody.substring(0,limit);
						break;

					case IMAGE:

						lastMessage =  SHAMChatApplication.getInstance().getApplicationContext().getResources().getString(R.string.sent_image);
						break;

					case VIDEO:
						lastMessage = SHAMChatApplication.getInstance().getApplicationContext().getResources().getString(R.string.sent_video);
						break;

					case STICKER:
						lastMessage = SHAMChatApplication.getInstance().getApplicationContext().getResources().getString(R.string.sent_sticker);
						break;


					case LOCATION:
						lastMessage = SHAMChatApplication.getInstance().getApplicationContext().getResources().getString(R.string.sent_location);
						break;

					case VOICE_RECORD:
						lastMessage = SHAMChatApplication.getInstance().getApplicationContext().getResources().getString(R.string.sent_voice);
						break;

					default:
						System.out.println("processMessage addChatMessageToDB saveOrUpdateThread DEFAULT"+ lastMessage);
						break;

				}

			}



			if (isExistingThread) {

				ContentValues values = new ContentValues();
				values.put(MessageThread.LAST_MESSAGE, lastMessage.trim());
				values.put(MessageThread.LAST_MESSAGE_CONTENT_TYPE,messageContentType);
				values.put(MessageThread.READ_STATUS, isRead ? 1 : 0);
				values.put(MessageThread.LAST_UPDATED_DATETIME,Utils.formatDate(new Date().getTime(),"yyyy/MM/dd HH:mm:ss"));
				values.put(MessageThread.LAST_MESSAGE_DIRECTION, direction);
				values.put(MessageThread.THREAD_OWNER, SHAMChatApplication.getConfig().getUserId());
				values.put(MessageThread.IS_GROUP_CHAT,isGroupChat);
				mContentResolver.update(ChatProviderNew.CONTENT_URI_THREAD, values, MessageThread.THREAD_ID + "=?", new String[] { threadId });
			} else {
				System.out.println("processMessage addChatMessageToDB saveOrUpdateThread new thread " + lastMessage);
				ContentValues values = new ContentValues();
				values.put(MessageThread.THREAD_ID, threadId);
				values.put(MessageThread.FRIEND_ID, friendId);
				values.put(MessageThread.READ_STATUS, isRead ? 1 : 0);
				values.put(MessageThread.LAST_MESSAGE, lastMessage.trim());
				values.put(MessageThread.LAST_MESSAGE_CONTENT_TYPE,messageContentType);
				values.put(MessageThread.LAST_UPDATED_DATETIME, Utils.formatDate(new Date().getTime(), "yyyy/MM/dd HH:mm:ss"));
				values.put(MessageThread.IS_GROUP_CHAT, isGroupChat);
				values.put(MessageThread.THREAD_OWNER, SHAMChatApplication.getConfig().getUserId());
				values.put(MessageThread.LAST_MESSAGE_DIRECTION, direction);

				try {
					if 	(SampleMsg.getBoolean(IncomingChannelMsg.ISFORWARDED)) {
						values.put(ChatMessage.ISFORWARD , true);
						values.put(ChatMessage.CHANNELTITLE , SampleMsg.getString(IncomingChannelMsg.CHANNELTITLE));
						values.put(ChatMessage.CHANNELHASHCODE , SampleMsg.getString(IncomingChannelMsg.CHANNELHASHCODE));
						values.put(ChatMessage.ORGINALPACKETID , SampleMsg.getString(IncomingChannelMsg.ORGINALPACKETID));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}



				mContentResolver.insert(ChatProviderNew.CONTENT_URI_THREAD, values);
			}

		} finally {
			threadCursor.close();
		}*/
		return true;
	}


	/**
	 * Updates the BLOB message status to x and updates UI too
	 * @param packetId
	 */

	private void updateMessageStatus(final String packetId, ChatMessage.MessageStatusType messageStatusType ) {

		ContentResolver mContentResolver = ApplicationLoader.getInstance().getContentResolver();
//reza_ak
	/*	Cursor cursor = null;
		try {
			ContentValues cv = new ContentValues();
			cv.put(ChatMessage.MESSAGE_STATUS, messageStatusType.ordinal());
			cv.put(ChatMessage.LAST_UPDATED_DATE_TIME, Utils.formatDate(
					new Date().getTime(), "yyyy/MM/dd HH:mm:ss"));

			cursor = mContentResolver
					.query(ChatProviderNew.CONTENT_URI_CHAT, new String[] {
									MessageConstants._ID, ChatMessage.THREAD_ID },
							ChatMessage.PACKET_ID + "=?",
							new String[] { packetId }, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();

				String columnID = Integer.toString(cursor.getInt(cursor
						.getColumnIndex(MessageConstants._ID)));

				String threadId = cursor.getString(cursor
						.getColumnIndex(ChatMessage.THREAD_ID));
				Uri rowuri = Uri.parse("content://" + ChatProviderNew.AUTHORITY
						+ "/" + ChatProviderNew.TABLE_NAME_CHATS + "/"
						+ columnID);

				mContentResolver.update(rowuri, cv, MessageConstants._ID
								+ " = ? AND " + ChatMessage.MESSAGE_STATUS + " != "
								+ MessageStatusType.SEEN.ordinal() + " AND "
								+ ChatMessage.MESSAGE_TYPE + " = "
								+ MyMessageType.OUTGOING_MSG.ordinal(),
						new String[] { columnID });

				//Update the UI to Delivered
				JobManager jobManager;
				jobManager = SHAMChatApplication.getInstance().getJobManager();
				jobManager.addJobInBackground(new MessageStateChangedJob(threadId, packetId, messageStatusType.getType()));


			}


		} finally {
			cursor.close();
		}*/

	}


	@Override
	protected int getRetryLimit() {
		// mast - just try to send to topic 1 time
		return 1;
	}


	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		// An error occurred in onRun.
		// Return value determines whether this job should retry running (true)
		// or abort (false).
		System.out.println("Publish to topic run again");

		return true;
	}




}
