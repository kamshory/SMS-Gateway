package com.planetbiru.ddns;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.planetbiru.config.Config;
import com.planetbiru.util.ResponseEntityCustom;
import com.planetbiru.util.Utility;

public class DNSNoIP extends DNS{
	
	private String email = "";
	private String username = "";
	private String endpoint = "https://dynupdate.no-ip.com/nic/update";
	private String password = "";
	private String company = "";
	
	@Override
	public JSONObject update(DDNSRecord ddnsRecord)  
	{
		JSONObject res = new JSONObject();
		HttpMethod method = HttpMethod.GET;
		Map<String, String> params = new HashMap<>();
		params.put("hostname", ddnsRecord.getRecordName());
		this.request(method, endpoint, params);
		return res;
	}
	
	/**
	* Issues an HTTPS request and returns the result
	*
	* @param string String method
	* @param string String endpoint
	* @param array  String params
	*
	* @throws Exception
	*
	* @return mixed
	*/
	public ResponseEntityCustom request(HttpMethod method, String endpoint, Map<String, String> params)
	{
		int timeout = 10000;
		HttpHeaders headers = this.createRequestHeader();
		String body = null;
		return this.httpExchange(method, endpoint, headers, body, timeout);
	}

	public HttpHeaders createRequestHeader() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add(DDNSKey.HEADER_AUTHORIZATION, Utility.basicAuth(this.username, this.password));
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, this.createUserAgent());
		requestHeaders.add(DDNSKey.HEADER_CONNECTION, "close");
		return requestHeaders;
	}
	
	private String createUserAgent()
	{
		return String.format("%s %s %s", this.company, Config.getNoIPDevice(), this.email);
	}

	public void setConfig(String endpoint, String username, String password, String email, String company) {
		this.endpoint = endpoint;
		this.username = username;
		this.password = password;
		this.email = email;		
		this.company = company;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}


}
