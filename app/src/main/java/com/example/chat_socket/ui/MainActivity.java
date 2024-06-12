    package com.example.chat_socket.ui;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.preference.PreferenceManager;
    import android.util.Log;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.chat_socket.adapter.FriendsAdapter;
    import com.example.chat_socket.R;
    import com.example.chat_socket.model.Friend;
    import com.example.chat_socket.model.Message;
    import com.example.chat_socket.model.User;
    import com.example.chat_socket.service.ApiService;

    import org.json.JSONException;
    import org.json.JSONObject;

    import java.net.URISyntaxException;
    import java.util.ArrayList;
    import java.util.List;

    import io.socket.client.IO;
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

            friendsRecyclerView = findViewById(R.id.friendsRecyclerView);

            friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            friendsAdapter = new FriendsAdapter(this,friendsList);
            friendsRecyclerView.setAdapter(friendsAdapter);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String token = preferences.getString("token", null);

            if (token == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // Initialize Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.9:8000")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);

            getUserDetails(token);

            try {
                IO.Options options = IO.Options.builder()
                        .setQuery("token=" + token)
                        .build();
                mSocket = IO.socket("http://192.168.1.9:8000", options);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show()))
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

        private void getUserDetails(String token) {
            Call<User> call = apiService.getUserDetails(token);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        Log.d(TAG, "onResponse: User details: " + user.toString());
                        friendsList.clear();
                        friendsList.addAll(user.getFriends());
                        friendsAdapter.notifyDataSetChanged();

                        friendsRecyclerView.scrollToPosition(friendsList.size() - 1);

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
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT);
            mSocket.off("message");
            mSocket.off(Socket.EVENT_DISCONNECT);
            mSocket.off(Socket.EVENT_CONNECT_ERROR);
        }
    }

