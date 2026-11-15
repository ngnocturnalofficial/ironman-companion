package com.nocturnal.ironman.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import com.nocturnal.ironman.model.NavigationSnapshot;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.coords.WorldPoint;

/** A* pathfinding over the currently loaded RuneLite scene collision map. */
public class ScenePathfinder
{
    private static final int CARDINAL_COST = 10;
    private static final int DIAGONAL_COST = 14;
    private static final int[][] DIRECTIONS = {
        {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
    };
    private static final String[] DIRECTION_NAMES = {
        "North", "North-east", "East", "South-east", "South", "South-west", "West", "North-west"
    };

    public PathResult findPath(NavigationSnapshot navigation, WorldPoint target)
    {
        if (navigation == null || navigation.getPlayerLocation() == null || target == null)
            return PathResult.unavailable("Player or destination coordinates are unavailable.");

        WorldPoint start = navigation.getPlayerLocation();
        if (start.getPlane() != target.getPlane())
            return PathResult.unavailable("The destination is on a different plane; a ladder, stair or other transition is required.");

        int[][] flags = navigation.getCollisionFlags();
        if (flags == null || flags.length == 0 || flags[0] == null)
            return PathResult.unavailable("Collision data is unavailable for the current scene.");

        int startX = start.getX() - navigation.getBaseX();
        int startY = start.getY() - navigation.getBaseY();
        int targetX = target.getX() - navigation.getBaseX();
        int targetY = target.getY() - navigation.getBaseY();
        return findPathOnGrid(flags, startX, startY, targetX, targetY);
    }

    public static PathResult findPathOnGrid(int[][] flags, int startX, int startY, int targetX, int targetY)
    {
        if (flags == null || flags.length == 0 || flags[0] == null)
            return PathResult.unavailable("The current scene has no collision grid.");
        int width = flags.length;
        int height = flags[0].length;
        for (int x = 0; x < width; x++)
        {
            if (flags[x] == null || flags[x].length != height)
                return PathResult.unavailable("The collision grid is malformed.");
        }

        if (!inside(startX, startY, width, height)) return PathResult.unavailable("The player is outside the loaded scene bounds.");
        if (!inside(targetX, targetY, width, height)) return PathResult.outOfScene();
        if (startX == targetX && startY == targetY) return PathResult.found(0, Collections.emptyList());

        int[][] costs = new int[width][height];
        int[][] previousX = new int[width][height];
        int[][] previousY = new int[width][height];
        for (int x = 0; x < width; x++)
        {
            Arrays.fill(costs[x], Integer.MAX_VALUE);
            Arrays.fill(previousX[x], -1);
            Arrays.fill(previousY[x], -1);
        }

        PriorityQueue<Node> open = new PriorityQueue<>((a, b) -> Integer.compare(a.estimate, b.estimate));
        costs[startX][startY] = 0;
        open.add(new Node(startX, startY, 0, heuristic(startX, startY, targetX, targetY)));
        boolean[][] closed = new boolean[width][height];
        int visited = 0;
        final int visitLimit = width * height;

        while (!open.isEmpty() && visited < visitLimit)
        {
            Node current = open.poll();
            if (closed[current.x][current.y]) continue;
            closed[current.x][current.y] = true;
            visited++;

            if (current.x == targetX && current.y == targetY)
                return reconstruct(startX, startY, targetX, targetY, costs[targetX][targetY], previousX, previousY);

            for (int direction = 0; direction < DIRECTIONS.length; direction++)
            {
                int dx = DIRECTIONS[direction][0];
                int dy = DIRECTIONS[direction][1];
                int nx = current.x + dx;
                int ny = current.y + dy;
                if (!inside(nx, ny, width, height) || closed[nx][ny] || !canStep(flags, current.x, current.y, dx, dy)) continue;

                int stepCost = dx != 0 && dy != 0 ? DIAGONAL_COST : CARDINAL_COST;
                int nextCost = current.cost + stepCost;
                if (nextCost >= costs[nx][ny]) continue;
                costs[nx][ny] = nextCost;
                previousX[nx][ny] = current.x;
                previousY[nx][ny] = current.y;
                open.add(new Node(nx, ny, nextCost, nextCost + heuristic(nx, ny, targetX, targetY)));
            }
        }

        return PathResult.unavailable("No collision-valid path was found inside the loaded scene.");
    }

    private static boolean canStep(int[][] flags, int x, int y, int dx, int dy)
    {
        int nx = x + dx;
        int ny = y + dy;
        int destinationFlags = flags[nx][ny];
        if ((destinationFlags & CollisionDataFlag.BLOCK_MOVEMENT_FULL) != 0) return false;

        if (dx == 0 || dy == 0) return cardinalOpen(flags, x, y, dx, dy);

        int diagonal = diagonalFlag(dx, dy);
        int opposite = diagonalFlag(-dx, -dy);
        if ((flags[x][y] & diagonal) != 0 || (destinationFlags & opposite) != 0) return false;

        // Do not cut diagonally through the corner of a blocked tile or wall.
        return cardinalOpen(flags, x, y, dx, 0)
            && cardinalOpen(flags, x, y, 0, dy)
            && cardinalOpen(flags, x + dx, y, 0, dy)
            && cardinalOpen(flags, x, y + dy, dx, 0);
    }

    private static boolean cardinalOpen(int[][] flags, int x, int y, int dx, int dy)
    {
        int nx = x + dx;
        int ny = y + dy;
        if (nx < 0 || nx >= flags.length || ny < 0 || ny >= flags[0].length) return false;
        if ((flags[nx][ny] & CollisionDataFlag.BLOCK_MOVEMENT_FULL) != 0) return false;

        int outgoing;
        int incoming;
        if (dx == 1) { outgoing = CollisionDataFlag.BLOCK_MOVEMENT_EAST; incoming = CollisionDataFlag.BLOCK_MOVEMENT_WEST; }
        else if (dx == -1) { outgoing = CollisionDataFlag.BLOCK_MOVEMENT_WEST; incoming = CollisionDataFlag.BLOCK_MOVEMENT_EAST; }
        else if (dy == 1) { outgoing = CollisionDataFlag.BLOCK_MOVEMENT_NORTH; incoming = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH; }
        else if (dy == -1) { outgoing = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH; incoming = CollisionDataFlag.BLOCK_MOVEMENT_NORTH; }
        else return false;

        return (flags[x][y] & outgoing) == 0 && (flags[nx][ny] & incoming) == 0;
    }

    private static int diagonalFlag(int dx, int dy)
    {
        if (dx > 0 && dy > 0) return CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST;
        if (dx > 0) return CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST;
        if (dy < 0) return CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST;
        return CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST;
    }

    private static int heuristic(int x, int y, int tx, int ty)
    {
        int dx = Math.abs(tx - x);
        int dy = Math.abs(ty - y);
        int diagonal = Math.min(dx, dy);
        int straight = Math.max(dx, dy) - diagonal;
        return diagonal * DIAGONAL_COST + straight * CARDINAL_COST;
    }

    private static boolean inside(int x, int y, int width, int height)
    {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private static PathResult reconstruct(int sx, int sy, int tx, int ty, int cost, int[][] previousX, int[][] previousY)
    {
        List<Integer> directions = new ArrayList<>();
        int x = tx;
        int y = ty;
        while (x != sx || y != sy)
        {
            int px = previousX[x][y];
            int py = previousY[x][y];
            if (px < 0 || py < 0) return PathResult.unavailable("The path could not be reconstructed.");
            directions.add(directionIndex(x - px, y - py));
            x = px;
            y = py;
        }
        Collections.reverse(directions);

        List<String> instructions = new ArrayList<>();
        int currentDirection = -1;
        int run = 0;
        for (int direction : directions)
        {
            if (direction != currentDirection)
            {
                if (run > 0) instructions.add(DIRECTION_NAMES[currentDirection] + " × " + run);
                currentDirection = direction;
                run = 1;
            }
            else run++;
        }
        if (run > 0) instructions.add(DIRECTION_NAMES[currentDirection] + " × " + run);
        return PathResult.found(directions.size(), instructions);
    }

    private static int directionIndex(int dx, int dy)
    {
        for (int i = 0; i < DIRECTIONS.length; i++)
            if (DIRECTIONS[i][0] == dx && DIRECTIONS[i][1] == dy) return i;
        return 0;
    }

    private static final class Node
    {
        private final int x;
        private final int y;
        private final int cost;
        private final int estimate;

        private Node(int x, int y, int cost, int estimate)
        {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.estimate = estimate;
        }
    }

    public static final class PathResult
    {
        private final boolean found;
        private final boolean targetOutsideScene;
        private final int steps;
        private final List<String> instructions;
        private final String message;

        private PathResult(boolean found, boolean targetOutsideScene, int steps, List<String> instructions, String message)
        {
            this.found = found;
            this.targetOutsideScene = targetOutsideScene;
            this.steps = steps;
            this.instructions = Collections.unmodifiableList(new ArrayList<>(instructions));
            this.message = message;
        }

        private static PathResult found(int steps, List<String> instructions)
        {
            return new PathResult(true, false, steps, instructions, "");
        }

        private static PathResult unavailable(String message)
        {
            return new PathResult(false, false, 0, Collections.emptyList(), message);
        }

        private static PathResult outOfScene()
        {
            return new PathResult(false, true, 0, Collections.emptyList(), "Destination is outside the currently loaded scene.");
        }

        public boolean isFound() { return found; }
        public boolean isTargetOutsideScene() { return targetOutsideScene; }
        public int getSteps() { return steps; }
        public List<String> getInstructions() { return instructions; }
        public String getMessage() { return message; }
    }
}
