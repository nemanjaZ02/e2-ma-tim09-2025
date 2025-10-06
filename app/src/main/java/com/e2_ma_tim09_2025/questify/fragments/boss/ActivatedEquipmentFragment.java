package com.e2_ma_tim09_2025.questify.fragments.boss;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.ActivatedEquipmentAdapter;
import com.e2_ma_tim09_2025.questify.viewmodels.ActivatedEquipmentViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivatedEquipmentFragment extends Fragment {

    private ActivatedEquipmentViewModel viewModel;
    private ActivatedEquipmentAdapter adapter;
    private RecyclerView recyclerViewActivatedEquipment;
    private TextView tvNoEquipment;
    private ImageButton btnClose;

    public interface OnFragmentClosedListener {
        void onFragmentClosed();
    }

    private OnFragmentClosedListener listener;

    public static ActivatedEquipmentFragment newInstance() {
        return new ActivatedEquipmentFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ActivatedEquipmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activated_equipment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        loadActivatedEquipment();
    }

    private void initViews(View view) {
        recyclerViewActivatedEquipment = view.findViewById(R.id.recyclerViewActivatedEquipment);
        tvNoEquipment = view.findViewById(R.id.tvNoEquipment);
        btnClose = view.findViewById(R.id.btnClose);
    }

    private void setupRecyclerView() {
        adapter = new ActivatedEquipmentAdapter(new ArrayList<>());
        recyclerViewActivatedEquipment.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewActivatedEquipment.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFragmentClosed();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getActivatedEquipment().observe(getViewLifecycleOwner(), equipmentList -> {
            if (equipmentList != null) {
                adapter.updateEquipmentList(equipmentList);
                
                // Show/hide no equipment message
                if (equipmentList.isEmpty()) {
                    tvNoEquipment.setVisibility(View.VISIBLE);
                    recyclerViewActivatedEquipment.setVisibility(View.GONE);
                } else {
                    tvNoEquipment.setVisibility(View.GONE);
                    recyclerViewActivatedEquipment.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Show loading indicator if needed
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Handle error if needed
            }
        });
    }

    private void loadActivatedEquipment() {
        viewModel.loadActivatedEquipment();
    }

    public void setOnFragmentClosedListener(OnFragmentClosedListener listener) {
        this.listener = listener;
    }
}
