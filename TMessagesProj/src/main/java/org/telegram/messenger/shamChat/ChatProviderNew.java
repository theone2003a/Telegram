/*
package org.telegram.messenger.shamChat;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.shamchat.adapters.EndlessScrollListener;
import com.shamchat.androidclient.SHAMChatApplication;
import com.shamchat.androidclient.chat.extension.MessageContentTypeProvider.MessageContentType;
import com.shamchat.androidclient.data.RosterProvider.RosterConstants;
import com.shamchat.androidclient.data.UserProvider.UserConstants;
import com.shamchat.models.ChatMessage;
import com.shamchat.models.ChatMessage.MessageStatusType;
import com.shamchat.models.Message;
import com.shamchat.models.MessageThread;
import com.shamchat.models.User;
import com.shamchat.utils.Utils;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatProviderNew extends ContentProvider {

	public static final String AUTHORITY = "org.zamin.androidclient.provider.Messages";
	public static final String TABLE_NAME_CHATS = "chat_message";
	public static final String TABLE_NAME_THREADS = "message_thread";
	private static final String TABLE_NAME_FAVORITE = "favorite";
	private static final String TABLE_NAME_FILE = "file_tb";

	public static final Uri CONTENT_URI_CHAT = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME_CHATS);

	public static final Uri CONTENT_URI_THREAD = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME_THREADS);

	public static final Uri CONTENT_URI_FILE = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME_FILE);









	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int MESSAGES = 1;
	private static final int MESSAGE_ID = 2;
	private static final int THREADS = 3;
	private static final int THREAD_ID = 4;
	private static final int FILES = 5;
	private static final int FILE_ID = 6;

	static {
		URI_MATCHER.addURI(AUTHORITY, "chat_message", MESSAGES);
		URI_MATCHER.addURI(AUTHORITY, "chat_message/#", MESSAGE_ID);
		URI_MATCHER.addURI(AUTHORITY, "message_thread", THREADS);
		URI_MATCHER.addURI(AUTHORITY, "message_thread/#", THREAD_ID);
		URI_MATCHER.addURI(AUTHORITY, "file_tb", FILES);
		URI_MATCHER.addURI(AUTHORITY, "file_tb/#", FILE_ID);


	}

	private static final String TAG = "zamin.ChatProviderNew";


	private SQLiteOpenHelper mOpenHelper;


	public ChatProviderNew() {
	}



	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String segment = null;
		switch (URI_MATCHER.match(url)) {

			case MESSAGES:
				count = db.delete(TABLE_NAME_CHATS, where, whereArgs);
				break;
			case MESSAGE_ID:
				segment = url.getPathSegments().get(1);

				if (TextUtils.isEmpty(where)) {
					where = "_id=" + segment;
				} else {
					where = "_id=" + segment + " AND (" + where + ")";
				}

				count = db.delete(TABLE_NAME_CHATS, where, whereArgs);
				break;

			case THREADS:
				count = db.delete(TABLE_NAME_THREADS, where, whereArgs);
				break;
			case THREAD_ID:
				segment = url.getPathSegments().get(1);

				if (TextUtils.isEmpty(where)) {
					where = "_id=" + segment;
				} else {
					where = "_id=" + segment + " AND (" + where + ")";
				}

				count = db.delete(TABLE_NAME_THREADS, where, whereArgs);
				break;
			case FILES :
				count = db.delete(TABLE_NAME_FILE, where, whereArgs);
				break;
			case FILE_ID :
				segment = url.getPathSegments().get(1);

				if (TextUtils.isEmpty(where)) {
					where = "_id=" + segment;
				} else {
					where = "_id=" + segment + " AND (" + where + ")";
				}
				count = db.delete(TABLE_NAME_FILE, where, whereArgs);

				break;

			default:
				throw new IllegalArgumentException("Cannot delete from URL: " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public String getType(Uri url) {
		int match = URI_MATCHER.match(url);
		switch (match) {
			case MESSAGES:
				return MessageConstants.CONTENT_CHAT_TYPE;
			case MESSAGE_ID:
				return MessageConstants.CONTENT_CHAT_ITEM_TYPE;
			case THREADS:
				return ThreadConstants.CONTENT_THREAD_TYPE;
			case THREAD_ID:
				return ThreadConstants.CONTENT_THREAD_ITEM_TYPE;
			case FILES:
				return FilesConstants.CONTENT_FILE_ITEM_TYPE;
			case FILE_ID:
				return FilesConstants.CONTENT_FILE_ITEM_TYPE;

			default:
				throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {

		ContentValues values = (initialValues != null) ? new ContentValues(
				initialValues) : new ContentValues();

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = -1;

		Uri noteUri = null;

		switch (URI_MATCHER.match(url)) {

			case MESSAGES:

				for (String colName : MessageConstants.getRequiredColumns()) {
					if (values.containsKey(colName) == false) {
						throw new IllegalArgumentException("Missing column: "
								+ colName);
					}
				}

				rowId = db.insert(TABLE_NAME_CHATS, "", values);

				if (rowId < 0) {
					throw new SQLException(
							"Failed to insert row into TABLE_NAME_CHATS" + url);
				}

				if (rowId > 0) {
					Log.d(TAG, "Message successfully entered at " + rowId);
				}

				noteUri = ContentUris.withAppendedId(CONTENT_URI_CHAT, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				break;

			case THREADS:

				for (String colName : ThreadConstants.getRequiredColumns()) {
					if (values.containsKey(colName) == false) {
						throw new IllegalArgumentException("Missing column: "
								+ colName);
					}
				}

				rowId = db.insert(TABLE_NAME_THREADS, "", values);

				if (rowId < 0) {
					throw new SQLException(
							"Failed to insert row into TABLE_NAME_THREADS" + url);
				}

				if (rowId > 0) {
					Log.d(TAG, "Thread successfully entered at " + rowId);
				}

				noteUri = ContentUris.withAppendedId(CONTENT_URI_THREAD, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				break;


			case  FILES :

				for (String colName : FilesConstants.getRequiredColumns()) {
					if (values.containsKey(colName) == false) {
						throw new IllegalArgumentException("Missing column: "
								+ colName);
					}
				}

				rowId = db.insert(TABLE_NAME_FILE , "", values);

				if (rowId < 0) {
					throw new SQLException(
							"Failed to insert row into TABLE_NAME_THREADS" + url);
				}

				if (rowId > 0) {
					Log.d(TAG, "Thread successfully entered at " + rowId);
				}

				noteUri = ContentUris.withAppendedId(CONTENT_URI_FILE, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				break;

			default:
				throw new IllegalArgumentException("Cannot insert from URL: " + url);
		}

		return noteUri;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new ChatDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri url, String[] projectionIn, String selection,
						String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = URI_MATCHER.match(url);

		switch (match) {
			case MESSAGES:
				qBuilder.setTables(TABLE_NAME_CHATS);
				break;
			case MESSAGE_ID:
				qBuilder.setTables(TABLE_NAME_CHATS);
				qBuilder.appendWhere("_id=");
				qBuilder.appendWhere(url.getPathSegments().get(1));
				break;
			case THREADS:
				qBuilder.setTables(TABLE_NAME_THREADS);
				break;
			case THREAD_ID:
				qBuilder.setTables(TABLE_NAME_THREADS);
				qBuilder.appendWhere("_id=");
				qBuilder.appendWhere(url.getPathSegments().get(1));
				break;

			case FILES:
				qBuilder.setTables(TABLE_NAME_FILE);
				break;
			case FILE_ID:
				qBuilder.setTables(TABLE_NAME_FILE);
				qBuilder.appendWhere("_id=");
				qBuilder.appendWhere(url.getPathSegments().get(1));
				break;

			default:
				throw new IllegalArgumentException("Unknown URL " + url);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = MessageConstants.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = qBuilder.query(db, projectionIn, selection, selectionArgs,
				null, null, orderBy);

		if (ret == null) {
			infoLog("ChatProvider.query: failed");
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), url);
		}

		return ret;
	}

	@Override
	public int update(Uri url, ContentValues values, String where,
					  String[] whereArgs) {
		int count;
		long rowId = 0;
		String segment = null;
		int match = URI_MATCHER.match(url);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (match) {
			case MESSAGES:
				count = db.update(TABLE_NAME_CHATS, values, where, whereArgs);

				if (count > 0) {
					Log.d(TAG, "Message successfully updated " + count);
				}

				break;
			case MESSAGE_ID:
				segment = url.getPathSegments().get(1);
				rowId = Long.parseLong(segment);
				count = db.update(TABLE_NAME_CHATS, values, "_id=" + rowId, null);

				Uri messageUri = ContentUris
						.withAppendedId(CONTENT_URI_CHAT, rowId);
				getContext().getContentResolver().notifyChange(messageUri, null);
				getContext().getContentResolver().notifyChange(url, null);

				return count;

			case THREADS:
				count = db.update(TABLE_NAME_THREADS, values, where, whereArgs);

				if (count > 0) {
					Log.d(TAG, "Thread successfully updated " + count);
				}

				break;
			case THREAD_ID:
				segment = url.getPathSegments().get(1);
				rowId = Long.parseLong(segment);
				count = db.update(TABLE_NAME_THREADS, values, where, whereArgs);

				Uri noteUri = ContentUris.withAppendedId(CONTENT_URI_THREAD, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				getContext().getContentResolver().notifyChange(url, null);

				return count;

			case FILES:
				count = db.update(TABLE_NAME_FILE , values, where, whereArgs);

				if (count > 0) {
					Log.d(TAG, "Thread successfully updated " + count);
				}

				break;

			case FILE_ID:
				segment = url.getPathSegments().get(1);
				rowId = Long.parseLong(segment);
				count = db.update(TABLE_NAME_FILE, values, where, whereArgs);

				Uri noteUria = ContentUris.withAppendedId(CONTENT_URI_FILE, rowId);
				getContext().getContentResolver().notifyChange(noteUria, null);
				getContext().getContentResolver().notifyChange(url, null);
				return count;

			default:
				throw new UnsupportedOperationException("Cannot update URL: " + url);
		}

		infoLog("*** notifyChange() rowId: " + rowId + " url " + url);

		getContext().getContentResolver().notifyChange(url, null);
		return count;

	}

	private static void infoLog(String data) {
	}

	public static class ChatDatabaseHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "chat.db";
		private static final String USER_DATABASE_NAME = "user.db";
		private static final int DATABASE_VERSION = 10 ;
		private String CHAT_DATABASE_PATH;
		private String USER_DATABASE_PATH;

		public ChatDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			CHAT_DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).toString();
			USER_DATABASE_PATH = context.getDatabasePath(USER_DATABASE_NAME).toString();
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			String CREATE_TABLE_CHAT_MESSAGE = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_NAME_CHATS + " (" + MessageConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ ChatMessage.THREAD_ID + " TEXT , "
					+ ChatMessage.MESSAGE_SENDER + " TEXT, "
					+ ChatMessage.MESSAGE_RECIPIENT + " TEXT, "
					+ ChatMessage.MESSAGE_CONTENT_TYPE + " INTEGER , "
					+ ChatMessage.TEXT_MESSAGE + " TEXT , "
					+ ChatMessage.BLOB_MESSAGE + " BLOB , "
					+ ChatMessage.MESSAGE_DATETIME
					+ " DATETIME DEFAULT (datetime('now','localtime')), "
					+ ChatMessage.DELIVERED_DATETIME + " DATETIME, "
					+ ChatMessage.MESSAGE_STATUS
					+ " INTEGER NOT NULL DEFAULT 0, "
					+ ChatMessage.MESSAGE_TYPE
					+ " INTEGER NOT NULL  DEFAULT 0, "
					+ ChatMessage.SENDERS_MOBILE_NO + " TEXT , "
					+ ChatMessage.PACKET_ID + " TEXT, "
					+ ChatMessage.DESCRIPTION + " TEXT, "
					+ ChatMessage.LONGITUDE + " DOUBLE, "
					+ ChatMessage.FILE_SIZE + " INTEGER NOT NULL DEFAULT 0, "
					+ ChatMessage.FILE_URL + " TEXT , "
					+ ChatMessage.UPLOADED_PERCENTAGE
					+ " INTEGER NOT NULL DEFAULT 0, "
					+ ChatMessage.LAST_UPDATED_DATE_TIME
					+ " DATETIME DEFAULT (datetime('now','localtime')), "
					+ ChatMessage.UPLOADED_FILE_URL + " TEXT , "
					+ ChatMessage.GROUP_ID + " TEXT , " + ChatMessage.LATITUDE + " DOUBLE , "
					+ ChatMessage.CHANNEL_VIEW + " TEXT NOT NULL DEFAULT '0' , "
					+ ChatMessage.CHANNEL_ID + " INTEGER NOT NULL DEFAULT 0  , "
					+ ChatMessage.ISFORWARD + " INTEGER NOT NULL DEFAULT 0  ,"
					+ ChatMessage.CHANNELTITLE + " TEXT NOT NULL DEFAULT '0' ,  "
					+ ChatMessage.CHANNELHASHCODE + " TEXT NOT NULL DEFAULT '0' , "
					+ ChatMessage.ORGINALPACKETID + " TEXT NOT NULL DEFAULT '0' "
					+ ")";



			db.execSQL(CREATE_TABLE_CHAT_MESSAGE);

			String CREATE_TABLE_CHAT_THREAD = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_NAME_THREADS + " (" + MessageConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ MessageThread.THREAD_ID + " TEXT NOT NULL , "
					+ MessageThread.LAST_UPDATED_DATETIME
					+ " DATETIME NOT NULL , " + MessageThread.THREAD_STATUS
					+ " INTEGER NOT NULL  DEFAULT 1, "
					+ MessageThread.IS_GROUP_CHAT
					+ " INTEGER NOT NULL  DEFAULT 0, "
					+ MessageThread.READ_STATUS
					+ " INTEGER NOT NULL  DEFAULT 0, "
					+ MessageThread.THREAD_OWNER + " TEXT NOT NULL, "
					+ MessageThread.LAST_MESSAGE + " TEXT NOT NULL, "
					+ MessageThread.LAST_MESSAGE_CONTENT_TYPE
					+ " INTEGER NOT NULL  DEFAULT 0 , "
					+ MessageThread.LAST_MESSAGE_DIRECTION
					+ " INTEGER NOT NULL , " + MessageThread.FRIEND_ID
					+ " TEXT NOT NULL" + ")";

			db.execSQL(CREATE_TABLE_CHAT_THREAD);

			String CREATE_TABLE_FAVORITE = "CREATE TABLE "
					+ TABLE_NAME_FAVORITE + " (" + Message.DB_MESSAGE_ID
					+ " TEXT PRIMARY KEY NOT NULL, " + Message.DB_MESSAGE_TYPE
					+ " INTEGER, " + Message.DB_CONTENT + " TEXT, "
					+ Message.DB_DELETED + " INTEGER NOT NULL DEFAULT 0 , "
					+ Message.DB_USER_ID + " TEXT, " + Message.DB_TIME
					+ " INTEGER" + ");";

			db.execSQL(CREATE_TABLE_FAVORITE);




            String PACKET_ID1 = "packet_id";
            String FILE_URL = "file_url";

			String CREATE_TABLE_FILE = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_NAME_FILE + " (" + MessageConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PACKET_ID1 + " TEXT NOT NULL , "
					+ FILE_URL + " TEXT NOT NULL  " + ")";



			db.execSQL(CREATE_TABLE_FILE);



		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			infoLog("onUpgrade: from " + oldVersion + " to " + newVersion);
			switch (oldVersion) {
				case 5:

					db.execSQL("ALTER TABLE " + TABLE_NAME_CHATS + " ADD " + ChatMessage.CHANNEL_VIEW + " TEXT NOT NULL DEFAULT '0' ");
					db.execSQL("ALTER TABLE " + TABLE_NAME_CHATS + " ADD " + ChatMessage.CHANNEL_ID + " INTEGER NOT NULL DEFAULT 0 ");

				case 6 :
					db.execSQL("ALTER TABLE " + TABLE_NAME_CHATS + " ADD " + ChatMessage.ISFORWARD + " INTEGER NOT NULL DEFAULT 0 ");
					db.execSQL("ALTER TABLE " + TABLE_NAME_CHATS + " ADD " + ChatMessage.CHANNELTITLE + " TEXT NOT NULL DEFAULT '0' ");
					db.execSQL("ALTER TABLE " + TABLE_NAME_CHATS + " ADD " + ChatMessage.CHANNELHASHCODE + " TEXT NOT NULL DEFAULT '0' ");
					db.execSQL("ALTER TABLE " + TABLE_NAME_CHATS + " ADD " + ChatMessage.ORGINALPACKETID + " TEXT NOT NULL DEFAULT '0' ");

				case 9 :

                   String PACKET_ID1 = "packet_id";
                    String FILE_URL = "file_url";

					String CREATE_TABLE_FILE = "CREATE TABLE IF NOT EXISTS "
							+ TABLE_NAME_FILE + " (" + MessageConstants._ID
							+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
							+ PACKET_ID1 + " TEXT NOT NULL , "
							+ FILE_URL + " TEXT NOT NULL  " + ")";


					db.execSQL(CREATE_TABLE_FILE);

					break;
				// case 3:
				// db.execSQL("UPDATE " + TABLE_NAME + " SET READ=1");
				// case 4:
				// db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " +
				// ChatConstants.PACKET_ID + " TEXT");
				// break;
				// default:
				// db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
				// onCreate(db);
			}
		}

	}

	public static final class MessageConstants implements BaseColumns {

		private MessageConstants() {
		}

		public static final String CONTENT_CHAT_TYPE = "vnd.android.cursor.dir/vnd.zamin.message";
		public static final String CONTENT_CHAT_ITEM_TYPE = "vnd.android.cursor.item/vnd.zamin.message";

		public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by
		// auto-id

		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();
			tmpList.add(ChatMessage.PACKET_ID);
			tmpList.add(ChatMessage.MESSAGE_SENDER);
			tmpList.add(ChatMessage.MESSAGE_RECIPIENT);
			tmpList.add(ChatMessage.THREAD_ID);
			return tmpList;
		}
	}


	public static final class FilesConstants implements BaseColumns {

		private FilesConstants() {
		}

		public static final String CONTENT_FILE_TYPE = "vnd.android.cursor.dir/vnd.zamin.files";
		public static final String CONTENT_FILE_ITEM_TYPE = "vnd.android.cursor.item/vnd.zamin.fils";

		public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by
		// auto-id

		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();
			tmpList.add("packet_id");
			tmpList.add("file_url");
			return tmpList;
		}
	}


	public static final class ThreadConstants implements BaseColumns {

		private ThreadConstants() {
		}

		public static final String CONTENT_THREAD_TYPE = "vnd.android.cursor.dir/vnd.zamin.thread";
		public static final String CONTENT_THREAD_ITEM_TYPE = "vnd.android.cursor.item/vnd.zamin.thread";

		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();
			tmpList.add(MessageThread.THREAD_ID);
			tmpList.add(MessageThread.FRIEND_ID);
			return tmpList;
		}

	}

	public List<ChatMessage> getAllMessagesByThreadIdAsList(String threadId,
															String currentLoadedIndex) {


		String orderBy = "" ;
		if (threadId.indexOf("-ch") != -1 ) {
			Log.d("tegg" , "true") ;
			orderBy = ChatMessage.MESSAGE_DATETIME ;
		}else {
			orderBy = "_id";
		}

		List<ChatMessage> listMsg = new ArrayList<ChatMessage>();

		Cursor cursor = SHAMChatApplication
				.getMyApplicationContext()
				.getContentResolver()
				.query(ChatProviderNew.CONTENT_URI_CHAT,
						null,
						ChatMessage.THREAD_ID + "=?",
						new String[] { threadId },
						MessageConstants._ID + " DESC LIMIT "
								+ currentLoadedIndex + " ");

		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {

				listMsg.add(getChatMessageByCursor(cursor));

			}
		}

		cursor.close();

		Collections.reverse(listMsg);
		return listMsg;

	}

	public ChatMessage getChatMessageByCursor(Cursor cursor) {
		int messageId = cursor.getInt(cursor.getColumnIndex(MessageConstants._ID));

		String ChannelView = cursor.getString(cursor.getColumnIndex(ChatMessage.CHANNEL_VIEW)) ;
		Log.d("isForwardedisForwarded" , cursor.getInt(cursor.getColumnIndex(ChatMessage.ISFORWARD)) + "" ) ;

		boolean isForward = cursor.getInt(cursor.getColumnIndex(ChatMessage.ISFORWARD)) == 1  ;
		String ChannelTitle = cursor.getString(cursor.getColumnIndex(ChatMessage.CHANNELTITLE)) ;
		String ChannelHashcode = cursor.getString(cursor.getColumnIndex(ChatMessage.CHANNELHASHCODE)) ;
		String orginalPacketId = cursor.getString(cursor.getColumnIndex(ChatMessage.ORGINALPACKETID)) ;


		MessageContentType messageContentType = readMessageContentType(cursor
				.getInt(cursor.getColumnIndex(ChatMessage.MESSAGE_CONTENT_TYPE)));
		String textMessage = cursor.getString(cursor
				.getColumnIndex(ChatMessage.TEXT_MESSAGE));

		byte[] blobMessage = cursor.getBlob(cursor
				.getColumnIndex(ChatMessage.BLOB_MESSAGE));

		String messageDateTime = cursor.getString(cursor
				.getColumnIndex(ChatMessage.MESSAGE_DATETIME));

		int messageStatus = cursor.getInt(cursor
				.getColumnIndex(ChatMessage.MESSAGE_STATUS));

		String deliverdDateTime = cursor.getString(cursor
				.getColumnIndex(ChatMessage.DELIVERED_DATETIME));

		int messageType = cursor.getInt(cursor.getColumnIndex(ChatMessage.MESSAGE_TYPE));
		String packetId = cursor.getString(cursor.getColumnIndex(ChatMessage.PACKET_ID));

		String sender = cursor.getString(cursor
				.getColumnIndex(ChatMessage.MESSAGE_SENDER));
		String recipient = cursor.getString(cursor
				.getColumnIndex(ChatMessage.MESSAGE_RECIPIENT));

		String description = cursor.getString(cursor
				.getColumnIndex(ChatMessage.DESCRIPTION));

		double longitude = cursor.getDouble(cursor
				.getColumnIndex(ChatMessage.LONGITUDE));
		double latitude = cursor.getDouble(cursor
				.getColumnIndex(ChatMessage.LATITUDE));
		String sendersMobileNumber = cursor.getString(cursor
				.getColumnIndex(ChatMessage.SENDERS_MOBILE_NO));

		long fileSize = cursor.getLong(cursor
				.getColumnIndex(ChatMessage.FILE_SIZE));

		int uploadedPercentage = cursor.getInt(cursor
				.getColumnIndex(ChatMessage.UPLOADED_PERCENTAGE));

		String fileUrl = cursor.getString(cursor
				.getColumnIndex(ChatMessage.FILE_URL));

		String threadId = cursor.getString(cursor
				.getColumnIndex(ChatMessage.THREAD_ID));

		String lastUpdatedDateTime = cursor.getString(cursor
				.getColumnIndex(ChatMessage.LAST_UPDATED_DATE_TIME));

		String uploadedFileUrl = cursor.getString(cursor
				.getColumnIndex(ChatMessage.UPLOADED_FILE_URL));

		*/
