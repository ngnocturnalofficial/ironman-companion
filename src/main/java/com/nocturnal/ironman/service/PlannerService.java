package com.nocturnal.ironman.service;

import com.nocturnal.ironman.data.DestinationCatalog;
import com.nocturnal.ironman.data.FairyRingCatalog;
import com.nocturnal.ironman.data.QuetzalCatalog;
import com.nocturnal.ironman.data.BossCatalog;
import com.nocturnal.ironman.data.SkillProgressionCatalog;
import com.nocturnal.ironman.data.SpellTeleportCatalog;
import com.nocturnal.ironman.data.TeleportCatalog;
import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.model.NavigationSnapshot;
import com.nocturnal.ironman.model.FarmingPatchSnapshot;
import com.nocturnal.ironman.navigation.ScenePathfinder;
import com.nocturnal.ironman.navigation.ScenePathfinder.PathResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.coords.WorldPoint;

public class PlannerService
{
    private final ScenePathfinder pathfinder;
    private AccountSnapshot cachedSnapshot;
    private final Map<String, TeleportCounts> teleportCounts = new HashMap<>();

    @Inject
    public PlannerService(ScenePathfinder pathfinder)
    {
        this.pathfinder = pathfinder;
    }

    public synchronized List<String> teleports(AccountSnapshot account)
    {
        return teleports(account, true);
    }

    public synchronized List<String> teleports(AccountSnapshot account, boolean includeBank)
    {
        List<String> ready = new ArrayList<>();
        List<String> banked = new ArrayList<>();
        if (account == null) return ready;
        ensureTeleportCounts(account);

        for (TeleportCatalog.Teleport teleport : TeleportCatalog.ALL)
        {
            int carried = countAccessible(account, teleport);
            int stored = includeBank ? countBanked(account, teleport) : 0;
            if (carried > 0)
                ready.add(teleport.name + " → " + teleport.destination + " × " + carried + " (in inventory/equipment; " + teleport.notes + ")");
            if (stored > 0)
                banked.add(teleport.name + " → " + teleport.destination + " × " + stored + " (banked; withdraw first)");
        }

        ready.sort(String.CASE_INSENSITIVE_ORDER);
        banked.sort(String.CASE_INSENSITIVE_ORDER);
        List<String> result = new ArrayList<>();
        result.add("CARRIED TELEPORT OPTIONS — check required charges and destination settings");
        result.addAll(ready.isEmpty() ? java.util.Collections.singletonList("No catalogued teleport item detected in inventory or equipment.") : ready);
        result.add("");
        result.add(includeBank ? "BANKED — owned, but not currently usable" : "BANKED — omitted by the AI bank-context setting");
        if (includeBank)
            result.addAll(banked.isEmpty() ? java.util.Collections.singletonList("No additional catalogued teleport item detected in the saved bank snapshot.") : banked);
        result.add("\nSPELLBOOK TELEPORTS — " + account.getSpellbook());
        List<SpellTeleportCatalog.SpellTeleport> spells = SpellTeleportCatalog.available(account);
        if (spells.isEmpty()) result.add("No catalogued spell teleports available for the active spellbook and detected quest/level requirements.");
        else
            for (SpellTeleportCatalog.SpellTeleport spell : spells)
                result.add(spell.name + " → " + spell.destination + " (" + spell.magicLevel + " Magic; " + spell.notes + ")");
        result.add("\nFAIRY RING NETWORK");
        result.add(fairyRingAccessStatus(account, includeBank));
        result.add("Destination-specific access still applies; a built POH fairy ring is not detectable from this account snapshot.");
        result.add("Additional realm/house fairy codes without fixed world-map landings: AJQ (Dorgesh-Kaan cave), BJR (Fisher King), BKQ (Enchanted Valley), BLQ (Yu'biusk), CKP (Cosmic plane), DIQ (built POH), DIR (Gorak's Plane), DLS (Myreque hideout). Some require quests or a built ring and cannot be spatially routed from the current snapshot.");
        result.add("\nSpell options are checked against spellbook, Magic and known quest completion; required runes/rune-pouch contents are not verified. Unknown destination unlocks are withheld. Actual remaining item charges and some optional unlocks require in-game confirmation.");
        return result;
    }

