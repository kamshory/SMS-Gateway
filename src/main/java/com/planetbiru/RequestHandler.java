package com.planetbiru;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.planetbiru.config.MIMEonfig;
import com.planetbiru.cons.ConstantString;
import com.planetbiru.cons.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.gsm.SMSInstance;
import com.planetbiru.settings.FeederSetting;
import com.planetbiru.settings.SMSSetting;
import com.planetbiru.user.User;
import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;
import com.planetbiru.ws.WebSocketClient;

@RestController
public class RequestHandler {

	@Autowired
	SMSInstance smsService;
	
	@Autowired
	WebSocketClient wsClient;
	
	@Autowired
	UserAccount userAccount;

	@Value("${sms.connection.type}")
	private String portName;

	@Value("${sms.ws.endpoint}")
	private String wsClientEndpoint = "ws://localhost:8888/ws";

	@Value("${sms.ws.username}")
	private String wsClientUsername;

	@Value("${sms.ws.password}")
	private String wsClientPassword;

	@Value("${sms.web.session.name}")
	private String sessionName;

	@Value("${sms.web.session.lifetime}")
	private int cacheLifetime;

	@Value("${sms.web.document.root}")
	private String documentRoot = "/static/www";

	@Value("${sms.path.setting.feeder}")
	private String feederSettingPath;

	@Value("${sms.path.setting.sms}")
	private String smsSettingPath;
	
	@Value("${sms.path.setting.all}")
	private String mimeSettingPath;	

