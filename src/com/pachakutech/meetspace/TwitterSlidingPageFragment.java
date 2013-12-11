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
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.util.*;
import org.apache.http.impl.client.*;
import com.google.gson.*;

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
		new LoadPicture( profilePictureView ).execute( cameoURL );
		profileNameView.setText( Name );

        profilePictureView.setOnClickListener( new OnClickListener( ){
				public void onClick( View v ) {
                    new FriendOnTwitter().execute();
				}
		    } );
        profilePictureView.setOnLongClickListener( new OnLongClickListener( ) { 
				@Override
				public boolean onLongClick( View v ) {
					//same thing, but for twitter
					try {
						//try to open page in twitter native app.
						String uri = "twitter://user?user_id=" + Id;    //Cutsom URL
						startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( uri ) ) );   
					} catch(ActivityNotFoundException ex) {
						//twitter native app isn't available, use browser.
						String uriWeb = "http://twitter.com/intent/user?user_id=" + Id;  //Normal URL  
						Intent i = new Intent( Intent.ACTION_VIEW, Uri.parse( uriWeb ) );    
						startActivity( i ); 
					}
					return true;
				}
			} );
		return view;		
	}
    
    private class FriendOnTwitter extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground( Void[] v ) {

            String response = "";
            final HttpClient client = new DefaultHttpClient( );
            HttpPost verifyPost = new HttpPost(
                "https://api.twitter.com/1.1/friendships/create.json?user_id="+Id+"&follow=true" );
            ParseTwitterUtils.getTwitter( ).signRequest( verifyPost );
            try { response = EntityUtils.toString( client.execute( verifyPost ).getEntity( ) );
            } catch(IOException e) {Log.e( MeetSpace.TAG, "Twitter error: " + e.toString( ) );}
            return response;
        }

        @Override
        protected void onPostExecute( String response ) {
            final JsonObject jsonObj = (new JsonParser().parse(response)).getAsJsonObject();
			Log.i(MeetSpace.TAG, "response: " + response);
			Log.i(MeetSpace.TAG, "response: " + jsonObj.get( "errors" ).getAsJsonArray().toString());
			boolean checked = jsonObj.getAsJsonArray("error") == null ? successfulNotice() : responseError(jsonObj);
			//above works, this needs to go
			Log.i(MeetSpace.TAG, "unable to follow, error: " + jsonObj.getAsJsonArray( "error" ).getAsString());
            if( !checked ) Log.i( MeetSpace.TAG, "Uncaught error: " + response );
        }
		
		private boolean successfulNotice(){
            Toast.makeText(MeetSpace.getContext(), "Successfully following this person; hold down on their picture to see their profile", Toast.LENGTH_LONG).show();
			return true;
		}
		
		private boolean responseError(JsonObject jsonObj){
			Log.i(MeetSpace.TAG, "unable to follow, error: " + jsonObj.getAsJsonArray( "error" ).getAsJsonObject().get("message").getAsString());
//            Log.i( MeetSpace.TAG, "response id: " + jsonObj.get("id_str").getAsString() + " response name: " + jsonObj.get("screen_name") + "cameo URL: " +jsonObj.get("profile_image_url")  );
			return true;
		}
	}
    
//	private void sendRequestDialog( ) {
//		//something twitter instead
//		String requestUri = "https://www.facebook.com/dialog/friends/?id=" +
//			Id + "&app_id=" + getString( R.string.fb_app_id ) +
//			"&redirect_uri=http://www.facebook.com";
//		WebView webView = new WebView( this.getActivity( ) );
//		webView.getSettings( ).setUserAgentString( getString( R.string.user_agent_string ) );
//		webView.setWebViewClient( new WebViewClient( ){
//				public boolean shouldOverrideUrlLoading( WebView view, String url ) {
//					return false;
//				}
//			} );
//		webView.loadUrl( requestUri );
//		AlertDialog.Builder dialog = new AlertDialog.Builder( this.getActivity( ) );
//		dialog.setView( webView );
//		dialog.setPositiveButton( "Done", new DialogInterface.OnClickListener( ) {
//
//				public void onClick( DialogInterface dialog, int which ) {
//
//					dialog.dismiss( );
//				}
//			} );
//		dialog.show( );
//	}
	
	private class LoadPicture extends AsyncTask<String, Void, Bitmap> {
		ImageView view;

		public LoadPicture( ImageView v ) { this.view = v; }

		protected Bitmap doInBackground( String... urls ) {
			Bitmap profilePic = null;
			try {
				InputStream in = new java.net.URL( urls[0] ).openStream( );
				profilePic = BitmapFactory.decodeStream( in );
			} catch(IOException e) {
				Log.e( MeetSpace.TAG, "Error: " + e.getMessage( ) );
				e.printStackTrace( );
			}
			return profilePic;
		}
		protected void onPostExecute( Bitmap profilePic ) {
			view.setImageBitmap( profilePic );
		}
	}
}