    public synchronized List<String> routeSuggestions(AccountSnapshot account, String destination)
    {
        List<String> suggestions = new ArrayList<>();
        if (account == null) return suggestions;
        ensureTeleportCounts(account);
        String query = destination == null ? "" : destination.trim();
        if (query.isEmpty())
        {
            suggestions.add("Enter a destination name. The catalog includes quest locations, farm patches, Slayer areas, boss rooms, transport landings and major hubs.");
            return suggestions;
        }
        DestinationCatalog.Destination target = DestinationCatalog.ALL.stream()
            .filter(d -> d.name.equalsIgnoreCase(query)).findFirst().orElse(null);
        if (target == null)
        {
            List<DestinationCatalog.Destination> matches = DestinationCatalog.ALL.stream()
                .filter(d -> d.name.toLowerCase(java.util.Locale.ROOT).contains(query.toLowerCase(java.util.Locale.ROOT)))
                .collect(java.util.stream.Collectors.toList());
            if (matches.size() == 1) target = matches.get(0);
            else if (!matches.isEmpty())
            {
                suggestions.add("Multiple destinations match '" + query + "'. Enter one of these exact names:");
                for (int i = 0; i < Math.min(8, matches.size()); i++) suggestions.add(matches.get(i).name);
                return suggestions;
            }
            else
            {
                suggestions.add("Destination not in the local landmark catalog. Try a named farm patch, Slayer area, guild, transport landing or major city; otherwise ask the Wiki assistant.");
                return suggestions;
            }
        }

        WorldPoint targetPoint = new WorldPoint(target.x, target.y, target.plane);
        suggestions.add("DESTINATION: " + target.name + " (" + target.activity + ")");
        suggestions.add("Access: " + target.access);
        if ("Zul-Andra".equals(target.name)
            && !"FINISHED".equals(account.getProgress().getQuests().get("Regicide")))
        {
            suggestions.add("LOCKED: Complete Regicide before travelling to Zul-Andra or Zulrah.");
            return suggestions;
        }

        NavigationSnapshot navigation = account.getNavigation();
        if (navigation != null && navigation.getPlayerLocation() != null)
        {
            WorldPoint current = navigation.getPlayerLocation();
            PathResult walking = pathfinder.findPath(navigation, targetPoint);
            if (walking.isFound())
            {
                suggestions.add("\nCURRENT-SCENE WALK ROUTE: " + walking.getSteps() + " collision-checked steps");
                suggestions.addAll(walking.getInstructions());
            }
            else if (walking.isTargetOutsideScene())
            {
                suggestions.add("\nThe destination is outside the loaded 104×104 scene. Global collision-aware routing is not available from the current client scene.");
                if (current.getPlane() == targetPoint.getPlane())
                    suggestions.add("Direct tile lower bound from current position: " + current.distanceTo2D(targetPoint) + " tiles (not a path length).");
            }
            else
            {
                suggestions.add("\nLocal pathfinding unavailable: " + walking.getMessage());
                if (current.getPlane() == targetPoint.getPlane())
                    suggestions.add("Direct tile lower bound: " + current.distanceTo2D(targetPoint) + " tiles (not a path length).");
            }
        }
        else
        {
            suggestions.add("\nCurrent player location is unavailable in the captured snapshot.");
        }

        List<TeleportCandidate> ready = new ArrayList<>();
        List<TeleportCandidate> banked = new ArrayList<>();
        for (TeleportCatalog.Teleport teleport : TeleportCatalog.ALL)
        {
            if (teleport.x <= 0 || teleport.y <= 0) continue;
            int distance = new WorldPoint(teleport.x, teleport.y, teleport.plane).distanceTo2D(targetPoint);
            int carried = countAccessible(account, teleport);
            int stored = countBanked(account, teleport);
            if (carried > 0) ready.add(new TeleportCandidate(teleport, distance, carried));
            if (stored > 0) banked.add(new TeleportCandidate(teleport, distance, stored));
        }
        Comparator<TeleportCandidate> byDistance = Comparator.comparingInt(candidate -> candidate.distance);
        ready.sort(byDistance);
        banked.sort(byDistance);

        suggestions.add("\nBEST TELEPORT STARTS — carried/equipped");
        if (ready.isEmpty()) suggestions.add("No catalogued teleport item is currently carried or equipped.");
        else
        {
            for (int i = 0; i < Math.min(4, ready.size()); i++)
            {
                TeleportCandidate candidate = ready.get(i);
                suggestions.add((i + 1) + ". " + candidate.teleport.name + " → " + candidate.teleport.destination
                    + " (" + candidate.count + " carried); about " + candidate.distance + " tiles from landing point to target (Chebyshev lower bound).");
            }
        }

        if (!banked.isEmpty())
        {
            suggestions.add("\nBANKED TELEPORT OPTIONS — withdraw before leaving");
            for (int i = 0; i < Math.min(3, banked.size()); i++)
            {
                TeleportCandidate candidate = banked.get(i);
                suggestions.add(candidate.teleport.name + " → " + candidate.teleport.destination
                    + " (" + candidate.count + " banked); about " + candidate.distance + " tiles from landing point to target.");
            }
        }

        List<SpellTeleportCatalog.SpellTeleport> spellCandidates = new ArrayList<>();
        for (SpellTeleportCatalog.SpellTeleport spell : SpellTeleportCatalog.available(account))
            if (spell.x > 0 && spell.y > 0) spellCandidates.add(spell);
        spellCandidates.sort(Comparator.comparingInt(spell -> new WorldPoint(spell.x, spell.y, 0).distanceTo2D(targetPoint)));
        if (!spellCandidates.isEmpty())
        {
            suggestions.add("\nBEST SPELLBOOK TELEPORT OPTIONS — " + account.getSpellbook() + " (runes not verified)");
            for (int i = 0; i < Math.min(4, spellCandidates.size()); i++)
            {
                SpellTeleportCatalog.SpellTeleport spell = spellCandidates.get(i);
                int distance = new WorldPoint(spell.x, spell.y, 0).distanceTo2D(targetPoint);
                suggestions.add(spell.name + " → " + spell.destination + "; about " + distance + " tiles from landing point to target.");
            }
        }

        appendGlobalTravelChains(suggestions, account, target, navigation, ready, banked, spellCandidates);

        suggestions.add("\nFAIRY RING NETWORK");
        suggestions.add(fairyRingAccessStatus(account));
        if (hasCarriedFairyRingAccess(account))
        {
            FairyRingCatalog.Ring nearestEntry = nearestSceneFairyRing(account, navigation);
            boolean entryIsSceneReachable = nearestEntry != null;
            if (nearestEntry == null)
                nearestEntry = FairyRingCatalog.nearestUnlocked(account,
                    navigation == null ? null : navigation.getPlayerLocation());
            FairyRingCatalog.Ring nearestLanding = FairyRingCatalog.nearestUnlockedLanding(account, targetPoint, target.name);
            if (nearestLanding != null && nearestLanding.location.distanceTo2D(targetPoint) > 450)
                nearestLanding = null;
            if (nearestEntry != null && nearestLanding != null && navigation != null
                && navigation.getPlayerLocation() != null)
            {
                int toEntry = navigation.getPlayerLocation().distanceTo2D(nearestEntry.location);
                int toTarget = nearestLanding.location.distanceTo2D(targetPoint);
                suggestions.add("Best network option: reach the "
                    + (entryIsSceneReachable ? "nearest reachable ring in the current scene" : "nearest catalogued ring (straight-line estimate)")
                    + " (" + nearestEntry.code + ", about " + toEntry + " tiles from your current position), then dial " + nearestLanding.code
                    + " (landing about " + toTarget + " tiles from the destination).");
                PathResult ringApproach = pathfinder.findPath(navigation, nearestEntry.location);
                if (ringApproach.isFound())
                {
                    suggestions.add("Local collision-checked approach to the entry ring: " + ringApproach.getSteps() + " steps");
                    suggestions.addAll(ringApproach.getInstructions());
                }
            }
            else if (nearestLanding != null)
            {
                int toTarget = nearestLanding.location.distanceTo2D(targetPoint);
                suggestions.add("Dial " + nearestLanding.code + " to land about " + toTarget
                    + " tiles from the destination; current player location is unavailable for the approach leg.");
            }
        }
        else if (hasPotentialHouseFairyRing(account))
        {
            suggestions.add("A POH fairy ring may be usable if one has been built; the house build state is not exposed in this snapshot.");
        }

        List<String> otherCarried = new ArrayList<>();
        List<String> otherBanked = new ArrayList<>();
        for (TeleportCatalog.Teleport teleport : TeleportCatalog.ALL)
        {
            if (teleport.x > 0 && teleport.y > 0) continue;
            int carried = countAccessible(account, teleport);
            int stored = countBanked(account, teleport);
            if (carried > 0) otherCarried.add(teleport.name + " → " + teleport.destination + " (" + carried + " carried; " + teleport.notes + ")");
            if (stored > 0) otherBanked.add(teleport.name + " → " + teleport.destination + " (" + stored + " banked; " + teleport.notes + ")");
        }
        if (!otherCarried.isEmpty())
        {
            suggestions.add("\nOTHER CARRIED TRANSPORT — selectable or multi-destination");
            suggestions.addAll(otherCarried);
        }
        if (!otherBanked.isEmpty())
        {
            suggestions.add("\nOTHER BANKED TRANSPORT — withdraw first");
            suggestions.addAll(otherBanked);
        }
        suggestions.add("\nImportant: bank ownership does not mean an item is available to use. Distances from teleport landing points are straight-line lower bounds, not collision-aware route lengths. Item charges, spellbook, quest access and destination selection are not inferred from ownership.");
        return suggestions;
    }

