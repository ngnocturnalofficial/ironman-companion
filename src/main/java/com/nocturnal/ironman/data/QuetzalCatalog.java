package com.nocturnal.ironman.data;

import com.nocturnal.ironman.model.AccountSnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.coords.WorldPoint;

/** Quetzal destinations with observed construction flags and verified/approximate map anchors. */
public final class QuetzalCatalog
{
    private QuetzalCatalog() {}

    public static final class Landing
    {
        public final String name;
        public final WorldPoint location;
        private final String constructionSetting;

        private Landing(String name, int x, int y, String constructionSetting)
        {
            this.name = name;
            location = x > 0 && y > 0 ? new WorldPoint(x, y, 0) : null;
            this.constructionSetting = constructionSetting;
        }

        public boolean isAvailable(AccountSnapshot account)
        {
            if (account == null || account.getTravelSetting("visitedVarlamore") <= 0) return false;
            return constructionSetting == null || account.getTravelSetting(constructionSetting) > 0;
        }
    }

    // The first eight landings are built by default. Extra sites require construction.
    // Unknown exact landing coordinates are deliberately excluded from spatial routes.
    public static final List<Landing> ALL = Collections.unmodifiableList(Arrays.asList(
        new Landing("Aldarin", 0, 0, null),
        new Landing("Auburnvale", 1411, 3361, null),
        new Landing("Civitas illa Fortis", 0, 0, null),
        new Landing("Hunter Guild", 1585, 3053, null),
        new Landing("Quetzacalli Gorge", 1510, 3222, null),
        new Landing("Sunset Coast", 1548, 2995, null),
        new Landing("Tal Teklan", 0, 0, null),
        new Landing("The Teomat", 0, 0, null),
        new Landing("Cam Torum", 0, 0, "quetzalCamTorum"),
        new Landing("Colossal Wyrm Remains", 0, 0, "quetzalColossalWyrm"),
        new Landing("Fortis Colosseum", 0, 0, "quetzalColosseum"),
        new Landing("Kastori", 0, 0, "quetzalKastori"),
        new Landing("Outer Fortis", 0, 0, "quetzalOuterFortis"),
        new Landing("Salvager Overlook", 0, 0, "quetzalSalvagerOverlook")
    ));

    public static boolean isDestinationAvailable(AccountSnapshot account, String name)
    {
        for (Landing landing : ALL)
            if (landing.name.equals(name)) return landing.isAvailable(account);
        return false;
    }

    public static boolean hasNetworkAccess(AccountSnapshot account)
    {
        return account != null && account.getTravelSetting("visitedVarlamore") > 0
            && account.getTravelSetting("quetzalNetwork") > 0;
    }
}
