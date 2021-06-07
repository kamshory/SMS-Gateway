package com.planetbiru.config;

public class Config {
	
	private Config()
	{
		
	}

	public static String getPortName() {
		return portName;
	}
	public static void setPortName(String portName) {
		Config.portName = portName;
	}

	public static String getWsClientEndpoint() {
		return wsClientEndpoint;
	}

	public static void setWsClientEndpoint(String wsClientEndpoint) {
		Config.wsClientEndpoint = wsClientEndpoint;
	}

	public static String getWsClientUsername() {
		return wsClientUsername;
	}

	public static void setWsClientUsername(String wsClientUsername) {
		Config.wsClientUsername = wsClientUsername;
	}

	public static String getWsClientPassword() {
		return wsClientPassword;
	}

	public static void setWsClientPassword(String wsClientPassword) {
		Config.wsClientPassword = wsClientPassword;
	}

	public static String getSessionName() {
		return sessionName;
	}

	public static void setSessionName(String sessionName) {
		Config.sessionName = sessionName;
	}

	private static String wsClientEndpoint = "ws://localhost:8888/ws?session=1";
	private static String wsClientUsername = "qa";
	private static String wsClientPassword = "4lt0@1234";
	private static String portName = "COM3";
	private static String sessionName = "SMSSESSID";

}