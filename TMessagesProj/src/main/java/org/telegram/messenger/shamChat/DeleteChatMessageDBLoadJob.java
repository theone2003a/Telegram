package org.telegram.messenger.shamChat;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;


import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

public class DeleteChatMessageDBLoadJob extends Job {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final AtomicInteger jobCounter = new AtomicInteger(0);
	private final int id;

	public static final int PRIORITY = 100;

	private String packetId;
	private String actorId;
	private String ChannelId;

	public DeleteChatMessageDBLoadJob(String packetId) {

		super(new Params(PRIORITY));
		id = jobCounter.incrementAndGet();
		this.packetId = packetId;

	}

	public DeleteChatMessageDBLoadJob(String packetId,String actorId,String ChannelId) {

		super(new Params(PRIORITY).persist().requireNetwork());
		id = jobCounter.incrementAndGet();
		this.packetId = packetId;
		this.actorId = actorId;
		this.ChannelId = ChannelId;

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
		if (id != jobCounter.get()) {
			// looks like other fetch jobs has been added after me. no reason to
			// keep fetching
			// many times, cancel me, let the other one fetch tweets.
			return;
		}
//reza_ak
	/*	try {

			SHAMChatApplication
					.getMyApplicationContext()
					.getContentResolver()
					.delete(ChatProviderNew.CONTENT_URI_CHAT,
							ChatMessage.PACKET_ID + "=?",
							new String[] { packetId });

			EventBus.getDefault().post(new MessageDeletedEvent(packetId));



if(ChannelId.startsWith("ch")) {

	final String URL = Constant.MqttChannelDeletePost + ChannelId + "/";
	//mast - send request to server to leave the room
	OkHttpClient client = new OkHttpClient();

	client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
	client.setReadTimeout(60, TimeUnit.SECONDS);    // socket timeout

	RequestBody formBody = new FormEncodingBuilder()
			.add("packet_id" ,packetId )
			.add("actor_id", actorId)
			.build();

	Request request = new Request.Builder()
			.url(URL)
		    .post(formBody)
			.build();

	Response response = client.newCall(request).execute();
	// check if request was successful
	if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);


	*//*

	final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	JSONObject jsContent = new JSONObject();
	jsContent.put("post_id", new JSONArray().put(packetId));
	jsContent.put("actor_id", new JSONArray().put(actorId));


	RequestBody formBody = RequestBody.create(JSON, jsContent.toString());


	Request request = new Request.Builder()
			.url(URL)
			.post(formBody)
			.build();*//*






			EventBus.getDefault().post(new MessageDeletedEvent(packetId));
}

		} catch (Exception e) {

			System.out.println("DisableStickerPackDBLoadJob " + e);
		}*/

	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		// An error occurred in onRun.
		// Return value determines whether this job should retry running (true)
		// or abort (false).

		return false;
	}



}
