package com.e2_ma_tim09_2025.questify.services;

import com.e2_ma_tim09_2025.questify.dtos.TaskCategoryStatsDto;
import com.e2_ma_tim09_2025.questify.dtos.TaskStatusStatsDto;
import com.e2_ma_tim09_2025.questify.dtos.TaskStreakStatsDto;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.TaskDifficultyStatsDto;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

public class UserStatisticsService {
    private final TaskService taskService;
    private final UserService  userService;

    @Inject
    public UserStatisticsService(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }
    public int calculateActiveDaysStreak(String userId) {
        List<Task> tasks = taskService.getTasksByUser(userId);
        
        // Debug logging
        System.out.println("DEBUG: calculateActiveDaysStreak - UserId: " + userId);
        System.out.println("DEBUG: Total tasks found: " + tasks.size());

        // Collect all days when user had any interaction with tasks
        Set<LocalDate> interactionDays = new HashSet<>();
        for (Task task : tasks) {
            // Add task creation date
            if (task.getCreatedAt() > 0) {
                LocalDate createdDate = Instant.ofEpochMilli(task.getCreatedAt())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                interactionDays.add(createdDate);
                System.out.println("DEBUG: Task created on: " + createdDate + " (Task: " + task.getName() + ")");
            }
            
            // Add last interaction date (pause, unpause, edit, etc.)
            if (task.getLastInteractionAt() > 0) {
                LocalDate lastInteractionDate = Instant.ofEpochMilli(task.getLastInteractionAt())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                interactionDays.add(lastInteractionDate);
                System.out.println("DEBUG: Last interaction on: " + lastInteractionDate + " (Task: " + task.getName() + ")");
            }
            
            // Add completion date
            if (task.getCompletedAt() > 0) {
                LocalDate completedDate = Instant.ofEpochMilli(task.getCompletedAt())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                interactionDays.add(completedDate);
                System.out.println("DEBUG: Task completed on: " + completedDate + " (Task: " + task.getName() + ")");
            }
        }

        System.out.println("DEBUG: Total unique interaction days: " + interactionDays.size());
        System.out.println("DEBUG: Interaction days: " + interactionDays);

        if (interactionDays.isEmpty()) {
            System.out.println("DEBUG: No interaction days found, returning 0");
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        System.out.println("DEBUG: Today: " + today + ", Yesterday: " + yesterday);

        // If last interaction was before yesterday → streak = 0
        LocalDate lastInteraction = interactionDays.stream().max(Comparator.naturalOrder()).get();
        System.out.println("DEBUG: Last interaction date: " + lastInteraction);
        
        if (lastInteraction.isBefore(yesterday)) {
            System.out.println("DEBUG: Last interaction was before yesterday, returning 0");
            return 0;
        }

        // Count consecutive days from last interaction backwards
        int streak = 0;
        LocalDate current = lastInteraction;
        while (interactionDays.contains(current)) {
            streak++;
            System.out.println("DEBUG: Found interaction on " + current + ", streak: " + streak);
            current = current.minusDays(1);
        }

        System.out.println("DEBUG: Final streak: " + streak);
        return streak;
    }

    public TaskStatusStatsDto calculateTaskStatusStats(String userId) {
        List<Task> tasks = taskService.getTasksByUser(userId); // add sync method if needed
        int totalCreated = tasks.size();
        int completed = 0;
        int notCompleted = 0;
        int canceled = 0;

        for (Task task : tasks) {
            switch (task.getStatus()) {
                case COMPLETED: completed++; break;
                case NOT_COMPLETED: notCompleted++; break;
                case CANCELLED: canceled++; break;
                case ACTIVE: notCompleted++; break; // treat active as not completed
            }
        }

        return new TaskStatusStatsDto(totalCreated, completed, notCompleted, canceled);
    }

    public TaskStreakStatsDto calculateLongestTaskStreak(String userId) {
        List<Task> tasks = taskService.getTasksByUser(userId);
        
        System.out.println("DEBUG: calculateLongestTaskStreak - UserId: " + userId);
        System.out.println("DEBUG: Total tasks found: " + tasks.size());

        // Get all tasks with due dates (past and today)
        List<Task> tasksWithDueDates = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Task task : tasks) {
            // Only consider tasks that have a due date
            if (task.getFinishDate() > 0) {
                LocalDate dueDate = Instant.ofEpochMilli(task.getFinishDate())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                // Only consider past and today's due dates
                if (!dueDate.isAfter(today)) {
                    tasksWithDueDates.add(task);
                    System.out.println("DEBUG: Task '" + task.getName() + "' due on " + dueDate + ", status: " + task.getStatus());
                }
            }
        }

        if (tasksWithDueDates.isEmpty()) {
            System.out.println("DEBUG: No tasks with due dates found, returning 0");
            return new TaskStreakStatsDto(0);
        }

        // Get the date range from earliest due date to today
        LocalDate minDueDate = tasksWithDueDates.stream()
                .map(task -> Instant.ofEpochMilli(task.getFinishDate())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .min(Comparator.naturalOrder())
                .get();
        
        System.out.println("DEBUG: Date range: " + minDueDate + " to " + today);

        int maxStreak = 0;
        int currentStreak = 0;

        // Check each day from earliest due date to today
        for (LocalDate day = minDueDate; !day.isAfter(today); day = day.plusDays(1)) {
            final LocalDate currentDay = day; // Make effectively final for lambda
            // Find tasks that were due on or before this day
            List<Task> tasksDueByThisDay = tasksWithDueDates.stream()
                    .filter(task -> {
                        LocalDate dueDate = Instant.ofEpochMilli(task.getFinishDate())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        return !dueDate.isAfter(currentDay);
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            if (tasksDueByThisDay.isEmpty()) {
                // No tasks were due by this day - streak continues
                System.out.println("DEBUG: Day " + day + " - No tasks due by this day, streak continues: " + currentStreak);
            } else {
                // Check if any task due by this day was NOT_COMPLETED
                boolean hasNotCompleted = tasksDueByThisDay.stream()
                        .anyMatch(task -> task.getStatus() == TaskStatus.NOT_COMPLETED);
                
                if (hasNotCompleted) {
                    // At least one task was NOT_COMPLETED - streak breaks
                    System.out.println("DEBUG: Day " + day + " - Some tasks not completed, streak breaks at: " + currentStreak);
                    maxStreak = Math.max(maxStreak, currentStreak);
                    currentStreak = 0;
                } else {
                    // All tasks due by this day were completed - streak continues
                    currentStreak++;
                    System.out.println("DEBUG: Day " + day + " - All tasks due by this day completed, streak continues: " + currentStreak);
                }
            }
        }

        // Check final streak
        maxStreak = Math.max(maxStreak, currentStreak);
        System.out.println("DEBUG: Final longest streak: " + maxStreak);
        
        return new TaskStreakStatsDto(maxStreak);
    }

    public List<TaskCategoryStatsDto> getCompletedTasksPerCategory(String userId) {
        List<Task> tasks = taskService.getTasksByUser(userId); // get all tasks of user
        List<TaskCategory> categories = taskService.getAllCategoriesSync(); // get all categories

        Map<Integer, Integer> completedCountMap = new HashMap<>();

        // Count completed tasks per category
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                completedCountMap.put(task.getCategoryId(),
                        completedCountMap.getOrDefault(task.getCategoryId(), 0) + 1);
            }
        }

        List<TaskCategoryStatsDto> stats = new ArrayList<>();
        for (TaskCategory category : categories) {
            int count = completedCountMap.getOrDefault(category.getId(), 0);
            stats.add(new TaskCategoryStatsDto(category.getName(), count));
        }

        return stats;
    }
    public List<TaskDifficultyStatsDto> getAverageDifficultyXPPerDay(String userId) {
        List<Task> tasks = taskService.getTasksByUser(userId); // fetch all user tasks
        
        // Calculate overall average difficulty of all completed tasks
        List<Integer> allDifficulties = new ArrayList<>();
        
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                // Convert difficulty to numeric value
                int difficultyValue = getDifficultyValue(task.getDifficulty());
                allDifficulties.add(difficultyValue);
            }
        }
        
