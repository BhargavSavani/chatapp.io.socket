package com.example.chat_socket.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_socket.R;
import com.example.chat_socket.model.UserProfile;
import com.example.chat_socket.model.UserProfileResponse;
import com.example.chat_socket.service.ApiService;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private CircleImageView profileImageView;
    private EditText nameEditText;
    private EditText aboutEditText;
    private TextView phoneTextView;
    private Button saveButton;
    private Retrofit retrofit;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.cviv1);
        nameEditText = findViewById(R.id.nameEditText);
        aboutEditText = findViewById(R.id.aboutEditText);
        phoneTextView = findViewById(R.id.phoneTextView);
        saveButton = findViewById(R.id.saveButton);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString("token", null);
        String username = preferences.getString("username", null);

        // Initialize Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.7:8000") // Replace with your base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        getUserProfile(token, username);
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString();
            String newAbout = aboutEditText.getText().toString();
            if (!newName.isEmpty() && !newAbout.isEmpty()) {
                updateUserDetails(token, newName,newAbout);
            } else {
                Toast.makeText(ProfileActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserProfile(String token, String username) {
        Call<UserProfileResponse> call = apiService.getUserProfile(token, username);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserProfile user = response.body().getUser();
                    Log.d(TAG, "onResponse: " + user);
                    updateUIWithUserProfile(user);
                } else {
                    Log.d(TAG, "onResponse Error: " + response.message());
                    Toast.makeText(ProfileActivity.this, "Failed to retrieve user details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithUserProfile(UserProfile userProfile) {
        nameEditText.setText(userProfile.getName());
        aboutEditText.setText(userProfile.getAbout());
        phoneTextView.setText(userProfile.getPhoneNumber());
        Picasso.get().load("http://192.168.1.7:8000/Assets/" + userProfile.getProfileImage()).into(profileImageView);
    }

    private void updateUserDetails(String token, String newName, String newAbout) {
        UserProfile updatedUserProfile = new UserProfile();

        updatedUserProfile.setName(newName);
        updatedUserProfile.setAbout(newAbout);

        Call<UserProfileResponse> call = apiService.updateUserDetails(token, updatedUserProfile);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    // Update UI with the updated profile if needed
                } else {
                    Log.d(TAG, "onResponse Error: " + response.message());
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
