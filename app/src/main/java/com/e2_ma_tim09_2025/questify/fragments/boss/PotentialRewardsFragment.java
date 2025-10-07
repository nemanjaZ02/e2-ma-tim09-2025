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
import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.enums.EquipmentType;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;
import com.e2_ma_tim09_2025.questify.viewmodels.BossViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PotentialRewardsFragment extends Fragment {

    private BossViewModel bossViewModel;
    
    @Inject
    EquipmentService equipmentService;
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
        bossViewModel = new ViewModelProvider(requireActivity()).get(BossViewModel.class);
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
        observeBossViewModel();
        loadEquipment();
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

    private void observeBossViewModel() {
        // Observe coins drop from BossViewModel
        bossViewModel.getCoinsDrop().observe(getViewLifecycleOwner(), coinsDrop -> {
            if (coinsDrop != null) {
                // Kill boss reward = full coins drop
                tvKillBossReward.setText("Kill Boss: " + coinsDrop + " coins");
                
                // Weaken boss reward = half coins drop
                int weakenReward = coinsDrop / 2;
                tvWeakenBossReward.setText("Weaken Boss: " + weakenReward + " coins");
                
                System.out.println("DEBUG: PotentialRewards - CoinsDrop: " + coinsDrop + 
                                 ", KillReward: " + coinsDrop + ", WeakenReward: " + weakenReward);
            }
        });
    }

    private void loadEquipment() {
        // Load equipment for potential rewards
        equipmentService.getAllEquipment(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Equipment> equipmentList = task.getResult();
                
                // Filter clothes and weapons
                List<Equipment> clothes = new ArrayList<>();
                List<Equipment> weapons = new ArrayList<>();
                
                for (Equipment equipment : equipmentList) {
                    if (equipment.getType() == EquipmentType.CLOTHES) {
                        clothes.add(equipment);
                    } else if (equipment.getType() == EquipmentType.WEAPON) {
                        weapons.add(equipment);
                    }
                }
                
                // Set the equipment lists (same for both kill and weaken scenarios)
                killBossClothesAdapter.updateEquipmentList(clothes);
                killBossWeaponsAdapter.updateEquipmentList(weapons);
                weakenBossClothesAdapter.updateEquipmentList(clothes);
                weakenBossWeaponsAdapter.updateEquipmentList(weapons);
            } else {
                // Set empty lists if equipment loading fails
                killBossClothesAdapter.updateEquipmentList(new ArrayList<>());
                killBossWeaponsAdapter.updateEquipmentList(new ArrayList<>());
                weakenBossClothesAdapter.updateEquipmentList(new ArrayList<>());
                weakenBossWeaponsAdapter.updateEquipmentList(new ArrayList<>());
            }
        });
    }

    public void setOnFragmentClosedListener(OnFragmentClosedListener listener) {
        this.listener = listener;
    }
}