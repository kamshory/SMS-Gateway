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

public class ConfigVendorNoIP {
	private static Logger logger = LogManager.getLogger(ConfigVendorNoIP.class);
	private ConfigVendorNoIP()
	{
		
	}
	
	private static String endpoint = "";
	private static String username = "";
	private static String email = "";
	private static String password = "";
	private static String company = "";
	
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
					String lUsername = json.optString("username", "");
					String lEmail = json.optString("email", "");
					String lPassword = json.optString("password", "");
					String lCompany = json.optString("company", "");
					
					ConfigVendorNoIP.setEndpoint(lEndpoint);
					ConfigVendorNoIP.setUsername(lUsername);
					ConfigVendorNoIP.setEmail(lEmail);
					ConfigVendorNoIP.setPassword(lPassword);
					ConfigVendorNoIP.setCompany(lCompany);
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

		config.put("endpoint", ConfigVendorNoIP.getEndpoint());
		config.put("username", ConfigVendorNoIP.getUsername());
		config.put("email", ConfigVendorNoIP.getEmail());
		config.put("password", ConfigVendorNoIP.getPassword());
		config.put("company", ConfigVendorNoIP.getCompany());
		return config;
	}

	public static String getEndpoint() {
		return endpoint;
	}

	public static void setEndpoint(String endpoint) {
		ConfigVendorNoIP.endpoint = endpoint;
	}

	

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		ConfigVendorNoIP.username = username;
	}

	public static String getEmail() {
		return email;
	}

	public static void setEmail(String email) {
		ConfigVendorNoIP.email = email;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		ConfigVendorNoIP.password = password;
	}

	public static String getCompany() {
		return company;
	}

	public static void setCompany(String company) {
		ConfigVendorNoIP.company = company;
	}

	public static JSONObject toJSONObject()
	{
		return getJSONObject();
	}
	
}