	private MIMEonfig mime = new MIMEonfig();
	


	
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
			mime = new MIMEonfig(mimeSettingPath);
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
		String port = Config.getPortName();
		smsService.init(port);
	}

	private void initWSClient() 
	{
		wsClient.setSMSService(smsService);
		wsClient.start();	
	}
	
	@GetMapping(path="/broadcast-ws")
	public ResponseEntity<byte[]> broadcast(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		ServerWebSocket.broadcast("Halooooo.. ini pesannya....");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	
	@PostMapping(path="/login.html")
	public ResponseEntity<byte[]> handleLogin(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
	    
	    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
	    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
	    String next = queryPairs.getOrDefault(JsonKey.NEXT, "");
	    
	    if(next.isEmpty())
		{
	    	next = "/";
		}
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
	    responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
	    
	    JSONObject res = new JSONObject();
	    JSONObject payload = new JSONObject();
	    payload.put("nextURL", next);
	    res.put("code", 0);
	    res.put("payload", payload);
	    
		cookie.setSessionValue(JsonKey.USERNAME, username);
		cookie.setSessionValue(JsonKey.PASSWORD, password);
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
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		responseHeaders.add(ConstantString.LOCATION, "/");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/account/self")
	public ResponseEntity<byte[]> handleSelfAccount(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		if(userAccount.checkUserAuth(headers))
		{
			String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
			String list = userAccount.getUser(loggedUsername).toString();
			responseBody = list.getBytes();
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;			
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/feeder-setting/get")
	public ResponseEntity<byte[]> handleFeederSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		if(userAccount.checkUserAuth(headers))
		{
			FeederSetting feederSetting = new FeederSetting();
			feederSetting.load(feederSettingPath);
			String list = feederSetting.toString();
			responseBody = list.getBytes();
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;			
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/sms-setting/get")
	public ResponseEntity<byte[]> handleSMSSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		if(userAccount.checkUserAuth(headers))
		{
			SMSSetting smsSetting = new SMSSetting();
			smsSetting.load(smsSettingPath);
			String list = smsSetting.toString();
			responseBody = list.getBytes();
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;			
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/user/list")
	public ResponseEntity<byte[]> handleUserList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		if(userAccount.checkUserAuth(headers))
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
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/user/detail/{username}")
	public ResponseEntity<byte[]> handleUserGet(@RequestHeader HttpHeaders headers, @PathVariable(value=JsonKey.USERNAME) String username, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		if(userAccount.checkUserAuth(headers))
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
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@PostMapping(path="/user/add**")
	public ResponseEntity<byte[]> userAdd(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(userAccount.checkUserAuth(headers))
		{
			Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);		
		    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
		    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
		    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
		    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
	
		    JSONObject jsonObject = new JSONObject();
			jsonObject.put(JsonKey.USERNAME, username);
			jsonObject.put(JsonKey.NAME, name);
			jsonObject.put(JsonKey.PASSWORD, password);
			jsonObject.put(JsonKey.PHONE, phone);
			jsonObject.put(JsonKey.BLOCKED, false);
			jsonObject.put(JsonKey.ACTIVE, true);
			
			if(!username.isEmpty())
			{
				userAccount.addUser(new User(jsonObject));		
				userAccount.save();
			}		    
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/user/update**")
	public ResponseEntity<byte[]> userUpdate(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(userAccount.checkUserAuth(headers))
		{
			Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);				
		    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
		    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
		    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
		    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
		    boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
		    boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");
	
		    JSONObject jsonObject = new JSONObject();
			jsonObject.put(JsonKey.USERNAME, username);
			jsonObject.put(JsonKey.NAME, name);
			jsonObject.put(JsonKey.PHONE, phone);
			jsonObject.put(JsonKey.BLOCKED, blocked);
			jsonObject.put(JsonKey.ACTIVE, active);
			if(!username.isEmpty())
			{
				jsonObject.put(JsonKey.USERNAME, username);
			}
			if(!password.isEmpty())
			{
				jsonObject.put(JsonKey.PASSWORD, password);
			}
			userAccount.updateUser(new User(jsonObject));		
			userAccount.save();
		    
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/user/remove**")
	public ResponseEntity<byte[]> userRemove(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(userAccount.checkUserAuth(headers))
		{			
			Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);			
		    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");

		    userAccount.deleteUser(username);		
			userAccount.save();
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}	

	@PostMapping(path="/api/sms**")
	public ResponseEntity<String> sendSMS(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		JSONObject responseJSON = this.processMessageRequest(requestBody);
		
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api/ussd**")
	public ResponseEntity<String> sendUSSD(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		JSONObject responseJSON = new JSONObject();
		
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
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
		
		responseHeaders.add(ConstantString.CONTENT_TYPE, contentType);
		
		if(fileName.endsWith(".html"))
		{
			cookie.saveSessionData();
		}
		else
		{
			responseHeaders.add(ConstantString.CACHE_CONTROL, "public, max-age="+cacheLifetime+", immutable");
		}
		
		cookie.putToHeaders(responseHeaders);
		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}	
	
	private void processFeedbackPost(HttpHeaders headers, String requestBody, HttpServletRequest request) 
	{
		if(userAccount.checkUserAuth(headers))
		{
			CookieServer cookie = new CookieServer(headers);
			String path = request.getServletPath();
			if(path.equals("/admin.html"))
			{
				this.processAdmin(requestBody, cookie);
			}
			if(path.equals("/account-update.html"))
			{
				this.processAccount(requestBody, cookie);
			}
			if(path.equals("/feeder-setting.html"))
			{
				this.processFeederSetting(requestBody);
			}
			if(path.equals("/sms-setting.html"))
			{
				this.processSMSSetting(requestBody);
			}
			if(path.equals("/sms.html"))
			{
				this.processSMS(requestBody);
			}
		}
	}
	
	private void processSMSSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("save_sms_setting"))
		{
			String connectionType = query.getOrDefault("connection_type", "");			
			String smsCenter = query.getOrDefault("sms_center", "");		
			int incommingInterval = 0;
			try
			{
				String incommingInt = query.getOrDefault("incomming_interval", "0");
				incommingInt = incommingInt.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(incommingInt.isEmpty())
				{
					incommingInt = "0";
				}
				incommingInterval = Integer.parseInt(incommingInt);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			int timeRange = 0;	
			try
			{
				String tmRange = query.getOrDefault("time_range", "0");
				tmRange = tmRange.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(tmRange.isEmpty())
				{
					tmRange = "0";
				}
				timeRange = Integer.parseInt(tmRange);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			int maxPerTimeRange = 0;
			try
			{
				String maxInRange = query.getOrDefault("max_per_time_range", "0");
				maxInRange = maxInRange.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(maxInRange.isEmpty())
				{
					maxInRange = "0";
				}
				maxPerTimeRange = Integer.parseInt(maxInRange);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			SMSSetting smsSetting = new SMSSetting();
			smsSetting.setConnectionType(connectionType);
			smsSetting.setSmsCenter(smsCenter);
			smsSetting.setIncommingInterval(incommingInterval);
			smsSetting.setTimeRange(timeRange);
			smsSetting.setMaxPerTimeRange(maxPerTimeRange);			
			
			smsSetting.save(smsSettingPath);			
		}		
	}
	
	private void processFeederSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("save_feeder_setting"))
		{
			String feederType = query.getOrDefault("feeder_type", "");			
			boolean feederSSL = query.getOrDefault("feeder_ssl", "").equals("1");		
			String feederAddress = query.getOrDefault("feeder_address", "");		
			int feederPort = 0;
			try
			{
				String port = query.getOrDefault("feeder_port", "0");
				port = port.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(port.isEmpty())
				{
					port = "0";
				}
				feederPort = Integer.parseInt(port);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			String feederPath = query.getOrDefault("feeder_path", "");		
			String feederUsername = query.getOrDefault("feeder_username", "");		
			String feederPassword = query.getOrDefault("feeder_password", "");		
			String feederChannel = query.getOrDefault("feeder_channel", "");
			
			int feederTimeout = 0;	
			try
			{
				String timeout = query.getOrDefault("feeder_timeout", "0");
				timeout = timeout.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(timeout.isEmpty())
				{
					timeout = "0";
				}
				feederTimeout = Integer.parseInt(timeout);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			int feederRefresh = 0;
			try
			{
				String refresh = query.getOrDefault("feeder_refresh", "0");
				refresh = refresh.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(refresh.isEmpty())
				{
					refresh = "0";
				}
				feederRefresh = Integer.parseInt(refresh);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			FeederSetting feederSetting = new FeederSetting();
			feederSetting.setFeederType(feederType);
			feederSetting.setFeederSSL(feederSSL);
			feederSetting.setFeederAddress(feederAddress);
			feederSetting.setFeederPort(feederPort);
			feederSetting.setFeederPath(feederPath);
			feederSetting.setFeederUsername(feederUsername);
			feederSetting.setFeederPassword(feederPassword);
			feederSetting.setFeederChannel(feederChannel);
			feederSetting.setFeederTimeout(feederTimeout);
			feederSetting.setFeederRefresh(feederRefresh);			
			
			feederSetting.save(feederSettingPath);			
		}		
	}
	
	private void processSMS(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("send"))
		{
			String receiver = query.getOrDefault("receiver", "");			
			String message = query.getOrDefault("message", "");		
			smsService.sendSMS(receiver, message);
		}		
	}
	
	private void processAccount(String requestBody, CookieServer cookie) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
		String phone = query.getOrDefault(JsonKey.PHONE, "");
		String password = query.getOrDefault(JsonKey.PASSWORD, "");
		String name = query.getOrDefault(JsonKey.NAME, "");
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
	
	private void processAdmin(String requestBody, CookieServer cookie) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
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
			this.processAdminDeactivate(query, loggedUsername);
		}
		if(query.containsKey("activate"))
		{
			/**
			 * Activate
			 */
			this.processAdminActivate(query);
		}
		if(query.containsKey("block"))
		{
			/**
			 * Block
			 */
			this.processAdminBlock(query, loggedUsername);
			
		}
		if(query.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			this.processAdminUnblock(query);
		}
		if(query.containsKey("update-data"))
		{
			this.processAdminUpdateData(query);
		}
	}
	private void processAdminDeactivate(Map<String, String> query, String loggedUsername)
	{
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
	private void processAdminActivate(Map<String, String> query)
	{
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
	private void processAdminBlock(Map<String, String> query, String loggedUsername)
	{
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
	private void processAdminUnblock(Map<String, String> query)
	{
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
	private void processAdminUpdateData(Map<String, String> query)
	{
		String pkID = query.getOrDefault("pk_id", "");
		String field = query.getOrDefault("field", "");
		String value = query.getOrDefault("value", "");
		if(!field.equals(JsonKey.USERNAME))
		{
			User user = userAccount.getUser(pkID);
			if(field.equals(JsonKey.PHONE))
			{
				user.setPhone(value);
			}
			if(field.equals(JsonKey.NAME))
			{
				user.setName(value);
			}
			userAccount.updateUser(user);
			userAccount.save();
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
			requireLogin = authFileInfo.optBoolean(JsonKey.CONTENT, false);
			fileSub = this.getFileName(authFileInfo.optString("data-file", ""));
		}
		
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		if(requireLogin)
		{
			responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
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
			responseBody = this.removeMeta(responseBody);
			webContent.setResponseBody(responseBody);
		}
		return webContent;
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
					JSONObject metaObj = XML.toJSONObject(meta);
					JSONObject metaObjFixed = this.lowerCaseJSONKey(metaObj);
					if(requireLogin(metaObjFixed))
					{
						return metaObjFixed.optJSONObject(JsonKey.META);
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
	
	private byte[] removeMeta(byte[] responseBody) 
	{
		String responseString = new String(responseBody);
		int start = 0;
		int end = 0;
		String metaOri = "";
		boolean found = false;
		do 
		{
			start = responseString.toLowerCase().indexOf("<meta ", end);
			end = responseString.toLowerCase().indexOf(">", start);
			if(start >-1 && end >-1 && end < responseString.length())
			{
				metaOri = responseString.substring(start, end+1);
				String meta = this.fixMeta(metaOri);
				try
				{
					JSONObject metaObj = XML.toJSONObject(meta);
					JSONObject metaObjFixed = this.lowerCaseJSONKey(metaObj); 
					if(requireLogin(metaObjFixed))
					{
						found = true;
						break;
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
		String content = "";
		if(found && responseBody != null)
		{
			content = new String(responseBody);
			return content.replace(metaOri, "").getBytes();
		}
		return responseBody;
	}

	private boolean requireLogin(JSONObject metaObj) {
		if(metaObj != null && metaObj.has(JsonKey.META))
		{
			JSONObject metaData = metaObj.optJSONObject(JsonKey.META);
			if(metaData != null)
			{
				String name = metaData.optString(JsonKey.NAME, "");
				boolean content = metaData.optBoolean(JsonKey.CONTENT, false);
				if(name.equals(JsonKey.REQUIRE_LOGIN) && content)
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
		return input;
	}
	
	private JSONObject lowerCaseJSONKey(Object object) 
	{
		JSONObject newMetaObj = new JSONObject();
		JSONArray keys = ((JSONObject) object).names();
		for (int i = 0; i < keys.length (); ++i) 
		{
		   String key = keys.getString(i); 
		   if(((JSONObject) object).get(key) instanceof JSONObject)
		   {
			   newMetaObj.put(key.toLowerCase(), this.lowerCaseJSONKey(((JSONObject) object).get(key)));
		   }
		   else
		   {
			   newMetaObj.put(key.toLowerCase(), ((JSONObject) object).get(key));
		   }
		}
		return newMetaObj;
	}

	private String getFileName(HttpServletRequest request) 
	{
		String file = request.getServletPath();
		if(file == null || file.isEmpty() || file.equals("/"))
		{
			file = Config.getDefaultFile();
		}
		return documentRoot+file;
	}
	
	private String getFileName(String request) 
	{
		return documentRoot+request;
	}
	
	private JSONObject processMessageRequest(String requestBody) {
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

}