    private void appendGlobalTravelChains(List<String> lines, AccountSnapshot account,
        DestinationCatalog.Destination destination, NavigationSnapshot navigation,
        List<TeleportCandidate> carried, List<TeleportCandidate> banked,
        List<SpellTeleportCatalog.SpellTeleport> spells)
    {
        WorldPoint target = new WorldPoint(destination.x, destination.y, destination.plane);
        GlobalTravelPlanner graph = new GlobalTravelPlanner(account, target, destination.name,
            hasCarriedFairyRingAccess(account));
        List<GlobalTravelPlanner.Origin> ready = new ArrayList<>();
        if (navigation != null && navigation.getPlayerLocation() != null)
            ready.add(new GlobalTravelPlanner.Origin("Start at your current location",
                navigation.getPlayerLocation(), 0));

        for (TeleportCandidate teleport : carried)
            ready.add(new GlobalTravelPlanner.Origin(
                "Use carried " + teleport.teleport.name + " (verify charges and landing)",
                new WorldPoint(teleport.teleport.x, teleport.teleport.y, teleport.teleport.plane), 10));
        for (SpellTeleportCatalog.SpellTeleport spell : spells)
            ready.add(new GlobalTravelPlanner.Origin(
                "Cast " + spell.name + " (confirm runes and selected destination)",
                new WorldPoint(spell.x, spell.y, 0), 10));

        List<GlobalTravelPlanner.Route> readyRoutes = graph.rank(ready, 4);
        lines.add("\nACCOUNT-AWARE MULTI-LEG TRAVEL (estimated lower-bound ranking)");
        if (readyRoutes.isEmpty())
        {
            lines.add("No short, catalogued walking/transport connection was found. Review the individual teleport starts and destination access notes.");
        }
        else
        {
            for (int i = 0; i < readyRoutes.size(); i++)
            {
                GlobalTravelPlanner.Route route = readyRoutes.get(i);
                lines.add((i + 1) + ". Approximate cost " + route.estimatedCost + " tile-equivalents");
                for (String step : route.steps) lines.add("  → " + step);
            }
        }
        List<GlobalTravelPlanner.Origin> withdraw = new ArrayList<>();
        for (TeleportCandidate teleport : banked)
            withdraw.add(new GlobalTravelPlanner.Origin(
                "Withdraw " + teleport.teleport.name + " from a bank first; then teleport (charges unverified)",
                new WorldPoint(teleport.teleport.x, teleport.teleport.y, teleport.teleport.plane), 10));
        List<GlobalTravelPlanner.Route> bankRoutes = graph.rank(withdraw, 2);
        if (!bankRoutes.isEmpty())
        {
            lines.add("Bank-dependent chains (require getting to a bank and withdrawing the item):");
            for (GlobalTravelPlanner.Route route : bankRoutes)
            {
                lines.add("  Approximate cost after withdrawal " + route.estimatedCost + " tile-equivalents");
                for (String step : route.steps) lines.add("    → " + step);
            }
        }
        lines.add("Transport edges use detected quests, items, diaries and known landing data. All off-scene walking segments are geometric lower bounds; water, terrain, stairs, boats, shortcuts, hazards and optional unlocks may change the true route.");
    }

    public List<String> progressionSuggestions(AccountSnapshot account)
    {
        return account == null ? java.util.Collections.emptyList() : SkillProgressionCatalog.suggestions(account);
    }

