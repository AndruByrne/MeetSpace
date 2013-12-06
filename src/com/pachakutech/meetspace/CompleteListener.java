package com.pachakutech.meetspace;
import com.facebook.widget.*;
import android.os.*;
import com.facebook.*;
import android.widget.*;
import com.pachakutech.meetspace.*;

public class CompleteListener implements WebDialog.OnCompleteListener {
	@Override
	public void onComplete( Bundle values,
						   FacebookException error ) {
		if( error != null ) {
			if( error instanceof FacebookOperationCanceledException ) {
				Toast.makeText( MeetSpace.getContext(), 
							   "Request cancelled", 
							   Toast.LENGTH_SHORT ).show( );
			} else {
				Toast.makeText( MeetSpace.getContext(), 
							   "Network Error", 
							   Toast.LENGTH_SHORT ).show( );
			}
		} else {
			final String requestId = values.getString( "request" );
			if( requestId != null ) {
				Toast.makeText( MeetSpace.getContext(), 
							   "Request sent",  
							   Toast.LENGTH_SHORT ).show( );
			} else {
				Toast.makeText( MeetSpace.getContext(), 
							   "Request cancelled", 
							   Toast.LENGTH_SHORT ).show( );
			}
		}   
	}
}
