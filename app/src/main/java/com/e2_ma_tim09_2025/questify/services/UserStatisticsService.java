package com.e2_ma_tim09_2025.questify.services;

import com.e2_ma_tim09_2025.questify.dtos.TaskCategoryStatsDto;
import com.e2_ma_tim09_2025.questify.dtos.TaskStatusStatsDto;
import com.e2_ma_tim09_2025.questify.dtos.TaskStreakStatsDto;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.TaskDifficultyStatsDto;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;

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

        // Sakupi dane kada je korisnik bio aktivan
        Set<LocalDate> interactionDays = new HashSet<>();
        for (Task task : tasks) {
            if (task.getLastInteractionAt() > 0) {
                LocalDate date = Instant.ofEpochMilli(task.getLastInteractionAt())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                interactionDays.add(date);
            }
        }

        if (interactionDays.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Ako je poslednja interakcija bila pre juče → streak = 0
        LocalDate lastInteraction = interactionDays.stream().max(Comparator.naturalOrder()).get();
        if (lastInteraction.isBefore(yesterday)) {
            return 0;
        }

        // Inače brojimo streak od poslednje interakcije unazad
        int streak = 0;
        LocalDate current = lastInteraction;
        while (interactionDays.contains(current)) {
            streak++;
            current = current.minusDays(1);
        }

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

        // Filtriraj samo prošle i današnje zadatke
        LocalDate today = LocalDate.now();
        Map<LocalDate, List<Task>> tasksByDay = new HashMap<>();

        for (Task task : tasks) {
            LocalDate day = Instant.ofEpochMilli(task.getCompletedAt())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (!day.isAfter(today)) { // ignoriši buduće zadatke
                tasksByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(task);
            }
        }

        if (tasksByDay.isEmpty()) {
            return new TaskStreakStatsDto(0);
        }

        LocalDate minDay = Collections.min(tasksByDay.keySet());
        LocalDate maxDay = Collections.max(tasksByDay.keySet());

        int maxStreak = 0;
        int currentStreak = 0;

        // Iteriraj kroz sve dane u opsegu
        for (LocalDate day = minDay; !day.isAfter(maxDay); day = day.plusDays(1)) {
            List<Task> dayTasks = tasksByDay.get(day);

            if (dayTasks == null) {
                // Nema zadataka → streak se nastavlja
                currentStreak++;
            } else {
                boolean hasIncomplete = dayTasks.stream()
                        .anyMatch(t -> t.getStatus() == TaskStatus.NOT_COMPLETED);

                if (hasIncomplete) {
                    currentStreak = 0; // streak se prekida
                } else {
                    currentStreak++;
                }
            }

            maxStreak = Math.max(maxStreak, currentStreak);
        }

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
        Map<Long, List<Integer>> xpPerDay = new HashMap<>();

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                // Normalize date to 00:00 to group tasks by day
                long dayTimestamp = getDayStartTimestamp(task.getCompletedAt());

                xpPerDay.computeIfAbsent(dayTimestamp, k -> new ArrayList<>()).add(task.getXp());
            }
        }

        List<TaskDifficultyStatsDto> result = new ArrayList<>();
        for (Map.Entry<Long, List<Integer>> entry : xpPerDay.entrySet()) {
            long day = entry.getKey();
            List<Integer> xpList = entry.getValue();
            int avgXP = (int) xpList.stream().mapToInt(Integer::intValue).average().orElse(0);
            result.add(new TaskDifficultyStatsDto(day, avgXP));
        }

        // Sort by date ascending for line chart
        result.sort(Comparator.comparingLong(TaskDifficultyStatsDto::getDate));
        return result;
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

}
