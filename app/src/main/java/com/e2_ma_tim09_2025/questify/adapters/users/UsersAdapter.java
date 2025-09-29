package com.e2_ma_tim09_2025.questify.adapters.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users;
    private OnUserClickListener listener;
    private List<User> allUsers; // keep a copy of all users


    public interface OnUserClickListener {
        void onUserClick(User user);
        void onAddFriendClick(User user);
    }

    public UsersAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
        this.allUsers = new ArrayList<>(users); // backup

    }
    public void filter(String query) {
        users.clear(); // clear the displayed list
        if (query == null || query.isEmpty()) {
            users.addAll(allUsers); // show all users
        } else {
            String lower = query.toLowerCase();
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(lower)) {
                    users.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position); // use the displayed list

        // Set username
        holder.userName.setText(user.getUsername());

        // Load avatar from drawable resource name
        String avatarName = user.getAvatar();
        int drawableResId = holder.itemView.getContext()
                .getResources()
                .getIdentifier(avatarName, "drawable", holder.itemView.getContext().getPackageName());
        if (drawableResId != 0) {
            holder.userAvatar.setImageResource(drawableResId);
        } else {
            holder.userAvatar.setImageResource(R.drawable.ninja); // fallback avatar
        }

        // Click on item opens profile
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });

        // Click on Add Friend
        holder.btnAddFriend.setOnClickListener(v -> {
            if (listener != null) listener.onAddFriendClick(user);
        });
    }



    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView userAvatar;
        TextView userName;
        Button btnAddFriend;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }
    }
    public void setUsers(List<User> users) {
        this.users = users;
        this.allUsers = new ArrayList<>(users); // update backup
        notifyDataSetChanged();
    }
    public void removeUserById(String userId) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(userId)) {
                users.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
    

}

