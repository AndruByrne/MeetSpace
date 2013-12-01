package com.pachakutech.meetspace;
import com.parse.*;
import android.graphics.*;
import java.util.*;

@ParseClassName("Rooms")
public class Room extends ParseObject {

	public Integer getNetwork(){
		return getInt("network");
	}
	
	public void setNetwork(Integer integer){
		put("network", integer);
	}
	
	public String getTitle(){
		return getString("title");
	}
	
	public void setTitle(String string){
		put("title", string);
	}
	
	public ParseGeoPoint getRoom(){
		return getParseGeoPoint("locaton");
	}
	
	public void makeRoom(ParseGeoPoint point){
		put("location", point);
	}
}
