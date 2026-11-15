package com.nocturnal.ironman;

import com.nocturnal.ironman.data.SkillProgressionCatalog;
import org.junit.Test;
import static org.junit.Assert.*;

public class SkillProgressionCatalogTest
{
    @Test
    public void experienceCurveMatchesKnownOsrsThresholds()
    {
        assertEquals(0, SkillProgressionCatalog.xpForLevel(1));
        assertEquals(83, SkillProgressionCatalog.xpForLevel(2));
        assertEquals(13_034_431, SkillProgressionCatalog.xpForLevel(99));
    }

    @Test
    public void experienceCurveIsMonotonic()
    {
        int previous = 0;
        for (int level = 2; level <= 99; level++)
        {
            int current = SkillProgressionCatalog.xpForLevel(level);
            assertTrue(current > previous);
            previous = current;
        }
    }
}
