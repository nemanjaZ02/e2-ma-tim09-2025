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
import com.e2_ma_tim09_2025.questify.adapters.EquipmentRewardAdapter;
import com.e2_ma_tim09_2025.questify.viewmodels.PotentialRewardsViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PotentialRewardsFragment extends Fragment {

    private PotentialRewardsViewModel viewModel;
    private TextView tvKillBossReward;
    private TextView tvWeakenBossReward;
    private ImageButton btnClose;
    
    // RecyclerViews for equipment
    private RecyclerView recyclerViewKillBossClothes;
    private RecyclerView recyclerViewKillBossWeapons;
    private RecyclerView recyclerViewWeakenBossClothes;
    private RecyclerView recyclerViewWeakenBossWeapons;
    
    // Adapters
    private EquipmentRewardAdapter killBossClothesAdapter;
    private EquipmentRewardAdapter killBossWeaponsAdapter;
    private EquipmentRewardAdapter weakenBossClothesAdapter;
    private EquipmentRewardAdapter weakenBossWeaponsAdapter;

    public interface OnFragmentClosedListener {
        void onFragmentClosed();
    }

    private OnFragmentClosedListener listener;

    public static PotentialRewardsFragment newInstance() {
        return new PotentialRewardsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PotentialRewardsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_potential_rewards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerViews();
        setupClickListeners();
        observeViewModel();
        loadRewards();
    }

    private void initViews(View view) {
        tvKillBossReward = view.findViewById(R.id.tvKillBossReward);
        tvWeakenBossReward = view.findViewById(R.id.tvWeakenBossReward);
        btnClose = view.findViewById(R.id.btnClose);
        
        recyclerViewKillBossClothes = view.findViewById(R.id.recyclerViewKillBossClothes);
        recyclerViewKillBossWeapons = view.findViewById(R.id.recyclerViewKillBossWeapons);
        recyclerViewWeakenBossClothes = view.findViewById(R.id.recyclerViewWeakenBossClothes);
        recyclerViewWeakenBossWeapons = view.findViewById(R.id.recyclerViewWeakenBossWeapons);
    }

    private void setupRecyclerViews() {
        // Initialize adapters
        killBossClothesAdapter = new EquipmentRewardAdapter(new ArrayList<>(), 95.0);
        killBossWeaponsAdapter = new EquipmentRewardAdapter(new ArrayList<>(), 5.0);
        weakenBossClothesAdapter = new EquipmentRewardAdapter(new ArrayList<>(), 45.0);
        weakenBossWeaponsAdapter = new EquipmentRewardAdapter(new ArrayList<>(), 2.5);
        
        // Setup RecyclerViews
        recyclerViewKillBossClothes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewKillBossClothes.setAdapter(killBossClothesAdapter);
        
        recyclerViewKillBossWeapons.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewKillBossWeapons.setAdapter(killBossWeaponsAdapter);
        
        recyclerViewWeakenBossClothes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewWeakenBossClothes.setAdapter(weakenBossClothesAdapter);
        
        recyclerViewWeakenBossWeapons.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewWeakenBossWeapons.setAdapter(weakenBossWeaponsAdapter);
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFragmentClosed();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getKillBossReward().observe(getViewLifecycleOwner(), reward -> {
            if (reward != null) {
                tvKillBossReward.setText(reward + " coins");
            }
        });

        viewModel.getWeakenBossReward().observe(getViewLifecycleOwner(), reward -> {
            if (reward != null) {
                tvWeakenBossReward.setText(reward + " coins");
            }
        });
        
        viewModel.getKillBossClothes().observe(getViewLifecycleOwner(), clothes -> {
            if (clothes != null) {
                killBossClothesAdapter.updateEquipmentList(clothes);
            }
        });
        
        viewModel.getKillBossWeapons().observe(getViewLifecycleOwner(), weapons -> {
            if (weapons != null) {
                killBossWeaponsAdapter.updateEquipmentList(weapons);
            }
        });
        
        viewModel.getWeakenBossClothes().observe(getViewLifecycleOwner(), clothes -> {
            if (clothes != null) {
                weakenBossClothesAdapter.updateEquipmentList(clothes);
            }
        });
        
        viewModel.getWeakenBossWeapons().observe(getViewLifecycleOwner(), weapons -> {
            if (weapons != null) {
                weakenBossWeaponsAdapter.updateEquipmentList(weapons);
            }
        });
    }

    private void loadRewards() {
        viewModel.loadRewards();
    }

    public void setOnFragmentClosedListener(OnFragmentClosedListener listener) {
        this.listener = listener;
    }
}