package org.mockserver.websocket;

import java.net.InetSocketAddress;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpRequest;
import org.mockserver.netty.MockServer;
import org.mockserver.socket.tls.KeyStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;

public class WebSocketTest {
	private static Logger logger = LoggerFactory.getLogger( WebSocketTest.class);
	protected static SSLSocketFactory defaultSocketFactory;
	
	@Test
	public void testWebSocket() throws Exception {
		
		int port = 8082;
		int mockServerPort = 8080;
		
		Runnable runnable = () -> {
			
			logger.info("Starting server");
			
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
		
		Thread thread = new Thread( runnable );
		
		
		thread.start();
		
		MockServer mockServer = new MockServer( mockServerPort );
		
		MockServerLogger mockServerLogger = new MockServerLogger();
		KeyStoreFactory ksf = new KeyStoreFactory( mockServerLogger );

		defaultSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		HttpsURLConnection.setDefaultSSLSocketFactory( ksf.sslContext().getSocketFactory() );

		System.setProperty( "http.proxyHost", "localhost" );
		System.setProperty( "http.proxyPort", mockServerPort + "" );
		System.setProperty( "https.proxyHost", "localhost" );
		System.setProperty( "https.proxyPort", mockServerPort + "" );
		
		logger.info( "Start test body..." );
		
		Thread.sleep(10000);
		
		Expectation[] recordedExpectations = new MockServerClient("localhost", mockServerPort)
			    .retrieveRecordedExpectations(
			        HttpRequest.request()
			    );
		
		logger.info( "Recorded expectations: " + recordedExpectations );
		
		HttpsURLConnection.setDefaultSSLSocketFactory( defaultSocketFactory );
		
		mockServer.stop();
		thread.interrupt();
	}
}
