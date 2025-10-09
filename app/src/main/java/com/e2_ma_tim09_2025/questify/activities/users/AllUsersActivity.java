package com.e2_ma_tim09_2025.questify.activities.users;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView; // instead of android.widget.SearchView
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.users.UsersAdapter;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.viewmodels.AllUsersViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint

public class AllUsersActivity extends AppCompatActivity {
    private AllUsersViewModel viewModel;
    private UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        RecyclerView recyclerView = findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersAdapter(new ArrayList<>(), new UsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(AllUsersActivity.this, OtherProfileActivity.class);
                intent.putExtra("userId", user.getId()); // send user id
                startActivity(intent);
            }

            @Override
            public void onAddFriendClick(User user) {
                viewModel.addFriend(user.getId());
                // Toast message will be handled by the ViewModel callback
            }
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AllUsersViewModel.class);

        viewModel.getUsers().observe(this, users -> {
            adapter.setUsers(users); // update list in adapter
        });
        // Observe the all users list
        viewModel.getUsers().observe(this, users -> {
            adapter.setUsers(users);
        });

        // Observe friend added LiveData to remove user instantly
        viewModel.getFriendAddedLiveData().observe(this, friendId -> {
            adapter.removeUserById(friendId);
        });
        
        // Observe friend add messages to show Toast
        viewModel.getFriendAddMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.fetchUsers();
        SearchView searchView = findViewById(R.id.searchUsers);
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);
        searchView.requestFocusFromTouch();


// Get internal EditText safely
        int searchEditTextId = androidx.appcompat.R.id.search_src_text;
        android.widget.EditText searchEditText = searchView.findViewById(searchEditTextId);

        if (searchEditText != null) {
            searchEditText.setTextColor(Color.BLACK);
            searchEditText.setHintTextColor(Color.GRAY);
        }
        searchView.setOnCloseListener(() -> {
            searchView.setQuery("", false); // clear text
            adapter.filter(""); // reset adapter to show all users
            return false; // return true to consume event, false to allow default behavior
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query); // implement filtering in adapter
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText); // live filter as user types
                return true;
            }
        });
    }
}
