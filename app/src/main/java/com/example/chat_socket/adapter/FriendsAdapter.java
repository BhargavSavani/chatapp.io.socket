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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FRIEND = 1;
    private static final int VIEW_TYPE_GROUP = 2;

    private List<Friend> friendsList;
    private Context context;

    public FriendsAdapter(Context context, List<Friend> friendsList) {
        this.context = context;
        this.friendsList = friendsList;
    }

    @Override
    public int getItemViewType(int position) {
        Friend friend = friendsList.get(position);
        return friend.getGroupName() != null ? VIEW_TYPE_GROUP : VIEW_TYPE_FRIEND;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FRIEND) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
            return new FriendViewHolder(view);
        } else if (viewType == VIEW_TYPE_GROUP) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            return new GroupViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Friend friend = friendsList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_FRIEND) {
            FriendViewHolder friendHolder = (FriendViewHolder) holder;
            friendHolder.bind(friend);
        } else if (holder.getItemViewType() == VIEW_TYPE_GROUP) {
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            groupHolder.bind(friend);
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {

        private TextView tvFullName;
        private CircleImageView profileImageView;
        private TextView lastMessageTextView;
        private TextView lastMessageTimeTextView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            profileImageView = itemView.findViewById(R.id.profile_image);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            lastMessageTimeTextView = itemView.findViewById(R.id.lastMessageTimeTextView);
        }

        public void bind(Friend friend) {
            tvFullName.setText(friend.getFirstName() + " " + friend.getLastName());

            if (friend.getLastMessage() != null) {
                lastMessageTextView.setText(friend.getLastMessage());
                lastMessageTimeTextView.setText((friend.getLastMessageTime()));
            } else {
                lastMessageTextView.setText("No messages yet");
                lastMessageTimeTextView.setText("");
            }

            // Use Glide to load profile pictures
            Glide.with(context)
                    .load("http://192.168.1.8:8000/Assets/" + friend.getProfilePicture())
                    .placeholder(R.drawable.profile)
                    .into(profileImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
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
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {

        private TextView tvGroupName;
        private CircleImageView groupIconImageView;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            groupIconImageView = itemView.findViewById(R.id.group_icon);
        }

        public void bind(Friend group) {
            tvGroupName.setText(group.getGroupName());

            // Load group icon using Glide
            Glide.with(context)
                    .load("http://192.168.1.8:8000/Assets/" + group.getGroupIcon())
                    .placeholder(R.drawable.profile)
                    .into(groupIconImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("image", group.getGroupIcon());
                    intent.putExtra("name", group.getGroupName());
                    intent.putExtra("to", group.getId());
                    context.startActivity(intent);                }
            });
        }
    }
}
