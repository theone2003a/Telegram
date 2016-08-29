package org.telegram.messenger.shamChat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class RokhPref {

	SharedPreferences								pref;
	Editor											editor;
	Context											_context;
	
	int Private_Mode								= 0;
		
	private static final String PREF_NAME			= "RokhgroupPref";
	private static final String IS_LOGIN			= "IsLoggedin";
	public static final String KEY_USERTOKEN		= "token";
	public static final String KEY_USERID			= "userId";
	public static final String KEY_USERNAME			= "username";
	public static final String KEY_CLIENTHANDLE 	= "clientHandle";
    public static final String KEY_IS_FIRST_RUN		= "isFirstRun";
    public static final String KEY_IS_UNREAD_MSG	= "isUnreadMsg";
    
	public RokhPref(Context context){
		this._context	= context;
		pref		= _context.getSharedPreferences(PREF_NAME, Private_Mode);
		editor		= pref.edit();
	}
	
	public void CreateUser(String token, String userid){
		editor.putBoolean(IS_LOGIN, true);
		editor.putString(KEY_USERTOKEN, token);
		editor.putString(KEY_USERID, userid);
		editor.commit();
	}
	
	public HashMap<String, String> GetUserData(){
		
		HashMap<String, String> UserInfo	= new HashMap<String, String>();
		
		UserInfo.put(KEY_USERTOKEN, pref.getString(KEY_USERTOKEN, null));
		UserInfo.put(KEY_USERID, pref.getString(KEY_USERID, null));
		
		return UserInfo;
		
	}
	
	public String getUSERID(){
		return pref.getString(KEY_USERID, null);
	}
	
	public String getUSERTOKEN(){
		return pref.getString(KEY_USERTOKEN, null);
		
	}
	
	public boolean isLoggedIn(){
		return pref.getBoolean(IS_LOGIN, false);
	}

	public void setUsername(String username){
		editor.putString(KEY_USERNAME, username);
		editor.commit();
	}
	
	public String getUsername(){
		return pref.getString(KEY_USERNAME, null);
	}
	
	public void setClientHandle(String clientHandle){
		editor.putString(KEY_CLIENTHANDLE, clientHandle);
		editor.commit();
	}
	
	public String getClientHandle(){
		return pref.getString(KEY_CLIENTHANDLE, null);
	}
    
    public void setFirstRun(boolean firstRun){
        editor.putBoolean(KEY_IS_FIRST_RUN, firstRun);
        editor.commit();
    }
    
    public boolean getFirstRun(){
        return pref.getBoolean(KEY_IS_FIRST_RUN, false);
    }
    
    public void setUnreadMsg(boolean unreadmsg){
        editor.putBoolean(KEY_IS_UNREAD_MSG, unreadmsg);
        editor.commit();
    }
    
    public boolean getIsUnreadMsg(){
        return pref.getBoolean(KEY_IS_UNREAD_MSG, false);
    }
;}
