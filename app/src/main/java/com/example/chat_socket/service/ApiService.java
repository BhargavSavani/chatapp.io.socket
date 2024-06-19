package com.example.chat_socket.service;

import com.example.chat_socket.model.MessageResponse;
import com.example.chat_socket.model.User;
import com.example.chat_socket.model.UserProfile;
import com.example.chat_socket.model.UserProfileResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @GET("/chat")
    Call<User> getUserDetails(@Header("Authorization") String token);

    @GET("/user/lastMessage/{username}")
    Call<MessageResponse> getLastMessage(
            @Header("Authorization") String token,
            @Path("username") String username
    );


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


    @GET("/user/{username}")
    Call<UserProfileResponse> getUserProfile(@Header("Authorization") String token, @Path("username") String username);

    @PUT("/user/update")
    Call<UserProfileResponse> updateUserDetails(@Header("Authorization") String token, @Body UserProfile userProfile);
}
