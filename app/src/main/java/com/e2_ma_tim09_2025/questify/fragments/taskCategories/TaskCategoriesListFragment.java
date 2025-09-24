package com.e2_ma_tim09_2025.questify.fragments.taskCategories;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.taskCategories.TaskCategoryDetailsActivity;
import com.e2_ma_tim09_2025.questify.adapters.taskCategories.TaskCategoriesRecyclerViewAdapter;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskCategoryViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TaskCategoriesListFragment extends Fragment {

    private TaskCategoryViewModel categoryViewModel;
    private TaskCategoriesRecyclerViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_categories_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTaskCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryViewModel = new ViewModelProvider(requireActivity()).get(TaskCategoryViewModel.class);

        adapter = new TaskCategoriesRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(category -> {
            Intent intent = new Intent(getContext(), TaskCategoryDetailsActivity.class);
            intent.putExtra("categoryId", category.getId());
            startActivity(intent);
        });

        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> adapter.setCategories(categories));
    }
}