/**
		 * Get the user
		 *//*


		String tmpUserId = sender;

		System.out.println("INCOMING SENDER "+sender);

		if (sender.startsWith("g") || sender.startsWith("ch")|| sender.substring( sender.indexOf("-")+1).startsWith("s")) {

			switch (messageContentType) {
				case FAVORITE:
					System.out.println("INCOMING CPN FAVORITE " +textMessage);
					break;
				case IMAGE:
					System.out.println("INCOMING CPN IMAGE  " +textMessage);
					break;
				case INCOMING_CALL:
					System.out.println("INCOMING CPN CALL  " +textMessage);

					break;
				case LOCATION:
					System.out.println("INCOMING CPN LOCATION  " +textMessage);

					break;
				case OUTGOING_CALL:
					System.out.println("INCOMING CPN OUTGOING_CALL " +textMessage);
					break;
				case STICKER:
					System.out.println("INCOMING CPN STICKER " +textMessage);

					String data[] = textMessage.split("-");

					tmpUserId = data[1];

					break;
				case TEXT:
					System.out.println("INCOMING CPN TEXT " +textMessage);
					break;
				case VOICE_RECORD:
					System.out.println("INCOMING CPN VOICE_RECORD " +textMessage);
					break;
				case VIDEO:
					System.out.println("INCOMING CPN VIDEO " +textMessage);
					break;

				case GROUP_INFO:
					System.out.println("INCOMING CPN GROUP_INFO " +textMessage);
					break;
				default:
					System.out.println("INCOMING CPN DEFAULT " +textMessage);
					break;
			}

		} else {
			tmpUserId = sender;
		}
		ContentResolver contentResolver = SHAMChatApplication
				.getMyApplicationContext().getContentResolver();

		Cursor friendCursor = contentResolver.query(
				UserProvider.CONTENT_URI_USER, null, UserConstants.USER_ID
						+ "=?", new String[] { tmpUserId }, null);

		User user = null;
		if (friendCursor != null && friendCursor.getCount() > 0) {
			friendCursor.moveToFirst();

			System.out.println("INCOMING CPN USER FOUND "+tmpUserId);
			user = new User();

			String userId = friendCursor.getString(friendCursor
					.getColumnIndex(UserConstants.USER_ID));
			user.setUserId(userId);

			String username = friendCursor.getString(friendCursor
					.getColumnIndex(UserConstants.USERNAME));
			user.setUsername(username);

			String friendMobileNo = friendCursor.getString(friendCursor
					.getColumnIndex(UserConstants.MOBILE_NO));

			user.setMobileNo(friendMobileNo);

			String profileImageUrl = friendCursor.getString(friendCursor
					.getColumnIndex(UserConstants.PROFILE_IMAGE_URL));
			user.setprofileImageUrl(profileImageUrl);
		} else {

			System.out.println("INCOMING CPN USER NOT FOUND "+tmpUserId);
		}
		friendCursor.close();

		ChatMessage c = new ChatMessage(messageId, threadId,
				messageContentType, textMessage, blobMessage, messageDateTime,
				deliverdDateTime, readMessageType(messageType), packetId,
				sender, recipient, description, longitude, latitude,
				sendersMobileNumber, fileSize, uploadedPercentage, fileUrl,
				readMessageStatus(messageStatus), lastUpdatedDateTime,
				uploadedFileUrl ,  ChannelView,isForward,ChannelTitle,ChannelHashcode,orginalPacketId);

		c.setUser(user);

		return c;
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


	public static MessageStatusType readMessageStatus(int type) {

		MessageStatusType messageType = MessageStatusType.QUEUED;

		switch (type) {
			case 1:
				messageType = MessageStatusType.SENDING;
				break;
			case 2:
				messageType = MessageStatusType.SENT;
				break;
			case 3:
				messageType = MessageStatusType.DELIVERED;
				break;
			case 4:
				messageType = MessageStatusType.SEEN;
				break;
			case 5:
				messageType = MessageStatusType.FAILED;
				break;

		}

		return messageType;
	}

	public static MessageContentType readMessageContentType(int type) {
		MessageContentType messageType = MessageContentType.TEXT;
		switch (type) {
			case 1:
				messageType = MessageContentType.IMAGE;
				break;
			case 2:
				messageType = MessageContentType.STICKER;
				break;
			case 3:
				messageType = MessageContentType.VOICE_RECORD;
				break;
			case 4:
				messageType = MessageContentType.FAVORITE;
				break;
			case 5:
				messageType = MessageContentType.MESSAGE_WITH_IMOTICONS;
				break;
			case 6:
				messageType = MessageContentType.LOCATION;
				break;
			case 7:
				messageType = MessageContentType.INCOMING_CALL;
				break;
			case 8:
				messageType = MessageContentType.OUTGOING_CALL;
				break;
			case 9:
				messageType = MessageContentType.VIDEO;
				break;
			case 10:
				messageType = MessageContentType.MISSED_CALL;
				break;
			case 11:
				messageType = MessageContentType.GROUP_INFO;
				break;
		}

		return messageType;
	}


	*/
