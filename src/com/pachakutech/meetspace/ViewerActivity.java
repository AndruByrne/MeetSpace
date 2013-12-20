package com.pachakutech.meetspace;

import org.json.JSONException;
import org.json.JSONObject;
import com.pachakutech.meetspace.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import java.util.*;
import com.facebook.*;
import com.google.android.gms.common.*;
import android.support.v4.app.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Application;
import android.app.Activity;
import android.content.*;
import android.location.Location;
import com.parse.*;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.*;
import android.view.*;
import android.support.v4.view.*;
import com.pachakutech.meetspace.ViewerActivity;
import com.pachakutech.meetspace.ZoomOutPageTransformer;
import org.apache.http.client.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import java.io.*;
import org.apache.http.HttpResponse;
import android.os.*;
import java.nio.*;
import org.apache.http.HttpEntity;
import org.apache.http.util.*;
import android.util.*;
import com.google.gson.*;
import android.nfc.*;
import android.widget.*;
import android.content.res.*;
import android.app.ProgressDialog;

public class ViewerActivity extends FragmentActivity implements LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private Location lastLocation = null;
	private Location currentLocation = null;
	private Dialog progressDialog;
	private Button logoutButton;
	private Button loginOtherButton;
	private TextView statusBar;
	private boolean hasSetUpInitialLocation;
	private ParseGeoPoint userGeoPoint;
	private int currentRadius = 3;
	private boolean checkedAbove;
	private int NUM_MUGS;
	private ViewPager pager;
	private ParseObject thisRoom = null;
	private ParseUser[] roomPopulation;
    private ViewerActivity.FacebookSlidePagerAdapter fbAdapter;
	private ViewerActivity.TwitterSlidePagerAdapter twAdapter;
	private String network_name;
	private int network;
	private NdefRecord ndefRecord;
	private boolean loggingOut = false;



	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		setContentView( R.layout.userdetails );


        setNetwork( getIntent( ).getExtras( ).getInt( "network" ) );

		pager = (ViewPager) findViewById( R.id.pager );
		pager.setId( 0x7F04FAF0 );

		statusBar = (TextView) findViewById(R.id.statusBar);
		Resources res = getResources( );
		loginOtherButton = (Button) findViewById( R.id.loginOtherButton );
		loginOtherButton.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick( View v ){
				if( network == MeetSpace.FACEBOOK ) 
					twitterLoginButtonClicked();
				else facebookLoginButtonClicked();
			}
		});
		logoutButton = (Button) findViewById( R.id.logoutButton );
		logoutButton.setOnClickListener( new View.OnClickListener( ) {
				@Override
				public void onClick( View v ) {onLogoutButtonClicked( );}
			} );

		//	pager.setPageTransformer(true, new ZoomOutPageTransformer());
		fbAdapter = new FacebookSlidePagerAdapter( getSupportFragmentManager( ) );
		twAdapter = new TwitterSlidePagerAdapter( getSupportFragmentManager( ) );
		NfcAdapter nfc = NfcAdapter.getDefaultAdapter( this );
		if( nfc != null ) {
			nfc.setNdefPushMessageCallback( this, this );
			nfc.setOnNdefPushCompleteCallback( this, this );
		}
		if( network == MeetSpace.FACEBOOK ) {
			logoutButton.setBackground( res.getDrawable( R.drawable.button_fb_login ) );
			loginOtherButton.setBackground( res.getDrawable( R.drawable.button_tw_login ) );
			pager.setAdapter( fbAdapter );
			// Fetch Facebook user info if the session is active
			Session session = ParseFacebookUtils.getSession( );
			if( session != null && session.isOpened( ) ) {
				getFbId( );
//			makeFriendsRequest( );
			} else Log.e( MeetSpace.TAG, "no request made" );
		} else if( network == MeetSpace.TWITTER ) {
			logoutButton.setBackground( res.getDrawable( R.drawable.button_tw_login ) );
			loginOtherButton.setBackground( res.getDrawable( R.drawable.button_fb_login ) );
			new GetTwitterId( ).execute( );
			pager.setAdapter( twAdapter );
		}
	}

	@Override
	public void onResume( ) {
		super.onResume( );
	    //reset room searching params
		checkedAbove = false;
		// Create a new global location parameters object
		locationRequest = LocationRequest.create( );
		locationRequest.setInterval( MeetSpace.TWELVE_SECONDS / 12 )
			.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY )
			.setFastestInterval( MeetSpace.TWELVE_SECONDS / 120 )
			.setExpirationDuration( MeetSpace.TWELVE_SECONDS )
			.setSmallestDisplacement( 10 );
		locationClient = new LocationClient( this, this, this );
		locationClient.connect( );

		ParseUser currentUser = ParseUser.getCurrentUser( );
		if( currentUser != null ) updateViewsWithSelfProfileInfo( );
		else startLoginActivity( );
	}

	@Override
	public void onPause( ) {
		super.onPause( );
		if( locationClient.isConnected( ) ) {
			locationClient.removeLocationUpdates( this );
			locationClient.unregisterConnectionCallbacks( this );
			locationClient.unregisterConnectionFailedListener( this );
			locationClient.disconnect( );

		}
	}

	@Override
	public void onStop( ) {
		if( thisRoom != null && loggingOut == false ) {
//			ParseRelation population = thisRoom.getRelation( "population" );
			thisRoom.getRelation( "population" ).remove( ParseUser.getCurrentUser( ) );
			thisRoom.saveInBackground( );
		}
		if( locationClient.isConnected( ) ) {stopPeriodicUpdates( );
			locationClient.disconnect( );
		}
		super.onStop( );
	}

	private void getFbId( ) {
		Request request = Request.newMeRequest( ParseFacebookUtils.getSession( ),
		    new Request.GraphUserCallback( ) {
				@Override
				public void onCompleted( GraphUser user, Response response ) {
					if( user != null ) {
						String id = user.getId( );
						ParseUser currentUser = ParseUser.getCurrentUser( );
						currentUser.put( network_name + "Id", id );
						currentUser.put( "name", user.getName( ) );
						currentUser.saveInBackground( );
						ndefRecord = NdefRecord.createUri( "http://facebook.com/profile.php?id=" + id );
						Log.i( MeetSpace.TAG, "NdefTag" + ndefRecord.toString( ) );
//						NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter( MeetSpace.getContext( ) );
//						if( nfcAdapter != null ) {
//							nfcAdapter.setOnNdefPushCompleteCallback( new NfcAdapter.OnNdefPushCompleteCallback( ){
//									public void onNdefPushComplete( NfcEvent nfcEvent ) {
//										Toast.makeText( MeetSpace.getContext( ), "sent profile information through NFC", Toast.LENGTH_SHORT ).show( );
//									}
//								}, null );
//							nfcAdapter.setNdefPushMessage( new NdefMessage( ndefRecord ), null );
//						}
					} else if( response.getError( ) != null ) {
						if( ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_RETRY )
                           || ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION ) ) {
							Log.d( MeetSpace.TAG, "The facebook session was invalidated." );
							onLogoutButtonClicked( );
						} else Log.d( MeetSpace.TAG, "Unknown error: " + response.getError( ).getErrorMessage( ) );
					}
				}
			} );
		request.executeAsync( );
	}

	private class GetTwitterId extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground( Void[] v ) {

			String response = "";
			final HttpClient client = new DefaultHttpClient( );
			HttpGet verifyGet = new HttpGet(
				"https://api.twitter.com/1.1/account/verify_credentials.json?include_user_entities=false&include_entities=false&skip_status=true" );
			ParseTwitterUtils.getTwitter( ).signRequest( verifyGet );
			try { response = EntityUtils.toString( client.execute( verifyGet ).getEntity( ) );
			} catch(IOException e) {Log.e( MeetSpace.TAG, "Twitter error: " + e.toString( ) );}
			return response;
		}

        @Override
        protected void onPostExecute( String response ) {
			final JsonObject jsonObj = ( new JsonParser( ).parse( response ) ).getAsJsonObject( );
			String id = jsonObj.get( "id_str" ).getAsString( );
			ParseUser currentUser = ParseUser.getCurrentUser( );
			currentUser.put( network_name + "Id", id );
			currentUser.put( "name", jsonObj.get( "screen_name" ).getAsString( ) );
			currentUser.put( "cameoURL", jsonObj.get( "profile_image_url" ).getAsString( ).replace( "_normal", "" ) );
			currentUser.saveInBackground( );
			ndefRecord = NdefRecord.createUri( "https://api.twitter.com/1.1/friendships/create.json?user_id=" + id + "&follow=true" );
//			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter( MeetSpace.getContext( ) );
//			if( nfcAdapter != null ) {
//				nfcAdapter.setOnNdefPushCompleteCallback( new NfcAdapter.OnNdefPushCompleteCallback( ){
//						public void onNdefPushComplete( NfcEvent nfcEvent ) {
//						}
//					}, null );
//				nfcAdapter.setNdefPushMessage( new NdefMessage( ndefRecord ), null );
//			}
		}
	}

	@Override
	public void onNdefPushComplete( NfcEvent nfcEvent ) {
		Toast.makeText( MeetSpace.getContext( ), "sent profile information through NFC", Toast.LENGTH_SHORT ).show( );
	}

	@Override
	public NdefMessage createNdefMessage( NfcEvent nfcEvent ) {
		return new NdefMessage( new NdefRecord[] {ndefRecord} );
	}

    //**********Room Checking, Creating, and Adding self******//
	private void lookForARoom( ) {
//		ParseQuery roomQuery = ParseQuery.getQuery( "Room" );
		ParseQuery.getQuery( "Room" ).whereWithinKilometers( "location", userGeoPoint, MeetSpace.SEARCH_RADIUS[currentRadius] )
			.whereEqualTo( "network", network_name )
			.countInBackground( new CountCallback( ){
				public void done( int count, ParseException e ) {
					if( e == null ) {
						if( count == 0 && currentRadius > MeetSpace.SEARCH_RADIUS.length - 2 ) {
							makeNewRoom( );
						} else if( count == 0 && currentRadius < MeetSpace.SEARCH_RADIUS.length - 1 && checkedAbove == false ) {
							currentRadius++;
							lookForARoom( );
						} else if( count == 0 && currentRadius < MeetSpace.SEARCH_RADIUS.length - 1 && checkedAbove == true ) {
							//eventually put a query into the upper rooms here, characterize them by preferences, and advertise them to user
							makeNewRoom( );
						} else if( count > 1 && currentRadius != 0 ) {
							checkedAbove = true;
							currentRadius--;
							lookForARoom( );
						} else if( count == 1 && currentRadius > 3 ) {
							Activity activity = ViewerActivity.this;
							AlertDialog.Builder builder = new AlertDialog.Builder( activity );
							builder.setMessage( getString( R.string.room_found ) + 
											   Double.toString( MeetSpace.SEARCH_RADIUS[currentRadius] * 1000 ) + 
											   getString( R.string.too_large_prompt ) );
							builder.setPositiveButton( "Yes", new DialogInterface.OnClickListener( ){
									public void onClick( DialogInterface dialog, int Id ) {
										makeNewRoom( );
									}
								} );
							builder.setNegativeButton( "No", new DialogInterface.OnClickListener( ){
									public void onClick( DialogInterface dialog, int Id ) {
										joinRoom( );
									}
								} );
							AlertDialog dialog = builder.create( );
							ErrorDialogFragment errorFragment = new ErrorDialogFragment( );
							errorFragment.setDialog( dialog );
							errorFragment.show( getSupportFragmentManager( ), MeetSpace.TAG );
						} else {
							joinRoom( );
						}
					}
				}
			} );
	}

	private void makeNewRoom( ) {
		if( ParseUser.getCurrentUser( ) != null ) {
			thisRoom = new ParseObject( "Room" );
			thisRoom.put( "network", network_name );
			thisRoom.put( "location", userGeoPoint );
			thisRoom.put( "title", getRoomTitle( ) );
//			ParseRelation population = thisRoom.getRelation( "population" );
			thisRoom.getRelation( "population" ).add( ParseUser.getCurrentUser( ) );
			thisRoom.saveInBackground( new SaveCallback( ){
					public void done( ParseException e ) {
						if( e == null ) {
							refreshRoom( );
						} else {
							Log.e( MeetSpace.TAG, "Error singing into room" );//should be in UI for retry
						}
					}
				} );
		}
	}

	private void joinRoom( ) {
		Log.i( MeetSpace.TAG, "joining room at radius " + currentRadius );
//		ParseQuery roomQuery = ParseQuery.getQuery( "Room" );
		ParseQuery.getQuery( "Room" )
		    .whereWithinKilometers( "location", userGeoPoint, MeetSpace.SEARCH_RADIUS[currentRadius] )
			.whereEqualTo( "network", network_name )
			.getFirstInBackground( new GetCallback<ParseObject>( ) {
				public void done( ParseObject room, ParseException e ) {
					// comments now contains the comments for posts without images.
					if( e == null ) {
						if( thisRoom != null ) {
//							ParseRelation population = thisRoom.getRelation( "population" );
							thisRoom.getRelation( "population" ).remove( ParseUser.getCurrentUser( ) );
						}
						thisRoom = room;
//						ParseRelation population = thisRoom.getRelation( "population" );
						thisRoom.getRelation( "population" ).add( ParseUser.getCurrentUser( ) );
						thisRoom.saveInBackground( new SaveCallback( ){
								public void done( ParseException e ) {
									if( e == null ) {
										refreshRoom( );
									} else {
										Log.e( MeetSpace.TAG, "Error singing into room" );//should be in UI for retry
									}
								}
							} );
					}
				}
			} );
	}
	private void refreshRoom( ) {
//		ParseRelation<ParseObject> relation = thisRoom.getRelation( "population" );
//		ParseQuery query = relation.getQuery( );
//		query.whereNotEqualTo( network_name + "Id", ParseUser.getCurrentUser( ).get( network_name + "Id" ) );
		thisRoom.getRelation( "population" )
			.getQuery( )
			.whereNotEqualTo( network_name + "Id", ParseUser.getCurrentUser( ).get( network_name + "Id" ) )
			.findInBackground( new FindCallback<ParseObject>( ){
			    public void done( List<ParseObject> population, ParseException e ) {
					if( e == null ) {
						roomPopulation = population.toArray( new ParseUser[population.size( )] );
						NUM_MUGS = roomPopulation.length;
						statusBar.setText( NUM_MUGS==0 ? getString(R.string.noone_home) : "" );
//						Log.i(MeetSpace.TAG, "Setting number of mugs@ " + NUM_MUGS);
						fbAdapter.notifyDataSetChanged( );
						twAdapter.notifyDataSetChanged( );
					} else {
						//something went wrong
					}
				}
			} );
	}



    //************MeetSpace Helper functions*************//
	private void setNetwork( int network ) {
		if( network == MeetSpace.FACEBOOK ) {
			this.network = MeetSpace.FACEBOOK;
			this.network_name = "facebook";
		} else if( network == MeetSpace.TWITTER ) {
			this.network_name = "twitter";
			this.network = MeetSpace.TWITTER;
		}
	}

	public int getNetwork( ) {
		return network;
	}

	private String getRoomTitle( ) {
		return "Public Room";
	}

	//*********Data display***********//
    private void updateViewsWithSelfProfileInfo( ) {
//		ParseUser currentUser = ParseUser.getCurrentUser( );
//		if( currentUser.get( "friends" ) != null ) userFriendsView.setText( Integer.toString( currentUser.getJSONArray( "friends" ).length( ) ) );
//		else userFriendsView.setText( "" );
	}

	//**********Logout Paths********//
	private void onLogoutButtonClicked( ) {
		loggingOut = true;
		if( thisRoom != null ) {
			ParseRelation population = thisRoom.getRelation( "population" );
			population.remove( ParseUser.getCurrentUser( ) );
			thisRoom.saveInBackground( );
		}
		ParseUser.logOut( );
		startLoginActivity( );
	}

	private void startLoginActivity( ) {
		Intent intent = new Intent( this, HomeActivity.class );
		intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		startActivity( intent );
	}


	private void facebookLoginButtonClicked( ) {
		loggingOut = true;
		if( thisRoom != null ) {
			ParseRelation population = thisRoom.getRelation( "population" );
			population.remove( ParseUser.getCurrentUser( ) );
			thisRoom.saveInBackground( );
		}
		ParseUser.logOut( );
		
		ViewerActivity.this.progressDialog = ProgressDialog.show(
			ViewerActivity.this, "", "Logging in...", true );
		List<String> permissions = Arrays.asList( "basic_info", "user_about_me",
												 "user_relationships", "user_birthday", "user_location" );
		ParseFacebookUtils.logIn( permissions, this, new LogInCallback( ) {
				@Override
				public void done( ParseUser user, ParseException err ) {
					ViewerActivity.this.progressDialog.dismiss( );
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

	private void twitterLoginButtonClicked( ) {
		loggingOut = true;
		if( thisRoom != null ) {
			ParseRelation population = thisRoom.getRelation( "population" );
			population.remove( ParseUser.getCurrentUser( ) );
			thisRoom.saveInBackground( );
		}
		ParseUser.logOut( );
		ViewerActivity.this.progressDialog = ProgressDialog.show( ViewerActivity.this, "", "Logging in...", true );
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
	
	@Override
    public void onBackPressed( ) {
        if( pager.getCurrentItem( ) == 0 ) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            onLogoutButtonClicked();
        } else {
            // Otherwise, select the previous step.
            pager.setCurrentItem( pager.getCurrentItem( ) - 1 );
        }
    }

	private void showUserDetailsActivity( int network ) {
		Bundle bundle = new Bundle();
		bundle.putInt("network", network);
		Intent intent = new Intent( this, ViewerActivity.class );
		intent.putExtras(bundle);
		startActivity( intent );
	}

	//***********Location Functions ********************//
	private ParseGeoPoint geoPointFromLocation( Location loc ) {return new ParseGeoPoint( loc.getLatitude( ), loc.getLongitude( ) );}

	//4 Required implementions
	public void onProviderDisabled( String string ) {}

	public void onStatusChanged( String string, int i, Bundle bundle ) {}

	public void onProviderEnabled( String string ) {}

	private void startPeriodicUpdates( ) {locationClient.requestLocationUpdates( locationRequest, this );}

	private void stopPeriodicUpdates( ) {locationClient.removeLocationUpdates( this );}

	private Location getLocation( ) {
		if( servicesConnected( ) ) {
			return locationClient.getLastLocation( );
		} else {
			return null;
		}
	}

	private boolean servicesConnected( ) {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this );

		// If Google Play services is available
		if( ConnectionResult.SUCCESS == resultCode ) {
			if( MeetSpace.APPDEBUG ) {
				// In debug mode, log the status
				Log.d( MeetSpace.TAG, "Google play services available" );
			}
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog( resultCode, this, 0 );
			if( dialog != null ) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment( );
				errorFragment.setDialog( dialog );
				//errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
			}
			return false;
		}
	}

	public void onConnected( Bundle bundle ) {
		if( MeetSpace.APPDEBUG ) {
			Log.d( "Connected to location services", MeetSpace.TAG );
		}
		currentLocation = getLocation( );
		startPeriodicUpdates( );
	}

	public void onDisconnected( ) {
		if( MeetSpace.APPDEBUG ) {
			Log.d( "Disconnected from location services", MeetSpace.TAG );
		}
	}

	public void onConnectionFailed( ConnectionResult connectionResult ) {
		if( connectionResult.hasResolution( ) ) {
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult( this, MeetSpace.CONNECTION_FAILURE_RESOLUTION_REQUEST );

			} catch(IntentSender.SendIntentException e) {

				if( MeetSpace.APPDEBUG ) {
					// Thrown if Google Play services canceled the original PendingIntent
					Log.d( MeetSpace.TAG, "An error occurred when connecting to location services.", e );
				}
			}
		} else {

			// If no resolution is available, display a dialog to the user with the error.
			showGoogleErrorDialog( connectionResult.getErrorCode( ) );
		}
	}

	/*
	 * Report location updates to the UI.
	 */
	public void onLocationChanged( Location location ) {
		currentLocation = location;
		if( lastLocation != null
           && geoPointFromLocation( location )
           .distanceInKilometersTo( geoPointFromLocation( lastLocation ) ) < 0.01 ) {
			// If the location hasn't changed by more than 10 meters, ignore it.
			return;
		}
		lastLocation = location;
		//LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		if( !hasSetUpInitialLocation ) {
			hasSetUpInitialLocation = true;		
		}
		userGeoPoint = geoPointFromLocation( ( currentLocation == null ) ? lastLocation : currentLocation );
		lookForARoom( );
	}

	private void showGoogleErrorDialog( int errorCode ) {
		// Get the error dialog from Google Play services

		Dialog errorDialog =
			GooglePlayServicesUtil.getErrorDialog( errorCode, this,
												  MeetSpace.CONNECTION_FAILURE_RESOLUTION_REQUEST );

		// If Google Play services can provide an error dialog
		if( errorDialog != null ) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment( );

			// Set the dialog in the DialogFragment
			errorFragment.setDialog( errorDialog );

			// Show the error dialog in the DialogFragment
			errorFragment.show( getSupportFragmentManager( ), MeetSpace.TAG );
		}
	}

	//******************Adapters********************//
	private class FacebookSlidePagerAdapter extends FragmentStatePagerAdapter {
        public FacebookSlidePagerAdapter( FragmentManager fm ) {
            super( fm );
        }

        @Override
        public Fragment getItem( int position ) {
            FacebookSlidingPageFragment fragment = new FacebookSlidingPageFragment( );
			fragment.setId( roomPopulation[position].getString( network_name + "Id" ) );
			fragment.setName( roomPopulation[position].getString( "name" ) );
			return fragment;
        }

        @Override
        public int getCount( ) {
            return NUM_MUGS;
        }
    }

	private class TwitterSlidePagerAdapter extends FragmentStatePagerAdapter {
        public TwitterSlidePagerAdapter( FragmentManager fm ) {
            super( fm );
        }

        @Override
        public Fragment getItem( int position ) {
            TwitterSlidingPageFragment fragment = new TwitterSlidingPageFragment( );
			fragment.setId( roomPopulation[position].getString( network_name + "Id" ) );
			fragment.setName( roomPopulation[position].getString( "name" ) );
			fragment.setCameoURL( roomPopulation[position].getString( "cameoURL" ) );
			return fragment;
        }

        @Override
        public int getCount( ) {
		    return NUM_MUGS;
        }
    }

	//******************Fragments********************//
	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog dialog;

		public ErrorDialogFragment( ) {
			super( );
			dialog = null;
		}

		public void setDialog( Dialog newDialog ) {
			dialog = newDialog;
		}

		@Override
		public Dialog onCreateDialog( Bundle savedInstanceState ) {
			return dialog;
		}
	}
