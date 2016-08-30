package org.telegram.messenger.shamChat;

import android.content.Context;

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

public class SubscribeToEventsJob extends Job {

	private static final long serialVersionUID = 1L;

	public static final int PRIORITY = 1000;

	private static final AtomicInteger jobCounter = new AtomicInteger(0);
	private final int id;


	String[] topics;
	boolean isError = false;
	
	public SubscribeToEventsJob() {
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
			
		MqttAndroidClient mqttClient;
		//reza_ak
		//RokhPref Session = new RokhPref(ApplicationLoader.getInstance().getApplicationContext());
		final String clientHandle = "user102015";
		//reza_ak
		String userId	= "102015";
		//phoneNumber = phoneNumber.startsWith("+") ? phoneNumber.substring(1) : phoneNumber;
		String topic = "events/"+userId;
		final Context context = ApplicationLoader.getInstance().getApplicationContext();
		
		topics = new String[1];
	    topics[0] = topic;
	     
		mqttClient = Connections.getInstance(ApplicationLoader.getInstance().getApplicationContext()).getConnection(clientHandle).getClient();
		mqttClient.subscribe(topic, 1, null,   new IMqttActionListener() {
			
			@Override
			public void onSuccess(IMqttToken arg0) {
				// TODO Auto-generated method stub
			    Connection c = Connections.getInstance(context).getConnection(clientHandle);
			    String actionTaken = context.getString(R.string.toast_sub_success,
			        (Object[]) topics);
			    c.addAction(actionTaken);
			    
			    //Notify.toast(context, actionTaken, Toast.LENGTH_SHORT);
			    //Notify.toast(context, "success", Toast.LENGTH_SHORT);
			}
			
			@Override
			public void onFailure(IMqttToken arg0, Throwable arg1) {
				
			    Connection c = Connections.getInstance(context).getConnection(clientHandle);
			    String action = context.getString(R.string.toast_sub_failed,
			        (Object[]) topics);
			    c.addAction(action);
			    
			    //Notify.toast(context, action, Toast.LENGTH_SHORT);
			    //Notify.toast(context, "fail", Toast.LENGTH_SHORT);
			    isError = true;
			}	
	
		});
		
		if (isError) throw new IOException("Unexpected code");
		
		
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		// An error occurred in onRun.
		// Return value determines whether this job should retry running (true)
		// or abort (false).
		System.out.println("********subscribe to events topic failed - retry******");
	
		return true;
	}


}