    public synchronized List<String> bossReadiness(AccountSnapshot account, String bossName)
    {
        List<String> lines = new ArrayList<>();
        if (account == null) return lines;
        BossCatalog.Boss boss = BossCatalog.find(bossName);
        if (boss == null)
        {
            lines.add("No local readiness profile for this boss. Use 'Draft full AI guide' to research any OSRS boss against the current account and OSRS Wiki.");
            return lines;
        }

        lines.add("BOSS READINESS — " + boss.name.toUpperCase());
        lines.add("Combat style: " + boss.combatStyle);
        lines.add("\nACCESS CHECKS");
        if (boss.accessQuest != null)
        {
            String state = account.getProgress().getQuests().get(boss.accessQuest);
            if ("FINISHED".equals(state)) lines.add("✓ Quest complete: " + boss.accessQuest);
            else if ("IN_PROGRESS".equals(state)) lines.add("! Quest in progress: " + boss.accessQuest);
            else if ("NOT_STARTED".equals(state)) lines.add("✗ Quest not completed: " + boss.accessQuest);
            else lines.add("? Quest state not detected: " + boss.accessQuest);
        }
        if (boss.requiredSlayerLevel > 0)
        {
            int current = account.getRealLevels().getOrDefault("Slayer", 1);
            lines.add((current >= boss.requiredSlayerLevel ? "✓" : "✗") + " Slayer level: " + current + " / " + boss.requiredSlayerLevel + " required");
            lines.add("Slayer check uses your real level; temporary boosts are not simulated. See the notes for any known boost route.");
        }
        if (!boss.requiredSkills.isEmpty())
        {
            lines.add("\nHARD SKILL REQUIREMENTS — base levels");
            lines.add("Temporary skill boosts are not simulated by this check.");
            for (Map.Entry<String, Integer> requirement : boss.requiredSkills.entrySet())
            {
                int current = account.getRealLevels().getOrDefault(requirement.getKey(), 1);
                lines.add((current >= requirement.getValue() ? "MET " : "NOT MET ")
                    + requirement.getKey() + ": " + current + " / " + requirement.getValue());
            }
        }
        if (boss.requiredTaskMatches.length > 0)
        {
            String task = account.getSlayerTask().getTaskName().toUpperCase(java.util.Locale.ROOT);
            boolean taskMatch = false;
            for (String required : boss.requiredTaskMatches) if (task.contains(required)) taskMatch = true;
            String diary = boss.taskExceptionDiary == null ? null
                : account.getProgress().getDiaries().get(boss.taskExceptionDiary);
            boolean diaryException = diary != null && diary.contains(boss.taskExceptionTier + ": Incomplete");
            if (taskMatch) lines.add("✓ Current Slayer task matches: " + account.getSlayerTask().getTaskName());
            else if (diaryException) lines.add("? Possible one-time diary exception: " + boss.taskExceptionTier + " "
                + boss.taskExceptionDiary + " Diary permits an off-task kill, but individual task completion is not captured.");
            else if (account.getSlayerTask().isActive())
                lines.add("✗ A matching Slayer task is required; current task: " + account.getSlayerTask().getTaskName());
            else lines.add("✗ A matching Slayer task is required; no active task detected.");
        }

        lines.add("\nRECOMMENDED SKILLS — recommendations, not access requirements");
        boss.recommendedSkills.forEach((skill, target) -> {
            int current = account.getRealLevels().getOrDefault(skill, 1);
            lines.add((current >= target ? "✓ " : "• ") + skill + ": " + current + " / " + target + (current >= target ? " (met)" : " (" + (target - current) + " levels short)"));
        });
        lines.add("\nGEAR CHECKLIST");
        for (String item : boss.gear) lines.add("□ " + item);
        lines.add("\nDETECTED OWNED ITEMS");
        Set<String> detected = detectedBossItems(account, boss);
        if (detected.isEmpty())
            lines.add("No exact gear/supply name matches detected in the current inventory, equipment or saved bank snapshot.");
        else
            lines.addAll(detected);
        lines.add("Generic gear categories and unlisted alternatives still require manual review.");
        lines.add("\nSUPPLIES CHECKLIST");
        for (String item : boss.supplies)
        {
            if (isGenericSupply(item))
                lines.add("PACK / VERIFY: " + item + " — choose a suitable quantity for your method; the exact supply choice is not inferred.");
            else
                lines.add("□ " + item);
        }
        lines.add("\\nNOTES");
        lines.add(boss.specialNotes);

        if (boss.destination != null && !boss.destination.isEmpty())
        {
            lines.add("\nROUTE AND TELEPORT OPTIONS");
            lines.addAll(routeSuggestions(account, boss.destination));
        }
        lines.add("\nCURRENT GUIDE: " + boss.wikiUrl);
        lines.add("This is a readiness checklist, not a guarantee of a safe kill. Verify current mechanics, gear and Hardcore death risk before entering.");
        return lines;
    }

    private boolean isGenericSupply(String requirement)
    {
        if (requirement == null) return false;
        String normalized = normalizeItemName(requirement);
        return normalized.equals("food") || normalized.equals("emergency teleport")
            || normalized.equals("team specific supplies") || normalized.equals("method specific supplies");
    }

    private Set<String> detectedBossItems(AccountSnapshot account, BossCatalog.Boss boss)
    {
        Set<String> requirements = new LinkedHashSet<>();
        requirements.addAll(boss.gear);
        requirements.addAll(boss.supplies);
        Set<String> detected = new LinkedHashSet<>();
        for (Map.Entry<Integer, String> entry : account.getItemNames().entrySet())
        {
            String itemName = entry.getValue();
            String normalizedItem = normalizeItemName(itemName);
            if (normalizedItem.length() < 4) continue;
            boolean matches = requirements.stream()
                .anyMatch(requirement -> itemMatchesRequirement(normalizedItem, requirement));
            if (!matches) continue;

            int carried = account.countAccessibleItem(entry.getKey());
            int banked = account.countBankedItem(entry.getKey());
            String location = carried > 0 ? "carried/equipped" : banked > 0 ? "banked" : "owned";
            detected.add("✓ " + itemName + " (" + location + ")");
        }
        return detected;
    }

    private boolean itemMatchesRequirement(String normalizedItem, String requirement)
    {
        if (requirement == null) return false;
        int alternativeIndex = requirement.toLowerCase(java.util.Locale.ROOT).indexOf(" or ");
        if (alternativeIndex >= 0)
        {
            String left = requirement.substring(0, alternativeIndex).trim();
            String right = requirement.substring(alternativeIndex + 4).trim();
            String lowerRight = right.toLowerCase(java.util.Locale.ROOT);
            boolean explanatory = lowerRight.startsWith("method-specific") || lowerRight.startsWith("suitable ")
                || lowerRight.startsWith("appropriate ") || lowerRight.startsWith("another ")
                || lowerRight.startsWith("other ");
            if (!explanatory && (itemMatchesRequirement(normalizedItem, left)
                || itemMatchesRequirement(normalizedItem, right))) return true;
            requirement = left;
        }
        if (requirement.contains("/"))
        {
            String[] parts = requirement.split("/", 2);
            String left = parts[0].trim();
            String right = parts[1].trim();
            int suffixStart = right.indexOf(' ');
            if (suffixStart > 0)
            {
                String suffix = right.substring(suffixStart);
                String leftAlternative = normalizeItemName(left + suffix);
                String rightAlternative = normalizeItemName(right);
                if (normalizedItem.contains(leftAlternative) || normalizedItem.contains(rightAlternative)) return true;
            }
        }

        String normalizedRequirement = normalizeItemName(requirement);
        return normalizedItem.contains(normalizedRequirement)
            || normalizedRequirement.contains(normalizedItem);
    }

