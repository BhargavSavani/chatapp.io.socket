package com.example.chat_socket.model;

import java.util.List;

public class User {

    private String profilePicture;
    private List<Friend> friends;

    public User(List<Friend> friends) {
        this.friends = friends;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<Friend> getFriends() {
        return friends;
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
    }

    @Override
    public String toString() {
        return "User{friends=" + friends +
                '}';
    }
}
