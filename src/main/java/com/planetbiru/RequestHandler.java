package com.planetbiru;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.planetbiru.config.Config;
import com.planetbiru.config.ServerConfig;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.gsm.SMSInstance;
import com.planetbiru.user.User;
import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.ws.WebSocketClient;

@RestController
public class RequestHandler {

	@Autowired
	SMSInstance smsService;
	
	@Autowired
	WebSocketClient wsClient;
	
	@Autowired
	UserAccount userAccount;

	private String portName = "COM3";

	private String wsClientEndpoint = "ws://localhost:8888/ws";

	private String wsClientUsername = "qa";

	private String wsClientPassword = "4lt0@1234";

	private String sessionName;
	
	private ServerConfig mime = new ServerConfig();
	
	@PostConstruct
	public void init()
	{
		initConfig();
		if(Config.isServiceEnabled())
		{
			initSerial();
			initWSClient();
		}
		try 
		{
			mime = new ServerConfig("/static/config/config.ini");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	@PreDestroy
	public void destroy()
	{
		wsClient.stopService();
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
	
	@PostMapping(path="/login.html")
	public ResponseEntity<byte[]> handleLogin(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		
		Map<String, String> queryPairs = this.parseURLEncoded(requestBody);
	    
	    String username = queryPairs.getOrDefault("username", "");
	    String password = queryPairs.getOrDefault("password", "");
	    String next = queryPairs.getOrDefault("next", "");
	    
	    if(next.isEmpty())
		{
	    	next = "/index.html";
		}
		responseHeaders.add("Cache-Control", "no-cache");
	    responseHeaders.add("Content-type", "application/json");
	    
	    JSONObject res = new JSONObject();
	    JSONObject payload = new JSONObject();
	    payload.put("nextURL", next);
	    res.put("code", 0);
	    res.put("payload", payload);
	    
		cookie.setSessionValue("username", username);
		cookie.setSessionValue("password", password);
		if(userAccount.checkUserAuth(username, password))
		{
			userAccount.updateLastActive(username);
			userAccount.save();
		}
		
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		byte[] responseBody = res.toString().getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/logout.html")
	public ResponseEntity<byte[]> handleLogout(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		
		byte[] responseBody = "".getBytes();
		cookie.destroySession();
		cookie.putToHeaders(responseHeaders);

		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		responseHeaders.add("Cache-Control", "no-cache");
		responseHeaders.add("Location", "/index.html");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/user/list")
	public ResponseEntity<byte[]> handleUserList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		if(this.checkUserAuth(headers))
		{
			String list = userAccount.list();
			responseBody = list.getBytes();
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;			
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add("Content-type", "application/json");
		responseHeaders.add("Cache-Control", "no-cache");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	@GetMapping(path="/account/self")
	public ResponseEntity<byte[]> handleSelfAccount(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		if(this.checkUserAuth(headers))
		{
			String loggedUsername = (String) cookie.getSessionValue("username", "");
			String list = userAccount.getUser(loggedUsername).toString();
			responseBody = list.getBytes();
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;			
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add("Content-type", "application/json");
		responseHeaders.add("Cache-Control", "no-cache");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	@GetMapping(path="/user/detail/{username}")
	public ResponseEntity<byte[]> handleUserGet(@RequestHeader HttpHeaders headers, @PathVariable(value="username") String username, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		if(this.checkUserAuth(headers))
		{
			String data = userAccount.getUser(username).toString();
			responseBody = data.getBytes();
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;			
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add("Content-type", "application/json");
		responseHeaders.add("Cache-Control", "no-cache");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@PostMapping(path="/user/add**")
	public ResponseEntity<byte[]> userAdd(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(this.checkUserAuth(headers))
		{
			Map<String, String> queryPairs = this.parseURLEncoded(requestBody);		
		    String username = queryPairs.getOrDefault("username", "");
		    String password = queryPairs.getOrDefault("password", "");
		    String name = queryPairs.getOrDefault("name", "");
		    String phone = queryPairs.getOrDefault("phone", "");
	
		    JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", username);
			jsonObject.put("name", name);
			jsonObject.put("password", password);
			jsonObject.put("phone", phone);
			jsonObject.put("blocked", false);
			jsonObject.put("active", true);
			
			if(!username.isEmpty())
			{
				userAccount.addUser(new User(jsonObject));		
				userAccount.save();
			}		    
		}
		responseHeaders.add("Location", "../../admin.html");
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add("Cache-Control", "no-cache");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/user/update**")
	public ResponseEntity<byte[]> userUpdate(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(this.checkUserAuth(headers))
		{
			Map<String, String> queryPairs = this.parseURLEncoded(requestBody);				
		    String username = queryPairs.getOrDefault("username", "");
		    String password = queryPairs.getOrDefault("password", "");
		    String name = queryPairs.getOrDefault("name", "");
		    String phone = queryPairs.getOrDefault("phone", "");
		    boolean blocked = queryPairs.getOrDefault("blocked", "").equals("1");
		    boolean active = queryPairs.getOrDefault("active", "").equals("1");
	
		    JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", username);
			jsonObject.put("name", name);
			jsonObject.put("phone", phone);
			jsonObject.put("blocked", blocked);
			jsonObject.put("active", active);
			if(!username.isEmpty())
			{
				jsonObject.put("username", username);
			}
			if(!password.isEmpty())
			{
				jsonObject.put("password", password);
			}
			userAccount.updateUser(new User(jsonObject));		
			userAccount.save();
		    
		}
		responseHeaders.add("Location", "../../admin.html");
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add("Cache-Control", "no-cache");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/user/remove**")
	public ResponseEntity<byte[]> userRemove(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(this.checkUserAuth(headers))
		{			
			Map<String, String> queryPairs = this.parseURLEncoded(requestBody);			
		    String username = queryPairs.getOrDefault("username", "");

		    userAccount.deleteUser(username);		
			userAccount.save();
		}
		responseHeaders.add("Location", "../../admin.html");
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add("Cache-Control", "no-cache");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}	
	
	private Map<String, String> parseURLEncoded(String data)
	{
		Map<String, String> queryPairs = new LinkedHashMap<>();
		String[] pairs = data.split("&");
		int index = 0;
	    for (String pair : pairs) 
	    {
	        int idx = pair.indexOf("=");
	        try 
	        {
	        	String key = this.fixURLEncodeKey(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), index);
				queryPairs.put(key, URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
			} 
	        catch (UnsupportedEncodingException e) 
	        {
				e.printStackTrace();
			}
	        index++;
	    }
		return queryPairs;
	}

	private String fixURLEncodeKey(String key, int index) 
	{
		return key.replace("[]", "["+index+"]");
	}

	@GetMapping(path="/**")
	public ResponseEntity<byte[]> handleDocumentRootGet(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{		
		return this.serveDocumentRoot(headers, request);
	}
	@PostMapping(path="/**")
	public ResponseEntity<byte[]> handleDocumentRootPost(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		this.processFeedbackPost(headers, requestBody, request);
		return this.serveDocumentRoot(headers, request);
	}
	
	public ResponseEntity<byte[]> serveDocumentRoot(HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		
		String fileName = this.getFileName(request);
		byte[] responseBody = "".getBytes();
		try 
		{
			responseBody = FileUtil.readResource(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
		}
		CookieServer cookie = new CookieServer(headers);
		
		WebContent newContent = this.updateContent(fileName, responseHeaders, responseBody, statusCode, cookie);	
		
		responseBody = newContent.getResponseBody();
		responseHeaders = newContent.getResponseHeaders();
		statusCode = newContent.getStatusCode();
		String contentType = this.getMIMEType(fileName);
		
		responseHeaders.add("Content-type", contentType);
		if(fileName.endsWith(".html"))
		{
			cookie.saveSessionData();
		}
		cookie.putToHeaders(responseHeaders);
		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	private void processFeedbackPost(HttpHeaders headers, String requestBody, HttpServletRequest request) 
	{
		System.out.println(request.getServletPath());
		System.out.println(requestBody);
		if(this.checkUserAuth(headers))
		{
			CookieServer cookie = new CookieServer(headers);
			String path = request.getServletPath();
			if(path.equals("/admin.html"))
			{
				this.processAdmin(headers, requestBody, request, cookie);
			}
			if(path.equals("/account-update.html"))
			{
				this.processAccount(headers, requestBody, request, cookie);
			}
			if(path.equals("/sms.html"))
			{
				this.processSMS(headers, requestBody, request, cookie);
			}
		}
	}
	
	private void processSMS(HttpHeaders headers, String requestBody, HttpServletRequest request, CookieServer cookie) {
		Map<String, String> query = this.parseURLEncoded(requestBody);
		if(query.containsKey("send"))
		{
			String receiver = query.getOrDefault("receiver", "");			
			String message = query.getOrDefault("message", "");		
			smsService.sendSMS(receiver, message);
		}		
	}
	
	private void processAccount(HttpHeaders headers, String requestBody, HttpServletRequest request, CookieServer cookie) {
		Map<String, String> query = this.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue("username", "");
		String phone = query.getOrDefault("phone", "");
		String password = query.getOrDefault("password", "");
		String name = query.getOrDefault("name", "");
		if(query.containsKey("update"))
		{
			User user = userAccount.getUser(loggedUsername);
			user.setName(name);
			user.setPhone(phone);
			if(!password.isEmpty())
			{
				user.setPassword(password);
			}
			userAccount.updateUser(user);
			userAccount.save();
		}		
	}
	
	private void processAdmin(HttpHeaders headers, String requestBody, HttpServletRequest request, CookieServer cookie) {
		Map<String, String> query = this.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue("username", "");
		if(query.containsKey("delete"))
		{
			/**
			 * Delete
			 */
			for (Map.Entry<String, String> entry : query.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					userAccount.deleteUser(value);
				}
			}
			userAccount.save();
		}
		if(query.containsKey("deactivate"))
		{
			/**
			 * Deactivate
			 */
			for (Map.Entry<String, String> entry : query.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					userAccount.deactivate(value);
				}
			}
			userAccount.save();
		}
		if(query.containsKey("activate"))
		{
			/**
			 * Activate
			 */
			for (Map.Entry<String, String> entry : query.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					userAccount.activate(value);
				}
			}
			userAccount.save();
		}
		if(query.containsKey("block"))
		{
			/**
			 * Block
			 */
			for (Map.Entry<String, String> entry : query.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					userAccount.block(value);
				}
			}
			userAccount.save();
		}
		if(query.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			for (Map.Entry<String, String> entry : query.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					userAccount.unblock(value);
				}
			}
			userAccount.save();
		}
		if(query.containsKey("update-data"))
		{
			String pkID = query.getOrDefault("pk_id", "");
			String field = query.getOrDefault("field", "");
			String value = query.getOrDefault("value", "");
			if(!field.equals("username"))
			{
				User user = userAccount.getUser(pkID);
				if(field.equals("phone"))
				{
					user.setPhone(value);
				}
				if(field.equals("name"))
				{
					user.setName(value);
				}
				userAccount.updateUser(user);
				userAccount.save();
			}
		}
	}
	
	
	private String getMIMEType(String fileName) 
	{
		String[] arr = fileName.split("\\.");	
		String ext = arr[arr.length - 1];
		return 	mime.getString("MIME", ext, "");
	}

	private WebContent updateContent(String fileName, HttpHeaders responseHeaders, byte[] responseBody, HttpStatus statusCode, CookieServer cookie) 
	{
		String contentType = this.getMIMEType(fileName);
		WebContent webContent = new WebContent(fileName, responseHeaders, responseBody, statusCode, cookie, contentType);
		boolean requireLogin = false;
		String fileSub = "";
		
		if(fileName.toLowerCase().endsWith(".html"))
		{
			JSONObject authFileInfo = this.processAuthFile(responseBody);
			requireLogin = authFileInfo.optBoolean("content", false);
			fileSub = this.getFileName(authFileInfo.optString("data-file", ""));
		}
		
		String username = cookie.getSessionData().optString("username", "");
		String password = cookie.getSessionData().optString("password", "");
		if(requireLogin)
		{
			responseHeaders.add("Cache-Control", "no-cache");
			webContent.setResponseHeaders(responseHeaders);
			if(!userAccount.checkUserAuth(username, password))	
			{
				try 
				{
					responseBody = FileUtil.readResource(fileSub);
					return this.updateContent(fileSub, responseHeaders, responseBody, statusCode, cookie);
				} 
				catch (FileNotFoundException e) 
				{
					statusCode = HttpStatus.NOT_FOUND;
					webContent.setStatusCode(statusCode);
				}	
			}
		}
		return webContent;
	}
	
	private boolean checkUserAuth(HttpHeaders headers)
	{
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString("username", "");
		String password = cookie.getSessionData().optString("password", "");
		return this.checkUserAuth(username, password);
	}
	
	private boolean checkUserAuth(String username, String password)
	{
		return userAccount.checkUserAuth(username, password);
	}
	
	private JSONObject processAuthFile(byte[] responseBody) 
	{
		String responseString = new String(responseBody);
		int start = 0;
		int end = 0;
		do 
		{
			start = responseString.toLowerCase().indexOf("<meta ", end);
			end = responseString.toLowerCase().indexOf(">", start);
			if(start >-1 && end >-1 && end < responseString.length())
			{
				String meta = responseString.substring(start, end+1);
				meta = this.fixMeta(meta);
				try
				{
					JSONObject xx = XML.toJSONObject(meta);
					if(requireLogin(xx))
					{
						return xx.optJSONObject("meta");
					}
				}
				catch(JSONException e)
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		while(start > -1);
		return new JSONObject();
	}
	
	private boolean requireLogin(JSONObject xx) {
		if(xx != null && xx.has("meta"))
		{
			JSONObject metaData = xx.optJSONObject("meta");
			if(metaData != null)
			{
				String name = metaData.optString("name", "");
				boolean content = metaData.optBoolean("content", false);
				if(name.equals("require-login") && content)
				{
					return true;
				}
			}
		}
		return false;
	}

	private String fixMeta(String input)
	{
		if(input.indexOf("</meta>") == -1 && input.indexOf("/>") == -1)
		{
			input = input.replace(">", "/>");
		}
		return input.toLowerCase();
	}

	private String getFileName(HttpServletRequest request) 
	{
		String file = request.getServletPath();
		if(file == null || file.isEmpty() || file.equals("/"))
		{
			file = Config.getDefaultFile();
		}
		return "/static/www"+file;
	}
	
	private String getFileName(String request) 
	{
		return "/static/www"+request;
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
