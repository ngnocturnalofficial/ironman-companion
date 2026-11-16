package com.nocturnal.ironman.data;

import com.nocturnal.ironman.model.AccountSnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.coords.WorldPoint;

/** Fairy-ring landing points aligned with RuneLite's world-map transport data. */
public final class FairyRingCatalog
{
    private FairyRingCatalog() {}

    public static final class Ring
    {
        public final String code;
        public final WorldPoint location;

        private Ring(String code, int x, int y)
        {
            this.code = code;
            this.location = new WorldPoint(x, y, 0);
        }

        private Ring(String code)
        {
            this.code = code;
            this.location = null;
        }
    }

    public static final List<Ring> ALL = Collections.unmodifiableList(Arrays.asList(
        new Ring("AIQ", 2995, 3112),
        new Ring("AIR", 2699, 3249),
        new Ring("AIS", 1430, 3323),
        new Ring("AJP", 1648, 3011),
        new Ring("AJQ"), // Dorgesh-Kaan cave: not present in the world-map point data.
        new Ring("AJR", 2779, 3615),
        new Ring("AJS", 2499, 3898),
        new Ring("AKP", 3283, 2704),
        new Ring("AKQ", 2318, 3617),
        new Ring("AKR", 1823, 3539),
        new Ring("AKS", 2570, 2958),
        new Ring("ALP", 2502, 3638),
        new Ring("ALQ", 3598, 3496),
        new Ring("ALR", 3059, 4877),
        new Ring("ALS", 2643, 3497),
        new Ring("BIP", 3409, 3326),
        new Ring("BIQ", 3248, 3095),
        new Ring("BIS", 2635, 3268),
        new Ring("BJP", 2264, 2976),
        new Ring("BJR"), // Realm of the Fisher King.
        new Ring("BJS", 2147, 3069),
        new Ring("BKP", 2384, 3037),
        new Ring("BKQ"), // Enchanted Valley.
        new Ring("BKR", 3468, 3433),
        new Ring("BKS", 2411, 4436),
        new Ring("BLP", 2432, 5127),
        new Ring("BLQ"), // Yu'biusk.
        new Ring("BLR", 2739, 3353),
        new Ring("BLS", 1293, 3495),
        new Ring("CIP", 2512, 3886),
        new Ring("CIQ", 2527, 3129),
        new Ring("CIR", 1303, 3762),
        new Ring("CIS", 1636, 3869),
        new Ring("CJQ", 3178, 2445),
        new Ring("CJR", 2704, 3578),
        new Ring("CKP"), // Cosmic entity's plane.
        new Ring("CKQ", 1358, 2943),
        new Ring("CKR", 2800, 3005),
        new Ring("CKS", 3446, 3472),
        new Ring("CLP", 3081, 3208),
        new Ring("CLR", 2737, 2739),
        new Ring("CLS", 2681, 3083),
        new Ring("DIP", 3036, 4761),
        new Ring("DIQ"), // Player-owned house: construction state and position vary.
        new Ring("DIR"), // Gorak's Plane.
        new Ring("DIS", 3109, 3149),
        new Ring("DJP", 2657, 3232),
        new Ring("DJR", 1452, 3659),
        new Ring("DKP", 2899, 3113),
        new Ring("DKR", 3126, 3496),
        new Ring("DKS", 2743, 3721),
        new Ring("DLP", 2923, 10455),
        new Ring("DLQ", 3422, 3018),
        new Ring("DLR", 2212, 3101),
        new Ring("DLS") // Myreque hideout, absent from the world-map point data.
    ));

    public static Ring nearest(WorldPoint point)
    {
        if (point == null) return null;
        return ALL.stream().filter(ring -> ring.location != null).min((a, b) -> Integer.compare(
            point.distanceTo2D(a.location), point.distanceTo2D(b.location))).orElse(null);
    }

    public static boolean isDestinationUnlocked(AccountSnapshot account, Ring ring)
    {
        if (account == null || ring == null) return false;
        String code = ring.code;
        if ("AJQ".equals(code) && !completed(account, "Death to the Dorgeshuun")) return false;
        // Partial quest progress can unlock these realms earlier; only completed quests are conclusive.
        if ("BJR".equals(code) && !completed(account, "Holy Grail")) return false;
        if ("BLQ".equals(code) && !completed(account, "Land of the Goblins")) return false;
        if ("DLS".equals(code) && !completed(account, "In Search of the Myreque")) return false;
        if ("DLP".equals(code) && account.getTravelSetting("visitedGrimstone") <= 0) return false;
        if ("DIQ".equals(code) || "CKP".equals(code)) return false; // Built POH / quest realm state unknown.
        if ("BJS".equals(code) && !completed(account, "Regicide")) return false;
        if ("BJS".equals(code) && account.getRealLevels().getOrDefault("Agility", 1) < 76) return false;
        if ("CLR".equals(code) && !completed(account, "Monkey Madness I")) return false;
        if ("CJQ".equals(code)) return false; // Arrival at The Great Conch is not captured.
        if ("CIP".equals(code) && !completed(account, "The Fremennik Trials")) return false;
        if (("AJP".equals(code) || "AIS".equals(code) || "CKQ".equals(code))
            && (!completed(account, "Children of the Sun")
                || account.getTravelSetting("visitedVarlamore") <= 0)) return false;
        if (("AKR".equals(code) || "CIR".equals(code) || "CIS".equals(code))
            && account.getTravelSetting("visitedKourend") <= 0) return false;
        return true;
    }

