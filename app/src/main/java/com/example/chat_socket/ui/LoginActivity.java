package com.example.chat_socket.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private EditText edtEmail, edtPassword;
    private Button loginButton;
    private TextView tvSignup;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        loginButton = findViewById(R.id.Login);
        tvSignup = findViewById(R.id.Signup);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString("token", null);
        if (token != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        loginButton.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                performLogin(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });

        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void performLogin(String email, String password) {
        RequestBody formBody = new FormBody.Builder()
                .add("userInput", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.1.8:8000/login")  // Replace with your API URL
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("LoginActivity", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String message = json.getString("message");
                        String userId = json.getString("userId");
                        String username = json.getString("username");
                        String token = json.getString("token");

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("userId", userId);
                        editor.putString("token", token);
                        editor.putString("username", username);
                        editor.apply();

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Response Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                } else {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String error = json.getString("error");
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login Error: " + error, Toast.LENGTH_LONG).show());
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Response Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }
            }
        });
    }
}