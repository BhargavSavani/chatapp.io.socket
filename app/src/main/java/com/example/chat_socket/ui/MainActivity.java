package com.example.chat_socket.ui;

import static android.net.http.SslCertificate.saveState;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.chat_socket.model.SocketManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PICK_IMAGE = 1;

    private Socket mSocket;
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private List<Friend> friendsList = new ArrayList<>();
    private List<Friend> filteredFriendsList = new ArrayList<>();
    private SearchView searchView;
    private FloatingActionButton fabCreateGroup;
    private String token;
    private String username;
    private ImageView imageGroupIcon;
    private Uri groupIconUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchView = findViewById(R.id.searchView);

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsAdapter = new FriendsAdapter(this, filteredFriendsList);
        friendsRecyclerView.setAdapter(friendsAdapter);

        fabCreateGroup = findViewById(R.id.fabCreateGroup);
        fabCreateGroup.setOnClickListener(v -> showCreateGroupDialog());

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
        mSocket = SocketManager.getInstance().getSocket();

        mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            sendUsernameToServer(username);
        }));

        mSocket.on("friends list", onFriendsList);
        mSocket.on("search result", onSearchResult);
        mSocket.on("group create success", onGroupCreateSuccess);

        mSocket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show()));
        mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Exception e = (Exception) args[0];
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connection Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        mSocket.connect();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFriends(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFriends(newText);
                return true;
            }
        });
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_group, null);
        builder.setView(dialogView);

        EditText editGroupName = dialogView.findViewById(R.id.editGroupName);
        EditText editGroupDescription = dialogView.findViewById(R.id.editGroupDescription);
        Button btnCreateGroup = dialogView.findViewById(R.id.btnCreateGroup);
        imageGroupIcon = dialogView.findViewById(R.id.imageGroupIcon);

        imageGroupIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });

        AlertDialog dialog = builder.create();

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = editGroupName.getText().toString().trim();
            String groupDescription = editGroupDescription.getText().toString().trim();
            createGroup(groupName, groupDescription, groupIconUri);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            groupIconUri = data.getData();
            imageGroupIcon.setImageURI(groupIconUri);
        }
    }

    private void createGroup(String groupName, String groupDescription, Uri groupIconUri) {
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject groupObject = new JSONObject();
        try {
            groupObject.put("groupName", groupName);
            groupObject.put("groupDescription", groupDescription);
            groupObject.put("username", username); // Assuming you want to include the username in the group creation request

            if (groupIconUri != null) {
                String imagePath = getPathFromUri(groupIconUri);
                groupObject.put("groupIcon", imagePath);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("create group", groupObject);
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
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
                showLogoutConfirmationDialog();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            logout();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void logout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().clear().apply();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
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
                    // The response is a JSONObject, not JSONArray
                    JSONObject response = (JSONObject) args[0];
                    Log.d(TAG, "Friends and groups list received: " + response.toString());

                    // Extract the friends and groups arrays from the response
                    JSONArray friendsData = response.getJSONArray("friends");
                    JSONArray groupsData = response.getJSONArray("groups");

                    friendsList.clear();
                    for (int i = 0; i < friendsData.length(); i++) {
                        JSONObject friendJson = friendsData.getJSONObject(i);
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
                    for (int i = 0; i < groupsData.length(); i++) {
                        JSONObject groupJson = groupsData.getJSONObject(i);
                        Friend group = new Friend(
                                groupJson.getString("_id"),
                                groupJson.getString("groupName"),
                                groupJson.optString("groupDescription", ""),
                                groupJson.getString("groupIcon")
                        );
                        friendsList.add(group);
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

        JSONObject searchRequest = new JSONObject();
        try {
            searchRequest.put("token", token);
            searchRequest.put("query", query);
        } catch (JSONException e) {
            Log.e(TAG, "Error forming search request: " + e.getMessage());
        }

        Log.d(TAG, "searchFriends: Sending search request: " + searchRequest.toString());
        mSocket.emit("search friend", searchRequest);
    }

    private void filterFriends(String query) {
        filteredFriendsList.clear();
        if (query.isEmpty()) {
            filteredFriendsList.addAll(friendsList);
        } else {
            for (Friend friend : friendsList) {
                String username = friend.getUsername() != null ? friend.getUsername().toLowerCase() : "";
                String firstName = friend.getFirstName() != null ? friend.getFirstName().toLowerCase() : "";
                String lastName = friend.getLastName() != null ? friend.getLastName().toLowerCase() : "";
                String groupName = friend.getGroupName() != null ? friend.getGroupName().toLowerCase() : "";

                if (username.contains(query.toLowerCase()) ||
                        firstName.contains(query.toLowerCase()) ||
                        lastName.contains(query.toLowerCase()) ||
                        groupName.contains(query.toLowerCase())) {
                    filteredFriendsList.add(friend);
                }
            }
        }
        friendsAdapter.notifyDataSetChanged();
    }

    private Emitter.Listener onSearchResult = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(() -> {
                try {
                    Object data = args[0];
                    List<Friend> searchResults = new ArrayList<>();
                    if (data instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) data;
                        Log.d(TAG, "Search results received (JSONArray): " + jsonArray.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject friendJson = jsonArray.getJSONObject(i);
                            if (friendJson.has("username")) { // Check if it is a friend
                                Friend friend = new Friend(
                                        friendJson.getString("_id"),
                                        friendJson.optString("username"),
                                        friendJson.optString("firstName"),
                                        friendJson.optString("lastName"),
                                        friendJson.optString("profilePicture"),
                                        friendJson.optString("lastMessage"),
                                        friendJson.optString("lastMessageTime")
                                );
                                searchResults.add(friend);
                            } else { // Assume it's a group if there's no username
                                Friend group = new Friend(
                                        friendJson.getString("_id"),
                                        friendJson.optString("groupName"),
                                        friendJson.optString("groupDescription"),
                                        friendJson.optString("groupIcon")
                                );
                                searchResults.add(group);
                            }
                        }
                    } else if (data instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) data;
                        Log.d(TAG, "Search results received (JSONObject): " + jsonObject.toString());
                        if (jsonObject.has("username")) { // Check if it is a friend
                            Friend friend = new Friend(
                                    jsonObject.getString("_id"),
                                    jsonObject.optString("username"),
                                    jsonObject.optString("firstName"),
                                    jsonObject.optString("lastName"),
                                    jsonObject.optString("profilePicture"),
                                    jsonObject.optString("lastMessage"),
                                    jsonObject.optString("lastMessageTime")
                            );
                            searchResults.add(friend);
                        } else { // Assume it's a group if there's no username
                            Friend group = new Friend(
                                    jsonObject.getString("_id"),
                                    jsonObject.optString("groupName"),
                                    jsonObject.optString("groupDescription"),
                                    jsonObject.optString("groupIcon")
                            );
                            searchResults.add(group);
                        }
                    }

                    filteredFriendsList.clear();
                    if (searchResults.isEmpty()) {
                        filteredFriendsList.add(new Friend("0", "No friends or groups found", "", ""));
                    } else {
                        filteredFriendsList.addAll(searchResults);
                    }
                    friendsAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing search results: " + e.getMessage());
                }
            });
        }
    };


    private Emitter.Listener onGroupCreateSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(() -> {
                try {
                    JSONObject groupJson = (JSONObject) args[0];
                    Log.d(TAG, "Group create success received: " + groupJson.toString());
                    Friend group = new Friend(
                            groupJson.getString("_id"),
                            groupJson.getString("groupName"),
                            groupJson.getString("groupDescription"),
                            groupJson.getString("groupIcon")
                    );
                    friendsList.add(0, group); // Insert at the beginning of the list
                    filteredFriendsList.clear();
                    filteredFriendsList.addAll(friendsList);
                    friendsAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing group create success: " + e.getMessage());
                }
            });
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("friends list", onFriendsList);
        mSocket.off("search result", onSearchResult);
        mSocket.off("group create success", onGroupCreateSuccess);
        mSocket.disconnect();
    }
}
