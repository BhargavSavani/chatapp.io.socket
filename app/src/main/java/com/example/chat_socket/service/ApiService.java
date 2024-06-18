package com.example.chat_socket.service;

import com.example.chat_socket.model.MessageResponse;
import com.example.chat_socket.model.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @GET("/chat")
    Call<User> getUserDetails(@Header("Authorization") String token);

    @GET("/friend/lastMessage")
    Call<MessageResponse> getLastMessages(@Header("Authorization") String token);

    @Multipart
    @POST("/signup")
    Call<ResponseBody> uploadImage(
            @Part MultipartBody.Part image,
            @Part("firstName") RequestBody firstName,
            @Part("lastName") RequestBody lastName,
            @Part("mobileNumber") RequestBody mobileNumber,
            @Part("emailAddress") RequestBody emailAddress,
            @Part("password") RequestBody password
    );
}