/**
	 * This loads n rows before the specified packetId row
	 * @param packetId
	 * @param threadId
	 * @param limit
	 * @return
	 *//*

	public ArrayList<ChatMessage> loadDataSet(String packetId, String threadId,int appendDirection, int limit) {
		return loadDataSet( packetId, threadId, appendDirection, limit, "Desc", false);
	}
	*/
/**
	 * This loads n rows before the specified packetId row
	 * @param packetId
	 * @param threadId
	 * @param limit
	 * @return
	 *//*

	public ArrayList<ChatMessage> loadDataSet(String packetId, String threadId,int appendDirection, int limit, String descAsc, boolean forceReverse) {

		User me = getUser(SHAMChatApplication.getConfig().getUserId());

		ArrayList<ChatMessage> listMsg = new ArrayList<ChatMessage>();

		if (me != null) {

			int _id = 0;

			ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
			SQLiteDatabase db = databaseHelper.getReadableDatabase();

			//get _id of message from database
			String[] tableColumns = new String[] {"_id"};
			String orderBy = null  ;

			Log.d("tegg" , "trues") ;
			if (threadId.indexOf("-ch") != -1 ) {
				Log.d("tegg" , "true") ;
				orderBy = ChatMessage.MESSAGE_DATETIME ;
			}else {
				orderBy = "_id";
			}

			Cursor cursor = db.query(TABLE_NAME_CHATS, tableColumns,
					ChatMessage.PACKET_ID + "=? OR _id=?",

					new String[] { packetId, packetId }, null, null, orderBy);

			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				_id = cursor.getInt(cursor.getColumnIndex(MessageConstants._ID));
			}


			String[] selectionArgs = new String[] {
					threadId,
					String.valueOf(_id)
			};

			String operator = (appendDirection == EndlessScrollListener.SCROLL_DIRECTION_UP) ? "<" : ">";

			Log.i("scrolldown", "loading from packet number to bottom: "+ String.valueOf(_id));

			String queryString = "SELECT * FROM (select * from "+TABLE_NAME_CHATS+" where thread_id=?) alias_name WHERE _id "+operator+" ? ORDER BY "+orderBy+" "+descAsc+" limit 0,"+limit;
			cursor = db.rawQuery(queryString, selectionArgs);
			if (cursor.moveToFirst()) {
				do {
					ChatMessage chatMessage = getChatMessageByCursor(cursor);


					String sender = chatMessage.getSender();
					System.out.println("chat message sender "+ chatMessage.getSender());

					if (me.getUserId().equals(sender)) {
						chatMessage.setUser(me);
					} else {

						if (!sender.startsWith("g") && !sender.startsWith("ch")|| !sender.substring( sender.indexOf("-")+1).startsWith("s")) {
							chatMessage.setUser(getUser(chatMessage.getSender()));
						}
					}
					listMsg.add(chatMessage);
				} while (cursor.moveToNext());


			}


			cursor.close();
			db.close();
		}

		if (forceReverse) {
			Collections.reverse(listMsg);
		}

		return listMsg;
	}


	public ArrayList<ChatMessage> ChannelDataLoad (String firstItemAdded , String ThreadId) {
		Cursor cursor = null ;
		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getReadableDatabase();

		User me = getUser(SHAMChatApplication.getConfig().getUserId());
		ArrayList<ChatMessage> listMsg = new ArrayList<ChatMessage>();

		String queryString = "SELECT * FROM  "+TABLE_NAME_CHATS+" WHERE thread_id=? AND _id >= ? ORDER BY "+ChatMessage.MESSAGE_DATETIME+" DESC " ;

		String[] selectionArgs = new String[] {
				ThreadId,
				String.valueOf(firstItemAdded)
		};

		cursor = db.rawQuery(queryString, selectionArgs);
		if (cursor.moveToFirst()) {
			do {
				ChatMessage chatMessage = getChatMessageByCursor(cursor);


				String sender = chatMessage.getSender();
				System.out.println("chat message sender "+ chatMessage.getSender());

				if (me.getUserId().equals(sender)) {
					chatMessage.setUser(me);
				} else {

					if (!sender.startsWith("g") && !sender.startsWith("ch")|| !sender.substring( sender.indexOf("-")+1).startsWith("s")) {
						chatMessage.setUser(getUser(chatMessage.getSender()));
					}
				}
				listMsg.add(chatMessage);
			} while (cursor.moveToNext());


		}


		return  listMsg ;
	}

	*/
