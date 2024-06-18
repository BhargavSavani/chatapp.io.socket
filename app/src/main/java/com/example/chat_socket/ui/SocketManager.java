package com.example.chat_socket.ui;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {
    private static Socket mSocket;
    private static String token;

    public static void initializeSocket(String token) {
        SocketManager.token = token;
        try {
            IO.Options options = IO.Options.builder()
                    .setQuery("token=" + token)
                    .build();
            mSocket = IO.socket("http://192.168.1.8:8000", options);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Socket getSocket() {
        return mSocket;
    }
}
