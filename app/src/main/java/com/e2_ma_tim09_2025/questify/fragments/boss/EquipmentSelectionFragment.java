package com.e2_ma_tim09_2025.questify.fragments.boss;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.bosses.BossMainActivity;
import com.e2_ma_tim09_2025.questify.adapters.EquipmentSelectionAdapter;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.viewmodels.EquipmentSelectionViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EquipmentSelectionFragment extends Fragment {

    private EquipmentSelectionViewModel viewModel;
    private EquipmentSelectionAdapter activeEquipmentAdapter;
    private EquipmentSelectionAdapter availableEquipmentAdapter;
    
    // Views
    private RecyclerView recyclerViewActiveEquipment;
    private RecyclerView recyclerViewAvailableEquipment;
    private Button btnActivateSelected;
    private ImageButton btnClose;
    
    // Selected equipment for activation
    private List<MyEquipment> selectedEquipmentForActivation = new ArrayList<>();

    public static EquipmentSelectionFragment newInstance() {
        return new EquipmentSelectionFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EquipmentSelectionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equipment_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerViews();
        setupClickListeners();
        observeViewModel();
        loadEquipment();
    }

    private void initViews(View view) {
        recyclerViewActiveEquipment = view.findViewById(R.id.recyclerViewActiveEquipment);
        recyclerViewAvailableEquipment = view.findViewById(R.id.recyclerViewAvailableEquipment);
        btnActivateSelected = view.findViewById(R.id.btnActivateSelected);
        btnClose = view.findViewById(R.id.btnClose);
    }

    private void setupRecyclerViews() {
        // Active equipment adapter (display only)
        activeEquipmentAdapter = new EquipmentSelectionAdapter();
        activeEquipmentAdapter.setSelectionMode(false); // Display only
        
        recyclerViewActiveEquipment.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewActiveEquipment.setAdapter(activeEquipmentAdapter);

        // Available equipment adapter (for activation)
        availableEquipmentAdapter = new EquipmentSelectionAdapter();
        availableEquipmentAdapter.setSelectionMode(true); // Enable multi-selection
        availableEquipmentAdapter.setOnEquipmentSelectedListener(this::onEquipmentSelectedForActivation);
        
        recyclerViewAvailableEquipment.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAvailableEquipment.setAdapter(availableEquipmentAdapter);
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> {
            // Close fragment and return to boss battle
            closeFragmentAndReturnToBoss();
        });

        btnActivateSelected.setOnClickListener(v -> {
            if (!selectedEquipmentForActivation.isEmpty()) {
                viewModel.activateEquipment(selectedEquipmentForActivation);
            }
        });
    }

    private void closeFragmentAndReturnToBoss() {
        if (getActivity() instanceof BossMainActivity) {
            BossMainActivity bossActivity = (BossMainActivity) getActivity();
            bossActivity.hideEquipmentSelection();
        } else {
            // Fallback to back press if not in BossMainActivity
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    private void observeViewModel() {
        // Observe active equipment (display only)
        viewModel.getActiveEquipment().observe(getViewLifecycleOwner(), equipmentList -> {
            if (equipmentList != null) {
                activeEquipmentAdapter.updateEquipmentList(equipmentList);
            }
        });

        // Observe available equipment (for activation)
        viewModel.getAvailableEquipment().observe(getViewLifecycleOwner(), equipmentList -> {
            if (equipmentList != null) {
                availableEquipmentAdapter.updateEquipmentList(equipmentList);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnActivateSelected.setEnabled(!isLoading && !selectedEquipmentForActivation.isEmpty());
        });

        // Observe messages
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe activation success
        viewModel.getActivationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                selectedEquipmentForActivation.clear();
                availableEquipmentAdapter.clearSelection();
                updateActivateButton();
                
                // Close fragment and return to boss battle after successful activation
                closeFragmentAndReturnToBoss();
            }
        });
    }

    private void loadEquipment() {
        viewModel.loadUserEquipment();
    }

    private void onEquipmentSelectedForActivation(MyEquipment equipment) {
        if (selectedEquipmentForActivation.contains(equipment)) {
            selectedEquipmentForActivation.remove(equipment);
        } else {
            selectedEquipmentForActivation.add(equipment);
        }
        updateActivateButton();
    }

    private void updateActivateButton() {
        btnActivateSelected.setEnabled(!selectedEquipmentForActivation.isEmpty());
        if (!selectedEquipmentForActivation.isEmpty()) {
            btnActivateSelected.setText("Activate Selected (" + selectedEquipmentForActivation.size() + ")");
        } else {
            btnActivateSelected.setText("Activate Selected Equipment");
        }
    }
}