/**
	 * This checks if this packetId is the last record of database
	 * according to top or bottom direction
	 * * @param packetId
	 * @param threadId
	 * @param direction
	 * @return
	 *//*

	public boolean isLastRecord(String packetId,String threadId,int direction) {
		String orderBy = "" ;
		if (threadId.indexOf("-ch") != -1 ) {
			Log.d("tegg" , "true") ;
			orderBy = ChatMessage.MESSAGE_DATETIME ;
		}else {
			orderBy = "_id";
		}

		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getReadableDatabase();


		String[] selectionArgs = new String[] {threadId};
		String operator = (direction == EndlessScrollListener.SCROLL_DIRECTION_UP) ? "min" : "max";

		int lastMessageId=0;
		String lastMessagepacketId=null;
		//String queryString = "SELECT * FROM (select * from "+TABLE_NAME_CHATS+" where thread_id=?) alias_name WHERE _id "+operator+" ? ORDER BY _id DESC limit 0,"+limit;
		String queryLastRow = "select "+operator+"(_id),_id,packet_id from "+TABLE_NAME_CHATS+" where thread_id=?";

		Cursor cursor = db.rawQuery(queryLastRow, selectionArgs);
		if (cursor.moveToFirst()) {
			do {
				lastMessageId = cursor.getInt(cursor.getColumnIndex(MessageConstants._ID));
				lastMessagepacketId = cursor.getString(cursor.getColumnIndex(ChatMessage.PACKET_ID));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

		if (lastMessagepacketId.equals(packetId)) return true;
		else return false;
	}


	*/
