package org.mockserver.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {
	private static Logger logger = LogManager.getLogger( WebSocketHandler.class );
	
//	private static Logger logger = LoggerFactory.getLogger( WebSocketHandler.class );
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof WebSocketFrame) {
//            System.out.println("This is a WebSocket frame");
            logger.info("Client Channel : " + ctx.channel());
            if (msg instanceof BinaryWebSocketFrame) {
            	logger.info("BinaryWebSocketFrame Received : ");
                System.out.println(((BinaryWebSocketFrame) msg).content());
            } else if (msg instanceof TextWebSocketFrame) {
            	logger.info("TextWebSocketFrame Received : ");
                ctx.channel().writeAndFlush(
                        new TextWebSocketFrame("Message recieved : " + ((TextWebSocketFrame) msg).text()));
                logger.info(((TextWebSocketFrame) msg).text());
            } else if (msg instanceof PingWebSocketFrame) {
                System.out.println("PingWebSocketFrame Received : ");
                System.out.println(((PingWebSocketFrame) msg).content());
            } else if (msg instanceof PongWebSocketFrame) {
                System.out.println("PongWebSocketFrame Received : ");
                System.out.println(((PongWebSocketFrame) msg).content());
            } else if (msg instanceof CloseWebSocketFrame) {
            	logger.info("CloseWebSocketFrame Received : ");
            	logger.info("ReasonText :" + ((CloseWebSocketFrame) msg).reasonText());
            	logger.info("StatusCode : " + ((CloseWebSocketFrame) msg).statusCode());
            } else {
            	logger.info("Unsupported WebSocketFrame");
            }
        }
    }
}