    public static Ring nearestUnlocked(AccountSnapshot account, WorldPoint point)
    {
        if (point == null || account == null) return null;
        return ALL.stream().filter(ring -> ring.location != null && isDestinationUnlocked(account, ring))
            .min((a, b) -> Integer.compare(point.distanceTo2D(a.location),
                point.distanceTo2D(b.location))).orElse(null);
    }

    /** Separate island/realm destinations cannot be treated as walks to the mainland. */
    public static boolean canWalkToDestination(Ring ring, String destinationName)
    {
        if (ring == null || ring.location == null || destinationName == null) return false;
        String target = destinationName.toLowerCase(java.util.Locale.ROOT);
        // Other mainland rings can be geographically close yet separated by the Poison Waste or sea.
        if (target.equals("zul-andra") || target.equals("zulrah")) return "BJS".equals(ring.code);
        if (target.contains("miscellania") || target.contains("etceteria")) return "CIP".equals(ring.code);
        if (target.equals("aldarin")) return "CKQ".equals(ring.code);
        if (target.contains("harmony island")) return false;
        switch (ring.code)
        {
            case "AIR": return target.contains("ardougne island");
            case "AJS": return target.contains("penguin island");
            case "BIP": return target.contains("river salve island");
            case "BIS": return target.contains("ardougne zoo");
            case "BJP": return target.contains("isle of souls");
            case "BJS": return target.contains("zul-andra") || target.contains("zulrah");
            case "BKS": return target.contains("zanaris");
            case "ALR": return target.contains("abyssal area") || target.equals("abyss");
            case "BLP": return target.contains("tzhaar") || target.contains("mor ul rek") || target.contains("fight cave");
            case "DIP": return target.contains("abyssal sire") || target.contains("abyssal nexus");
            case "CIP": return target.contains("miscellania") || target.contains("etceteria");
            case "CJQ": return target.contains("great conch");
            case "CKQ": return target.contains("aldarin");
            case "CLP": return target.contains("draynor island");
            case "CLR": return target.contains("ape atoll") || target.contains("ape atoll agility");
            case "CLS": return target.contains("hazelmere");
            case "DLP": return target.contains("grimstone");
            case "DLR": return target.contains("poison waste island") || target.contains("spectate zulrah");
            default: return true;
        }
    }

    /** A nearby map tile does not imply walkable access to a separate island or realm. */
    public static boolean isIsolated(Ring ring)
    {
        if (ring == null) return false;
        switch (ring.code)
        {
            case "AIR": case "AJS": case "ALR": case "BIP": case "BIS":
            case "BJP": case "BJS": case "BKS": case "BLP": case "CIP":
            case "CJQ": case "CKQ": case "CLP": case "CLR": case "CLS":
            case "DIP": case "DLP": case "DLR": return true;
            default: return false;
        }
    }

    public static Ring nearestUnlockedLanding(AccountSnapshot account, WorldPoint point, String destinationName)
    {
        if (point == null || account == null) return null;
        return ALL.stream().filter(ring -> ring.location != null && isDestinationUnlocked(account, ring)
                && canWalkToDestination(ring, destinationName))
            .min((a, b) -> Integer.compare(point.distanceTo2D(a.location),
                point.distanceTo2D(b.location))).orElse(null);
    }

    private static boolean completed(AccountSnapshot account, String name)
    {
        return "FINISHED".equals(account.getProgress().getQuests().get(name));
    }

    public static List<Ring> nearest(WorldPoint point, int limit)
    {
        if (point == null || limit <= 0) return Collections.emptyList();
        java.util.ArrayList<Ring> result = new java.util.ArrayList<>();
        for (Ring ring : ALL) if (ring.location != null) result.add(ring);
        result.sort((a, b) -> Integer.compare(point.distanceTo2D(a.location), point.distanceTo2D(b.location)));
        if (result.size() > limit) result.subList(limit, result.size()).clear();
        return Collections.unmodifiableList(result);
    }
}
