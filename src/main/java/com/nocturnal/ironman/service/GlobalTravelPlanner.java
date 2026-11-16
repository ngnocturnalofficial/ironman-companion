package com.nocturnal.ironman.service;

import com.nocturnal.ironman.data.FairyRingCatalog;
import com.nocturnal.ironman.data.QuetzalCatalog;
import com.nocturnal.ironman.data.SpiritTreeCatalog;
import com.nocturnal.ironman.model.AccountSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import net.runelite.api.coords.WorldPoint;

/**
 * An advisory directed transport graph. Walking edges are geometric lower bounds;
 * only the separately provided scene pathfinder can verify actual walkability.
 */
public final class GlobalTravelPlanner
{
    private static final int APPROACH_LIMIT = 180;
    private static final int TRANSFER_LIMIT = 140;
    private static final int FINAL_WALK_LIMIT = 450;
    private static final int TRANSPORT_COST = 8;
    private static final int INFINITY = Integer.MAX_VALUE / 4;

    public static final class Origin
    {
        public final String description;
        public final WorldPoint location;
        public final int actionCost;

        public Origin(String description, WorldPoint location, int actionCost)
        {
            this.description = description;
            this.location = location;
            this.actionCost = actionCost;
        }
    }

    public static final class Route
    {
        public final int estimatedCost;
        public final List<String> steps;

        private Route(int estimatedCost, List<String> steps)
        {
            this.estimatedCost = estimatedCost;
            this.steps = Collections.unmodifiableList(steps);
        }
    }

    private static final class Node
    {
        private final String name;
        private final WorldPoint location;
        private final String network;
        private final boolean canDepart;
        private final boolean isolated;
        private final boolean canFinishByWalking;

        private Node(String name, WorldPoint location, String network, boolean canDepart,
            boolean isolated, boolean canFinishByWalking)
        {
            this.name = name;
            this.location = location;
            this.network = network;
            this.canDepart = canDepart;
            this.isolated = isolated;
            this.canFinishByWalking = canFinishByWalking;
        }
    }

    private static final class Edge
    {
        private final int from;
        private final int to;
        private final int cost;
        private final String action;

        private Edge(int from, int to, int cost, String action)
        {
            this.from = from;
            this.to = to;
            this.cost = cost;
            this.action = action;
        }
    }

    private static final class QueueEntry
    {
        private final int node;
        private final int cost;

        private QueueEntry(int node, int cost)
        {
            this.node = node;
            this.cost = cost;
        }
    }

    private final List<Node> nodes = new ArrayList<>();
    private final List<List<Edge>> incoming = new ArrayList<>();
    private final WorldPoint target;
    private final String destinationName;
    private final int jarvaldDockIndex;
    private final int waterbirthLandingIndex;
    private final int destinationIndex;
    private final int[] remainingCost;
    private final Edge[] nextEdge;

    public GlobalTravelPlanner(AccountSnapshot account, WorldPoint target, String destinationName,
        boolean fairyRingAccess)
    {
        this.target = target;
        this.destinationName = destinationName;
        if (fairyRingAccess)
        {
            for (FairyRingCatalog.Ring ring : FairyRingCatalog.ALL)
                if (ring.location != null && FairyRingCatalog.isDestinationUnlocked(account, ring))
                    nodes.add(new Node("fairy ring " + ring.code, ring.location, "fairy", true,
                        FairyRingCatalog.isIsolated(ring),
                        FairyRingCatalog.canWalkToDestination(ring, destinationName)));
        }
        for (SpiritTreeCatalog.Tree tree : SpiritTreeCatalog.available(account))
            nodes.add(new Node("spirit tree " + tree.name, tree.location, "spirit",
                tree.canDepart(account), false, true));
        if (QuetzalCatalog.hasNetworkAccess(account))
        {
            for (QuetzalCatalog.Landing landing : QuetzalCatalog.ALL)
                if (landing.location != null && landing.isAvailable(account))
                    nodes.add(new Node("quetzal landing at " + landing.name, landing.location,
                        "quetzal", true, false, true));
        }

        jarvaldDockIndex = nodes.size();
        nodes.add(new Node("Jarvald at the Rellekka docks (approximate location)",
            new WorldPoint(2638, 3701, 0), "jarvald", true, false, true));
        waterbirthLandingIndex = nodes.size();
        nodes.add(new Node("Waterbirth Island boat landing", new WorldPoint(2546, 3755, 0),
            "jarvald", true, true, true));

        destinationIndex = nodes.size();
        for (int i = 0; i <= destinationIndex; i++) incoming.add(new ArrayList<>());
        addEdges();
        remainingCost = new int[destinationIndex + 1];
        nextEdge = new Edge[destinationIndex + 1];
        calculateReverseShortestPaths();
    }

