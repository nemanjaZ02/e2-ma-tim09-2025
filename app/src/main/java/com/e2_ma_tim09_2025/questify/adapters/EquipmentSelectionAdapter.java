package com.e2_ma_tim09_2025.questify.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;

import java.util.ArrayList;
import java.util.List;

public class EquipmentSelectionAdapter extends RecyclerView.Adapter<EquipmentSelectionAdapter.EquipmentViewHolder> {

    private List<MyEquipment> equipmentList;
    private OnEquipmentSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnEquipmentSelectedListener {
        void onEquipmentSelected(MyEquipment equipment);
    }

    public EquipmentSelectionAdapter(List<MyEquipment> equipmentList, OnEquipmentSelectedListener listener) {
        this.equipmentList = new ArrayList<>(equipmentList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment_selection, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        MyEquipment equipment = equipmentList.get(position);
        holder.bind(equipment, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public void updateEquipmentList(List<MyEquipment> newEquipmentList) {
        this.equipmentList.clear();
        this.equipmentList.addAll(newEquipmentList);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    class EquipmentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivEquipmentIcon;
        private TextView tvEquipmentName;
        private TextView tvEquipmentUses;
        private TextView tvEquipmentUpgrades;
        private ImageView ivSelectionIndicator;
        private TextView tvStatusBadge;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEquipmentIcon = itemView.findViewById(R.id.ivEquipmentIcon);
            tvEquipmentName = itemView.findViewById(R.id.tvEquipmentName);
            tvEquipmentUses = itemView.findViewById(R.id.tvEquipmentUses);
            tvEquipmentUpgrades = itemView.findViewById(R.id.tvEquipmentUpgrades);
            ivSelectionIndicator = itemView.findViewById(R.id.ivSelectionIndicator);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onEquipmentSelected(equipmentList.get(position));
                    }
                }
            });
        }

        public void bind(MyEquipment equipment, boolean isSelected) {
            // Set equipment name (using equipmentId for now since we don't have name)
            tvEquipmentName.setText("Equipment " + equipment.getEquipmentId());
            
            // Set uses left
            tvEquipmentUses.setText("Amount left: " + equipment.getLeftAmount());
            
            // Set upgrades
            tvEquipmentUpgrades.setText("Upgrades: " + equipment.getTimesUpgraded());
            
            // Show selection indicator
            ivSelectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            // Show status badge if activated
            if (equipment.isActivated()) {
                tvStatusBadge.setVisibility(View.VISIBLE);
                tvStatusBadge.setText("ACTIVE");
            } else {
                tvStatusBadge.setVisibility(View.GONE);
            }
            
            // Set equipment icon based on equipmentId
            setEquipmentIcon(equipment.getEquipmentId());
        }
        
        private void setEquipmentIcon(String equipmentId) {
            if (equipmentId == null || equipmentId.isEmpty()) {
                ivEquipmentIcon.setImageResource(R.drawable.ic_equipment_default);
                return;
            }
            
            // Convert equipmentId to lowercase and get resource name
            String resourceName = equipmentId.toLowerCase();
            int resourceId = ivEquipmentIcon.getContext().getResources()
                    .getIdentifier(resourceName, "drawable", ivEquipmentIcon.getContext().getPackageName());
            
            if (resourceId != 0) {
                ivEquipmentIcon.setImageResource(resourceId);
            } else {
                // Fallback to default icon if image not found
                ivEquipmentIcon.setImageResource(R.drawable.ic_equipment_default);
            }
        }
    }
}
