package com.example.chat_socket.model;

public class Message {
    private String _id;
    private String senderId;
    private String receiverId;
    private String messageId;
    private String message;
    private String time;
    private boolean isRead;

    public Message() {
    }

    public Message(String senderId,String message,boolean isRead) {
        this.senderId = senderId;
        this.message = message;
        this.isRead = isRead;
    }

    public String getSenderId() {
        return senderId;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Message{" +
                "senderId='" + senderId + '\'' +
                "receiverId='" + receiverId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", message='" + message + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
