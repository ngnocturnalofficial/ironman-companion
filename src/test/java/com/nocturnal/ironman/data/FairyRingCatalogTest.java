package com.nocturnal.ironman.data;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class FairyRingCatalogTest
{
    @Test
    public void currentRuneLiteFairyRingCodesAreUniqueAndIncludeAldarinRoute()
    {
        Set<String> codes = new HashSet<>();
        for (FairyRingCatalog.Ring ring : FairyRingCatalog.ALL)
        {
            assertTrue(codes.add(ring.code));
        }
        assertEquals(55, FairyRingCatalog.ALL.size());
        assertEquals(8, FairyRingCatalog.ALL.stream().filter(r -> r.location == null).count());
        FairyRingCatalog.Ring ajp = FairyRingCatalog.ALL.stream()
            .filter(ring -> ring.code.equals("AJP")).findFirst().orElse(null);
        assertNotNull(ajp);
        assertEquals(1648, ajp.location.getX());
        assertEquals(3011, ajp.location.getY());
        assertFalse(FairyRingCatalog.canWalkToDestination(
            FairyRingCatalog.ALL.stream().filter(r -> r.code.equals("DLR")).findFirst().get(), "Zul-Andra"));
    }
}
