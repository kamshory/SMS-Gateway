package com.planetbiru.ws;

import java.io.IOException;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.planetbiru.config.Config;
import com.planetbiru.gsm.SMSService;
import com.planetbiru.util.Utility;

import javax.websocket.ClientEndpointConfig.Configurator;

import org.springframework.stereotype.Service;

@Service
public class WebSocketClient extends Thread implements WebSocket
{
	private static Object waitLock = new Object();
	private Session session = null;
	private SMSService smsService;
	private WebSocketContainer container;
	public WebSocketClient(SMSService smsService) {
		this.smsService = smsService;
	}

	public WebSocketClient() {
	}

	public void initWSClient() throws WSConnectionException {
		initWSClient(null);
	}
	public void run()
	{
		boolean connected = false;
		do
		{
			System.out.println("Connecting...");
			try 
			{
				this.initWSClient(smsService);
				connected = true;
			} 
			catch (WSConnectionException e) 
			{
				connected = false;
				e.printStackTrace();
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e1) 
				{
					e1.printStackTrace();
					Thread.currentThread().interrupt();
				}
				try 
				{
					this.initWSClient(smsService);
				} 
				catch (WSConnectionException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
		while(!connected);
	}
	public void initWSClient(SMSService smss) throws WSConnectionException
	{
		try
		{
			if(smss != null)
			{
				this.smsService = smss;
			}
			String url = Config.getWsClientEndpoint();
			this.container = ContainerProvider.getWebSocketContainer(); 	
			
			javax.websocket.ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
			configBuilder.configurator(new Configurator() {
			    @Override
			    public void beforeRequest(Map<String, List<String>> headers) 
			    {
			        headers.put("Authorization", Utility.asList(basicAuth(Config.getWsClientUsername(), Config.getWsClientPassword())));
			    }
			});
			ClientEndpointConfig clientConfig = configBuilder.build();
			
			this.session = container.connectToServer(new ClientEndpoint(this, smsService), clientConfig, URI.create(url)); 
			wait4TerminateSignal();
			
		} 
		catch (DeploymentException | IOException e) 
		{
			throw new WSConnectionException(e);
		}
		finally
		{
			if(this.session != null)
			{
				try 
				{
					this.session.close();
				} 
				catch (IOException e) 
				{     
					e.printStackTrace();
				}
			}         
		} 
		

	}
	
	public void close() {
		try 
		{
			this.session.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}		
	}

	
	private static String basicAuth(String username, String password)
	{
		return "Basic " + Utility.base64Encode(username+":"+password);
	}
	
	private static void wait4TerminateSignal()
	{
		synchronized(waitLock)
		{
			try 
			{
				waitLock.wait();
			} 
			catch (InterruptedException e) 
			{
				Thread.currentThread().interrupt();
			}
		}
	}

	public void reconnect() 
	{
		System.out.println("Reconnect...");
		try 
		{
			initWSClient();
		} 
		catch (WSConnectionException e) 
		{
			e.printStackTrace();
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e1) 
			{
				Thread.currentThread().interrupt();
			}
			reconnect();
		}
	}

	@Override
	public void abort() {
		/**
		 * Do nothing
		 */
	}

	@Override
	public String getSubprotocol() {
		return null;
	}

	@Override
	public boolean isInputClosed() {
		return false;
	}

	@Override
	public boolean isOutputClosed() {
		return false;
	}

	@Override
	public void request(long arg0) {
		/**
		 * Do nothing
		 */
	}

	@Override
	public CompletableFuture<WebSocket> sendBinary(ByteBuffer arg0, boolean arg1) {
		return null;
	}

	@Override
	public CompletableFuture<WebSocket> sendClose(int arg0, String arg1) {
		return null;
	}

	@Override
	public CompletableFuture<WebSocket> sendPing(ByteBuffer arg0) {
		return null;
	}

	@Override
	public CompletableFuture<WebSocket> sendPong(ByteBuffer arg0) {
		return null;
	}

	@Override
	public CompletableFuture<WebSocket> sendText(CharSequence arg0, boolean arg1) {
		return null;
	}

	public void setSMSService(SMSService smsService) {
		this.smsService = smsService;	
	}


	
	
}