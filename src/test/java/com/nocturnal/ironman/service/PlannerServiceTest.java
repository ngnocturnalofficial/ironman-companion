package com.nocturnal.ironman.service;

import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.ProgressSnapshot;
import com.nocturnal.ironman.model.SlayerTaskSnapshot;
import com.nocturnal.ironman.navigation.ScenePathfinder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlannerServiceTest
{
    private PlannerService planner()
    {
        return new PlannerService(new ScenePathfinder());
    }

    @Test
    public void quetzalWhistleRoutesExcludeBlueprintsAndUnbuiltLandings()
    {
        Map<Integer, Integer> inventory = new HashMap<>();
        inventory.put(29271, 1);
        inventory.put(29251, 1);
        Map<Integer, String> itemNames = new HashMap<>();
        itemNames.put(29271, "Basic quetzal whistle");
        itemNames.put(29251, "Basic quetzal whistle blueprint");
        Map<String, Integer> travelSettings = new HashMap<>();
        travelSettings.put("visitedVarlamore", 1);
        AccountSnapshot visited = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "unknown", null, false, ProgressSnapshot.empty(),
            SlayerTaskSnapshot.empty(), null, itemNames, Collections.emptyList(), travelSettings);

        List<String> options = planner().teleports(visited, false);
        assertTrue(options.stream().anyMatch(line -> line.contains("Quetzal whistle → Hunter Guild × 1")));
        assertTrue(options.stream().anyMatch(line -> line.contains("Quetzal whistle — Sunset Coast → Sunset Coast × 1")));
        assertFalse(options.stream().anyMatch(line -> line.contains("Quetzal whistle — Kastori →")));

        travelSettings.put("quetzalKastori", 1);
        AccountSnapshot built = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "unknown", null, false, ProgressSnapshot.empty(),
            SlayerTaskSnapshot.empty(), null, itemNames, Collections.emptyList(), travelSettings);
        assertTrue(planner().teleports(built, false).stream()
            .anyMatch(line -> line.contains("Quetzal whistle — Kastori → Kastori × 1")));
    }

    private AccountSnapshot account(SlayerTaskSnapshot task)
    {
        Map<String, Integer> levels = new HashMap<>();
        levels.put("Slayer", 90);
        return new AccountSnapshot("test", "Ironman", "Standard", levels,
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 100, 1500, "3200, 3200", null, false,
            ProgressSnapshot.empty(), task);
    }

    @Test
    public void routeKeepsBankedTeleportsWhenSameTypeIsAlsoCarried()
    {
        Map<Integer, Integer> inventory = new HashMap<>();
        inventory.put(8007, 1);
        Map<Integer, Integer> bank = new HashMap<>();
        bank.put(8007, 4);
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            bank, 3, 10, "3200, 3200", null, false, ProgressSnapshot.empty(),
            SlayerTaskSnapshot.empty());
        List<String> lines = planner().routeSuggestions(account, "Varrock");
        assertTrue(lines.stream().anyMatch(line -> line.contains("Varrock teleport") && line.contains("4 banked")));
    }

    @Test
    public void bossReadinessDetectsNamedGearInCarriedAndBankedItems()
    {
        Map<Integer, Integer> inventory = new HashMap<>();
        inventory.put(1540, 1);
        Map<Integer, Integer> bank = new HashMap<>();
        bank.put(10588, 1);
        Map<Integer, String> names = new HashMap<>();
        names.put(1540, "Anti-dragon shield");
        names.put(10588, "Salve amulet (ei)");
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            bank, 100, 1500, "3200, 3200", null, false, ProgressSnapshot.empty(),
            SlayerTaskSnapshot.empty(), null, names);
        List<String> lines = planner().bossReadiness(account, "Vorkath");
        assertTrue(lines.stream().anyMatch(line -> line.contains("Anti-dragon shield (carried/equipped)")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Salve amulet (ei) (banked)")));
    }

    @Test
    public void maxCapeOnlyUnlocksSkillcapeTeleportsForNinetyNineSkills()
    {
        Map<Integer, Integer> inventory = new HashMap<>();
        inventory.put(13280, 1);
        Map<Integer, String> names = new HashMap<>();
        names.put(13280, "Max cape");
        Map<String, Integer> levels = new HashMap<>();
        levels.put("Farming", 98);
        levels.put("Hunter", 99);
        levels.put("Construction", 99);
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            levels, Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3200", null, false,
            ProgressSnapshot.empty(), SlayerTaskSnapshot.empty(), null, names);
        List<String> lines = planner().teleports(account);
        assertFalse(lines.stream().anyMatch(line -> line.contains("Farming cape →")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Hunter cape → Hunter Guild")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Construction cape — Rimmington →")));
    }

    @Test
    public void watchtowerTabletUsesDiaryDependentLanding()
    {
        Map<Integer, Integer> inventory = new HashMap<>();
        inventory.put(8012, 1);
        Map<Integer, String> names = new HashMap<>();
        names.put(8012, "Watchtower teleport");
        Map<String, String> quests = Collections.singletonMap("Watchtower", "FINISHED");
        AccountSnapshot base = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "test", null, false,
            new ProgressSnapshot(quests, Collections.emptyMap()), SlayerTaskSnapshot.empty(), null, names);
        List<String> baseLines = planner().teleports(base);
        assertTrue(baseLines.stream().anyMatch(line -> line.contains("Watchtower teleport → Watchtower")));
        assertFalse(baseLines.stream().anyMatch(line -> line.contains("Watchtower teleport — Yanille landing → Yanille")));

        AccountSnapshot diary = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "test", null, false,
            new ProgressSnapshot(quests, Collections.singletonMap("Ardougne", "Hard: Complete")),
            SlayerTaskSnapshot.empty(), null, names);
        List<String> diaryLines = planner().teleports(diary);
        assertTrue(diaryLines.stream().anyMatch(line -> line.contains("Watchtower teleport → Watchtower")));
        assertTrue(diaryLines.stream().anyMatch(line -> line.contains("Watchtower teleport — Yanille landing → Yanille")));
    }

    @Test
    public void multiDestinationJewelleryListsEachOption()
    {
        Map<Integer, Integer> inventory = Collections.singletonMap(3853, 1);
        Map<Integer, String> names = Collections.singletonMap(3853, "Games necklace(8)");
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "test", null, false, ProgressSnapshot.empty(),
            SlayerTaskSnapshot.empty(), null, names);
        List<String> lines = planner().teleports(account);
        assertTrue(lines.stream().anyMatch(line -> line.contains("Games necklace → Burthorpe")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Games necklace — Barbarian Assault → Barbarian Assault")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Games necklace — Wintertodt → Wintertodt Camp")));
    }

    @Test
    public void enchantedLyreDestinationsFollowFremennikDiaryUnlocks()
    {
        Map<Integer, Integer> inventory = Collections.singletonMap(23458, 1);
        Map<Integer, String> itemNames = Collections.singletonMap(23458, "Enchanted lyre(i)");
        Map<String, String> diaries = new HashMap<>();
        String[] lockedDestinations = {
            "Enchanted lyre — Waterbirth Island", "Enchanted lyre — Neitiznot", "Enchanted lyre — Jatizso"
        };
        for (String status : new String[]{"Easy: Complete", "Hard: Complete", "Elite: Complete"})
        {
            diaries.put("Fremennik", status);
            AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
                Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
                Collections.emptyMap(), 3, 10, "test", null, false,
                new ProgressSnapshot(Collections.emptyMap(), diaries), SlayerTaskSnapshot.empty(),
                null, itemNames);
            List<String> lines = planner().teleports(account);
            assertTrue(lines.stream().anyMatch(line -> line.contains("Enchanted lyre → Rellekka")));
            for (int i = 0; i < lockedDestinations.length; i++)
            {
                String selectedDestination = lockedDestinations[i];
                boolean available = "Elite: Complete".equals(status)
                    || (i == 0 && "Hard: Complete".equals(status));
                assertEquals(selectedDestination + " at " + status, available,
                    lines.stream().anyMatch(line -> line.contains(selectedDestination + " → ")));
            }
        }
    }

    @Test
    public void ortusHerbRouteDoesNotAssumeUnvisitedVarlamoreRingUnlocked()
    {
        Map<String, String> quests = fairyRingQuests("FINISHED");
        quests.put("Children of the Sun", "FINISHED");
        Map<Integer, Integer> equipment = Collections.singletonMap(772, 1);
        Map<Integer, String> itemNames = Collections.singletonMap(772, "Dramen staff");
        AccountSnapshot unvisited = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), equipment,
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(quests, Collections.emptyMap()), SlayerTaskSnapshot.empty(),
            null, itemNames);
        List<String> before = planner().herbRun(unvisited);
        assertTrue(before.stream().anyMatch(line -> line.contains("Ortus Farm herb patch")
            && line.contains("visit Varlamore")));

        AccountSnapshot visited = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), equipment,
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(quests, Collections.emptyMap()), SlayerTaskSnapshot.empty(),
            null, itemNames, Collections.emptyList(), Collections.singletonMap("visitedVarlamore", 1));
        List<String> after = planner().herbRun(visited);
        assertTrue(after.stream().anyMatch(line -> line.contains("Ortus Farm herb patch")
            && line.contains("dial AJP")));
    }

    @Test
    public void bossReadinessMatchesConsumableVariantsAndAlternativeAmmunition()
    {
        Map<Integer, Integer> inventory = new HashMap<>();
        inventory.put(1001, 100);
        inventory.put(1002, 4);
        inventory.put(1003, 1);
        inventory.put(1004, 1);
        Map<Integer, String> names = new HashMap<>();
        names.put(1001, "Ruby dragon bolts (e)");
        names.put(1002, "Prayer potion(4)");
        names.put(1003, "Dragonfire ward");
        names.put(1004, "Ava's assembler");
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 100, 1500, "3200, 3200", null, false,
            ProgressSnapshot.empty(), SlayerTaskSnapshot.empty(), null, names);
        List<String> lines = planner().bossReadiness(account, "Vorkath");
        assertTrue(lines.stream().anyMatch(line -> line.contains("Ruby dragon bolts (e) (carried/equipped)")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Prayer potion(4) (carried/equipped)")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Dragonfire ward (carried/equipped)")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Ava's assembler (carried/equipped)")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("PACK / VERIFY: Food")));
        assertFalse(lines.stream().anyMatch(line -> line.contains("NOT DETECTED: Food")));
    }

    @Test
    public void varrockAndCamelotTabletsUseDiaryDependentLandings()
    {
        Map<Integer, Integer> inventory = new HashMap<>();
        inventory.put(8007, 1);
        inventory.put(8010, 1);
        Map<Integer, String> names = new HashMap<>();
        names.put(8007, "Varrock teleport");
        names.put(8010, "Camelot teleport");
        AccountSnapshot base = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "test", null, false, ProgressSnapshot.empty(),
            SlayerTaskSnapshot.empty(), null, names);
        List<String> baseLines = planner().teleports(base);
        assertTrue(baseLines.stream().anyMatch(line -> line.contains("Varrock teleport → Varrock")));
        assertTrue(baseLines.stream().anyMatch(line -> line.contains("Camelot teleport → Camelot")));

        Map<String, String> diaries = new HashMap<>();
        diaries.put("Varrock", "Medium: Complete");
        diaries.put("Kandarin", "Hard: Complete");
        AccountSnapshot upgraded = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "test", null, false,
            new ProgressSnapshot(Collections.emptyMap(), diaries), SlayerTaskSnapshot.empty(), null, names);
        List<String> upgradedLines = planner().teleports(upgraded);
        assertTrue(upgradedLines.stream().anyMatch(line -> line.contains("Varrock teleport → Varrock")));
        assertTrue(upgradedLines.stream().anyMatch(line -> line.contains("Varrock teleport — Grand Exchange landing → Grand Exchange")));
        assertTrue(upgradedLines.stream().anyMatch(line -> line.contains("Camelot teleport → Camelot")));
        assertTrue(upgradedLines.stream().anyMatch(line -> line.contains("Camelot teleport — Seers' bank landing → Seers' Village bank")));
    }

    @Test
    public void fairyRingRouteRequiresQuestChainPermissionAndEquippedStaff()
    {
        Map<Integer, Integer> inventory = Collections.singletonMap(772, 1);
        Map<Integer, String> names = Collections.singletonMap(772, "Dramen staff");
        Map<String, String> quests = fairyRingQuests("IN_PROGRESS");
        AccountSnapshot inventoryOnly = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.singletonMap("Agility", 76), Collections.emptyMap(), inventory, Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(quests, Collections.emptyMap()), SlayerTaskSnapshot.empty(),
            new com.nocturnal.ironman.model.NavigationSnapshot(new net.runelite.api.coords.WorldPoint(3200, 3424, 0), 3150, 3350, null), names);
        List<String> inventoryLines = planner().routeSuggestions(inventoryOnly, "Zul-Andra");
        assertTrue(inventoryLines.stream().anyMatch(line -> line.contains("equip a Dramen/Lunar staff")));
        assertFalse(inventoryLines.stream().anyMatch(line -> line.contains("Best network option")));

        AccountSnapshot equipped = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.singletonMap("Agility", 76), Collections.emptyMap(), Collections.emptyMap(), inventory,
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(quests, Collections.emptyMap()), SlayerTaskSnapshot.empty(),
            new com.nocturnal.ironman.model.NavigationSnapshot(new net.runelite.api.coords.WorldPoint(3200, 3424, 0), 3150, 3350, null), names);
        List<String> equippedLines = planner().routeSuggestions(equipped, "Zul-Andra");
        assertTrue(equippedLines.stream().anyMatch(line -> line.contains("Best network option")));
        assertTrue(equippedLines.stream().anyMatch(line -> line.contains("dial")));
    }

    @Test
    public void eliteLumbridgeDiaryWaivesFairyRingStaffRequirement()
    {
        Map<String, String> quests = fairyRingQuests("FINISHED");
        Map<String, String> diaries = Collections.singletonMap("Lumbridge & Draynor", "Elite: Complete");
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.singletonMap("Agility", 76), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(quests, diaries), SlayerTaskSnapshot.empty(),
            new com.nocturnal.ironman.model.NavigationSnapshot(new net.runelite.api.coords.WorldPoint(3200, 3424, 0), 3150, 3350, null),
            Collections.emptyMap());
        List<String> lines = planner().routeSuggestions(account, "Zul-Andra");
        assertTrue(lines.stream().anyMatch(line -> line.contains("Best network option")));
    }

    @Test
    public void fairyRingStaffInBankIsNotTreatedAsImmediatelyUsable()
    {
        Map<Integer, Integer> bank = Collections.singletonMap(772, 1);
        Map<Integer, String> names = Collections.singletonMap(772, "Dramen staff");
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            bank, 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(fairyRingQuests("FINISHED"), Collections.emptyMap()),
            SlayerTaskSnapshot.empty(), null, names);
        List<String> lines = planner().teleports(account);
        assertTrue(lines.stream().anyMatch(line -> line.contains("Staff detected in bank")));
    }

    private Map<String, String> fairyRingQuests(String fairyTwoState)
    {
        Map<String, String> quests = new HashMap<>();
        quests.put("The Restless Ghost", "FINISHED");
        quests.put("Priest in Peril", "FINISHED");
        quests.put("Nature Spirit", "FINISHED");
        quests.put("Lost City", "FINISHED");
        quests.put("Fairytale I - Growing Pains", "FINISHED");
        quests.put("Fairytale II - Cure a Queen", fairyTwoState);
        quests.put("Regicide", "FINISHED");
        return quests;
    }

    @Test
    public void aiBankContextFlagOmitsBankedTeleportsAndBankOnlyHerbRoutes()
    {
        Map<Integer, Integer> bank = new HashMap<>();
        bank.put(8007, 2);
        bank.put(772, 1);
        Map<Integer, String> names = new HashMap<>();
        names.put(8007, "Varrock teleport");
        names.put(772, "Dramen staff");
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            bank, 3, 10, "3200, 3424", null, false, ProgressSnapshot.empty(),
            SlayerTaskSnapshot.empty(), null, names);
        List<String> transport = planner().teleports(account, false);
        assertFalse(transport.stream().anyMatch(line -> line.contains("Varrock teleport") && line.contains("banked")));
        assertTrue(transport.stream().anyMatch(line -> line.contains("omitted by the AI bank-context setting")));
        assertFalse(transport.stream().anyMatch(line -> line.contains("Staff detected in bank")));
        List<String> herbRun = planner().herbRun(account, false);
        assertFalse(herbRun.stream().anyMatch(line -> line.contains("banked:")));
    }

    @Test
    public void thermonuclearSmokeDevilAllowsOnlyTheUncompletedDiaryException()
    {
        Map<String, Integer> levels = Collections.singletonMap("Slayer", 93);
        AccountSnapshot diaryIncomplete = new AccountSnapshot("test", "Ironman", "Standard",
            levels, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(Collections.emptyMap(), Collections.singletonMap("Western Provinces", "Elite: Incomplete")),
            SlayerTaskSnapshot.empty());
        List<String> available = planner().bossReadiness(diaryIncomplete, "Thermonuclear Smoke Devil");
        assertTrue(available.stream().anyMatch(line -> line.contains("Possible one-time diary exception")));

        AccountSnapshot diaryComplete = new AccountSnapshot("test", "Ironman", "Standard",
            levels, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(Collections.emptyMap(), Collections.singletonMap("Western Provinces", "Elite: Complete")),
            SlayerTaskSnapshot.empty());
        List<String> unavailable = planner().bossReadiness(diaryComplete, "Thermonuclear Smoke Devil");
        assertTrue(unavailable.stream().anyMatch(line -> line.contains("matching Slayer task is required")));
    }

    @Test
    public void nexReadinessChecksHardAccessSkillsSeparatelyFromRecommendations()
    {
        Map<String, Integer> levels = new HashMap<>();
        levels.put("Hitpoints", 70); levels.put("Ranged", 69); levels.put("Strength", 70); levels.put("Agility", 70);
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            levels, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(Collections.singletonMap("The Frozen Door", "FINISHED"), Collections.emptyMap()),
            SlayerTaskSnapshot.empty());
        List<String> lines = planner().bossReadiness(account, "Nex");
        assertTrue(lines.stream().anyMatch(line -> line.contains("HARD SKILL REQUIREMENTS")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("NOT MET Ranged: 69 / 70")));
    }

    @Test
    public void herbRunSeparatesAccessiblePatchesFromQuestAndLevelLockedPatches()
    {
        Map<String, Integer> levels = Collections.singletonMap("Farming", 65);
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            levels, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            ProgressSnapshot.empty(), SlayerTaskSnapshot.empty());
        List<String> lines = planner().herbRun(account, false);
        assertTrue(lines.stream().anyMatch(line -> line.contains("Ardougne herb patch")));
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("LOCKED — Troll Stronghold herb patch")));
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("LOCKED — Weiss herb patch")));
        assertFalse(lines.stream().anyMatch(line -> line.matches("\\d+\\. Troll Stronghold herb patch.*")));
    }

    @Test
    public void harmonyIslandHerbPatchRequiresEliteDiaryAndQuest()
    {
        Map<String, Integer> levels = Collections.singletonMap("Farming", 99);
        Map<String, String> quests = Collections.singletonMap("The Great Brain Robbery", "FINISHED");
        AccountSnapshot noDiary = new AccountSnapshot("test", "Ironman", "Standard",
            levels, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(quests, Collections.emptyMap()), SlayerTaskSnapshot.empty());
        assertTrue(planner().herbRun(noDiary, false).stream()
            .anyMatch(line -> line.startsWith("LOCKED — Harmony Island herb patch")));

        AccountSnapshot withDiary = new AccountSnapshot("test", "Ironman", "Standard",
            levels, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            new ProgressSnapshot(quests, Collections.singletonMap("Morytania", "Elite: Complete")),
            SlayerTaskSnapshot.empty());
        assertFalse(planner().herbRun(withDiary, false).stream()
            .anyMatch(line -> line.startsWith("LOCKED — Harmony Island herb patch")));
    }

    @Test
    public void herbRunDefersPatchesWithFutureGrowthEstimates()
    {
        java.time.Instant observed = java.time.Instant.parse("2026-11-15T12:00:00Z");
        com.nocturnal.ironman.model.FarmingPatchSnapshot growing =
            new com.nocturnal.ironman.model.FarmingPatchSnapshot("Catherby herb patch", "Ranarr",
                "Growing", 3, 4, observed, java.time.Instant.now().plusSeconds(3600));
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.singletonMap("Farming", 65), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            ProgressSnapshot.empty(), SlayerTaskSnapshot.empty(), null, Collections.emptyMap(),
            Collections.singletonList(growing));
        List<String> lines = planner().herbRun(account, false);
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("WAIT — Catherby herb patch")));
        assertFalse(lines.stream().anyMatch(line -> line.matches("\\d+\\. Catherby herb patch.*")));
    }

    @Test
    public void routePlannerResolvesUniquePartialDestinationNames()
    {
        AccountSnapshot account = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), 3, 10, "3200, 3424", null, false,
            ProgressSnapshot.empty(), SlayerTaskSnapshot.empty());
        List<String> lines = planner().routeSuggestions(account, "Quetzacalli");
        assertTrue(lines.stream().anyMatch(line -> line.contains("DESTINATION: Quetzacalli Gorge")));
    }

    @Test
    public void slayerBossWithoutTaskReportsNoActiveTask()
    {
        List<String> lines = planner().bossReadiness(account(SlayerTaskSnapshot.empty()), "Kraken");
        assertTrue(lines.stream().anyMatch(line -> line.contains("no active task detected")));
    }

    @Test
    public void slayerBossWithWrongTaskReportsCurrentTask()
    {
        SlayerTaskSnapshot task = new SlayerTaskSnapshot(true, "Aberrant spectres", "Slayer Tower", 120, 150, 50, 20);
        List<String> lines = planner().bossReadiness(account(task), "Kraken");
        assertTrue(lines.stream().anyMatch(line -> line.contains("current task: Aberrant spectres")));
    }

    @Test
    public void slayerBossWithMatchingTaskPassesTaskCheck()
    {
        SlayerTaskSnapshot task = new SlayerTaskSnapshot(true, "Cave kraken", "Kraken Cove", 120, 150, 50, 20);
        List<String> lines = planner().bossReadiness(account(task), "Kraken");
        assertTrue(lines.stream().anyMatch(line -> line.contains("Current Slayer task matches: Cave kraken")));
    }
}
