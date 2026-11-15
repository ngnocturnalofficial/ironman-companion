package com.nocturnal.ironman.model;

public final class SlayerTaskSnapshot
{
    private final boolean active;
    private final String taskName;
    private final String location;
    private final int remaining;
    private final int initialAmount;
    private final int points;
    private final int streak;

    public SlayerTaskSnapshot(boolean active, String taskName, String location, int remaining,
        int initialAmount, int points, int streak)
    {
        this.active = active;
        this.taskName = taskName == null ? "Unknown" : taskName;
        this.location = location == null ? "Not specified" : location;
        this.remaining = Math.max(0, remaining);
        this.initialAmount = Math.max(0, initialAmount);
        this.points = Math.max(0, points);
        this.streak = Math.max(0, streak);
    }

    public static SlayerTaskSnapshot empty()
    {
        return new SlayerTaskSnapshot(false, "No active task", "", 0, 0, 0, 0);
    }

    public boolean isActive() { return active; }
    public String getTaskName() { return taskName; }
    public String getLocation() { return location; }
    public int getRemaining() { return remaining; }
    public int getInitialAmount() { return initialAmount; }
    public int getPoints() { return points; }
    public int getStreak() { return streak; }

    public int getCompleted()
    {
        return Math.max(0, initialAmount - remaining);
    }

    public int getProgressPercent()
    {
        return initialAmount <= 0 ? 0 : Math.min(100, Math.max(0, getCompleted() * 100 / initialAmount));
    }
}