        if (allDifficulties.isEmpty()) {
            return new ArrayList<>(); // No completed tasks
        }
        
        // Calculate overall average difficulty
        double overallAverage = allDifficulties.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        
        System.out.println("DEBUG: All difficulties: " + allDifficulties);
        System.out.println("DEBUG: Overall average difficulty: " + overallAverage);
        System.out.println("DEBUG: Stored as integer * 100: " + (int) (overallAverage * 100));
        
        // Create a single data point for the chart (use today's date)
        List<TaskDifficultyStatsDto> result = new ArrayList<>();
        long todayTimestamp = getDayStartTimestamp(System.currentTimeMillis());
        result.add(new TaskDifficultyStatsDto(todayTimestamp, (int) (overallAverage * 100))); // Store as integer * 100 to preserve decimals
        
        return result;
    }
    
    private int getDifficultyValue(TaskDifficulty difficulty) {
        switch (difficulty) {
            case VERY_EASY: return 1;
            case EASY: return 2;
            case HARD: return 3;
            case EXTREME: return 4;
            default: return 0;
        }
    }
    public List<TaskDifficultyStatsDto> getXPLast7Days(String userId) {
        List<Task> tasks = taskService.getTasksByUser(userId);

        Map<Long, Integer> xpPerDay = new HashMap<>();
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - TimeUnit.DAYS.toMillis(6); // include today + 6 previous days

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED && task.getCompletedAt() >= sevenDaysAgo) {
                // Normalize date to 00:00
                long dayTimestamp = getDayStartTimestamp(task.getCompletedAt());
                xpPerDay.put(dayTimestamp, xpPerDay.getOrDefault(dayTimestamp, 0) + task.getXp());
            }
        }

        List<TaskDifficultyStatsDto> result = new ArrayList<>();
        // Ensure all last 7 days are included even if no XP
        for (int i = 0; i < 7; i++) {
            long day = getDayStartTimestamp(now - TimeUnit.DAYS.toMillis(i));
            //long day = getDayStartTimestamp(now - TimeUnit.DAYS.toMillis(i));
            int xp = xpPerDay.getOrDefault(day, 0);
            result.add(new TaskDifficultyStatsDto(day, xp));
        }

        // Sort by date ascending
        result.sort(Comparator.comparingLong(TaskDifficultyStatsDto::getDate));
        return result;
    }

    private long getDayStartTimestamp(long timestamp) {
        // Normalize timestamp to 00:00 of that day
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    /**
     * Get mission statistics for a user
     */
    public void getMissionStatistics(String userId, OnCompleteListener<MissionStatsDto> listener) {
        userService.getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    MissionStatsDto stats = new MissionStatsDto();
                    stats.setStartedMissions(user.getStartedMissions());
                    stats.setFinishedMissions(user.getFinishedMissions());
                    stats.setSuccessRate(calculateSuccessRate(user.getStartedMissions(), user.getFinishedMissions()));
                    
                    System.out.println("DEBUG: Mission stats for user " + userId + 
                        " - Started: " + stats.getStartedMissions() + 
                        ", Finished: " + stats.getFinishedMissions() + 
                        ", Success Rate: " + stats.getSuccessRate() + "%");
                    
                    listener.onComplete(Tasks.forResult(stats));
                } else {
                    listener.onComplete(Tasks.forException(new Exception("User not found")));
                }
            } else {
                listener.onComplete(Tasks.forException(new Exception("Failed to get user")));
            }
        });
    }
    
    private double calculateSuccessRate(int startedMissions, int finishedMissions) {
        if (startedMissions == 0) return 0.0;
        return Math.round((double) finishedMissions / startedMissions * 100.0 * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    /**
     * Data Transfer Object for mission statistics
     */
    public static class MissionStatsDto {
        private int startedMissions;
        private int finishedMissions;
        private double successRate;
        
        public int getStartedMissions() { return startedMissions; }
        public void setStartedMissions(int startedMissions) { this.startedMissions = startedMissions; }
        
        public int getFinishedMissions() { return finishedMissions; }
        public void setFinishedMissions(int finishedMissions) { this.finishedMissions = finishedMissions; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
    }

}

