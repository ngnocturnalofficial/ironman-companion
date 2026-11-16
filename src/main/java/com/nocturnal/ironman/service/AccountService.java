package com.nocturnal.ironman.service;

import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.ProgressSnapshot;
import com.nocturnal.ironman.model.NavigationSnapshot;
import com.nocturnal.ironman.model.SlayerTaskSnapshot;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.WorldView;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

public class AccountService
{
    private static final String CONFIG_GROUP = "ironmancompanion";
    private static final String BANK_CACHE_KEY = "bankCacheV1";

    private final Client client;
    private final ConfigManager configManager;
    private final ProgressService progressService;
    private final SlayerService slayerService;
    private final ItemManager itemManager;
    private final FarmingService farmingService;
    private final Map<Integer, String> itemNameCache = new LinkedHashMap<>();
    private AccountSnapshot latest;
    private Map<Integer, Integer> bankCache = new LinkedHashMap<>();
    private Instant bankCapturedAt;
    private ProgressSnapshot progressCache = ProgressSnapshot.empty();
    private boolean progressCaptured;
    private boolean lastBankOpen;

    @Inject
    public AccountService(Client client, ConfigManager configManager, ProgressService progressService,
        SlayerService slayerService, ItemManager itemManager, FarmingService farmingService)
    {
        this.client = client;
        this.configManager = configManager;
        this.progressService = progressService;
        this.slayerService = slayerService;
        this.itemManager = itemManager;
        this.farmingService = farmingService;
    }

    public void reloadBankCache()
    {
        farmingService.resetRegion();
        latest = null;
        itemNameCache.clear();
        bankCache = new LinkedHashMap<>();
        bankCapturedAt = null;
        progressCaptured = false;
        lastBankOpen = false;
        String raw = configManager.getRSProfileConfiguration(CONFIG_GROUP, BANK_CACHE_KEY);
        if (raw == null || raw.trim().isEmpty()) return;

        try
        {
            String[] parts = raw.split("\\n", 2);
            bankCapturedAt = Instant.parse(parts[0].trim());
            if (parts.length > 1 && !parts[1].trim().isEmpty())
            {
                for (String item : parts[1].split(","))
                {
                    String[] pair = item.split(":", 2);
                    if (pair.length != 2) continue;
                    int id = Integer.parseInt(pair[0]);
                    int quantity = Integer.parseInt(pair[1]);
                    if (id > 0 && quantity > 0) bankCache.put(id, quantity);
                }
            }
        }
        catch (RuntimeException ex)
        {
            bankCache = new LinkedHashMap<>();
            bankCapturedAt = null;
        }
    }

    public void clearSession()
    {
        latest = null;
        bankCache = new LinkedHashMap<>();
        bankCapturedAt = null;
        progressCache = ProgressSnapshot.empty();
        progressCaptured = false;
        lastBankOpen = false;
        itemNameCache.clear();
        farmingService.resetRegion();
    }

    public AccountSnapshot capture()
    {
        return capture(false, false);
    }

