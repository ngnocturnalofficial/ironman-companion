package com.nocturnal.ironman.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ActivityCatalog
{
    private ActivityCatalog() {}

    public static final class Activity
    {
        public final String category;
        public final String title;
        public final String summary;
        public final String requirements;
        public final String supplies;

        public Activity(String category, String title, String summary, String requirements, String supplies)
        {
            this.category = category;
            this.title = title;
            this.summary = summary;
            this.requirements = requirements;
            this.supplies = supplies;
        }
    }

    public static final List<Activity> ALL = Collections.unmodifiableList(Arrays.asList(
        new Activity("Farming", "Herb run", "Visit unlocked herb patches, plant the best available herb, and harvest when grown.", "Farming level for the chosen herb; patch access, disease protection and diary benefits vary.", "Seeds, spade, rake, seed dibber, compost, plant cure and patch access items."),
        new Activity("Farming", "Tree and fruit tree run", "Plant and check available tree and fruit-tree patches for large Farming experience drops.", "Tree type level; patch quests and access requirements vary.", "Saplings, spade, rake, payment or protection items, and patch teleports."),
        new Activity("Farming", "Allotment and flower run", "Combine allotments, flower patches and nearby herb patches into one route.", "Patch access and crop level requirements.", "Seeds, tools, compost, protection payments and route teleports."),
        new Activity("Farming", "Birdhouse run", "Replace birdhouses on Fossil Island and collect nests, seeds and Hunter experience.", "Bone Voyage and Fossil Island access; birdhouse tier depends on Hunter and Crafting.", "Logs, clockwork, hammer, chisel, seeds and a Digsite pendant or other Fossil Island route."),
        new Activity("Farming", "Giant seaweed run", "Check underwater seaweed patches alongside birdhouses and other Fossil Island activities.", "Fossil Island access; seaweed spores and diving equipment or suitable underwater access.", "Spade, spores, compost and underwater supplies."),
        new Activity("Farming", "Farming contracts", "Complete Guildmaster Jane's requested crop contract for seed packs and Farming progression.", "Farming Guild access and the level for the requested contract crop.", "Seeds, tools, compost, payment items and a Farming Guild route."),
        new Activity("Farming", "Hespori", "Prepare a Hespori kill around the Farming Guild growth cycle and seed reward.", "65 Farming and Hespori access in the Farming Guild.", "A combat setup, food, prayer supplies, and protection against the boss's mechanics."),

        new Activity("Slayer", "Task preparation", "Check task, combat style, protection, required items and supply stock before leaving.", "Current Slayer assignment, task area and any task-specific Slayer level or quest.", "Food, potions, ammunition or runes, teleport, anti-poison and protection gear as relevant."),
        new Activity("Slayer", "Slayer point boosting", "Compare task streak milestones, preferred masters, skips and blocks before spending points.", "Slayer master access and the player's current task streak and point balance.", "Task teleports, combat supplies and enough points for the planned unlocks or skips."),
        new Activity("Slayer", "Burst and barrage tasks", "Identify eligible multi-combat tasks and compare burst, barrage and alternative methods.", "Ancient Magicks or other multi-target method, task eligibility and suitable Magic level.", "Runes, prayer potions, darts or other tagging method, gear and emergency teleport."),
        new Activity("Slayer", "Cannon tasks", "Find tasks where a dwarf multicannon is useful and compare cannon spots with alternatives.", "Dwarf Cannon quest, cannon ownership and a valid multi-combat or suitable cannon location.", "Cannon, cannonballs, ammunition, food and a route to the task area."),
        new Activity("Slayer", "Slayer boss task", "Evaluate whether the current task can be upgraded into a boss kill and whether the risk is worthwhile.", "A matching Slayer task, boss Slayer level, boss access and required gear.", "Boss-specific gear, food, potions, supplies and a safe return route."),

        new Activity("Bossing", "Boss preparation", "Build a boss-specific loadout and verify access, combat stats and supplies.", "Boss-specific quests, stats, combat mechanics and unlocks.", "Food, potions, gear, ammunition or runes, escape teleport and required access items."),
        new Activity("Bossing", "God Wars Dungeon", "Plan faction kill count, kill order, protection, entry and escape for a God Wars trip.", "God Wars Dungeon access and boss-specific gear, kill-count and protection requirements.", "Faction-appropriate gear, food, prayer restores, stamina and an escape teleport."),
        new Activity("Bossing", "Wilderness boss", "Compare solo and group options while accounting for player-killer risk and escape routes.", "Wilderness access, boss-specific requirements and an acceptable risk level.", "Low-risk gear, food, supplies, a teleport or escape method and a plan for player encounters."),
        new Activity("Bossing", "The Gauntlet and Corrupted Gauntlet", "Plan prep routes, weapon choices, Hunllef prayer switches and progression from normal to corrupted.", "Song of the Elves; higher stats and consistent prep improve reliability.", "No outside gear or supplies are required inside; bring knowledge of the prep and boss phases."),
        new Activity("Bossing", "Chambers of Xeric", "Plan a solo or group raid, room order, role coverage and Ironman gear alternatives.", "Kebos Lowlands access; combat and skilling recommendations depend on team size and raid level.", "Three combat styles, food, prayer restores, stamina and room-specific tools."),
        new Activity("Bossing", "Theatre of Blood", "Plan team roles, entry mode, gear switches and the mechanics of each encounter.", "A Taste of Hope and access to Ver Sinhaza; team experience strongly affects difficulty.", "Three combat styles, food, potions, runes and role-specific supplies."),
        new Activity("Bossing", "Tombs of Amascut", "Choose an entry level and invocation set, then build a raid-specific Ironman loadout.", "Beneath Cursed Sands and access to the Necropolis; gear and skill recommendations scale with invocations.", "Three combat styles, food, prayer restores, stamina and room-specific tools."),
        new Activity("Bossing", "Inferno", "Plan a long-form wave strategy, gear setup, supply conservation and final encounter.", "A Fire cape and high-level combat preparation are strongly recommended.", "Ranged and Magic gear, brews, restores, bastion or ranging potions and emergency planning."),

        new Activity("Skilling", "Skill progression", "Choose a target level and compare available methods against current level and unlocks.", "Method-specific level, quest and area requirements.", "Method-specific tools, materials and transport."),
        new Activity("Skilling", "Wintertodt", "Prepare warm clothing, food and inventory for solo or mass Wintertodt runs.", "50 Firemaking to enter; lower Hitpoints can reduce damage and supply needs.", "Warm clothing, food, axe, tinderbox, hammer and appropriate inventory."),
        new Activity("Skilling", "Tempoross", "Plan a solo or mass Tempoross trip around fishing, cooking, harpoon and energy phases.", "Fishing level for the chosen method and access to the Ruins of Unkah.", "Harpoon, rope, buckets, hammer, axe and food if required."),
        new Activity("Skilling", "Guardians of the Rift", "Plan essence, guardian fragments, portal usage and reward-point strategy.", "Temple of the Eye quest and Runecraft level for the preferred method.", "Pickaxe, essence pouches, talismans or tiaras and appropriate Runecraft gear."),
        new Activity("Skilling", "Blast Furnace", "Compare solo and world-based Blast Furnace methods, bars, costs and stamina use.", "Keldagrim access; stamina and coal-bag access improve some methods.", "Ores, coal bag where relevant, ice gloves, stamina and payment for the foreman if needed."),
        new Activity("Skilling", "Motherlode Mine", "Plan pay-dirt mining, sack capacity, upper-level access and Prospector progression.", "30 Mining to enter; 72 Mining for the upper level.", "Pickaxe, hammer, and optional Prospector outfit or coal bag."),
        new Activity("Skilling", "Rooftop Agility", "Choose the best unlocked rooftop course and compare marks, experience and graceful progress.", "Course-specific Agility levels and access quests.", "Stamina or energy potions, graceful outfit and course access."),
        new Activity("Skilling", "Hunter rumours", "Plan a Hunter Guild rumour rotation, creature targets and reward progression.", "Hunter Guild and Hunters' Rumours access; rumour tier depends on Hunter progression.", "Hunter equipment, teasing stick, noose wand, traps, sacks and creature-specific supplies."),
        new Activity("Skilling", "Sailing progression", "Plan Sailing unlocks, ship upgrades, routes, trials and efficient experience methods.", "Pandemonium and the current Sailing unlock progression.", "Ship materials, tools, repair supplies and route-specific gear."),

        new Activity("Quests and diaries", "Quest readiness", "Review prerequisites, skill checks, item requirements and travel before starting.", "Quest prerequisites and skill requirements must be verified against the current Wiki.", "Quest items, combat supplies and required teleports."),
        new Activity("Quests and diaries", "Achievement diary progression", "Find the highest-value diary tier that the account can complete next.", "Region-specific skill, quest and task requirements.", "Diary task items, boosts where allowed, tools and regional teleports."),
        new Activity("Quests and diaries", "Quest cape progression", "Prioritise remaining quests by unlock value, prerequisite chains and account readiness.", "All quest requirements and skill checks; some boosts may satisfy level checks.", "Quest-specific items, combat supplies and route teleports."),

        new Activity("Account progression", "Gear upgrade review", "Compare current equipment against a target activity and identify missing upgrade prerequisites.", "Requirements and Ironman source availability vary by item.", "Upgrade materials, quest unlocks, drops and crafting supplies."),
        new Activity("Account progression", "Ironman supply route", "Identify repeatable sources for food, potions, ammunition, runes and crafting materials.", "Account-specific skill, quest and area unlocks.", "Gathering tools, teleports, storage space and the materials required by the chosen route."),
        new Activity("Account progression", "Clue scroll preparation", "Plan clue tiers, required emote items, quest unlocks and efficient stash-unit coverage.", "Clue tier and step-specific quest, skill and item requirements.", "Clue items, emote gear, spade, teleports and stash-unit supplies."),
        new Activity("Account progression", "Collection log target", "Plan a collection-log grind with drop sources, prerequisites, expected time and alternative goals.", "Boss, minigame, skilling or clue-specific unlocks.", "Target-specific gear, supplies, transport and inventory space.")
    ));
}
