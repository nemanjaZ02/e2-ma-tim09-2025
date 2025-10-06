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

public class ActivatedEquipmentAdapter extends RecyclerView.Adapter<ActivatedEquipmentAdapter.EquipmentViewHolder> {

    private List<MyEquipment> equipmentList;

    public ActivatedEquipmentAdapter(List<MyEquipment> equipmentList) {
        this.equipmentList = new ArrayList<>(equipmentList);
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activated_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        MyEquipment equipment = equipmentList.get(position);
        holder.bind(equipment);
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public void updateEquipmentList(List<MyEquipment> newEquipmentList) {
        this.equipmentList.clear();
        this.equipmentList.addAll(newEquipmentList);
        notifyDataSetChanged();
    }

    class EquipmentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivEquipmentIcon;
        private TextView tvEquipmentName;
        private TextView tvEquipmentUses;
        private TextView tvEquipmentUpgrades;
        private TextView tvActiveBadge;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEquipmentIcon = itemView.findViewById(R.id.ivEquipmentIcon);
            tvEquipmentName = itemView.findViewById(R.id.tvEquipmentName);
            tvEquipmentUses = itemView.findViewById(R.id.tvEquipmentUses);
            tvEquipmentUpgrades = itemView.findViewById(R.id.tvEquipmentUpgrades);
            tvActiveBadge = itemView.findViewById(R.id.tvActiveBadge);
        }

        public void bind(MyEquipment equipment) {
            // Set equipment name (using equipmentId for now since we don't have name)
            tvEquipmentName.setText(equipment.getEquipmentId());
            
            // Set uses left
            tvEquipmentUses.setText("Amount left: " + equipment.getLeftAmount());
            
            // Set upgrades
            tvEquipmentUpgrades.setText("Upgrades: " + equipment.getTimesUpgraded());
            
            // Show active badge
            tvActiveBadge.setVisibility(View.VISIBLE);
            
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
