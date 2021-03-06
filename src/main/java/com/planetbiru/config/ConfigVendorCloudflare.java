package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigVendorCloudflare {
	private static Logger logger = LogManager.getLogger(ConfigVendorCloudflare.class);
	private ConfigVendorCloudflare()
	{
		
	}
	private static String endpoint = "";
	private static String accountId = "";
	private static String authEmail = "";
	private static String authApiKey = "";
	private static String authToken = "";
	
	public static void load(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONObject json = new JSONObject(text);
					String lEndpoint = json.optString("endpoint", "");
					String lAccountId = json.optString("accountId", "");
					String lAuthEmail = json.optString("authEmail", "");
					String lAuthApiKey = json.optString("authApiKey", "");
					String lAuthToken = json.optString("authToken", "");
					
					ConfigVendorCloudflare.setEndpoint(lEndpoint);
					ConfigVendorCloudflare.setAccountId(lAccountId);
					ConfigVendorCloudflare.setAuthEmail(lAuthEmail);
					ConfigVendorCloudflare.setAuthApiKey(lAuthApiKey);
					ConfigVendorCloudflare.setAuthToken(lAuthToken);
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			logger.error(e.getMessage());
			//e.printStackTrace();
		}
		
	}	

	public static void save(String path) {
		JSONObject config = getJSONObject();
		save(path, config);
	}

	public static void save(String path, JSONObject config) {
		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		prepareDir(fileName);
		
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
	

	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();

		config.put("endpoint", ConfigVendorCloudflare.getEndpoint());
		config.put("accountId", ConfigVendorCloudflare.getAccountId());
		config.put("authEmail", ConfigVendorCloudflare.getAuthEmail());
		config.put("authApiKey", ConfigVendorCloudflare.getAuthApiKey());
		config.put("authToken", ConfigVendorCloudflare.getAuthToken());
		return config;
	}

	public static String getEndpoint() {
		return endpoint;
	}

	public static void setEndpoint(String endpoint) {
		ConfigVendorCloudflare.endpoint = endpoint;
	}

	public static String getAccountId() {
		return accountId;
	}

	public static void setAccountId(String accountId) {
		ConfigVendorCloudflare.accountId = accountId;
	}

	public static String getAuthEmail() {
		return authEmail;
	}

	public static void setAuthEmail(String authEmail) {
		ConfigVendorCloudflare.authEmail = authEmail;
	}

	public static String getAuthApiKey() {
		return authApiKey;
	}

	public static void setAuthApiKey(String authApiKey) {
		ConfigVendorCloudflare.authApiKey = authApiKey;
	}

	public static String getAuthToken() {
		return authToken;
	}

	public static void setAuthToken(String authToken) {
		ConfigVendorCloudflare.authToken = authToken;
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}
	
}
