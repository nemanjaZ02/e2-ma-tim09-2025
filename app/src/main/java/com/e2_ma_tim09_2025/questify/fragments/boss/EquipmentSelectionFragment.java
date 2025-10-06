package com.e2_ma_tim09_2025.questify.fragments.boss;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.EquipmentSelectionAdapter;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.viewmodels.EquipmentSelectionViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EquipmentSelectionFragment extends Fragment implements EquipmentSelectionAdapter.OnEquipmentSelectedListener {

    private EquipmentSelectionViewModel viewModel;
    private EquipmentSelectionAdapter adapter;
    private RecyclerView recyclerViewEquipment;
    private LinearLayout layoutSelectedEquipment;
    private TextView tvSelectedEquipmentName;
    private TextView tvSelectedEquipmentUses;
    private Button btnSkip;
    private Button btnApply;
    private ImageButton btnClose;

    private MyEquipment selectedEquipment = null;

    public interface OnEquipmentSelectionCompleteListener {
        void onEquipmentSelected(MyEquipment equipment);
        void onEquipmentSkipped();
        void onFragmentClosed();
    }

    private OnEquipmentSelectionCompleteListener listener;

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
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        loadUserEquipment();
    }

    private void initViews(View view) {
        recyclerViewEquipment = view.findViewById(R.id.recyclerViewEquipment);
        layoutSelectedEquipment = view.findViewById(R.id.layoutSelectedEquipment);
        tvSelectedEquipmentName = view.findViewById(R.id.tvSelectedEquipmentName);
        tvSelectedEquipmentUses = view.findViewById(R.id.tvSelectedEquipmentUses);
        btnSkip = view.findViewById(R.id.btnSkip);
        btnApply = view.findViewById(R.id.btnApply);
        btnClose = view.findViewById(R.id.btnClose);
    }

    private void setupRecyclerView() {
        adapter = new EquipmentSelectionAdapter(new ArrayList<>(), this);
        recyclerViewEquipment.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEquipment.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSkip.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipmentSkipped();
            }
        });

        btnApply.setOnClickListener(v -> {
            if (selectedEquipment != null && listener != null) {
                // Call equipment activation function (commented out as colleague said it doesn't work)
                viewModel.activateEquipment(selectedEquipment);
                listener.onEquipmentSelected(selectedEquipment);
            }
        });

        btnClose.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFragmentClosed();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getUserEquipment().observe(getViewLifecycleOwner(), equipmentList -> {
            if (equipmentList != null) {
                adapter.updateEquipmentList(equipmentList);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Show loading indicator if needed
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserEquipment() {
        viewModel.loadUserEquipment();
    }

    @Override
    public void onEquipmentSelected(MyEquipment equipment) {
        selectedEquipment = equipment;
        updateSelectedEquipmentUI();
        btnApply.setEnabled(true);
    }

    private void updateSelectedEquipmentUI() {
        if (selectedEquipment != null) {
            layoutSelectedEquipment.setVisibility(View.VISIBLE);
            tvSelectedEquipmentName.setText("Equipment ID: " + selectedEquipment.getEquipmentId());
            tvSelectedEquipmentUses.setText("Amount left: " + selectedEquipment.getLeftAmount());
        } else {
            layoutSelectedEquipment.setVisibility(View.GONE);
        }
    }

    public void setOnEquipmentSelectionCompleteListener(OnEquipmentSelectionCompleteListener listener) {
        this.listener = listener;
    }
}
