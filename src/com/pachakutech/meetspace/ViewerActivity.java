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
import org.json.*;
import com.google.android.gms.common.*;
import android.support.v4.app.*;
import android.app.Dialog;
import android.app.Application;
import android.content.*;
import android.location.Location;
import com.parse.*;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.*;

public class ViewerActivity extends Activity implements LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {

	private final String TAG = getClass( ).getSimpleName( );
	private static final int TWELVE_SECONDS = 12000;
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private Location lastLocation = null;
	private Location currentLocation = null;
	private ProfilePictureView userProfilePictureView;
	private TextView userNameView;
	private TextView userLocationView;
	private TextView userGenderView;
	private TextView userDateOfBirthView;
	private TextView userRelationshipView;
	private TextView userFriendsView;
	private TextView userLatitude;
	private Button logoutButton;
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 0;
    private static final double[] SEARCH_RADIUS = {.004, .008, .016, .032, .064, .096, .2, .4, .8, 1.4, 2};
	private boolean hasSetUpInitialLocation;
	private ParseGeoPoint userGeoPoint;
	private int currentRadius = 3;
	private boolean checkedAbove;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		setContentView( R.layout.userdetails );

		userProfilePictureView = (ProfilePictureView) findViewById( R.id.userProfilePicture );
		userNameView = (TextView) findViewById( R.id.userName );
		userLocationView = (TextView) findViewById( R.id.userLocation );
		userGenderView = (TextView) findViewById( R.id.userGender );
		userDateOfBirthView = (TextView) findViewById( R.id.userDateOfBirth );
		userFriendsView = (TextView) findViewById( R.id.userFriends );
		userRelationshipView = (TextView) findViewById( R.id.userRelationship );
		userLatitude = (TextView) findViewById( R.id.userLatitude );


		logoutButton = (Button) findViewById( R.id.logoutButton );
		logoutButton.setOnClickListener( new View.OnClickListener( ) {
				@Override
				public void onClick( View v ) {onLogoutButtonClicked( );}
			} );

		// Create a new global location parameters object
		locationRequest = LocationRequest.create( );
		locationRequest.setInterval( TWELVE_SECONDS / 12 );
		locationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
		locationRequest.setFastestInterval( TWELVE_SECONDS / 120 );
		locationClient = new LocationClient( this, this, this );

		// Fetch Facebook user info if the session is active
		Session session = ParseFacebookUtils.getSession( );

