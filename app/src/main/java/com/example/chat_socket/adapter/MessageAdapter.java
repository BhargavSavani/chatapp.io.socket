package com.example.chat_socket.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_socket.R;
import com.example.chat_socket.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter";

    private List<Message> messageList;
    private String userId;

    public MessageAdapter(List<Message> messageList, String userId) {
        this.messageList = messageList;
        this.userId = userId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        Log.d("MessageAdapter", "Message Username: " + message.getSenderId());
        Log.d("MessageAdapter", "Current Username: " + userId);
        if (message.getSenderId().equals(userId)) {
            return 1; // Sent message
        } else {
            return 0; // Received message
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) { // Sent message
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
        } else { // Received message
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_friend, parent, false);
        }
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageTextView.setText(message.getMessage());

        if (holder.viewType == 1) { // Sent message
            if (message.isRead()) {
                holder.ivTick.setVisibility(View.VISIBLE);
                holder.ivTick.setImageResource(R.drawable.baseline_done_all); // Double tick for read
            } else {
                holder.ivTick.setVisibility(View.VISIBLE);
                holder.ivTick.setImageResource(R.drawable.baseline_done); // Single tick for sent
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void updateMessageReadStatus(String messageId, boolean isRead) {
        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            if (message.getMessageId().equals(messageId)) {
                message.setRead(isRead);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView ivTick;
        int viewType;

        MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            messageTextView = itemView.findViewById(R.id.messageTextView);
            if (viewType == 1) { // Sent message
                ivTick = itemView.findViewById(R.id.ivTick);
            }
        }
    }
}
