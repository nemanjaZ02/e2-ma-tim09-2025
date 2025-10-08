package com.e2_ma_tim09_2025.questify.activities.alliance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.AllianceMessageAdapter;
import com.e2_ma_tim09_2025.questify.models.AllianceMessage;
import com.e2_ma_tim09_2025.questify.viewmodels.AllianceChatViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AllianceChatActivity extends AppCompatActivity {
    
    private AllianceChatViewModel viewModel;
    private AllianceMessageAdapter messageAdapter;
    
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private Button buttonSend;
    private ImageButton buttonBack;
    private TextView textViewAllianceName;
    private ProgressBar progressBar;
    private TextView textViewError;
    
    private String allianceId;
    private String allianceName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_chat);
        
        // Get alliance data from intent
        allianceId = getIntent().getStringExtra("allianceId");
        allianceName = getIntent().getStringExtra("allianceName");
        
        if (allianceId == null) {
            Toast.makeText(this, "Alliance not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();
        
        // Initialize chat
        viewModel.initializeChat(allianceId);
        
        // Set alliance name
        if (allianceName != null) {
            textViewAllianceName.setText(allianceName + " Chat");
        }
    }
    
    private void initViews() {
        buttonBack = findViewById(R.id.buttonBack);
        textViewAllianceName = findViewById(R.id.textViewAllianceName);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        progressBar = findViewById(R.id.progressBar);
        textViewError = findViewById(R.id.textViewError);
        
        // Initially disable send button
        buttonSend.setEnabled(false);
    }
    
    private void setupRecyclerView() {
        messageAdapter = new AllianceMessageAdapter(null); // Will be updated when user ID is available
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);
        
        // Auto-scroll to bottom when new messages arrive
        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (positionStart == messageAdapter.getItemCount() - itemCount) {
                    recyclerViewMessages.scrollToPosition(positionStart);
                }
            }
        });
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AllianceChatViewModel.class);
        
        // Observe messages
        viewModel.getMessages().observe(this, messages -> {
            if (messages != null) {
                messageAdapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    recyclerViewMessages.scrollToPosition(messages.size() - 1);
                }
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            buttonSend.setEnabled(!isLoading && !editTextMessage.getText().toString().trim().isEmpty());
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                textViewError.setText(error);
                textViewError.setVisibility(View.VISIBLE);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                textViewError.setVisibility(View.GONE);
            }
        });
        
        // Observe message sent
        viewModel.getMessageSent().observe(this, sent -> {
            if (sent != null && sent) {
                editTextMessage.setText("");
                viewModel.resetMessageSent();
            }
        });
        
        // Observe current user ID for adapter
        viewModel.getCurrentUserId().observe(this, userId -> {
            if (userId != null) {
                messageAdapter.setCurrentUserId(userId);
            }
        });
    }
    
    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());
        
        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                viewModel.sendMessage(message);
            }
        });
        
        // Enable/disable send button based on text input
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = !s.toString().trim().isEmpty();
                boolean isLoading = viewModel.getIsLoading().getValue() != null && viewModel.getIsLoading().getValue();
                buttonSend.setEnabled(hasText && !isLoading);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Send message on Enter key
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                viewModel.sendMessage(message);
                return true;
            }
            return false;
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Real-time listening is handled automatically by the ViewModel
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Real-time listening continues in background
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop real-time listening when activity is destroyed
        if (viewModel != null) {
            viewModel.stopRealTimeListening();
        }
    }
}
