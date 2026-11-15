package com.nocturnal.ironman.service;

import com.nocturnal.ironman.model.FarmingPatchSnapshot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;

/** Tracks herb patch states when their regions are loaded and estimates growth between visits. */
public class FarmingService
{
    private static final String CONFIG_GROUP = "ironmancompanion";
    private static final String CONFIG_PREFIX = "farmingPatch.";
    private static final long HERB_GROWTH_TICK_MINUTES = 20;
    private static final int HERB_GROWTH_STAGES = 4;

    private static final List<PatchDefinition> PATCHES = Collections.unmodifiableList(Arrays.asList(
        patch("Ardougne herb patch", VarbitID.FARMING_TRANSMIT_D, 10548),
        patch("Catherby herb patch", VarbitID.FARMING_TRANSMIT_D, 11062, 11061, 11318, 11317),
        patch("Falador herb patch", VarbitID.FARMING_TRANSMIT_D, 12083),
        patch("Morytania herb patch", VarbitID.FARMING_TRANSMIT_D, 14391, 14390),
        patch("Hosidius herb patch", VarbitID.FARMING_TRANSMIT_D, 6967, 6711),
        patch("Harmony Island herb patch", VarbitID.FARMING_TRANSMIT_B, 15148),
        patch("Troll Stronghold herb patch", VarbitID.FARMING_TRANSMIT_A, 11321),
        patch("Weiss herb patch", VarbitID.FARMING_TRANSMIT_A, 11325),
        patch("Farming Guild herb patch", VarbitID.FARMING_TRANSMIT_E,
            4922, 5177, 5178, 5179, 4921, 4923, 4665, 4666, 4667),
        patch("Ortus Farm herb patch", VarbitID.FARMING_TRANSMIT_D,
            6192, 6447, 6448, 6449, 6191, 6193)
    ));

    private final Client client;
    private final ConfigManager configManager;
    private int pendingRegionId = Integer.MIN_VALUE;
    private int pendingRegionTick;

    @Inject
    public FarmingService(Client client, ConfigManager configManager)
    {
        this.client = client;
        this.configManager = configManager;
    }

    public void resetRegion()
    {
        pendingRegionId = Integer.MIN_VALUE;
    }

    public List<FarmingPatchSnapshot> capture()
    {
        Player player = client.getLocalPlayer();
        if (player != null && client.getGameState() == GameState.LOGGED_IN)
        {
            WorldPoint location = player.getWorldLocation();
            if (location == null) return snapshots(Instant.now());
            int regionId = location.getRegionID();
            if (regionId != pendingRegionId)
            {
                pendingRegionId = regionId;
                pendingRegionTick = client.getTickCount();
            }
            else if (client.getTickCount() > pendingRegionTick)
            {
                for (PatchDefinition patch : PATCHES)
                {
                    if (!patch.isApplicable(location)) continue;
                    int value = client.getVarbitValue(patch.varbit);
                    persistIfChanged(patch, value);
                }
            }
        }

        return snapshots(Instant.now());
    }

    private List<FarmingPatchSnapshot> snapshots(Instant now)
    {
        List<FarmingPatchSnapshot> result = new ArrayList<>();
        for (PatchDefinition patch : PATCHES)
        {
            StoredState stored = readStored(patch);
            result.add(toSnapshot(patch, stored, now));
        }
        return Collections.unmodifiableList(result);
    }

    private void persistIfChanged(PatchDefinition patch, int value)
    {
        String key = CONFIG_PREFIX + patch.key;
        String raw = configManager.getRSProfileConfiguration(CONFIG_GROUP, key);
        StoredState previous = parse(raw);
        if (previous == null || previous.value != value)
        {
            String next = value + ":" + Instant.now().getEpochSecond();
            configManager.setRSProfileConfiguration(CONFIG_GROUP, key, next);
        }
    }

    private StoredState readStored(PatchDefinition patch)
    {
        return parse(configManager.getRSProfileConfiguration(CONFIG_GROUP, CONFIG_PREFIX + patch.key));
    }

