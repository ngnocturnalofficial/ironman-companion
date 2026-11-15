package com.nocturnal.ironman.navigation;

import com.nocturnal.ironman.model.NavigationSnapshot;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScenePathfinderTest
{
    @Test
    public void openSceneUsesDiagonalShortestPath()
    {
        int[][] flags = new int[12][12];
        ScenePathfinder.PathResult result = ScenePathfinder.findPathOnGrid(flags, 1, 1, 8, 8);
        assertTrue(result.isFound());
        assertEquals(7, result.getSteps());
        assertEquals(1, result.getInstructions().size());
        assertEquals("North-east × 7", result.getInstructions().get(0));
    }

    @Test
    public void routeDetoursAroundBlockedTiles()
    {
        int[][] flags = new int[12][12];
        for (int y = 0; y < 10; y++) flags[5][y] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
        ScenePathfinder.PathResult result = ScenePathfinder.findPathOnGrid(flags, 2, 5, 8, 5);
        assertTrue(result.isFound());
        assertTrue(result.getSteps() > 6);
    }

    @Test
    public void fullyBlockedBarrierHasNoRoute()
    {
        int[][] flags = new int[8][8];
        for (int y = 0; y < 8; y++) flags[4][y] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
        ScenePathfinder.PathResult result = ScenePathfinder.findPathOnGrid(flags, 1, 3, 6, 3);
        assertFalse(result.isFound());
    }

    @Test
    public void diagonalDoesNotCutBlockedCorner()
    {
        int[][] flags = new int[3][3];
        flags[2][1] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
        flags[1][2] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
        ScenePathfinder.PathResult result = ScenePathfinder.findPathOnGrid(flags, 1, 1, 2, 2);
        assertFalse(result.isFound());
    }

    @Test
    public void snapshotRouteUsesCapturedSceneOrigin()
    {
        int[][] flags = new int[12][12];
        NavigationSnapshot snapshot = new NavigationSnapshot(
            new WorldPoint(3201, 3201, 0), 3200, 3200, flags);
        ScenePathfinder.PathResult result = new ScenePathfinder().findPath(
            snapshot, new WorldPoint(3208, 3208, 0));
        assertTrue(result.isFound());
        assertEquals(7, result.getSteps());
    }

    @Test
    public void snapshotRouteDoesNotNeedLiveClient()
    {
        NavigationSnapshot snapshot = new NavigationSnapshot(
            new WorldPoint(3201, 3201, 0), 3200, 3200, new int[5][5]);
        ScenePathfinder.PathResult result = new ScenePathfinder().findPath(
            snapshot, new WorldPoint(3210, 3210, 0));
        assertTrue(result.isTargetOutsideScene());
    }

    @Test
    public void targetOutsideSceneIsReportedSeparately()
    {
        ScenePathfinder.PathResult result = ScenePathfinder.findPathOnGrid(new int[5][5], 1, 1, 9, 9);
        assertFalse(result.isFound());
        assertTrue(result.isTargetOutsideScene());
    }
}
