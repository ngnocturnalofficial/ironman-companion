package com.nocturnal.ironman.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ProgressSnapshot
{
    private final Map<String, String> quests;
    private final Map<String, String> diaries;

    public ProgressSnapshot(Map<String, String> quests, Map<String, String> diaries)
    {
        this.quests = immutable(quests);
        this.diaries = immutable(diaries);
    }

    private static Map<String, String> immutable(Map<String, String> source)
    {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source == null ? Collections.emptyMap() : source));
    }

    public static ProgressSnapshot empty()
    {
        return new ProgressSnapshot(Collections.emptyMap(), Collections.emptyMap());
    }

    public Map<String, String> getQuests() { return quests; }
    public Map<String, String> getDiaries() { return diaries; }

    public int getCompletedQuestCount()
    {
        int count = 0;
        for (Map.Entry<String, String> entry : quests.entrySet())
        {
            if (!isRecipeForDisasterSubquest(entry.getKey()) && "FINISHED".equals(entry.getValue())) count++;
        }
        return count;
    }

    public int getQuestCount()
    {
        int count = 0;
        for (String name : quests.keySet()) if (!isRecipeForDisasterSubquest(name)) count++;
        return count;
    }

    public int getInProgressQuestCount()
    {
        int count = 0;
        for (Map.Entry<String, String> entry : quests.entrySet())
        {
            if (!isRecipeForDisasterSubquest(entry.getKey()) && "IN_PROGRESS".equals(entry.getValue())) count++;
        }
        return count;
    }

    private boolean isRecipeForDisasterSubquest(String name)
    {
        return name != null && name.startsWith("Recipe for Disaster -");
    }
}
