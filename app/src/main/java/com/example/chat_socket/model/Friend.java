package com.example.chat_socket.model;

public class Friend {

    private String _id;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String lastMessage;

    public Friend(String _id, String firstName, String lastName, String profilePicture, String lastMessage) {
        this._id = _id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.lastMessage = lastMessage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "friendId='" + _id + '\'' +
                ", firstName='" + firstName + '\'' +
                ",lastName='" + lastName + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                '}';
    }
}
