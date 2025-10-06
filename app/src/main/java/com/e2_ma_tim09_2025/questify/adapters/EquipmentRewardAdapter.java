package com.e2_ma_tim09_2025.questify.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.Equipment;

import java.util.ArrayList;
import java.util.List;

public class EquipmentRewardAdapter extends RecyclerView.Adapter<EquipmentRewardAdapter.EquipmentViewHolder> {

    private List<Equipment> equipmentList;
    private double chancePercentage;

    public EquipmentRewardAdapter(List<Equipment> equipmentList, double chancePercentage) {
        this.equipmentList = new ArrayList<>(equipmentList);
        this.chancePercentage = chancePercentage;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment_reward, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);
        holder.bind(equipment, chancePercentage);
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public void updateEquipmentList(List<Equipment> newEquipmentList) {
        this.equipmentList.clear();
        this.equipmentList.addAll(newEquipmentList);
        notifyDataSetChanged();
    }

    class EquipmentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivEquipmentIcon;
        private TextView tvEquipmentName;
        private TextView tvChance;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEquipmentIcon = itemView.findViewById(R.id.ivEquipmentIcon);
            tvEquipmentName = itemView.findViewById(R.id.tvEquipmentName);
            tvChance = itemView.findViewById(R.id.tvChance);
        }

        public void bind(Equipment equipment, double chancePercentage) {
            // Set equipment name
            tvEquipmentName.setText(equipment.getName());
            
            // Set chance percentage
            tvChance.setText(chancePercentage + "%");
            
            // Set equipment icon based on equipment id
            setEquipmentIcon(equipment.getId());
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
