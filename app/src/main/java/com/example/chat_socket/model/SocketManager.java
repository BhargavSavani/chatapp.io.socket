package com.example.chat_socket.model;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {
    private static SocketManager instance;
    private Socket mSocket;

    private SocketManager(String token) {
        try {
            IO.Options opts = new IO.Options();
            opts.query = "token=" + token;
            mSocket = IO.socket("http://192.168.1.7:8000", opts);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void initializeSocket(String token) {
        if (instance == null) {
            instance = new SocketManager(token);
        }
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SocketManager is not initialized.");
        }
        return instance;
    }

    public Socket getSocket() {
        return mSocket;
    }

}
