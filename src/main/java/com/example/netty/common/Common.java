package com.example.netty.common;


public class Common {
    /**
     * WebSocket 端口号
     */
    public static final int WEBSOCKET_PORT = 8083;
    /**
     * WebSocket ip
     */
    public static final String WEBSOCKET_IP = "192.168.0.168";
    /**
     * WebSocket 路由
     */
    public static final String WEBSOCKET_PATH = "/wsPath";
    /**
     * WebSocket Server
     */
    public static final String WEBSOCKET_Server_URL="ws://" + WEBSOCKET_IP +":"+WEBSOCKET_PORT;
    /**
     * WebSocket Client
     */
    public static final String WEBSOCKET_Client_URL="ws://" + WEBSOCKET_IP +":"+WEBSOCKET_PORT + WEBSOCKET_PATH;
}
