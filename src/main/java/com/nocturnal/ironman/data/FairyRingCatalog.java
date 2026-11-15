package com.nocturnal.ironman.data;

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
    }

    public static final List<Ring> ALL = Collections.unmodifiableList(Arrays.asList(
        new Ring("AIQ", 2995, 3112),
        new Ring("AIR", 2699, 3249),
        new Ring("AIS", 1430, 3323),
        new Ring("AJP", 1648, 3011),
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
        new Ring("BJS", 2147, 3069),
        new Ring("BKP", 2384, 3037),
        new Ring("BKR", 3468, 3433),
        new Ring("BKS", 2411, 4436),
        new Ring("BLP", 2432, 5127),
        new Ring("BLR", 2739, 3353),
        new Ring("BLS", 1293, 3495),
        new Ring("CIP", 2512, 3886),
        new Ring("CIQ", 2527, 3129),
        new Ring("CIR", 1303, 3762),
        new Ring("CIS", 1636, 3869),
        new Ring("CJQ", 3178, 2445),
        new Ring("CJR", 2704, 3578),
        new Ring("CKQ", 1358, 2943),
        new Ring("CKR", 2800, 3005),
        new Ring("CKS", 3446, 3472),
        new Ring("CLP", 3081, 3208),
        new Ring("CLR", 2737, 2739),
        new Ring("CLS", 2681, 3083),
        new Ring("DIP", 3036, 4761),
        new Ring("DIS", 3109, 3149),
        new Ring("DJP", 2657, 3232),
        new Ring("DJR", 1452, 3659),
        new Ring("DKP", 2899, 3113),
        new Ring("DKR", 3126, 3496),
        new Ring("DKS", 2743, 3721),
        new Ring("DLP", 2923, 10455),
        new Ring("DLQ", 3422, 3018),
        new Ring("DLR", 2212, 3101)
    ));

    public static Ring nearest(WorldPoint point)
    {
        if (point == null) return null;
        return ALL.stream().min((a, b) -> Integer.compare(
            point.distanceTo2D(a.location), point.distanceTo2D(b.location))).orElse(null);
    }

    public static List<Ring> nearest(WorldPoint point, int limit)
    {
        if (point == null || limit <= 0) return Collections.emptyList();
        java.util.ArrayList<Ring> result = new java.util.ArrayList<>(ALL);
        result.sort((a, b) -> Integer.compare(point.distanceTo2D(a.location), point.distanceTo2D(b.location)));
        if (result.size() > limit) result.subList(limit, result.size()).clear();
        return Collections.unmodifiableList(result);
    }
}
