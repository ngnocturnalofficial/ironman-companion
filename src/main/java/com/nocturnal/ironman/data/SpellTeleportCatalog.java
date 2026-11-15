package com.nocturnal.ironman.data;

import com.nocturnal.ironman.model.AccountSnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Teleport spells available on the currently selected spellbook. */
public final class SpellTeleportCatalog
{
    private SpellTeleportCatalog() {}

    public static final class SpellTeleport
    {
        public final String name;
        public final String spellbook;
        public final int magicLevel;
        public final String requiredQuest;
        public final String requiredDiary;
        public final String requiredDiaryTier;
        public final String destination;
        public final int x;
        public final int y;
        public final String notes;

        public SpellTeleport(String name, String spellbook, int magicLevel, String requiredQuest,
            String destination, int x, int y, String notes)
        {
            this(name, spellbook, magicLevel, requiredQuest, null, null, destination, x, y, notes);
        }

        public SpellTeleport(String name, String spellbook, int magicLevel, String requiredQuest,
            String requiredDiary, String requiredDiaryTier, String destination, int x, int y, String notes)
        {
            this.name = name;
            this.spellbook = spellbook;
            this.magicLevel = magicLevel;
            this.requiredQuest = requiredQuest;
            this.requiredDiary = requiredDiary;
            this.requiredDiaryTier = requiredDiaryTier;
            this.destination = destination;
            this.x = x;
            this.y = y;
            this.notes = notes;
        }

        public boolean canUse(AccountSnapshot account)
        {
            if (account == null || !spellbook.equalsIgnoreCase(account.getSpellbook())) return false;
            if (account.getRealLevels().getOrDefault("Magic", 1) < magicLevel) return false;
            if (requiredQuest != null
                && !"FINISHED".equals(account.getProgress().getQuests().get(requiredQuest))) return false;
            String ardougneDiary = account.getProgress().getDiaries().get("Ardougne");
            boolean ardougneHardComplete = ardougneDiary != null && ardougneDiary.contains("Hard: Complete");
            String varrockDiary = account.getProgress().getDiaries().get("Varrock");
            boolean varrockHardComplete = varrockDiary != null && varrockDiary.contains("Hard: Complete");
            String kandarinDiary = account.getProgress().getDiaries().get("Kandarin");
            boolean kandarinHardComplete = kandarinDiary != null && kandarinDiary.contains("Hard: Complete");
            if ("Watchtower Teleport".equals(name) && ardougneHardComplete) return false;
            if ("Varrock Teleport".equals(name) && varrockHardComplete) return false;
            if ("Camelot Teleport".equals(name) && kandarinHardComplete) return false;
            if (requiredDiary != null)
            {
                String diary = account.getProgress().getDiaries().get(requiredDiary);
                return diary != null && diary.contains(requiredDiaryTier + ": Complete");
            }
            return true;
        }
    }