/**
	 * This checks if this packetId is the last record of database
	 * according to top or bottom direction
	 * * @param packetId
	 * @param threadId
	 * @param direction
	 * @return
	 *//*

	public boolean previousToLastRecord(String packetId,String threadId,int direction) {

		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getReadableDatabase();

		String orderBy = "" ;
		if (threadId.indexOf("-ch") != -1 ) {
			Log.d("tegg" , "true") ;
			orderBy = ChatMessage.MESSAGE_DATETIME ;
		}else {
			orderBy = "_id";
		}


		String[] selectionArgs = new String[] {threadId};
		String operator = (direction == EndlessScrollListener.SCROLL_DIRECTION_UP) ? "Asc" : "Desc";

		int beforeLastMessageId=0;
		String beforeLastMessagepacketId=null;
		//String queryString = "SELECT * FROM (select * from "+TABLE_NAME_CHATS+" where thread_id=?) alias_name WHERE _id "+operator+" ? ORDER BY _id DESC limit 0,"+limit;
		String queryLastRow = "select _id,packet_id from "+TABLE_NAME_CHATS+" where thread_id=?  ORDER BY "+orderBy+" "+operator+" limit 1,1";

		Cursor cursor = db.rawQuery(queryLastRow, selectionArgs);

		if (cursor.moveToFirst()) {
			beforeLastMessageId = cursor.getInt(cursor.getColumnIndex(MessageConstants._ID));
			beforeLastMessagepacketId = cursor.getString(cursor.getColumnIndex(ChatMessage.PACKET_ID));
		}

		cursor.close();
		db.close();

		if (beforeLastMessagepacketId.equals(packetId)) return true;
		else return false;
	}

	*/
