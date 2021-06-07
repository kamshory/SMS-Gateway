package com.planetbiru;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.planetbiru.config.Config;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.gsm.SMSInstance;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.ws.WebSocketClient;

@RestController
public class RequestHandler {

	@Autowired
	SMSInstance smsService;
	
	@Autowired
	WebSocketClient wsClient;

	private String portName = "COM3";

	private String wsClientEndpoint = "ws://localhost:8888/ws";

	private String wsClientUsername = "qa";

	private String wsClientPassword = "4lt0@1234";

	private String sessionName;
	
	@PostConstruct
	public void init()
	{
		initConfig();		
		initSerial();
		initWSClient();
	}
	
	private void initConfig() {
		Config.setPortName(portName);
		Config.setWsClientEndpoint(wsClientEndpoint);
		Config.setWsClientUsername(wsClientUsername);
		Config.setWsClientPassword(wsClientPassword);
		Config.setSessionName(sessionName);
	}

	private void initSerial() {
		String portName = Config.getPortName();
		smsService.init(portName);
	}

	private void initWSClient() 
	{
		wsClient.setSMSService(smsService);
		wsClient.start();	
	}
	
	String username = "";
	String password = "";

	@PostMapping(path="/login/**")
	public ResponseEntity<byte[]> handleGet(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		
		Map<String, String> queryPairs = new LinkedHashMap<>();
		
		String[] pairs = requestBody.split("&");
	    for (String pair : pairs) 
	    {
	        int idx = pair.indexOf("=");
	        try 
	        {
				queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
			} 
	        catch (UnsupportedEncodingException e) 
	        {
				e.printStackTrace();
			}
	    }
	    username = queryPairs.getOrDefault("username", "");
	    password = queryPairs.getOrDefault("password", "");
	    
	    System.out.println(username);
	    System.out.println(password);
		
		String fileName = this.getFileName(request);
		byte[] responseBody = "".getBytes();
		try 
		{
			responseBody = FileUtil.read(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
		}
		CookieServer cookie = new CookieServer(headers);
		cookie.setSessionValue("username", username);
		cookie.setSessionValue("password", password);
		cookie.putToHeaders(responseHeaders);
	
		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/**")
	public ResponseEntity<byte[]> handleLogin(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		
		String fileName = this.getFileName(request);
		byte[] responseBody = "".getBytes();
		try 
		{
			responseBody = FileUtil.read(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
		}
		CookieServer cookie = new CookieServer(headers);
		String u = cookie.getSessionData().optString("username", "");
		String p = cookie.getSessionData().optString("password", "");
		System.out.println(u);
		System.out.println(p);
		cookie.putToHeaders(responseHeaders);
		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	private String getFileName(HttpServletRequest request) 
	{
		return "/static/www"+request.getServletPath();
	}
	
	

	@GetMapping(path="/api**")
	public ResponseEntity<String> handleGet2(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		return (new ResponseEntity<>("TEST = "+request.getServletPath(), responseHeaders, statusCode));	
	}

	@PostMapping(path="/api")
	public ResponseEntity<String> handleMessage(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		return this.handleMessageRequest(headers, requestBody, request);
	}

	@PostMapping(path="/config")
	public ResponseEntity<String> handleConfig(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		return this.handleConfigRequest(headers, requestBody, request);
	}

	private ResponseEntity<String> handleMessageRequest(HttpHeaders headers, String requestBody, HttpServletRequest request) 
	{
		JSONObject rresponseJSON = this.processMessageRequest(headers, requestBody, request);
		String responseBody = rresponseJSON.toString(4);
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	private ResponseEntity<String> handleConfigRequest(HttpHeaders headers, String requestBody, HttpServletRequest request) 
	{
		JSONObject rresponseJSON = this.processConfigRequest(headers, requestBody, request);
		String responseBody = rresponseJSON.toString(4);
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	private JSONObject processMessageRequest(HttpHeaders headers, String requestBody, HttpServletRequest request) {
		JSONObject requestJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString("command", "");
			if(command.equals("send-message"))
			{
				JSONArray data = requestJSON.optJSONArray("data");
				if(data != null && !data.isEmpty())
				{
					int length = data.length();
					int i;
					for(i = 0; i<length; i++)
					{
						JSONObject dt = data.getJSONObject(i);
						if(dt != null)
						{
							String receiver = dt.optString("receiver", "");
							String textMessage = dt.optString("message", "");
							this.smsService.sendSMS(receiver, textMessage);
						}
					}
				}
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return requestJSON;
	}

	private JSONObject processConfigRequest(HttpHeaders headers, String requestBody, HttpServletRequest request) 
	{
		
		JSONObject requestJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return requestJSON;
	}

}
