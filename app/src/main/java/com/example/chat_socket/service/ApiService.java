package com.example.chat_socket.service;

import com.example.chat_socket.model.Message;
import com.example.chat_socket.model.MessageResponse;
import com.example.chat_socket.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/chat")
    Call<User> getUserDetails(@Header("Authorization") String token);

    @GET("/friend/lastMessage")
    Call<MessageResponse> getLastMessages(@Header("Authorization") String token);
}
