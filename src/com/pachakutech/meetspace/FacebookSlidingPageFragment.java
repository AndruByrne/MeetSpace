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
import android.webkit.*;
import android.app.AlertDialog;

public class FacebookSlidingPageFragment extends Fragment {

	private String Id;
	private String Name;

	public void setId( String id ) {
		Id = id;
	}
	public void setName( String name ) {
		Name = name;
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		ViewGroup view = (ViewGroup) inflater.inflate( R.layout.fb_mugs, container, false );
		ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById( R.id.profilePicture );
		TextView profileNameView = (TextView) view.findViewById( R.id.profileName );
		profilePictureView.setProfileId( Id );
		profileNameView.setText( Name );
		profilePictureView.setOnClickListener( new OnClickListener( ){
				public void onClick( View v ) {
					sendRequestDialog( );
				}
			} );
		profilePictureView.setOnLongClickListener( new OnLongClickListener( ) { 
				@Override
				public boolean onLongClick( View v ) {
						try {
							//try to open page in facebook native app.
							String uri = "fb://page/" + Id;    //Cutsom URL
							startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( uri ) ) );   
						} catch(ActivityNotFoundException ex) {
							//facebook native app isn't available, use browser.
							String uriWeb = "http://facebook.com/profile.php?id=" + Id;  //Normal URL  
							startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( uriWeb ) ) ); 
						}
					return true;
				}
			} );
		return view;		
	}
	private void sendRequestDialog( ) {
		String requestUri = "https://www.facebook.com/dialog/friends/?id=" +
			Id + "&app_id=" + getString( R.string.fb_app_id ) +
			"&redirect_uri=http://www.facebook.com";
		WebView webView = new WebView( this.getActivity( ) );
		webView.getSettings( ).setUserAgentString( getString( R.string.user_agent_string ) );
		webView.setWebViewClient( new WebViewClient( ){
				public boolean shouldOverrideUrlLoading( WebView view, String url ) {
					return false;
				}
			} );
		webView.loadUrl( requestUri );
		AlertDialog.Builder dialog = new AlertDialog.Builder( this.getActivity( ) );
		dialog.setView( webView );
		dialog.setPositiveButton( "Done", new DialogInterface.OnClickListener( ) {
				public void onClick( DialogInterface dialog, int which ) {
					dialog.dismiss( );
				}
			} );
		dialog.show( );
	}
}
	

