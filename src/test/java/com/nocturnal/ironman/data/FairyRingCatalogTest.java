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
            assertNotNull(ring.location);
            assertTrue(codes.add(ring.code));
        }
        assertEquals(47, FairyRingCatalog.ALL.size());
        FairyRingCatalog.Ring ajp = FairyRingCatalog.ALL.stream()
            .filter(ring -> ring.code.equals("AJP")).findFirst().orElse(null);
        assertNotNull(ajp);
        assertEquals(1648, ajp.location.getX());
        assertEquals(3011, ajp.location.getY());
    }
}
