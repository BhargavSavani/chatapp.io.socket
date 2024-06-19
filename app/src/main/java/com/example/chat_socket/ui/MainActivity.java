package com.example.chat_socket.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_socket.R;
import com.example.chat_socket.adapter.FriendsAdapter;
import com.example.chat_socket.model.Friend;
import com.example.chat_socket.model.User;
import com.example.chat_socket.service.ApiService;
import com.example.chat_socket.service.SocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    private static final String TAG = "MainActivity";

    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private List<Friend> friendsList = new ArrayList<>();

    private Retrofit retrofit;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);

        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsAdapter = new FriendsAdapter(this, friendsList);
        friendsRecyclerView.setAdapter(friendsAdapter);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString("token", null);
        String username = preferences.getString("username", null);

        Log.d(TAG, "onCreate: " + token);
        Log.d(TAG, "onCreate: " + username);

        if (token == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.8:8000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        getUserDetails(token);

        SocketManager.initializeSocket(token);
        mSocket = SocketManager.getSocket();

        mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show()));
        mSocket.emit("register", username)
                .on("new message", args -> {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        String message = data.getString("message");
                        runOnUiThread(() -> Log.d(TAG, "Message received: " + message));
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing message: " + e.getMessage());
                    }
                })
                .on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show()))
                .on(Socket.EVENT_CONNECT_ERROR, args -> {
                    Exception e = (Exception) args[0];
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                });

        mSocket.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.ivMenu) {
            View view = findViewById(R.id.ivMenu);
            showPopupMenu(view);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_settings) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (item.getItemId() == R.id.menu_logout) {
                logout();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void logout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("token");
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void getUserDetails(String token) {
        Call<User> call = apiService.getUserDetails(token);
        Log.d(TAG, "getUserDetails: " + token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "onResponse: User details: " + user.toString());
                    friendsList.clear();
                    friendsList.addAll(user.getFriends());

                    friendsAdapter.notifyDataSetChanged();

                } else {
                    Log.d(TAG, "onResponse Error: " + response.message());
                    Toast.makeText(MainActivity.this, "Failed to retrieve user details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
        }
    }
}

