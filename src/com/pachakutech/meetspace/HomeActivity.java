package com.pachakutech.meetspace;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import com.pachakutech.meetspace.*;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.*;
import android.content.*;


public class HomeActivity extends Activity {

	private Button fbLoginButton;
	private Button twLoginButton;
	private Dialog progressDialog;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		setContentView( R.layout.home );

		fbLoginButton = (Button) findViewById( R.id.fbLoginButton );
		fbLoginButton.setOnClickListener( new View.OnClickListener( ) {
				@Override
				public void onClick( View v ) {
					onFacebookLoginButtonClicked( );
				}
			} );
		twLoginButton = (Button) findViewById( R.id.twLoginButton );
		twLoginButton.setOnClickListener( new View.OnClickListener( ) {
				@Override
				public void onClick( View v ) {
					onTwitterLoginButtonClicked( );
				}
			} );
		
//    	Utils.saveChipherText(MeetSpace.getContext(), "");

		// Check if there is a currently logged in user
		// and they are linked to a Facebook account.
		ParseUser currentUser = ParseUser.getCurrentUser( );
		if( ( currentUser != null ) && ParseFacebookUtils.isLinked( currentUser ) ) {
			// Go to the user info activity
			showUserDetailsActivity( MeetSpace.FACEBOOK );
		}else if(( currentUser != null ) &&ParseTwitterUtils.isLinked( currentUser ) ){
			showUserDetailsActivity( MeetSpace.TWITTER );
		}
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater( ).inflate( R.menu.main, menu );
		return true;
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent data ) {
		super.onActivityResult( requestCode, resultCode, data );
		ParseFacebookUtils.finishAuthentication( requestCode, resultCode, data );
	}

	private void onFacebookLoginButtonClicked( ) {
		HomeActivity.this.progressDialog = ProgressDialog.show(
			HomeActivity.this, "", "Logging in...", true );
		List<String> permissions = Arrays.asList( "basic_info", "user_about_me",
												 "user_relationships", "user_birthday", "user_location" );
		ParseFacebookUtils.logIn( permissions, this, new LogInCallback( ) {
				@Override
				public void done( ParseUser user, ParseException err ) {
					HomeActivity.this.progressDialog.dismiss( );
					if( user == null ) Log.d( MeetSpace.TAG, "User cancelled the Facebook login." );
					else if( user.isNew( ) ) {
						Log.d( MeetSpace.TAG,
							  "User signed up and logged in through Facebook!" );
						showUserDetailsActivity( MeetSpace.FACEBOOK );
					} else {
						Log.d( MeetSpace.TAG,
							  "User logged in through Facebook!" );
						showUserDetailsActivity( MeetSpace.FACEBOOK );
					}
				}
			} );
	}

	private void onTwitterLoginButtonClicked( ) {
		HomeActivity.this.progressDialog = ProgressDialog.show( HomeActivity.this, "", "Logging in...", true );
        ParseTwitterUtils.logIn( this, new LogInCallback( ){
				@Override
				public void done( ParseUser user, ParseException e ) {
					if( user == null ) Log.d( MeetSpace.TAG, "User cancelled twitter login" );
					else if( user.isNew( ) ) {
						Log.d( MeetSpace.TAG, "User signed up and logged in with twitter" );
						showUserDetailsActivity(MeetSpace.TWITTER);
					} else { 
					    Log.d( MeetSpace.TAG, "User signed in with twitter" );
						showUserDetailsActivity(MeetSpace.TWITTER);
					}
				}
			} );
	}

	private void showUserDetailsActivity( int network ) {
		Bundle bundle = new Bundle();
		bundle.putInt("network", network);
		Intent intent = new Intent( this, ViewerActivity.class );
		intent.putExtras(bundle);
		startActivity( intent );
	}
}
