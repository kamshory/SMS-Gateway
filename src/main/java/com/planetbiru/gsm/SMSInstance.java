package com.planetbiru.gsm;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SMSInstance {
	private GSM gsm;
	public SMSInstance()
	{
		/**
		 * Constructor
		 */
		this.gsm = new GSM();
	}
	public boolean init(String port)
	{
		return this.gsm.initialize(port);
	}
	public void close() throws GSMNotInitalizedException {
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMNotInitalizedException("Serial port is null");
		}
		this.gsm.closePort();
	}
	public String sendSMS(String receiver, String message) throws GSMNotInitalizedException
	{
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMNotInitalizedException("Serial port is null");
		}
		return this.gsm.sendSMS(receiver, message);
	}
	public List<SMS> readSMS() throws GSMNotInitalizedException
	{
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMNotInitalizedException("Serial port is null");
		}
		return this.gsm.readSMS();
	}
}
