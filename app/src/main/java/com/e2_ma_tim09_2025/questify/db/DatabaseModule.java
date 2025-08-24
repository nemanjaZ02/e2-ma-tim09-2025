package com.e2_ma_tim09_2025.questify.db;

import android.app.Application;

import androidx.room.Room;

import com.e2_ma_tim09_2025.questify.dao.TaskCategoryDao;
import com.e2_ma_tim09_2025.questify.dao.TaskDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(Application app) {
        return Room.databaseBuilder(app, AppDatabase.class, "questify-db").build();
    }

    @Provides
    public TaskDao provideTaskDao(AppDatabase db) {
        return db.taskDao();
    }

    @Provides
    public TaskCategoryDao provideTaskCategoryDao(AppDatabase db) {
        return db.taskCategoryDao();
    }
}
