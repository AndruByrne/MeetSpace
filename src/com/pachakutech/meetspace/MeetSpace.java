package com.pachakutech.meetspace;

import android.app.Application;
import com.pachakutech.*;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import android.content.*;
import com.parse.*;


public class MeetSpace extends Application {
//Hotel Utah 415.495.0617
    public static final Boolean APPDEBUG = true;
    public static final String TAG = "MeetSpace";
	public static final int TWITTER = 44;
	public static final int FACEBOOK = 55;
	
    private volatile static Context context;
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "MGLk8RjoPAXV5Ct2jJynl77xl8XHq4VyTtfWETtf", 
		        "7CWwbI90dM5Yf8VBagfLEREhD2rvW7ZKwSwONzCr"); 
		ParseTwitterUtils.initialize(getString(R.string.tw_app_id), getString(R.string.tw_app_sec));

		/// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(getString(R.string.fb_app_id));

		MeetSpace.context = getApplicationContext();
		
	}
	public static Context getContext(){
		return MeetSpace.context;
	}

}
