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
	
	public void setId( String id ) {
		Id = id;
	}
	public void setName( String name ){
		Name = name;
	}
	
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		ViewGroup view = (ViewGroup) inflater.inflate( R.layout.mugs, container, false );
		ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById( R.id.profilePicture );
		TextView profileNameView = (TextView) view.findViewById( R.id.profileName );
		profilePictureView.setProfileId( Id );
		profileNameView.setText( Name );
		profilePictureView.setOnClickListener( new OnClickListener( ){
				public void onClick( View v ) {
					try {
						//try to open page in facebook native app.
						String uri = "fb://page/" + Id;    //Cutsom URL
						Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( uri ) );
						startActivity( intent );   
					} catch(ActivityNotFoundException ex) {
						//facebook native app isn't available, use browser.
						String uriWeb = "http://facebook.com/profile.php?id=" + Id;  //Normal URL  
						Intent i = new Intent( Intent.ACTION_VIEW, Uri.parse( uriWeb ) );    
						startActivity( i ); 
					}
				}
			} );
		profilePictureView.setOnLongClickListener( new OnLongClickListener( ) { 
				@Override
				public boolean onLongClick( View v ) {
					sendRequestDialog( );
					return true;
				}
			} );
		return view;		
	}
	private void sendRequestDialog( ) {
//		String requestUrl = "https://www.facebook.com/dialog/friends/?id="+
//		     Id+"&app_id="+getString(R.string.fb_app_id)+"&redirect_uri=http://www.facebook.com";
//		WebDialog requestDialog = new WebDialog(this.getActivity(), requestUrl);
//		requestDialog.show();
//		CompleteListener listener = new CompleteListener();
//		FacebookFriendsPatch friendsPatch = new FacebookFriendsPatch(getString(R.string.fb_app_id));
		Bundle params = new Bundle();
		params.putString( "id", Id );
//        friendsPatch.dialog(this.getActivity(), "friends/", params, listener);
		WebDialog requestsDialog = (
			new WebDialog.Builder( this.getActivity(),
								  getString(R.string.fb_app_id),
		    					  "friends/", params)
            .setOnCompleteListener( new CompleteListener( ) )
            .build( ));
		requestsDialog.show( );
		
	}

	
}
	

