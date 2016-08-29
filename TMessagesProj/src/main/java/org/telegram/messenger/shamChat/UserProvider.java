/*
package org.telegram.messenger.shamChat;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.shamchat.activity.R;
import com.shamchat.androidclient.SHAMChatApplication;
import com.shamchat.androidclient.util.PreferenceConstants;
import com.shamchat.androidclient.util.PreferenceConstants.AllowDeniedStatus;
import com.shamchat.androidclient.util.PreferenceConstants.EnableDisableStatus;
import com.shamchat.androidclient.util.PreferenceConstants.FeatureAlertStatus;
import com.shamchat.models.FriendGroup;
import com.shamchat.models.FriendGroupMember;
import com.shamchat.models.User;
import com.shamchat.models.User.BooleanStatus;
import com.shamchat.models.UserNotification;
import com.shamchat.utils.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.conn.ClientConnectionManager;
import cz.msebera.android.httpclient.conn.scheme.*;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.conn.*;
import cz.msebera.android.httpclient.impl.conn.tsccm.*;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.params.BasicHttpParams;


public class UserProvider extends ContentProvider {

	public static final String AUTHORITY = "org.zamin.androidclient.provider.Users";
	public static final String TABLE_NAME_USER = "user";
	public static final String TABLE_NAME_FRIEND_GROUP = "friend_group";
	public static final String TABLE_NAME_FRIEND_GROUP_MEMBER = "friend_group_member";
	public static final String TABLE_NAME_USER_NOTIFICATION = "user_notification";

	public static final Uri CONTENT_URI_USER = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME_USER);
	public static final Uri CONTENT_URI_GROUP = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME_FRIEND_GROUP);
	public static final Uri CONTENT_URI_GROUP_MEMBER = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME_FRIEND_GROUP_MEMBER);
	public static final Uri CONTENT_URI_NOTIFICATION = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME_USER_NOTIFICATION);

	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int ALL_USERS = 1;
	private static final int USER_ID = 2;
	private static final int GROUPS = 3;
	private static final int GROUP_ID = 4;
	private static final int GROUP_MEMBERS = 5;
	private static final int GROUP_MEMBER_ID = 6;
	private static final int NOTIFICATION = 7;

	static {
		URI_MATCHER.addURI(AUTHORITY, "user", ALL_USERS);
		URI_MATCHER.addURI(AUTHORITY, "user/#", USER_ID);
		URI_MATCHER.addURI(AUTHORITY, "friend_group", GROUPS);
		URI_MATCHER.addURI(AUTHORITY, "friend_group/#", GROUP_ID);
		URI_MATCHER.addURI(AUTHORITY, "friend_group_member", GROUP_MEMBERS);
		URI_MATCHER.addURI(AUTHORITY, "friend_group_member/#", GROUP_MEMBER_ID);
		URI_MATCHER.addURI(AUTHORITY, "user_notification", NOTIFICATION);
	}

	private static final String TAG = "zamin.UserProvider";

	private SQLiteOpenHelper mOpenHelper;

	public UserProvider() {

	}

	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (URI_MATCHER.match(url)) {

			case ALL_USERS:
				count = db.delete(TABLE_NAME_USER, where, whereArgs);
				break;
			case USER_ID:
				String segment = url.getPathSegments().get(1);

				if (TextUtils.isEmpty(where)) {
					where = "_id=" + segment;
				} else {
					where = "_id=" + segment + " AND (" + where + ")";
				}

				count = db.delete(TABLE_NAME_USER, where, whereArgs);
				break;

			case GROUPS:
				count = db.delete(TABLE_NAME_FRIEND_GROUP, where, whereArgs);
				break;

			case GROUP_ID:
				segment = url.getPathSegments().get(1);

				if (TextUtils.isEmpty(where)) {
					where = "_id=" + segment;
				} else {
					where = "_id=" + segment + " AND (" + where + ")";
				}

				count = db.delete(TABLE_NAME_FRIEND_GROUP, where, whereArgs);
				break;

			case GROUP_MEMBERS:
				count = db.delete(TABLE_NAME_FRIEND_GROUP_MEMBER, where, whereArgs);
				break;

			case GROUP_MEMBER_ID:
				segment = url.getPathSegments().get(1);

				if (TextUtils.isEmpty(where)) {
					where = "_id=" + segment;
				} else {
					where = "_id=" + segment + " AND (" + where + ")";
				}

				count = db.delete(TABLE_NAME_FRIEND_GROUP_MEMBER, where, whereArgs);
				break;

			case NOTIFICATION:
				count = db.delete(TABLE_NAME_USER_NOTIFICATION, where, whereArgs);
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
			case ALL_USERS:
				return UserConstants.CONTENT_TYPE;
			case USER_ID:
				return UserConstants.CONTENT_ITEM_TYPE;
			case GROUPS:
				return GroupConstants.CONTENT_GROUP_TYPE;
			case GROUP_ID:
				return GroupConstants.CONTENT_GROUP_ITEM_TYPE;
			case GROUP_MEMBERS:
				return GroupMemberConstants.CONTENT_GROUP_MEMBER_TYPE;
			case GROUP_MEMBER_ID:
				return GroupMemberConstants.CONTENT_GROUP_MEMBER_ITEM_TYPE;
			case NOTIFICATION:
				return NotificationConstants.CONTENT_NOTIFICATION_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {

		if (URI_MATCHER.match(url) != ALL_USERS && URI_MATCHER.match(url) != GROUPS && URI_MATCHER.match(url) != GROUP_MEMBERS && URI_MATCHER.match(url) != NOTIFICATION) {
			throw new IllegalArgumentException("Cannot insert into URL: " + url);
		}

		long rowId=-1;
		Uri noteUri=null;

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		ContentValues values = (initialValues != null) ? new ContentValues(
				initialValues) : new ContentValues();

		switch (URI_MATCHER.match(url)) {

			case ALL_USERS:

				for (String colName : UserConstants.getRequiredColumns()) {
					if (values.containsKey(colName) == false) {
						throw new IllegalArgumentException("Missing column: "
								+ colName);
					}
				}

				rowId = db.insert(TABLE_NAME_USER, "", values);

				if (rowId < 0) {
					throw new SQLException("Failed to insert row into " + url);
				}

				noteUri = ContentUris.withAppendedId(CONTENT_URI_USER, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				break;

			case GROUPS:
				for (String colName : GroupConstants.getRequiredColumns()) {
					if (values.containsKey(colName) == false) {
						throw new IllegalArgumentException("Missing column: " + colName);
					}
				}

				rowId = db.insert(TABLE_NAME_FRIEND_GROUP, "", values);

				if (rowId < 0) {
					throw new SQLException("Failed to insert row into Group" + url);
				}

				if (rowId > 0) {
					Log.d(TAG, "Group successfully entered at "+rowId);
				}


				noteUri = ContentUris.withAppendedId(CONTENT_URI_GROUP, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				break;

			case GROUP_MEMBERS:
				//TODO
				for (String colName : GroupMemberConstants.getRequiredColumns()) {
					if (values.containsKey(colName) == false) {
						throw new IllegalArgumentException("Missing column: " + colName);
					}
				}

				rowId = db.insert(TABLE_NAME_FRIEND_GROUP_MEMBER, "", values);

				if (rowId < 0) {
					throw new SQLException("Failed to insert row into group_members" + url);
				}

				if (rowId > 0) {
					Log.d(TAG, "Group Members successfully entered at "+rowId);
				}

				noteUri = ContentUris.withAppendedId(CONTENT_URI_GROUP_MEMBER, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				break;

			case NOTIFICATION:
				//TODO
				for (String colName : NotificationConstants.getRequiredColumns()) {
					if (values.containsKey(colName) == false) {
						throw new IllegalArgumentException("Missing column: " + colName);
					}
				}

				rowId = db.insert(TABLE_NAME_USER_NOTIFICATION, "", values);

				if (rowId < 0) {
					throw new SQLException("Failed to insert row into group_members" + url);
				}

				if (rowId > 0) {
					Log.d(TAG, "Group Members successfully entered at "+rowId);
				}

				noteUri = ContentUris.withAppendedId(CONTENT_URI_NOTIFICATION, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				break;

			default:
				throw new IllegalArgumentException("Cannot insert from URL: " + url);
		}


		return noteUri;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new UserDatabaseHelper(getContext());
		return true;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {

		if (URI_MATCHER.match(uri) != ALL_USERS && URI_MATCHER.match(uri) != GROUPS && URI_MATCHER.match(uri) != GROUP_MEMBERS && URI_MATCHER.match(uri) != NOTIFICATION) {
			throw new IllegalArgumentException("Cannot insert into URL: " + uri);
		}

		int rowId=0;

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (URI_MATCHER.match(uri)) {
			case ALL_USERS:

				db.beginTransaction();

				for (ContentValues i:values) {

					rowId = rowId + (int) db.insert(TABLE_NAME_USER, "", i);
				}

				db.setTransactionSuccessful();
				db.endTransaction();

				getContext().getContentResolver().notifyChange(CONTENT_URI_USER, null);
				break;
			case GROUPS:

				db.beginTransaction();

				for (ContentValues i:values) {

					rowId = rowId + (int) db.insert(TABLE_NAME_FRIEND_GROUP, "", i);
				}

				db.setTransactionSuccessful();
				db.endTransaction();

				getContext().getContentResolver().notifyChange(CONTENT_URI_GROUP, null);
				break;
			case GROUP_MEMBERS:

				db.beginTransaction();

				for (ContentValues i:values) {

					rowId = rowId + (int) db.insert(TABLE_NAME_FRIEND_GROUP_MEMBER, "", i);
				}

				db.setTransactionSuccessful();
				db.endTransaction();

				getContext().getContentResolver().notifyChange(CONTENT_URI_GROUP_MEMBER, null);
				break;
			case NOTIFICATION:

				db.beginTransaction();

				for (ContentValues i:values) {

					rowId = rowId + (int) db.insert(TABLE_NAME_USER_NOTIFICATION, "", i);
				}

				db.setTransactionSuccessful();
				db.endTransaction();

				getContext().getContentResolver().notifyChange(CONTENT_URI_NOTIFICATION, null);
				break;

		}

		return rowId;
	}

	@Override
	public Cursor query(Uri url, String[] projectionIn, String selection,
						String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		int match = URI_MATCHER.match(url);

		boolean isDistinct=false;

		switch (match) {
			case ALL_USERS:
				qBuilder.setTables(TABLE_NAME_USER);
				isDistinct=true;
				break;
			case USER_ID:
				qBuilder.setTables(TABLE_NAME_USER);
				qBuilder.appendWhere(UserConstants.USER_ID+"=");
				qBuilder.appendWhere(url.getPathSegments().get(1));
				break;
			case GROUPS:
				qBuilder.setTables(TABLE_NAME_FRIEND_GROUP);
				break;
			case GROUP_ID:
				qBuilder.setTables(TABLE_NAME_FRIEND_GROUP);
				qBuilder.appendWhere("_id=");
				qBuilder.appendWhere(url.getPathSegments().get(1));
				break;
			case GROUP_MEMBERS:
				qBuilder.setTables(TABLE_NAME_FRIEND_GROUP_MEMBER);
				break;
			case GROUP_MEMBER_ID:
				qBuilder.setTables(TABLE_NAME_FRIEND_GROUP_MEMBER);
				qBuilder.appendWhere("_id=");
				qBuilder.appendWhere(url.getPathSegments().get(1));
				break;
			case NOTIFICATION:
				qBuilder.setTables(TABLE_NAME_USER_NOTIFICATION);
				break;
			default:

				throw new IllegalArgumentException("Unknown URL " + url);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = UserConstants.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		Cursor ret = null;


		if(isDistinct)
		{
			try {
				ret = db.query(isDistinct, TABLE_NAME_USER, projectionIn, selection, selectionArgs, UserConstants.USERNAME, null, UserConstants.USERNAME+" COLLATE NOCASE ASC", null);
			} catch (Exception e) {
				ret = null;
			}
		}

		else
		{
			try {
				ret = qBuilder.query(db, projectionIn, selection, selectionArgs, null, null, orderBy);
			} catch (Exception e) {
				ret = null;
			}
		}

		if (ret == null) {
			infoLog("UserProvider.query: failed");
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
		int match = URI_MATCHER.match(url);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (match) {
			case ALL_USERS:
				count = db.update(TABLE_NAME_USER, values, where, whereArgs);
				break;
			case USER_ID:
				String segment = url.getPathSegments().get(1);
				rowId = Long.parseLong(segment);
				count = db.update(TABLE_NAME_USER, values, UserConstants.USER_ID+"=" + rowId, null);

				if (count > 0) {
					Log.d(TAG, "User successfully updated "+rowId);
				}

				break;
			case GROUPS:
				count = db.update(TABLE_NAME_FRIEND_GROUP, values, where, whereArgs);

				if (count > 0) {
					Log.d(TAG, "Message successfully updated "+count);
				}

				break;
			case GROUP_ID:
				segment = url.getPathSegments().get(1);
				rowId = Long.parseLong(segment);
				count = db.update(TABLE_NAME_FRIEND_GROUP, values, "_id=" + rowId, null);
				break;
			case GROUP_MEMBERS:
				count = db.update(TABLE_NAME_FRIEND_GROUP_MEMBER, values, where, whereArgs);

				if (count > 0) {
					Log.d(TAG, "Thread successfully updated "+count);
				}

				break;
			case GROUP_MEMBER_ID:
				segment = url.getPathSegments().get(1);
				rowId = Long.parseLong(segment);
				count = db.update(TABLE_NAME_FRIEND_GROUP_MEMBER, values, where, whereArgs);

				break;

			case NOTIFICATION:
				count = db.update(TABLE_NAME_USER_NOTIFICATION, values, where, whereArgs);

				if (count > 0) {
					Log.d(TAG, "Message successfully updated "+count);
				}

				break;
			default:
				throw new UnsupportedOperationException("Cannot update URL: " + url);
		}

		infoLog("*** notifyChange() rowId: " + rowId + " url " + url);

		getContext().getContentResolver().notifyChange(url, null);
		return count;

		//return 1;
	}

	private static void infoLog(String data) {

	}

	public static class UserDatabaseHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "user.db";
		private static final int DATABASE_VERSION = 11;

		public UserDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {



			String sql="CREATE TABLE IF NOT EXISTS "
					+ UserProvider.TABLE_NAME_USER + " ( " + UserConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + UserConstants.USER_ID
					+ " TEXT NOT NULL," + UserConstants.CHAT_ID + " TEXT," + UserConstants.USERNAME
					+ " TEXT," + UserConstants.GENDER + " TEXT," + UserConstants.PROFILE_IAMGE + " TEXT,"
					+ UserConstants.ONLINE_STATUS + " TEXT  NOT NULL DEFAULT Offline,"
					+ UserConstants.EMAIL + " TEXT," + UserConstants.EMAIL_VERIFICATION_STATUS + " TEXT,"
					+ UserConstants.IN_APP_ALERT + " TEXT," + UserConstants.MY_STATUS + " TEXT, "
					+ UserConstants.MOBILE_NO + " TEXT, " + UserConstants.CITY_OR_REGION + " TEXT, "
					+ UserConstants.TEMP_USER_ID + " TEXT," + UserConstants.NEW_MESSAGE_ALERT + " TEXT, "
					+ UserConstants.COVER_PHOTO_BYTE + " BLOB, " + UserConstants.JABBERD_RESOURCE
					+ " TEXT, "
					+ UserConstants.FIND_BY_PHONE_NO
					+ " INTEGER NOT NULL DEFAULT 1,"
					+ UserConstants.IS_ADDED_TO_ROSTER
					+ " INTEGER NOT NULL DEFAULT 1,"
					+ UserConstants.IS_VCARD_DOWNLOADED
					+ " INTEGER NOT NULL DEFAULT 1,"
					+ UserConstants.PROFILE_IMAGE_URL
					+ " TEXT, "
					+ UserConstants.USER_TYPE
					+ " INTEGER NOT NULL DEFAULT 1, "
					+ UserConstants.FIND_BY_SHAM_ID
					+ " INTEGER NOT NULL DEFAULT 1);";

			db.execSQL(sql);

			String CREATE_TABLE_FRIEND_GROUP = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_NAME_FRIEND_GROUP + " (" + GroupConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"+ FriendGroup.DB_ID
					+ " TEXT ," + FriendGroup.DB_NAME + " TEXT , "
					+ FriendGroup.CHAT_ROOM_NAME + " TEXT , "
					+ FriendGroup.DID_JOIN_ROOM + " INTEGER NOT NULL DEFAULT 0 , "
					+ FriendGroup.DID_LEAVE + " INTEGER NOT NULL DEFAULT 0 , "
					+ FriendGroup.DB_RECORD_OWNER + " TEXT , "
					+ FriendGroup.DB_DESCRIPTION + " TEXT , "
					+ FriendGroup.DB_LINK_NAME + " TEXT  ,"
					+ FriendGroup.CHANNEL_LOGO + " TEXT , "
					+ FriendGroup.IS_MUTE + " INTEGER NOT NULL DEFAULT 0  "
					+ ");";

			//Log.d("CREATE_TABLE_FRIEND_GROUP" , CREATE_TABLE_FRIEND_GROUP) ;

			db.execSQL(CREATE_TABLE_FRIEND_GROUP);

			String CREATE_TABLE_FRIEND_GROUP_MEMBER = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_NAME_FRIEND_GROUP_MEMBER
					+ " ("
					+ GroupMemberConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ FriendGroupMember.DB_ID
					+ " TEXT ,"
					+ FriendGroupMember.DB_GROUP
					+ " TEXT," + FriendGroupMember.DB_FRIEND + " TEXT , "
					+ FriendGroupMember.DB_FRIEND_DID_JOIN + " INTEGER NOT NULL DEFAULT 0,"
					+ FriendGroupMember.DB_FRIEND_IS_ADMIN + " INTEGER NOT NULL DEFAULT 0, "
					+ FriendGroupMember.PHONE_NUMBER + " TEXT"
					+");";

			db.execSQL(CREATE_TABLE_FRIEND_GROUP_MEMBER);

			String CREATE_TABLE_USER_NOTIFICATION = "CREATE TABLE "
					+ TABLE_NAME_USER_NOTIFICATION
					+ " ("+
					NotificationConstants._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ UserNotification.USER_ID
					+ " TEXT NOT NULL  UNIQUE , "
					+ UserNotification.MESSAGE_ALERT_STATUS
					+ " INTEGER NOT NULL  DEFAULT 1, "
					+ UserNotification.SOUND_ALERT_STATUS
					+ " INTEGER NOT NULL  DEFAULT 1, "
					+ UserNotification.VIBRATE_STATUS
					+ " INTEGER NOT NULL  DEFAULT 0, "
					+ UserNotification.NOTIFICATION_SOUND
					+ " TEXT, "
					+ UserNotification.NOTIFICATION_TIMING_START
					+ " TEXT NOT NULL DEFAULT '0', "
					+ UserNotification.NOTIFICATION_TIMING_END
					+ " TEXT NOT NULL DEFAULT '0', "
					+ UserNotification.OTHER_FEATURE_ALERT_STATUS
					+ " INTEGER NOT NULL  DEFAULT 1, "
					+ UserNotification.MOVEMENT_UPDATE_STATUS
					+ " INTEGER NOT NULL  DEFAULT 1" + ")";

			db.execSQL(CREATE_TABLE_USER_NOTIFICATION);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			infoLog("onUpgrade: from " + oldVersion + " to " + newVersion);
			switch (oldVersion) {
				case 5:
					db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP_MEMBER + " ADD " + FriendGroupMember.DB_FRIEND_DID_JOIN + " INTEGER NOT NULL DEFAULT 0 ");
					db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP_MEMBER + " ADD " + FriendGroupMember.DB_FRIEND_IS_ADMIN + " INTEGER NOT NULL DEFAULT 0 ");
					db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP_MEMBER + " ADD " + FriendGroupMember.PHONE_NUMBER + " TEXT ");

				case 6:
					try {
						db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP_MEMBER + " ADD " + FriendGroupMember.PHONE_NUMBER + " TEXT ");
					} catch (Exception e) {}

				case 7:
					try {
						db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP_MEMBER + " ADD " + FriendGroupMember.PHONE_NUMBER + " TEXT ");
					} catch (Exception e) {

					}

				case 8:
					try {
						db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP + " ADD " + FriendGroup.DB_DESCRIPTION + " TEXT ");
						db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP + " ADD " + FriendGroup.DB_LINK_NAME + " TEXT ");
					} catch (Exception e) {

					}

				case 9:
					try {
						db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP + " ADD " + FriendGroup.CHANNEL_LOGO + " TEXT ");
					} catch (Exception e) {}

				case 10 :
					try {
						db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP + " ADD " + FriendGroup.IS_MUTE + " INTEGER NOT NULL DEFAULT 0 ");
						//db.execSQL("ALTER TABLE " + TABLE_NAME_FRIEND_GROUP + " ADD " + "is_mute" + " INTEGER NOT NULL DEFAULT 0 ");
					}
					catch (Exception e) {}
				break;

//			case 6:
//				db.execSQL("UPDATE " + TABLE_NAME + " SET READ=1");
//			default:
//				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//				onCreate(db);
			}


		}

		public ArrayList<Cursor> getData(String Query){
			//get writable database
			SQLiteDatabase sqlDB = this.getWritableDatabase();
			String[] columns = new String[] { "mesage" };
			//an array list of cursor to save two cursors one has results from the query
			//other cursor stores error message if any errors are triggered
			ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
			MatrixCursor Cursor2= new MatrixCursor(columns);
			alc.add(null);
			alc.add(null);


			try{
				String maxQuery = Query ;
				//execute the query results will be save in Cursor c
				Cursor c = sqlDB.rawQuery(maxQuery, null);


				//add value to cursor2
				Cursor2.addRow(new Object[] { "Success" });

				alc.set(1,Cursor2);
				if (null != c && c.getCount() > 0) {


					alc.set(0,c);
					c.moveToFirst();

					return alc ;
				}
				return alc;
			} catch(SQLException sqlEx){
				Log.d("printing exception", sqlEx.getMessage());
				//if any exceptions are triggered save the error message to cursor an return the arraylist
				Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
				alc.set(1,Cursor2);
				return alc;
			} catch(Exception ex){

				Log.d("printing exception", ex.getMessage());

				//if any exceptions are triggered save the error message to cursor an return the arraylist
				Cursor2.addRow(new Object[] { ""+ex.getMessage() });
				alc.set(1,Cursor2);
				return alc;
			}


		}
	}

	public static final class UserConstants implements BaseColumns {

		private UserConstants() {
		}

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.zamin.user";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.zamin.user";
		public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by auto-id

		public final static String USER_ID = "userId";
		public final static String USERNAME = "name";
		public final static String CHAT_ID = "chatId";
		public final static String MOBILE_NO = "mobileNo";
		public final static String EMAIL = "email";
		public final static String PROFILE_IAMGE = "profileImageBytes";
		public final static String TEMP_USER_ID = "tempUserId";
		public final static String IN_APP_ALERT = "inAppAlert";
		public final static String EMAIL_VERIFICATION_STATUS = "emailVerificationStatus";
		public final static String NEW_MESSAGE_ALERT = "newMessageAlert";
		public final static String ONLINE_STATUS = "onlineStatus";
		public final static String MY_STATUS = "myStatus";
		public final static String GENDER = "gender";
		public final static String CITY_OR_REGION = "region";
		public final static String COVER_PHOTO_BYTE = "cover_photo_byte";
		public final static String JABBERD_RESOURCE = "jabberd_resource";
		public final static String FIND_BY_PHONE_NO = "find_me_by_mobile_no";
		public final static String FIND_BY_SHAM_ID = "find_me_by_chat_id";
		public final static String PROFILE_IMAGE_URL = "profileimage_url";
		public final static String USER_TYPE = "user_type";
		public final static String IS_ADDED_TO_ROSTER = "is_added_to_roster";
		public final static String IS_VCARD_DOWNLOADED = "is_vcard_downloaded";
		//0 normal contact 1 in chat 2 in chat and contacts


		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();
			tmpList.add(USER_ID);
			return tmpList;
		}

	}

	public static final class GroupConstants implements BaseColumns {

		private GroupConstants() {
		}

		public static final String CONTENT_GROUP_TYPE = "vnd.android.cursor.dir/vnd.zamin.groups";
		public static final String CONTENT_GROUP_ITEM_TYPE = "vnd.android.cursor.item/vnd.zamin.groups";

		public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by auto-id

		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();;
			tmpList.add(FriendGroup.DB_RECORD_OWNER);
			return tmpList;
		}
	}

	public static final class GroupMemberConstants implements BaseColumns {

		private GroupMemberConstants() {
		}

		public static final String CONTENT_GROUP_MEMBER_TYPE = "vnd.android.cursor.dir/vnd.zamin.groupmember";
		public static final String CONTENT_GROUP_MEMBER_ITEM_TYPE = "vnd.android.cursor.item/vnd.zamin.groupmember";

		public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by auto-id

		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();;
			tmpList.add(FriendGroupMember.DB_GROUP);
			return tmpList;
		}
	}

	public static final class NotificationConstants implements BaseColumns {

		private NotificationConstants() {
		}

		public static final String CONTENT_NOTIFICATION_TYPE = "vnd.android.cursor.dir/vnd.zamin.notification";

		public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by auto-id

		public static ArrayList<String> getRequiredColumns() {
			ArrayList<String> tmpList = new ArrayList<String>();;
			tmpList.add(UserNotification.USER_ID);
			return tmpList;
		}
	}

	public Bitmap getProfileImageByUserId(String userId)
	{
		Bitmap bitmap=null;

		Cursor cursor=SHAMChatApplication.getMyApplicationContext().getContentResolver().query(UserProvider.CONTENT_URI_USER, new String [] {UserConstants.PROFILE_IAMGE}, UserConstants.USER_ID+"=?", new String [] {userId}, null);

		cursor.moveToFirst();

		try {

			File file = new File(
					Environment.getExternalStorageDirectory()
							+ PreferenceConstants.THUMBNAIL_DIRECTORY+"/"+userId+".jpg");

			if(file.exists())
			{
				bitmap=BitmapFactory.decodeFile(file.getAbsolutePath());
				//System.gc();
			}

		} catch (Exception e) {
			System.out.println("getProfileImageByUserId "+e);;
		} finally {
			cursor.close();
		}

		return bitmap;

	}

	public Bitmap getMyProfileImage()
	{
		Cursor cursor=SHAMChatApplication.getMyApplicationContext().getContentResolver().query(UserProvider.CONTENT_URI_USER, new String [] {UserConstants.PROFILE_IAMGE}, UserConstants.USER_ID+"=?", new String [] {SHAMChatApplication.getConfig().getUserId()}, null);

		cursor.moveToFirst();

		Bitmap bitmap=null;

		try {

			bitmap = Utils.base64ToBitmap(cursor.getString(cursor.getColumnIndex(UserConstants.PROFILE_IAMGE)));

			if(bitmap==null)
			{
				bitmap=getProfileImageByUserId(SHAMChatApplication.getConfig().getUserId());
			}

		} catch (Exception e) {
			cursor.close();
			return bitmap;
		}

		cursor.close();

		return bitmap;

	}

	public static User userFromCursor(Cursor cursor) {
		//cursor.moveToFirst();
		User user = null;
		if (cursor != null && cursor.getCount() > 0) {
			user = new User();
			user.setUserId(cursor.getString(cursor.getColumnIndex(UserConstants.USER_ID)));
			user.setUsername(cursor.getString(cursor
					.getColumnIndex(UserConstants.USERNAME)));
			user.setChatId(cursor.getString(cursor.getColumnIndex(UserConstants.CHAT_ID)));
			user.setMobileNo(cursor.getString(cursor
					.getColumnIndex(UserConstants.MOBILE_NO)));
			user.setEmail(cursor.getString(cursor.getColumnIndex(UserConstants.EMAIL)));
			user.setGender(cursor.getString(cursor.getColumnIndex(UserConstants.GENDER)));
			user.setEmailVerificationStatus(cursor.getString(cursor
					.getColumnIndex(UserConstants.EMAIL_VERIFICATION_STATUS)));
			user.setCityOrRegion(cursor.getString(cursor
					.getColumnIndex(UserConstants.CITY_OR_REGION)));
			user.setCoverPhoto(cursor.getString(cursor
					.getColumnIndex(UserConstants.COVER_PHOTO_BYTE)));
			user.setprofileImageUrl(cursor.getString(cursor
					.getColumnIndex(UserConstants.PROFILE_IMAGE_URL)));

			user.setDbRowId(String.valueOf(cursor.getInt(cursor.getColumnIndex(UserConstants._ID))));

		}
		return user;
	}

	public static List<User> usersFromCursor(Cursor cursor) {
		List<User> list = new ArrayList<User>();

		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				User user = userFromCursor(cursor);
				list.add(user);
			}
		}

		return list;
	}

	public static ArrayList<User> usersFromCursorArray(Cursor cursor){

		ArrayList<User> array    = new ArrayList<User>();

		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				User user = userFromCursor(cursor);
				array.add(user);
			}
		}

		return array;
	}


	public Cursor getUsersInGroup(String groupId, boolean negative) {

		UserDatabaseHelper databaseHelper=new UserDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase database = databaseHelper.getWritableDatabase();

		Cursor query=database.query(true, UserProvider.TABLE_NAME_USER, null, UserConstants.USER_ID + (negative ? " NOT " : " ") + "IN (SELECT "
				+ FriendGroupMember.DB_FRIEND + " FROM "
				+ TABLE_NAME_FRIEND_GROUP_MEMBER + " WHERE "
				+ FriendGroupMember.DB_GROUP + "=?)", new String[] { groupId }, UserConstants.USERNAME, null, UserConstants.USERNAME + " ASC", null);

		return query;
	}


	public static FriendGroup groupFromCursor(Cursor cursor) {
		FriendGroup group = new FriendGroup();
		cursor.moveToFirst();
		group.setId(cursor.getString(cursor.getColumnIndex(FriendGroup.DB_ID)));
		group.setName(cursor.getString(cursor
				.getColumnIndex(FriendGroup.DB_NAME)));
		group.setRecordOwnerId(cursor.getString(cursor
				.getColumnIndex(FriendGroup.DB_RECORD_OWNER)));
		group.setChatRoomName(cursor.getString(cursor
				.getColumnIndex(FriendGroup.CHAT_ROOM_NAME)));
		group.setDbRecordId(String.valueOf(cursor.getInt(cursor.getColumnIndex(GroupConstants._ID))));

		cursor.close();

		return group;
	}

	public void updateUser(User user) {

		ContentValues values = new ContentValues();

		values.put(UserConstants.USERNAME, user.getUsername());
		values.put(UserConstants.CHAT_ID, user.getChatId());
		values.put(UserConstants.MOBILE_NO, user.getMobileNo());
		values.put(UserConstants.EMAIL, user.getEmail());
		values.put(UserConstants.GENDER, user.getGender());
		values.put(UserConstants.PROFILE_IAMGE, user.getProfileImage());
		values.put(UserConstants.MY_STATUS, user.getMyStatus());
		values.put(UserConstants.NEW_MESSAGE_ALERT, user.getNewMessageAlert());
		values.put(UserConstants.IN_APP_ALERT, user.getInAppAlert());
		values.put(UserConstants.EMAIL_VERIFICATION_STATUS,
				user.getEmailVerificationStatus());
		values.put(UserConstants.TEMP_USER_ID, user.getTmpUserId());
		values.put(UserConstants.CITY_OR_REGION, user.getCityOrRegion());
		values.put(UserConstants.JABBERD_RESOURCE, user.getJabberdResource());
		values.put(UserConstants.PROFILE_IMAGE_URL, user.getprofileImageUrl());
		if (user.getFindMeByPhoneNoStatus() != null
				&& user.getFindMeByShamIdStatus() != null) {
			values.put(UserConstants.FIND_BY_PHONE_NO, user.getFindMeByPhoneNoStatus()
					.getStatus());
			values.put(UserConstants.FIND_BY_SHAM_ID, user.getFindMeByShamIdStatus()
					.getStatus());
		}

		SHAMChatApplication.getMyApplicationContext().getContentResolver().update(Uri.parse(CONTENT_URI_USER.toString()+"/"+user.getUserId()), values, null, null);

	}

	public User getCurrentUser() {

		Cursor cursor=SHAMChatApplication.getMyApplicationContext().getContentResolver().query(Uri.parse(CONTENT_URI_USER.toString()+"/"+SHAMChatApplication.getConfig().getUserId()), null, null, null,null);
		cursor.moveToFirst();

		User user = null;
		if (cursor != null && cursor.getCount() > 0) {
			user = new User();
			user.setUserId(cursor.getString(cursor.getColumnIndex(UserConstants.USER_ID)));
			user.setUsername(cursor.getString(cursor
					.getColumnIndex(UserConstants.USERNAME)));
			user.setChatId(cursor.getString(cursor.getColumnIndex(UserConstants.CHAT_ID)));
			user.setMobileNo(cursor.getString(cursor
					.getColumnIndex(UserConstants.MOBILE_NO)));
			user.setEmail(cursor.getString(cursor.getColumnIndex(UserConstants.EMAIL)));
			user.setGender(cursor.getString(cursor.getColumnIndex(UserConstants.GENDER)));
			user.setProfileImage(cursor.getString(cursor
					.getColumnIndex(UserConstants.PROFILE_IAMGE)));
			user.setMyStatus(cursor.getString(cursor
					.getColumnIndex(UserConstants.MY_STATUS)));
			user.setNewMessageAlert(cursor.getString(cursor
					.getColumnIndex(UserConstants.NEW_MESSAGE_ALERT)));
			user.setInAppAlert(cursor.getString(cursor
					.getColumnIndex(UserConstants.IN_APP_ALERT)));
			user.setEmailVerificationStatus(cursor.getString(cursor
					.getColumnIndex(UserConstants.EMAIL_VERIFICATION_STATUS)));
			user.setTmpUserId(cursor.getString(cursor
					.getColumnIndex(UserConstants.TEMP_USER_ID)));
			user.setCityOrRegion(cursor.getString(cursor
					.getColumnIndex(UserConstants.CITY_OR_REGION)));
			user.setCoverPhoto(cursor.getString(cursor
					.getColumnIndex(UserConstants.COVER_PHOTO_BYTE)));
			user.setJabberdResource(cursor.getString(cursor
					.getColumnIndex(UserConstants.JABBERD_RESOURCE)));
			user.setprofileImageUrl(cursor.getString(cursor
					.getColumnIndex(UserConstants.PROFILE_IMAGE_URL)));

			BooleanStatus bool = BooleanStatus.FALSE;
			switch (cursor.getInt(cursor.getColumnIndex(UserConstants.FIND_BY_PHONE_NO))) {
				case 0:
					bool = BooleanStatus.FALSE;
					break;
				case 1:
					bool = BooleanStatus.TRUE;
					break;
			}

			user.setFindMeByPhoneNoStatus(bool);

			BooleanStatus bool2 = BooleanStatus.FALSE;
			switch (cursor.getInt(cursor.getColumnIndex(UserConstants.FIND_BY_SHAM_ID))) {
				case 0:
					bool2 = BooleanStatus.FALSE;
					break;
				case 1:
					bool2 = BooleanStatus.TRUE;
					break;
			}

			user.setFindMeByShamIdStatus(bool2);

		}
		return user;
	}

	*/
