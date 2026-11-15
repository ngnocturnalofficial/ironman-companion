package com.nocturnal.ironman.model;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProgressSnapshotTest
{
    @Test
    public void recipeForDisasterSubquestsDoNotInflateMainQuestCount()
    {
        Map<String, String> quests = new LinkedHashMap<>();
        quests.put("Cook's Assistant", "FINISHED");
        quests.put("Recipe for Disaster", "IN_PROGRESS");
        quests.put("Recipe for Disaster - Pirate Pete", "FINISHED");
        ProgressSnapshot snapshot = new ProgressSnapshot(quests, new LinkedHashMap<>());
        assertEquals(2, snapshot.getQuestCount());
        assertEquals(1, snapshot.getCompletedQuestCount());
        assertEquals(1, snapshot.getInProgressQuestCount());
    }

    @Test
    public void mapsAreDefensivelyCopied()
    {
        Map<String, String> quests = new LinkedHashMap<>();
        quests.put("Cook's Assistant", "FINISHED");
        ProgressSnapshot snapshot = new ProgressSnapshot(quests, new LinkedHashMap<>());
        quests.clear();
        assertEquals("FINISHED", snapshot.getQuests().get("Cook's Assistant"));
    }

    @Test
    public void slayerProgressIsBounded()
    {
        SlayerTaskSnapshot task = new SlayerTaskSnapshot(true, "Abby demons", "Catacombs", 30, 100, 250, 12);
        assertEquals(70, task.getCompleted());
        assertEquals(70, task.getProgressPercent());
        SlayerTaskSnapshot inconsistent = new SlayerTaskSnapshot(true, "Task", "Area", 150, 100, 0, 0);
        assertEquals(0, inconsistent.getCompleted());
        assertEquals(0, inconsistent.getProgressPercent());
    }
}