/**
	 * returns the last message of a threadId
	 * @param threadId
	 * @return
	 *//*

	public ChatMessage getLastMessageFromDB(String threadId) {

		String orderBy = "" ;
		if (threadId.indexOf("-ch") != -1 ) {

			orderBy = ChatMessage.MESSAGE_DATETIME ;
		}else {

		}

		orderBy = "_id";

		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getReadableDatabase();


		String[] selectionArgs = new String[] {threadId};

		String queryLastRow = "SELECT * FROM "+TABLE_NAME_CHATS+" WHERE thread_id=? ORDER BY "+orderBy+" DESC LIMIT 1";

		Cursor cursor = db.rawQuery(queryLastRow, selectionArgs);
		ChatMessage c = null;

		if (cursor.moveToFirst()) {
			c = getChatMessageByCursor(cursor);
		}

		cursor.close();
		db.close();

		return c;
	}

	*/
/**
	 * returns the message object from a packetId
	 * @param packetId
	 * @return
	 *//*

	public ChatMessage getMessageFromDB(String packetId) {

		User me = getUser(SHAMChatApplication.getConfig().getUserId());

		Cursor cursor = SHAMChatApplication.getMyApplicationContext().getContentResolver()
				.query(ChatProviderNew.CONTENT_URI_CHAT, null,
						ChatMessage.PACKET_ID + "=? OR _id=?",
						new String[] { packetId, packetId }, null);

		ChatMessage chatMessage = null;

		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();

			chatMessage = getChatMessageByCursor(cursor);

			String sender = chatMessage.getSender();
			System.out.println("chat message sender "+ chatMessage.getSender());

			if (me.getUserId().equals(sender)) {
				chatMessage.setUser(me);
			} else {

				if (!sender.startsWith("g") && !sender.startsWith("ch") || !sender.substring( sender.indexOf("-")+1).startsWith("s")) {
					chatMessage.setUser(getUser(chatMessage.getSender()));
				}
			}


		}

		cursor.close();

		return chatMessage;
	}


	*/
/**
	 * This sets last N messages of a thread as unread
	 * used this for debugging
	 * @param threadId
	 * @param limit
	 * @return
	 *//*

	public ArrayList<ChatMessage> setAsUnread(String threadId, int limit) {

		String orderBy = "" ;
		if (threadId.indexOf("-ch") != -1 ) {
			Log.d("tegg" , "true") ;
			orderBy = ChatMessage.MESSAGE_DATETIME ;
		}else {
			orderBy = "_id";
		}

		User me = getUser(SHAMChatApplication.getConfig().getUserId());

		ArrayList<ChatMessage> listMsg = new ArrayList<ChatMessage>();

		if (me != null) {

			ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
			SQLiteDatabase db = databaseHelper.getReadableDatabase();

			String[] selectionArgs = new String[] {threadId, String.valueOf(MyMessageType.INCOMING_MSG.ordinal())};


			String queryString = "SELECT * FROM "+TABLE_NAME_CHATS+" WHERE thread_id=? AND message_type =? ORDER BY "+orderBy+" DESC LIMIT 0,"+ limit;
			Cursor cursor = db.rawQuery(queryString, selectionArgs);
			if (cursor.moveToFirst()) {
				do {
					ChatMessage chatMessage = getChatMessageByCursor(cursor);
					ContentValues cv = new ContentValues();
					cv.put("message_status",MessageStatusType.QUEUED.ordinal());
					db.update(TABLE_NAME_CHATS, cv, "_id=?", new String[] {String.valueOf(chatMessage.getMessageId())});

				} while (cursor.moveToNext());


			}


			cursor.close();
			db.close();
		}

		return listMsg;
	}


	*/
/**
	 * returns the first unread message of a thread from db
	 * @param threadId
	 * @return
	 *//*

	public ChatMessage getFirtUnreadMessageFromDB(String threadId) {

		String orderBy = "" ;
		if (threadId.indexOf("-ch") != -1 ) {
			Log.d("tegg" , "true") ;
			orderBy = ChatMessage.MESSAGE_DATETIME ;
		}else {
			orderBy = "_id";
		}

		ChatMessage chatMessage = null;

		User me = getUser(SHAMChatApplication.getConfig().getUserId());

		Cursor unreadMessagesCursor = SHAMChatApplication.getMyApplicationContext().getContentResolver()
				.query(ChatProviderNew.CONTENT_URI_CHAT,null,
						ChatMessage.MESSAGE_STATUS + "=? AND "
								+ ChatMessage.MESSAGE_TYPE + "=? AND "
								+ ChatMessage.THREAD_ID + "=?",
						new String[] {
								MessageStatusType.QUEUED.ordinal() + "",
								MyMessageType.INCOMING_MSG.ordinal() + "",
								threadId }, null);

		if (unreadMessagesCursor != null && unreadMessagesCursor.getCount() > 0) {
			unreadMessagesCursor.moveToFirst();

			chatMessage = getChatMessageByCursor(unreadMessagesCursor);

			String sender = chatMessage.getSender();
			System.out.println("chat message sender "+ chatMessage.getSender());

			if (me.getUserId().equals(sender)) {
				chatMessage.setUser(me);
			} else {

				if (!sender.startsWith("g") && !sender.startsWith("ch") || !sender.substring( sender.indexOf("-")+1).startsWith("s")) {
					chatMessage.setUser(getUser(chatMessage.getSender()));
				}
			}


		}

		unreadMessagesCursor.close();

		return chatMessage;
	}


	*/
/**
	 * returns the unread messages count of a thread
	 * @param threadId
	 * @return
	 *//*

	public int getUnreadMessagesCount(String threadId) {

		String orderBy = "" ;
		if (threadId.indexOf("-ch") != -1 ) {
			Log.d("tegg" , "true") ;
			orderBy = ChatMessage.MESSAGE_DATETIME ;
		}else {
			orderBy = "_id";
		}

		Cursor unreadMessagesCursor = SHAMChatApplication.getMyApplicationContext().getContentResolver()
				.query(ChatProviderNew.CONTENT_URI_CHAT,
						new String[] { ChatMessage.PACKET_ID },
						ChatMessage.MESSAGE_STATUS + "=? AND "
								+ ChatMessage.MESSAGE_TYPE + "=? AND "
								+ ChatMessage.THREAD_ID + "=?",
						new String[] {
								MessageStatusType.QUEUED.ordinal() + "",
								MyMessageType.INCOMING_MSG.ordinal() + "",
								threadId }, null);
		unreadMessagesCursor.close();

		int count = unreadMessagesCursor.getCount();


		unreadMessagesCursor.close();

		return count;
	}

	*/
