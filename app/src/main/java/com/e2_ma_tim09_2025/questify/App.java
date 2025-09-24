package com.e2_ma_tim09_2025.questify;

import android.app.Application;

import androidx.work.Configuration;
import dagger.hilt.android.HiltAndroidApp;
import androidx.hilt.work.HiltWorkerFactory;
import javax.inject.Inject;

@HiltAndroidApp
public class App extends Application implements Configuration.Provider {

    @Inject
    public HiltWorkerFactory workerFactory;

    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}