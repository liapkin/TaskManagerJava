package com.medialab.tasksystem.model;

public enum ReminderType {
    ONE_DAY_BEFORE,
    ONE_WEEK_BEFORE,
    ONE_MONTH_BEFORE,
    CUSTOM_DATE;

    @Override
    public String toString() {
        switch (this) {
            case ONE_DAY_BEFORE: return "1 day before deadline";
            case ONE_WEEK_BEFORE: return "1 week before deadline";
            case ONE_MONTH_BEFORE: return "1 month before deadline";
            case CUSTOM_DATE: return "Custom date";
            default: return name();
        }
    }
}