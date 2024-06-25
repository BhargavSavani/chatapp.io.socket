package com.example.chat_socket.model;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {
    private static Socket mSocket;
    private static final String BASE_URL = "http://192.168.1.7:8000";

    public static void initializeSocket(String token) {
        try {
            IO.Options options = new IO.Options();
            options.query = "token=" + token;
            mSocket = IO.socket(BASE_URL, options);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Socket getSocket() {
        return mSocket;
    }

}
