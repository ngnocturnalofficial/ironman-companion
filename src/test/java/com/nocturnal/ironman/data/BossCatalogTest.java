package com.nocturnal.ironman.data;

import org.junit.Test;
import static org.junit.Assert.*;

public class BossCatalogTest
{
    @Test
    public void bossGuidesHaveRoutesAndWikiSources()
    {
        assertTrue(BossCatalog.ALL.size() >= 8);
        for (BossCatalog.Boss boss : BossCatalog.ALL)
        {
            assertNotNull(boss.name);
            assertNotNull(boss.destination);
            assertTrue(boss.wikiUrl.startsWith("https://oldschool.runescape.wiki/"));
            assertTrue(DestinationCatalog.ALL.stream().anyMatch(d -> d.name.equals(boss.destination)));
        }
    }

    @Test
    public void expandedBossLibraryIncludesDesertTreasureTwoAndIronmanBosses()
    {
        assertNotNull(BossCatalog.find("Vardorvis"));
        assertNotNull(BossCatalog.find("Duke Sucellus"));
        assertNotNull(BossCatalog.find("The Whisperer"));
        assertNotNull(BossCatalog.find("The Leviathan"));
        assertNotNull(BossCatalog.find("Araxxor"));
        assertNotNull(BossCatalog.find("Nex"));
        assertNotNull(BossCatalog.find("Zalcano"));
        assertNotNull(BossCatalog.find("Hespori"));
        assertNotNull(BossCatalog.find("Obor"));
        assertNotNull(BossCatalog.find("Bryophyta"));
        assertNotNull(BossCatalog.find("Corporeal Beast"));
        assertNotNull(BossCatalog.find("General Graardor"));
        assertNotNull(BossCatalog.find("K'ril Tsutsaroth"));
        assertNotNull(BossCatalog.find("Commander Zilyana"));
        assertNotNull(BossCatalog.find("Kree'arra"));
        assertNotNull(BossCatalog.find("The Inferno"));
        assertNotNull(BossCatalog.find("TzKal-Zuk"));
        assertEquals(Integer.valueOf(70), BossCatalog.find("Nex").requiredSkills.get("Ranged"));
        assertEquals(92, BossCatalog.find("Araxxor").requiredSlayerLevel);
    }

    @Test
    public void slayerBossesRetainHardTaskRequirements()
    {
        BossCatalog.Boss kraken = BossCatalog.find("Kraken");
        BossCatalog.Boss cerberus = BossCatalog.find("Cerberus");
        BossCatalog.Boss hydra = BossCatalog.find("Alchemical Hydra");
        assertEquals(87, kraken.requiredSlayerLevel);
        assertEquals(91, cerberus.requiredSlayerLevel);
        assertEquals(95, hydra.requiredSlayerLevel);
        assertTrue(hydra.requiredTaskMatches.length > 0);
    }
}
