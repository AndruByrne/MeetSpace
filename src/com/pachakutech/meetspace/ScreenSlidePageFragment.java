package com.pachakutech.meetspace;
import android.support.v4.app.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import com.facebook.widget.ProfilePictureView;
import android.widget.TextView;

public class ScreenSlidePageFragment extends Fragment {
	
	//but it's not here that we address the feilds, this fragemnt returns a view!!!
//	static ScreenSlidePageFragment newInstance( int num ) {
//		ScreenSlidePageFragment s = new ScreenSlidePageFragment( );
//		Bundle args = new Bundle( );
//		args.putInt( "num", num );
//		s.setArguments( args );
//		return s;
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		networkID = (getArguments() != null ? getArguments().getInt("num") : 1);
//	}
//	
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		
		ViewGroup rootView = (ViewGroup) inflater.inflate( R.layout.mugs, container, false );
//		userProfilePictureView = (ProfilePictureView) getView().findViewById( R.id.userProfilePicture );
//		userNameView = (TextView) getView().findViewById( R.id.userName );
		//userProfilePictureView.setProfileId
		return rootView;
	}
}
	

