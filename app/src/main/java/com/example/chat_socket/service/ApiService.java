package com.example.chat_socket.service;

import com.example.chat_socket.model.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiService {
    @GET("/chat")
    Call<User> getUserDetails(@Header("Authorization") String token);
}
