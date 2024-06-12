package com.example.chat_socket.ui;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chat_socket.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText userInputField;
    private EditText passwordField;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString("token", null);
        if (token != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        userInputField = findViewById(R.id.edtEmail);
        passwordField = findViewById(R.id.edtPassword);
        loginButton = findViewById(R.id.Login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = userInputField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                if (!userInput.isEmpty() && !password.isEmpty()) {
                    performLogin(userInput, password);
                } else {
                    Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void performLogin(String userInput, String password) {
        OkHttpClient client = new OkHttpClient();

        String url = "http://192.168.1.9:8000/login";
        RequestBody formBody = new FormBody.Builder()
                .add("userInput", userInput)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String message = json.getString("message");
                        String token = json.getString("token");
                        String userId = json.getString("userId");
                        String username = json.getString("username");

                        System.out.println("Username: " + username);
                        Log.d(TAG, "onResponse: " + userId);
                        Log.d(TAG, "onResponse: " + token);

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", token);
                        editor.putString("userId", userId);
                        editor.putString("username", username);
                        editor.apply();

                        Log.d(TAG, "Stored Username: " + preferences.getString("username", "null"));


                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            // Start MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Response Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String error = json.getString("error");

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Response Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }
        });
    }
}