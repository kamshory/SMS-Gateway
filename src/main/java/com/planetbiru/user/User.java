package com.planetbiru.user;

import org.json.JSONObject;

public class User {

	private String username = "";
	private String name = "";
	private String password = "";
	private String phone = "";
	private long lastActive = 0;
	private boolean blocked = false;
	private boolean active = true;

	public User() {
	}

	public User(JSONObject jsonObject) 
	{
		this.username = jsonObject.optString("username", "");
		this.name = jsonObject.optString("name", "");
		this.password = jsonObject.optString("password", "");
		this.phone = jsonObject.optString("phone", "");
		this.lastActive = jsonObject.optLong("lastActive", 0);
		this.blocked = jsonObject.optBoolean("blocked", false);
		this.active = jsonObject.optBoolean("active", true);
	}
	public User(String username, String name, String password, String phone, long lastActive, boolean blocked, boolean active)
	{
		this.username = username;
		this.name = name;
		this.password = password;
		this.phone = phone;
		this.lastActive = lastActive;
		this.blocked = blocked;
		this.active = active;
	}

	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", this.username);
		jsonObject.put("name", this.name);
		jsonObject.put("password", this.password);
		jsonObject.put("phone", this.phone);
		jsonObject.put("lastActive", this.lastActive);
		jsonObject.put("blocked", this.blocked);
		jsonObject.put("active", this.active);		
		return jsonObject;
	}
	public String toString()
	{
		return this.toJSONObject().toString(4);
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public long getLastActive() {
		return lastActive;
	}
	public void setLastActive(long lastActive) {
		this.lastActive = lastActive;
	}
	public boolean isBlocked() {
		return blocked;
	}
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	

}
