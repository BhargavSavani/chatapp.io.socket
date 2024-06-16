package com.example.chat_socket.model;

public class Friend {

    private String userId;
    private String username;
    private String profilePicture;
    private String lastMessage;

    public Friend(String userId, String username, String profilePicture, String lastMessage) {
        this.userId = userId;
        this.username = username;
        this.profilePicture = profilePicture;
        this.lastMessage = lastMessage;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                '}';
    }

}