/**
	 * Gets a user object from a user id
	 * @param userId
	 * @return
	 *//*

	private User getUser(String userId) {

		ContentResolver contentResolver = SHAMChatApplication
				.getMyApplicationContext().getContentResolver();

		Cursor friendCursor = null;
		Cursor rosterCursor = null;
		User user = null;
		try {

			friendCursor = contentResolver.query(UserProvider.CONTENT_URI_USER,
					null, UserConstants.USER_ID + "=?",
					new String[] { userId }, null);
			friendCursor.moveToFirst();

			String friendJID = Utils.createXmppUserIdByUserId(userId);

			rosterCursor = contentResolver.query(RosterProvider.CONTENT_URI,
					null, RosterConstants.JID + "=?",
					new String[] { friendJID }, null);

			if (friendCursor.getCount()>= 1) {

				user = new User();
				int blockCount = 0;

				user.setUserId(userId);

				String username = friendCursor.getString(friendCursor
						.getColumnIndex(UserConstants.USERNAME));
				user.setUsername(username);

				*/
/*String status = friendCursor.getString(friendCursor
						.getColumnIndex(UserConstants.MY_STATUS));
				user.setMyStatus(status);*//*


				String friendMobileNo = friendCursor.getString(friendCursor
						.getColumnIndex(UserConstants.MOBILE_NO));

				user.setMobileNo(friendMobileNo);

				*/
/*String profileImageUrl = friendCursor.getString(friendCursor
						.getColumnIndex(UserConstants.PROFILE_IMAGE_URL));
				user.setprofileImageUrl(profileImageUrl);

				if (rosterCursor != null && rosterCursor.getCount() > 0) {
					rosterCursor.moveToFirst();
					if (rosterCursor != null) {
						blockCount = rosterCursor.getCount();
					}

					int blockStatus = rosterCursor.getInt(rosterCursor
							.getColumnIndex(RosterConstants.USER_STATUS));
					System.out.println("User blck status " + blockStatus);
					if (blockStatus == 1) {
						user.setBlocked(true);
					} else {
						user.setBlocked(false);
					}

					user.setStatusMode(rosterCursor.getInt(rosterCursor
							.getColumnIndex(RosterConstants.STATUS_MODE)));

					String statusMessage = rosterCursor.getString(rosterCursor
							.getColumnIndex(RosterConstants.STATUS_MESSAGE));

					user.setMyStatus(statusMessage);

				}

				int userType = friendCursor.getInt(friendCursor
						.getColumnIndex(UserConstants.USER_TYPE));

				if (userType == 2) {
					user.setIsInChat(BooleanStatus.TRUE);
				} else {
					user.setIsInChat(BooleanStatus.FALSE);
				}

				String gender = friendCursor.getString(friendCursor
						.getColumnIndex(UserConstants.GENDER));
				user.setGender(gender);

				String cityOrRegion = friendCursor.getString(friendCursor
						.getColumnIndex(UserConstants.CITY_OR_REGION));
				user.setCityOrRegion(cityOrRegion);*//*


				String chatId = friendCursor.getString(friendCursor
						.getColumnIndex(UserConstants.CHAT_ID));
				user.setChatId(chatId);
			}

		} finally {
			friendCursor.close();
			rosterCursor.close();
		}
		return user;
	}


	public ArrayList<Message> getFavorites(String userId) {
		ArrayList<Message> favorites = new ArrayList<Message>();

		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(
				SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		Cursor cursor = db.query(TABLE_NAME_FAVORITE, null, Message.DB_USER_ID
				+ "=? AND " + Message.DB_DELETED + "=?", new String[] { userId,
				"0" }, null, null, Message.DB_TIME + " DESC");
		if (cursor.moveToFirst()) {
			do {
				favorites.add(favoriteToCursor(cursor));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
		return favorites;
	}

	public Message favoriteToCursor(Cursor cursor) {
		Message message = new Message();
		message.setMessageId(cursor.getString(cursor
				.getColumnIndex(Message.DB_MESSAGE_ID)));
		int type = cursor
				.getInt(cursor.getColumnIndex(Message.DB_MESSAGE_TYPE));
		message.setType(type);
		message.setMessageContent(cursor.getString(cursor
				.getColumnIndex(Message.DB_CONTENT)));
		message.setTime(cursor.getLong(cursor.getColumnIndex(Message.DB_TIME)));
		message.setUserId(cursor.getString(cursor
				.getColumnIndex(Message.DB_USER_ID)));
		return message;
	}

	public void insertFavorite(Message message) {
		// SQLiteDatabase database = getWritableDatabase();
		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(
				SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Message.DB_MESSAGE_ID, message.getMessageId());
		values.put(Message.DB_MESSAGE_TYPE, message.getType().getValue());
		values.put(Message.DB_CONTENT, message.getMessageContent());
		values.put(Message.DB_TIME, message.getTime());
		values.put(Message.DB_USER_ID, message.getUserId());

		long row = db.insert(TABLE_NAME_FAVORITE, null, values);

		if (row == -1) {
			Log.e(TAG, "Error inserting Message(" + message + ")");
		}

		Log.v(TAG, "Inserted new Message with id '" + message.getMessageId()
				+ "' at rowid " + row);

		db.close();
	}

	public void updateFavorite(Message message) {

		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(
				SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Message.DB_MESSAGE_ID, message.getMessageId());
		values.put(Message.DB_MESSAGE_TYPE, message.getType().getValue());
		values.put(Message.DB_CONTENT, message.getMessageContent());
		values.put(Message.DB_TIME, message.getTime());
		values.put(Message.DB_USER_ID, message.getUserId());

		long row = db.update(TABLE_NAME_FAVORITE, values, Message.DB_MESSAGE_ID
				+ "=?", new String[] { message.getMessageId() });

		if (row < 1) {
			Log.e(TAG, "Error updating Message(" + message + ")");
		}

		db.close();
	}

	public boolean removeFavorite(String messageId) {

		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(
				SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		int rows = 0;

		try {

			ContentValues values = new ContentValues();
			values.put(Message.DB_DELETED, 1);
			rows = db.update(TABLE_NAME_FAVORITE, values, Message.DB_MESSAGE_ID
					+ " = ?", new String[] { messageId });

		} catch (Exception e) {
			Log.w(TAG, "Couldn't remove message with id '" + messageId
					+ "' (not found)");
			e.printStackTrace();
		} finally {
			db.close();
		}

		return rows >= 0;
	}

	*/
/**
	 * Returns the favorite message or null
	 *
	 * @param messageId
	 * @return
	 *//*

	public Message getFavorite(String messageId) {
		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(
				SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		Cursor cursor = db.query(TABLE_NAME_FAVORITE, null,
				Message.DB_MESSAGE_ID + "=?", new String[] { messageId }, null,
				null, null);

		Message message = null;

		if (cursor.moveToFirst()) {
			message = favoriteToCursor(cursor);
		}

		cursor.close();
		db.close();

		return message;
	}

	*/
/**
	 *
	 * @return
	 *//*

	public String geLasttFavoriteMessageId() {
		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(
				SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		String messageId = null;
		Cursor cursor = db.query(TABLE_NAME_FAVORITE,
				new String[] { Message.DB_MESSAGE_ID }, null, null, null, null,
				Message.DB_MESSAGE_ID + " DESC", "1");

		if (cursor.moveToFirst()) {
			messageId = cursor.getString(cursor
					.getColumnIndex(Message.DB_MESSAGE_ID));
		}

		cursor.close();
		db.close();
		return messageId;
	}

	*/
