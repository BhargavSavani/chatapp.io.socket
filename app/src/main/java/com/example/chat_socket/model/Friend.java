package com.example.chat_socket.model;

public class Friend {
    private String _id;
    private String username;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String lastMessage;
    private String lastMessageTime;
    private String groupName;
    private String groupDescription;
    private String groupIcon;

    public Friend(String _id, String username, String firstName, String lastName, String profilePicture, String lastMessage, String lastMessageTime) {
        this._id = _id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;

    }

    public Friend(String _id, String groupName, String groupDescription, String groupIcon) {
        this._id = _id;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupIcon = groupIcon;
    }


    public String getId() {
        return _id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public String get_id() {
        return _id;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "_id='" + _id + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", lastMessageTime='" + lastMessageTime + '\'' +
                ", groupName='" + groupName + '\'' +
                ", groupDescription='" + groupDescription + '\'' +
                ", groupIcon='" + groupIcon + '\'' +
                '}';
    }
}
