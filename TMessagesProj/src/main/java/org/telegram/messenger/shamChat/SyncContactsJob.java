/*
package org.telegram.messenger.shamChat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.Toast;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

public class SyncContactsJob extends Job {

	private static final long serialVersionUID = 1L;

	public static final int PRIORITY = 9000;

	private static final AtomicInteger jobCounter = new AtomicInteger(0);
	private final int id;

	public SyncContactsJob(long delay) {
		super(new Params(PRIORITY).delayInMs(delay).persist().requireNetwork());
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
		
		showToast("خطایی در بروزرسانی مخاطبین رخ داد لطفا مجدد سعی نمایید.");

		EventBus.getDefault().postSticky(new SyncContactsCompletedEvent());		

	}

	@Override
	protected int getRetryLimit() {
		// TODO Auto-generated method stub
		return 2;
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

			List<User> phoneContacts = new ArrayList<User>();
			String result = null;


			PhoneContacts contacts = getPhoneContacts();

			if (contacts == null) {
				return;
			}
			phoneContacts.addAll(contacts.getNewlyAddedContacts());

			JSONObject jObject = new JSONObject();

			jObject.put("phone_numbers", contacts.getContactsJson());

			String value = jObject.toString();
			System.out.println("value" + value);
			String value2=SHAMChatApplication.getConfig().getUserId();
			//String URL = SHAMChatApplication.getMyApplicationContext().getResources().getString(R.string.homeBaseURL) + "getFreindsForPhoneNumbers.htm";
		String URL= Constant.SyncContacts;

		//mast - send request to server for syncing contacts
			  OkHttpClient client = new OkHttpClient();
			  
			   client.setConnectTimeout(180, TimeUnit.SECONDS); // connect timeout
			   client.setReadTimeout(180, TimeUnit.SECONDS);    // socket timeout
			   
			    RequestBody formBody = new FormEncodingBuilder()
			        .add("contact", value)
					.add("user_id", value2)
			        .build();
			    Request request = new Request.Builder()
			        .url(URL)
			        .post(formBody)
			        .build();
			  
			    Response response = client.newCall(request).execute();
				// check if request was successful
			    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
			    
			result = response.body().string();
			response.body().close();
			
			if (result != null) {

				JSONObject serverResponse = new JSONObject(result);
				String status = serverResponse.getString("status");

				if (status.equals("200")) {
					//JSONObject webData = serverResponse.getJSONObject("objects");
					markCurrentAppUsers(serverResponse.getJSONArray("objects"),phoneContacts);

					SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SHAMChatApplication.getMyApplicationContext());
					preferences.edit().putBoolean(PreferenceConstants.INITIAL_LOGIN, false);
					preferences.edit().apply();

				}

			}
			
			showToast("بروز رسانی مخاطبین با موفقیت انجام شد ");	

		EventBus.getDefault().postSticky(new SyncContactsCompletedEvent());

	}
	
	private void showToast(final String message) {
        try {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(SHAMChatApplication.getMyApplicationContext(), message , Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }.start();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
  		
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable throwable) {

		// An error occurred in onRun.
		// Return value determines whether this job should retry running (true)
		// or abort (false).
		System.out.println("Sync Contacts should retry? tries 2 times ");
		
		return true;
	}

	private PhoneContacts getPhoneContacts() throws JSONException {
		 User me;
		 ContentResolver mContentResolver;
		mContentResolver = SHAMChatApplication .getMyApplicationContext().getContentResolver();
		Cursor cursor = mContentResolver.query(UserProvider.CONTENT_URI_USER,
				null, UserConstants.USER_ID + "=?",
				new String[] { SHAMChatApplication.getConfig().getUserId() },
				null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		me = UserProvider.userFromCursor(cursor);

		String mo=	me.getMobileNo().substring((me.getMobileNo().length()-10));
		List<User> newlyAddedContacts = new ArrayList<User>();
		List<String> contactListOnyNumbers = new ArrayList<String>();
		JSONArray contactsJson = new JSONArray();
		ContentResolver cr = SHAMChatApplication.getMyApplicationContext()
				.getContentResolver();

		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, BaseColumns._ID + " ASC");
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(SHAMChatApplication
						.getMyApplicationContext());
		Editor editor = preferences.edit();
		editor.putInt(PreferenceConstants.CONTACT_LAST_COUNT, cur.getCount());
		editor.apply();

		PhoneContacts contacts = null;
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				User user = new User();

				String id = cur.getString(cur.getColumnIndex(BaseColumns._ID));

				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				user.setUsername(name);
				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor phonesCursor = cr.query(
							Phone.CONTENT_URI,
							null,
							Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);

					while (phonesCursor.moveToNext()) {
						String contactNumber = phonesCursor
								.getString(phonesCursor
										.getColumnIndex(Phone.NUMBER));

						Cursor cursor2 =  SHAMChatApplication.getMyApplicationContext().getContentResolver().query(UserProvider.CONTENT_URI_USER, null, UserConstants.MOBILE_NO+" LIKE ? AND "+UserConstants.USER_ID+"!=?  AND "+UserConstants.USER_TYPE+"=?" , new String [] {"%"+contactNumber+"%",SHAMChatApplication.getConfig().getUserId(),"2"}, null);


						if (contactNumber != null && !contactNumber.replace(" ", "").endsWith(mo)&&cursor2.getCount()==0) {
							contactNumber = contactNumber.replace(" ", "")
									.trim();
						} else {
							continue;
						}

						String phoneNumber = contactNumber.replaceAll("[^0-9]",
								"");

						if (phoneNumber != null && phoneNumber.length() > 0) {
							user.setMobileNo(phoneNumber);

							user.setUserId(PreferenceConstants.CONTACT_IDENTIFIER
									+ id);
							contactListOnyNumbers.add(phoneNumber);

							newlyAddedContacts.add(user);

							contactsJson.put(phoneNumber);
						} else {
							System.out.println("Not a valid Number"
									+ contactNumber);
						}
					}

					phonesCursor.close();
				}

			}

			cur.moveToLast();
		}

		long lastId = cur.getLong(cur.getColumnIndex(BaseColumns._ID));
		cur.close();

		if (newlyAddedContacts != null && newlyAddedContacts.size() > 0) {
			contacts = new PhoneContacts(newlyAddedContacts, contactsJson,
					contactListOnyNumbers, lastId);
		}
		return contacts;
	}



	private boolean markCurrentAppUsers(final JSONArray contactsInAppJSONArray,
			final List<User> phoneContacts) {

		System.out.println("contactsInAppJSONArray " + contactsInAppJSONArray);
		if (contactsInAppJSONArray != null
				&& contactsInAppJSONArray.length() > 0) {
			ContentResolver cr = SHAMChatApplication.getMyApplicationContext()
					.getContentResolver();
			for (int i = 0; i < contactsInAppJSONArray.length(); i++) {
				try {
					JSONObject contact = contactsInAppJSONArray
							.getJSONObject(i);
					for (User user : phoneContacts) {

						String mobileNo = user.getMobileNo();

						if (mobileNo.contains(" "))
							mobileNo = mobileNo.replace(" ", "");

						if (mobileNo.length() < 9) {
							continue;
						}

						if (contact != null
								&& contact.has("phone")
								&& contact.getString("phone").length() > 9
								&& contact.getString("phone").substring(
												contact.getString("phone")
														.length() - 9)
										.contains(mobileNo.substring(mobileNo
														.length() - 9))) {

							String userId = contact.getString("user_id");

							ContentValues values = new ContentValues();
							values.put(UserConstants.USER_ID,userId);
							//values.put(UserConstants.CHAT_ID,contact.getString("chatId"));
							values.put(UserConstants.MOBILE_NO, mobileNo);
							values.put(UserConstants.USERNAME,user.getUsername());
							//values.put(UserConstants.EMAIL,contact.getString("email"));
							//values.put(UserConstants.CITY_OR_REGION,contact.getString("region"));
							//values.put(UserConstants.GENDER,contact.getString("gender"));
							values.put(UserConstants.PROFILE_IMAGE_URL,contact.getString("avatar"));

						//	values.put(UserConstants.MY_STATUS,contact.getString("myStatus"));
							values.put(UserConstants.USER_TYPE, 2);

							int result = cr.update(UserProvider.CONTENT_URI_USER, values,
									UserConstants.USER_ID + "=?",
									new String[] { user.getUserId() });
							if (result == 0) {
								cr.insert(UserProvider.CONTENT_URI_USER, values);
							} else {
								System.out.println("SynContacts result "+result+" "+mobileNo);
							}
							
							
							
							String userJabberId = Utils
									.createXmppUserIdByUserId(userId);

							
							
							// add fake roster entry

							final ContentValues rosterValues = new ContentValues();

							rosterValues.put(RosterConstants.JID, userJabberId);

							rosterValues
									.put(RosterConstants.ALIAS,
											user.getUsername()
													+ PreferenceConstants.ROSTER_ITEM_SPLITTER
													+ mobileNo);

							rosterValues.put(RosterConstants.STATUS_MODE,
									StatusMode.offline.ordinal());

							rosterValues
									.put(RosterConstants.STATUS_MESSAGE, "");
							rosterValues.put(RosterConstants.GROUP, "");
							rosterValues.put(RosterConstants.SHOW_IN_CHAT, 1);

							if (cr.update(RosterProvider.CONTENT_URI,
									rosterValues, RosterConstants.JID + "=?",
									new String[] { userJabberId }) == 0) {
								cr.insert(RosterProvider.CONTENT_URI,
										rosterValues);
							}

							// VCard card=new VCard();
							// card.load(SmackableImp.getXmppConnection(),
							// userJabberId);

							break;
						}

					}
				}

				catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		return true;
	}

}
*/
