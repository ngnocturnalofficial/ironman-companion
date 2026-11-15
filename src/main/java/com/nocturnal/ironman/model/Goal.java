package com.nocturnal.ironman.model;

import java.time.LocalDate;
import java.util.UUID;

public final class Goal
{
    private final String id;
    private final String title;
    private final String category;
    private final String detail;
    private final LocalDate targetDate;
    private boolean complete;

    public Goal(String title, String category, String detail, LocalDate targetDate)
    {
        this(UUID.randomUUID().toString(), title, category, detail, targetDate, false);
    }

    public Goal(String id, String title, String category, String detail, LocalDate targetDate, boolean complete)
    {
        this.id = id; this.title = title; this.category = category; this.detail = detail;
        this.targetDate = targetDate; this.complete = complete;
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDetail() { return detail; }
    public LocalDate getTargetDate() { return targetDate; }
    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }
}
