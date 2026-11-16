package com.nocturnal.ironman.data;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DestinationCatalog
{
    private DestinationCatalog() {}
    public static final class Destination
    {
        public final String name, activity, access;
        public final int x,y,plane;
        public Destination(String name,String activity,String access,int x,int y){this(name,activity,access,x,y,0);}
        public Destination(String name,String activity,String access,int x,int y,int plane){this.name=name;this.activity=activity;this.access=access;this.x=x;this.y=y;this.plane=plane;}
    }
    private static final List<Destination> CORE = Collections.unmodifiableList(Arrays.asList(
        new Destination("Farming Guild","Farming","Guild entrance; the herb patch itself requires 65 Farming",1249,3726),
        new Destination("Catherby herb patch","Farming","Camelot-area access; patch access and disease protection apply",2803,3463),
        new Destination("Ardougne herb patch","Farming","East Ardougne access",2670,3374),
        new Destination("Falador herb patch","Farming","Falador access",3054,3306),
        new Destination("Morytania herb patch","Farming","West of Port Phasmatys; Morytania access",3603,3527),
        new Destination("Troll Stronghold herb patch","Farming","My Arm's Big Adventure; Stony basalt additionally requires Making Friends with My Arm",2827,3692),
        new Destination("Weiss herb patch","Farming","Making Friends with My Arm; build the Fire of Nourishment; Weiss access",2848,3933),
        new Destination("Hosidius herb patch","Farming","Visit Great Kourend to access Hosidius; Easy Kourend & Kebos Diary makes the patch disease-free",1743,3557),
        new Destination("Farming Guild herb patch","Farming","65 Farming required; route point is the guild interior, not the exact patch tile; Hard Kourend & Kebos Diary adds herb yield",1260,3740),
        new Destination("Harmony Island herb patch","Farming","Elite Morytania Diary required to use this disease-free patch; Harmony Island access also requires The Great Brain Robbery",3793,2864),
        new Destination("Ortus Farm herb patch","Farming","Children of the Sun opens Varlamore; Twilight's Promise unlocks Aldarin and the farm route. Use Quetzal/Hunter Guild or fairy ring AJP, then run north-west. The patch is disease-free at Champion rank in the Fortis Colosseum; this coordinate is a route anchor, not the patch tile",1714,3110),
        new Destination("Lumbridge tree patch","Farming","Lumbridge access",3193,3233),
        new Destination("Varrock tree patch","Farming","Varrock access",3229,3459),
        new Destination("Falador tree patch","Farming","Falador access",3001,3373),
        new Destination("Taverley tree patch","Farming","Taverley access",2936,3438),
        new Destination("Gnome Stronghold fruit tree patch","Farming","Tree Gnome Stronghold access",2474,3445),
        new Destination("Catherby fruit tree patch","Farming","Catherby access",2860,3433),
        new Destination("Brimhaven fruit tree patch","Farming","Brimhaven access",2762,3212),
        new Destination("Lletya fruit tree patch","Farming","Mourning's End Part I and Lletya access",2345,3162),
        new Destination("Slayer Tower","Slayer","Morytania access",3429,3537),
        new Destination("Stronghold Slayer Cave","Slayer","Tree Gnome Stronghold access",2432,3423),
        new Destination("Catacombs of Kourend","Slayer / Bossing","Kourend access",1666,10050),
        new Destination("Taverley Dungeon","Slayer / Bossing","Taverley access",2884,9797),
        new Destination("Edgeville","General","Edgeville access",3087,3496),
        new Destination("Grand Exchange","General","Varrock access",3164,3478),
        new Destination("Warriors' Guild","General","60 Attack or 60 Strength; other entry paths may apply",2878,3546),
        new Destination("Fishing Guild","Skilling","68 Fishing or boost; other access requirements may apply",2611,3391),
        new Destination("Burthorpe","General","Burthorpe access",2899,3553),
        new Destination("Castle Wars","General","Castle Wars access",2440,3090),
        new Destination("Camelot","General","Camelot access",2757,3477),
        new Destination("Ardougne","General","Ardougne access",2662,3305),
        new Destination("Falador","General","Falador access",2965,3379),
        new Destination("Varrock","General","Varrock access",3212,3424),
        new Destination("Lumbridge","General","Lumbridge access",3222,3218),
        new Destination("Barrows","Bossing","Priest in Peril and Morytania access",3565,3312),
        new Destination("Zul-Andra","Bossing","Regicide and Zul-Andra access; sacrificial boat is required for the shrine",2200,3056),
        new Destination("Rellekka","General / Bossing","Fremennik access; Torfinn transports players to Ungael for Vorkath",2658,3661),
        new Destination("Kraken Cove","Bossing","87 Slayer and a Cave kraken/Kraken task; fairy ring AKQ is a common route",2280,3610),
        new Destination("Mount Karuulm","Bossing / Slayer","Kebos Lowlands access; heat-protection boots are required without Kourend & Kebos Elite Diary",1311,3807),
        new Destination("Weiss","Bossing / General","Making Friends with My Arm access; continue to Ghorrock Dungeon for Phantom Muspah",2848,3933),
        new Destination("Prifddinas","General / Bossing","Song of the Elves access; Gauntlet portal is north-west",3265,6066),
        new Destination("Nardah","Bossing / General","Desert access; common teleport start for Tombs of Amascut",3420,2917),
        new Destination("Giant Mole","Bossing","Falador Park; Falador Hard Diary makes the mole locator available",2996,3377),
        new Destination("Sarachnis","Bossing","Forthos Dungeon; use a crush setup and watch the web special",1845,9906),
        new Destination("TzHaar Fight Cave","Bossing","TzHaar city; Tokkul-Zo offers a direct teleport when unlocked",2445,5170),
        new Destination("Chambers of Xeric","Bossing","Raids 1 entrance in the Kebos Lowlands; Xeric's talisman can shorten the approach",1240,3550),
        new Destination("Kalphite Lair","Bossing","West of Shantay Pass; bring two ropes unless the Hard Desert Diary has secured them permanently",3226,3108),
        new Destination("Waterbirth Island","Bossing / General","Jarvald's boat from Rellekka; pay 1,000 coins if The Fremennik Trials is incomplete. Waterbirth Teleport or an appropriately enchanted lyre can land directly on the island",2546,3755),
        new Destination("God Wars Dungeon","Bossing","North of Trollheim; partial Troll Stronghold, 60 Strength or Agility and a first-entry rope are required",2881,5310),
        new Destination("Lava Maze","Bossing / Wilderness","Burning amulet landing near the KBD route; high-level Wilderness and player-killer risk",3028,3840),
        new Destination("Ghorrock Dungeon","Bossing","Enter through Weiss's Salt Mine after Secrets of the North; Ring of shadows can teleport here after its Frozen tablet unlock",2912,10336),
        new Destination("Lassar Undercity","Bossing","Access beneath the Ruins of Camdozaal; Ring of shadows destination after the Sirenic tablet unlock",2588,6435),
        new Destination("The Stranglewood","Bossing","Desert Treasure II access; Ring of shadows destination after the Strangled tablet unlock",1175,3424),
        new Destination("The Scar approach","Bossing","Reach the Temple of the Eye through the Wizard's Tower, then use the Catalytic Guardian to enter the Scar",3104,3162),
        new Destination("Sisterhood Sanctuary","Bossing","Under Slepe in Morytania; Drakan's medallion can teleport directly after its Slepey tablet unlock",3808,9700),
        new Destination("Morytania Spider Cave","Bossing / Slayer","North-east of Darkmeyer; Spider cave teleport scroll lands near the entrance",3658,3403),
        new Destination("Smoke Devil Dungeon","Slayer / Bossing","South of Castle Wars; fairy ring BKP is a nearby route",2384,3037),
        new Destination("Edgeville Dungeon","Bossing","Hill Giant and Obor route; Hill Giant key is consumed to enter Obor's arena",3097,9867),
        new Destination("Varrock Sewers","Bossing","Moss Giant route; Mossy key is consumed to enter Bryophyta's arena",3164,9882),
        new Destination("Hunter Guild","Hunter / Transport","Default-built quetzal landing; whistle access after reaching Varlamore, Renu network during Twilight's Promise",1585,3053),
        new Destination("Sunset Coast","Transport","Default-built quetzal landing; whistle charges or Renu network access required",1548,2995),
        new Destination("Auburnvale","Transport","Default-built quetzal landing; whistle charges or Renu network access required",1411,3361),
        new Destination("Quetzacalli Gorge","Transport","Default-built quetzal landing; whistle charges or Renu network access required",1510,3222),
        new Destination("Aldarin","Varlamore","Town centre route anchor; not the exact Quetzal landing tile",1391,2942),
        new Destination("The Teomat","Varlamore","Town area route anchor near Ralos' Rise",1497,3173),
        new Destination("Cam Torum","Varlamore","Town route anchor; this point is on the lower floor",1440,9568,1),
        new Destination("Outer Fortis","Varlamore","Quetzal landing area; city-side route anchor",1720,3065),
        new Destination("Kastori","Varlamore","Pendant of Ates destination",1366,3086),
        new Destination("Colossal Wyrm Remains","Varlamore","Teleport scroll destination",1641,2921)
    ));

    public static final List<Destination> ALL = buildAll();

    private static List<Destination> buildAll()
    {
        Map<String, Destination> destinations = new LinkedHashMap<>();
        for (Destination destination : CORE)
            destinations.put(destination.name.toLowerCase(Locale.ROOT), destination);

        for (TeleportCatalog.Teleport teleport : TeleportCatalog.ALL)
        {
            if (teleport.x <= 0 || teleport.y <= 0 || teleport.destination == null
                || teleport.destination.trim().isEmpty()) continue;
            String key = teleport.destination.toLowerCase(Locale.ROOT);
            destinations.putIfAbsent(key, new Destination(teleport.destination, "Transport",
                "Landing point from " + teleport.name + "; verify unlock, charge and destination-selection requirements",
                teleport.x, teleport.y, teleport.plane));
        }
        for (SpellTeleportCatalog.SpellTeleport spell : SpellTeleportCatalog.ALL)
        {
            if (spell.x <= 0 || spell.y <= 0 || spell.destination == null
                || spell.destination.trim().isEmpty()) continue;
            String key = spell.destination.toLowerCase(Locale.ROOT);
            destinations.putIfAbsent(key, new Destination(spell.destination, "Transport",
                "Spell landing point from " + spell.name + "; active spellbook, quest and rune requirements apply",
                spell.x, spell.y, 0));
        }
        return Collections.unmodifiableList(new ArrayList<>(destinations.values()));
    }
}