    private String normalizeItemName(String value)
    {
        if (value == null) return "";
        String normalized = value.toLowerCase(java.util.Locale.ROOT)
            .replaceAll("\\(\\s*[1-4]\\s*\\)$", "")
            .replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
        normalized = normalized.replace("assembler", "device");
        String[] words = normalized.split(" ");
        StringBuilder singular = new StringBuilder();
        for (String word : words)
        {
            if (word.length() > 3 && word.endsWith("s") && !word.endsWith("ss"))
                word = word.substring(0, word.length() - 1);
            if (singular.length() > 0) singular.append(' ');
            singular.append(word);
        }
        return singular.toString();
    }

    public synchronized List<String> herbRun(AccountSnapshot account)
    {
        return herbRun(account, true);
    }

    public synchronized List<String> herbRun(AccountSnapshot account, boolean includeBank)
    {
        List<String> lines = new ArrayList<>();
        if (account == null) return lines;
        ensureTeleportCounts(account);
        lines.add("HERB RUN ROUTE — NEAREST-NEIGHBOUR ORDER");
        lines.add("The order is a practical distance heuristic, not a globally optimal route. Some newer patch entries use a nearby access anchor rather than the exact patch tile; confirm local access and disease protection.");

        List<DestinationCatalog.Destination> remaining = new ArrayList<>();
        Instant now = Instant.now();
        for (DestinationCatalog.Destination destination : DestinationCatalog.ALL)
        {
            if (!destination.activity.equals("Farming")
                || !destination.name.toLowerCase(java.util.Locale.ROOT).contains("herb patch")) continue;

            String lockReason = herbPatchLockReason(account, destination.name);
            if (lockReason != null)
            {
                lines.add("LOCKED — " + destination.name + ": " + lockReason);
                continue;
            }

            FarmingPatchSnapshot state = account.getFarmingPatches().stream()
                .filter(patch -> patch.getName().equalsIgnoreCase(destination.name))
                .findFirst().orElse(null);
            if (state != null && "Growing".equals(state.getState())
                && state.getReadyAt() != null && state.getReadyAt().isAfter(now))
            {
                lines.add("WAIT — " + destination.name + ": " + state.getCrop() + ", "
                    + state.getStatusText(now));
                continue;
            }
            remaining.add(destination);
        }
        NavigationSnapshot navigation = account.getNavigation();
        boolean hasCurrentLocation = navigation != null && navigation.getPlayerLocation() != null;
        WorldPoint cursor = hasCurrentLocation
            ? navigation.getPlayerLocation() : new WorldPoint(3212, 3424, 0);
        if (!hasCurrentLocation)
            lines.add("Current location unavailable; the route order uses a Lumbridge starting-point fallback.");
        int order = 1;
        while (!remaining.isEmpty())
        {
            WorldPoint from = cursor;
            DestinationCatalog.Destination next = remaining.stream()
                .min(Comparator.comparingInt(d -> from.distanceTo2D(new WorldPoint(d.x, d.y, d.plane))))
                .orElse(remaining.get(0));
            remaining.remove(next);
            String access;
            if (next.name.toLowerCase(java.util.Locale.ROOT).contains("ortus"))
            {
                access = ortusFarmAccess(account, includeBank);
            }
            else
            {
                SpellTeleportCatalog.SpellTeleport spell = nearestSpellTeleport(account, next);
                TeleportCatalog.Teleport readyTeleport = nearestTeleport(account, next, false);
                TeleportCatalog.Teleport bankedTeleport = includeBank && readyTeleport == null ? nearestTeleport(account, next, true) : null;
                int spellDistance = spell == null ? Integer.MAX_VALUE : new WorldPoint(spell.x, spell.y, 0).distanceTo2D(new WorldPoint(next.x, next.y, next.plane));
                int itemDistance = readyTeleport == null ? Integer.MAX_VALUE : new WorldPoint(readyTeleport.x, readyTeleport.y, readyTeleport.plane).distanceTo2D(new WorldPoint(next.x, next.y, next.plane));
                FairyRingCatalog.Ring fairyLanding = hasCarriedFairyRingAccess(account)
                    ? FairyRingCatalog.nearestUnlocked(account, new WorldPoint(next.x, next.y, next.plane)) : null;
                int fairyDistance = fairyLanding == null ? Integer.MAX_VALUE
                    : fairyLanding.location.distanceTo2D(new WorldPoint(next.x, next.y, next.plane));
                if (fairyLanding != null && fairyDistance < Math.min(spellDistance, itemDistance))
                {
                    FairyRingCatalog.Ring entry = FairyRingCatalog.nearestUnlocked(account, cursor);
                    access = "fairy ring " + fairyLanding.code + " (landing ~" + fairyDistance
                        + " tiles away; nearest entry " + (entry == null ? "unknown" : entry.code) + ")";
                }
                else if (spell != null && spellDistance <= itemDistance) access = "spell option: " + spell.name + " (confirm runes)";
                else if (readyTeleport != null) access = "carried: " + readyTeleport.name;
                else if (bankedTeleport != null) access = "banked: " + bankedTeleport.name + " (withdraw first)";
                else if (spell != null) access = "spell option: " + spell.name + " (confirm runes)";
                else if (hasPotentialHouseFairyRing(account)) access = "possible POH fairy ring (built state not detected)";
                else access = "no catalogued teleport found";
            }
            FarmingPatchSnapshot patchState = account.getFarmingPatches().stream()
                .filter(patch -> patch.getName().equalsIgnoreCase(next.name)).findFirst().orElse(null);
            String tracked = patchState == null ? "" : " | last observed: " + patchState.getCrop()
                + ", " + patchState.getStatusText(java.time.Instant.now());
            lines.add(order++ + ". " + next.name + " — " + next.access + " | " + access + tracked);
            cursor = new WorldPoint(next.x, next.y, next.plane);
        }
        lines.add("\nCarry: herb seeds, spade, rake, seed dibber, compost/supercompost/ultracompost, plant cure if needed, and access items for your unlocked patches.");
        lines.add("The bank cache is account-owned inventory evidence, not proof that a teleport is currently available. Charges and unlocks are not verified.");
        return lines;
    }

