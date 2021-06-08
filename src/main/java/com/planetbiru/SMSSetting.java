package com.planetbiru;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;

public class SMSSetting {
	private String connectionType = "";
	private String smsCenter = "";
	private int incommingInterval = 0;
	private int timeRange = 0;
	private int maxPerTimeRange = 0;
	
	public JSONObject toJSONObject()
	{
		JSONObject smsSetting = new JSONObject();
		smsSetting.put("connectionType", this.connectionType);
		smsSetting.put("smsCenter", this.smsCenter);
		smsSetting.put("incommingInterval", this.incommingInterval);
		smsSetting.put("timeRange", this.timeRange);
		smsSetting.put("maxPerTimeRange", this.maxPerTimeRange);
		return smsSetting;
	}
	
	public String toString()
	{
		return this.toJSONObject().toString();
	}
	
	public void save(String path) {
		String fileName = this.getBaseDir() + path;
		this.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, this.toString().getBytes());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void load(String path)
	{
		String fileName = this.getBaseDir() + path;
		this.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileUtil.read(fileName);
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
				this.connectionType = smsSetting.optString("connectionType", "");
				this.smsCenter = smsSetting.optString("smsCenter", "");
				this.incommingInterval = smsSetting.optInt("incommingInterval", 0);
				this.timeRange = smsSetting.optInt("timeRange", 0);
				this.maxPerTimeRange = smsSetting.optInt("maxPerTimeRange", 0);
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
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

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public String getSmsCenter() {
		return smsCenter;
	}

	public void setSmsCenter(String smsCenter) {
		this.smsCenter = smsCenter;
	}

	public int getIncommingInterval() {
		return incommingInterval;
	}

	public void setIncommingInterval(int incommingInterval) {
		this.incommingInterval = incommingInterval;
	}

	public int getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(int timeRange) {
		this.timeRange = timeRange;
	}

	public int getMaxPerTimeRange() {
		return maxPerTimeRange;
	}

	public void setMaxPerTimeRange(int maxPerTimeRange) {
		this.maxPerTimeRange = maxPerTimeRange;
	}

	
	
	
}
