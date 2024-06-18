package com.example.chat_socket.ui;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat_socket.R;
import com.example.chat_socket.adapter.MessageAdapter;
import com.example.chat_socket.model.Message;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText edtMessage;
    private ImageView btnSend;
    private Socket mSocket;
    private String token;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        CircleImageView profileImageView = findViewById(R.id.profile_image1);
        TextView usernameTextView = findViewById(R.id.usernameTextView1);
        ImageView ivBack = findViewById(R.id.ivBack);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        String image = getIntent().getStringExtra("image");
        String name = getIntent().getStringExtra("name");
        String to = getIntent().getStringExtra("to");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        token = preferences.getString("token", null);
        userId = preferences.getString("userId", null);

        Log.d(TAG, "Current Username: " + userId);

        usernameTextView.setText(name);
        Picasso.get().load("http://192.168.1.8:8000/Assets/" + image).into(profileImageView);

        ivBack.setOnClickListener(v -> finish());

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, userId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        fetchPreviousMessages(to);

        mSocket = SocketManager.getSocket();

        btnSend.setOnClickListener(v -> {
            String messageText = edtMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                edtMessage.setText("");
                sendMessage(to, messageText, token);
                chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void fetchPreviousMessages(String user) {
        AsyncTask.execute(() -> {
            try {
                URL url = new URL("http://192.168.1.8:8000/messages?user=" + user);
                Log.d(TAG, "fetchPreviousMessages: " + user);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", token);

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "fetchPreviousMessages: ResponseCode=" + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject responseObject = new JSONObject(result.toString());
                    JSONArray messagesArray = responseObject.getJSONArray("message");

                    List<Message> fetchedMessages = new ArrayList<>();
                    for (int i = 0; i < messagesArray.length(); i++) {
                        JSONObject messageObject = messagesArray.getJSONObject(i);
                        String messageText = messageObject.getString("message");
                        String senderId = messageObject.getString("senderId");
                        Log.d(TAG, "fetchPreviousMessages: " + senderId);
                        Message message = new Message(senderId, messageText);
                        fetchedMessages.add(message);
                    }

                    runOnUiThread(() -> {
                        messageList.clear();
                        messageList.addAll(fetchedMessages);
                        messageAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Failed to load previous messages", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error fetching previous messages", e);
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error loading previous messages", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void sendMessage(String to, String messageText, String token) {
        Message message = new Message(userId, messageText);
        messageAdapter.addMessage(message);

        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put("to", to);
            messageObject.put("message", messageText);
            messageObject.put("token", token);
            messageObject.put("from", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("message", messageObject);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(() -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String messageText = data.getString("message");
                    String senderId = data.getString("userId");
                    Message message = new Message(senderId, messageText);
                    messageAdapter.addMessage(message);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Error receiving message", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}