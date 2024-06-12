package com.example.chat_socket.model;

public class Friend {

    private String username;
    private String profilePicture;


    public Friend(String username, String profilePicture) {
        this.username = username;
        this.profilePicture = profilePicture;
    }


    // Getters and Setters
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
}
