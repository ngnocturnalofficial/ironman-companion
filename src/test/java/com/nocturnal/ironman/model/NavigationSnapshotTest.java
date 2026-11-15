package com.nocturnal.ironman.model;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class NavigationSnapshotTest
{
    @Test
    public void collisionGridIsCopiedAtCapture()
    {
        int[][] flags = new int[2][2];
        flags[1][1] = 7;
        NavigationSnapshot snapshot = new NavigationSnapshot(new WorldPoint(3201, 3201, 0), 3200, 3200, flags);
        flags[1][1] = 99;
        assertEquals(7, snapshot.getCollisionFlags()[1][1]);
    }

    @Test
    public void callersCannotMutateCapturedCollisionGrid()
    {
        int[][] flags = new int[2][2];
        flags[1][1] = 7;
        NavigationSnapshot snapshot = new NavigationSnapshot(new WorldPoint(3201, 3201, 0), 3200, 3200, flags);
        int[][] exposed = snapshot.getCollisionFlags();
        exposed[1][1] = 99;
        assertEquals(7, snapshot.getCollisionFlags()[1][1]);
    }

    @Test
    public void missingCollisionGridRemainsExplicit()
    {
        NavigationSnapshot snapshot = new NavigationSnapshot(new WorldPoint(3201, 3201, 0), 3200, 3200, null);
        assertFalse(snapshot.hasCollisionGrid());
    }
}