    public synchronized String nearestKnownTeleport(AccountSnapshot account)
    {
        NavigationSnapshot navigation = account == null ? null : account.getNavigation();
        if (navigation == null || navigation.getPlayerLocation() == null) return "Log in to calculate nearby options.";
        ensureTeleportCounts(account);
        WorldPoint here = navigation.getPlayerLocation();
        TeleportCatalog.Teleport item = TeleportCatalog.ALL.stream()
            .filter(t -> t.x > 0 && t.y > 0 && countAccessible(account, t) > 0)
            .min(Comparator.comparingInt(t -> here.distanceTo2D(new WorldPoint(t.x, t.y, t.plane))))
            .orElse(null);
        SpellTeleportCatalog.SpellTeleport spell = SpellTeleportCatalog.available(account).stream()
            .filter(t -> t.x > 0 && t.y > 0)
            .min(Comparator.comparingInt(t -> here.distanceTo2D(new WorldPoint(t.x, t.y, 0))))
            .orElse(null);
        int itemDistance = item == null ? Integer.MAX_VALUE : here.distanceTo2D(new WorldPoint(item.x, item.y, item.plane));
        int spellDistance = spell == null ? Integer.MAX_VALUE : here.distanceTo2D(new WorldPoint(spell.x, spell.y, 0));
        if (spell != null && spellDistance < itemDistance) return "spell option: " + spell.name + " to " + spell.destination + " (runes not verified; about " + spellDistance + " tiles; lower bound)";
        if (item != null) return item.name + " to " + item.destination + " (about " + itemDistance + " tiles; lower bound)";
        return "No catalogued carried teleport item or available spell teleport detected.";
    }

    private String herbPatchLockReason(AccountSnapshot account, String patchName)
    {
        Map<String, String> quests = account.getProgress().getQuests();
        Map<String, String> diaries = account.getProgress().getDiaries();
        if ("Troll Stronghold herb patch".equalsIgnoreCase(patchName)
            && !"FINISHED".equals(quests.get("My Arm's Big Adventure")))
            return "complete My Arm's Big Adventure";
        if ("Weiss herb patch".equalsIgnoreCase(patchName)
            && !"FINISHED".equals(quests.get("Making Friends with My Arm")))
            return "complete Making Friends with My Arm and build the Fire of Nourishment";
        if ("Harmony Island herb patch".equalsIgnoreCase(patchName))
        {
            if (!"FINISHED".equals(quests.get("The Great Brain Robbery")))
                return "complete The Great Brain Robbery to reach Harmony Island";
            String diary = diaries.get("Morytania");
            if (diary == null || !diary.contains("Elite: Complete"))
                return "complete the Elite Morytania Diary";
        }
        if ("Farming Guild herb patch".equalsIgnoreCase(patchName)
            && account.getRealLevels().getOrDefault("Farming", 1) < 65)
            return "requires 65 Farming";
        if ("Morytania herb patch".equalsIgnoreCase(patchName)
            && !"FINISHED".equals(quests.get("Priest in Peril")))
            return "complete Priest in Peril to access Morytania";
        if ("Ortus Farm herb patch".equalsIgnoreCase(patchName)
            && !"FINISHED".equals(quests.get("Children of the Sun")))
            return "complete Children of the Sun to access Varlamore";
        return null;
    }

    private String ortusFarmAccess(AccountSnapshot account)
    {
        return ortusFarmAccess(account, true);
    }

    private String ortusFarmAccess(AccountSnapshot account, boolean includeBank)
    {
        TeleportCatalog.Teleport quetzal = TeleportCatalog.ALL.stream().filter(t -> t.name.equals("Quetzal whistle")).findFirst().orElse(null);
        TeleportCatalog.Teleport hunterCape = TeleportCatalog.ALL.stream().filter(t -> t.name.equals("Hunter cape")).findFirst().orElse(null);
        TeleportCatalog.Teleport civitas = TeleportCatalog.ALL.stream().filter(t -> t.name.equals("Civitas illa Fortis teleport")).findFirst().orElse(null);
        boolean ajpUnlocked = FairyRingCatalog.ALL.stream()
            .anyMatch(ring -> "AJP".equals(ring.code) && FairyRingCatalog.isDestinationUnlocked(account, ring));
        if (civitas != null && countAccessible(account, civitas) > 0) return "carried: Civitas illa Fortis teleport → local route to Ortus Farm (city-side route anchor ~33 tiles away)";
        if (ajpUnlocked && hasCarriedFairyRingAccess(account)) return "carried: use a nearby fairy ring and dial AJP for the Ortus Farm area; confirm the final walk in game";
        if (quetzal != null && countAccessible(account, quetzal) > 0) return "carried: Quetzal whistle → Hunter Guild, then run north to Ortus Farm";
        if (hunterCape != null && countAccessible(account, hunterCape) > 0) return "carried: Hunter cape → Hunter Guild, then run north to Ortus Farm";
        if (ajpUnlocked && fairyRingAccessStatus(account).startsWith("Staff detected in inventory")) return "equip the carried Dramen/Lunar staff, then use fairy ring AJP for the Ortus Farm area";
        if (ajpUnlocked && includeBank && fairyRingAccessStatus(account, true).startsWith("Staff detected in bank")) return "banked: withdraw and equip a Dramen/Lunar staff, then use fairy ring AJP for the Ortus Farm area";
        if (ajpUnlocked && hasPotentialHouseFairyRing(account)) return "possible POH fairy ring route to AJP if a ring has been built; built state is not detected";
        if (includeBank && civitas != null && countBanked(account, civitas) > 0) return "banked: withdraw Civitas illa Fortis teleport; local route to Ortus Farm";
        if (includeBank && quetzal != null && countBanked(account, quetzal) > 0) return "banked: Quetzal whistle → Hunter Guild (withdraw first), then run north";
        if (includeBank && hunterCape != null && countBanked(account, hunterCape) > 0) return "banked: Hunter cape (withdraw first), then teleport to Hunter Guild";
        if (ajpUnlocked) return "fallback: ferry or mainland approach to Varlamore; fairy ring AJP is unlocked if a ring network entry and staff or diary waiver are available";
        return "fallback: visit Varlamore and confirm access to Avium Savannah; then unlock/use fairy ring AJP or an accessible Quetzal destination";
    }