//	private void makeFriendsRequest( ) {
//		Request request = Request.newMyFriendsRequest( ParseFacebookUtils.getSession( ),
//			new Request.GraphUserListCallback( ) {
//				@Override
//				public void onCompleted( List<GraphUser> users, Response response ) {
//					if( users != null ) {
//						List<String> friends = new ArrayList<String>( );
//						for( GraphUser user : users ) {
//							friends.add( user.getId( ) );
//						}
//						ParseUser currentUser = ParseUser.getCurrentUser( );
//						currentUser.put( "friends", friends );
//						currentUser.saveInBackground( );
//						Log.e( MeetSpace.TAG, "Uploaded friends list with " + friends.size( ) + " entries" );
//
//						// Show the user info
//						updateViewsWithSelfProfileInfo( );
//					} else if( response.getError( ) != null ) {
//						if( ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_RETRY )
//                           || ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION ) ) {
//							Log.d( MeetSpace.TAG,
//								  "The facebook session was invalidated." );
//							onLogoutButtonClicked( );
//						} else {
//							Log.d( MeetSpace.TAG,
//								  "Some other error: "
//								  + response.getError( )
//								  .getErrorMessage( ) );
//						}
//					}
//				}
//			} );
//		request.executeAsync( );
//	}
//	

}
