package com.planetbiru.gsm;

public class GSMNotInitalizedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Default constructor
	 */
	public GSMNotInitalizedException() 
	{ 
		super(); 
	}
	/**
	 * Constructor with the message
	 * @param message Message
	 */
	public GSMNotInitalizedException(String message) 
	{ 
		super(message); 
	}
	/**
	 * Constructor with the message and cause
	 * @param message Message
	 * @param cause Cause
	 */
	public GSMNotInitalizedException(String message, Throwable cause) 
	{ 
		super(message, cause); 
	}
	/**
	 * Constructor with cause
	 * @param cause Cause
	 */
	public GSMNotInitalizedException(Throwable cause) 
	{ 
		super(cause); 
	}
}