		if( session != null && session.isOpened( ) ) {
			//eventually put case statement here
			getFbId( );
			makeFriendsRequest( );
		} else Log.e( TAG, "no request made" );
	}

	@Override
	public void onStart( ) {
		super.onStart( );
		locationClient.connect( );
	}

	@Override
	public void onResume( ) {
		super.onResume( );
		checkedAbove = false;
		ParseUser currentUser = ParseUser.getCurrentUser( );
		if( currentUser != null ) updateViewsWithSelfProfileInfo( );
		else startLoginActivity( );
	}

	@Override
	public void onStop( ) {
		if( locationClient.isConnected( ) ) stopPeriodicUpdates( );
		locationClient.disconnect( );
		super.onStop( );
	}

	private void getFbId( ) {
		Request request = Request.newMeRequest( ParseFacebookUtils.getSession( ),
		    new Request.GraphUserCallback( ) {
				@Override
				public void onCompleted( GraphUser user, Response response ) {
					if( user != null ) {
						//JSONObject networkId = new JSONObject( );
						//networkId.put( "facebookId", user.getId( ) );
						//networkId.put( "name", user.getName( ) );
						ParseUser currentUser = ParseUser.getCurrentUser( );
						currentUser.put( "facebookId", user.getId( ) );
						currentUser.put( "name", user.getName( ) );
						currentUser.saveInBackground( );
					} else if( response.getError( ) != null ) {
						if( ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_RETRY )
                           || ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION ) ) {
							Log.d( TAG, "The facebook session was invalidated." );
							onLogoutButtonClicked( );
						} else Log.d( TAG, "Unknown error: " + response.getError( ).getErrorMessage( ) );
					}
				}
			} );
		request.executeAsync( );
	}

	private void makeFriendsRequest( ) {
		Request request = Request.newMyFriendsRequest( ParseFacebookUtils.getSession( ),
			new Request.GraphUserListCallback( ) {
				@Override
				public void onCompleted( List<GraphUser> users, Response response ) {
					if( users != null ) {
						List<String> friends = new ArrayList<String>( );
						for( GraphUser user : users ) {
							friends.add( user.getId( ) );
						}
						ParseUser currentUser = ParseUser.getCurrentUser( );
						currentUser.put( "friends", friends );
						currentUser.saveInBackground( );
						Log.e( TAG, "Uploaded friends list with " + friends.size( ) + " entries" );

						// Show the user info
						updateViewsWithSelfProfileInfo( );
					} else if( response.getError( ) != null ) {
						if( ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_RETRY )
                           || ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION ) ) {
							Log.d( TAG,
								  "The facebook session was invalidated." );
							onLogoutButtonClicked( );
						} else {
							Log.d( TAG,
								  "Some other error: "
								  + response.getError( )
								  .getErrorMessage( ) );
						}
					}
				}
			} );
		request.executeAsync( );
	}

    //**********Room Checking, Creating, and Adding self******//
	private void lookForARoom( ) {
		userLatitude.setText( Double.toString( currentLocation.getLatitude( ) ) );

		userGeoPoint = geoPointFromLocation( ( currentLocation == null ) ? lastLocation : currentLocation );
		//to be replaced by a function
		//dialog will need meters to room center, init to true, then false as circle expands
		boolean roomJustRight = true;
		if( nearARoom( ) ) {
			if( roomJustRight ) {
				//enterRoom();
			} //else user wants to make new room anyway
		} else {
			if( null == null ) {
				makeNewRoom( );
			} else {//throw an error wrt lack of network connectivity
			}
		}
	}

	private boolean nearARoom( ) {
		ParseQuery roomQuery = ParseQuery.getQuery( "Room" );
		roomQuery.whereWithinKilometers( "location", userGeoPoint, SEARCH_RADIUS[currentRadius] );
		roomQuery.whereEqualTo( "network", getNetwork( ) );
		roomQuery.countInBackground( new CountCallback( ){
				public void done( int count, ParseException e ) {
					if( e == null ) {
						if( count == 0 && currentRadius > SEARCH_RADIUS.length - 2 ) {
							makeNewRoom( );
						} else if( count == 0 && currentRadius < SEARCH_RADIUS.length - 1 && checkedAbove == false ) {
							currentRadius++;
							nearARoom( );
						} else if( count == 0 && currentRadius < SEARCH_RADIUS.length - 1 && checkedAbove == true ) {
							currentRadius++; //inform that several rooms are available, perhaps pick?, then
							joinRoom( );
						} else if( count > 1 && currentRadius != 0 ) {
							checkedAbove = true;
							currentRadius--;
							nearARoom( );
						} else {//should give option to create new room if this radius is too big
							joinRoom( );
						}
					}
				}
			} );
		return false;
	}

	private void makeNewRoom( ) {
		if( ParseUser.getCurrentUser( ) != null ) {
			ParseObject room = new ParseObject( "Room" );
			room.put( "network", getNetwork( ) );
			room.put( "location", userGeoPoint );
			room.put( "title", getRoomTitle( ) );
			ParseRelation roomPopulation = room.getRelation( "population" );
			roomPopulation.add( ParseUser.getCurrentUser( ) );
			room.saveInBackground( );
		}
	}

	private void joinRoom( ) {
		Log.i( MeetSpace.TAG, "joining room at radius " + currentRadius );
	}



    //************MeetSpace Helper functions*************//
	private String getNetwork( ) {
		return "facebook";
	}				
	private String getRoomTitle( ) {
		return "Public Room";
	}

	//*********Data display***********//
    private void updateViewsWithSelfProfileInfo( ) {
		ParseUser currentUser = ParseUser.getCurrentUser( );
		if( currentUser.getString( "facebookId" ) != null ) userProfilePictureView.setProfileId( currentUser.get( "facebookId" ).toString( ) );
		else userProfilePictureView.setProfileId( null );
		if( currentUser.get( "name" ) != null ) userNameView.setText( currentUser.getString( "name" ) );
		else userNameView.setText( "null" );
		if( currentUser.get( "friends" ) != null ) userFriendsView.setText( Integer.toString( currentUser.getJSONArray( "friends" ).length( ) ) );
		else userFriendsView.setText( "" );
	}

//	private void makeMeRequest( ) {
//	    Request request = Request.newMeRequest( ParseFacebookUtils.getSession( ),
//			new Request.GraphUserCallback( ) {
//				@Override
//				public void onCompleted( GraphUser user, Response response ) {
//					if( user != null ) {
//						// Create a JSON object to hold the profile info
//						JSONObject userProfile = new JSONObject( );
//						// And one for the friends
//						JSONObject userFriends = new JSONObject( );
//						try {
//							// Populate the profile JSON object
//							userProfile.put( "facebookId", user.getId( ) );
//							userProfile.put( "name", user.getName( ) );
//							if( user.getLocation( ).getProperty( "name" ) != null ) {
//								userProfile.put( "location", (String) user
//												.getLocation( ).getProperty( "name" ) );
//							}
//							if( user.getProperty( "gender" ) != null ) {
//								userProfile.put( "gender",
//												(String) user.getProperty( "gender" ) );
//							}
//							if( user.getBirthday( ) != null ) {
//								userProfile.put( "birthday",
//												user.getBirthday( ) );
//							}
//							if( user.getProperty( "relationship_status" ) != null ) {
//								userProfile
//									.put( "relationship_status",
//										 (String) user
//										 .getProperty( "relationship_status" ) );
//							}
//							if( user.getProperty( "friends" ) != null ) {
//								userFriends.put( "friends",
//												(Object[]) user.getProperty( "friends" ) );
//							}
//							// Save the user profile info in a user property
//							ParseUser currentUser = ParseUser
//								.getCurrentUser( );
//							currentUser.put( "profile", userProfile );
//							currentUser.saveInBackground( );
//
//							//make friendsRequest
//							makeFriendsRequest( );
//
//
//							// Show the user info
////								updateViewsWithProfileInfo();
//						} catch(JSONException e) {
//							Log.d( TAG,
//								  "Error parsing returned user data." );
//						}
//
//					} else if( response.getError( ) != null ) {
//						if( ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_RETRY )
//                           || ( response.getError( ).getCategory( ) == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION ) ) {
//							Log.d( TAG,
//								  "The facebook session was invalidated." );
//							onLogoutButtonClicked( );
//						} else {
//							Log.d( TAG,
//								  "Some other error: "
//								  + response.getError( )
//								  .getErrorMessage( ) );
//						}
//					}
//				}
//			} );
//		request.executeAsync( );
//	}

