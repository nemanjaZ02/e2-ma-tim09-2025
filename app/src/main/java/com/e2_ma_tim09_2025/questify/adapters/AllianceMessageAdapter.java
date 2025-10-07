package com.e2_ma_tim09_2025.questify.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.AllianceMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllianceMessageAdapter extends RecyclerView.Adapter<AllianceMessageAdapter.MessageViewHolder> {
    private List<AllianceMessage> messages;
    private String currentUserId;
    private SimpleDateFormat timeFormat;

    public AllianceMessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public void updateMessages(List<AllianceMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(AllianceMessage message) {
        if (messages != null) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged(); // Refresh to update message positions
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        
        // Use different layouts for sent vs received messages
        if (viewType == 0) { // Sent message (right side)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else { // Received message (left side)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        if (messages != null && position < messages.size()) {
            AllianceMessage message = messages.get(position);
            holder.bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages != null && position < messages.size() && currentUserId != null) {
            AllianceMessage message = messages.get(position);
            // Return 0 for sent messages (current user), 1 for received messages
            return message.getSenderId().equals(currentUserId) ? 0 : 1;
        }
        return 1; // Default to received message layout
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvSender;
        private TextView tvTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(AllianceMessage message) {
            tvMessage.setText(message.getMessage());
            
            // Only show sender name for received messages
            if (tvSender != null) {
                tvSender.setText(message.getSenderName());
            }
            
            // Format timestamp
            Date date = new Date(message.getTimestamp());
            String timeString = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
            tvTime.setText(timeString);
        }
    }
}
