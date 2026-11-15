package com.nocturnal.ironman.data;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class DestinationCatalogTest
{
    @Test
    public void destinationPickerIncludesMappedTransportLandings()
    {
        assertTrue(DestinationCatalog.ALL.stream().anyMatch(d -> d.name.equals("Civitas illa Fortis")));
        assertTrue(DestinationCatalog.ALL.stream().anyMatch(d -> d.name.equals("Hunter Guild")));
        assertTrue(DestinationCatalog.ALL.stream().anyMatch(d -> d.name.equals("Ghorrock Dungeon")));
        assertTrue(DestinationCatalog.ALL.stream().anyMatch(d -> d.name.equals("The Stranglewood")));
        assertTrue(DestinationCatalog.ALL.stream().anyMatch(d -> d.name.equals("Kourend Castle")));
        assertTrue(DestinationCatalog.ALL.stream().anyMatch(d -> d.name.equals("Arceuus Library")));
    }

    @Test
    public void herbRunCatalogIncludesAllCurrentPatchRegions()
    {
        long count = DestinationCatalog.ALL.stream()
            .filter(d -> "Farming".equals(d.activity) && d.name.toLowerCase().contains("herb patch"))
            .count();
        assertTrue(count >= 10);
    }

    @Test
    public void destinationNamesAreUnique()
    {
        Set<String> names = new HashSet<>();
        for (DestinationCatalog.Destination destination : DestinationCatalog.ALL)
            assertTrue(names.add(destination.name));
    }
}