    public static final List<SpellTeleport> ALL = Collections.unmodifiableList(Arrays.asList(
        new SpellTeleport("Varrock Teleport", "Standard", 25, null, "Varrock", 3213, 3424, "Standard spellbook"),
        new SpellTeleport("Varrock Teleport (Grand Exchange landing)", "Standard", 25, null, "Varrock", "Hard", "Grand Exchange", 3164, 3478, "Varrock Hard Diary changes the Varrock Teleport landing point"),
        new SpellTeleport("Lumbridge Teleport", "Standard", 31, null, "Lumbridge", 3222, 3218, "Standard spellbook"),
        new SpellTeleport("Falador Teleport", "Standard", 37, null, "Falador", 2965, 3381, "Standard spellbook"),
        new SpellTeleport("House Teleport", "Standard", 40, null, "Player-owned house", 0, 0, "Landing point depends on house portal and redirect setting"),
        new SpellTeleport("Camelot Teleport", "Standard", 45, null, "Camelot", 2757, 3477, "Standard spellbook"),
        new SpellTeleport("Camelot Teleport (Seers' bank landing)", "Standard", 45, null, "Kandarin", "Hard", "Seers' Village bank", 2726, 3485, "Kandarin Hard Diary changes the Camelot Teleport landing point"),
        new SpellTeleport("Ardougne Teleport", "Standard", 51, "Plague City", "Ardougne", 2664, 3306, "Plague City required"),
        new SpellTeleport("Watchtower Teleport", "Standard", 58, "Watchtower", "Watchtower", 2547, 3114, "Watchtower quest required"),
        new SpellTeleport("Trollheim Teleport", "Standard", 61, "Eadgar's Ruse", "Trollheim", 2891, 3678, "Eadgar's Ruse required"),
        new SpellTeleport("Kourend Castle Teleport", "Standard", 48, "Client of Kourend", "Kourend Castle", 1643, 3672, "Client of Kourend required"),
        new SpellTeleport("Civitas illa Fortis Teleport", "Standard", 54, "Twilight's Promise", "Civitas illa Fortis", 1681, 3133, "Twilight's Promise required"),
        new SpellTeleport("Ape Atoll Teleport", "Standard", 64, "Recipe for Disaster - Freeing King Awowogei", "Ape Atoll", 2796, 2791, "King Awowogei subquest required"),
        new SpellTeleport("Watchtower Teleport (Yanille landing)", "Standard", 58, "Watchtower", "Ardougne", "Hard", "Yanille", 2584, 3097, "Ardougne Hard Diary changes the Watchtower Teleport landing point"),

        new SpellTeleport("Paddewwa Teleport", "Ancient", 54, "Desert Treasure I", "Paddewwa", 3097, 9880, "Ancient Magicks"),
        new SpellTeleport("Senntisten Teleport", "Ancient", 60, "Desert Treasure I", "Senntisten", 3319, 3336, "Ancient Magicks"),
        new SpellTeleport("Kharyrll Teleport", "Ancient", 66, "Desert Treasure I", "Canifis", 3494, 3473, "Ancient Magicks; useful for Morytania"),
        new SpellTeleport("Lassar Teleport", "Ancient", 72, "Desert Treasure I", "Lassar", 3002, 3472, "Ancient Magicks"),
        new SpellTeleport("Dareeyak Teleport", "Ancient", 78, "Desert Treasure I", "Dareeyak", 2969, 3695, "Ancient Magicks"),
        new SpellTeleport("Carrallangar Teleport", "Ancient", 84, "Desert Treasure I", "Carrallangar", 3157, 3667, "Ancient Magicks"),
        new SpellTeleport("Annakarl Teleport", "Ancient", 90, "Desert Treasure I", "Annakarl", 3288, 3888, "Ancient Magicks"),
        new SpellTeleport("Ghorrock Teleport", "Ancient", 96, "Desert Treasure I", "Ghorrock", 2977, 3872, "Ancient Magicks"),

        new SpellTeleport("Moonclan Teleport", "Lunar", 69, "Lunar Diplomacy", "Moonclan Island", 2113, 3915, "Lunar spellbook"),
        new SpellTeleport("Ourania Teleport", "Lunar", 71, "Lunar Diplomacy", "Ourania Altar", 2468, 3246, "Lunar spellbook"),
        new SpellTeleport("Waterbirth Teleport", "Lunar", 72, "Lunar Diplomacy", "Waterbirth Island", 2546, 3755, "Lunar spellbook"),
        new SpellTeleport("Barbarian Teleport", "Lunar", 75, "Lunar Diplomacy", "Barbarian Outpost", 2543, 3568, "Lunar spellbook"),
        new SpellTeleport("Khazard Teleport", "Lunar", 78, "Lunar Diplomacy", "Port Khazard", 2636, 3167, "Lunar spellbook"),
        new SpellTeleport("Fishing Guild Teleport", "Lunar", 85, "Lunar Diplomacy", "Fishing Guild", 2612, 3391, "Lunar spellbook"),
        new SpellTeleport("Catherby Teleport", "Lunar", 87, "Lunar Diplomacy", "Catherby", 2802, 3449, "Lunar spellbook; close to Catherby herb patch"),
        new SpellTeleport("Ice Plateau Teleport", "Lunar", 89, "Lunar Diplomacy", "Ice Plateau", 2973, 3939, "Lunar spellbook"),

        new SpellTeleport("Arceuus Library Teleport", "Arceuus", 6, null, "Arceuus Library", 1632, 3838, "Arceuus spellbook"),
        new SpellTeleport("Draynor Manor Teleport", "Arceuus", 17, null, "Draynor Manor", 3108, 3352, "Arceuus spellbook"),
        new SpellTeleport("Battlefront Teleport", "Arceuus", 23, null, "Battlefront", 1349, 3739, "Arceuus spellbook"),
        new SpellTeleport("Mind Altar Teleport", "Arceuus", 28, null, "Mind Altar", 2979, 3509, "Arceuus spellbook"),
        new SpellTeleport("Respawn Teleport", "Arceuus", 34, null, "Respawn point", 0, 0, "Arceuus spellbook; landing point depends on respawn setting"),
        new SpellTeleport("Salve Graveyard Teleport", "Arceuus", 40, "Priest in Peril", "Salve Graveyard", 3433, 3461, "Priest in Peril required"),
        new SpellTeleport("Fenkenstrain's Castle Teleport", "Arceuus", 48, "Creature of Fenkenstrain", "Fenkenstrain's Castle", 3548, 3528, "Creature of Fenkenstrain required"),
        new SpellTeleport("West Ardougne Teleport", "Arceuus", 61, "Biohazard", "West Ardougne", 2500, 3291, "Biohazard required"),
        new SpellTeleport("Harmony Island Teleport", "Arceuus", 65, "The Great Brain Robbery", "Harmony Island", 3797, 2866, "The Great Brain Robbery required; close to the herb patch"),
        new SpellTeleport("Cemetery Teleport", "Arceuus", 71, null, "Cemetery", 2978, 3763, "Arceuus spellbook"),
        new SpellTeleport("Barrows Teleport", "Arceuus", 83, "Priest in Peril", "Barrows", 3565, 3315, "Priest in Peril required"),
        new SpellTeleport("Ape Atoll Teleport", "Arceuus", 90, "Monkey Madness I", "Ape Atoll", 2770, 2703, "Arceuus spellbook; Monkey Madness I required")
    ));

    public static List<SpellTeleport> available(AccountSnapshot account)
    {
        java.util.ArrayList<SpellTeleport> result = new java.util.ArrayList<>();
        for (SpellTeleport teleport : ALL) if (teleport.canUse(account)) result.add(teleport);
        return Collections.unmodifiableList(result);
    }
}
