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

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.viewmodels.PotentialRewardsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PotentialRewardsFragment extends Fragment {

    private PotentialRewardsViewModel viewModel;
    private TextView tvKillBossReward;
    private TextView tvWeakenBossReward;
    private ImageButton btnClose;

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
        setupClickListeners();
        observeViewModel();
        loadRewards();
    }

    private void initViews(View view) {
        tvKillBossReward = view.findViewById(R.id.tvKillBossReward);
        tvWeakenBossReward = view.findViewById(R.id.tvWeakenBossReward);
        btnClose = view.findViewById(R.id.btnClose);
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
    }

    private void loadRewards() {
        viewModel.loadRewards();
    }

    public void setOnFragmentClosedListener(OnFragmentClosedListener listener) {
        this.listener = listener;
    }
}
