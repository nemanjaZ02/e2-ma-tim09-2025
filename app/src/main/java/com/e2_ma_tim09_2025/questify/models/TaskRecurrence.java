package com.e2_ma_tim09_2025.questify.models;

import com.e2_ma_tim09_2025.questify.models.enums.RecurrenceUnit;

public class TaskRecurrence {
    private boolean isRecurring;
    private int interval;
    private RecurrenceUnit unit;
    private long startDate;
    private long endDate;

    public TaskRecurrence(boolean isRecurring, long endDate, long startDate, RecurrenceUnit unit, int interval) {
        this.isRecurring = isRecurring;
        this.endDate = endDate;
        this.startDate = startDate;
        this.unit = unit;
        this.interval = interval;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public RecurrenceUnit getUnit() {
        return unit;
    }

    public void setUnit(RecurrenceUnit unit) {
        this.unit = unit;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }
}
