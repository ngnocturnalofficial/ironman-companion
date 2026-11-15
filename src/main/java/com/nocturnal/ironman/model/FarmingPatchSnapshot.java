package com.nocturnal.ironman.model;

import java.time.Duration;
import java.time.Instant;

public final class FarmingPatchSnapshot
{
    private final String name;
    private final String crop;
    private final String state;
    private final int stage;
    private final int stages;
    private final Instant observedAt;
    private final Instant readyAt;

    public FarmingPatchSnapshot(String name, String crop, String state, int stage, int stages,
        Instant observedAt, Instant readyAt)
    {
        this.name = name == null ? "Unknown patch" : name;
        this.crop = crop == null ? "Unknown crop" : crop;
        this.state = state == null ? "Unknown" : state;
        this.stage = Math.max(0, stage);
        this.stages = Math.max(0, stages);
        this.observedAt = observedAt;
        this.readyAt = readyAt;
    }

    public String getName() { return name; }
    public String getCrop() { return crop; }
    public String getState() { return state; }
    public int getStage() { return stage; }
    public int getStages() { return stages; }
    public Instant getObservedAt() { return observedAt; }
    public Instant getReadyAt() { return readyAt; }

    public String getStatusText(Instant now)
    {
        String detail;
        if ("Growing".equals(state) && readyAt != null)
        {
            long remainingSeconds = Duration.between(now, readyAt).getSeconds();
            long minutes = Math.max(1, (remainingSeconds + 59) / 60);
            detail = remainingSeconds <= 0
                ? "possibly harvestable; verify in game"
                : "estimated ready in up to " + minutes + " min (may be ready sooner)";
        }
        else
        {
            detail = state.toLowerCase(java.util.Locale.ROOT);
        }

        if (observedAt != null)
        {
            long ageHours = Math.max(0, Duration.between(observedAt, now).toHours());
            if (ageHours >= 24) detail += " (last observed " + (ageHours / 24) + "d ago)";
            else if (ageHours >= 1) detail += " (last observed " + ageHours + "h ago)";
        }
        return detail;
    }
}
