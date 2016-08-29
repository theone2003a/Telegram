package org.telegram.messenger.shamChat;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class Utils {
/*
	public static void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size=1024;
		try
		{

			byte[] bytes=new byte[buffer_size];
			for(;;)
			{
				//Read byte from input stream

				int count=is.read(bytes, 0, buffer_size);
				if(count==-1)
					break;

				//Write byte from output stream
				os.write(bytes, 0, count);
			}
		}
		catch(Exception ex){}
	}

	public Utils() {

	}

	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		boolean status = false;

		if (activeNetInfo != null) {
			status = true;
		}

		return status;
	}

	public static boolean isWifiConnected() {
		ConnectivityManager connManager = (ConnectivityManager) SHAMChatApplication
				.getMyApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isGPRSWIFIConnected(Context context){
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

	    if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
	            return true;
	    }else{
	            return false;
	    }
	}	
	

	public static String convertToEnglishDigits(String value) {
		String newValue = value.replace("١", "1").replace("١", "1")
				.replace("٢", "2").replace("٤", "4").replace("٥", "5")
				.replace("٦", "6").replace("٧", "7").replace("٨", "8")
				.replace("٩", "9").replace("٠", "0").replace("٣", "3")
				.replace("۱", "1").replace("۲", "2").replace("۳", "3")
				.replace("۴", "4").replace("۵", "5").replace("۶", "6")
				.replace("۷", "7").replace("۸", "8").replace("۹", "9")
				.replace("۰", "0");

		return newValue;
	}

	public static Bitmap scaleDownImageSize(Bitmap bm, int width, int height,
			int orientation) {
		Bitmap photo = bm;
		photo = Bitmap.createScaledBitmap(photo, width, height, false);
		photo = rotateImage(photo, orientation);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		photo.compress(CompressFormat.JPEG, 100, bytes);

		File f = new File(Environment.getExternalStorageDirectory()
				+ "/salam/profileimage" + File.separator
				+ "Imagename.jpg");
		FileOutputStream fo;
		try {
			f.createNewFile();
			fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return photo;
	}

	public static Bitmap rotateImage(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();

			m.setRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				throw ex;
			}
		}
		return b;
	}

	public static int getExifRotation(String imgPath) {
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			String rotationAmount = exif
					.getAttribute(ExifInterface.TAG_ORIENTATION);
			if (!TextUtils.isEmpty(rotationAmount)) {
				int rotationParam = Integer.parseInt(rotationAmount);
				switch (rotationParam) {
				case ExifInterface.ORIENTATION_NORMAL:
					return 0;
				case ExifInterface.ORIENTATION_ROTATE_90:
					return 90;
				case ExifInterface.ORIENTATION_ROTATE_180:
					return 180;
				case ExifInterface.ORIENTATION_ROTATE_270:
					return 270;
				default:
					return 0;
				}
			} else {
				return 0;
			}
		} catch (Exception ex) {
			return 0;
		}
	}

	public static boolean existCameraApp(PackageManager manager) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		return takePictureIntent.resolveActivity(manager) != null;
	}

	public static boolean isCameraAvailable(PackageManager manager) {
		return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public static File createImageFile(Context context) throws IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getDefault());
        String timeStamp = dateFormat.format(new Date());

		//String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		String imageFileName = "JPEG_" + timeStamp + "_";

		File storageDir = context
				.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, ".jpg", storageDir);
		return image;
	}

	public static Bitmap scaleDownImageSizeProfile(Bitmap bm, int width,
			int height, int quality) {
		Bitmap photo = bm;
		// trycatch
		try {
			photo = Bitmap.createScaledBitmap(photo, width, height, false);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			photo.compress(CompressFormat.JPEG, quality, bytes);
		} catch (Exception e) {
			;
		}

		return photo;
	}

	public static String encodeImage(Bitmap bitmap) {
		// trycatch
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.PNG, 80, byteArrayOutputStream);
			return encodeImage(byteArrayOutputStream.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	private static String encodeImage(byte[] data) {
		return Base64.encodeToString(data, Base64.DEFAULT);
	}


	public static boolean isValidEmail(EditText email) {
		boolean status = false;
		if (email != null) {
			String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
			CharSequence inputString = email.getText();
			Pattern pattern = Pattern.compile(expression,
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(inputString);
			status = matcher.matches();
			if (!status) {
				email.setHint("Invalid Email");
				email.setHintTextColor(Color.RED);
			}
		}
		return status;
	}

	public static void showKeyboard(EditText editText, Context context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	}

	public static String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size
				/ Math.pow(1024, digitGroups))
				+ " " + units[digitGroups];
	}


	public static Bitmap fixOrientation(Bitmap mBitmap) {
		if (mBitmap.getWidth() > mBitmap.getHeight()) {
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
					mBitmap.getHeight(), matrix, true);
		}
		return mBitmap;
	}


	public static String createXmppUserIdByUserId(String userId) {
		if (userId != null && !userId.contains("@")) {
			userId = userId + "@" + PreferenceConstants.XMPP_SERVER;
		}
		return userId;
	}



	public static String getUserIdFromXmppUserId(String xmppUserId) {

		try {
			xmppUserId = xmppUserId.substring(0, xmppUserId.indexOf("@"));

		} catch (Exception e) {
			xmppUserId = null;
		}

		return xmppUserId;
	}

	public static Bitmap base64ToBitmap(String base64) {
		ByteArrayInputStream bais = null;
		Bitmap image = null;
		if (base64 != null) {
			bais = new ByteArrayInputStream(decodeImage(base64));
			image = BitmapFactory.decodeStream(bais);
		}

		return image;
	}

	public static byte[] decodeImage(String base64) {
		return Base64.decode(base64, Base64.DEFAULT);
	}

	public byte[] downloadImageFromUrl(String profileUrl) {
		File downloadedFile = null;
		int count = 0;
		File noMedia = null;
		byte[] blobMessage = null;
		try {
			if (profileUrl == null || profileUrl.equalsIgnoreCase("exception"))
				return null;

			String strUrl = profileUrl;
			URL url = new URL(strUrl);

			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/salam/profileimage");
			if (!folder.exists()) {
				folder.mkdirs();
				noMedia = new File(folder.getAbsolutePath() + "/" + ".nomedia");
			}
			downloadedFile = new File(folder.getAbsolutePath() + "/"
					+ strUrl.substring(strUrl.lastIndexOf("/") + 1));

			if (!downloadedFile.exists()) {
				URLConnection conection = url.openConnection();
				conection.connect();
				// getting file length
				// with 8k buffer
				InputStream input = new BufferedInputStream(url.openStream(),
						8192);

				// Output stream to write file
				OutputStream output = new FileOutputStream(downloadedFile);

				byte data[] = new byte[1024];

				while ((count = input.read(data)) != -1) {
					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();
			} else {
				// file has been already
				// downloaded
				System.out.println("This file has been already downloaded "
						+ downloadedFile.getName());
			}
			blobMessage = getBytesFromFilePath(downloadedFile);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return blobMessage;
	}

	public byte[] getBytesFromFilePath(File filePath) {
		FileInputStream fileInputStream = null;
		byte[] bFile = new byte[(int) filePath.length()];

		try {
			System.out.println("Converting to byte[]");

			// convert file into array of bytes
			fileInputStream = new FileInputStream(filePath);
			fileInputStream.read(bFile);
			fileInputStream.close();

			System.out.println("Done");
		} catch (Exception e) {
			System.out.println("error converting to byte[] " + e.getMessage());
		}
		return bFile;
	}

	public Bitmap scaleDownImageSize(Bitmap bm, int width, int height) {
		Bitmap photo = bm;
		photo = Bitmap.createScaledBitmap(photo, width, height, false);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		photo.compress(CompressFormat.JPEG, 100, bytes);

		File f = new File(Environment.getExternalStorageDirectory()
				+ "/salam/profileimage" + File.separator
				+ "Imagename.jpg");
		FileOutputStream fo;
		try {
			f.createNewFile();
			fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return photo;
	}

	public static boolean isEditTextEmpty(EditText etText) {
		if (etText.getText().toString().trim().length() > 0) {
			return false;
		} else {
			return true;
		}
	}

	public static void hideKeyboard(EditText editText, Context context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public static ContactDetails getConactExists(Context context, String number) {
		// / number is the phone number
		ContactDetails details = new ContactDetails();
		details.isExist = false;
		details.displayName = "";

		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String[] mPhoneNumberProjection = { PhoneLookup._ID,
				PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME };
		Cursor cur = context.getContentResolver().query(lookupUri,
				mPhoneNumberProjection, null, null, null);
		try {
			if (cur.moveToFirst()) {
				details.isExist = true;
				details.displayName = cur.getString(cur
						.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		return details;
	}

	public static class ContactDetails {
		public boolean isExist;
		public String displayName;

		public ContactDetails() {

		}

		public ContactDetails(boolean isExist, String displayName) {
			this.isExist = isExist;
			this.displayName = displayName;
		}
	}

	public static Bitmap decodeSampledBitmapFromResource(String path,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public static boolean contactExists(Context context, String number) {
		// / number is the phone number
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String[] mPhoneNumberProjection = { PhoneLookup._ID,
				PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME };
		Cursor cur = context.getContentResolver().query(lookupUri,
				mPhoneNumberProjection, null, null, null);
		try {
			if (cur.moveToFirst()) {
				return true;
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		return false;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static boolean checkPlayServices(Context context) {
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (status != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				Toast.makeText(context, R.string.googleplay_not_found,
						Toast.LENGTH_SHORT).show();
				try {
					ProgressBarLoadingDialog.getInstance().dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				Toast.makeText(context, R.string.device_not_supported,
						Toast.LENGTH_LONG).show();
				try {
					ProgressBarLoadingDialog.getInstance().dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return false;
		}
		return true;
	}

	public static Map<String, Long> getDurationBreakdownArray(long millis) {
		String[] units = { "Days", "Hours", "Minutes", "Seconds" };
		Long[] values = new Long[units.length];
		Map<String, Long> jo = new HashMap<String, Long>();
		boolean startPrinting = false;
		if (millis <= 0) {
			for (int i = 0; i < units.length; i++) {

				try {
					jo.put(units[i], 0L);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} else {

			values[0] = TimeUnit.MILLISECONDS.toDays(millis);
			millis -= TimeUnit.DAYS.toMillis(values[0]);
			values[1] = TimeUnit.MILLISECONDS.toHours(millis);
			millis -= TimeUnit.HOURS.toMillis(values[1]);
			values[2] = TimeUnit.MILLISECONDS.toMinutes(millis);
			millis -= TimeUnit.MINUTES.toMillis(values[2]);
			values[3] = TimeUnit.MILLISECONDS.toSeconds(millis);

			for (int i = 0; i < units.length; i++) {
				if (!startPrinting && values[i] != 0)
					startPrinting = true;
				if (startPrinting) {
					try {
						jo.put(units[i], values[i]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return jo;
	}

	public static String createXmppRoomIDByUserId(String room) {
		if (room != null && !room.contains("@")) {
			room = room + "@" + PreferenceConstants.CONFERENCE_SERVICE;
		}
		return room;
	}

	public static File createFileFromBase64(Context context,String base64ImageData) {
		FileOutputStream fos = null;
		File file = null;
		try {

			if (base64ImageData != null) {
				file = new File(Environment.getExternalStorageDirectory()+ File.separator + "temp_asasjakska32jaac.png");
				if (!file.exists()) {
					file.createNewFile();
				}
				fos = new FileOutputStream(file);
				byte[] decodedString = Base64.decode(
						base64ImageData, Base64.DEFAULT);
				fos.write(decodedString);
				fos.flush();
				fos.close();

			}

		} catch (Exception e) {
			System.out.println("Error creating file, Utils");
		} finally {
			if (fos != null) {
				fos = null;
			}
		}
		return file;

	}





	public static String getThumbnailsVideo() {
		String videoImageFolder = null;

		try {
			videoImageFolder = Environment.getExternalStorageDirectory()+ "/salam/thumbnailsVideo";



			File profileImageFolder = null;

			try {
				profileImageFolder = new File(videoImageFolder);
				if (!profileImageFolder.exists()) {
					profileImageFolder.mkdirs();

				*//*	File noMedia = new File(profileImageFolder.getAbsolutePath()
							+ "/" + ".nomedia");

					noMedia.createNewFile();*//*

				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("XXX e1 " + e);
			}








		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("XXX e1 " + e);
		}
		return videoImageFolder;
	}



	public static File getProfileImageFolder() {
		File profileImageFolder = null;

		try {
			profileImageFolder = new File(
					Environment.getExternalStorageDirectory()
							+ "/salam/thumbnails");
			if (!profileImageFolder.exists()) {
				profileImageFolder.mkdirs();

				File noMedia = new File(profileImageFolder.getAbsolutePath()
						+ "/" + ".nomedia");

				noMedia.createNewFile();

			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("XXX e1 " + e);
		}
		return profileImageFolder;
	}

	public static File getChatMultimediaFolder() {
		File profileImageFolder = null;

		try {
			profileImageFolder = new File(
					Environment.getExternalStorageDirectory()
							+ "/salam/multimedia");
			if (!profileImageFolder.exists()) {
				profileImageFolder.mkdirs();

				File noMedia = new File(profileImageFolder.getAbsolutePath()
						+ "/" + ".nomedia");

				noMedia.createNewFile();

			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Multimedia e1 " + e);
		}
		return profileImageFolder;
	}

	public static int compareDatesWithoutTime(Calendar c1, Calendar c2) {
		if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
			return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
		if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
			return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
		return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
	}

	public static void handleProfileImage(final Context context,
			final String userId, final String url) {

		if (userId != null && url != null && url.contains("http://")) {

			Target target = new Target() {
				@Override
				public void onBitmapLoaded(final Bitmap bitmap,
						Picasso.LoadedFrom from) {
					System.out.println("Bit map loaded");
					SHAMChatApplication.USER_IMAGES.put(userId, bitmap);
				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable) {
				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable) {
					if (placeHolderDrawable != null) {

					}
				}
			};

			Picasso.with(context).load(url).into(target);
		}

	}

	public static MessageStatusType getMessageStatusType(int status) {

		MessageStatusType messageStatusType = null;

		switch (status) {

		case 0:
			messageStatusType = MessageStatusType.QUEUED;
			break;
		case 1:
			messageStatusType = MessageStatusType.SENDING;
			break;
		case 2:
			messageStatusType = MessageStatusType.SENT;
			break;
		case 3:
			messageStatusType = MessageStatusType.DELIVERED;
			break;
		case 4:
			messageStatusType = MessageStatusType.SEEN;
			break;
		case 5:
			messageStatusType = MessageStatusType.FAILED;
			break;
		}

		return messageStatusType;
	}

	public static int getFileSize(URL url) {
	    HttpURLConnection conn = null;
	    try {
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("HEAD");
	        conn.getInputStream();
	        return conn.getContentLength();
	    } catch (IOException e) {
	        return -1;
	    } finally {
	        conn.disconnect();
	    }
	}


	*//**
	 * generate a random packet id
	 * @return
	 *//*
	 *
	 */



		public static String makePacketId(String userId) {
			Long tsLong = System.currentTimeMillis()/1000;
			String ts = tsLong.toString();
			Random r = new Random();
			int i1 = r.nextInt(80000 - 60000) + 60000;

			String packetId = "packet-"+ userId + "-" + ts +i1;
			return packetId;

	}


		  public static String detectPacketType(String jsonMessageString) {

				JSONObject SampleMsg=null;
				String packetType=null;
				try {
						SampleMsg = new JSONObject(jsonMessageString);
						packetType = SampleMsg.getString("packet_type");


					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						if (jsonMessageString.equalsIgnoreCase("ping")) packetType="ping";
						else packetType ="unknown";

						e1.printStackTrace();
					}

				return packetType;
		  }
/*
		  *//**
		   * Detects packet type based on json string received
		   *//*
		  public static int detectMessageContentType(String jsonMessageString) {

				JSONObject SampleMsg=null;
				int messageType = -1;
				try {
						SampleMsg = new JSONObject(jsonMessageString);
						messageType = SampleMsg.getInt("messageType");

					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				return messageType;
		  }

		  *//**
		   * gets packet id from json message
		   *//*
		  public static String getPacketId(String jsonMessageString) {

				JSONObject SampleMsg=null;
				String packetId = null;
				try {
						SampleMsg = new JSONObject(jsonMessageString);

						if (SampleMsg.has("packetId"))
							packetId = SampleMsg.getString("packetId");
						else if (SampleMsg.has("packet_id"))
							packetId = SampleMsg.getString("packet_id");

					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				return packetId;
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
			case 11 :
				messageType = MessageContentType.GROUP_INFO;
				break;

			}

			return messageType;
		}

	  *//**
	   * Detects packet type based on json string received
	   *//*
	  public static boolean isMyOwnPacket(String jsonMessageString) {

			JSONObject SampleMsg=null;
			int fromUserId = -1;
			try {
					SampleMsg = new JSONObject(jsonMessageString);
					fromUserId = SampleMsg.getInt("from_userid");

				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			int myUserId = Integer.parseInt(SHAMChatApplication.getConfig().getUserId());

			if (myUserId == fromUserId) return true;
			else return false;
	  }

	*//*
	 *  url = file path or whatever suitable URL you want.
	 *//*
	  public static String getMimeType(String url) {
	      String type = null;
	      String extension = MimeTypeMap.getFileExtensionFromUrl(url);
	      if (extension != null) {
	          type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	      }
	      return type;
	  }

	  public static File getErrorsFolder() {
		  File folder = new File(Environment.getExternalStorageDirectory()+"/salam_errors");
		  return folder;
	  }

	 *//**
	  * Get Uri of a image path starting with content://
	  * @param context
	  * @param uri
	  * @return
	  *//*
	  @TargetApi(Build.VERSION_CODES.KITKAT)
	  public static String getFilePathImage(Context context, Uri uri)
	  {
	      int currentApiVersion;
	      try
	      {
	           currentApiVersion = Build.VERSION.SDK_INT;
	      }
	      catch(NumberFormatException e)
	      {
	          //API 3 will crash if SDK_INT is called
	          currentApiVersion = 3;
	      }
	      if (currentApiVersion >= Build.VERSION_CODES.KITKAT)
	      {
	          String filePath = "";
	          String wholeID = DocumentsContract.getDocumentId(uri);

	          // Split at colon, use second item in the array
	          String id = wholeID.split(":")[1];

	          String[] column = {MediaStore.Images.Media.DATA};

	          // where id is equal to
	          String sel = MediaStore.Images.Media._ID + "=?";

	          Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                  column, sel, new String[]{id}, null);

	          int columnIndex = cursor.getColumnIndex(column[0]);

	          if (cursor.moveToFirst())
	          {
	              filePath = cursor.getString(columnIndex);
	          }
	          cursor.close();
	          return filePath;
	      }
	      else if (currentApiVersion <= Build.VERSION_CODES.HONEYCOMB_MR2 && currentApiVersion >= Build.VERSION_CODES.HONEYCOMB)

	      {
	          String[] proj = {MediaStore.Images.Media.DATA};
	          String result = null;

	          CursorLoader cursorLoader = new CursorLoader(
	                  context,
	                  uri, proj, null, null, null);
	          Cursor cursor = cursorLoader.loadInBackground();

	          if (cursor != null)
	          {
	              int column_index =
	                      cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	              cursor.moveToFirst();
	              result = cursor.getString(column_index);
	          }
	          return result;
	      }
	      else
	      {

	          String[] proj = {MediaStore.Images.Media.DATA};
	          Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
	          int column_index
	                  = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	          cursor.moveToFirst();
	          return cursor.getString(column_index);
	      }
	  }	
	  
	  *//**
	   * Converts a packetId to its safe file name equivalent
	   * @param packetId
	   * @return
	   *//*
	  public static String packetIdToFileName(String packetId)
	  {
		  //packetId = packet-6-4332345334   --> for group chat
		  // packetId = 6iIXK-53  --> for single chat
		  int count = StringUtils.countMatches(packetId, "-");
		  String fileName = packetId;
		  //if this is a group packet id we omit user id from it (it has two - characters)
		  if (count >=2) fileName= packetId.substring(packetId.indexOf('-')+1);
		  
		  fileName = fileName.replace('-','_');
		  fileName =  fileName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
		  
		return fileName;
		  
	  }
	  *//**
	   * Checks if a file exists or not
	   * @param filePath
	   * @return
	   *//*
	  public static boolean fileExists (String filePath)
	  {
		  if (filePath==null) return false;
		  
		  File file = new File(filePath);
		  if(file.exists())      
		   return true;
		  else		  
		   return false;  
	  }
	  
		*//*
		 * Efficiently Load Image from file path    
		 *//*
	public static Bitmap decodeSampledBitmapFromFile(String path,
		            int reqWidth, int reqHeight) { // BEST QUALITY MATCH

		    	Bitmap bm = null;
		    try {
		    	

		        // First decode with inJustDecodeBounds=true to check dimensions
		        final BitmapFactory.Options options = new BitmapFactory.Options();
		        options.inJustDecodeBounds = true;
		        BitmapFactory.decodeFile(path, options);

		        // Calculate inSampleSize
		            // Raw height and width of image
		            final int height = options.outHeight;
		            final int width = options.outWidth;
		            //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		            
		            int inSampleSize = 1;

		            if (height > reqHeight) {
		                inSampleSize = Math.round((float)height / (float)reqHeight);
		            }

		            int expectedWidth = width / inSampleSize;

		            if (expectedWidth > reqWidth) {
		                //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
		                inSampleSize = Math.round((float)width / (float)reqWidth);
		            }


		        options.inSampleSize = inSampleSize;

		        // Decode bitmap with inSampleSize set
		        options.inJustDecodeBounds = false;
		        
		        bm = BitmapFactory.decodeFile(path, options);
		        
		    } catch (Exception e) {
		 	   e.printStackTrace();
			   Toast.makeText(SHAMChatApplication.getMyApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			  }
		    
		        return bm;
		      }
	  
	
	*//**
	 * returns current image file local file path or null if nothing exists
	 * @return
	 *//*
	public static String getImageLocalFilePath(String packetId)
	{
		Cursor cursor = SHAMChatApplication.getMyApplicationContext().getContentResolver()
				.query(
		ChatProviderNew.CONTENT_URI_CHAT, null,
		ChatMessage.PACKET_ID + "=?", new String[] { packetId },
		null);

		String fileUrl = null;
		if (cursor != null) {

			cursor.moveToFirst();
		 	fileUrl = cursor.getString(cursor.getColumnIndex(ChatMessage.FILE_URL));
		}

        if (cursor != null) {
            cursor.close();
        }

        return fileUrl;
	}


	*//**
	 * Stack Blur v1.0 from
	 * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
	 * Java Author: Mario Klingemann <mario at quasimondo.com>
	 * http://incubator.quasimondo.com
	 *
	 * created Feburary 29, 2004
	 * Android port : Yahel Bouaziz <yahel at kayenko.com>
	 * http://www.kayenko.com
	 * ported april 5th, 2012
	 *
	 * This is a compromise between Gaussian Blur and Box blur
	 * It creates much better looking blurs than Box Blur, but is
	 * 7x faster than my Gaussian Blur implementation.
	 *
	 * I called it Stack Blur because this describes best how this
	 * filter works internally: it creates a kind of moving stack
	 * of colors whilst scanning through the image. Thereby it
	 * just has to add one new block of color to the right side
	 * of the stack and remove the leftmost color. The remaining
	 * colors on the topmost layer of the stack are either added on
	 * or reduced by one, depending on if they are on the right or
	 * on the left side of the stack.
	 *  
	 * If you are using this algorithm in your code please add
	 * the following line:
	 * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
	 *//*

	public static Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

	    int width = Math.round(sentBitmap.getWidth() * scale);
	    int height = Math.round(sentBitmap.getHeight() * scale);
	    sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

	    Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

	    if (radius < 1) {
	        return (null);
	    }

	    int w = bitmap.getWidth();
	    int h = bitmap.getHeight();

	    int[] pix = new int[w * h];
	    Log.e("pix", w + " " + h + " " + pix.length);
	    bitmap.getPixels(pix, 0, w, 0, 0, w, h);

	    int wm = w - 1;
	    int hm = h - 1;
	    int wh = w * h;
	    int div = radius + radius + 1;

	    int r[] = new int[wh];
	    int g[] = new int[wh];
	    int b[] = new int[wh];
	    int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
	    int vmin[] = new int[Math.max(w, h)];

	    int divsum = (div + 1) >> 1;
	    divsum *= divsum;
	    int dv[] = new int[256 * divsum];
	    for (i = 0; i < 256 * divsum; i++) {
	        dv[i] = (i / divsum);
	    }

	    yw = yi = 0;

	    int[][] stack = new int[div][3];
	    int stackpointer;
	    int stackstart;
	    int[] sir;
	    int rbs;
	    int r1 = radius + 1;
	    int routsum, goutsum, boutsum;
	    int rinsum, ginsum, binsum;

	    for (y = 0; y < h; y++) {
	        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
	        for (i = -radius; i <= radius; i++) {
	            p = pix[yi + Math.min(wm, Math.max(i, 0))];
	            sir = stack[i + radius];
	            sir[0] = (p & 0xff0000) >> 16;
	            sir[1] = (p & 0x00ff00) >> 8;
	            sir[2] = (p & 0x0000ff);
	            rbs = r1 - Math.abs(i);
	            rsum += sir[0] * rbs;
	            gsum += sir[1] * rbs;
	            bsum += sir[2] * rbs;
	            if (i > 0) {
	                rinsum += sir[0];
	                ginsum += sir[1];
	                binsum += sir[2];
	            } else {
	                routsum += sir[0];
	                goutsum += sir[1];
	                boutsum += sir[2];
	            }
	        }
	        stackpointer = radius;

	        for (x = 0; x < w; x++) {

	            r[yi] = dv[rsum];
	            g[yi] = dv[gsum];
	            b[yi] = dv[bsum];

	            rsum -= routsum;
	            gsum -= goutsum;
	            bsum -= boutsum;

	            stackstart = stackpointer - radius + div;
	            sir = stack[stackstart % div];

	            routsum -= sir[0];
	            goutsum -= sir[1];
	            boutsum -= sir[2];

	            if (y == 0) {
	                vmin[x] = Math.min(x + radius + 1, wm);
	            }
	            p = pix[yw + vmin[x]];

	            sir[0] = (p & 0xff0000) >> 16;
	            sir[1] = (p & 0x00ff00) >> 8;
	            sir[2] = (p & 0x0000ff);

	            rinsum += sir[0];
	            ginsum += sir[1];
	            binsum += sir[2];

	            rsum += rinsum;
	            gsum += ginsum;
	            bsum += binsum;

	            stackpointer = (stackpointer + 1) % div;
	            sir = stack[(stackpointer) % div];

	            routsum += sir[0];
	            goutsum += sir[1];
	            boutsum += sir[2];

	            rinsum -= sir[0];
	            ginsum -= sir[1];
	            binsum -= sir[2];

	            yi++;
	        }
	        yw += w;
	    }
	    for (x = 0; x < w; x++) {
	        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
	        yp = -radius * w;
	        for (i = -radius; i <= radius; i++) {
	            yi = Math.max(0, yp) + x;

	            sir = stack[i + radius];

	            sir[0] = r[yi];
	            sir[1] = g[yi];
	            sir[2] = b[yi];

	            rbs = r1 - Math.abs(i);

	            rsum += r[yi] * rbs;
	            gsum += g[yi] * rbs;
	            bsum += b[yi] * rbs;

	            if (i > 0) {
	                rinsum += sir[0];
	                ginsum += sir[1];
	                binsum += sir[2];
	            } else {
	                routsum += sir[0];
	                goutsum += sir[1];
	                boutsum += sir[2];
	            }

	            if (i < hm) {
	                yp += w;
	            }
	        }
	        yi = x;
	        stackpointer = radius;
	        for (y = 0; y < h; y++) {
	            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
	            pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

	            rsum -= routsum;
	            gsum -= goutsum;
	            bsum -= boutsum;

	            stackstart = stackpointer - radius + div;
	            sir = stack[stackstart % div];

	            routsum -= sir[0];
	            goutsum -= sir[1];
	            boutsum -= sir[2];

	            if (x == 0) {
	                vmin[y] = Math.min(y + r1, hm) * w;
	            }
	            p = x + vmin[y];

	            sir[0] = r[p];
	            sir[1] = g[p];
	            sir[2] = b[p];

	            rinsum += sir[0];
	            ginsum += sir[1];
	            binsum += sir[2];

	            rsum += rinsum;
	            gsum += ginsum;
	            bsum += binsum;

	            stackpointer = (stackpointer + 1) % div;
	            sir = stack[stackpointer];

	            routsum += sir[0];
	            goutsum += sir[1];
	            boutsum += sir[2];

	            rinsum -= sir[0];
	            ginsum -= sir[1];
	            binsum -= sir[2];

	            yi += w;
	        }
	    }

	    Log.e("pix", w + " " + h + " " + pix.length);
	    bitmap.setPixels(pix, 0, w, 0, 0, w, h);

	    return (bitmap);
	}

	public static boolean isUsernameValid(String username) {
		boolean isValid = false;

		String expression = "^[a-z0-9_-]{3,15}$";
		CharSequence inputStr =  username;

		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}




    *//**
     * This is used to save in database for last update time and so
     *
     * @param yourDate  in GMT/UTC time or unix epoch
     * @param format
     * @return
     *//*
     * */
    public static String formatDate(long yourDate, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = new Date(yourDate);
        String dobStr = dateFormat.format(date);
        return dobStr;
    }
/*
    *//**
     * This formats and converts date from epoch unix timestamp to local time of user (which is set in settings-> date time)
     * @param timestamp
     * @param format
     * @return
     *//*
    public static String formatDateToLocal(long timestamp, String format) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getDefault());

        String dateTime = formatter.format(new Date(timestamp));

        dateTime = UTCToLocalDateConvert(dateTime, format);
       return dateTime;

    }

    *//**
     * Converts a string like 2011-06-23T15:11:32 from UTC to android local time
     * @param OurDate
     * @return
     *//*
    public static String UTCToLocalDateConvert(String OurDate, String fromat)
    {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date value = null;
        String dt = "";
        try {
            value = formatter.parse(OurDate);

        SimpleDateFormat dateFormatter = new SimpleDateFormat(fromat);
        dateFormatter.setTimeZone(TimeZone.getDefault());
        dt = dateFormatter.format(value);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dt;
    }

    *//**
     * returns datetime - sqllite compatible - GMT/UTC time
     * @return
     *//*
     * */
    public static String getTimeStamp() {
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = new Date();

        Calendar c = new GregorianCalendar();
        c.setTime(date);

        //mast - backend has a bug which sends back the time 15 minutes added to it
        //so we add 15 minutes to time to keep listview ordering of channel/group correctly

        c.add(Calendar.MINUTE, 5);
        //c.add(Calendar.HOUR, 1);
        //c.add(Calendar.HOUR, 1);
        //c.set(Calendar.MINUTE, 0);
        //c.set(Calendar.SECOND, 0);
        date = c.getTime();

        return simpleDateFormat.format(date);
    }
/*
    public static String getTimeInReadableFormat(Context context, long time) {
        long timeDiff = System.currentTimeMillis() - time;

        if (timeDiff < 1000 * 60 * 60 * 24) {
            return context.getResources().getString(R.string.today);
        } else if (timeDiff < 1000 * 60 * 60 * 24 * 2) {
            return context.getResources().getString(R.string.yesterday);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM kk.mm", Locale.US);
        dateFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(new Date(time));
    }

    *//**
     * Gets a UTC datetime and converts it to user local time
     * @param dateInString
     //* @param format
     * @return
     *//*
    public static String formatStringDate(String dateInString, String format) {
        Date formatedDate = null;
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {

            formatedDate = formatter.parse(dateInString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date = new Date(formatedDate.getTime());
        String dobStr = formatter.format(date);

        dobStr = UTCToLocalDateConvert(dobStr,format);

        return dobStr;
    }

	public static String UTCToLocalDateConvertSingleChat(String OurDate, String fromat)
	{

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		//formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date value = null;
		String dt = "";
		try {
			value = formatter.parse(OurDate);

			SimpleDateFormat dateFormatter = new SimpleDateFormat(fromat);
			dateFormatter.setTimeZone(TimeZone.getDefault());
			dt = dateFormatter.format(value);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dt;
	}
	public static String formatStringDateSingleChat(String dateInString, String format) {
		Date formatedDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
		//formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		try {

			formatedDate = formatter.parse(dateInString);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		Date date = new Date(formatedDate.getTime());
		String dobStr = formatter.format(date);

		dobStr = UTCToLocalDateConvertSingleChat(dobStr,format);

		return dobStr;
	}


*/
	public static Date getDateFromStringDate(String dateInString) {
        Date formatedDate = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        //formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {

            formatedDate = formatter.parse(dateInString);
            // System.out.println(formatedDate);
            // System.out.println(formatter.format(formatedDate));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formatedDate;
    }
/*
    public static Date getDateFromStringDate(String dateInString, String existingFormat) {
        Date formatedDate = null;
        SimpleDateFormat formatter = new SimpleDateFormat(existingFormat, Locale.US);
        //formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {

            formatedDate = formatter.parse(dateInString);
            // System.out.println(formatedDate);
            // System.out.println(formatter.format(formatedDate));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formatedDate;
    }

	*//**
	 * Returns the good width size to use in chats in pixels
	 * @param ctx
	 * @return
	 *//*

	public static String b="";
	public static  String getImagePorofile(final String userId)
	{
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					OkHttpClient client = new OkHttpClient();


					client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
					client.setReadTimeout(60, TimeUnit.SECONDS);    // socket timeout
					Request request = new Request.Builder()
							.url("http://social.rabtcdn.com/groups/api/v1/avatar/with/userid/"+userId+"/")
							.cacheControl(new CacheControl.Builder().noCache().build())
							.build();

					Response responses=null;
					try {
						responses = client.newCall(request).execute();

					} catch (IOException e) {
						e.printStackTrace();
					}
					String jsonData = responses.body().string();
					JSONObject Jobject = new JSONObject(jsonData);
					String z= Jobject.getString("avatar");
					b=z;
					//JSONArray Jarray = Jobject.getJSONArray("objects");

				*//*	for (int i = 0; i < Jarray.length(); i++) {
						JSONObject object     = Jarray.getJSONObject(i);
					}*//*
				}
				catch (Exception e)
				{

					Log.e("getImagePorofile", "getImagePorofile: ",e );
				}

			}
		};

		thread.start();

		return b;
	}

	public  static int getDisplayWidth(Context ctx)
	{

		int displayWidth = (int) com.rokhgroup.utils.Utils.getWidthInPx(ctx);
		return displayWidth;
	}
	public static int getFittingImageWidthPx(Context ctx) {
		int imageWidthInPx = 0;
		//get device width in pixels
		int displayWidth = (int) com.rokhgroup.utils.Utils.getWidthInPx(ctx);

		if (displayWidth>=1000)
			imageWidthInPx = 900;
		else if (displayWidth>=900)
			imageWidthInPx = 800;
		else if (displayWidth>=800)
			imageWidthInPx = 700;
		else if (displayWidth>=700)
			imageWidthInPx = 600;
		else if (displayWidth>=650)
			imageWidthInPx = 550;
		else if (displayWidth>=550)
			imageWidthInPx = 450;
		else if (displayWidth>=450)
			imageWidthInPx = 350;
		else if (displayWidth>=350)
			imageWidthInPx = 300;
		else if (displayWidth>=250)
			imageWidthInPx = 220;
				else
			imageWidthInPx = 240;

		return imageWidthInPx;
	}


	public static int stikersize(Context ctx) {
		int imageWidthInPx = 0;
		//get device width in pixels
		int displayWidth = (int) com.rokhgroup.utils.Utils.getWidthInPx(ctx);

		if (displayWidth>=1000)
			imageWidthInPx = 512;
		else if (displayWidth>=900)
			imageWidthInPx = 450;
		else if (displayWidth>=800)
			imageWidthInPx = 400;
		else if (displayWidth>=700)
			imageWidthInPx = 350;
		else if (displayWidth>=650)
			imageWidthInPx = 300;
		else if (displayWidth>=550)
			imageWidthInPx = 300;
		else if (displayWidth>=450)
			imageWidthInPx = 300;
		else if (displayWidth>=350)
			imageWidthInPx = 200;
		else if (displayWidth>=250)
			imageWidthInPx = 150;
		else
			imageWidthInPx = 150;

		return imageWidthInPx;
	}



	*//**
	 * Get file path from URI
	 *
	 * @param context context of Activity
	 * @param uri     uri of file
	 * @return path of given URI
	 *//*
	public static String getPath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {column};
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}*/

}