    public AccountSnapshot capture(boolean forceBankSync, boolean refreshProgress)
    {
        Map<String, Integer> levels = new LinkedHashMap<>();
        Map<String, Integer> xp = new LinkedHashMap<>();
        for (Skill skill : Skill.values())
        {
            if (skill == Skill.OVERALL) continue;
            levels.put(skill.getName(), client.getRealSkillLevel(skill));
            xp.put(skill.getName(), client.getSkillExperience(skill));
        }

        Player player = client.getLocalPlayer();
        String name = player == null ? "Not logged in" : player.getName();
        String location = "Unknown";
        if (player != null)
        {
            WorldPoint point = player.getWorldLocation();
            location = point.getX() + ", " + point.getY() + " (plane " + point.getPlane() + ")";
        }

        boolean bankOpen = isBankOpen();
        boolean bankJustOpened = bankOpen && !lastBankOpen;
        lastBankOpen = bankOpen;
        if (bankOpen)
        {
            ItemContainer container = client.getItemContainer(InventoryID.BANK);
            if (container != null)
            {
                Map<Integer, Integer> currentBank = readContainer(container);
                if (forceBankSync || bankJustOpened || bankCapturedAt == null || !currentBank.equals(bankCache))
                {
                    bankCache = currentBank;
                    bankCapturedAt = Instant.now();
                    saveBankCache();
                }
            }
        }

        if (refreshProgress || !progressCaptured)
        {
            progressCache = progressService.capture();
            progressCaptured = true;
        }

        SlayerTaskSnapshot slayer = slayerService.capture();
        Map<Integer, Integer> inventory = readContainer(InventoryID.INVENTORY);
        Map<Integer, Integer> equipment = readContainer(InventoryID.EQUIPMENT);
        Map<Integer, String> itemNames = captureItemNames(inventory, equipment, bankCache);
        latest = new AccountSnapshot(name, accountTypeName(client.getVarbitValue(Varbits.ACCOUNT_TYPE)),
            spellbookName(client.getVarbitValue(Varbits.SPELLBOOK)), levels, xp,
            inventory, equipment, bankCache, player == null ? 0 : player.getCombatLevel(), client.getTotalLevel(), location,
            bankCapturedAt, bankOpen, progressCache, slayer, captureNavigation(), itemNames,
            farmingService.capture(), captureTravelSettings());
        return latest;
    }

    private Map<String, Integer> captureTravelSettings()
    {
        Map<String, Integer> settings = new LinkedHashMap<>();
        settings.put("varrockGe", client.getVarbitValue(net.runelite.api.gameval.VarbitID.VARROCK_GE_TELEPORT));
        settings.put("seersCamelot", client.getVarbitValue(net.runelite.api.gameval.VarbitID.SEERS_CAMELOT_TELEPORT));
        settings.put("yanille", client.getVarbitValue(net.runelite.api.gameval.VarbitID.YANILLE_TELEPORT_LOCATION));
        settings.put("ringLassar", client.getVarbitValue(net.runelite.api.gameval.VarbitID.DT2_LASSAR_TELEPORT));
        settings.put("ringGhorrock", client.getVarbitValue(net.runelite.api.gameval.VarbitID.DT2_GHORROCK_TELEPORT));
        settings.put("ringScar", client.getVarbitValue(net.runelite.api.gameval.VarbitID.DT2_SCAR_TELEPORT));
        settings.put("ringStranglewood", client.getVarbitValue(net.runelite.api.gameval.VarbitID.DT2_STRANGLEWOOD_TELEPORT));
        settings.put("fairyPermission", client.getVarbitValue(net.runelite.api.gameval.VarbitID.FAIRYRING_PERMISSION));
        settings.put("visitedKourend", client.getVarbitValue(net.runelite.api.gameval.VarbitID.ZEAH_PLAYERHASVISITED));
        settings.put("visitedVarlamore", client.getVarbitValue(net.runelite.api.gameval.VarbitID.VARLAMORE_VISITED));
        settings.put("visitedGrimstone", client.getVarbitValue(net.runelite.api.gameval.VarbitID.GRIMSTONE_VISITED));
        settings.put("colosseumGlory", client.getVarpValue(net.runelite.api.gameval.VarPlayerID.COLOSSEUM_GLORY));
        settings.put("quetzalNetwork", client.getVarpValue(net.runelite.api.gameval.VarPlayerID.QUETZALS_UNLOCKED));
        settings.put("quetzalCamTorum", client.getVarbitValue(net.runelite.api.gameval.VarbitID.QUETZAL_CAMTORUM));
        settings.put("quetzalColossalWyrm", client.getVarbitValue(net.runelite.api.gameval.VarbitID.QUETZAL_COLOSSALWYRM));
        settings.put("quetzalColosseum", client.getVarbitValue(net.runelite.api.gameval.VarbitID.QUETZAL_COLOSSEUM));
        settings.put("quetzalKastori", client.getVarbitValue(net.runelite.api.gameval.VarbitID.QUETZAL_KASTORI));
        settings.put("quetzalOuterFortis", client.getVarbitValue(net.runelite.api.gameval.VarbitID.QUETZAL_OUTERFORTIS));
        settings.put("quetzalSalvagerOverlook", client.getVarbitValue(net.runelite.api.gameval.VarbitID.QUETZAL_SALVAGEROVERLOOK));
        return settings;
    }

