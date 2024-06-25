package com.example.chat_socket.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_socket.R;
import com.example.chat_socket.model.Friend;
import com.example.chat_socket.ui.ChatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<Friend> friendsList;
    private Context context;

    public FriendsAdapter(Context context, List<Friend> friendsList) {
        this.context = context;
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);

        holder.tvFullName.setText(friend.getFirstName() + " " + friend.getLastName());

        if (friend.getLastMessage() != null) {
            holder.lastMessageTextView.setText(friend.getLastMessage());
           // holder.lastMessageTimeTextView.setText(formatDate(friend.getLastMessageTime()));
        } else {
            holder.lastMessageTextView.setText("No messages yet");
            holder.lastMessageTimeTextView.setText("");
        }

        // Use Glide to load profile pictures
        Glide.with(context)
                .load("http://192.168.1.7:8000/Assets/" + friend.getProfilePicture())
                .placeholder(R.drawable.profile)
                .into(holder.profileImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("image", friend.getProfilePicture());
                intent.putExtra("name", friend.getFirstName() + " " + friend.getLastName());
                intent.putExtra("to", friend.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName;
        CircleImageView profileImageView;
        TextView lastMessageTextView;
        TextView lastMessageTimeTextView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            profileImageView = itemView.findViewById(R.id.profile_image);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            lastMessageTimeTextView = itemView.findViewById(R.id.lastMessageTimeTextView);
        }
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
}
