package com.nocturnal.ironman;

import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.NavigationSnapshot;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class AccountSnapshotTest
{
    @Test public void itemCountsAggregateAcrossContainers()
    {
        Map<Integer,Integer> inventory=new HashMap<>(); inventory.put(8007,2);
        Map<Integer,Integer> equipment=new HashMap<>(); equipment.put(8007,1);
        Map<Integer,Integer> bank=new HashMap<>(); bank.put(8007,4);
        AccountSnapshot s=new AccountSnapshot("test",Collections.emptyMap(),Collections.emptyMap(),inventory,equipment,bank,3,10,"test");
        assertEquals(7,s.countItem(8007));
        assertTrue(s.ownsAny(1,8007));
        assertFalse(s.ownsAny(1,2));
    }

    @Test public void separatesCarriedItemsFromBankOwnership()
    {
        Map<Integer,Integer> inventory=new HashMap<>(); inventory.put(8007,1);
        Map<Integer,Integer> bank=new HashMap<>(); bank.put(8007,5);
        AccountSnapshot s=new AccountSnapshot("test",Collections.emptyMap(),Collections.emptyMap(),inventory,Collections.emptyMap(),bank,3,10,"test");
        assertEquals(1,s.countAccessibleItem(8007));
        assertEquals(5,s.countBankedItem(8007));
        assertEquals(6,s.countItem(8007));
    }

    @Test public void retainsNavigationSnapshot()
    {
        NavigationSnapshot navigation = new NavigationSnapshot(
            new WorldPoint(3201, 3202, 0), 3200, 3200, new int[4][4]);
        AccountSnapshot snapshot = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), 3, 10, "3201, 3202",
            null, false, null, null, navigation);
        assertNotNull(snapshot.getNavigation());
        assertEquals(3201, snapshot.getNavigation().getPlayerLocation().getX());
    }

    @Test public void itemNamesAreCapturedAndImmutable()
    {
        Map<Integer, String> names = new HashMap<>();
        names.put(8007, "Varrock teleport");
        AccountSnapshot snapshot = new AccountSnapshot("test", "Ironman", "Standard",
            Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), 3, 10, "3200, 3200",
            null, false, null, null, null, names);
        names.put(8007, "Changed");
        assertEquals("Varrock teleport", snapshot.getItemName(8007));
        assertEquals("Item #999", snapshot.getItemName(999));
    }

    @Test public void snapshotDefensivelyCopiesInput()
    {
        Map<Integer,Integer> inventory=new HashMap<>(); inventory.put(1,1);
        AccountSnapshot s=new AccountSnapshot("test",Collections.emptyMap(),Collections.emptyMap(),inventory,Collections.emptyMap(),Collections.emptyMap(),3,10,"test");
        inventory.put(1,99);
        assertEquals(Integer.valueOf(1),s.getInventory().get(1));
    }
}
