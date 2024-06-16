package com.example.chat_socket.model;

import java.util.List;

public class MessageResponse {
    private List<Message> lastMessage;

    public List<Message> getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(List<Message> lastMessage) {
        this.lastMessage = lastMessage;
    }
}