//*****************Not long for this world********************//	
//	private void updateViewsWithProfileInfo( ) {
//		ParseUser currentUser = ParseUser.getCurrentUser( );
//		if( currentUser.get( "profile" ) != null ) {
//			JSONObject userProfile = currentUser.getJSONObject( "profile" );
//			try {
//				if( userProfile.getString( "facebookId" ) != null ) {
//					String facebookId = userProfile.get( "facebookId" )
//						.toString( );
//					userProfilePictureView.setProfileId( facebookId );
//				} else {
//					// Show the default, blank user profile picture
//					userProfilePictureView.setProfileId( null );
//				}
//				if( userProfile.getString( "name" ) != null ) {
//					userNameView.setText( userProfile.getString( "name" ) );
//				} else {
//					userNameView.setText( "" );
//				}
//				if( userProfile.getString( "location" ) != null ) {
//					userLocationView.setText( userProfile.getString( "location" ) );
//				} else {
//					userLocationView.setText( "" );
//				}
//				if( userProfile.getString( "gender" ) != null ) {
//					userGenderView.setText( userProfile.getString( "gender" ) );
//				} else {
//					userGenderView.setText( "" );
//				}
//				if( userProfile.getString( "birthday" ) != null ) {
//					userDateOfBirthView.setText( userProfile
//												.getString( "birthday" ) );
//				} else {
//					userDateOfBirthView.setText( "" );
//				}
//				if( currentUser.get( "friends" ) != null ) {
//					userFriendsView.setText( Integer.toString(
//												currentUser.getJSONArray( "friends" ).length( ) ) );
//				} else {
//					userFriendsView.setText( "no friends" );
//				}
//				if( userProfile.getString( "relationship_status" ) != null ) {
//					userRelationshipView.setText( userProfile
//												 .getString( "relationship_status" ) );
//				} else {
//					userRelationshipView.setText( "" );
//				}
//			} catch(JSONException e) {
//				Log.d( TAG,
//					  "Error parsing saved user data." );
//			}
//
//		}
//	}


	//**********Logout********//
	private void onLogoutButtonClicked( ) {
		// Log the user out
		ParseUser.logOut( );

		// Go to the login view
		startLoginActivity( );
	}

	private void startLoginActivity( ) {
		Intent intent = new Intent( this, LoginActivity.class );
		intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		startActivity( intent );
	}







	//***********Location Functions from here to end*******//
	private ParseGeoPoint geoPointFromLocation( Location loc ) {
		return new ParseGeoPoint( loc.getLatitude( ), loc.getLongitude( ) );
	}

	//4 Required implementions
	public void onProviderDisabled( String string ) {}

	public void onStatusChanged( String string, int i, Bundle bundle ) {
		// TODO: Implement this method
	}

	public void onProviderEnabled( String string ) {
		// TODO: Implement this method
	}

	private void startPeriodicUpdates( ) {
		locationClient.requestLocationUpdates( locationRequest, this );
	}

	private void stopPeriodicUpdates( ) {
		locationClient.removeLocationUpdates( this );
	}

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
				connectionResult.startResolutionForResult( this, CONNECTION_FAILURE_RESOLUTION_REQUEST );

			} catch(IntentSender.SendIntentException e) {

				if( MeetSpace.APPDEBUG ) {
					// Thrown if Google Play services canceled the original PendingIntent
					Log.d( MeetSpace.TAG, "An error occurred when connecting to location services.", e );
				}
			}
		} else {

			// If no resolution is available, display a dialog to the user with the error.
			showErrorDialog( connectionResult.getErrorCode( ) );
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
		lookForARoom( );
	}

	private void showErrorDialog( int errorCode ) {
		// Get the error dialog from Google Play services
		Dialog errorDialog =
			GooglePlayServicesUtil.getErrorDialog( errorCode, this,
												  CONNECTION_FAILURE_RESOLUTION_REQUEST );

		// If Google Play services can provide an error dialog
		if( errorDialog != null ) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment( );

			// Set the dialog in the DialogFragment
			errorFragment.setDialog( errorDialog );

			// Show the error dialog in the DialogFragment
			//	errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
		}
	}
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
}
