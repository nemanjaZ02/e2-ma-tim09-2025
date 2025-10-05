package com.e2_ma_tim09_2025.questify.adapters.alliance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.MemberProgress;

import java.util.ArrayList;
import java.util.List;

public class MemberProgressAdapter extends RecyclerView.Adapter<MemberProgressAdapter.MemberProgressViewHolder> {
    
    private List<MemberProgress> memberProgressList;
    
    public MemberProgressAdapter() {
        this.memberProgressList = new ArrayList<>();
    }
    
    public void setMemberProgressList(List<MemberProgress> memberProgressList) {
        this.memberProgressList = memberProgressList != null ? memberProgressList : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public MemberProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_progress, parent, false);
        return new MemberProgressViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MemberProgressViewHolder holder, int position) {
        MemberProgress memberProgress = memberProgressList.get(position);
        holder.bind(memberProgress);
    }
    
    @Override
    public int getItemCount() {
        return memberProgressList.size();
    }
    
    static class MemberProgressViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameText;
        private TextView progressText;
        private TextView damageText;
        private ProgressBar progressBar;
        
        public MemberProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.textViewUsername);
            progressText = itemView.findViewById(R.id.textViewProgress);
            damageText = itemView.findViewById(R.id.textViewDamage);
            progressBar = itemView.findViewById(R.id.progressBarMember);
        }
        
        public void bind(MemberProgress memberProgress) {
            usernameText.setText(memberProgress.getUsername());
            progressText.setText(memberProgress.getCompletedTasks() + "/" + memberProgress.getMaxTasks() + " tasks");
            damageText.setText("Damage: " + memberProgress.getTotalDamage() + " HP");
            
            int progress = (int) memberProgress.getProgressPercentage();
            progressBar.setProgress(progress);
        }
    }
}
