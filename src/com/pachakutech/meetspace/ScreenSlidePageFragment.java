package com.pachakutech.meetspace;
import android.support.v4.app.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import com.facebook.widget.ProfilePictureView;
import android.widget.TextView;
import android.util.*;
import com.parse.*;
import android.view.View.*;
import android.content.*;
import android.net.*;
import com.facebook.widget.*;
import com.facebook.*;
import android.widget.*;

public class ScreenSlidePageFragment extends Fragment {

	private String Id;
	private String Name;
	
	public void setName( String name ){
		Name = name;
	}

	public void setId( String id ) {
		Id = id;
	}
	
	
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		ViewGroup view = (ViewGroup) inflater.inflate( R.layout.mugs, container, false );
		ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById( R.id.profilePicture );
		TextView profileNameView = (TextView) view.findViewById( R.id.profileName );
		profileNameView.setText( Name );
		profilePictureView.setProfileId( Id );
		view.setOnClickListener( new OnClickListener( ){
				public void onClick( View v ) {
					try {
						//try to open page in facebook native app.
						String uri = "fb://page/" + Id;    //Cutsom URL
						Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( uri ) );
						startActivity( intent );   
					} catch(ActivityNotFoundException ex) {
						//facebook native app isn't available, use browser.
						String uriWeb = "http://touch.facebook.com/pages/x/" + Id;  //Normal URL  
						Intent i = new Intent( Intent.ACTION_VIEW, Uri.parse( uriWeb ) );    
						startActivity( i ); 
					}
				}
			} );
		view.setOnLongClickListener( new OnLongClickListener( ) { 
				@Override
				public boolean onLongClick( View v ) {
					sendRequestDialog( );
					return true;
				}
			} );
		return view;		
	}
	private void sendRequestDialog( ) {
		Bundle params = new Bundle();
	    params.putString("message", "Met on MeetSpace!");
		WebDialog requestsDialog = (
			new WebDialog.Builder( this.getActivity(),
									Session.getActiveSession( ),
		    "friends/?id="+Id+"&app_id="+getString(R.string.fb_app_id), params)
            .setOnCompleteListener( new completeListener( ) )
            .build( ));
		requestsDialog.show( );
	}

	class completeListener implements WebDialog.OnCompleteListener {
		@Override
		public void onComplete( Bundle values,
							   FacebookException error ) {
			if( error != null ) {
				if( error instanceof FacebookOperationCanceledException ) {
					Toast.makeText( getActivity( ).getApplicationContext( ), 
								   "Request cancelled", 
								   Toast.LENGTH_SHORT ).show( );
				} else {
					Toast.makeText( getActivity( ).getApplicationContext( ), 
								   "Network Error", 
								   Toast.LENGTH_SHORT ).show( );
				}
			} else {
				final String requestId = values.getString( "request" );
				if( requestId != null ) {
					Toast.makeText( getActivity( ).getApplicationContext( ), 
								   "Request sent",  
								   Toast.LENGTH_SHORT ).show( );
				} else {
					Toast.makeText( getActivity( ).getApplicationContext( ), 
								   "Request cancelled", 
								   Toast.LENGTH_SHORT ).show( );
				}
			}   
		}
	}
}
	

