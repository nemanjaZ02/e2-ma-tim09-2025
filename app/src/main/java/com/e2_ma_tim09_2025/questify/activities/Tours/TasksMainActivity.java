package com.e2_ma_tim09_2025.questify.activities.Tours;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.TasksAdapter;
import com.e2_ma_tim09_2025.questify.db.AppDatabase;
import com.e2_ma_tim09_2025.questify.repositories.TaskCategoryRepository;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;
import com.e2_ma_tim09_2025.questify.services.TaskService;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.google.android.material.button.MaterialButton;

public class TasksMainActivity extends AppCompatActivity {

    private static final String TAG = "TasksMain";
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerViewTasks;
    private TasksAdapter taskAdapter;
    private MaterialButton addTaskButton;

    //ZA DODAVANJE KATEGORIJA
    //private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_main);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TasksAdapter();
        recyclerViewTasks.setAdapter(taskAdapter);

        addTaskButton = findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(TasksMainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        AppDatabase db = AppDatabase.getDatabase(this);

        // ZA DODAVANJE KATEGORIJA
        /*databaseExecutor.execute(() -> {
            TaskCategory category1 = new TaskCategory("Work", "a", 0);
            TaskCategory category2 = new TaskCategory("School", "b", 0);
            TaskCategory category3 = new TaskCategory("Personal", "c", 0);

            db.taskCategoryDao().insert(category1);
            db.taskCategoryDao().insert(category2);
            db.taskCategoryDao().insert(category3);

            Log.d(TAG, "Added three default categories in a background thread.");
        });*/

        TaskRepository taskRepository = new TaskRepository(db.taskDao());
        TaskCategoryRepository categoryRepository = new TaskCategoryRepository(db.taskCategoryDao());
        TaskService taskService = new TaskService(taskRepository, categoryRepository);

        taskViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new TaskViewModel(taskService);
            }
        }).get(TaskViewModel.class);

        taskViewModel.getTasks().observe(this, tasks -> {
            Log.d(TAG, "Task list updated! Total tasks: " + tasks.size());
            taskAdapter.setTasks(tasks);
        });
    }
}