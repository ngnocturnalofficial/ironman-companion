package com.nocturnal.ironman.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Access gates and starting checklists; the linked Wiki guide remains authoritative for current gear and mechanics. */
public final class BossCatalog
{
    private BossCatalog() {}

    public static final class Boss
    {
        public final String name;
        public final String accessQuest;
        public final int requiredSlayerLevel;
        public final String[] requiredTaskMatches;
        public final String taskExceptionDiary;
        public final String taskExceptionTier;
        public final Map<String, Integer> requiredSkills;
        public final Map<String, Integer> recommendedSkills;
        public final String combatStyle;
        public final String destination;
        public final List<String> gear;
        public final List<String> supplies;
        public final String specialNotes;
        public final String wikiUrl;

        public Boss(String name, String accessQuest, int requiredSlayerLevel, String[] requiredTaskMatches,
            Map<String, Integer> recommendedSkills, String combatStyle, String destination,
            List<String> gear, List<String> supplies, String specialNotes, String wikiUrl)
        {
            this(name, accessQuest, requiredSlayerLevel, requiredTaskMatches, Collections.emptyMap(),
                recommendedSkills, null, null, combatStyle, destination, gear, supplies, specialNotes, wikiUrl);
        }

        public Boss(String name, String accessQuest, int requiredSlayerLevel, String[] requiredTaskMatches,
            Map<String, Integer> requiredSkills, Map<String, Integer> recommendedSkills,
            String combatStyle, String destination, List<String> gear, List<String> supplies,
            String specialNotes, String wikiUrl)
        {
            this(name, accessQuest, requiredSlayerLevel, requiredTaskMatches, requiredSkills,
                recommendedSkills, null, null, combatStyle, destination, gear, supplies, specialNotes, wikiUrl);
        }

        public Boss(String name, String accessQuest, int requiredSlayerLevel, String[] requiredTaskMatches,
            Map<String, Integer> requiredSkills, Map<String, Integer> recommendedSkills,
            String taskExceptionDiary, String taskExceptionTier, String combatStyle,
            String destination, List<String> gear, List<String> supplies,
            String specialNotes, String wikiUrl)
        {
            this.name = name;
            this.accessQuest = accessQuest;
            this.requiredSlayerLevel = requiredSlayerLevel;
            this.requiredTaskMatches = requiredTaskMatches == null ? new String[0] : requiredTaskMatches.clone();
            this.taskExceptionDiary = taskExceptionDiary;
            this.taskExceptionTier = taskExceptionTier;
            this.requiredSkills = Collections.unmodifiableMap(new LinkedHashMap<>(requiredSkills));
            this.recommendedSkills = Collections.unmodifiableMap(new LinkedHashMap<>(recommendedSkills));
            this.combatStyle = combatStyle;
            this.destination = destination;
            this.gear = Collections.unmodifiableList(gear);
            this.supplies = Collections.unmodifiableList(supplies);
            this.specialNotes = specialNotes;
            this.wikiUrl = wikiUrl;
        }
    }

    private static Map<String, Integer> stats(Object... values)
    {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put((String) values[i], (Integer) values[i + 1]);
        return result;
    }

    private static List<String> list(String... values) { return Arrays.asList(values); }

