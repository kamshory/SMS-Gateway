package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigSMS {
	private static Logger logger = LogManager.getLogger(ConfigSMS.class);
	
	private static String connectionType = "";
	private static String smsCenter = "";
	private static int incommingInterval = 0;
	private static int timeRange = 0;
	private static int maxPerTimeRange = 0;
	private static String imei = "";
	private static String simCardPIN = "";
	private static boolean sendIncommingSMS = false;
	private static String countryCode = "";
	
	private ConfigSMS()
	{
		
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject smsSetting = new JSONObject();
		smsSetting.put(JsonKey.CONNECTION_TYPE, ConfigSMS.connectionType);
		smsSetting.put(JsonKey.SMS_CENTER, ConfigSMS.smsCenter);
		smsSetting.put(JsonKey.INCOMMING_INTERVAL, ConfigSMS.incommingInterval);
		smsSetting.put(JsonKey.TIME_RANGE, ConfigSMS.timeRange);
		smsSetting.put(JsonKey.MAX_PER_TIME_RANGE, ConfigSMS.maxPerTimeRange);
		smsSetting.put(JsonKey.IMEI, ConfigSMS.imei);
		smsSetting.put(JsonKey.SIM_CARD_PIN, ConfigSMS.simCardPIN);
		smsSetting.put(JsonKey.SEND_INCOMMING_SMS, ConfigSMS.sendIncommingSMS);
		smsSetting.put(JsonKey.COUNTRY_CODE, ConfigSMS.countryCode);
		return smsSetting;
	}
	
	public static void save(String path, JSONObject config) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSMS.prepareDir(fileName);	
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
			//e.printStackTrace();
		}
		
	}
	public static void save(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSMS.prepareDir(fileName);	
		try 
		{
			FileConfigUtil.write(fileName, ConfigSMS.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	public static void load(String path)
	{
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigSMS.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileConfigUtil.read(fileName);
		} 
		catch (FileNotFoundException e1) 
		{
			/**
			 * Do nothing
			 */
		}
		if(data != null)
		{
			String text = new String(data);
			try
			{
				JSONObject smsSetting = new JSONObject(text);
				ConfigSMS.connectionType = smsSetting.optString(JsonKey.CONNECTION_TYPE, "");
				ConfigSMS.smsCenter = smsSetting.optString(JsonKey.SMS_CENTER, "");
				ConfigSMS.imei = smsSetting.optString(JsonKey.IMEI, "");
				ConfigSMS.simCardPIN = smsSetting.optString(JsonKey.SIM_CARD_PIN, "");
				ConfigSMS.incommingInterval = smsSetting.optInt(JsonKey.INCOMMING_INTERVAL, 0);
				ConfigSMS.timeRange = smsSetting.optInt(JsonKey.TIME_RANGE, 0);
				ConfigSMS.maxPerTimeRange = smsSetting.optInt(JsonKey.MAX_PER_TIME_RANGE, 0);
				ConfigSMS.sendIncommingSMS = smsSetting.optBoolean(JsonKey.SEND_INCOMMING_SMS, false);
				ConfigSMS.countryCode = smsSetting.optString(JsonKey.COUNTRY_CODE, "");
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	private static void prepareDir(String fileName) {
		File file = new File(fileName);
		String directory1 = file.getParent();
		File file2 = new File(directory1);
		String directory2 = file2.getParent();
		
		File d1 = new File(directory1);
		File d2 = new File(directory2);	
		
		if(!d2.exists())
		{
			d2.mkdir();
		}
		if(!d1.exists())
		{
			d1.mkdir();
		}		
	}
	
	public static String getConnectionType() {
		return connectionType;
	}

	public static void setConnectionType(String connectionType) {
		ConfigSMS.connectionType = connectionType;
	}

	public static String getSmsCenter() {
		return smsCenter;
	}

	public static void setSmsCenter(String smsCenter) {
		ConfigSMS.smsCenter = smsCenter;
	}

	public static int getIncommingInterval() {
		return incommingInterval;
	}

	public static void setIncommingInterval(int incommingInterval) {
		ConfigSMS.incommingInterval = incommingInterval;
	}

	public static int getTimeRange() {
		return timeRange;
	}

	public static void setTimeRange(int timeRange) {
		ConfigSMS.timeRange = timeRange;
	}

	public static int getMaxPerTimeRange() {
		return maxPerTimeRange;
	}

	public static void setMaxPerTimeRange(int maxPerTimeRange) {
		ConfigSMS.maxPerTimeRange = maxPerTimeRange;
	}

	public static String getImei() {
		return imei;
	}

	public static void setImei(String imei) {
		ConfigSMS.imei = imei;
	}

	public static String getSimCardPIN() {
		return simCardPIN;
	}

	public static void setSimCardPIN(String simCardPIN) {
		ConfigSMS.simCardPIN = simCardPIN;
	}

	public static boolean isSendIncommingSMS() {
		return sendIncommingSMS;
	}

	public static void setSendIncommingSMS(boolean sendIncommingSMS) {
		ConfigSMS.sendIncommingSMS = sendIncommingSMS;
	}

	public static String getCountryCode() {
		return countryCode;
	}

	public static void setCountryCode(String countryCode) {
		ConfigSMS.countryCode = countryCode;
	}

	

	
}
