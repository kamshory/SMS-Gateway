package com.planetbiru.tools;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {

    private JSONObject messageJSON = new JSONObject();
	private String command = "";
	private String originalMessage = "";
	private boolean save = false;
	public Message(String messageReceived) {
		this.originalMessage = messageReceived;
		try {
			this.messageJSON = new JSONObject(messageReceived);
			this.setData();
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	public Message(JSONObject json) {
		this.originalMessage = json.toString(4);
		this.messageJSON = json;
		this.setData();
	}
	
	

	private void setData() {
		this.command = this.messageJSON.optString("command", "");
		this.save = this.messageJSON.optBoolean("save", false);
	}
	
	@Override
	public String toString()
	{
		return this.originalMessage;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public JSONObject getMessageJSON() {
		return messageJSON;
	}
	public void setMessageJSON(JSONObject messageJSON) {
		this.messageJSON = messageJSON;
	}
	public String getOriginalMessage() {
		return originalMessage;
	}
	public void setOriginalMessage(String originalMessage) {
		this.originalMessage = originalMessage;
	}
	public boolean isSave() {
		return save;
	}
	public void setSave(boolean save) {
		this.save = save;
	}
	
}
