package com.nocturnal.ironman.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public final class AccountSnapshot
{
    private final String playerName;
    private final String accountType;
    private final String spellbook;
    private final Map<String, Integer> realLevels;
    private final Map<String, Integer> xp;
    private final Map<Integer, Integer> inventory;
    private final Map<Integer, Integer> equipment;
    private final Map<Integer, Integer> bank;
    private final int combatLevel;
    private final int totalLevel;
    private final String location;
    private final Instant capturedAt;
    private final Instant bankCapturedAt;
    private final boolean bankOpen;
    private final ProgressSnapshot progress;
    private final SlayerTaskSnapshot slayerTask;
    private final NavigationSnapshot navigation;
    private final Map<Integer, String> itemNames;
    private final List<FarmingPatchSnapshot> farmingPatches;

    public AccountSnapshot(String playerName, Map<String, Integer> realLevels, Map<String, Integer> xp,
        Map<Integer, Integer> inventory, Map<Integer, Integer> equipment, Map<Integer, Integer> bank,
        int combatLevel, int totalLevel, String location)
    {
        this(playerName, "Unknown", "Unknown", realLevels, xp, inventory, equipment, bank, combatLevel,
            totalLevel, location, null, false, ProgressSnapshot.empty(), SlayerTaskSnapshot.empty());
    }

    public AccountSnapshot(String playerName, String accountType, Map<String, Integer> realLevels,
        Map<String, Integer> xp, Map<Integer, Integer> inventory, Map<Integer, Integer> equipment,
        Map<Integer, Integer> bank, int combatLevel, int totalLevel, String location,
        Instant bankCapturedAt, boolean bankOpen, ProgressSnapshot progress, SlayerTaskSnapshot slayerTask)
    {
        this(playerName, accountType, "Unknown", realLevels, xp, inventory, equipment, bank,
            combatLevel, totalLevel, location, bankCapturedAt, bankOpen, progress, slayerTask);
    }

    public AccountSnapshot(String playerName, String accountType, String spellbook,
        Map<String, Integer> realLevels, Map<String, Integer> xp, Map<Integer, Integer> inventory,
        Map<Integer, Integer> equipment, Map<Integer, Integer> bank, int combatLevel, int totalLevel,
        String location, Instant bankCapturedAt, boolean bankOpen, ProgressSnapshot progress,
        SlayerTaskSnapshot slayerTask)
    {
        this(playerName, accountType, spellbook, realLevels, xp, inventory, equipment, bank,
            combatLevel, totalLevel, location, bankCapturedAt, bankOpen, progress, slayerTask, null,
            Collections.emptyMap());
    }

    public AccountSnapshot(String playerName, String accountType, String spellbook,
        Map<String, Integer> realLevels, Map<String, Integer> xp, Map<Integer, Integer> inventory,
        Map<Integer, Integer> equipment, Map<Integer, Integer> bank, int combatLevel, int totalLevel,
        String location, Instant bankCapturedAt, boolean bankOpen, ProgressSnapshot progress,
        SlayerTaskSnapshot slayerTask, NavigationSnapshot navigation)
    {
        this(playerName, accountType, spellbook, realLevels, xp, inventory, equipment, bank,
            combatLevel, totalLevel, location, bankCapturedAt, bankOpen, progress, slayerTask,
            navigation, Collections.emptyMap(), Collections.emptyList());
    }

    public AccountSnapshot(String playerName, String accountType, String spellbook,
        Map<String, Integer> realLevels, Map<String, Integer> xp, Map<Integer, Integer> inventory,
        Map<Integer, Integer> equipment, Map<Integer, Integer> bank, int combatLevel, int totalLevel,
        String location, Instant bankCapturedAt, boolean bankOpen, ProgressSnapshot progress,
        SlayerTaskSnapshot slayerTask, NavigationSnapshot navigation, Map<Integer, String> itemNames)
    {
        this(playerName, accountType, spellbook, realLevels, xp, inventory, equipment, bank,
            combatLevel, totalLevel, location, bankCapturedAt, bankOpen, progress, slayerTask,
            navigation, itemNames, Collections.emptyList());
    }

    public AccountSnapshot(String playerName, String accountType, String spellbook,
        Map<String, Integer> realLevels, Map<String, Integer> xp, Map<Integer, Integer> inventory,
        Map<Integer, Integer> equipment, Map<Integer, Integer> bank, int combatLevel, int totalLevel,
        String location, Instant bankCapturedAt, boolean bankOpen, ProgressSnapshot progress,
        SlayerTaskSnapshot slayerTask, NavigationSnapshot navigation, Map<Integer, String> itemNames,
        List<FarmingPatchSnapshot> farmingPatches)
    {
        this.playerName = playerName == null ? "Unknown" : playerName;
        this.accountType = accountType == null ? "Unknown" : accountType;
        this.spellbook = spellbook == null ? "Unknown" : spellbook;
        this.realLevels = immutable(realLevels);
        this.xp = immutable(xp);
        this.inventory = immutable(inventory);
        this.equipment = immutable(equipment);
        this.bank = immutable(bank);
        this.combatLevel = combatLevel;
        this.totalLevel = totalLevel;
        this.location = location == null ? "Unknown" : location;
        this.capturedAt = Instant.now();
        this.bankCapturedAt = bankCapturedAt;
        this.bankOpen = bankOpen;
        this.progress = progress == null ? ProgressSnapshot.empty() : progress;
        this.slayerTask = slayerTask == null ? SlayerTaskSnapshot.empty() : slayerTask;
        this.navigation = navigation;
        this.itemNames = immutable(itemNames);
        this.farmingPatches = Collections.unmodifiableList(new java.util.ArrayList<>(
            farmingPatches == null ? Collections.emptyList() : farmingPatches));
    }

    private static <K, V> Map<K, V> immutable(Map<K, V> source)
    {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source == null ? Collections.emptyMap() : source));
    }

    public String getPlayerName() { return playerName; }
    public String getAccountType() { return accountType; }
    public String getSpellbook() { return spellbook; }
    public Map<String, Integer> getRealLevels() { return realLevels; }
    public Map<String, Integer> getXp() { return xp; }
    public Map<Integer, Integer> getInventory() { return inventory; }
    public Map<Integer, Integer> getEquipment() { return equipment; }
    public Map<Integer, Integer> getBank() { return bank; }
    public int getCombatLevel() { return combatLevel; }
    public int getTotalLevel() { return totalLevel; }
    public String getLocation() { return location; }
    public Instant getCapturedAt() { return capturedAt; }
    public Instant getBankCapturedAt() { return bankCapturedAt; }
    public boolean isBankOpen() { return bankOpen; }
    public ProgressSnapshot getProgress() { return progress; }
    public SlayerTaskSnapshot getSlayerTask() { return slayerTask; }
    public NavigationSnapshot getNavigation() { return navigation; }
    public Map<Integer, String> getItemNames() { return itemNames; }
    public String getItemName(int id) { return itemNames.getOrDefault(id, "Item #" + id); }
    public List<FarmingPatchSnapshot> getFarmingPatches() { return farmingPatches; }

    public int countItem(int id)
    {
        return countAccessibleItem(id) + bank.getOrDefault(id, 0);
    }

    public int countAccessibleItem(int id)
    {
        return inventory.getOrDefault(id, 0) + equipment.getOrDefault(id, 0);
    }

    public int countBankedItem(int id)
    {
        return bank.getOrDefault(id, 0);
    }

    public boolean ownsAny(int... ids)
    {
        for (int id : ids) if (countItem(id) > 0) return true;
        return false;
    }
}