    public static final List<Boss> ALL = Collections.unmodifiableList(Arrays.asList(
        new Boss("Barrows", "Priest in Peril", 0, null,
            stats("Prayer", 43, "Magic", 50, "Attack", 50, "Strength", 50, "Defence", 50, "Ranged", 50),
            "Magic with melee/ranged coverage", "Barrows",
            list("Magic weapon for Ahrim; melee setup for the other brothers; ranged option if preferred", "Spade", "Morytania access"),
            list("Food", "Prayer potions or restores", "Runes for the chosen spell", "Emergency teleport", "Optional: Barrows teleport tablets"),
            "Morytania Hard Diary improves rune rewards and offers a close teleport. Prayer drain and brother order affect the trip.",
            "https://oldschool.runescape.wiki/w/Barrows/Strategies"),
        new Boss("Zulrah", "Regicide", 0, null,
            stats("Ranged", 80, "Magic", 75, "Defence", 70, "Prayer", 45),
            "Ranged + Magic switches", "Zul-Andra",
            list("Ranged and Magic weapon/armour switches", "Ring of recoil", "Poison/venom protection", "Rune pouch or spell runes"),
            list("Antivenom or suitable venom protection", "Food", "Prayer potions", "Ring of recoil", "Teleport out"),
            "Regicide is required to reach Zul-Andra; speak to High Priestess Zul-Harcinqa before boarding the sacrificial boat.",
            "https://oldschool.runescape.wiki/w/Zulrah/Strategies"),
        new Boss("Vorkath", "Dragon Slayer II", 0, null,
            stats("Ranged", 85, "Defence", 80, "Prayer", 70, "Magic", 75),
            "Ranged (melee is an alternative)", "Rellekka",
            list("Ranged setup", "Anti-dragon shield or dragonfire ward", "Ava's device", "Salve amulet (ei) when appropriate", "Ruby/diamond dragon bolts (e) or method-specific ammunition"),
            list("Extended super antifire", "Antivenom+", "Food", "Prayer potions", "Crumble Undead runes/spell", "Emergency teleport"),
            "Dragon Slayer II is the access requirement. Acid pools, dragonfire and the spawn special can be lethal; Hardcore accounts should assess death risk first.",
            "https://oldschool.runescape.wiki/w/Vorkath/Strategies"),
        new Boss("Kraken", null, 87, new String[]{"CAVE KRAKEN", "KRAKEN"},
            stats("Magic", 75, "Defence", 70, "Prayer", 43),
            "Magic", "Kraken Cove",
            list("Trident or suitable Magic weapon", "Magic accuracy/damage gear", "Fishing explosives"),
            list("Food", "Prayer potions", "Fishing explosives", "Optional: antipoison"),
            "Requires 87 Slayer and a Cave kraken/Kraken Slayer task. Kraken is immune to melee and takes heavily reduced ranged damage.",
            "https://oldschool.runescape.wiki/w/Kraken/Strategies"),
        new Boss("Cerberus", null, 91, new String[]{"HELLHOUND", "CERBERUS"},
            stats("Prayer", 70, "Attack", 75, "Strength", 75, "Ranged", 75),
            "Melee, Ranged or Magic", "Taverley Dungeon",
            list("Style-specific high-level weapon and armour", "Boots appropriate to the chosen setup", "Prayer bonus gear"),
            list("Prayer potions", "Food", "Super restores", "Emergency teleport"),
            "Requires 91 Slayer and a Cerberus or hellhound task. Wild pies can boost from 86 Slayer, but the boost must be maintained.",
            "https://oldschool.runescape.wiki/w/Cerberus/Strategies"),
        new Boss("Alchemical Hydra", null, 95, new String[]{"HYDRA"},
            stats("Ranged", 85, "Prayer", 74, "Defence", 75),
            "Ranged", "Mount Karuulm",
            list("Ranged weapon and armour", "Rada's blessing 3/4 or another route to Mount Karuulm", "Boots of stone, boots of brimstone or granite boots unless protected by the elite diary"),
            list("Food", "Prayer potions", "Antidote/venom protection as appropriate", "Ranged potions", "Emergency teleport"),
            "Requires 95 Slayer and a Hydra task. The Alchemical Hydra task is assigned by Konar; the dungeon floor requires heat-protection boots unless Kourend & Kebos Elite Diary is complete.",
            "https://oldschool.runescape.wiki/w/Alchemical_Hydra"),
        new Boss("Phantom Muspah", "Secrets of the North", 0, null,
            stats("Ranged", 85, "Magic", 85, "Prayer", 74),
            "Ranged with Magic or Sapphire bolt support", "Weiss",
            list("Strong Ranged setup", "Magic switch if using freezes", "Sapphire bolts (e) or another shield-phase method", "Thrall runes if unlocked"),
            list("Food", "Prayer potions", "Sapphire bolts (e)", "Antivenom/poison protection as needed", "Emergency teleport"),
            "Secrets of the North unlocks the Ghorrock Dungeon. Desert Treasure II plus the Frozen tablet adds a faster Ring of shadows route.",
            "https://oldschool.runescape.wiki/w/Phantom_Muspah/Strategies"),
        new Boss("The Gauntlet", "Song of the Elves", 0, null,
            stats("Attack", 80, "Strength", 85, "Defence", 80, "Ranged", 85, "Magic", 80, "Prayer", 77, "Cooking", 48),
            "Ranged + Magic (weapons gathered inside)", "Prifddinas",
            list("No equipment is required; gather armour and weapons inside", "Learn resource routes, prep timing and Hunllef prayer switches"),
            list("No outside supplies required; food and potions are made inside"),
            "Song of the Elves is required. The listed levels are recommendations, not access requirements; the Gauntlet supplies its own equipment.",
            "https://oldschool.runescape.wiki/w/The_Gauntlet/Strategies"),
        new Boss("Tombs of Amascut", "Beneath Cursed Sands", 0, null,
            stats("Attack", 70, "Strength", 70, "Defence", 70, "Ranged", 75, "Magic", 70, "Prayer", 77),
            "Melee + Ranged + Magic", "Nardah",
            list("Three combat styles", "Rune pickaxe or better for the mining check", "Gear adjusted to invocation level and team size"),
            list("Food", "Prayer restores", "Stamina", "Antidote/poison protection", "Emergency teleport"),
            "Beneath Cursed Sands unlocks the raid. Start with low invocations and scale only after learning the paths and boss mechanics.",
            "https://oldschool.runescape.wiki/w/Tombs_of_Amascut/Strategies"),
        new Boss("Giant Mole", null, 0, null,
            stats("Ranged", 70, "Prayer", 43, "Defence", 60),
            "Ranged", "Giant Mole",
            list("Ranged setup", "Spade", "Falador Hard Diary mole locator is strongly recommended"),
            list("Food", "Prayer potions", "Emergency teleport"),
            "No quest or Slayer task is required. Falador Hard Diary makes the mole locator available and substantially improves tracking.",
            "https://oldschool.runescape.wiki/w/Giant_Mole/Strategies"),
        new Boss("Sarachnis", null, 0, null,
            stats("Attack", 60, "Strength", 60, "Defence", 60, "Prayer", 43),
            "Crush melee", "Sarachnis",
            list("Crush weapon", "Melee armour", "Prayer bonus gear"),
            list("Food", "Prayer potions", "Antipoison if required by the chosen route"),
            "No quest or Slayer task is required. Prioritise crush accuracy and learn the web and minion mechanics.",
            "https://oldschool.runescape.wiki/w/Sarachnis/Strategies"),
        new Boss("TzTok-Jad", null, 0, null,
            stats("Ranged", 75, "Defence", 70, "Prayer", 70),
            "Ranged", "TzHaar Fight Cave",
            list("Ranged weapon with reliable ammunition", "High Ranged defence", "Optional: task-specific Slayer helmet if on task"),
            list("Prayer potions", "Saradomin brews", "Super restores", "Emergency teleport is unavailable inside the cave"),
            "No quest is required. Learn Jad's attack animations and prayer switches; the Fight Cave is a long single-session challenge.",
            "https://oldschool.runescape.wiki/w/TzTok-Jad/Strategies"),
        new Boss("Chambers of Xeric", null, 0, null,
            stats("Attack", 75, "Strength", 75, "Defence", 70, "Ranged", 80, "Magic", 80, "Prayer", 70),
            "Melee + Ranged + Magic", "Chambers of Xeric",
            list("Three combat styles", "Stamina and utility gear", "Pickaxe for the resource rooms"),
            list("Food", "Prayer potions", "Stamina", "Antidote/poison protection", "Rope if required by the raid route"),
            "No quest is required. Learn the room order and team roles; solo and group strategies differ significantly.",
            "https://oldschool.runescape.wiki/w/Chambers_of_Xeric/Strategies"),
        new Boss("Kalphite Queen", null, 0, null,
            stats("Attack", 80, "Strength", 80, "Defence", 75, "Ranged", 80, "Prayer", 70),
            "Melee + Ranged switch", "Kalphite Lair",
            list("Strong melee weapon and Ranged switch", "Two ropes unless the Hard Desert Diary has secured them permanently", "Antipoison or poison protection"),
            list("Food", "Prayer potions", "Antipoison", "Two ropes if not permanently placed", "Emergency teleport"),
            "The lair is west of Shantay Pass. The Hard Desert Diary makes placed ropes permanent; the Elite Desert Diary unlocks the 86 Agility shortcut.",
            "https://oldschool.runescape.wiki/w/Kalphite_Queen/Strategies"),
        new Boss("Dagannoth Kings", "The Fremennik Trials", 0, null,
            stats("Attack", 80, "Strength", 80, "Defence", 75, "Ranged", 80, "Magic", 80, "Prayer", 70),
            "Three combat styles; one style per King", "Waterbirth Island",
            list("Melee setup", "Ranged setup", "Magic setup", "Pet rock", "Rune thrownaxes", "Waterbirth access"),
            list("Food", "Prayer potions", "Stamina", "Pet rock", "Rune thrownaxes", "Three-style ammunition and runes"),
            "The three Kings use different combat styles and can attack together. Learn the Waterbirth Dungeon route before descending to the lair.",
            "https://oldschool.runescape.wiki/w/Dagannoth_Kings/Strategies"),
        new Boss("King Black Dragon", null, 0, null,
            stats("Ranged", 75, "Defence", 70, "Prayer", 43),
            "Ranged", "Lava Maze",
            list("Ranged setup", "Anti-dragon shield or suitable dragonfire protection", "Low-risk Wilderness gear"),
            list("Food", "Prayer potions", "Dragonfire protection", "Emergency teleport", "Optional: 50,000 coins for a private instance"),
            "The route to the lair entrance crosses deep Wilderness. The lair itself is safe from player attacks, but the approach is not; the lever is required to enter.",
            "https://oldschool.runescape.wiki/w/King_black_dragon"),
        new Boss("Grotesque Guardians", null, 75, new String[]{"GARGOYLE", "GROTESQUE"},
            stats("Attack", 75, "Strength", 75, "Defence", 70, "Ranged", 75, "Prayer", 70),
            "Melee + Ranged", "Slayer Tower",
            list("Brittle key to unlock the rooftop", "Melee and Ranged gear switches", "Gargoyle or Grotesque Guardians task"),
            list("Food", "Prayer potions", "Ranged ammunition", "Emergency teleport"),
            "The brittle key only drops from gargoyles while on task. After unlocking the roof, a gargoyle or Grotesque Guardians task is still required to fight them.",
            "https://oldschool.runescape.wiki/w/Grotesque_Guardians/Strategies"),
        new Boss("Thermonuclear Smoke Devil", null, 93, new String[]{"SMOKE DEVIL"},
            Collections.emptyMap(), stats("Attack", 85, "Strength", 85, "Defence", 70, "Magic", 75, "Prayer", 70),
            "Western Provinces", "Elite", "Melee", "Smoke Devil Dungeon",
            list("Melee setup", "Smoke devil Slayer task"),
            list("Food", "Prayer potions", "Super combat potion", "Emergency teleport"),
            "Requires 93 Slayer and a smoke devil task, except for the single Western Provinces Diary kill. A dwarf multicannon cannot be used in the boss room.",
            "https://oldschool.runescape.wiki/w/Thermonuclear_smoke_devil"),
        new Boss("Araxxor", "Priest in Peril", 92, new String[]{"ARAXYTE", "SPIDER"},
            stats("Attack", 85, "Strength", 85, "Defence", 80, "Ranged", 85, "Prayer", 70),
            "Melee with optional Ranged/Magic switch", "Morytania Spider Cave",
            list("Melee setup", "Anti-venom protection", "Araxyte or spider Slayer task"),
            list("Food", "Prayer potions", "Antivenom", "Vengeance runes if unlocked", "Emergency teleport"),
            "Requires 92 Slayer and an Araxyte or spider task. The cave is north-east of Darkmeyer; a spider cave teleport scroll is a direct approach.",
            "https://oldschool.runescape.wiki/w/Araxxor"),
        new Boss("The Nightmare", "Priest in Peril", 0, null,
            stats("Attack", 85, "Strength", 85, "Defence", 80, "Ranged", 85, "Magic", 85, "Prayer", 70),
            "Melee + Ranged + Magic", "Sisterhood Sanctuary",
            list("Three combat styles", "Group-ready gear and role assignment", "Slepe access"),
            list("Food", "Prayer potions", "Sanfew serum or poison protection as appropriate", "Stamina", "Emergency teleport"),
            "Located beneath Slepe in Morytania. Access to Morytania is the only quest requirement; Drakan's medallion can teleport directly to Slepe after the Slepey tablet unlock.",
            "https://oldschool.runescape.wiki/w/The_Nightmare/Strategies"),
        new Boss("Nex", "The Frozen Door", 0, null,
            stats("Hitpoints", 70, "Ranged", 70, "Strength", 70, "Agility", 70),
            stats("Attack", 90, "Strength", 90, "Defence", 80, "Ranged", 90, "Magic", 76, "Prayer", 74),
            "Ranged with team-based role switches", "God Wars Dungeon",
            list("Frozen Door miniquest", "Four faction kill-count/key pieces to unlock the Ancient Prison", "Armadyl, Bandos, Saradomin and Zamorak protection items"),
            list("Food", "Brews", "Super restores", "Ranging potions", "Ammunition", "Team-specific supplies"),
            "The Frozen Door is required. Access to the four God Wars bases requires 70 Ranged, Strength, Agility and Hitpoints respectively; the Ancient Prison is reached after assembling the frozen key. Nex is strongly group-oriented.",
            "https://oldschool.runescape.wiki/w/Nex/Strategies"),
        new Boss("Vardorvis", "Desert Treasure II - The Fallen Empire", 0, null,
            stats("Attack", 85, "Strength", 85, "Defence", 80, "Ranged", 80, "Prayer", 76),
            "Melee", "The Stranglewood",
            list("Strong melee setup", "Desert Treasure II completion", "Ring of shadows with Strangled tablet is a direct route if unlocked"),
            list("Food", "Prayer potions", "Combat potions", "Emergency teleport"),
            "The boss is at the Stranglewood Ritual Site. The Ring of shadows requires the Strangled tablet to unlock its direct teleport; the approach can still be dangerous.",
            "https://oldschool.runescape.wiki/w/Vardorvis"),
        new Boss("Duke Sucellus", "Desert Treasure II - The Fallen Empire", 0, null,
            stats("Attack", 80, "Strength", 80, "Defence", 75, "Ranged", 80, "Magic", 76, "Prayer", 70),
            "Melee with Magic support", "Ghorrock Dungeon",
            list("Melee setup", "Desert Treasure II completion", "Ring of shadows with Frozen tablet or Icy basalt to Weiss"),
            list("Food", "Prayer potions", "Combat potions", "Pickaxe for the salt mine route", "Emergency teleport"),
            "Duke Sucellus is in Ghorrock Prison, reached through Ghorrock Dungeon from Weiss's Salt Mine. The Ring of shadows requires the Frozen tablet for a direct dungeon teleport.",
            "https://oldschool.runescape.wiki/w/Duke_Sucellus/Strategies"),
        new Boss("The Whisperer", "Desert Treasure II - The Fallen Empire", 0, null,
            stats("Attack", 80, "Strength", 80, "Defence", 75, "Ranged", 85, "Magic", 85, "Prayer", 74),
            "Ranged + Magic", "Lassar Undercity",
            list("Ranged and Magic switches", "Desert Treasure II completion", "Ring of shadows with Sirenic tablet if unlocked"),
            list("Food", "Prayer potions", "Ranged ammunition", "Runes", "Emergency teleport"),
            "The Whisperer is in the Sunken Cathedral of Lassar Undercity. The Ring of shadows requires the Sirenic tablet to unlock the direct teleport.",
            "https://oldschool.runescape.wiki/w/The_Whisperer"),
        new Boss("The Leviathan", "Desert Treasure II - The Fallen Empire", 0, null,
            stats("Attack", 80, "Strength", 80, "Defence", 75, "Ranged", 90, "Magic", 80, "Prayer", 74),
            "Ranged", "The Scar approach",
            list("Strong Ranged setup", "Desert Treasure II completion", "Ring of shadows with Scarred tablet if unlocked"),
            list("Food", "Prayer potions", "Ranged ammunition", "Stamina", "Emergency teleport"),
            "Reach the Temple of the Eye through the Wizard's Tower, then speak to the Catalytic Guardian to enter the Scar. The Ring of shadows requires the Scarred tablet to unlock its direct teleport.",
            "https://oldschool.runescape.wiki/w/The_Leviathan"),
        new Boss("Zalcano", "Song of the Elves", 0, null,
            stats("Mining", 70, "Smithing", 70, "Runecraft", 70, "Agility", 70),
            "Skilling boss; Mining, Smithing and Runecraft roles", "Prifddinas",
            list("Pickaxe", "Hammer", "Chisel"),
            list("Food", "Stamina", "Mining and Smithing tools", "Emergency teleport"),
            "Song of the Elves is required to enter Prifddinas. The listed levels are recommendations for comfortable contribution, not hard entry requirements.",
            "https://oldschool.runescape.wiki/w/Zalcano"),
        new Boss("Hespori", null, 0, null,
            Collections.singletonMap("Farming", 65), stats("Attack", 60, "Strength", 60, "Defence", 60, "Prayer", 43),
            "Melee", "Farming Guild",
            list("Hespori seed", "Combat setup"),
            list("Food", "Prayer potions", "Antipoison if needed", "Spade to clear the patch after the fight"),
            "Requires a Hespori seed planted in the Farming Guild boss patch. The seed is obtained from farming activities; the fight is instanced.",
            "https://oldschool.runescape.wiki/w/Hespori"),
        new Boss("Obor", null, 0, null,
            stats("Attack", 50, "Strength", 50, "Defence", 40, "Prayer", 43),
            "Melee or Ranged", "Edgeville Dungeon",
            list("Hill Giant key", "Basic combat gear"),
            list("Food", "Prayer potions", "Emergency teleport"),
            "A Hill Giant key is required and is consumed to enter Obor's arena. Keys are dropped by hill giants; the arena is accessed from Edgeville Dungeon.",
            "https://oldschool.runescape.wiki/w/Obor"),
        new Boss("Bryophyta", null, 0, null,
            stats("Attack", 50, "Strength", 50, "Defence", 40, "Prayer", 43, "Magic", 50),
            "Melee with Magic utility", "Varrock Sewers",
            list("Mossy key", "Combat gear", "Magic option for the growthlings"),
            list("Food", "Prayer potions", "Fire spell or other growthling solution", "Emergency teleport"),
            "A Mossy key is required and is consumed to enter Bryophyta's arena. Mossy keys drop from moss giants; the lair is inside Varrock Sewers.",
            "https://oldschool.runescape.wiki/w/Bryophyta"),
        new Boss("Corporeal Beast", null, 0, null,
            stats("Attack", 90, "Strength", 90, "Defence", 80, "Ranged", 90, "Prayer", 74),
            "High-damage melee; team or specialized solo methods", "Corporeal Beast",
            list("High-tier melee weapon", "Spectral spirit shield or another suitable damage-reduction option"),
            list("Food", "Prayer potions", "Stamina", "Emergency teleport", "Team-specific supplies"),
            "Use the games necklace or the level 21 Wilderness lair entrance. The Corporeal Beast has very high defence and damage reduction against most weapons; review current Ironman solo methods before committing supplies.",
            "https://oldschool.runescape.wiki/w/Corporeal_Beast"),
        new Boss("General Graardor", "Troll Stronghold", 0, null,
            Collections.singletonMap("Strength", 70), stats("Attack", 85, "Strength", 90, "Defence", 80, "Ranged", 85, "Prayer", 70),
            "Melee or Ranged", "God Wars Dungeon",
            list("Bandos protection item", "Rope if the entrance has not been secured", "Combat setup"),
            list("Food", "Prayer potions", "Stamina", "Emergency teleport", "Team-specific supplies"),
            "The Bandos stronghold requires 70 Strength. Bring a Bandos protection item and learn the minion/kill-count route; the first GWD entry requires a rope.",
            "https://oldschool.runescape.wiki/w/General_Graardor/Strategies"),
        new Boss("K'ril Tsutsaroth", "Troll Stronghold", 0, null,
            Collections.singletonMap("Hitpoints", 70), stats("Attack", 85, "Strength", 90, "Defence", 80, "Prayer", 70),
            "Melee or Ranged", "God Wars Dungeon",
            list("Zamorak protection item", "Rope if the entrance has not been secured", "Combat setup"),
            list("Food", "Prayer potions", "Stamina", "Emergency teleport", "Team-specific supplies"),
            "The Zamorak fortress requires 70 Hitpoints. Bring a Zamorak protection item; K'ril's attacks can drain Prayer and deal heavy melee damage.",
            "https://oldschool.runescape.wiki/w/K%27ril_Tsutsaroth/Strategies"),
        new Boss("Commander Zilyana", "Troll Stronghold", 0, null,
            Collections.singletonMap("Agility", 70), stats("Attack", 85, "Strength", 85, "Ranged", 90, "Defence", 75, "Prayer", 70),
            "Ranged or melee", "God Wars Dungeon",
            list("Saradomin protection item", "Rope if the entrance has not been secured", "Combat setup"),
            list("Food", "Prayer potions", "Stamina", "Emergency teleport", "Team-specific supplies"),
            "The Saradomin encampment requires 70 Agility. Bring a Saradomin protection item; Zilyana is mobile and can hit accurately with melee.",
            "https://oldschool.runescape.wiki/w/Commander_Zilyana/Strategies"),
        new Boss("Kree'arra", "Troll Stronghold", 0, null,
            Collections.singletonMap("Ranged", 70), stats("Ranged", 90, "Defence", 80, "Prayer", 70, "Magic", 80),
            "Ranged", "God Wars Dungeon",
            list("Armadyl protection item", "Rope if the entrance has not been secured", "High Ranged defence setup"),
            list("Food", "Prayer potions", "Stamina", "Emergency teleport", "Team-specific supplies"),
            "Armadyl's Eyrie requires 70 Ranged. Kree'arra uses all three combat styles; high Ranged defence and a team or specialized solo strategy are important.",
            "https://oldschool.runescape.wiki/w/Kree%27arra/Strategies"),
        new Boss("The Inferno", null, 0, null,
            stats("Ranged", 90, "Prayer", 77, "Defence", 80, "Magic", 94),
            "Ranged with Magic utility", "Mor Ul Rek",
            list("Fire cape (entry requirement)", "Ranged setup", "Magic switch", "Tank gear for the later waves"),
            list("Saradomin brews", "Super restores", "Bastion potion", "Stamina", "Emergency teleport is unavailable inside"),
            "A Fire cape is required to enter. The Inferno is a long, high-risk wave challenge ending with TzKal-Zuk; learn safe spots, spawn solving and prayer flicking before attempting it on a Hardcore account.",
            "https://oldschool.runescape.wiki/w/Inferno/Strategies"),
        new Boss("TzKal-Zuk", null, 0, null,
            stats("Ranged", 90, "Prayer", 77, "Defence", 80, "Magic", 94),
            "Ranged with Magic utility", "Mor Ul Rek",
            list("Fire cape (Inferno entry requirement)", "Ranged setup", "Magic switch", "Tank gear for the final wave"),
            list("Saradomin brews", "Super restores", "Bastion potion", "Stamina", "No teleport out of the Inferno"),
            "TzKal-Zuk is the final boss of the Inferno, not a separate encounter. The profile covers the final-wave loadout; the full 69-wave challenge must be completed first.",
            "https://oldschool.runescape.wiki/w/TzKal-Zuk")    ));

    public static Boss find(String name)
    {
        if (name == null) return null;
        for (Boss boss : ALL) if (boss.name.equalsIgnoreCase(name)) return boss;
        return null;
    }
}