    private FairyRingCatalog.Ring nearestSceneFairyRing(AccountSnapshot account, NavigationSnapshot navigation)
    {
        if (navigation == null || navigation.getPlayerLocation() == null) return null;
        WorldPoint current = navigation.getPlayerLocation();
        FairyRingCatalog.Ring best = null;
        int bestSteps = Integer.MAX_VALUE;
        for (FairyRingCatalog.Ring ring : FairyRingCatalog.ALL)
        {
            if (ring.location == null || !FairyRingCatalog.isDestinationUnlocked(account, ring)) continue;
            if (ring.location.getPlane() != current.getPlane()) continue;
            int dx = ring.location.getX() - navigation.getBaseX();
            int dy = ring.location.getY() - navigation.getBaseY();
            if (dx < 0 || dx >= 104 || dy < 0 || dy >= 104) continue;
            PathResult path = pathfinder.findPath(navigation, ring.location);
            if (path.isFound() && path.getSteps() < bestSteps)
            {
                best = ring;
                bestSteps = path.getSteps();
            }
        }
        return best;
    }

    private String fairyRingAccessStatus(AccountSnapshot account)
    {
        return fairyRingAccessStatus(account, true);
    }

    private String fairyRingAccessStatus(AccountSnapshot account, boolean includeBank)
    {
        if (account == null) return "Account snapshot unavailable.";
        List<String> missing = fairyRingMissingQuests(account);
        if (!missing.isEmpty()) return "Not unlocked: complete " + String.join(", ", missing) + ".";
        if (hasEliteLumbridgeDiary(account))
            return "Available: fairy-ring quest chain is unlocked and the Elite Lumbridge & Draynor Diary waives the staff requirement.";
        if (hasEquippedFairyRingStaff(account))
            return "Available: fairy-ring quest chain is unlocked and a Dramen/Lunar staff is equipped.";
        if (hasInventoryFairyRingStaff(account))
            return "Staff detected in inventory: equip a Dramen/Lunar staff before using a field ring.";
        if (includeBank && hasBankedFairyRingStaff(account))
            return "Staff detected in bank: withdraw and equip a Dramen/Lunar staff before using a field ring.";
        if (hasPotentialHouseFairyRing(account))
            return "Potentially available through a built POH fairy ring; the house build state is not detectable.";
        return "Quest chain unlocked, but no equipped staff, diary waiver or detectable POH ring access was found.";
    }

    private List<String> fairyRingMissingQuests(AccountSnapshot account)
    {
        List<String> missing = new ArrayList<>();
        String fairyTwo = account.getProgress().getQuests().get("Fairytale II - Cure a Queen");
        if (!"IN_PROGRESS".equals(fairyTwo) && !"FINISHED".equals(fairyTwo))
            missing.add("permission during Fairytale II - Cure a Queen");
        else if (account.getTravelSetting("fairyPermission") == 0)
            missing.add("the Fairy Godfather's permission during Fairytale II");
        String[] prerequisites = {"The Restless Ghost", "Priest in Peril", "Nature Spirit", "Lost City", "Fairytale I - Growing Pains"};
        for (String quest : prerequisites)
            if (!"FINISHED".equals(account.getProgress().getQuests().get(quest))) missing.add(quest);
        return missing;
    }

    private boolean hasCarriedFairyRingAccess(AccountSnapshot account)
    {
        return fairyRingMissingQuests(account).isEmpty()
            && (hasEliteLumbridgeDiary(account) || hasEquippedFairyRingStaff(account));
    }

    private boolean hasEliteLumbridgeDiary(AccountSnapshot account)
    {
        String diary = account.getProgress().getDiaries().get("Lumbridge & Draynor");
        return diary != null && diary.contains("Elite: Complete");
    }

    private boolean hasEquippedFairyRingStaff(AccountSnapshot account)
    {
        for (Map.Entry<Integer, String> entry : account.getItemNames().entrySet())
        {
            String name = entry.getValue() == null ? "" : entry.getValue().toLowerCase(java.util.Locale.ROOT);
            if ((name.contains("dramen staff") || name.contains("lunar staff"))
                && account.getEquipment().getOrDefault(entry.getKey(), 0) > 0) return true;
        }
        return false;
    }

    private boolean hasInventoryFairyRingStaff(AccountSnapshot account)
    {
        for (Map.Entry<Integer, String> entry : account.getItemNames().entrySet())
        {
            String name = entry.getValue() == null ? "" : entry.getValue().toLowerCase(java.util.Locale.ROOT);
            if ((name.contains("dramen staff") || name.contains("lunar staff"))
                && account.getInventory().getOrDefault(entry.getKey(), 0) > 0) return true;
        }
        return false;
    }

    private boolean hasBankedFairyRingStaff(AccountSnapshot account)
    {
        for (Map.Entry<Integer, String> entry : account.getItemNames().entrySet())
        {
            String name = entry.getValue() == null ? "" : entry.getValue().toLowerCase(java.util.Locale.ROOT);
            if ((name.contains("dramen staff") || name.contains("lunar staff"))
                && account.countBankedItem(entry.getKey()) > 0) return true;
        }
        return false;
    }

    private boolean hasPotentialHouseFairyRing(AccountSnapshot account)
    {
        return fairyRingMissingQuests(account).isEmpty()
            && "FINISHED".equals(account.getProgress().getQuests().get("Fairytale II - Cure a Queen"))
            && account.getRealLevels().getOrDefault("Construction", 1) >= 85;
    }

    private SpellTeleportCatalog.SpellTeleport nearestSpellTeleport(AccountSnapshot account, DestinationCatalog.Destination target)
    {
        WorldPoint targetPoint = new WorldPoint(target.x, target.y, target.plane);
        return SpellTeleportCatalog.available(account).stream()
            .filter(spell -> spell.x > 0 && spell.y > 0)
            .min(Comparator.comparingInt(spell -> new WorldPoint(spell.x, spell.y, 0).distanceTo2D(targetPoint)))
            .orElse(null);
    }

    private TeleportCatalog.Teleport nearestTeleport(AccountSnapshot account, DestinationCatalog.Destination target, boolean bankOnly)
    {
        return TeleportCatalog.ALL.stream()
            .filter(t -> t.x > 0 && t.y > 0 && (bankOnly ? countBanked(account, t) > 0 : countAccessible(account, t) > 0))
            .min(Comparator.comparingInt(t -> new WorldPoint(t.x, t.y, t.plane).distanceTo2D(new WorldPoint(target.x, target.y, target.plane))))
            .orElse(null);
    }

