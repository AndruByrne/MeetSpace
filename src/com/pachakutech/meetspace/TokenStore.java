package com.pachakutech.meetspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.util.Log;

import android.os.*;


public class TokenStore {
	private String token = null;
	private static final String TAG = TokenStore.class.getSimpleName();
	

	public String getToken(Context context, String tokenName) {
		if (token == null) {
			String inFile = FileUtil.getFileInExternalFileDir (tokenName, "tokens", context);
			File file = new File(inFile);
			InputStream inStream = null;
			if (file.exists()) {
				try {
					inStream = new FileInputStream(file);
					token = Utils.deString(inStream);
					FileUtil.close(inStream);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "Error getting token "+e.toString());
					FileUtil.close(inStream);
				}
			}
		}
		return token;
	}

	public void setToken(Context context, String tokenName, String token) {
		this.token = token;
		String outFile = FileUtil.getFileInExternalFileDir (tokenName, "tokens", context);
		FileUtil.mkDirIfNotExtant (outFile);
		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(outFile);
			Utils.eString(outStream, token);
			FileUtil.close(outStream);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Error saving token "+e.toString());
			e.printStackTrace();
			FileUtil.close(outStream);
		}
	}
}
