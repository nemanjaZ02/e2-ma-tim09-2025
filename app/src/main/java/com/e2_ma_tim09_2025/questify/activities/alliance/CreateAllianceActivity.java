package com.e2_ma_tim09_2025.questify.activities.alliance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.users.FriendsAdapter;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.viewmodels.CreateAllianceViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateAllianceActivity extends AppCompatActivity {
    
    private CreateAllianceViewModel viewModel;
    private FriendsAdapter adapter;
    private EditText allianceNameInput;
    private Button createButton;
    private Button selectAllButton;
    private TextView selectedFriendsCount;
    private RecyclerView recyclerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_alliance);
        
        initViews();
        setupRecyclerView();
        setupViewModel();
        setupClickListeners();
        
        // Load friends
        viewModel.loadFriends();
    }
    
    private void initViews() {
        allianceNameInput = findViewById(R.id.editTextAllianceName);
        createButton = findViewById(R.id.buttonCreateAlliance);
        selectAllButton = findViewById(R.id.buttonSelectAll);
        selectedFriendsCount = findViewById(R.id.textViewSelectedFriendsCount);
        recyclerView = findViewById(R.id.recyclerViewFriendsSelection);
        
        // Back button
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("CreateAlliance", "*** CREATING FRIENDS ADAPTER - SHOULD NOT HAVE ADD FRIEND BUTTONS ***");
        adapter = new FriendsAdapter(new ArrayList<>(), new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(User friend) {
                // Handle friend click (not used in selection mode)
            }
            
            @Override
            public void onFriendSelectionChanged(String friendId, boolean isSelected) {
                // Update selected count
                selectedFriendsCount.setText(adapter.getSelectedCount() + " selected");
            }
        });
        
        // Enable selection mode
        adapter.setSelectionMode(true);
        recyclerView.setAdapter(adapter);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CreateAllianceViewModel.class);
        
        // Observe friends list
        viewModel.getFriends().observe(this, friends -> {
            if (friends != null) {
                adapter.setFriends(friends);
                if (friends.isEmpty()) {
                    Toast.makeText(this, "No friends found. Add some friends first!", Toast.LENGTH_LONG).show();
                }
            }
        });
        
        // Observe selected friends count
        viewModel.getSelectedFriendsCount().observe(this, count -> {
            selectedFriendsCount.setText(count + " selected");
        });
        
        // Observe alliance creation result
        viewModel.getIsAllianceCreated().observe(this, isCreated -> {
            if (isCreated) {
                String allianceName = allianceNameInput.getText().toString().trim();
                Toast.makeText(this, "Alliance '" + allianceName + "' created successfully!", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupClickListeners() {
        createButton.setOnClickListener(v -> {
            String allianceName = allianceNameInput.getText().toString().trim();
            if (allianceName.isEmpty()) {
                allianceNameInput.setError("Alliance name is required");
                return;
            }
            
            // Get selected friends from adapter
            viewModel.setSelectedFriends(adapter.getSelectedFriendIds());
            viewModel.createAlliance(allianceName);
        });
        
        selectAllButton.setOnClickListener(v -> {
            adapter.selectAll();
            selectedFriendsCount.setText(adapter.getSelectedCount() + " selected");
        });
    }
}