/**
	 * returns all the chat message threads with its unread count, username, etc
	 * sroted by Date desc
	 * @return
	 *//*

	public List<MessageThread> getChatThreadsSorted() throws IOException, JSONException {

		ChatDatabaseHelper databaseHelper = new ChatDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase db = databaseHelper.getReadableDatabase();

		String CHAT_DATABASE_PATH = databaseHelper.CHAT_DATABASE_PATH;
		String USER_DATABASE_PATH = databaseHelper.USER_DATABASE_PATH;


		List<MessageThread> messageThreads = new ArrayList<MessageThread>();

		String queryLastRow = "SELECT  friend_group_name as username,\n" +
				"(SELECT COUNT(*) FROM chat_message WHERE chat_message.thread_id = message_thread.thread_id AND chat_message.message_status=0 AND chat_message.message_type=1) AS count,\n" +
				"friend_id, thread_id, is_group_chat, last_message, last_message_direction, last_updated_datetime, last_message_content_type\n" +
				"FROM message_thread\n" +
				" INNER JOIN user.friend_group\n" +
				"      ON message_thread.friend_id = user.friend_group.friend_group_id\n" +
				"UNION ALL\n" +
				"SELECT  name,\n" +
				"(SELECT COUNT(*) FROM chat_message WHERE chat_message.thread_id = message_thread.thread_id AND chat_message.message_status=0 AND chat_message.message_type=1) AS count,\n" +
				"friend_id, thread_id, is_group_chat, last_message, last_message_direction, last_updated_datetime, last_message_content_type\n" +
				"FROM message_thread\n" +
				" INNER JOIN (SELECT * \n" +
				"             FROM user.user \n" +
				"             GROUP BY userId \n" +
				"             ORDER BY userId ) O ON friend_id = userId\n" +
				"ORDER BY last_updated_datetime DESC\n";

		db.execSQL("ATTACH DATABASE '" + USER_DATABASE_PATH + "' AS user");

		Cursor cursor = db.rawQuery(queryLastRow, null);
		if (cursor.moveToFirst()) {
			do {
				MessageThread messageThread = new MessageThread();
				messageThread.setThreadOwner(SHAMChatApplication.getConfig()
						.getUserId());
				messageThread.setFriendId(cursor.getString(cursor.getColumnIndex("friend_id")));
				messageThread.setThreadId(cursor.getString(cursor.getColumnIndex("thread_id")));
				messageThread.setGroupChat(cursor.getInt(cursor.getColumnIndex("is_group_chat"))==1);
				messageThread.setLastMessage(cursor.getString(cursor.getColumnIndex("last_message")));
				messageThread.setLastMessageDirection(cursor.getInt(cursor.getColumnIndex("last_message_direction")));
				messageThread.setLastUpdatedDate(Utils.getDateFromStringDate(cursor.getString(cursor.getColumnIndex("last_updated_datetime")), "yyyy/MM/dd hh:mm"));
				messageThread.setLastMessageMedium(cursor.getInt(cursor.getColumnIndex("last_message_content_type")));
				messageThread.setUsername(cursor.getString(cursor.getColumnIndex("username")));
				messageThread.setMessageCount(cursor.getInt(cursor.getColumnIndex("count")));


				String imageLocation="";
		if (cursor.getString(cursor.getColumnIndex("friend_id")).startsWith("g") || cursor.getString(cursor.getColumnIndex("friend_id")).substring( cursor.getString(cursor.getColumnIndex("friend_id")).indexOf("-")+1).startsWith("s"))
		{*/
/*
			OkHttpClient client = new OkHttpClient();


			client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
			client.setReadTimeout(60, TimeUnit.SECONDS);

			// socket timeout
			Request request = new Request.Builder()
					.url("http://social.rabtcdn.com/groups/api/v1/avatar/with/topic_hashcode/" + cursor.getString(cursor.getColumnIndex("friend_id")) + "/")
					.cacheControl(new CacheControl.Builder().noCache().build())
					.build();

			Response responses = null;
			try {
				responses = client.newCall(request).execute();



			} catch (IOException e) {
				e.printStackTrace();
			}

			if (responses.isSuccessful()) {
				String jsonData = responses.body().string();
				JSONObject Jobject = new JSONObject(jsonData);
				imageLocation = Jobject.getString("avatar");
			}
			//JSONArray Jarray = Jobject.getJSONArray("objects");

				*//*
*/
/*	for (int i = 0; i < Jarray.length(); i++) {
						JSONObject object     = Jarray.getJSONObject(i);
					}*//*
*/
/*
*//*

		}
		else if(cursor.getString(cursor.getColumnIndex("friend_id")).startsWith("ch"))
		{*/
/*
			OkHttpClient client = new OkHttpClient();


			client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
			client.setReadTimeout(60, TimeUnit.SECONDS);

			// socket timeout
			Request request = new Request.Builder()
					.url("http://social.rabtcdn.com/groups/api/v1/avatar/with/channel_hashcode/" + cursor.getString(cursor.getColumnIndex("friend_id")) + "/")
					.cacheControl(new CacheControl.Builder().noCache().build())
					.build();

			Response responses = null;
			try {
				responses = client.newCall(request).execute();



			} catch (IOException e) {
				e.printStackTrace();
			}

			if (responses.isSuccessful()) {
				String jsonData = responses.body().string();
				JSONObject Jobject = new JSONObject(jsonData);
				imageLocation = Jobject.getString("avatar");
			}
			//JSONArray Jarray = Jobject.getJSONArray("objects");

				*//*
*/
/*	for (int i = 0; i < Jarray.length(); i++) {
						JSONObject object     = Jarray.getJSONObject(i);
					}*//*
*/
/*
*//*

		}
		else
		{		OkHttpClient client = new OkHttpClient();


			client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
			client.setReadTimeout(60, TimeUnit.SECONDS);

			// socket timeout
			Request request = new Request.Builder()
					.url("http://social.rabtcdn.com/groups/api/v1/avatar/with/userid/" + cursor.getString(cursor.getColumnIndex("friend_id")) + "/")
					.cacheControl(new CacheControl.Builder().noCache().build())
					.build();

			Response responses = null;
			try {
				responses = client.newCall(request).execute();



			} catch (IOException e) {
				e.printStackTrace();
			}

			if (responses.isSuccessful()) {
				String jsonData = responses.body().string();
				JSONObject Jobject = new JSONObject(jsonData);
				imageLocation = Jobject.getString("avatar");
			}
			//JSONArray Jarray = Jobject.getJSONArray("objects");

				*/
/*	for (int i = 0; i < Jarray.length(); i++) {
						JSONObject object     = Jarray.getJSONObject(i);
					}*//*


		}





				messageThread.setFriendProfileImageUrl("http://social.rabtcdn.com"+imageLocation);
				messageThreads.add(messageThread);
			} while (cursor.moveToNext());
		}


		cursor.close();
		db.close();

		return messageThreads;

	}

}
*/