    private boolean isBankOpen()
    {
        Widget widget = client.getWidget(ComponentID.BANK_CONTAINER);
        return widget != null && !widget.isHidden();
    }

    private void saveBankCache()
    {
        if (bankCapturedAt == null) return;
        StringBuilder value = new StringBuilder(bankCapturedAt.toString()).append('\n');
        boolean first = true;
        for (Map.Entry<Integer, Integer> entry : bankCache.entrySet())
        {
            if (!first) value.append(',');
            value.append(entry.getKey()).append(':').append(entry.getValue());
            first = false;
        }
        configManager.setRSProfileConfiguration(CONFIG_GROUP, BANK_CACHE_KEY, value.toString());
    }

    private String spellbookName(int value)
    {
        switch (value)
        {
            case 0: return "Standard";
            case 1: return "Ancient";
            case 2: return "Lunar";
            case 3: return "Arceuus";
            default: return "Unknown (" + value + ")";
        }
    }

    private String accountTypeName(int value)
    {
        switch (value)
        {
            case 0: return "Normal";
            case 1: return "Ironman";
            case 2: return "Ultimate Ironman";
            case 3: return "Hardcore Ironman";
            case 4: return "Group Ironman";
            case 5: return "Hardcore Group Ironman";
            case 6: return "Unranked Group Ironman";
            default: return "Unknown (" + value + ")";
        }
    }


    private Map<Integer, String> captureItemNames(Map<Integer, Integer> inventory,
        Map<Integer, Integer> equipment, Map<Integer, Integer> bank)
    {
        Set<Integer> ids = new LinkedHashSet<>();
        ids.addAll(inventory.keySet());
        ids.addAll(equipment.keySet());
        ids.addAll(bank.keySet());

        Map<Integer, String> names = new LinkedHashMap<>();
        for (int id : ids)
        {
            names.put(id, itemNameCache.computeIfAbsent(id, this::resolveItemName));
        }
        return names;
    }

    private String resolveItemName(int id)
    {
        try
        {
            String name = itemManager.getItemComposition(id).getName();
            return name == null || name.trim().isEmpty() ? "Item #" + id : name;
        }
        catch (RuntimeException ex)
        {
            return "Item #" + id;
        }
    }

    private NavigationSnapshot captureNavigation()
    {
        if (client.getLocalPlayer() == null) return null;
        WorldPoint location = client.getLocalPlayer().getWorldLocation();
        if (location == null) return null;
        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null) return new NavigationSnapshot(location, 0, 0, null);
        int[][] flags = null;
        CollisionData[] maps = worldView.getCollisionMaps();
        int plane = location.getPlane();
        if (maps != null && plane >= 0 && plane < maps.length && maps[plane] != null)
            flags = maps[plane].getFlags();
        return new NavigationSnapshot(location, worldView.getBaseX(), worldView.getBaseY(), flags);
    }

    private Map<Integer, Integer> readContainer(InventoryID id)
    {
        return readContainer(client.getItemContainer(id));
    }

    private Map<Integer, Integer> readContainer(ItemContainer container)
    {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        if (container == null) return result;
        for (Item item : container.getItems())
        {
            if (item != null && item.getId() > 0 && item.getQuantity() > 0)
                result.merge(item.getId(), item.getQuantity(), Integer::sum);
        }
        return result;
    }

    public AccountSnapshot getLatest() { return latest; }
}