    private StoredState parse(String raw)
    {
        if (raw == null) return null;
        String[] parts = raw.split(":", -1);
        if (parts.length != 2) return null;
        try
        {
            return new StoredState(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
        }
        catch (NumberFormatException ex)
        {
            return null;
        }
    }

    private FarmingPatchSnapshot toSnapshot(PatchDefinition patch, StoredState stored, Instant now)
    {
        if (stored == null)
        {
            return new FarmingPatchSnapshot(patch.name, "Unknown", "Not observed", 0, 0, null, null);
        }

        HerbState state = decode(stored.value);
        Instant observedAt = Instant.ofEpochSecond(stored.timestamp);
        if (state == null)
        {
            return new FarmingPatchSnapshot(patch.name, "Unknown", "Unknown state", 0, 0, observedAt, null);
        }

        Instant readyAt = null;
        if ("Growing".equals(state.state))
        {
            long remainingStages = Math.max(0, HERB_GROWTH_STAGES - state.stage);
            readyAt = observedAt.plusSeconds(remainingStages * HERB_GROWTH_TICK_MINUTES * 60);
        }
        else if ("Ready".equals(state.state))
        {
            readyAt = observedAt;
        }

        return new FarmingPatchSnapshot(patch.name, state.crop, state.state, state.stage,
            state.stages, observedAt, readyAt);
    }

    static HerbState decode(int value)
    {
        if (value >= 0 && value <= 3) return weeds(3 - value);
        if (value == 67 || between(value, 176, 191) || between(value, 204, 219) || between(value, 221, 255))
            return weeds(3);

        HerbState state;
        state = growing(value, 4, 7, "Guam", 4); if (state != null) return state;
        state = ready(value, 8, 10, "Guam", 10); if (state != null) return state;
        state = growing(value, 11, 14, "Marrentill", 11); if (state != null) return state;
        state = ready(value, 15, 17, "Marrentill", 17); if (state != null) return state;
        state = growing(value, 18, 21, "Tarromin", 18); if (state != null) return state;
        state = ready(value, 22, 24, "Tarromin", 24); if (state != null) return state;
        state = growing(value, 25, 28, "Harralander", 25); if (state != null) return state;
        state = ready(value, 29, 31, "Harralander", 31); if (state != null) return state;
        state = growing(value, 32, 35, "Ranarr", 32); if (state != null) return state;
        state = ready(value, 36, 38, "Ranarr", 38); if (state != null) return state;
        state = growing(value, 39, 42, "Toadflax", 39); if (state != null) return state;
        state = ready(value, 43, 45, "Toadflax", 45); if (state != null) return state;
        state = growing(value, 46, 49, "Irit", 46); if (state != null) return state;
        state = ready(value, 50, 52, "Irit", 52); if (state != null) return state;
        state = growing(value, 53, 56, "Avantoe", 53); if (state != null) return state;
        state = ready(value, 57, 59, "Avantoe", 59); if (state != null) return state;
        state = growing(value, 60, 63, "Huasca", 60); if (state != null) return state;
        state = ready(value, 64, 66, "Huasca", 66); if (state != null) return state;
        state = growing(value, 68, 71, "Kwuarm", 68); if (state != null) return state;
        state = ready(value, 72, 74, "Kwuarm", 74); if (state != null) return state;
        state = growing(value, 75, 78, "Snapdragon", 75); if (state != null) return state;
        state = ready(value, 79, 81, "Snapdragon", 81); if (state != null) return state;
        state = growing(value, 82, 85, "Cadantine", 82); if (state != null) return state;
        state = ready(value, 86, 88, "Cadantine", 88); if (state != null) return state;
        state = growing(value, 89, 92, "Lantadyme", 89); if (state != null) return state;
        state = ready(value, 93, 95, "Lantadyme", 95); if (state != null) return state;
        state = growing(value, 96, 99, "Dwarf weed", 96); if (state != null) return state;
        state = ready(value, 100, 102, "Dwarf weed", 102); if (state != null) return state;
        state = growing(value, 103, 106, "Torstol", 103); if (state != null) return state;
        state = ready(value, 107, 109, "Torstol", 109); if (state != null) return state;

        state = diseased(value, 128, 130, "Guam", 127); if (state != null) return state;
        state = diseased(value, 131, 133, "Marrentill", 130); if (state != null) return state;
        state = diseased(value, 134, 136, "Tarromin", 133); if (state != null) return state;
        state = diseased(value, 137, 139, "Harralander", 136); if (state != null) return state;
        state = diseased(value, 140, 142, "Ranarr", 139); if (state != null) return state;
        state = diseased(value, 143, 145, "Toadflax", 142); if (state != null) return state;
        state = diseased(value, 146, 148, "Irit", 145); if (state != null) return state;
        state = diseased(value, 149, 151, "Avantoe", 148); if (state != null) return state;
        state = diseased(value, 152, 154, "Kwuarm", 151); if (state != null) return state;
        state = diseased(value, 155, 157, "Snapdragon", 154); if (state != null) return state;
        state = diseased(value, 158, 160, "Cadantine", 157); if (state != null) return state;
        state = diseased(value, 161, 163, "Lantadyme", 160); if (state != null) return state;
        state = diseased(value, 164, 166, "Dwarf weed", 163); if (state != null) return state;
        state = diseased(value, 167, 169, "Torstol", 166); if (state != null) return state;
        if (between(value, 170, 172))
            return new HerbState("Any herb", "Dead", value - 169, 3);
        state = diseased(value, 173, 175, "Huasca", 172); if (state != null) return state;

        state = growing(value, 192, 195, "Goutweed", 192); if (state != null) return state;
        state = ready(value, 196, 197, "Goutweed", 197, 2); if (state != null) return state;
        state = diseased(value, 198, 200, "Goutweed", 197); if (state != null) return state;
        if (between(value, 201, 203)) return new HerbState("Goutweed", "Dead", value - 200, 3);
        return null;
    }

    private static HerbState weeds(int stage)
    {
        return new HerbState("Weeds", "Needs clearing", stage, 4);
    }

    private static HerbState growing(int value, int min, int max, String crop, int offset)
    {
        return between(value, min, max) ? new HerbState(crop, "Growing", value - offset, HERB_GROWTH_STAGES) : null;
    }

    private static HerbState ready(int value, int min, int max, String crop, int reverseOffset)
    {
        return ready(value, min, max, crop, reverseOffset, 3);
    }

    private static HerbState ready(int value, int min, int max, String crop, int reverseOffset, int stages)
    {
        return between(value, min, max) ? new HerbState(crop, "Ready", reverseOffset - value, stages) : null;
    }

    private static HerbState diseased(int value, int min, int max, String crop, int offset)
    {
        return between(value, min, max) ? new HerbState(crop, "Diseased", value - offset, 3) : null;
    }

    private static boolean between(int value, int min, int max)
    {
        return value >= min && value <= max;
    }

    private static PatchDefinition patch(String name, int varbit, int... regions)
    {
        Set<Integer> ids = new HashSet<>();
        for (int region : regions) ids.add(region);
        return new PatchDefinition(name, varbit, ids);
    }

    static boolean isPatchRegion(String patchName, int regionId)
    {
        return PATCHES.stream().anyMatch(patch -> patch.name.equals(patchName)
            && patch.regionIds.contains(regionId));
    }

    static boolean isFaladorHerbPatchInBounds(WorldPoint location)
    {
        return location != null && location.getY() >= 3272;
    }

    static boolean isCatherbyHerbPatchInBounds(WorldPoint location)
    {
        if (location == null) return false;
        if (location.getX() >= 2816 && location.getY() < 3456)
        {
            return location.getX() < 2840 && location.getY() >= 3440
                && location.getPlane() == 0;
        }
        return true;
    }

    private static final class PatchDefinition
    {
        private final String name;
        private final String key;
        private final int varbit;
        private final Set<Integer> regionIds;

        private PatchDefinition(String name, int varbit, Set<Integer> regionIds)
        {
            this.name = name;
            this.key = name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
            this.varbit = varbit;
            this.regionIds = Collections.unmodifiableSet(new HashSet<>(regionIds));
        }

        private boolean isApplicable(WorldPoint location)
        {
            int regionId = location.getRegionID();
            if (!regionIds.contains(regionId)) return false;

            // RuneLite shares region 12083 with Port Sarim's spirit tree patch.
            if ("Falador herb patch".equals(name) && regionId == 12083)
                return isFaladorHerbPatchInBounds(location);

            // The Catherby region also covers the upstairs fruit-tree area.
            if ("Catherby herb patch".equals(name))
                return isCatherbyHerbPatchInBounds(location);
            return true;
        }
    }

    private static final class StoredState
    {
        private final int value;
        private final long timestamp;

        private StoredState(int value, long timestamp)
        {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    static final class HerbState
    {
        final String crop;
        final String state;
        final int stage;
        final int stages;

        private HerbState(String crop, String state, int stage, int stages)
        {
            this.crop = crop;
            this.state = state;
            this.stage = stage;
            this.stages = stages;
        }
    }
}
