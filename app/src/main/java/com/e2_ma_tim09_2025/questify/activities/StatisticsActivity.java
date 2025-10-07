package com.e2_ma_tim09_2025.questify.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.dtos.TaskCategoryStatsDto;
import com.e2_ma_tim09_2025.questify.dtos.TaskStatusStatsDto;
import com.e2_ma_tim09_2025.questify.dtos.TaskStreakStatsDto;
import com.e2_ma_tim09_2025.questify.models.TaskDifficultyStatsDto;
import com.e2_ma_tim09_2025.questify.services.UserStatisticsService;
import com.e2_ma_tim09_2025.questify.viewmodels.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatisticsActivity extends AppCompatActivity {

    private StatisticsViewModel viewModel;
    
    // Text views for statistics
    private TextView activeDaysStreakText;
    private TextView longestTaskStreakText;
    
    // Charts
    private PieChart taskStatusChart;
    private BarChart categoryChart;
    private LineChart difficultyChart;
    private LineChart xpProgressChart;
    private PieChart missionComparisonChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DEBUG: StatisticsActivity.onCreate() called");
        setContentView(R.layout.activity_statistics);

        initializeViews();
        initializeViewModel();
        setupCharts();
        observeData();
        
        // Load statistics
        System.out.println("DEBUG: About to call viewModel.loadAllStatistics()");
        viewModel.loadAllStatistics();
    }

    private void initializeViews() {
        activeDaysStreakText = findViewById(R.id.activeDaysStreakText);
        longestTaskStreakText = findViewById(R.id.longestTaskStreakText);
        taskStatusChart = findViewById(R.id.taskStatusChart);
        categoryChart = findViewById(R.id.categoryChart);
        difficultyChart = findViewById(R.id.difficultyChart);
        xpProgressChart = findViewById(R.id.xpProgressChart);
        missionComparisonChart = findViewById(R.id.missionComparisonChart);
    }

    private void initializeViewModel() {
        System.out.println("DEBUG: Initializing StatisticsViewModel");
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        System.out.println("DEBUG: StatisticsViewModel initialized successfully");
    }

    private void setupCharts() {
        setupTaskStatusChart();
        setupCategoryChart();
        setupDifficultyChart();
        setupXpProgressChart();
        setupMissionComparisonChart();
    }

    private void setupTaskStatusChart() {
        taskStatusChart.setUsePercentValues(true);
        taskStatusChart.getDescription().setEnabled(false);
        taskStatusChart.setExtraOffsets(5, 10, 5, 5);
        taskStatusChart.setDragDecelerationFrictionCoef(0.95f);
        taskStatusChart.setDrawHoleEnabled(true);
        taskStatusChart.setHoleColor(Color.WHITE);
        taskStatusChart.setTransparentCircleColor(Color.WHITE);
        taskStatusChart.setTransparentCircleAlpha(110);
        taskStatusChart.setHoleRadius(58f);
        taskStatusChart.setTransparentCircleRadius(61f);
        taskStatusChart.setDrawCenterText(true);
        taskStatusChart.setRotationAngle(0);
        taskStatusChart.setRotationEnabled(true);
        taskStatusChart.setHighlightPerTapEnabled(true);
        taskStatusChart.setEntryLabelColor(Color.BLACK);
        taskStatusChart.setEntryLabelTextSize(12f);

        Legend l = taskStatusChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
    }

    private void setupCategoryChart() {
        categoryChart.getDescription().setEnabled(false);
        categoryChart.setMaxVisibleValueCount(60);
        categoryChart.setPinchZoom(false);
        categoryChart.setDrawBarShadow(false);
        categoryChart.setDrawGridBackground(false);

        XAxis xAxis = categoryChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        YAxis leftAxis = categoryChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);

        categoryChart.getAxisRight().setEnabled(false);
        categoryChart.getLegend().setEnabled(false);
    }

    private void setupDifficultyChart() {
        difficultyChart.getDescription().setEnabled(false);
        difficultyChart.setTouchEnabled(true);
        difficultyChart.setDragEnabled(true);
        difficultyChart.setScaleEnabled(true);
        difficultyChart.setPinchZoom(true);
        difficultyChart.setDrawGridBackground(false);

        XAxis xAxis = difficultyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return new SimpleDateFormat("MMM dd", Locale.getDefault())
                        .format(new Date((long) value));
            }
        });

        YAxis leftAxis = difficultyChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        difficultyChart.getAxisRight().setEnabled(false);
        difficultyChart.getLegend().setEnabled(false);
    }

    private void setupXpProgressChart() {
        xpProgressChart.getDescription().setEnabled(false);
        xpProgressChart.setTouchEnabled(true);
        xpProgressChart.setDragEnabled(true);
        xpProgressChart.setScaleEnabled(true);
        xpProgressChart.setPinchZoom(true);
        xpProgressChart.setDrawGridBackground(false);

        XAxis xAxis = xpProgressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return new SimpleDateFormat("MMM dd", Locale.getDefault())
                        .format(new Date((long) value));
            }
        });

        YAxis leftAxis = xpProgressChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        xpProgressChart.getAxisRight().setEnabled(false);
        xpProgressChart.getLegend().setEnabled(false);
    }

    private void observeData() {
        System.out.println("DEBUG: Setting up data observers");
        
        viewModel.getActiveDaysStreak().observe(this, streak -> {
            System.out.println("DEBUG: ActiveDaysStreak observer triggered, value: " + streak);
            if (streak != null) {
                activeDaysStreakText.setText(String.valueOf(streak));
            }
        });

        viewModel.getLongestTaskStreak().observe(this, streakDto -> {
            System.out.println("DEBUG: LongestTaskStreak observer triggered, value: " + (streakDto != null ? streakDto.getLongestStreak() : "null"));
            if (streakDto != null) {
                longestTaskStreakText.setText(String.valueOf(streakDto.getLongestStreak()));
            }
        });

        viewModel.getTaskStatusStats().observe(this, this::updateTaskStatusChart);
        viewModel.getCompletedTasksPerCategory().observe(this, this::updateCategoryChart);
        viewModel.getAverageDifficultyXP().observe(this, this::updateDifficultyChart);
        viewModel.getXpLast7Days().observe(this, this::updateXpProgressChart);
        viewModel.getMissionStats().observe(this, this::updateMissionStats);
        
        System.out.println("DEBUG: Data observers setup completed");
    }

    private void updateTaskStatusChart(TaskStatusStatsDto stats) {
        if (stats == null) return;

        List<PieEntry> entries = new ArrayList<>();
        if (stats.getCompleted() > 0) {
            entries.add(new PieEntry(stats.getCompleted(), "Finished"));
        }
        if (stats.getNotCompleted() > 0) {
            entries.add(new PieEntry(stats.getNotCompleted(), "Not finished"));
        }
        if (stats.getCanceled() > 0) {
            entries.add(new PieEntry(stats.getCanceled(), "Canceled"));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "No tasks"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4CAF50")); // Green for completed
        colors.add(Color.parseColor("#FF9800")); // Orange for not completed
        colors.add(Color.parseColor("#F44336")); // Red for canceled
        colors.add(Color.parseColor("#9E9E9E")); // Gray for no tasks
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        taskStatusChart.setData(data);
        taskStatusChart.invalidate();
    }

    private void updateCategoryChart(List<TaskCategoryStatsDto> stats) {
        if (stats == null || stats.isEmpty()) return;

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            TaskCategoryStatsDto stat = stats.get(i);
            if (stat.getCompletedTasks() > 0) {
                entries.add(new BarEntry(i, stat.getCompletedTasks()));
                labels.add(stat.getCategoryName());
            }
        }

        if (entries.isEmpty()) return;

        BarDataSet dataSet = new BarDataSet(entries, "Finished tasks by category");
        dataSet.setColor(Color.parseColor("#5C4033"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        categoryChart.setData(data);

        XAxis xAxis = categoryChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        categoryChart.invalidate();
    }

    private void updateDifficultyChart(List<TaskDifficultyStatsDto> stats) {
        if (stats == null || stats.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        for (TaskDifficultyStatsDto stat : stats) {
            // Convert back from integer * 100 to decimal
            float difficultyValue = stat.getXp() / 100.0f;
            entries.add(new Entry(stat.getDate(), difficultyValue));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Average Difficulty (1=Very Easy, 2=Easy, 3=Hard, 4=Extreme)");
        dataSet.setColor(Color.parseColor("#5C4033"));
        dataSet.setCircleColor(Color.parseColor("#5C4033"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(9f);
        dataSet.setFillColor(Color.parseColor("#5C4033"));
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(50);

        LineData data = new LineData(dataSet);
        difficultyChart.setData(data);
        
        // Configure Y-axis with difficulty labels
        YAxis leftAxis = difficultyChart.getAxisLeft();
        leftAxis.setAxisMinimum(0.5f);
        leftAxis.setAxisMaximum(4.5f);
        leftAxis.setGranularity(0.5f);
        leftAxis.setLabelCount(9, false);
        
        // Set custom labels for difficulty levels
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 1.0f) return "Very Easy - 1";
                if (value == 2.0f) return "Easy - 2";
                if (value == 3.0f) return "Hard - 3";
                if (value == 4.0f) return "Extreme - 4";
                return ""; // Hide intermediate values
            }
        });
        
        // Make labels smaller to fit
        leftAxis.setTextSize(8f);
        leftAxis.setTextColor(Color.parseColor("#666666"));
        
        // Hide right axis
        difficultyChart.getAxisRight().setEnabled(false);
        
        difficultyChart.invalidate();
    }

    private void updateXpProgressChart(List<TaskDifficultyStatsDto> stats) {
        if (stats == null || stats.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        for (TaskDifficultyStatsDto stat : stats) {
            entries.add(new Entry(stat.getDate(), stat.getXp()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "XP in the last 7 days");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawValues(true);

        LineData data = new LineData(dataSet);
        xpProgressChart.setData(data);
        xpProgressChart.invalidate();
    }
    
    private void updateMissionStats(UserStatisticsService.MissionStatsDto stats) {
        if (stats == null) return;
        
        System.out.println("DEBUG: Updating mission stats display - Started: " + stats.getStartedMissions() + 
            ", Finished: " + stats.getFinishedMissions() + ", Success Rate: " + stats.getSuccessRate() + "%");
        
        // Find the mission stats TextViews and update them
        TextView startedMissionsText = findViewById(R.id.startedMissionsText);
        TextView finishedMissionsText = findViewById(R.id.finishedMissionsText);
        TextView successRateText = findViewById(R.id.successRateText);
        
        if (startedMissionsText != null) {
            startedMissionsText.setText(String.valueOf(stats.getStartedMissions()));
        }
        if (finishedMissionsText != null) {
            finishedMissionsText.setText(String.valueOf(stats.getFinishedMissions()));
        }
        if (successRateText != null) {
            successRateText.setText(String.format("%.1f%%", stats.getSuccessRate()));
        }
        
        // Update mission comparison chart if it exists
        updateMissionComparisonChart(stats);
    }
    
    private void setupMissionComparisonChart() {
        missionComparisonChart.setUsePercentValues(true);
        missionComparisonChart.getDescription().setEnabled(false);
        missionComparisonChart.setExtraOffsets(5, 10, 5, 5);
        missionComparisonChart.setDragDecelerationFrictionCoef(0.95f);
        missionComparisonChart.setDrawHoleEnabled(true);
        missionComparisonChart.setHoleColor(Color.WHITE);
        missionComparisonChart.setTransparentCircleColor(Color.WHITE);
        missionComparisonChart.setTransparentCircleAlpha(110);
        missionComparisonChart.setHoleRadius(58f);
        missionComparisonChart.setTransparentCircleRadius(61f);
        missionComparisonChart.setDrawCenterText(true);
        missionComparisonChart.setRotationAngle(0);
        missionComparisonChart.setRotationEnabled(true);
        missionComparisonChart.setHighlightPerTapEnabled(true);
        missionComparisonChart.setCenterText("Mission\nSuccess");
        missionComparisonChart.setCenterTextSize(12f);
        missionComparisonChart.setCenterTextColor(Color.parseColor("#5C4033"));
        
        Legend l = missionComparisonChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTextSize(11f);
        l.setTextColor(Color.parseColor("#5C4033"));
    }
    
    private void updateMissionComparisonChart(UserStatisticsService.MissionStatsDto stats) {
        if (stats == null) return;
        
        List<PieEntry> entries = new ArrayList<>();
        
        if (stats.getFinishedMissions() > 0) {
            entries.add(new PieEntry(stats.getFinishedMissions(), "Successfully Finished"));
        }
        
        int unfinishedMissions = stats.getStartedMissions() - stats.getFinishedMissions();
        if (unfinishedMissions > 0) {
            entries.add(new PieEntry(unfinishedMissions, "Not Finished"));
        }
        
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "No Missions Yet"));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "Mission Status");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        
        // Colors
        ArrayList<Integer> colors = new ArrayList<>();
        if (stats.getFinishedMissions() > 0) {
            colors.add(Color.parseColor("#4CAF50")); // Green for finished
        }
        if (unfinishedMissions > 0) {
            colors.add(Color.parseColor("#FF9800")); // Orange for unfinished
        }
        if (entries.size() == 1 && entries.get(0).getLabel().equals("No Missions Yet")) {
            colors.add(Color.parseColor("#9E9E9E")); // Gray for no missions
        }
        
        dataSet.setColors(colors);
        
        PieData data = new PieData(dataSet);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(11f);
        
        missionComparisonChart.setData(data);
        missionComparisonChart.invalidate();
        
        System.out.println("DEBUG: Mission comparison chart updated - Started: " + stats.getStartedMissions() + 
            ", Finished: " + stats.getFinishedMissions() + ", Unfinished: " + unfinishedMissions);
    }
}
