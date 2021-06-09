package com.planetbiru.user;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import com.planetbiru.cons.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;

public class UserAccount {
	
	private static final Logger logger = LoggerFactory.getLogger(UserAccount.class);
	private String path = "/static/data/user/urses.json";
	
	private Map<String, User> users = new HashMap<>();
	
	public UserAccount(String userSettingPath) {
		this.path = userSettingPath;
		this.init();
	}

	public UserAccount() {
		this.init();
	}

	public void addUser(User user)
	{
		this.users.put(user.getUsername(), user);
	}
	
	public void addUser(String username, JSONObject jsonObject) 
	{
		User user = new User(jsonObject);
		this.users.put(username, user);
		
	}
	
	public void addUser(JSONObject jsonObject) {
		User user = new User(jsonObject);
		this.users.put(jsonObject.optString(JsonKey.USERNAME, ""), user);
	}	
	
	public User getUser(String username)
	{
		return this.users.getOrDefault(username, new User());
	}
	
	public void activate(String username) 
	{
		User user = this.getUser(username);
		user.setActive(true);
		this.updateUser(user);
	}
	
	public void deactivate(String username) 
	{
		User user = this.getUser(username);
		user.setActive(false);
		this.updateUser(user);
	}
	
	public void block(String username) 
	{
		User user = this.getUser(username);
		user.setBlocked(true);
		this.updateUser(user);
	}
	
	public void unblock(String username) 
	{
		User user = this.getUser(username);
		user.setBlocked(false);
		this.updateUser(user);
	}
	
	public void updateLastActive(String username) 
	{
		User user = this.getUser(username);
		user.setLastActive(System.currentTimeMillis());
		this.updateUser(user);
	}
	
	public void updateUser(User user)
	{
		this.users.put(user.getUsername(), user);
	}
	
	public void deleteUser(User user)
	{
		this.users.remove(user.getUsername());
	}
	public void deleteUser(String username) {
		this.users.remove(username);
	}
	public boolean checkUserAuth(Map<String, List<String>> headers) {
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		return this.checkUserAuth(username, password);
	}
	public boolean checkUserAuth(HttpHeaders headers)
	{
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		return this.checkUserAuth(username, password);
	}
	
	public boolean checkUserAuth(String username, String password) {
		if(username.isEmpty())
		{
			return false;
		}
		User user = this.getUser(username);
		return user.getPassword().equals(password);
	}
	
	public void init()
	{
		String dir = this.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		this.prepareDir(fileName);
		this.load(fileName);
	}
	
	private void prepareDir(String fileName) {
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
	
	private String getBaseDir()
	{
		return UserAccount.class.getResource("/").getFile();
	}
	
	public void load(String fileName)
	{
		try 
		{
			byte[] data = FileUtil.read(fileName);
			if(data != null)
			{
				String text = new String(data);
				JSONObject jsonObject = new JSONObject(text);
				Iterator<String> keys = jsonObject.keys();
				while(keys.hasNext()) {
				    String username = keys.next();
				    JSONObject user = jsonObject.optJSONObject(username);
				    this.addUser(username, user);
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			try 
			{
				FileUtil.write(fileName, "{}".getBytes());
			} 
			catch (IOException e2) 
			{
				logger.error(e2.getMessage());
			}
			logger.error(e.getMessage());
		}
	}
	
	public void save()
	{
		String fileName = this.getBaseDir() + path;
		String userData = this.toString();
		try 
		{
			FileUtil.write(fileName, userData.getBytes());
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
		}
	}
	
	public String toString()
	{
		return this.toJSONObject().toString();
	}
	
	public JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		for (Map.Entry<String, User> entry : this.users.entrySet())
		{
			String username = entry.getKey();
			JSONObject user = ((User) entry.getValue()).toJSONObject();
			json.put(username, user);
		}
		return json;
	}
	
	public String list() {
		return this.toString();
	}

}
