package com.pachakutech.meetspace;

import android.app.Application;
import com.pachakutech.*;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import android.content.*;
import com.parse.*;


public class MeetSpace extends Application {
//Hotel Utah 415.495.0617
//NFC will send to server URI, which will relay to app store, 
//which can register the IP address?
//then recognise user and send ID of originator
//
//Or Originator is marked as first on a stack of users in the room
//who have originated
//assume all users in a room have similar data connectivitiy
//and so connect to the bottom of whatever the room stack is
//so can I get a callback for successul ndef send?
//then new user logic is seeing bottom of nfc stack
//(if NFC, maybe if recent NFC?)
//then friending that user
//could maybe do a check in with the server to see if 
//they visited it just recently
//if not, then leave user illuminiated
//certainly only turn off illumination if user selects profile right off
//maybe just always show stack in order, cutting down at 5-10 minutes
//popping off bottom on immediate freinding
//
    protected static final Boolean APPDEBUG = false;
    protected static final String TAG = "MeetSpace";
	protected static final int TWITTER = 44;
	protected static final int FACEBOOK = 55;
	protected static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    protected static final double[] SEARCH_RADIUS = {.001, .004, .008, .016, .032, .064, .096, .2, .4, .8, 1.4, 2};
	protected static final int TWELVE_SECONDS = 12000;
	
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
