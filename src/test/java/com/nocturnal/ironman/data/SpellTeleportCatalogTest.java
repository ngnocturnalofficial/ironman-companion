package com.nocturnal.ironman.data;

import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.ProgressSnapshot;
import com.nocturnal.ironman.model.SlayerTaskSnapshot;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class SpellTeleportCatalogTest
{
    private AccountSnapshot account(String spellbook, int magic, Map<String, String> quests)
    {
        Map<String, Integer> levels = new HashMap<>();
        levels.put("Magic", magic);
        return new AccountSnapshot("test", "Ironman", spellbook, levels, Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), 3, 10,
            "test", null, false, new ProgressSnapshot(quests, new LinkedHashMap<>()), SlayerTaskSnapshot.empty());
    }

    @Test
    public void standardSpellbookUsesMagicAndQuestRequirements()
    {
        AccountSnapshot beforeQuest = account("Standard", 99, Collections.emptyMap());
        assertFalse(SpellTeleportCatalog.available(beforeQuest).stream().anyMatch(t -> t.name.equals("Ardougne Teleport")));
        Map<String, String> quests = new HashMap<>();
        quests.put("Plague City", "FINISHED");
        AccountSnapshot afterQuest = account("Standard", 99, quests);
        assertTrue(SpellTeleportCatalog.available(afterQuest).stream().anyMatch(t -> t.name.equals("Ardougne Teleport")));
    }

    @Test
    public void kourendCastleTeleportRequiresQuestCompletion()
    {
        AccountSnapshot beforeQuest = account("Standard", 99, Collections.emptyMap());
        assertFalse(SpellTeleportCatalog.available(beforeQuest).stream()
            .anyMatch(t -> t.name.equals("Kourend Castle Teleport")));
        AccountSnapshot afterQuest = account("Standard", 99,
            Collections.singletonMap("Client of Kourend", "FINISHED"));
        assertTrue(SpellTeleportCatalog.available(afterQuest).stream()
            .anyMatch(t -> t.name.equals("Kourend Castle Teleport")));
    }

    @Test
    public void diaryChangesWatchtowerLandingToYanille()
    {
        Map<String, String> quests = Collections.singletonMap("Watchtower", "FINISHED");
        AccountSnapshot withoutDiary = account("Standard", 99, quests);
        assertTrue(SpellTeleportCatalog.available(withoutDiary).stream()
            .anyMatch(t -> t.name.equals("Watchtower Teleport")));
        assertFalse(SpellTeleportCatalog.available(withoutDiary).stream()
            .anyMatch(t -> t.name.equals("Watchtower Teleport (Yanille landing)")));

        Map<String, String> diaries = Collections.singletonMap("Ardougne", "Hard: Complete");
        AccountSnapshot withDiary = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.singletonMap("Magic", 99), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), 3, 10, "test", null, false,
            new ProgressSnapshot(quests, diaries), SlayerTaskSnapshot.empty());
        assertTrue(SpellTeleportCatalog.available(withDiary).stream()
            .anyMatch(t -> t.name.equals("Watchtower Teleport")));
        assertTrue(SpellTeleportCatalog.available(withDiary).stream()
            .anyMatch(t -> t.name.equals("Watchtower Teleport (Yanille landing)")));
    }

    @Test
    public void civitasAndApeAtollTeleportsUseTheirUnlockQuests()
    {
        Map<String, String> quests = new HashMap<>();
        quests.put("Twilight's Promise", "FINISHED");
        quests.put("Recipe for Disaster - King Awowogei", "FINISHED");
        AccountSnapshot account = account("Standard", 99, quests);
        assertTrue(SpellTeleportCatalog.available(account).stream()
            .anyMatch(t -> t.name.equals("Civitas illa Fortis Teleport")));
        assertTrue(SpellTeleportCatalog.available(account).stream()
            .anyMatch(t -> t.name.equals("Ape Atoll Teleport")));
    }

    @Test
    public void arceuusApeAtollTeleportRequiresMonkeyMadness()
    {
        AccountSnapshot beforeQuest = account("Arceuus", 99, Collections.emptyMap());
        assertFalse(SpellTeleportCatalog.available(beforeQuest).stream()
            .anyMatch(t -> t.name.equals("Ape Atoll Teleport")));
        AccountSnapshot afterQuest = account("Arceuus", 99,
            Collections.singletonMap("Monkey Madness I", "FINISHED"));
        assertTrue(SpellTeleportCatalog.available(afterQuest).stream()
            .anyMatch(t -> t.name.equals("Ape Atoll Teleport")));
    }

    @Test
    public void diaryRewardsAddSelectableDestinationsWithoutRemovingTheOriginal()
    {
        AccountSnapshot base = account("Standard", 99, Collections.emptyMap());
        assertTrue(SpellTeleportCatalog.available(base).stream().anyMatch(t -> t.name.equals("Varrock Teleport")));
        assertTrue(SpellTeleportCatalog.available(base).stream().anyMatch(t -> t.name.equals("Camelot Teleport")));

        Map<String, String> diaries = new HashMap<>();
        diaries.put("Varrock", "Medium: Complete");
        diaries.put("Kandarin", "Hard: Complete");
        AccountSnapshot upgraded = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.singletonMap("Magic", 99), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), 3, 10, "test", null, false,
            new ProgressSnapshot(Collections.emptyMap(), diaries), SlayerTaskSnapshot.empty());
        assertTrue(SpellTeleportCatalog.available(upgraded).stream().anyMatch(t -> t.name.equals("Varrock Teleport")));
        assertTrue(SpellTeleportCatalog.available(upgraded).stream().anyMatch(t -> t.name.equals("Varrock Teleport (Grand Exchange landing)")));
        assertTrue(SpellTeleportCatalog.available(upgraded).stream().anyMatch(t -> t.name.equals("Camelot Teleport")));
        assertTrue(SpellTeleportCatalog.available(upgraded).stream().anyMatch(t -> t.name.equals("Camelot Teleport (Seers' bank landing)")));
    }

    @Test
    public void onlyActiveSpellbookTeleportsAreListed()
    {
        AccountSnapshot lunar = account("Lunar", 99, Collections.singletonMap("Lunar Diplomacy", "FINISHED"));
        assertTrue(SpellTeleportCatalog.available(lunar).stream().anyMatch(t -> t.name.equals("Catherby Teleport")));
        assertFalse(SpellTeleportCatalog.available(lunar).stream().anyMatch(t -> t.name.equals("Varrock Teleport")));
    }
}
