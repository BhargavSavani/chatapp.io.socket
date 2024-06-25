package com.example.chat_socket.ui;

import static io.socket.client.On.on;

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
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_socket.R;
import com.example.chat_socket.adapter.FriendsAdapter;
import com.example.chat_socket.model.Friend;
import com.example.chat_socket.model.User;
import com.example.chat_socket.service.ApiService;
import com.example.chat_socket.model.SocketManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Socket mSocket;
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private List<Friend> friendsList = new ArrayList<>();
    private List<Friend> filteredFriendsList = new ArrayList<>();
    private SearchView searchView;
    private String token;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFriends(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFriends(newText);
                return false;
            }
        });

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsAdapter = new FriendsAdapter(this, filteredFriendsList);
        friendsRecyclerView.setAdapter(friendsAdapter);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        token = preferences.getString("token", null);
        username = preferences.getString("username", null);

        Log.d(TAG, "onCreate: " + token);
        Log.d(TAG, "onCreate: " + username);

        if (token == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize Socket and set up listeners
        SocketManager.initializeSocket(token);
        mSocket = SocketManager.getSocket();

        mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            sendUsernameToServer(username);
        }));

        mSocket.on("friends list", onFriendsList);

        mSocket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show()));
        mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Exception e = (Exception) args[0];
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        mSocket.connect();
    }

    private void sendUsernameToServer(String username) {
        JSONObject registerObject = new JSONObject();
        try {
            registerObject.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("register", registerObject);
    }

    private Emitter.Listener onFriendsList = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(() -> {
                try {
                    JSONArray data = (JSONArray) args[0];
                    Log.d(TAG, "Friends list received: " + data.toString());  // Log the received data
                    friendsList.clear();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject friendJson = data.getJSONObject(i);
                        Friend friend = new Friend(
                                friendJson.getString("_id"),
                                friendJson.getString("username"),
                                friendJson.getString("firstName"),
                                friendJson.getString("lastName"),
                                friendJson.getString("profilePicture"),
                                friendJson.optString("lastMessage"),
                                friendJson.optString("lastMessageTime")
                        );
                        friendsList.add(friend);
                    }
                    filteredFriendsList.clear();
                    filteredFriendsList.addAll(friendsList);
                    friendsAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing friends list: " + e.getMessage());
                }
            });
        }
    };

    private void searchFriends(String query) {
        if (token == null) {
            Toast.makeText(this, "Token is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (query.isEmpty()) {
            filteredFriendsList.clear();
            filteredFriendsList.addAll(friendsList);
            friendsAdapter.notifyDataSetChanged();
            return;
        }

        JSONObject searchRequest = new JSONObject();
        try {
            searchRequest.put("token", token);
            searchRequest.put("query", query);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating search request JSON: " + e.getMessage());
            return;
        }

        mSocket.emit("search friend", searchRequest, (Ack) args -> {
            runOnUiThread(() -> {
                try {
                    JSONArray data = (JSONArray) args[0];
                    List<Friend> searchResults = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject friendJson = data.getJSONObject(i);
                        Friend friend = new Friend(
                                friendJson.getString("_id"),
                                friendJson.getString("username"),
                                friendJson.getString("firstName"),
                                friendJson.getString("lastName"),
                                friendJson.getString("profilePicture"),
                                friendJson.optString("lastMessage"),
                                friendJson.optString("lastMessageTime")
                        );
                        searchResults.add(friend);
                    }
                    filteredFriendsList.clear();
                    if (searchResults.isEmpty()) {
                        filteredFriendsList.add(new Friend("0", "No friends found", "", "", "", "", ""));
                    } else {
                        filteredFriendsList.addAll(searchResults);
                    }
                    friendsAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing search results: " + e.getMessage());
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off("friends list", onFriendsList);
        }
    }
}

