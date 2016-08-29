package org.telegram.messenger.shamChat;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.mqtt.Connection;
import org.telegram.messenger.mqtt.Connections;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class SubscribeToAllTopicsJob extends Job {

	private static final long serialVersionUID = 1L;

	public static final int PRIORITY = 1000;

	private static final AtomicInteger jobCounter = new AtomicInteger(0);
	private final int id;
	private boolean isError = false;
	String[] allTopics;
	
	public SubscribeToAllTopicsJob() {
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

		if (id != jobCounter.get()) {
			// looks like other fetch jobs has been added after me. no reason to
			// keep fetching
			// many times, cancel me, let the other one fetch tweets.
			return;
		}
		/*reza_ak

		MqttAndroidClient mqttClient;		
		ContentResolver mContentResolver = ApplicationLoader.applicationContext.getContentResolver();
			
		RokhPref Session = new RokhPref(ApplicationLoader.applicationContext.getApplicationContext());
		final String clientHandle = Session.getClientHandle();
		
		final Context context = ApplicationLoader.applicationContext.getApplicationContext();
		//reza_ak
		Cursor cursor		= mContentResolver.query( UserProvider.CONTENT_URI_GROUP, null, null, null, null);
		int	groupCount		= cursor.getCount();

		
		allTopics	= new String[groupCount];
		isError = false;
		int [] qosArray = new int[groupCount];
		int i	= 0;
		if (groupCount >0)
		 {
			while (cursor.moveToNext()) {
				String hashcode	= cursor.getString(cursor.getColumnIndex(FriendGroup.CHAT_ROOM_NAME));
				String groupAlias	= cursor.getString(cursor.getColumnIndex(FriendGroup.DB_NAME));

				if (hashcode==null) hashcode = "nothing";
                    //if this is a group
					if (hashcode.startsWith("ch")) {
						Log.e("Subscribe", "channels/" + hashcode + "  channelAlias:" + groupAlias);
						allTopics[i] = "channels/" + hashcode;
						qosArray[i] = 1;
                    //if this is a channel
					} else {

						Log.e("Subscribe", "groups/" + hashcode + "  groupAlias:" + groupAlias);
						allTopics[i] = "groups/" + hashcode;
						qosArray[i] = 1;
					}
					i++;
	
			}
			cursor.close();
			
			//final ActionListener callback = new ActionListener(context, ActionListener.Action.SUBSCRIBE, clientHandle, allTopics);

			
			mqttClient = Connections.getInstance(ApplicationLoader.applicationContext.getApplicationContext()).getConnection(clientHandle).getClient();
			
		    
			mqttClient.subscribe(allTopics, qosArray, null,  new IMqttActionListener() {
				
				@Override
				public void onSuccess(IMqttToken arg0) {
					// TODO Auto-generated method stub
				    Connection c = Connections.getInstance(context).getConnection(clientHandle);
				    String actionTaken = context.getString(R.string.toast_sub_success,
				        (Object[]) allTopics);
				    c.addAction(actionTaken);
				    
				    //Notify.toast(context, actionTaken, Toast.LENGTH_SHORT);
				    //Notify.toast(context, "successG", Toast.LENGTH_SHORT);
				}
				
				@Override
				public void onFailure(IMqttToken arg0, Throwable arg1) {
					
				    Connection c = Connections.getInstance(context).getConnection(clientHandle);
				    String action = context.getString(R.string.toast_sub_failed,
				        (Object[]) allTopics);
				    c.addAction(action);
				    
				    //Notify.toast(context, action, Toast.LENGTH_SHORT);
				    //Notify.toast(context, "failG", Toast.LENGTH_SHORT);
				    isError = true;
				}	
		
			});

			if (isError) throw new IOException("Unexpected code");
		 }*/
	}


	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		// An error occurred in onRun.
		// Return value determines whether this job should retry running (true)
		// or abort (false).
		Log.e("Subscribe", "to all topics failed: "+ throwable.getCause());	
		System.out.println("subscribe to events topic failed - retry");

		return true;
	}


}