    private int countAccessible(AccountSnapshot account, TeleportCatalog.Teleport teleport)
    {
        ensureTeleportCounts(account);
        TeleportCounts counts = teleportCounts.get(teleport.name);
        return counts == null ? 0 : counts.carried;
    }

    private int countBanked(AccountSnapshot account, TeleportCatalog.Teleport teleport)
    {
        ensureTeleportCounts(account);
        TeleportCounts counts = teleportCounts.get(teleport.name);
        return counts == null ? 0 : counts.banked;
    }

    private void ensureTeleportCounts(AccountSnapshot account)
    {
        if (account == cachedSnapshot) return;
        cachedSnapshot = account;
        teleportCounts.clear();
        for (TeleportCatalog.Teleport teleport : TeleportCatalog.ALL)
            teleportCounts.put(teleport.name, new TeleportCounts());
        addContainerCounts(account, account.getInventory(), true);
        addContainerCounts(account, account.getEquipment(), true);
        addContainerCounts(account, account.getBank(), false);
    }

    private void addContainerCounts(AccountSnapshot account, Map<Integer, Integer> items, boolean carried)
    {
        for (Map.Entry<Integer, Integer> entry : items.entrySet())
        {
            String name = account.getItemName(entry.getKey());
            if (TeleportCatalog.isUnchargedItemName(name)) continue;
            for (TeleportCatalog.Teleport teleport : TeleportCatalog.ALL)
            {
                if (!meetsTeleportSkillRequirement(account, teleport, name)) continue;
                if (teleport.matchesItemId(entry.getKey()) || teleport.matchesItemName(name))
                {
                    TeleportCounts counts = teleportCounts.get(teleport.name);
                    if (carried) counts.carried += entry.getValue();
                    else counts.banked += entry.getValue();
                }
            }
        }
    }

    private boolean meetsTeleportSkillRequirement(AccountSnapshot account,
        TeleportCatalog.Teleport teleport, String itemName)
    {
        if (itemName == null) return true;
        if (teleport.name.startsWith("Quetzal whistle"))
            return !itemName.toLowerCase(java.util.Locale.ROOT).contains("blueprint")
                && QuetzalCatalog.isDestinationAvailable(account, teleport.destination);
        String diary = account.getProgress().getDiaries().get("Ardougne");
        boolean ardougneHard = diary != null && diary.contains("Hard: Complete");
        String varrockDiary = account.getProgress().getDiaries().get("Varrock");
        boolean varrockMedium = varrockDiary != null && (varrockDiary.contains("Medium: Complete")
            || varrockDiary.contains("Hard: Complete") || varrockDiary.contains("Elite: Complete"));
        String kandarinDiary = account.getProgress().getDiaries().get("Kandarin");
        boolean kandarinHard = kandarinDiary != null && kandarinDiary.contains("Hard: Complete");
        if ("Watchtower teleport — Yanille landing".equals(teleport.name)
            && !ardougneHard) return false;
        if ("Varrock teleport — Grand Exchange landing".equals(teleport.name)
            && !varrockMedium) return false;
        if ("Camelot teleport — Seers' bank landing".equals(teleport.name)
            && !kandarinHard) return false;

        if (teleport.name.startsWith("Enchanted lyre — "))
        {
            String fremennik = account.getProgress().getDiaries().get("Fremennik");
            boolean elite = fremennik != null && fremennik.contains("Elite: Complete");
            boolean hard = elite || (fremennik != null && fremennik.contains("Hard: Complete"));
            if (teleport.name.contains("Waterbirth Island") && !hard) return false;
            if ((teleport.name.contains("Neitiznot") || teleport.name.contains("Jatizso"))
                && !elite) return false;
        }

        if (teleport.name.startsWith("Ring of shadows"))
        {
            // 28329 is the uncharged version. Every teleport consumes a charge.
            if (!"FINISHED".equals(account.getProgress().getQuests().get("Desert Treasure II"))) return false;
            if (itemName.toLowerCase(java.util.Locale.ROOT).contains("uncharged")) return false;
            if (!"Ring of shadows".equals(teleport.name))
            {
                String unlock = teleport.name.contains("Lassar") ? "ringLassar"
                    : teleport.name.contains("Ghorrock") ? "ringGhorrock"
                    : teleport.name.contains("Scar") ? "ringScar" : "ringStranglewood";
                if (account.getTravelSetting(unlock) <= 0) return false;
            }
        }
        if ("Stony basalt — upper landing".equals(teleport.name))
        {
            String fremennik = account.getProgress().getDiaries().get("Fremennik");
            if (account.getRealLevels().getOrDefault("Agility", 1) < 73 || fremennik == null
                || (!fremennik.contains("Hard: Complete") && !fremennik.contains("Elite: Complete"))) return false;
        }
        if (("Icy basalt".equals(teleport.name) || teleport.name.startsWith("Stony basalt"))
            && !"FINISHED".equals(account.getProgress().getQuests().get("Making Friends with My Arm"))) return false;
        if ("Ring of dueling — Fortis Colosseum".equals(teleport.name)
            && account.getTravelSetting("colosseumGlory") < 12000) return false;

        String item = itemName.toLowerCase(java.util.Locale.ROOT);
        boolean maxCape = item.startsWith("max cape");
        String name = teleport.name.toLowerCase(java.util.Locale.ROOT);
        String skill = null;
        if (name.startsWith("farming cape")) skill = "Farming";
        else if (name.startsWith("hunter cape")) skill = "Hunter";
        else if (name.startsWith("construction cape")) skill = "Construction";
        else if (name.startsWith("crafting cape")) skill = "Crafting";
        else if (name.startsWith("fishing cape")) skill = "Fishing";
        else if (name.startsWith("sailing cape")) skill = "Sailing";
        else if (name.startsWith("strength cape")) skill = "Strength";
        if (skill == null) return true;
        if (!maxCape && !item.contains(skill.toLowerCase(java.util.Locale.ROOT) + " cape")) return true;
        return account.getRealLevels().getOrDefault(skill, 1) >= 99;
    }

    private static final class TeleportCounts
    {
        private int carried;
        private int banked;
    }

    private static final class TeleportCandidate
    {
        private final TeleportCatalog.Teleport teleport;
        private final int distance;
        private final int count;
        private TeleportCandidate(TeleportCatalog.Teleport teleport, int distance, int count)
        {
            this.teleport = teleport;
            this.distance = distance;
            this.count = count;
        }
    }
}
