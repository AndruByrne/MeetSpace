package com.pachakutech.meetspace;
import android.support.v4.app.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import com.facebook.widget.ProfilePictureView;
import android.widget.TextView;
import android.widget.ImageView;
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
import android.graphics.drawable.*;
import java.io.*;
import java.net.*;
import android.graphics.*;
import android.os.*;

public class TwitterSlidingPageFragment extends Fragment {

	private String Id;
	private String Name;
	private String cameoURL;

	public void setCameoURL( String s ) {
	    cameoURL = s;
	}

	public void setId( String s ) {
		Id = s;
	}
	public void setName( String s ) {
		Name = s;
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		ViewGroup view = (ViewGroup) inflater.inflate( R.layout.tw_mugs, container, false );
		ImageView profilePictureView = (ImageView) view.findViewById( R.id.profilePicture );
		TextView profileNameView = (TextView) view.findViewById( R.id.profileName );
	    //if null in user field, can init laoding animation here
		new LoadPicture( profilePictureView ).execute(cameoURL);
			Log.i( MeetSpace.TAG, "Attempted to show image at " + cameoURL );
		//profilePictureView.setProfileId( Id );
		profileNameView.setText( Name );
		Log.i( MeetSpace.TAG, "twitter id: " + Id );
		profilePictureView.setOnClickListener( new OnClickListener( ){
				public void onClick( View v ) {
                    sendRequestDialog( );
				}
			} );
		profilePictureView.setOnLongClickListener( new OnLongClickListener( ) { 
				@Override
				public boolean onLongClick( View v ) {
					//same thing, but for twitter
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

					return true;
				}
			} );
		return view;		
	}
	private void sendRequestDialog( ) {
		//something twitter instead
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
	private class LoadPicture extends AsyncTask<String, Void, Bitmap> {
		ImageView view;

		public LoadPicture( ImageView v ) { this.view = v; }

		protected Bitmap doInBackground( String... urls ) {
			Bitmap profilePic = null;
			//may not be right
			try {
				InputStream in = new java.net.URL( urls[0] ).openStream( );
				profilePic = BitmapFactory.decodeStream( in );
			} catch(IOException e) {
				Log.e( MeetSpace.TAG, "Error: " + e.getMessage( ) );
				e.printStackTrace( );
			}
			return profilePic;
		}
		protected void onPostExecute(Bitmap profilePic){
			view.setImageBitmap(profilePic);
		}
	}
}
	

