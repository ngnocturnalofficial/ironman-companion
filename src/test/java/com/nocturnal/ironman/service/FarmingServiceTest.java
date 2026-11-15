package com.nocturnal.ironman.service;

import com.nocturnal.ironman.model.FarmingPatchSnapshot;
import java.time.Instant;
import org.junit.Test;
import static org.junit.Assert.*;

public class FarmingServiceTest
{
    @Test
    public void decodesHerbGrowthStages()
    {
        FarmingService.HerbState state = FarmingService.decode(35);
        assertNotNull(state);
        assertEquals("Ranarr", state.crop);
        assertEquals("Growing", state.state);
        assertEquals(3, state.stage);
        assertEquals(4, state.stages);
    }

    @Test
    public void decodesHarvestableHerbs()
    {
        FarmingService.HerbState state = FarmingService.decode(38);
        assertNotNull(state);
        assertEquals("Ranarr", state.crop);
        assertEquals("Ready", state.state);
        assertEquals(0, state.stage);
    }

    @Test
    public void decodesDiseasedAndDeadHerbs()
    {
        FarmingService.HerbState diseased = FarmingService.decode(140);
        FarmingService.HerbState dead = FarmingService.decode(170);
        assertEquals("Ranarr", diseased.crop);
        assertEquals("Diseased", diseased.state);
        assertEquals("Any herb", dead.crop);
        assertEquals("Dead", dead.state);
    }

    @Test
    public void decodesWeedsAndGoutweed()
    {
        FarmingService.HerbState weeds = FarmingService.decode(0);
        FarmingService.HerbState goutweed = FarmingService.decode(197);
        assertEquals("Needs clearing", weeds.state);
        assertEquals("Goutweed", goutweed.crop);
        assertEquals("Ready", goutweed.state);
    }

    @Test
    public void mapsHerbPatchesToRuneLiteRegionVarbits()
    {
        assertTrue(FarmingService.isPatchRegion("Ardougne herb patch", 10548));
        assertTrue(FarmingService.isPatchRegion("Catherby herb patch", 11317));
        assertTrue(FarmingService.isPatchRegion("Morytania herb patch", 14390));
        assertTrue(FarmingService.isPatchRegion("Farming Guild herb patch", 4922));
        assertTrue(FarmingService.isPatchRegion("Ortus Farm herb patch", 6192));
        assertFalse(FarmingService.isPatchRegion("Catherby herb patch", 12083));
    }

    @Test
    public void excludesPortSarimSideOfFaladorRegion()
    {
        assertFalse(FarmingService.isFaladorHerbPatchInBounds(new net.runelite.api.coords.WorldPoint(3040, 3271, 0)));
        assertTrue(FarmingService.isFaladorHerbPatchInBounds(new net.runelite.api.coords.WorldPoint(3040, 3272, 0)));
    }

    @Test
    public void excludesCatherbyUpstairsFruitTreeArea()
    {
        assertTrue(FarmingService.isCatherbyHerbPatchInBounds(new net.runelite.api.coords.WorldPoint(2820, 3445, 0)));
        assertFalse(FarmingService.isCatherbyHerbPatchInBounds(new net.runelite.api.coords.WorldPoint(2840, 3445, 0)));
        assertFalse(FarmingService.isCatherbyHerbPatchInBounds(new net.runelite.api.coords.WorldPoint(2820, 3445, 1)));
    }

    @Test
    public void deadHerbStatePreservesStage()
    {
        FarmingService.HerbState state = FarmingService.decode(171);
        assertNotNull(state);
        assertEquals("Dead", state.state);
        assertEquals(2, state.stage);
        assertEquals(3, state.stages);
    }

    @Test
    public void unknownHerbVarbitRemainsUnknown()
    {
        assertEquals(null, FarmingService.decode(127));
    }

    @Test
    public void subMinuteGrowthEstimateRoundsUp()
    {
        Instant observed = Instant.parse("2026-11-15T12:00:00Z");
        FarmingPatchSnapshot patch = new FarmingPatchSnapshot("Falador herb patch", "Ranarr",
            "Growing", 3, 4, observed, observed.plusSeconds(30));
        assertTrue(patch.getStatusText(observed).contains("up to 1 min"));
    }

    @Test
    public void readyEstimateUsesLastObservedStage()
    {
        Instant observed = Instant.parse("2026-11-15T12:00:00Z");
        FarmingPatchSnapshot patch = new FarmingPatchSnapshot("Falador herb patch", "Ranarr",
            "Growing", 3, 5, observed, observed.plusSeconds(20 * 60));
        assertTrue(patch.getStatusText(observed.plusSeconds(21 * 60)).contains("possibly harvestable"));
        assertEquals(observed.plusSeconds(20 * 60), patch.getReadyAt());
    }
}
