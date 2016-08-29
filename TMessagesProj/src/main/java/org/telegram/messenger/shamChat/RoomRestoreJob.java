package org.telegram.messenger.shamChat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

public class RoomRestoreJob extends Job {

    private static final long serialVersionUID = 1L;

    public static final int PRIORITY = 1000;

    private static final AtomicInteger jobCounter = new AtomicInteger(0);
    private final int id;
//reza_ak
    final String  userId = "6000000";

    String[] allTopics;
    int [] qosArray;


    MqttAndroidClient mqttClient;

    public RoomRestoreJob() {
        super(new Params(PRIORITY).persist().requireNetwork());

        id = jobCounter.incrementAndGet();
    }

    @Override
    public void onAdded() {
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job will
        // eventually run.
        // In this example, it would be good to update the UI with the newly
        // posted tweet.
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
//reza_ak
   //     User userObject 		= new UserProvider().getCurrentUserForMyProfile();

//reza_ak
        //RokhPref Session = new RokhPref(getApplicationContext());

     //   final String clientHandle				= Session.getClientHandle();


        final String	URL						= Constant.MqttTopicsList+userId+"/";

//reza_ak
/*
        //mast - send request to server to restore the rooms
        OkHttpClient client = new OkHttpClient();

        client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(60, TimeUnit.SECONDS);    // socket timeout

        Request request = new Request.Builder()
                .url(URL)
                .build();

        Response response = client.newCall(request).execute();
        // check if request was successful
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        String stringResponse = response.body().string();
        response.body().close();

        System.out.println(stringResponse);
        JSONObject jsonResponse = new JSONObject(stringResponse);
        String status = jsonResponse.getString("status");

        // check if API processed the room restore request successfully
        if(status.equals("200")){

            //Parsing JSONObject
            JSONArray	groupsJSONArray	= jsonResponse.getJSONArray("objects");
            JSONArray	channelsJSONArray	= jsonResponse.getJSONArray("channels");
            JSONArray	allTopicsArray	= concatJSONArray(groupsJSONArray, channelsJSONArray);

            if(allTopicsArray.length() > 0){

                addTopicsToDB(allTopicsArray);

                final ActionListener callback = new ActionListener(SHAMChatApplication.getInstance().getApplicationContext(), ActionListener.Action.SUBSCRIBE, clientHandle, allTopics);

                mqttClient = Connections.getInstance(
                        SHAMChatApplication.getInstance().getApplicationContext()).getConnection(clientHandle).getClient();
                mqttClient.subscribe(allTopics, qosArray, null, callback);



            }

        } else {
            throw new IOException("Unexpected reponse code " + status);
        }*/


    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {

        // An error occurred in onRun.
        // Return value determines whether this job should retry running (true)
        // or abort (false).
        System.out.println("Room Restore Job run again");

        return true;
    }

    private JSONArray concatJSONArray(JSONArray... arrs)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }

    /**
     * Add a JSON array of topics (groups/channels) to database
     * @param allTopicsArray
     * @return
     */
    protected boolean addTopicsToDB(JSONArray allTopicsArray) {
//reza_ak
     //   final ContentResolver mContentResolver	= SHAMChatApplication.getInstance().getApplicationContext().getContentResolver();

        if(allTopicsArray.length() > 0){

            allTopics = new String[allTopicsArray.length()];
            qosArray  = new int[allTopicsArray.length()];


            try {

                for (int i = 0; i < allTopicsArray.length(); i++) {

                    JSONObject allTopicsList = allTopicsArray.getJSONObject(i);
                    String groupAlias = allTopicsList.getString("title");
                    String subscibtionpath = allTopicsList.getString("sub_path");
                    String groupMemberCnt = String.valueOf(allTopicsList.getInt("cnt_members"));
                    String groupHashCode = allTopicsList.getString("hashcode");
                    allTopics[i] = allTopicsList.getString("hashcode");
                    qosArray[i] = 1;
                    JSONObject groupAdmin = allTopicsList.getJSONObject("admin");
                    String groupAdminID = groupAdmin.getString("user_id");
                    String groupAdminPhone = groupAdmin.getString("phone");

                    String description="";
                    String linkName="";

                    Boolean isChannel = false;
                    if (groupHashCode.startsWith("ch")) {
                        isChannel = true;
                    }

                    if (isChannel) {
                        description = allTopicsList.getString("description");
                        linkName =  allTopicsList.getString("name");
                    }
//reza_ak
                 /*  Cursor groupCursor = mContentResolver.query(UserProvider.CONTENT_URI_GROUP, new String[]{FriendGroup.DB_ID}, FriendGroup.CHAT_ROOM_NAME + "=?", new String[]{groupHashCode}, null);
                    boolean isUpdate = false;

                    if (groupCursor.getCount() > 0) {
                        isUpdate = true;
                    }
                    groupCursor.close();
*/
                    final FriendGroup group = new FriendGroup();
                    group.setId(groupHashCode);
                    group.setName(groupAlias);
                    group.setRecordOwnerId(groupAdminID);
                    group.setChatRoomName(groupHashCode);

                    if (isChannel) {
                        group.setGroupDescription(description);
                        group.setGroupLinkName(linkName);
                    }

                    ContentValues values = new ContentValues();
                    values.put(FriendGroup.DB_ID, groupHashCode);
                    values.put(FriendGroup.DB_NAME, groupAlias);
                    values.put(FriendGroup.DB_RECORD_OWNER, groupAdminID);
                    values.put(FriendGroup.CHAT_ROOM_NAME, groupHashCode);

                    if (allTopicsList.has("logo")) {
                        if (!allTopicsList.getString("logo").equals("")) {
                            values.put(FriendGroup.CHANNEL_LOGO,   allTopicsList.getString("logo"));


                        }
                    }

                    MessageThread thread = new MessageThread();
                    thread.setFriendId(groupHashCode);
                    thread.setGroupChat(true);
                    thread.setThreadOwner(userId);

                    ContentValues vals = new ContentValues();
                    vals.put(MessageThread.THREAD_ID, thread.getThreadId());
                    vals.put(MessageThread.FRIEND_ID, thread.getFriendId());
                    vals.put(MessageThread.READ_STATUS, 0);
                    vals.put(MessageThread.LAST_UPDATED_DATETIME, Utils.formatDate(new Date().getTime(), "yyyy/MM/dd HH:mm:ss"));
                    vals.put(MessageThread.IS_GROUP_CHAT, 1);
                    vals.put(MessageThread.THREAD_OWNER, thread.getThreadOwner());

                    //reza_ak
                   /* if (!isUpdate) {

                        mContentResolver.insert(UserProvider.CONTENT_URI_GROUP, values);

                        if (groupHashCode.startsWith("ch"))
                        {vals.put(MessageThread.LAST_MESSAGE, SHAMChatApplication.getInstance().getString(R.string.new_channel_invited));}
                        else if (groupHashCode.startsWith("g"))
                        { vals.put(MessageThread.LAST_MESSAGE, SHAMChatApplication.getInstance().getString(R.string.new_group_invited));}
                        else
                        {vals.put(MessageThread.LAST_MESSAGE, "");}

                        vals.put(MessageThread.LAST_MESSAGE_DIRECTION, MyMessageType.INCOMING_MSG.ordinal());
                        vals.put(MessageThread.LAST_MESSAGE_CONTENT_TYPE, 0);
                        mContentResolver.insert(ChatProviderNew.CONTENT_URI_THREAD, vals);

                    } else {

                        mContentResolver.update(UserProvider.CONTENT_URI_GROUP, values, FriendGroup.DB_ID + "=?", new String[]{group.getId()});
                        mContentResolver.update(ChatProviderNew.CONTENT_URI_THREAD, vals, MessageThread.THREAD_ID + "=?", new String[]{thread.getThreadId()});
                    }*/

                    // add admin as member
                    FriendGroupMember admin = new FriendGroupMember(group.getId(), groupAdminID);
                    admin.assignUniqueId(userId);

                    ContentValues adminCv = new ContentValues();
                    adminCv.put(FriendGroupMember.DB_ID, admin.getId());
                    adminCv.put(FriendGroupMember.DB_FRIEND, admin.getFriendId());
                    adminCv.put(FriendGroupMember.DB_GROUP, admin.getGroupID());
                    adminCv.put(FriendGroupMember.DB_FRIEND_IS_ADMIN, 1);
                    adminCv.put(FriendGroupMember.PHONE_NUMBER, admin.getPhoneNumber());
//reza_ak
                 /*   if (mContentResolver.update(UserProvider.CONTENT_URI_GROUP_MEMBER, adminCv, FriendGroupMember.DB_GROUP + "=? AND " + FriendGroupMember.DB_FRIEND + "=?", new String[]{admin.getGroupID(), groupAdminID}) == 0) {
                        mContentResolver.insert(UserProvider.CONTENT_URI_GROUP_MEMBER, adminCv);
                    }*/

                    //we add users just for groups
                    if (!isChannel) {
                         try {


                        JSONArray users = allTopicsList.getJSONArray("users");

                        if (users.length() > 0) {


                            for (int j = 0; j < users.length(); j++) {

                                JSONObject member = users.getJSONObject(j);

                                String memberId = member.getString("user_id");
                                String memberPhone = member.getString("phone");
                                boolean memberisAdmin = member.getBoolean("is_admin");

                                //add members
                                FriendGroupMember members = new FriendGroupMember(group.getId(), memberId);

                                members.assignUniqueId(userId);

                                ContentValues groupMembers = new ContentValues();
                                groupMembers.put(FriendGroupMember.DB_ID, members.getId());
                                groupMembers.put(FriendGroupMember.DB_FRIEND, members.getFriendId());
                                groupMembers.put(FriendGroupMember.DB_GROUP, members.getGroupID());
                                groupMembers.put(FriendGroupMember.PHONE_NUMBER, memberPhone);

                                if (memberisAdmin) {
                                    groupMembers.put(FriendGroupMember.DB_FRIEND_IS_ADMIN, 1);
                                }

                                groupMembers.put(FriendGroupMember.DB_FRIEND_DID_JOIN, 1);
//reza_ak
                             /*   if (mContentResolver.update(UserProvider.CONTENT_URI_GROUP_MEMBER, groupMembers, FriendGroupMember.DB_GROUP + "=? AND " + FriendGroupMember.DB_FRIEND + "=?", new String[]{members.getGroupID(), memberId}) == 0) {
                                    mContentResolver.insert(UserProvider.CONTENT_URI_GROUP_MEMBER, groupMembers);
                                }*/

                            }

                        }
                    }catch (Exception e)
                         {}


                    }


                    //send a new message event so the ChatThreadFragment updates itself
                    NewMessageEvent newMessageEvent = new NewMessageEvent();
                    newMessageEvent.setThreadId(thread.getThreadId());

                    newMessageEvent.setDirection(MyMessageType.INCOMING_MSG.ordinal());
                    EventBus.getDefault().postSticky(newMessageEvent);

                }
            } catch (Exception e) {
                return false;
            }

        }
        return true;
    }

}
