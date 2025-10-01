package com.e2_ma_tim09_2025.questify.adapters.alliance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.User;

import java.util.List;

public class AllianceMembersAdapter extends RecyclerView.Adapter<AllianceMembersAdapter.MemberViewHolder> {
    
    private List<User> members;
    
    public AllianceMembersAdapter(List<User> members) {
        this.members = members;
    }
    
    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alliance_member, parent, false);
        return new MemberViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = members.get(position);
        holder.bind(member);
    }
    
    @Override
    public int getItemCount() {
        return members.size();
    }
    
    public void setMembers(List<User> members) {
        this.members = members;
        notifyDataSetChanged();
    }
    
    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameText;
        private TextView levelText;
        private TextView titleText;
        
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.textViewMemberUsername);
            levelText = itemView.findViewById(R.id.textViewMemberLevel);
            titleText = itemView.findViewById(R.id.textViewMemberTitle);
        }
        
        public void bind(User member) {
            usernameText.setText(member.getUsername());
            levelText.setText("Level " + member.getLevel());
            titleText.setText(member.getTitle() != null ? member.getTitle() : "No title");
        }
    }
}
