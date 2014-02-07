package com.pachakutech.meetspace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class Utils {

	private static final String TAG = Utils.class.getSimpleName();
	private static String xyz = "987731691433d512583be7d53543786d";  //AES key created by makeAES

	public static String deString (InputStream  input) {
		String clearText = null;
		try {
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			
			byte[] ciphertext = IOUtils.toByteArray(input);
			aes.init(Cipher.DECRYPT_MODE, getKey());
			clearText = new String(aes.doFinal(ciphertext));
				Log.d(TAG,"ClearText:" + clearText);
		} catch (NoSuchAlgorithmException e) {
			Log.e (TAG, e.getMessage());
		} catch (NoSuchPaddingException e) {
			Log.e (TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e (TAG, e.getMessage());
		} catch (IllegalBlockSizeException e) {
			Log.e (TAG, e.getMessage());
		} catch (BadPaddingException e) {
			Log.e (TAG, e.getMessage());
		} catch (IOException e) {
			Log.e (TAG, e.getMessage());
		}
		return clearText;
	}

	public static void eString (OutputStream  output, String clearText) {
		try {
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aes.init(Cipher.ENCRYPT_MODE, getKey());
			byte[] ciphertext = aes.doFinal(clearText.getBytes());
			//	Log.d(TAG,"ChipherText:" + ciphertext);
			output.write(ciphertext);
		} catch (NoSuchAlgorithmException e) {
			Log.e (TAG, e.getMessage());
		} catch (NoSuchPaddingException e) {
			Log.e (TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e (TAG, e.getMessage());
		} catch (IllegalBlockSizeException e) {
			Log.e (TAG, e.getMessage());
		} catch (BadPaddingException e) {
			Log.e (TAG, e.getMessage());
		} catch (IOException e) {
			Log.e (TAG, e.getMessage());
		}
	}

	private static SecretKeySpec getKey() {
		byte[] encoded = new BigInteger(xyz, 16).toByteArray();
		SecretKeySpec key = new SecretKeySpec(encoded, 0, 16, "AES");        	
		return key;
	}


	//  one time functions used during development to create the key and some encrypted String. 
	//  these values will be stored in the app.

	public static void makeChipherKey (Context context) {
		//  Create a key and send to a file
		//  copy the string from this file (either the log file or the key file in the ext dir will do),
		//  and paste it into the value Utils.xyz 

		try {

			String passphrase = "passphrase";  //  passphrase
			MessageDigest digest = MessageDigest.getInstance("SHA");
			digest.update(passphrase.getBytes());
			SecretKeySpec key = new SecretKeySpec(digest.digest(), 0, 16, "AES"); 
			byte[] encoded = key.getEncoded();
			String keyOutput = new BigInteger(1, encoded).toString(16);
			Log.d(TAG,"ChipherKey:" + keyOutput);
			FileUtil.writeText (FileUtil.getFileInExternalFileDir("key", context), keyOutput);
		} catch (NoSuchAlgorithmException e) {
			Log.e (TAG, e.getMessage());
		}
	}

	public static void saveChipherText (Context context, String clearText) {
		//  Create a ChiperText and write it to a file
		//  this file will be part of and used in the application, for example
		//   to store an encrypted oauth client secret in your application
		//  
		//  After calling this function copy the "cipherText" file created to your assets directory in the project, 
		//   and use deString to decrypt it.

		FileOutputStream output;
		try {
			output = new FileOutputStream(new File(FileUtil.getFileInExternalFileDir("cipherText", context)));
			eString (output, clearText);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

	}

}
