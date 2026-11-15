package com.nocturnal.ironman.model;

import net.runelite.api.coords.WorldPoint;

/** Immutable client-thread copy of the current scene's collision grid for safe UI-thread routing. */
public final class NavigationSnapshot
{
    private final WorldPoint playerLocation;
    private final int baseX;
    private final int baseY;
    private final int[][] collisionFlags;

    public NavigationSnapshot(WorldPoint playerLocation, int baseX, int baseY, int[][] collisionFlags)
    {
        this.playerLocation = playerLocation;
        this.baseX = baseX;
        this.baseY = baseY;
        this.collisionFlags = copy(collisionFlags);
    }

    private static int[][] copy(int[][] source)
    {
        if (source == null) return null;
        int[][] result = new int[source.length][];
        for (int x = 0; x < source.length; x++) result[x] = source[x] == null ? null : source[x].clone();
        return result;
    }

    public WorldPoint getPlayerLocation() { return playerLocation; }
    public int getBaseX() { return baseX; }
    public int getBaseY() { return baseY; }
    public int[][] getCollisionFlags() { return copy(collisionFlags); }
    public boolean hasCollisionGrid() { return collisionFlags != null && collisionFlags.length > 0; }
}
