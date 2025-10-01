package com.e2_ma_tim09_2025.questify.adapters.alliance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.User;

import java.util.List;

public class EligibleUsersAdapter extends RecyclerView.Adapter<EligibleUsersAdapter.UserViewHolder> {
    
    private List<User> users;
    private OnUserInviteClickListener listener;
    
    public interface OnUserInviteClickListener {
        void onInviteClick(User user);
    }
    
    public EligibleUsersAdapter(List<User> users, OnUserInviteClickListener listener) {
        this.users = users;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_eligible_user, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
    
    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameText;
        private TextView levelText;
        private TextView titleText;
        private Button inviteButton;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.textViewUserUsername);
            levelText = itemView.findViewById(R.id.textViewUserLevel);
            titleText = itemView.findViewById(R.id.textViewUserTitle);
            inviteButton = itemView.findViewById(R.id.buttonInvite);
        }
        
        public void bind(User user) {
            usernameText.setText(user.getUsername());
            levelText.setText("Level " + user.getLevel());
            titleText.setText(user.getTitle() != null ? user.getTitle() : "No title");
            
            inviteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInviteClick(user);
                }
            });
        }
    }
}