/**
	 * getCurrentUserForMyProfile gets the user object of the current user, just a db call
	 * @return
	 *//*

	public User getCurrentUserForMyProfile() {

		Cursor cursor=SHAMChatApplication.getMyApplicationContext().getContentResolver().query(Uri.parse(CONTENT_URI_USER.toString()+"/"+SHAMChatApplication.getConfig().getUserId()), null, null, null,null);
		cursor.moveToFirst();

		User user = null;
		if (cursor != null && cursor.getCount() > 0) {
			user = new User();
			user.setUserId(cursor.getString(cursor.getColumnIndex(UserConstants.USER_ID)));
			user.setUsername(cursor.getString(cursor
					.getColumnIndex(UserConstants.USERNAME)));
			user.setChatId(cursor.getString(cursor.getColumnIndex(UserConstants.CHAT_ID)));
			user.setMobileNo(cursor.getString(cursor
					.getColumnIndex(UserConstants.MOBILE_NO)));
			user.setGender(cursor.getString(cursor.getColumnIndex(UserConstants.GENDER)));
			user.setProfileImage(cursor.getString(cursor
					.getColumnIndex(UserConstants.PROFILE_IAMGE)));
			user.setMyStatus(cursor.getString(cursor
					.getColumnIndex(UserConstants.MY_STATUS)));
			user.setCityOrRegion(cursor.getString(cursor
					.getColumnIndex(UserConstants.CITY_OR_REGION)));
			user.setprofileImageUrl(cursor.getString(cursor
					.getColumnIndex(UserConstants.PROFILE_IMAGE_URL)));

		}

		cursor.close();
		return user;
	}

	public static enum UserNotificationUpdateType {
		NOTIFICATION_SOUND, NOTIFICATION_TIMING_START, NOTIFICATION_TIMING_END, MESSAGE_ALERT_STATUS, MOVEMENT_UPDATE_STATUS, OTHER_FEATURE_ALERT_STATUS, SOUND_ALERT_STATUS, VIBRATE_STATUS
	}

	public void updateNotification(String userId,
								   UserNotificationUpdateType updateType, Object value) {

		ContentValues values = new ContentValues();
		switch (updateType) {
			case NOTIFICATION_SOUND:
				values.put(UserNotification.NOTIFICATION_SOUND, (String) value);
				break;
			case NOTIFICATION_TIMING_START:
				values.put(UserNotification.NOTIFICATION_TIMING_START,
						(String) value);
				break;
			case NOTIFICATION_TIMING_END:
				values.put(UserNotification.NOTIFICATION_TIMING_END, (String) value);
				break;
			case MESSAGE_ALERT_STATUS:
				values.put(UserNotification.MESSAGE_ALERT_STATUS,
						((AllowDeniedStatus) value).getStatus());
				break;
			case MOVEMENT_UPDATE_STATUS:
				values.put(UserNotification.MOVEMENT_UPDATE_STATUS,
						((EnableDisableStatus) value).getStatus());
				break;
			case OTHER_FEATURE_ALERT_STATUS:
				values.put(UserNotification.OTHER_FEATURE_ALERT_STATUS,
						((FeatureAlertStatus) value).getStatus());
				break;
			case SOUND_ALERT_STATUS:
				values.put(UserNotification.SOUND_ALERT_STATUS,
						((EnableDisableStatus) value).getStatus());
				break;
			case VIBRATE_STATUS:
				values.put(UserNotification.VIBRATE_STATUS,
						((EnableDisableStatus) value).getStatus());
				break;
		}

		SHAMChatApplication.getMyApplicationContext().getContentResolver().update(CONTENT_URI_NOTIFICATION, values, UserNotification.USER_ID+"=?", new String [] {userId});
	}

	public static UserNotification userNotificationFromCursor(Cursor cursor) {
		UserNotification userNotification = new UserNotification();

		try {

			userNotification.setUserNotificationId(cursor.getInt(cursor.getColumnIndex(NotificationConstants._ID)));
			userNotification.setUserId(cursor.getString(cursor.getColumnIndex(UserNotification.USER_ID)));
			userNotification
					.setMessageAlertStatus((cursor.getInt(cursor.getColumnIndex(UserNotification.MESSAGE_ALERT_STATUS)) == 1 ? AllowDeniedStatus.ALLOW
							: AllowDeniedStatus.DENIED));
			userNotification
					.setSoundAlertStatus((cursor.getInt(cursor.getColumnIndex(UserNotification.SOUND_ALERT_STATUS)) == 1 ? EnableDisableStatus.ENABLE
							: EnableDisableStatus.DISABLE));
			userNotification
					.setVibrateStatus(cursor.getInt(cursor.getColumnIndex(UserNotification.VIBRATE_STATUS)) == 1 ? EnableDisableStatus.ENABLE
							: EnableDisableStatus.DISABLE);
			userNotification.setNotificationSound(cursor.getString(cursor.getColumnIndex(UserNotification.NOTIFICATION_SOUND)));
			userNotification.setNotificationTimingStart(cursor.getString(cursor.getColumnIndex(UserNotification.NOTIFICATION_TIMING_START)));
			userNotification.setNotificationTimingEnd(cursor.getString(cursor.getColumnIndex(UserNotification.NOTIFICATION_TIMING_END)));
			FeatureAlertStatus fStatus = FeatureAlertStatus.DISABLE;

			switch (cursor.getInt(8)) {
				case 1:
					fStatus = FeatureAlertStatus.ENABLE;
					break;
				case 2:
					fStatus = FeatureAlertStatus.NIGHT_MODE;
					break;
				default:
					fStatus = FeatureAlertStatus.DISABLE;
			}
			userNotification.setOtherFeatureAlertStatus(fStatus);
			userNotification
					.setMovementUpdateStatus(cursor.getInt(cursor.getColumnIndex(UserNotification.MOVEMENT_UPDATE_STATUS)) == 1 ? EnableDisableStatus.ENABLE
							: EnableDisableStatus.DISABLE);

		} catch (Exception e) {

			if(SHAMChatApplication.getConfig().getUserId()!=null)
			{
				ContentValues vals = new ContentValues();
				vals.put(UserNotification.USER_ID, SHAMChatApplication.getConfig().getUserId());
				SHAMChatApplication.getInstance().getContentResolver()
						.insert(UserProvider.CONTENT_URI_NOTIFICATION,
								vals);
			}

			userNotificationFromCursor(cursor);

		}

		return userNotification;
	}

	public Cursor getGroupMemberCursor(String groupId) {

		UserDatabaseHelper databaseHelper=new UserDatabaseHelper(SHAMChatApplication.getMyApplicationContext());
		SQLiteDatabase database = databaseHelper.getWritableDatabase();

		Cursor cursor=database.query(true, TABLE_NAME_FRIEND_GROUP_MEMBER, null, FriendGroupMember.DB_GROUP+"=?", new String [] {groupId}, FriendGroupMember.DB_FRIEND, null, FriendGroupMember.DB_FRIEND, null);

		return cursor;
	}

	public User getUserDetailsFromServer(String userId)
	{
		User user =null;

		try {

			BasicHttpParams params = new BasicHttpParams();

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			final SSLSocketFactory sslSocketFactory = SSLSocketFactory
					.getSocketFactory();
			schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
			ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
					schemeRegistry);

			DefaultHttpClient httpclient = new DefaultHttpClient(cm, params);

			Context context = SHAMChatApplication
					.getMyApplicationContext();

			HttpPost httpPost = new HttpPost(context
					.getApplicationContext().getResources()
					.getString(R.string.homeBaseURL)
					+ "getMyDetails.htm");

			List<NameValuePair> data = new ArrayList<NameValuePair>();

			data.add(new BasicNameValuePair("userId", userId));

			httpPost.setEntity(new UrlEncodedFormEntity(data));

			HttpResponse httpResponse = httpclient.execute(httpPost);

			InputStream inputStream = httpResponse.getEntity().getContent();

			BufferedReader bufferreader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder responseStr = new StringBuilder();
			String responseLineStr = null;

			while ((responseLineStr = bufferreader.readLine()) != null) {
				responseStr.append(responseLineStr);

			}

			bufferreader.close();
			inputStream.close();

			String result = responseStr.toString().trim();


			if (result != null) {

				JSONObject serverResponse = new JSONObject(result);
				String status = serverResponse.getString("status");

				if(status.equalsIgnoreCase("success"))
				{
					JSONObject userJsonObject = serverResponse.getJSONObject("data");

					user = new User(userJsonObject);

					ContentValues values = new ContentValues();
					values.put(UserConstants.USERNAME, user.getUsername());
					values.put(UserConstants.CHAT_ID, user.getChatId());
					values.put(UserConstants.USER_ID, user.getUserId());
					values.put(UserConstants.MOBILE_NO, user.getMobileNo());
					values.put(UserConstants.EMAIL, user.getEmail());
					values.put(UserConstants.GENDER, user.getGender());
					values.put(UserConstants.PROFILE_IAMGE, Utils.decodeImage(user.getProfileImage()));
					values.put(UserConstants.MY_STATUS, user.getMyStatus());
					values.put(UserConstants.CITY_OR_REGION, user.getCityOrRegion());
					values.put(UserConstants.PROFILE_IMAGE_URL, user.getprofileImageUrl());

					if(context.getContentResolver().update(UserProvider.CONTENT_URI_USER, values, UserConstants.USER_ID+"=?", new String [] {user.getUserId()})==0)
					{
						context.getContentResolver().insert(UserProvider.CONTENT_URI_USER, values);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return user;
	}


}
*/
