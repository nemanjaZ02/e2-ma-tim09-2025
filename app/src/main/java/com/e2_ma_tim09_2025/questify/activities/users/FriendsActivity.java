package com.e2_ma_tim09_2025.questify.activities.users;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.alliance.CreateAllianceActivity;
import com.e2_ma_tim09_2025.questify.adapters.users.FriendsAdapter;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.viewmodels.FriendsViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint

public class FriendsActivity extends AppCompatActivity {
    private FriendsViewModel viewModel;
    private FriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsAdapter(new ArrayList<>(), new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(User friend) {
                // handle friend click
            }

            @Override
            public void onFriendSelectionChanged(String friendId, boolean isSelected) {
                // Not used in friends list view
            }
        });
        Button createAllianceBtn = findViewById(R.id.btnCreateAlliance);
        createAllianceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsActivity.this, CreateAllianceActivity.class);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        // Observe friends list
        viewModel.getUsers().observe(this, friends -> adapter.setFriends(friends));

        // Fetch friends
        viewModel.fetchUsers();
    }
}
