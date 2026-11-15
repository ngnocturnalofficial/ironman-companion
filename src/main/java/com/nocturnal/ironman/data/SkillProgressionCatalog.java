package com.nocturnal.ironman.data;

import com.nocturnal.ironman.model.AccountSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Account-independent milestone references; detailed training methods are retrieved from the OSRS Wiki. */
public final class SkillProgressionCatalog
{
    private static final Map<String, List<Milestone>> MILESTONES;

    static
    {
        Map<String, List<Milestone>> data = new LinkedHashMap<>();
        add(data, "Attack", m(40, "Rune weapons"), m(60, "Dragon weapons"), m(70, "Abyssal whip"), m(75, "Ghrazi rapier and other high-tier weapons"));
        add(data, "Defence", m(40, "Rune armour"), m(60, "Dragon armour"), m(70, "Barrows armour"));
        add(data, "Strength", m(50, "Mid-game melee setup checkpoint"), m(60, "High-tier melee training checkpoint"), m(70, "End-game melee setup checkpoint"));
        add(data, "Hitpoints", m(50, "Mid-game survivability checkpoint"), m(70, "High-level PvM survivability checkpoint"), m(90, "End-game PvM survivability checkpoint"));
        add(data, "Ranged", m(50, "Magic shortbow"), m(61, "Rune crossbow"), m(75, "Toxic blowpipe"));
        add(data, "Prayer", m(43, "Protection prayers"), m(52, "Smite"), m(70, "Piety (also requires King's Ransom and Knight Waves)"), m(74, "Rigour (Dexterous prayer scroll)"), m(77, "Augury (Arcane prayer scroll)"));
        add(data, "Magic", m(55, "High Level Alchemy"), m(75, "Trident of the seas"), m(94, "Ice Barrage (Desert Treasure I)"));
        add(data, "Cooking", m(40, "Lobsters"), m(62, "Monkfish (Swan Song)"), m(80, "Sharks"), m(84, "Anglerfish"));
        add(data, "Woodcutting", m(35, "Teak trees"), m(50, "Mahogany trees"), m(60, "Yew trees"), m(75, "Magic trees"), m(90, "Redwood trees"));
        add(data, "Fletching", m(50, "Magic shortbow"), m(55, "Broad bolts (requires the Slayer unlock)"), m(75, "Magic longbow"), m(81, "Rune darts"));
        add(data, "Fishing", m(62, "Monkfish (Swan Song)"), m(76, "Sharks"), m(82, "Anglerfish"));
        add(data, "Firemaking", m(50, "Wintertodt"), m(60, "Yew logs"), m(75, "Magic logs"), m(90, "Redwood logs"));
        add(data, "Crafting", m(40, "Ruby jewellery"), m(63, "Dragonstone jewellery"), m(75, "Zenyte jewellery (Monkey Madness II access)"));
        add(data, "Smithing", m(40, "Gold bars"), m(60, "Mithril equipment"), m(70, "Adamant equipment"), m(85, "Runite bars"));
        add(data, "Mining", m(40, "Gold ore"), m(55, "Mithril ore"), m(70, "Adamantite ore"), m(85, "Runite ore"), m(92, "Amethyst"));
        add(data, "Herblore", m(38, "Prayer potions"), m(63, "Super restore potions"), m(72, "Ranging potions"), m(81, "Saradomin brews"), m(90, "Super combat potions"));
        add(data, "Agility", m(40, "Canifis rooftop course"), m(52, "Wilderness course"), m(60, "Seers' Village rooftop course"), m(70, "Pollnivneach rooftop course"), m(80, "Rellekka rooftop course"), m(90, "Ardougne rooftop course"));
        add(data, "Thieving", m(50, "Mid-level blackjacking methods"), m(65, "Higher-tier Pyramid Plunder rooms"), m(85, "Prifddinas elf pickpocketing"));
        add(data, "Slayer", m(75, "Gargoyles"), m(85, "Abyssal demons"), m(91, "Cerberus"), m(95, "Alchemical Hydra"));
        add(data, "Farming", m(32, "Ranarr herb patches"), m(38, "Toadflax herb patches"), m(62, "Snapdragon herb patches"), m(73, "Lantadyme herb patches"), m(85, "Torstol herb patches"));
        add(data, "Runecraft", m(27, "Cosmic runes"), m(44, "Nature runes"), m(54, "Law runes"), m(77, "Blood runes"), m(90, "Soul runes"));
        add(data, "Hunter", m(43, "Falconry"), m(63, "Red chinchompas"), m(73, "Black chinchompas"), m(80, "Herbiboar"));
        add(data, "Construction", m(50, "Portal chambers"), m(75, "Gilded altar"), m(83, "High-tier player-owned house planning"), m(90, "Ornate rejuvenation pool"));
        add(data, "Sailing", m(1, "Complete Pandemonium to begin Sailing; receive the starter raft"), m(15, "Skiff and small shipwreck salvaging"), m(30, "The Tempor Tantrum Barracuda Trial"), m(40, "Crab Quest"), m(45, "Troubled Tortugans"), m(52, "The Red Reef"), m(55, "The Jubbly Jive Barracuda Trial"), m(62, "Fallen From Grace and Rellekka route"), m(70, "Prifddinas route (Song of the Elves)"), m(72, "The Gwenith Glide Barracuda Trial"), m(76, "Lunar Isle route (Lunar Diplomacy)"));
        MILESTONES = Collections.unmodifiableMap(data);
    }