    public List<Route> rank(List<Origin> origins, int limit)
    {
        if (origins == null || limit <= 0) return Collections.emptyList();
        List<Route> candidates = new ArrayList<>();
        for (Origin origin : origins)
        {
            if (origin == null || origin.location == null) continue;
            int shortest = INFINITY;
            int via = -1;
            int distance = canWalkToDestinationFrom(origin.location)
                ? walkingLowerBound(origin.location, target) : INFINITY;
            if (distance <= FINAL_WALK_LIMIT) shortest = distance;

            for (int i = 0; i < nodes.size(); i++)
            {
                int walk = walkingLowerBound(origin.location, nodes.get(i).location);
                if (nodes.get(i).isolated && walk > 12) continue;
                if (walk > APPROACH_LIMIT || remainingCost[i] == INFINITY) continue;
                int total = walk + remainingCost[i];
                if (total < shortest)
                {
                    shortest = total;
                    via = i;
                }
            }
            if (shortest == INFINITY) continue;

            List<String> steps = new ArrayList<>();
            if (origin.description != null && !origin.description.isEmpty()) steps.add(origin.description);
            if (via == -1)
            {
                if (distance > 0) steps.add(walkInstruction(distance, "destination"));
            }
            else
            {
                int approach = walkingLowerBound(origin.location, nodes.get(via).location);
                if (approach > 0) steps.add(walkInstruction(approach, nodes.get(via).name));
                int at = via;
                int guard = 0;
                while (at != destinationIndex && nextEdge[at] != null && guard++ <= nodes.size())
                {
                    Edge leg = nextEdge[at];
                    steps.add(leg.action);
                    at = leg.to;
                }
                if (at != destinationIndex) continue;
            }
            candidates.add(new Route(origin.actionCost + shortest, steps));
        }
        candidates.sort(Comparator.comparingInt(r -> r.estimatedCost));
        if (candidates.size() > limit) candidates.subList(limit, candidates.size()).clear();
        return candidates;
    }

    private void addEdges()
    {
        for (int i = 0; i < nodes.size(); i++)
        {
            Node origin = nodes.get(i);
            int finalWalk = walkingLowerBound(origin.location, target);
            if (origin.canFinishByWalking && canWalkToDestinationFrom(origin.location)
                && finalWalk <= FINAL_WALK_LIMIT)
                add(i, destinationIndex, finalWalk, walkInstruction(finalWalk, "destination"));

            for (int j = 0; j < nodes.size(); j++)
            {
                if (i == j) continue;
                Node destination = nodes.get(j);
                if (origin.network.equals(destination.network) && origin.canDepart
                    && ("fairy".equals(origin.network) || "spirit".equals(origin.network)
                        || "quetzal".equals(origin.network)))
                    add(i, j, TRANSPORT_COST,
                        ("fairy".equals(origin.network) ? "Dial "
                            : "quetzal".equals(origin.network) ? "Fly by quetzal to " : "Travel by ")
                            + destination.name + " from " + origin.name);

                int walk = walkingLowerBound(origin.location, destination.location);
                if (!origin.isolated && !destination.isolated && walk > 0 && walk <= TRANSFER_LIMIT)
                    add(i, j, walk, walkInstruction(walk, destination.name));
            }
        }
        add(jarvaldDockIndex, waterbirthLandingIndex, 15,
            "Take Jarvald's boat from Rellekka to Waterbirth Island (1,000 coins if The Fremennik Trials is incomplete; free after completion)");
        add(waterbirthLandingIndex, jarvaldDockIndex, 15,
            "Take Jarvald's return boat from Waterbirth Island to Rellekka");
    }

