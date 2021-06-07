package com.planetbiru;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;

import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class CookieServer {
	private String sessionName = "SMSSESSID";
	private String sessionID = Utility.md5(System.currentTimeMillis()+"");
	private Map<String, CookieItem> cookieItem = new HashMap<>();
	private long sessionLifetime = 1440000;
	private JSONObject sessionData = new JSONObject();
	
	public CookieServer()
	{
		
	}
	public CookieServer(HttpHeaders headers)
	{
		this.parseCookie(headers);
		this.updateSessionID();
	}
	public CookieServer(HttpHeaders headers, String sessionName)
	{
		this.sessionName = sessionName;
		this.parseCookie(headers);
		this.updateSessionID();
	}
	public CookieServer(String rawCookie)
	{
		this.parseCookie(rawCookie);
		this.updateSessionID();
	}
	public CookieServer(String rawCookie, String sessionName)
	{
		this.sessionName = sessionName;
		this.parseCookie(rawCookie);
		this.updateSessionID();
	}
	private void updateSessionID() {
		if(!this.cookieItem.containsKey(this.sessionName))
		{
			CookieItem cookie = new CookieItem(this.sessionName, this.sessionID);
			this.cookieItem.put(this.sessionName, cookie);
		}
		
	}
	private void parseCookie(String rawCookie) {
		URLCodec urlCodec = new URLCodec();
		Map<String, CookieItem> list = new HashMap<>();
		String[] rawCookieParams = rawCookie.split("\\; ");
		
		for(int j = 0; j<rawCookieParams.length; j++)
		{
			String cookiePair = rawCookieParams[j];
			String[] arr = cookiePair.split("=");
	        String cookieName = arr[0];
	        String cookieValue = "";
	        try 
	        {
	        	cookieValue = urlCodec.decode(arr[1]);
	        }
	        catch (DecoderException e) 
	        {
	        	e.printStackTrace();
	        }
	        CookieItem cookie = new CookieItem(cookieName, cookieValue);
	        list.put(cookieName, cookie);
		}
		this.cookieItem = list;
		if(this.cookieItem.containsKey(this.sessionName))
		{
			this.sessionID = this.cookieItem.get(this.sessionName).getValue();
		}
		this.setSessionData(this.readSessionData());
	}
	private void parseCookie(HttpHeaders headers)
	{
		URLCodec urlCodec = new URLCodec();
		Map<String, CookieItem> list = new HashMap<>();
		List<String> rawCookies = headers.get("cookie");
		if(rawCookies != null)
		{
			for(int i = 0; i<rawCookies.size(); i++)
			{
				String rawCookie = rawCookies.get(i);
				String[] rawCookieParams = rawCookie.split("\\; ");
				
				for(int j = 0; j<rawCookieParams.length; j++)
				{
					String cookiePair = rawCookieParams[j];
					String[] arr = cookiePair.split("=");
			        String cookieName = arr[0];
			        String cookieValue = "";
			        try 
			        {
			        	cookieValue = urlCodec.decode(arr[1]);
			        }
			        catch (DecoderException e) 
			        {
			        	e.printStackTrace();
			        }
			        CookieItem cookie = new CookieItem(cookieName, cookieValue);
			        list.put(cookieName, cookie);
				}
			}
		}
		this.cookieItem = list;		
		if(this.cookieItem.containsKey(this.sessionName))
		{
			this.sessionID = this.cookieItem.get(this.sessionName).getValue();
		}
		this.setSessionData(this.readSessionData());
	}

	public void setCookieItem(Map<String, CookieItem> cookieItem) {
		this.cookieItem = cookieItem;	
	}
	public void setValue(String name, String value)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setValue(value);
			}
		}
	}
	public void setDomain(String name, String domain)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setDomain(domain);
			}
		}
	}
	public void setPath(String name, String path)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setPath(path);
			}
		}
	}
	public void setExpires(String name, Date expires)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setExpires(expires);
			}
		}
	}

	public void setSecure(String name, boolean secure)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setSecure(secure);
			}
		}
	}

	public void setHttpOnly(String name, boolean httpOnly)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setHttpOnly(httpOnly);
			}
		}
	}
	public void putToHeaders(HttpHeaders responseHeaders) {
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(this.sessionName))
			{
				((CookieItem) entry.getValue()).setExpires(new Date(System.currentTimeMillis() + this.sessionLifetime));
			}
			responseHeaders.add("Set-Cookie", ((CookieItem) entry.getValue()).toString());
		}
		this.saveSessionData();
	}
	private void saveSessionData() {
		String sessionFile = this.getSessionFile();
		try 
		{
			FileUtil.write(sessionFile, this.getSessionData().toString().getBytes());
		} 
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	private JSONObject readSessionData() {
		JSONObject jsonData = new JSONObject();
		String sessionFile = this.getSessionFile();
		try 
		{
			String text = new String(FileUtil.read(sessionFile));
			jsonData = new JSONObject(text);
		} 
		catch (FileNotFoundException e) 
		{
			/**
			 * Do nothing
			 */
		}
		return jsonData;
	}
	private String getSessionFile() {
		return "/static/session/"+this.sessionID;
	}
	public void setSessionValue(String sessionKey, Object sessionValue) {
		this.getSessionData().put(sessionKey, sessionValue);		
	}
	public Object getSessionValue(String sessionKey)
	{
		return this.getSessionData().get(sessionKey);
	}
	public JSONObject getSessionData() {
		return sessionData;
	}
	public void setSessionData(JSONObject sessionData) {
		this.sessionData = sessionData;
	}
}
