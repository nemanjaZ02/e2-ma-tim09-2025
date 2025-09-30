package com.e2_ma_tim09_2025.questify.adapters.users;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.User;
import androidx.cardview.widget.CardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<User> friends;
    private OnFriendClickListener listener;
    private Set<String> selectedFriendIds = new HashSet<>();
    private boolean isSelectionMode = false;

    public interface OnFriendClickListener {
        void onFriendClick(User friend);
        void onFriendSelectionChanged(String friendId, boolean isSelected);
    }

    public FriendsAdapter(List<User> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friends.get(position);

        // Set username and level
        holder.userName.setText(friend.getUsername());
        holder.textViewLevel.setText("Level " + friend.getLevel());

        // Load avatar from drawable resource name
        String avatarName = friend.getAvatar();
        int drawableResId = holder.itemView.getContext()
                .getResources()
                .getIdentifier(avatarName, "drawable", holder.itemView.getContext().getPackageName());
        if (drawableResId != 0) {
            holder.userAvatar.setImageResource(drawableResId);
        } else {
            holder.userAvatar.setImageResource(R.drawable.ninja); // fallback avatar
        }

        // Handle selection mode
        if (isSelectionMode) {
            holder.checkboxSelect.setVisibility(View.VISIBLE);
            boolean isSelected = selectedFriendIds.contains(friend.getId());
            holder.checkboxSelect.setChecked(isSelected);

            // Update visual state
            updateVisualState(holder, friend.getId());
            
            holder.checkboxSelect.setOnCheckedChangeListener(null); // Remove previous listener to avoid conflicts
            holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedFriendIds.add(friend.getId());
                } else {
                    selectedFriendIds.remove(friend.getId());
                }
                if (listener != null) {
                    listener.onFriendSelectionChanged(friend.getId(), isChecked);
                }
                // Update visual state immediately
                updateVisualState(holder, friend.getId());
            });
        } else {
            holder.checkboxSelect.setVisibility(View.GONE);
            holder.cardViewFriend.setCardBackgroundColor(0xFFFFFFFF); // White background
            holder.cardViewFriend.setCardElevation(4); // Normal elevation
        }

        // Click on item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                if (isSelectionMode) {
                    // Toggle selection
                    boolean isCurrentlySelected = selectedFriendIds.contains(friend.getId());
                    holder.checkboxSelect.setChecked(!isCurrentlySelected);
                    updateVisualState(holder, friend.getId());
                } else {
                    listener.onFriendClick(friend);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewFriend;
        CheckBox checkboxSelect;
        ShapeableImageView userAvatar;
        TextView userName;
        TextView textViewLevel;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewFriend = (CardView) itemView; // The itemView itself is the CardView
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.textViewUsername);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);
        }
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        notifyDataSetChanged();
    }

    public void selectAll() {
        selectedFriendIds.clear();
        for (User friend : friends) {
            selectedFriendIds.add(friend.getId());
        }
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedFriendIds.clear();
        notifyDataSetChanged();
    }

    public Set<String> getSelectedFriendIds() {
        return new HashSet<>(selectedFriendIds);
    }

    public int getSelectedCount() {
        return selectedFriendIds.size();
    }
    
    private void updateVisualState(FriendViewHolder holder, String friendId) {
        boolean isSelected = selectedFriendIds.contains(friendId);

        if (isSelected) {
            holder.cardViewFriend.setCardBackgroundColor(0xFFD4EDDA); // More visible light green background
            holder.cardViewFriend.setCardElevation(8); // Higher elevation for selected items
        } else {
            holder.cardViewFriend.setCardBackgroundColor(0xFFFFFFFF); // White background
            holder.cardViewFriend.setCardElevation(4); // Normal elevation
        }
    }
}
