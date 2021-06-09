package com.planetbiru.config;

public class Config {
	
	private Config()
	{
		
	}

	private static String wsClientEndpoint = "ws://localhost:8888/ws";
	private static String wsClientUsername = "qa";
	private static String wsClientPassword = "4lt0@1234";
	private static String portName = "COM3";
	private static String sessionName = "SMSSESSID";
	private static long reconnectDelay = 5000;
	private static String defaultFile = "/index.html";
	private static boolean serviceEnabled = false;

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
	public static long getReconnectDelay() {
		return reconnectDelay;
	}
	public static void setReconnectDelay(long reconnectDelay) {
		Config.reconnectDelay = reconnectDelay;
	}
	public static String getDefaultFile() {
		return defaultFile;
	}
	public static void setDefaultFile(String defaultFile) {
		Config.defaultFile = defaultFile;
	}
	public static boolean isServiceEnabled() {
		return serviceEnabled;
	}
	public static void setServiceEnabled(boolean serviceEnabled) {
		Config.serviceEnabled = serviceEnabled;
	}


}
