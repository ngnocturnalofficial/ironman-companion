package com.nocturnal.ironman.data;

import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.ProgressSnapshot;
import com.nocturnal.ironman.model.SlayerTaskSnapshot;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuetzalCatalogTest
{
    private AccountSnapshot account(Map<String, Integer> travelSettings)
    {
        return new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), 3, 10, "unknown",
            null, false, ProgressSnapshot.empty(), SlayerTaskSnapshot.empty(),
            null, Collections.emptyMap(), Collections.emptyList(), travelSettings);
    }

    @Test
    public void defaultBuiltLandingsRequireVarlamoreVisitAndExtraSitesRequireConstruction()
    {
        AccountSnapshot unknown = account(Collections.emptyMap());
        assertFalse(QuetzalCatalog.isDestinationAvailable(unknown, "Hunter Guild"));

        Map<String, Integer> settings = new HashMap<>();
        settings.put("visitedVarlamore", 1);
        AccountSnapshot visited = account(settings);
        assertTrue(QuetzalCatalog.isDestinationAvailable(visited, "Hunter Guild"));
        assertTrue(QuetzalCatalog.isDestinationAvailable(visited, "Auburnvale"));
        assertFalse(QuetzalCatalog.isDestinationAvailable(visited, "Kastori"));
        assertFalse(QuetzalCatalog.isDestinationAvailable(visited, "Unknown site"));

        settings.put("quetzalKastori", 1);
        assertTrue(QuetzalCatalog.isDestinationAvailable(account(settings), "Kastori"));
        assertFalse(QuetzalCatalog.isDestinationAvailable(account(settings), "Outer Fortis"));
    }

    @Test
    public void renuNetworkAccessIsIndependentOfWhistleDestinationAccess()
    {
        Map<String, Integer> settings = new HashMap<>();
        settings.put("visitedVarlamore", 1);
        assertFalse(QuetzalCatalog.hasNetworkAccess(account(settings)));
        settings.put("quetzalNetwork", 1);
        assertTrue(QuetzalCatalog.hasNetworkAccess(account(settings)));
        settings.put("visitedVarlamore", 0);
        assertFalse(QuetzalCatalog.hasNetworkAccess(account(settings)));
    }
}
