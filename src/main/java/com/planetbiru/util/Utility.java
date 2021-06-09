package com.planetbiru.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.planetbiru.cons.ConstantString;

public class Utility {
	
	private static final Logger logger = LogManager.getLogger(Utility.class);
	private Utility()
	{
		
	}

	public static Map<String, String> parseURLEncoded(String data)
	{
		Map<String, String> queryPairs = new LinkedHashMap<>();
		String[] pairs = data.split("&");
		int index = 0;
	    for (String pair : pairs) 
	    {
	        int idx = pair.indexOf("=");
	        try 
	        {
	        	String key = Utility.fixURLEncodeKey(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), index);
				queryPairs.put(key, URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
			} 
	        catch (UnsupportedEncodingException e) 
	        {
				e.printStackTrace();
			}
	        index++;
	    }
		return queryPairs;
	}

	private static String fixURLEncodeKey(String key, int index) 
	{
		return key.replace("[]", "["+index+"]");
	}

	public static List<String> asList(String input) 
	{
		List<String> list = new ArrayList<>();
		list.add(input);
		return list;
	}
	
	/**
	 * Get current time with specified format
	 * @return Current time with format yyyy-MM-dd
	 */
	public static String now()
	{
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Get current time with specified format
	 * @param precission Decimal precision
	 * @return Current time with format yyyy-MM-dd
	 */
	public static String now(int precission)
	{
		if(precission > 6)
		{
			precission = 6;
		}
		if(precission < 0)
		{
			precission = 0;
		}
		long decimal = 0;
		long nanoSecond = System.nanoTime();
		if(precission == 6)
		{
			decimal = nanoSecond % 1000000;
		}
		else if(precission == 5)
		{
			decimal = nanoSecond % 100000;
		}
		else if(precission == 4)
		{
			decimal = nanoSecond % 10000;
		}
		else if(precission == 3)
		{
			decimal = nanoSecond % 1000;
		}
		else if(precission == 2)
		{
			decimal = nanoSecond % 100;
		}
		else if(precission == 1)
		{
			decimal = nanoSecond % 10;
		}
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject)+"."+decimal;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Current time with precision and time zone
	 * @param precission Precision
	 * @param timezone Time zone
	 * @return MySQL format date time with time zone
	 */
	public static String now(int precission, String timezone)
	{
		if(precission > 6)
		{
			precission = 6;
		}
		if(precission < 0)
		{
			precission = 0;
		}
		long decimal = 0;
		long nanoSecond = System.nanoTime();
		if(precission == 6)
		{
			decimal = nanoSecond % 1000000;
		}
		else if(precission == 5)
		{
			decimal = nanoSecond % 100000;
		}
		else if(precission == 4)
		{
			decimal = nanoSecond % 10000;
		}
		else if(precission == 3)
		{
			decimal = nanoSecond % 1000;
		}
		else if(precission == 2)
		{
			decimal = nanoSecond % 100;
		}
		else if(precission == 1)
		{
			decimal = nanoSecond % 10;
		}
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
			dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject)+"."+decimal;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Current time with mili second precision
	 * @return MySQL format date time with mili second precision
	 */
	public static String now3()
	{
		String result = "";
		try
		{
			long miliSecond = System.nanoTime() % 1000;
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject)+"."+miliSecond;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Current time with micro second precision
	 * @return MySQL format date time with micro second precision
	 */
	public static String now6()
	{
		String result = "";
		try
		{
			long microSecond = System.nanoTime() % 1000000;
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject)+"."+microSecond;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Get current time with specified format
	 * @param format Time format
	 * @return Current time with specified format
	 */
	public static String now(String format)
	{
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(format);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Get current time with specified format and time zone
	 * @param format Time format
	 * @param timezone Time zone
	 * @return Current time with specified format and time zone
	 */
	public static String now(String format, String timezone)
	{
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(format);
			dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Get current time with MMddHHmmss format
	 * @return Current time with MMddHHmmss format
	 */
	public static String date10()
	{
		return now("MMddHHmmss");
	}
	/**
	 * ISO 8583 standard date time with time zone
	 * @param timezone Time zone
	 * @return ISO 8583 standard date time with time zone
	 */
	public static String date10(String timezone)
	{
		return now("MMddHHmmss", timezone);
	}
	/**
	 * Get current time with MMdd format
	 * @return Current time with MMdd format
	 */
	public static String date4()
	{
		return now("MMdd");
	}
	/**
	 * Get current time with HHmmss format
	 * @return Current time with HHmmss format
	 */
	public static String time6()
	{
		return now("HHmmss");
	}
	/**
	 * Get current time with HHmm format
	 * @return Current time with HHmm format
	 */
	public static String time4()
	{
		return now("HHmm");
	}
	/**
	 * Date time
	 * @param format Date time format
	 * @return String contains current date time
	 */
	public static String date(String format)
	{
		String result = "";
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			Date dateObject = new Date();
			result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Date time
	 * @param format Date time format
	 * @param date Date time
	 * @return String contains current date time
	 */
	public static String date(String format, Date date)
	{
		String result = "";
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			result = dateFormat.format(date);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Date time
	 * @param format Date time format
	 * @param date Date time
	 * @return String contains current date time
	 */
	public static String date(String format, Date date, String timeZone)
	{
		String result = "";
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			result = dateFormat.format(date);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Date time
	 * @param format Date time format
	 * @param time Unix Timestamp
	 * @return String contains current date time
	 */
	public static String date(String format, long time)
	{
		String result = "";
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			Date dateObject = new Date(time);
			result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Date yesterday
	 * @return Date yesterday
	 */
	public static Date yesterday() 
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	/**
	 * Date before yesterday
	 * @return Date before yesterday
	 */
	public static Date beforeYesterday() 
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -2);
	    return cal.getTime();
	}
	/**
	 * Date tomorrow
	 * @return Date tomorrow
	 */
	public static Date tomorrow()
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, +1);
	    return cal.getTime();		
	}
	/**
	 * Date tomorrow
	 * @return Date tomorrow
	 */
	public static Date nextDay(int n)
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, n);
	    return cal.getTime();		
	}
	public static Date dateBefore(String dateTime, String format, int nDay) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
	    Date date = sdf.parse(dateTime);
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.add(Calendar.DATE, -nDay);
	    return calendar.getTime();
	}
	/**
	 * Date after tomorrow
	 * @return Date after tomorrow
	 */
	public static Date afterTomorrow()
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, +2);
	    return cal.getTime();		
	}
	/**
	/**
	 * Convert array byte to string contains hexadecimal number
	 * @param b array byte
	 * @return String contains hexadecimal number
	 */
	public static String byteArrayToHexString(byte[] b) 
	{
		String result = "";
		StringBuilder str = new StringBuilder(); 
		for (int i=0; i < b.length; i++) 
		{
			str.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		result = str.toString();
		return result;
	}

	/**
	 * Generate SHA-256 hash code from a string
	 * @param input Input string
	 * @return SHA-256 hash code
	 */
	public static String sha256(String input)
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance(ConstantString.HASH_SHA256);
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			output = Utility.bytesToHex(encodedhash);
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	/**
	 * Generate SHA-256 hash code from a string with specified encoding
	 * @param input Input string
	 * @param encode Encoding used
	 * @return SHA-256 hash code
	 * @throws EncodingException if encoding is invalid
	 */
	public static String sha256(String input, String encode) throws EncodingException
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance(ConstantString.HASH_SHA256);
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			if(encode.equals(ConstantString.BASE_64))
			{
				output = Utility.base64Encode(encodedhash);
			}
			else if(encode.equals("hexa"))
			{
				output = Utility.bytesToHex(encodedhash);
			}
			else
			{
				throw new EncodingException(ConstantString.INVALID_ENCODING);
			}
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	/**
	 * Generate SHA-1 hash code from a string
	 * @param input Input string
	 * @return SHA-1 hash code
	 */
	public static String sha1(String input)
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			output = Utility.bytesToHex(encodedhash);
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	/**
	 * Generate SHA-1 hash code from a string with specified encoding
	 * @param input Input string
	 * @param encode Encoding used
	 * @return SHA-1 hash code
	 * @throws EncodingException if encoding is invalid
	 */
	public static String sha1(String input, String encode) throws EncodingException
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			if(encode.equals(ConstantString.BASE_64))
			{
				output = Utility.base64Encode(encodedhash);
			}
			else if(encode.equals("hexa"))
			{
				output = Utility.bytesToHex(encodedhash);
			}
			else
			{
				throw new EncodingException(ConstantString.INVALID_ENCODING);
			}
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	/**
	 * Generate SHA-1 with RSA hash code from a string with specified encoding
	 * @param input Input string
	 * @param encode Encoding used
	 * @return SHA-1 with RSA hash code
	 * @throws EncodingException if encoding is invalid
	 */
	public static String sha1WithRSA(String input, String encode) throws EncodingException
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			Signature instance = Signature.getInstance("SHA1withRSA");
			instance.initSign(privateKey);
			instance.update((input).getBytes());
			byte[] signature = instance.sign();			
			if(encode.equals(ConstantString.BASE_64))
			{
				output = Utility.base64Encode(signature);
			}
			else if(encode.equals("hexa"))
			{
				output = Utility.bytesToHex(signature);
			}
			else
			{
				throw new EncodingException(ConstantString.INVALID_ENCODING);
			}
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	/**
	 * Generate MD5 hash code from a string
	 * @param input Input string
	 * @return MD5 hash code
	 */
	public static String md5(String input)
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			output = Utility.bytesToHex(encodedhash);
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	/**
	 * Generate MD5 hash code from a string with specified encoding
	 * @param input Input string
	 * @param encode Encoding used
	 * @return MD5 hash code
	 * @throws EncodingException if encoding is invalid
	 */
	public static String md5(String input, String encode) throws EncodingException
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			if(encode.equals(ConstantString.BASE_64))
			{
				output = Utility.base64Encode(encodedhash);
			}
			else if(encode.equals("hexa"))
			{
				output = Utility.bytesToHex(encodedhash);
			}
			else
			{
				throw new EncodingException(ConstantString.INVALID_ENCODING);
			}
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	/**
	 * Convert byte to hexadecimal number
	 * @param hash Byte to be converted
	 * @return String containing hexadecimal number
	 */
	public static String bytesToHex(byte[] hash) 
	{
		StringBuilder hexString = new StringBuilder();
		String hex;
	    for (int i = 0; i < hash.length; i++) 
	    {
		    hex = Integer.toHexString(0xff & hash[i]);
		    if(hex.length() == 1)
		    {
		    	hexString.append('0');
		    }
	    	hexString.append(hex);
	    }
	    return hexString.toString();
	}
	
	/**
	 * Encode byte array with base 64 encoding
	 * @param input Byte array to be encoded
	 * @return Encoded string
	 */
	public static String base64Encode(byte[] input)
	{
		byte[] encodedBytes = Base64.getEncoder().encode(input);
		return new String(encodedBytes);
	}
	/**
	 * Encode string with base 64 encoding
	 * @param input String to be encoded
	 * @return Encoded string
	 */
	public static String base64Encode(String input)
	{
		byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
		return new String(encodedBytes);
	}
	/**
	 * Decode string with base 64 encoding
	 * @param input String to be decoded
	 * @return Decoded string
	 */
	public static String base64Decode(String input)
	{
		byte[] decodedBytes = Base64.getDecoder().decode(input.getBytes());
		return new String(decodedBytes);
	}
	/**
	 * Decode string with base 64 encoding
	 * @param input String to be decoded
	 * @return Decoded string
	 */
	public static byte[] base64DecodeRaw(String input)
	{
		return Base64.getDecoder().decode(input.getBytes());
	}

	/**
	 * hMac 256
	 * @param algorithm Algorithm
	 * @param data Data
	 * @param secret Password
	 * @return array byte contains hMac of data
	 * @throws IllegalArgumentException if any invalid arguments
	 * @throws NoSuchAlgorithmException if algorithm not found
	 * @throws InvalidKeyException if key is invalid
	 */
	public static byte[] hMac(String algorithm, String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException
	{
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(),"Hmac"+algorithm);
        Mac mac =  Mac.getInstance("Hmac"+algorithm);
        mac.init(keySpec);
        return mac.doFinal(data.getBytes());
    }

	public static String changeDateFormat(String oldDateString, String oldFormat, String newFormat) 
	{
		String newDateString = oldDateString;
		SimpleDateFormat sdf = new SimpleDateFormat(oldFormat);
		Date d;
		try 
		{
			d = sdf.parse(oldDateString);
			sdf.applyPattern(newFormat);
			newDateString = sdf.format(d);
		} 
		catch (ParseException e) 
		{
			logger.error(e.getMessage());
		}
		return newDateString;
	}
}