    private SkillProgressionCatalog() {}

    private static Milestone m(int level, String description) { return new Milestone(level, description); }

    private static void add(Map<String, List<Milestone>> data, String skill, Milestone... milestones)
    {
        data.put(skill, Collections.unmodifiableList(Arrays.asList(milestones)));
    }

    public static List<String> suggestions(AccountSnapshot account)
    {
        List<String> result = new ArrayList<>();
        result.add("NEXT SKILL MILESTONES — all skills");
        result.add("XP figures use the OSRS level curve. Milestones are checkpoints, not a claim that one training method is optimal for every Ironman.");
        for (Map.Entry<String, Integer> entry : account.getRealLevels().entrySet())
        {
            String skill = entry.getKey();
            int level = entry.getValue();
            int xp = account.getXp().getOrDefault(skill, 0);
            Milestone next = nextMilestone(skill, level);
            String line = skill + " " + level + " (" + String.format("%,d", xp) + " XP)";
            if ("Sailing".equals(skill) && level <= 1
                && !"FINISHED".equals(account.getProgress().getQuests().get("Pandemonium")))
            {
                result.add(line + " — complete Pandemonium to unlock Sailing training.");
                continue;
            }
            if (level >= 99)
            {
                result.add(line + " — 99 reached.");
                continue;
            }
            int nextLevel = Math.min(99, level + 1);
            int xpToNext = Math.max(0, xpForLevel(nextLevel) - xp);
            line += " — " + String.format("%,d XP", xpToNext) + " to level " + nextLevel;
            if (next != null)
            {
                int xpToMilestone = Math.max(0, xpForLevel(next.level) - xp);
                line += "; milestone " + next.level + ": " + next.description + " (" + String.format("%,d", xpToMilestone) + " XP)";
            }
            else line += "; no additional catalogued milestone — ask the Wiki assistant for a tailored route.";
            result.add(line);
        }
        return result;
    }

    public static int xpForLevel(int level)
    {
        if (level <= 1) return 0;
        if (level > 99) return 13_034_431;
        double points = 0;
        for (int current = 1; current < level; current++)
            points += Math.floor(current + 300.0 * Math.pow(2.0, current / 7.0));
        return (int) Math.floor(points / 4.0);
    }

    private static Milestone nextMilestone(String skill, int currentLevel)
    {
        List<Milestone> milestones = MILESTONES.get(skill);
        if (milestones == null) return null;
        for (Milestone milestone : milestones) if (milestone.level > currentLevel) return milestone;
        return null;
    }

    private static final class Milestone
    {
        private final int level;
        private final String description;
        private Milestone(int level, String description)
        {
            this.level = level;
            this.description = description;
        }
    }
}
