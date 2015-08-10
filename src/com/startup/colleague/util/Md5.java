package com.startup.colleague.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;  
import java.security.NoSuchAlgorithmException;  
/** 
 * 对外提供getMD5(String)方法 
 * 
 */  
public class Md5 {  
	public static String getMD5(String val) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(val.getBytes("UTF-8"));
			byte[] m = md5.digest();// 加密
			
			return getString(m);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("Huh, UTF-8 should be supported?", e);
	    }
		
		return null;
	}

	private static String getString(byte[] hash) {
		StringBuilder hex = new StringBuilder(hash.length * 2);
	    for (byte b : hash) {
	        if ((b & 0xFF) < 0x10) hex.append("0");
	        hex.append(Integer.toHexString(b & 0xFF));
	    }
	    return hex.toString();
	}  
} 
