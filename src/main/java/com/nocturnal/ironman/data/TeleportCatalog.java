package com.nocturnal.ironman.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class TeleportCatalog
{
    private TeleportCatalog() {}

    public static final class Teleport
    {
        public final String name;
        public final String destination;
        public final String notes;
        public final int[] itemIds;
        public final String[] nameMatches;
        public final int x;
        public final int y;
        public final int plane;

        public Teleport(String name, String destination, String notes, int x, int y, int... itemIds)
        {
            this(name, destination, notes, x, y, 0, itemIds, new String[0]);
        }

        public Teleport(String name, String destination, String notes, int x, int y, int[] itemIds, String... nameMatches)
        {
            this(name, destination, notes, x, y, 0, itemIds, nameMatches);
        }

        public Teleport(String name, String destination, String notes, int x, int y,
            int plane, int[] itemIds, String... nameMatches)
        {
            this.name = name;
            this.destination = destination;
            this.notes = notes;
            this.x = x;
            this.y = y;
            this.plane = plane;
            this.itemIds = itemIds == null ? new int[0] : itemIds.clone();
            this.nameMatches = nameMatches == null ? new String[0] : nameMatches.clone();
        }

        public boolean matchesItemId(int id)
        {
            for (int itemId : itemIds) if (itemId == id) return true;
            return false;
        }

        public boolean matchesItemName(String itemName)
        {
            if (itemName == null) return false;
            String normalized = itemName.toLowerCase(Locale.ROOT);
            if (isUnchargedItemName(normalized)) return false;
            for (String match : nameMatches)
                if (match != null && normalized.contains(match.toLowerCase(Locale.ROOT))) return true;
            return false;
        }
    }

    public static boolean isUnchargedItemName(String itemName)
    {
        if (itemName == null) return false;
        String normalized = itemName.toLowerCase(Locale.ROOT);
        return normalized.contains("(0)") || normalized.contains("uncharged") || normalized.contains("depleted");
    }

    private static Teleport named(String name, String destination, String notes, int x, int y, String... matches)
    {
        return new Teleport(name, destination, notes, x, y, new int[0], matches);
    }

    private static Teleport named(String name, String destination, String notes, int x, int y,
        int plane, String... matches)
    {
        return new Teleport(name, destination, notes, x, y, plane, new int[0], matches);
    }

    public static final List<Teleport> ALL = Collections.unmodifiableList(Arrays.asList(
        new Teleport("Varrock teleport", "Varrock", "Standard spell or tablet", 3213, 3424, 8007),
        named("Varrock teleport — Grand Exchange landing", "Grand Exchange", "Varrock Hard Diary changes the Varrock teleport landing point", 3164, 3478, "varrock teleport"),
        new Teleport("Lumbridge teleport", "Lumbridge", "Standard spell or tablet", 3222, 3218, 8008),
        new Teleport("Falador teleport", "Falador", "Standard spell or tablet", 2965, 3381, 8009),
        new Teleport("Camelot teleport", "Camelot", "Standard spell or tablet", 2757, 3477, 8010),
        named("Camelot teleport — Seers' bank landing", "Seers' Village bank", "Kandarin Hard Diary changes the Camelot teleport landing point", 2726, 3485, "camelot teleport"),
        new Teleport("Ardougne teleport", "Ardougne", "Standard spell or tablet; spell unlock required for spell", 2664, 3306, 8011),
        new Teleport("Watchtower teleport", "Watchtower", "Quest and spell requirements apply; Ardougne Hard Diary changes the landing point to Yanille", 2547, 3114, 8012),
        named("Watchtower teleport — Yanille landing", "Yanille", "Ardougne Hard Diary changes the Watchtower teleport landing point", 2584, 3097, "watchtower teleport"),
        new Teleport("House teleport", "Player-owned house", "Destination depends on house portal/redirect", 0, 0, 8013),
        new Teleport("Trollheim teleport", "Trollheim", "Spell/tablet access and Eadgar's Ruse requirements apply", 2891, 3678, new int[0], "trollheim teleport"),
        new Teleport("Barrows teleport", "Barrows", "Tablet or spellbook access; confirm the landing point and route", 3565, 3315, new int[0], "barrows teleport"),
        new Teleport("Kharyrll teleport", "Canifis", "Ancient Magicks; Desert Treasure I", 3494, 3473, new int[0], "kharyrll teleport"),
        new Teleport("Kourend Castle teleport", "Kourend Castle", "Tablet or spell; Client of Kourend access", 1643, 3672, new int[0], "kourend castle teleport"),
        new Teleport("Nardah teleport", "Nardah", "Tablet or spell; Desert access", 3425, 2928, new int[0], "nardah teleport"),
        new Teleport("Salve Graveyard teleport", "Salve Graveyard", "Tablet or spell; Morytania access", 3433, 3461, new int[0], "salve graveyard teleport"),
        new Teleport("Fenkenstrain's Castle teleport", "Fenkenstrain's Castle", "Tablet or spell; Creature of Fenkenstrain access", 3548, 3528, new int[0], "fenkenstrain"),
        new Teleport("Games necklace", "Burthorpe Games Room", "Charged variants; also offers Barbarian Assault, Tears of Guthix, Corporeal Beast and Wintertodt", 2898, 3554,
            new int[]{3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867}, "games necklace"),
        new Teleport("Ring of dueling", "Castle Wars", "Charged variants; also offers Ferox Enclave and PvP arena destinations", 2441, 3091,
            new int[]{2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566}, "ring of dueling"),
        new Teleport("Amulet of glory", "Edgeville", "Charged variants; also offers Karamja, Draynor and Al Kharid", 3087, 3496,
            new int[]{1706, 1708, 1710, 1712, 11978, 11980}, "amulet of glory"),
        new Teleport("Combat bracelet", "Warriors' Guild", "Charged variants; also offers Champions' Guild, Monastery and Ranging Guild", 2883, 3549,
            new int[]{11118, 11120, 11122, 11124}, "combat bracelet"),
        new Teleport("Skills necklace", "Fishing Guild", "Charged variants; also offers Mining, Crafting, Cooking, Woodcutting and Farming Guilds", 2613, 3390,
            new int[]{11105, 11107, 11109, 11111, 11113, 11115}, "skills necklace"),
        named("Skills necklace (Farming Guild option)", "Farming Guild", "Choose the Farming Guild destination from the necklace menu; 65 Farming for the herb patch", 1249, 3717, "skills necklace"),
        named("Farming cape", "Farming Guild", "Teleport to the Farming Guild; requires 99 Farming", 1248, 3725, "farming cape", "max cape"),
        new Teleport("Slayer ring", "Stronghold Slayer Cave", "Charged variants; also offers Slayer Tower, Fremennik Slayer Dungeon and Tarn's Lair", 2433, 3421,
            new int[]{11866, 11868, 11870, 11872, 11874, 11876}, "slayer ring"),
        named("Necklace of passage", "Wizard Tower", "Charged variants; also offers the Outpost and Eagles' Eyrie", 3114, 3181, "necklace of passage"),
        named("Ring of wealth", "Grand Exchange", "Charged variants; also offers Miscellania and the Fountain of Rune", 3162, 3480, "ring of wealth"),
        named("Burning amulet", "Bandit Camp", "Charged variants; also offers Lava Maze and Chaos Temple", 3038, 3651, "burning amulet"),
        named("Xeric's talisman", "Xeric's Glade", "Charged variants; offers several Great Kourend destinations", 1754, 3564, "xeric's talisman", "xeric talisman"),
        named("Ectophial", "Ectofuntus", "One-click teleport; refill at the Ectofuntus", 3660, 3522, "ectophial"),
        named("Drakan's medallion", "Morytania transport network", "Multi-destination teleport between Meiyerditch, Darkmeyer and Ver Sinhaza; landing point depends on selection", 0, 0, "drakan's medallion"),
        named("Royal seed pod", "Gnome Stronghold", "One-click teleport; quest reward", 2465, 3495, "royal seed pod"),
        named("Crystal teleport seed", "Lletya", "Charged variants; can teleport to Lletya and Prifddinas", 2330, 3172, "crystal teleport seed"),
        named("Icy basalt", "Weiss", "Unlimited teleport; Making Friends with My Arm access", 2846, 3940, "icy basalt"),
        named("Stony basalt", "Troll Stronghold", "Unlimited teleport; Making Friends with My Arm access", 2844, 3693, "stony basalt"),
        named("Stony basalt — upper landing", "Troll Stronghold upper landing", "Alternate landing; requires 73 Agility", 2838, 3693, "stony basalt"),
        named("Enchanted lyre", "Rellekka", "Charged variants; Fremennik quest upgrades add destinations", 2664, 3643, "enchanted lyre", "lyre(i)"),
        named("Enchanted lyre — Waterbirth Island", "Waterbirth Island", "Select this destination from the Enchanted lyre", 2550, 3756, "enchanted lyre", "lyre(i)"),
        named("Enchanted lyre — Neitiznot", "Neitiznot", "Select this destination from the Enchanted lyre", 2336, 3801, "enchanted lyre", "lyre(i)"),
        named("Enchanted lyre — Jatizso", "Jatizso", "Select this destination from the Enchanted lyre", 2409, 3809, "enchanted lyre", "lyre(i)"),
        named("Pharaoh's sceptre", "Jalsavrah", "Charges and unlocked pyramid destinations apply", 3288, 2795, "pharaoh's sceptre"),
        named("Digsite pendant", "Digsite", "Charged variants; can also reach Fossil Island and Lithkren after unlocks", 3339, 3445, "digsite pendant"),
        named("Digsite pendant — House on the Hill", "House on the Hill", "Select this destination from the Digsite pendant", 3763, 3869, "digsite pendant"),
        named("Digsite pendant — Lithkren", "Lithkren", "Select this destination from the Digsite pendant", 3547, 10456, "digsite pendant"),
        named("Ardougne cloak", "Ardougne Monastery", "Diary reward; teleport destinations depend on tier", 2606, 3222, "ardougne cloak"),
        named("Explorer's ring", "Cabbage patch", "Diary reward; teleport count depends on tier", 3052, 3292, "explorer's ring"),
        named("Karamja gloves", "Gem Mine", "Diary reward; tier determines teleport access", 2827, 2997, "karamja gloves"),
        named("Fremennik sea boots", "Rellekka", "Diary reward; teleport destinations depend on tier", 2640, 3675, "fremennik sea boots"),
        named("Rada's blessing", "Mount Karuulm", "Diary reward; tier determines teleport uses", 1311, 3795, "rada's blessing"),
        named("Ghommal's hilt", "God Wars Dungeon", "Combat achievement reward; teleport access depends on hilt tier", 2898, 3709, "ghommal's hilt"),
        named("Desert amulet", "Nardah", "Diary reward; teleport uses and access depend on tier", 3425, 2928, "desert amulet"),
        named("Wilderness sword", "Wilderness obelisk", "Diary reward; teleport destinations depend on tier", 3377, 3891, "wilderness sword"),
        named("Pendant of Ates", "Varlamore statue network", "Multi-destination teleport; select a statue manually. Landing point varies by selection.", 0, 0, "pendant of ates"),
        named("Quetzal whistle", "Hunter Guild", "Quetzal Transport System; Twilight's Promise and a built Hunter Guild landing site are required. Run north to Ortus Farm.", 1585, 3053, "quetzal whistle"),
        named("Quetzal whistle — Sunset Coast", "Sunset Coast", "Quetzal landing site; built-site state is not detected", 1548, 2995, "quetzal whistle"),
        named("Quetzal whistle — Auburnvale", "Auburnvale", "Quetzal landing site; built-site state is not detected", 1411, 3361, "quetzal whistle"),
        named("Quetzal whistle — Quetzacalli Gorge", "Quetzacalli Gorge", "Quetzal landing site; built-site state is not detected", 1510, 3222, "quetzal whistle"),
        named("Quetzal whistle — Aldarin", "Aldarin", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — Civitas illa Fortis", "Civitas illa Fortis", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — Tal Teklan", "Tal Teklan", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — The Teomat", "The Teomat", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — Cam Torum", "Cam Torum", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — Colossal Wyrm Remains", "Colossal Wyrm Remains", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — Fortis Colosseum", "Fortis Colosseum", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — Kastori", "Kastori", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — Outer Fortis", "Outer Fortis", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Quetzal whistle — Salvager Overlook", "Salvager Overlook", "Quetzal landing site; built-site state and exact landing tile are not detected", 0, 0, "quetzal whistle"),
        named("Hunter cape", "Hunter Guild", "Teleport unlock must be obtained from Guildmaster Apatura; run north to Ortus Farm.", 1558, 3046, "hunter cape", "max cape"),
        new Teleport("Civitas illa Fortis teleport", "Civitas illa Fortis", "Tablet (item 28824) or spell; Twilight's Promise required. Take the Quetzal Transport System to Hunter Guild for Ortus Farm.", 1681, 3133, 28824),
        named("Zul-andra teleport scroll", "Zul-Andra", "Direct access to the Zul-Andra dock; Regicide is still required", 2197, 3056, "zul-andra teleport scroll", "zul andra teleport scroll"),
        named("Ring of shadows", "Lassar Undercity", "Requires the Sirenic tablet unlock; ring charge and destination unlock state are not detected", 2588, 6435, "ring of shadows"),
        named("Ring of shadows — Ghorrock Dungeon", "Ghorrock Dungeon", "Requires the Frozen tablet unlock on Ring of shadows; unlock state and charges are not detected", 2977, 3872, "ring of shadows"),
        named("Ring of shadows — The Scar", "The Scar", "Requires the Scarred tablet unlock on Ring of shadows; instanced landing is not mapped by the local route planner", 0, 0, "ring of shadows"),
        named("Ring of shadows — The Stranglewood", "The Stranglewood", "Requires the Strangled tablet unlock; ring charge and destination unlock state are not detected", 1175, 3422, "ring of shadows"),
        named("Tokkul-Zo", "TzHaar Fight Cave", "Direct teleport to the Fight Cave area; unlock via the TzHaar city", 2445, 5170, "tokkul-zo"),

        named("Games necklace — Barbarian Assault", "Barbarian Assault", "Select this destination from a charged Games necklace", 2520, 3571, "games necklace"),
        named("Games necklace — Tears of Guthix", "Tears of Guthix", "Select this destination from a charged Games necklace", 3245, 9500, "games necklace"),
        named("Games necklace — Corporeal Beast", "Corporeal Beast", "Select this destination from a charged Games necklace", 2967, 4384, "games necklace"),
        named("Games necklace — Wintertodt", "Wintertodt Camp", "Select this destination from a charged Games necklace", 1624, 3938, "games necklace"),
        named("Ring of dueling — Emir's Arena", "Emir's Arena", "Select this destination from a charged Ring of dueling", 3315, 3235, "ring of dueling"),
        named("Ring of dueling — Ferox Enclave", "Ferox Enclave", "Select this destination from a charged Ring of dueling", 3151, 3636, "ring of dueling"),
        named("Ring of dueling — Fortis Colosseum", "Fortis Colosseum", "Select this destination from a charged Ring of dueling", 1793, 3107, "ring of dueling"),
        named("Combat bracelet — Champions' Guild", "Champions' Guild", "Select this destination from a charged Combat bracelet", 3189, 3368, "combat bracelet"),
        named("Combat bracelet — Edgeville Monastery", "Edgeville Monastery", "Select this destination from a charged Combat bracelet", 3053, 3487, "combat bracelet"),
        named("Combat bracelet — Ranging Guild", "Ranging Guild", "Select this destination from a charged Combat bracelet", 2654, 3441, "combat bracelet"),
        named("Skills necklace — Mining Guild", "Mining Guild", "Select this destination from a charged Skills necklace", 3049, 9762, "skills necklace"),
        named("Skills necklace — Crafting Guild", "Crafting Guild", "Select this destination from a charged Skills necklace", 2934, 3294, "skills necklace"),
        named("Skills necklace — Cooking Guild", "Cooking Guild", "Select this destination from a charged Skills necklace", 3145, 3438, "skills necklace"),
        named("Skills necklace — Woodcutting Guild", "Woodcutting Guild", "Select this destination from a charged Skills necklace", 1662, 3505, "skills necklace"),
        named("Amulet of glory — Karamja", "Karamja", "Select this destination from a charged Amulet of glory", 2918, 3176, "amulet of glory"),
        named("Amulet of glory — Draynor", "Draynor Village", "Select this destination from a charged Amulet of glory", 3105, 3251, "amulet of glory"),
        named("Amulet of glory — Al Kharid", "Al-Kharid", "Select this destination from a charged Amulet of glory", 3293, 3163, "amulet of glory"),
        named("Ring of wealth — Miscellania", "Miscellania", "Select this destination from a charged Ring of wealth", 2535, 3862, "ring of wealth"),
        named("Ring of wealth — Falador Park", "Falador Park", "Select this destination from a charged Ring of wealth", 2995, 3375, "ring of wealth"),
        named("Ring of wealth — Dondakan", "Dondakan", "Select this destination from a charged Ring of wealth", 2831, 10165, "ring of wealth"),
        named("Slayer ring — Slayer Tower", "Slayer Tower", "Select this destination from a charged Slayer ring", 3423, 3536, "slayer ring"),
        named("Slayer ring — Fremennik Slayer Dungeon", "Fremennik Slayer Dungeon", "Select this destination from a charged Slayer ring", 2800, 9998, "slayer ring"),
        named("Slayer ring — Fremennik Slayer Dungeon (inside)", "Fremennik Slayer Dungeon (inside)", "Select this destination from a charged Slayer ring", 2800, 3615, "slayer ring"),
        named("Slayer ring — Tarn's Lair", "Tarn's Lair", "Select this destination from a charged Slayer ring", 3187, 4601, "slayer ring"),
        named("Slayer ring — Dark Beasts", "Dark Beasts", "Select this destination from a charged Slayer ring", 2028, 4638, "slayer ring"),
        named("Slayer ring — Wyrmscraig Cavern", "Wyrmscraig Cavern", "Select this destination from a charged Slayer ring", 2773, 8633, "slayer ring"),
        named("Slayer ring — Wyrmscraig Cavern (inside)", "Wyrmscraig Cavern", "Select this destination from a charged Slayer ring", 2533, 2206, "slayer ring"),
        named("Necklace of passage — Jorral", "Jorral's Outpost", "Select this destination from a charged Necklace of passage", 2431, 3348, "necklace of passage"),
        named("Necklace of passage — Eagle station", "Desert eagle station", "Select this destination from a charged Necklace of passage", 3406, 3157, "necklace of passage"),
        named("Necklace of passage — Wyrmscraig", "Wyrmscraig", "Select this destination from a charged Necklace of passage", 2591, 2221, "necklace of passage"),
        named("Burning amulet — Chaos Temple", "Chaos Temple", "Select this destination from a charged Burning amulet", 3234, 3637, "burning amulet"),
        named("Burning amulet — Lava Maze", "Lava Maze", "Select this destination from a charged Burning amulet", 3028, 3840, "burning amulet"),
        named("Xeric's talisman — Lookout", "Xeric's Lookout", "Select this destination from Xeric's talisman", 1576, 3528, "xeric's talisman", "xeric talisman"),
        named("Xeric's talisman — Inferno", "Xeric's Inferno", "Select this destination from Xeric's talisman", 1504, 3819, "xeric's talisman", "xeric talisman"),
        named("Xeric's talisman — Heart", "Xeric's Heart", "Select this destination from Xeric's talisman", 1641, 3670, "xeric's talisman", "xeric talisman"),
        named("Xeric's talisman — Honour", "Xeric's Honour", "Select this destination from Xeric's talisman", 1254, 3559, "xeric's talisman", "xeric talisman"),
        named("Crystal teleport seed — Prifddinas", "Prifddinas", "Requires the relevant crystal teleport unlock", 3264, 6065, "crystal teleport seed"),
        named("Drakan's medallion — Ver Sinhaza", "Ver Sinhaza", "Select this destination from Drakan's medallion", 3649, 3230, "drakan's medallion"),
        named("Drakan's medallion — Darkmeyer", "Darkmeyer", "Select this destination from Drakan's medallion", 3592, 3337, "drakan's medallion"),
        named("Drakan's medallion — Slepe", "Slepe", "Select this destination from Drakan's medallion", 3808, 9700, "drakan's medallion"),
        named("Drakan's medallion — Castle Drakan", "Castle Drakan", "Select this destination from Drakan's medallion", 3557, 3358, "drakan's medallion"),
        named("Pendant of Ates — North Aldarin", "North Aldarin", "Select this destination from the Pendant of Ates", 1425, 2995, "pendant of ates"),
        named("Pendant of Ates — Darkfrost", "The Darkfrost", "Select this destination from the Pendant of Ates", 1490, 3284, "pendant of ates"),
        named("Pendant of Ates — Ralos' Rise", "Ralos' Rise", "Select this destination from the Pendant of Ates", 1459, 3137, "pendant of ates"),
        named("Pendant of Ates — Twilight Temple", "Twilight Temple", "Select this destination from the Pendant of Ates", 1666, 3223, "pendant of ates"),
        named("Pendant of Ates — Kastori", "Kastori", "Select this destination from the Pendant of Ates", 1366, 3086, "pendant of ates"),
        named("Pendant of Ates — Nemus Retreat", "Nemus Retreat", "Select this destination from the Pendant of Ates", 1364, 3277, "pendant of ates"),
        named("Ring of the elements — Air Altar", "Air Altar", "Select this destination from the Ring of the elements", 2981, 3276, "ring of the elements"),
        named("Ring of the elements — Water Altar", "Water Altar", "Select this destination from the Ring of the elements", 3166, 3158, "ring of the elements"),
        named("Ring of the elements — Earth Altar", "Earth Altar", "Select this destination from the Ring of the elements", 3288, 3468, "ring of the elements"),
        named("Ring of the elements — Fire Altar", "Fire Altar", "Select this destination from the Ring of the elements", 3315, 3278, "ring of the elements"),
        named("Pharaoh's sceptre — Jaleustrophos", "Jaleustrophos", "Select this destination from Pharaoh's sceptre", 3341, 2827, "pharaoh's sceptre"),
        named("Pharaoh's sceptre — Jaldraocht", "Jaldraocht", "Select this destination from Pharaoh's sceptre", 3232, 2897, "pharaoh's sceptre"),
        named("Pharaoh's sceptre — Jaltevas", "Jaltevas", "Select this destination from Pharaoh's sceptre", 3313, 2720, "pharaoh's sceptre"),
        named("Ardougne cloak — Farming patch", "Ardougne farm", "Diary reward; teleport access depends on tier", 2673, 3375, "ardougne cloak"),
        named("Karamja gloves — Duradel", "Duradel", "Diary reward; teleport access depends on tier", 2870, 2981, "karamja gloves"),
        named("Desert amulet — Kalphite Cave", "Kalphite Cave", "Diary reward; teleport access depends on tier", 3322, 3122, "desert amulet"),
        named("Rada's blessing — Woodcutting Guild", "Woodcutting Guild", "Diary reward; teleport access depends on tier", 1553, 3454, "rada's blessing"),
        named("Ghommal's hilt — Mor Ul Rek", "Mor Ul Rek", "Combat achievement reward; hilt tier determines teleport access", 2554, 5130, "ghommal's hilt"),
        named("Digsite teleport scroll", "Digsite", "Scroll teleport", 3324, 3412, "digsite teleport"),
        named("Iorwerth Camp teleport scroll", "Iorwerth Camp", "Scroll teleport", 2193, 3257, "iorwerth camp teleport"),
        named("Feldip Hills teleport scroll", "Feldip Hills", "Scroll teleport", 2542, 2925, "feldip hills teleport"),
        named("Lumberyard teleport scroll", "Lumberyard", "Scroll teleport", 3303, 3487, "lumberyard teleport"),
        named("Lunar Isle teleport scroll", "Lunar Isle", "Scroll teleport", 2093, 3912, "lunar isle teleport"),
        named("Mort'ton teleport scroll", "Mort'ton", "Scroll teleport", 3489, 3288, "mort'ton"),
        named("Mos Le'Harmless teleport scroll", "Mos Le'Harmless", "Scroll teleport", 3701, 2996, "mos le'harmless teleport"),
        named("Nardah teleport scroll", "Nardah", "Scroll teleport", 3421, 2917, "nardah teleport"),
        named("Pest Control teleport scroll", "Pest Control", "Scroll teleport", 2657, 2660, "pest control teleport"),
        named("Piscatoris teleport scroll", "Piscatoris", "Scroll teleport", 2339, 3648, "piscatoris teleport"),
        named("Tai Bwo Wannai teleport scroll", "Tai Bwo Wannai", "Scroll teleport", 2788, 3066, "tai bwo wannai teleport"),
        named("Key Master teleport scroll", "Cerberus lair", "Scroll teleport", 2686, 9882, "key master teleport"),
        named("Revenant Cave teleport scroll", "Revenant Caves", "Scroll teleport", 3127, 3833, "revenant cave teleport"),
        named("Watson teleport scroll", "Watson", "Scroll teleport", 1645, 3579, "watson teleport"),
        named("Chasm of Fire teleport scroll", "Chasm of Fire", "Scroll teleport", 1311, 9882, "chasm of fire teleport"),
        named("Colossal Wyrm teleport scroll", "Colossal Wyrm", "Scroll teleport", 1641, 2921, "colossal wyrm teleport"),
        named("Mythical cape", "Myth's Guild", "Requires Dragon Slayer II; teleports to Myth's Guild", 2458, 2851, "mythical cape"),
        named("Chronicle", "Champions' Guild", "Charged Chronicle teleport", 3202, 3357, "chronicle"),
        named("Skull sceptre", "Stronghold of Security", "Sceptre teleport", 3081, 3421, "skull sceptre"),
        named("Camulet", "Enakhra's Temple", "Camulet teleport", 3190, 2923, "camulet"),
        named("Construction cape — Rimmington", "Rimmington", "Skillcape teleport; requires 99 Construction", 2954, 3224, "construction cape", "max cape"),
        named("Construction cape — Taverley", "Taverley", "Skillcape teleport; requires 99 Construction", 2894, 3465, "construction cape", "max cape"),
        named("Construction cape — Pollnivneach", "Pollnivneach", "Skillcape teleport; requires 99 Construction", 3340, 3004, "construction cape", "max cape"),
        named("Construction cape — Hosidius", "Hosidius", "Skillcape teleport; requires 99 Construction", 1744, 3517, "construction cape", "max cape"),
        named("Construction cape — Rellekka", "Rellekka", "Skillcape teleport; requires 99 Construction", 2670, 3632, "construction cape", "max cape"),
        named("Construction cape — Aldarin", "Aldarin", "Skillcape teleport; requires 99 Construction", 1422, 2966, "construction cape", "max cape"),
        named("Construction cape — Brimhaven", "Brimhaven", "Skillcape teleport; requires 99 Construction", 2758, 3178, "construction cape", "max cape"),
        named("Construction cape — Yanille", "Yanille", "Skillcape teleport; requires 99 Construction", 2544, 3095, "construction cape", "max cape"),
        named("Construction cape — Prifddinas", "Prifddinas", "Skillcape teleport; requires 99 Construction", 3239, 6076, "construction cape", "max cape"),
        named("Crafting cape", "Crafting Guild", "Skillcape teleport; requires 99 Crafting", 2931, 3286, "crafting cape", "max cape"),
        named("Fishing cape — Fishing Guild", "Fishing Guild", "Skillcape teleport; requires 99 Fishing", 2604, 3401, "fishing cape", "max cape"),
        named("Fishing cape — Otto's Grotto", "Otto's Grotto", "Skillcape teleport; requires 99 Fishing", 2504, 3484, "fishing cape", "max cape"),
        named("Hunter cape — Carnivorous Chinchompa Area", "Carnivorous Chinchompa Area", "Skillcape teleport; requires 99 Hunter", 2556, 2916, "hunter cape", "max cape"),
        named("Hunter cape — Black Chinchompa Area", "Black Chinchompa Area", "Skillcape teleport; requires 99 Hunter", 3144, 3772, "hunter cape", "max cape"),
        named("Sailing cape", "The Pandemonium", "Skillcape teleport; requires 99 Sailing", 3048, 2972, "sailing cape", "max cape"),
        named("Strength cape", "Warriors' Guild", "Skillcape teleport; requires 99 Strength", 2865, 3546, "strength cape", "max cape"),
        named("Quest cape", "Legends' Guild", "Quest cape teleport", 2729, 3348, "quest cape"),
        named("Music cape", "Falo the Bard", "Music cape teleport", 2689, 3547, "music cape"),
        named("Achievement diary cape — Two-pints", "Two-pints", "Select this destination from the Achievement diary cape", 2574, 3324, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Jarr", "Jarr", "Select this destination from the Achievement diary cape", 3300, 3121, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Sir Rebral", "Sir Rebral", "Select this destination from the Achievement diary cape", 2977, 3345, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Thorodin", "Thorodin", "Select this destination from the Achievement diary cape", 2659, 3627, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Flax Keeper", "Flax Keeper", "Select this destination from the Achievement diary cape", 2744, 3444, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Pirate Jackie the Fruit", "Pirate Jackie the Fruit", "Select this destination from the Achievement diary cape", 2810, 3191, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Kaleb Paramaya", "Kaleb Paramaya", "Select this destination from the Achievement diary cape", 2861, 2995, 1, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Jungle forester", "Jungle forester", "Select this destination from the Achievement diary cape", 2795, 2945, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — TzHaar-Mej", "TzHaar-Mej", "Select this destination from the Achievement diary cape", 2455, 5133, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Elise", "Elise", "Select this destination from the Achievement diary cape", 1647, 3666, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Hatius Cosaintus", "Hatius Cosaintus", "Select this destination from the Achievement diary cape", 3239, 3220, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Le-sabrè", "Le-sabrè", "Select this destination from the Achievement diary cape", 3465, 3477, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Toby", "Toby", "Select this destination from the Achievement diary cape", 3224, 3414, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Lesser Fanatic", "Lesser Fanatic", "Select this destination from the Achievement diary cape", 3122, 3516, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Elder Gnome child", "Elder Gnome child", "Select this destination from the Achievement diary cape", 2467, 3459, "achievement diary cape", "achievement cape"),
        named("Achievement diary cape — Twiggy O'Korn", "Twiggy O'Korn", "Select this destination from the Achievement diary cape", 3097, 3226, "achievement diary cape", "achievement cape"),
        named("Kharedst's Memoirs — Hosidius", "Hosidius", "Book teleport; unlock the corresponding memoir page", 1713, 3612, "kharedst's memoirs", "kharedsts memoirs"),
        named("Kharedst's Memoirs — Piscarilius", "Piscarilius", "Book teleport; unlock the corresponding memoir page", 1802, 3748, "kharedst's memoirs", "kharedsts memoirs"),
        named("Kharedst's Memoirs — Shayzien", "Shayzien", "Book teleport; unlock the corresponding memoir page", 1478, 3576, "kharedst's memoirs", "kharedsts memoirs"),
        named("Kharedst's Memoirs — Lovakengj", "Lovakengj", "Book teleport; unlock the corresponding memoir page", 1544, 3762, "kharedst's memoirs", "kharedsts memoirs"),
        named("Kharedst's Memoirs — Arceuus", "Arceuus", "Book teleport; unlock the corresponding memoir page", 1680, 3746, "kharedst's memoirs", "kharedsts memoirs"),
        named("Morytania legs — Slime Pit", "Slime Pit", "Diary reward; teleport access depends on tier", 3654, 3516, "morytania legs"),
        named("Morytania legs — Burgh de Rott", "Burgh de Rott", "Diary reward; teleport access depends on tier", 3482, 3231, "morytania legs"),
        named("Kandarin headgear", "Seers' Village", "Diary reward; teleport access depends on tier", 2729, 3411, "kandarin headgear"),
        named("Western Banner", "Piscatoris", "Diary reward; teleport access depends on tier", 2329, 3685, "western banner"),
        named("Sailors' Amulet — Pandemonium", "The Pandemonium", "Sailing transport; unlock the destination", 3060, 2974, "sailors' amulet", "sailors amulet"),
        named("Sailors' Amulet — Port Roberts", "Port Roberts", "Sailing transport; unlock the destination", 1889, 3292, "sailors' amulet", "sailors amulet"),
        named("Sailors' Amulet — Red Rock", "Red Rock", "Sailing transport; unlock the destination", 2797, 2521, "sailors' amulet", "sailors amulet"),
        named("Sailors' Amulet — Deepfin Point", "Deepfin Point", "Sailing transport; unlock the destination", 1943, 2758, "sailors' amulet", "sailors amulet"),
        named("Cowbell Amulet", "Cow field", "Transport item; confirm current unlock requirements", 3259, 3276, "cowbell amulet"),
        named("Giantsoul Amulet — Bryophyta", "Bryophyta's Lair", "Select the lair destination from the amulet", 3174, 9898, "giantsoul amulet"),
        named("Giantsoul Amulet — Obor", "Obor's Lair", "Select the lair destination from the amulet", 3096, 9833, "giantsoul amulet"),
        named("Giantsoul Amulet — Branda and Eldric", "Branda and Eldric's Lair", "Select the lair destination from the amulet", 2952, 9574, "giantsoul amulet"),
        named("Spider cave teleport scroll", "Spider Cave", "Scroll teleport", 3658, 3403, "spider cave teleport"),
        named("Ardeaglais teleport scroll", "Ardeaglais", "Scroll teleport", 2543, 2216, "ardeaglais teleport")
    ));
}
