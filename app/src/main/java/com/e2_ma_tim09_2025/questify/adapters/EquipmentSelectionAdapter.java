package com.e2_ma_tim09_2025.questify.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EquipmentSelectionAdapter extends RecyclerView.Adapter<EquipmentSelectionAdapter.EquipmentViewHolder> {

    private List<MyEquipment> equipmentList;
    private Set<MyEquipment> selectedEquipment = new HashSet<>();
    private boolean selectionMode = false;
    private OnEquipmentSelectedListener listener;

    public interface OnEquipmentSelectedListener {
        void onEquipmentSelected(MyEquipment equipment);
    }

    public EquipmentSelectionAdapter() {
        this.equipmentList = new ArrayList<>();
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        notifyDataSetChanged();
    }

    public void setOnEquipmentSelectedListener(OnEquipmentSelectedListener listener) {
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
        boolean isSelected = selectedEquipment.contains(equipment);
        holder.bind(equipment, isSelected, selectionMode);
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public void updateEquipmentList(List<MyEquipment> newEquipmentList) {
        this.equipmentList.clear();
        this.equipmentList.addAll(newEquipmentList);
        selectedEquipment.clear();
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedEquipment.clear();
        notifyDataSetChanged();
    }

    class EquipmentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivEquipmentIcon;
        private TextView tvEquipmentName;
        private TextView tvEquipmentDetails;
        private CheckBox checkboxSelection;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEquipmentIcon = itemView.findViewById(R.id.ivEquipmentIcon);
            tvEquipmentName = itemView.findViewById(R.id.tvEquipmentName);
            tvEquipmentDetails = itemView.findViewById(R.id.tvEquipmentDetails);
            checkboxSelection = itemView.findViewById(R.id.checkboxSelection);
        }

        public void bind(MyEquipment equipment, boolean isSelected, boolean selectionMode) {
            // Set equipment name
            tvEquipmentName.setText("Equipment " + equipment.getEquipmentId());
            
            // Set equipment details
            String details = "Amount: " + equipment.getLeftAmount();
            if (equipment.isActivated()) {
                details += " | ACTIVE";
            }
            tvEquipmentDetails.setText(details);
            
            // Set equipment icon
            setEquipmentIcon(equipment.getEquipmentId());
            
            // Handle selection mode
            if (selectionMode) {
                checkboxSelection.setVisibility(View.VISIBLE);
                checkboxSelection.setChecked(isSelected);
                itemView.setBackgroundColor(isSelected ? 
                    itemView.getContext().getResources().getColor(R.color.selected_background) : 
                    itemView.getContext().getResources().getColor(R.color.default_background));
                
                // Set checkbox click listener
                checkboxSelection.setOnCheckedChangeListener((buttonView, checked) -> {
                    if (listener != null) {
                        listener.onEquipmentSelected(equipment);
                    }
                });
                
                // Set item click listener (for tapping anywhere on the item)
                itemView.setOnClickListener(v -> {
                    checkboxSelection.setChecked(!checkboxSelection.isChecked());
                });
            } else {
                checkboxSelection.setVisibility(View.GONE);
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.default_background));
                itemView.setOnClickListener(null);
                checkboxSelection.setOnCheckedChangeListener(null);
            }
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