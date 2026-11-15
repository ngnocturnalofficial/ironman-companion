package com.nocturnal.ironman;

import com.nocturnal.ironman.data.DestinationCatalog;
import com.nocturnal.ironman.data.ActivityCatalog;
import com.nocturnal.ironman.data.BossCatalog;
import com.nocturnal.ironman.data.TeleportCatalog;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class CatalogTest
{
    @Test public void destinationsHaveNamesAndCoordinates()
    {
        Set<String> names=new HashSet<>();
        for(DestinationCatalog.Destination d:DestinationCatalog.ALL)
        { assertNotNull(d.name); assertTrue(d.x>0); assertTrue(d.y>0); names.add(d.name); }
        assertTrue(names.contains("Farming Guild"));
        assertTrue(names.contains("Slayer Tower"));
    }
    @Test public void teleportEntriesHaveItemIds()
    {
        for(TeleportCatalog.Teleport t:TeleportCatalog.ALL)
        { assertNotNull(t.name); assertNotNull(t.destination); assertTrue(t.itemIds.length>0 || t.nameMatches.length>0); }
    }

    @Test public void expandedTeleportOptionsHaveUniqueNames()
    {
        Set<String> names = new HashSet<>();
        for (TeleportCatalog.Teleport teleport : TeleportCatalog.ALL)
        {
            assertTrue(names.add(teleport.name));
        }
        assertTrue(TeleportCatalog.ALL.size() >= 150);
        assertTrue(TeleportCatalog.ALL.stream().anyMatch(t -> t.name.equals("Sailors' Amulet — Port Roberts")));
        assertTrue(TeleportCatalog.ALL.stream().anyMatch(t -> t.name.equals("Pendant of Ates — North Aldarin")));
    }

    @Test public void planningAndBossLibrariesHaveBroadCoverage()
    {
        assertTrue(ActivityCatalog.ALL.size() >= 20);
        assertTrue(BossCatalog.ALL.size() >= 35);
        assertTrue(DestinationCatalog.ALL.size() >= 150);
    }

    @Test public void teleportCatalogPreservesNonzeroLandingPlanes()
    {
        TeleportCatalog.Teleport kaleb = TeleportCatalog.ALL.stream()
            .filter(t -> t.name.equals("Achievement diary cape — Kaleb Paramaya"))
            .findFirst().orElse(null);
        assertNotNull(kaleb);
        assertEquals(1, kaleb.plane);
    }

    @Test public void teleportNameMatchingCoversChargedAndDiaryVariants()
    {
        TeleportCatalog.Teleport games = TeleportCatalog.ALL.stream().filter(t -> t.name.equals("Games necklace")).findFirst().get();
        TeleportCatalog.Teleport xeric = TeleportCatalog.ALL.stream().filter(t -> t.name.equals("Xeric's talisman")).findFirst().get();
        TeleportCatalog.Teleport rada = TeleportCatalog.ALL.stream().filter(t -> t.name.equals("Rada's blessing")).findFirst().get();
        assertTrue(games.matchesItemName("Games necklace(8)"));
        assertTrue(xeric.matchesItemName("Xeric's talisman (5)"));
        assertTrue(rada.matchesItemName("Rada's blessing 4"));
        assertFalse(games.matchesItemName("Skills necklace(8)"));
        assertFalse(games.matchesItemName("Games necklace(0)"));
    }
}
