package com.example.chat_socket.model;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("user")
    private UserProfile user;

    public UserProfileResponse(boolean success, UserProfile user) {
        this.success = success;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserProfileResponse{" +
                "success=" + success +
                ", user=" + user +
                '}';
    }
}
