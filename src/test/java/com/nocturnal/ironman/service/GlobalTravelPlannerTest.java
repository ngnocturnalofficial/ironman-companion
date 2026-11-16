package com.nocturnal.ironman.service;

import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.ProgressSnapshot;
import com.nocturnal.ironman.model.SlayerTaskSnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class GlobalTravelPlannerTest
{
    private AccountSnapshot account(Map<String, String> quests, int agility)
    {
        return new AccountSnapshot("test", "Ironman", "Standard",
            Collections.singletonMap("Agility", agility), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            20, 100, "Unknown", null, false,
            new ProgressSnapshot(quests, Collections.emptyMap()), SlayerTaskSnapshot.empty());
    }

    @Test
    public void spiritTreeTravelRespectsQuestAndOneWayStrongholdDeparture()
    {
        Map<String, String> quests = new HashMap<>();
        quests.put("Tree Gnome Village", "FINISHED");
        quests.put("Song of the Elves", "FINISHED");
        AccountSnapshot unlocked = account(quests, 1);
        GlobalTravelPlanner toPrif = new GlobalTravelPlanner(unlocked,
            new WorldPoint(3275, 6124, 0), "Prifddinas", false);
        List<GlobalTravelPlanner.Route> routes = toPrif.rank(Collections.singletonList(
            new GlobalTravelPlanner.Origin("At Grand Exchange", new WorldPoint(3185, 3510, 0), 0)), 3);
        assertEquals(1, routes.size());
        assertTrue(routes.get(0).steps.stream().anyMatch(step -> step.contains("spirit tree Prifddinas")));

        GlobalTravelPlanner strongholdOut = new GlobalTravelPlanner(unlocked,
            new WorldPoint(3185, 3510, 0), "Grand Exchange", false);
        assertTrue(strongholdOut.rank(Collections.singletonList(
            new GlobalTravelPlanner.Origin("At Gnome Stronghold", new WorldPoint(2460, 3446, 0), 0)), 3).isEmpty());

        quests.put("The Grand Tree", "FINISHED");
        GlobalTravelPlanner strongholdUnlocked = new GlobalTravelPlanner(account(quests, 1),
            new WorldPoint(3185, 3510, 0), "Grand Exchange", false);
        assertFalse(strongholdUnlocked.rank(Collections.singletonList(
            new GlobalTravelPlanner.Origin("At Gnome Stronghold", new WorldPoint(2460, 3446, 0), 0)), 3).isEmpty());
    }

    @Test
    public void islandRingCannotBeUsedAsWalkableZulAndraShortcut()
    {
        WorldPoint destination = new WorldPoint(2200, 3056, 0);
        GlobalTravelPlanner graph = new GlobalTravelPlanner(account(Collections.emptyMap(), 75),
            destination, "Zul-Andra", true);
        List<GlobalTravelPlanner.Origin> origins = Arrays.asList(
            new GlobalTravelPlanner.Origin("Poison Waste observation island", new WorldPoint(2212, 3101, 0), 0),
            new GlobalTravelPlanner.Origin("Castle Wars area", new WorldPoint(2384, 3037, 0), 0));
        assertTrue(graph.rank(origins, 3).isEmpty());

        Map<String, String> quests = Collections.singletonMap("Regicide", "FINISHED");
        GlobalTravelPlanner unlocked = new GlobalTravelPlanner(account(quests, 76),
            destination, "Zul-Andra", true);
        List<GlobalTravelPlanner.Route> available = unlocked.rank(
            Collections.singletonList(origins.get(0)), 3);
        assertEquals(1, available.size());
        assertTrue(available.get(0).steps.stream().anyMatch(step -> step.contains("fairy ring BJS")));
    }

    @Test
    public void rellekkaHasExplicitBoatConnectionToWaterbirthWithoutStraightLineWalk()
    {
        AccountSnapshot novice = account(Collections.emptyMap(), 1);
        GlobalTravelPlanner graph = new GlobalTravelPlanner(novice,
            new WorldPoint(2546, 3755, 0), "Waterbirth Island", false);
        List<GlobalTravelPlanner.Route> routes = graph.rank(Collections.singletonList(
            new GlobalTravelPlanner.Origin("Rellekka center", new WorldPoint(2658, 3661, 0), 0)), 3);
        assertEquals(1, routes.size());
        assertTrue(routes.get(0).steps.stream().anyMatch(step -> step.contains("Jarvald's boat")
            && step.contains("1,000 coins")));

        GlobalTravelPlanner returnTrip = new GlobalTravelPlanner(novice,
            new WorldPoint(2658, 3661, 0), "Rellekka", false);
        List<GlobalTravelPlanner.Route> returnRoutes = returnTrip.rank(Collections.singletonList(
            new GlobalTravelPlanner.Origin("Waterbirth Island", new WorldPoint(2546, 3755, 0), 0)), 3);
        assertEquals(1, returnRoutes.size());
        assertTrue(returnRoutes.get(0).steps.stream().anyMatch(step -> step.contains("Jarvald's return boat")));
    }

    @Test
    public void quetzalTransportNeedsNetworkUnlockAndUsesOnlyKnownLandings()
    {
        Map<String, Integer> visited = Collections.singletonMap("visitedVarlamore", 1);
        Map<String, Integer> unlocked = new HashMap<>(visited);
        unlocked.put("quetzalNetwork", 1);
        WorldPoint auburnvale = new WorldPoint(1411, 3361, 0);
        GlobalTravelPlanner.Origin origin = new GlobalTravelPlanner.Origin(
            "Hunter Guild", new WorldPoint(1585, 3053, 0), 0);

        AccountSnapshot noNetwork = account(Collections.emptyMap(), 1);
        GlobalTravelPlanner withoutFlight = new GlobalTravelPlanner(noNetwork, auburnvale,
            "Auburnvale", false);
        assertFalse(withoutFlight.rank(Collections.singletonList(origin), 2).stream()
            .flatMap(route -> route.steps.stream()).anyMatch(step -> step.contains("Fly by quetzal")));

        AccountSnapshot networkAccount = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), 3, 10, "unknown", null, false,
            ProgressSnapshot.empty(), SlayerTaskSnapshot.empty(), null,
            Collections.emptyMap(), Collections.emptyList(), unlocked);
        GlobalTravelPlanner withFlight = new GlobalTravelPlanner(networkAccount, auburnvale,
            "Auburnvale", false);
        assertTrue(withFlight.rank(Collections.singletonList(origin), 2).stream()
            .flatMap(route -> route.steps.stream()).anyMatch(step -> step.contains("Fly by quetzal")));
    }
}
