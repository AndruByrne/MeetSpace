package com.pachakutech.meetspace;
import java.io.*;
import android.nfc.*;
import android.util.*;
import android.text.format.*;
import android.content.*;
import android.os.*;
import org.apache.commons.io.*;

public class FileUtil
{
	private static final String TAG = FileUtil.class.getSimpleName();
	
	public static void writeText( String fileInExternalCacheDir, String keyOutput ) {
		File outFile = new File(fileInExternalCacheDir);
		if( outFile != null ){
			try {
				OutputStream out = new FileOutputStream( outFile, false );
				try {
					out.write( IOUtils.toByteArray( keyOutput ) );
					out.flush(); 
					out.close(); 
					out = null; 
				} catch(IOException e) {Log.e(TAG, "error in writing to file "+e.toString());}
			} catch(FileNotFoundException e) {Log.e(TAG, "error opening file "+e.toString());}
			
		}
	}
	
	public static String getFileInExternalCacheDir( String file, String subDir, Context context ) {
		
		String string = context.getExternalCacheDir().toString()+"/"+subDir+"/"+file;
		Log.i(TAG, "File named: " + string);
		return string;
	}

	public static String getFileInExternalCacheDir( String file, Context context ) {
		String string = context.getExternalCacheDir().toString()+"/"+file;
		Log.i(TAG, "File named: " + string);
		return string;
	}
	
	public static void close( OutputStream outStream ) {
		try {
			outStream.close( );
		} catch(IOException e) {
			Log.e(TAG, "error opening output stream "+e.toString());
		}}

	public static void close(InputStream inStream){
		try {
			inStream.close( );
		} catch(IOException e) {
			Log.e(TAG, "error opening input stream "+e.toString());
		}
	}
	
	public static boolean mkDirIfNotExtant(String dirName)
	{
	    File dir = new File(dirName + "/");
	
		if( !dir.exists( ) ) 
		{
			try
			{
				dir.mkdirs();
			    return true;
			}
			catch (SecurityException e)
			{
				Log.e(TAG, "unable to write on the sd card " + e.toString());
				return false;
			}
		}
		else return true;
	}
}
