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
						String uri = getString(R.string.twitter_native_URI) + Id;    //Cutsom URL
						startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( uri ) ) );   
					} catch(ActivityNotFoundException ex) {
						//twitter native app isn't available, use browser.
						String uriWeb = getString(R.string.twitter_browser_URI) + Id;  //Normal URL  
						startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( uriWeb ) ) ); 
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
            HttpPost followPost = new HttpPost(
                getString(R.string.follow_html)+
				Id+getString(R.string.follow_true) );
            ParseTwitterUtils.getTwitter( ).signRequest( followPost );
            try { response = EntityUtils.toString( client.execute( followPost ).getEntity( ) );
            } catch(IOException e) {Log.e( MeetSpace.TAG, "Twitter error: " + e.toString( ) );}
            return response;
        }

        @Override
        protected void onPostExecute( String response ) {
            final JsonObject jsonObj = (new JsonParser().parse(response)).getAsJsonObject();
		//	Log.i(MeetSpace.TAG, "response: " + jsonObj.get( "errors" ).getAsJsonArray().toString());
			String toastText = jsonObj.get(getString(R.string.twitter_errors)).getAsJsonArray() == null ? getString(R.string.twitter_follow_success) : responseError(jsonObj);
			Toast.makeText(MeetSpace.getContext(), toastText, Toast.LENGTH_LONG).show();
        }
		
		private String responseError(JsonObject jsonObj){
			String errorMessage = "";
			final JsonArray jsonArray = jsonObj.get( getString(R.string.twitter_errors) ).getAsJsonArray();
			for( final JsonElement jsonElem : jsonArray ){
				final JsonObject errorObj = jsonElem.getAsJsonObject();
				errorMessage += "Error: " + errorObj.get( "message" ).getAsString() + " ";				
			}
			return errorMessage;
		}
	}
	
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

