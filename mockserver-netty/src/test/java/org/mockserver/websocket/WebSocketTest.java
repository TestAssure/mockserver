package org.mockserver.websocket;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpRequest;
import org.mockserver.stop.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebSocketTest {
	private static Logger logger = LoggerFactory.getLogger(WebSocketTest.class );
//	private static Logger logger = LogManager.getLogger( WebSocketTest.class );
//	protected static SSLSocketFactory defaultSocketFactory;
	
	private static final int mockServerPort = 1080;
	private static final int port = 8082;
//	private MockServer mockServer = null;
	private ClientAndServer mockServer = null;
	
	static {
//		System.setProperty("java.util.logging.config.file", "/Users/360capture/workspace/github/mockserver/mockserver-netty/src/test/resources/logging.properties");
//		logger = Logger.getLogger(WebSocketTest.class.getName());
	}
	@Test
	public void testWebSocket() throws Exception {
		
		Runnable runnable = () -> {			
			logger.info("Starting server ...");
			
			final ChatServer endpoint = new ChatServer();
			ChannelFuture future = endpoint.start(new InetSocketAddress(port));
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					endpoint.destroy();
				}
			});
			future.channel().closeFuture().syncUninterruptibly();
		};
		
		Thread serverThread = new Thread( runnable );
		
		try {
			serverThread.start();
			
			startMockServer();	// Start Mock Server Proxy			
			
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", mockServerPort));
			OkHttpClient client = new OkHttpClient().newBuilder()
					.proxy(proxy)
					.build();			
			
//			Request.Builder builder = new Request.Builder()
//				      .url( "http://localhost:" + port + "/index.html" )
//				      .get();
			
//			Response indexPageResponse = httpClient.newCall(builder.build() ).execute();
//			String responseBody = indexPageResponse.body().string();
//			logger.info( "response body: " + responseBody );
			
			WebSocketManager websocketMgr = new WebSocketManager( "Batman" );
			websocketMgr.connect("localhost:" + port, client, 1000);
			
			
//			WebSocketManager websocketMgr2 = new WebSocketManager( "Robin" );
//			websocketMgr2.connect("localhost:" + port, httpClient);
			
			websocketMgr.send("Hello kitty");
//			websocketMgr2.send("Hello kitty 2");
			
			websocketMgr.closeConnection();
//			websocketMgr2.closeConnection();
			
			getMockServerRecording();
			stopMockServer();
			
		} finally {
			logger.info( "Shutting down web server..." );
			serverThread.interrupt();
			Thread.sleep(1000);
		}
	}
	
	private void startMockServer() {
		mockServer = ClientAndServer.startClientAndServer(mockServerPort);
		
//		System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", String.valueOf(mockServerPort));
        
//		mockServer = new MockServer( mockServerPort );
		
//		MockServerLogger mockServerLogger = new MockServerLogger();
//		KeyStoreFactory ksf = new KeyStoreFactory( mockServerLogger );

//		defaultSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
//		HttpsURLConnection.setDefaultSSLSocketFactory( ksf.sslContext().getSocketFactory() );

		System.setProperty( "http.proxyHost", "localhost" );
		System.setProperty( "http.proxyPort", mockServerPort + "" );
//		System.setProperty( "https.proxyHost", "localhost" );
//		System.setProperty( "https.proxyPort", mockServerPort + "" );
	}
	
	private void getMockServerRecording() {
		MockServerClient client = new MockServerClient("localhost", mockServerPort);
		Expectation[] recordedExpectations = client.retrieveRecordedExpectations( HttpRequest.request() );
		logger.info( "Recorded expectations: " + recordedExpectations );
		
		client.close();
		client.stop();
		
	}
	private void stopMockServer() {
//		HttpsURLConnection.setDefaultSSLSocketFactory( defaultSocketFactory );
		
		Stop.stopQuietly(mockServer);
		mockServer.stop();
	}
	
}
