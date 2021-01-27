package org.mockserver.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManager extends WebSocketListener {
	private static Logger logger = LoggerFactory.getLogger( WebSocketManager.class);
	
	private WebSocket webSocket;

	private boolean connected = false;
	private String lastMessage;
	private List<String> unreadMessages = Collections.synchronizedList(new ArrayList<>());

	public void connect(String host, OkHttpClient client) {

		String wssPath = "ws://" + host + "/ws";
		logger.info("Connecting to " + wssPath);
		Request request = new Request.Builder().url(wssPath).addHeader("Accept-Encoding", "gzip, deflate, br")
				.addHeader("Connection", "Upgrade").addHeader("Upgrade", "websocket")
				.addHeader("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.0 Safari/537.36")
				.build();

		webSocket = client.newWebSocket(request, this);

		for (int i = 0; i < 1000; i++) {
			if (connected == true) {
				return;
			} else {
				try {
					Thread.sleep(50);
				} catch (Throwable e) {

				}
			}
		}

		Preconditions.checkState(connected, "Unable to create WebSocket connection to " + host);
	}

	public synchronized String send( String message) {
		try {
			logger.info( "Send msg:" + message );
//			lastMessage = null;
			unreadMessages.clear();
			webSocket.send( message );
			
			for ( int i = 0; i < 1000; i++ ) {
				for ( String unreadMsg : unreadMessages ) {
					return unreadMsg;	
				}
							
				try {
					Thread.sleep( 50 );
				} catch ( Throwable e ) {
					
				}
			}
			
			unreadMessages.clear();
			throw new RuntimeException( "No response received for msg " + message );
		} catch ( Throwable e ) {
			throw new RuntimeException( e );
		}
	}
	
	@Override
	public void onOpen(WebSocket webSocket, Response response) {
		logger.info("Web Socket Response: " + response);
		if (response.code() == 101) {
			connected = true;
		}
	}

	@Override
	public void onMessage(WebSocket webSocket, String text) {
		logger.info("MESSAGE RECV: " + text);

		if (text.startsWith("\"primus::ping::")) {
			String pingId = text.substring(15);
			webSocket.send("\"primus::pong::" + pingId);
		} else {
			unreadMessages.add(text);
		}
//			lastMessage = text;
	}

	@Override
	public void onMessage(WebSocket webSocket, ByteString bytes) {
		logger.info("MESSAGE BYTES RECV: " + bytes.hex());
	}

	@Override
	public void onClosing(WebSocket webSocket, int code, String reason) {
		webSocket.close(1000, null);
		logger.info("CLOSE: " + code + " " + reason);
	}

	@Override
	public void onFailure(WebSocket webSocket, Throwable t, Response response) {
		t.printStackTrace();
	}

}