    private void add(int from, int to, int cost, String action)
    {
        incoming.get(to).add(new Edge(from, to, cost, action));
    }

    private void calculateReverseShortestPaths()
    {
        Arrays.fill(remainingCost, INFINITY);
        remainingCost[destinationIndex] = 0;
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>(Comparator.comparingInt(e -> e.cost));
        queue.add(new QueueEntry(destinationIndex, 0));
        while (!queue.isEmpty())
        {
            QueueEntry current = queue.poll();
            if (current.cost != remainingCost[current.node]) continue;
            for (Edge incomingEdge : incoming.get(current.node))
            {
                int alternative = current.cost + incomingEdge.cost;
                if (alternative < remainingCost[incomingEdge.from])
                {
                    remainingCost[incomingEdge.from] = alternative;
                    nextEdge[incomingEdge.from] = incomingEdge;
                    queue.add(new QueueEntry(incomingEdge.from, alternative));
                }
            }
        }
    }

    private static String walkInstruction(int tiles, String destination)
    {
        return tiles == 0 ? "At " + destination
            : "Walk toward " + destination + " (~" + tiles
                + " tiles minimum; global obstacles and access must be checked)";
    }

    private boolean canWalkToDestinationFrom(WorldPoint origin)
    {
        // Geometric closeness alone cannot bridge these named water/quest boundaries.
        String name = destinationName == null ? "" : destinationName.toLowerCase(java.util.Locale.ROOT);
        int x = origin.getX();
        int y = origin.getY();
        boolean fromWaterbirth = x >= 2470 && x <= 2600 && y >= 3690 && y <= 3830;
        boolean toWaterbirth = target.getX() >= 2470 && target.getX() <= 2600
            && target.getY() >= 3690 && target.getY() <= 3830;
        if (fromWaterbirth != toWaterbirth) return false;
        if ((name.equals("zul-andra") || name.equals("zulrah"))
            && (x > 2280 || x < 2050 || y < 2980 || y > 3220)) return false;
        if (name.contains("waterbirth island")
            && (x < 2470 || x > 2600 || y < 3690 || y > 3830)) return false;
        if (name.equals("aldarin")
            && (x < 1270 || x > 1460 || y < 2860 || y > 3030)) return false;
        if (name.contains("harmony island")
            && (x < 3710 || x > 3880 || y < 2770 || y > 2980)) return false;
        if ((name.contains("miscellania") || name.contains("etceteria"))
            && (x < 2390 || x > 2660 || y < 3800 || y > 3970)) return false;
        for (FairyRingCatalog.Ring ring : FairyRingCatalog.ALL)
        {
            if (ring.location != null && FairyRingCatalog.isIsolated(ring)
                && walkingLowerBound(origin, ring.location) <= 12
                && !FairyRingCatalog.canWalkToDestination(ring, destinationName)) return false;
        }
        return true;
    }

    private static int walkingLowerBound(WorldPoint from, WorldPoint to)
    {
        if (from == null || to == null || from.getPlane() != to.getPlane()
            || from.getY() / 4096 != to.getY() / 4096) return INFINITY;
        return Math.max(Math.abs(from.getX() - to.getX()),
            Math.abs(from.getY() - to.getY()));
    }
}
