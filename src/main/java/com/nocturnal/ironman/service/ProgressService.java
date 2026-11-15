package com.nocturnal.ironman.service;

import com.nocturnal.ironman.model.ProgressSnapshot;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Varbits;
import net.runelite.api.gameval.VarbitID;

public class ProgressService
{
    private static final String[] TIERS = {"Easy", "Medium", "Hard", "Elite"};
    private static final Set<Integer> DIARY_VARBITS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE, VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE,
        VarbitID.ARDOUGNE_DIARY_HARD_COMPLETE, VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE,
        VarbitID.DESERT_DIARY_EASY_COMPLETE, VarbitID.DESERT_DIARY_MEDIUM_COMPLETE,
        VarbitID.DESERT_DIARY_HARD_COMPLETE, VarbitID.DESERT_DIARY_ELITE_COMPLETE,
        VarbitID.FALADOR_DIARY_EASY_COMPLETE, VarbitID.FALADOR_DIARY_MEDIUM_COMPLETE,
        VarbitID.FALADOR_DIARY_HARD_COMPLETE, VarbitID.FALADOR_DIARY_ELITE_COMPLETE,
        VarbitID.FREMENNIK_DIARY_EASY_COMPLETE, VarbitID.FREMENNIK_DIARY_MEDIUM_COMPLETE,
        VarbitID.FREMENNIK_DIARY_HARD_COMPLETE, VarbitID.FREMENNIK_DIARY_ELITE_COMPLETE,
        VarbitID.KANDARIN_DIARY_EASY_COMPLETE, VarbitID.KANDARIN_DIARY_MEDIUM_COMPLETE,
        VarbitID.KANDARIN_DIARY_HARD_COMPLETE, VarbitID.KANDARIN_DIARY_ELITE_COMPLETE,
        Varbits.DIARY_KARAMJA_EASY, Varbits.DIARY_KARAMJA_MEDIUM,
        Varbits.DIARY_KARAMJA_HARD, Varbits.DIARY_KARAMJA_ELITE,
        VarbitID.KOUREND_DIARY_EASY_COMPLETE, VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE,
        VarbitID.KOUREND_DIARY_HARD_COMPLETE, VarbitID.KOUREND_DIARY_ELITE_COMPLETE,
        VarbitID.LUMBRIDGE_DIARY_EASY_COMPLETE, VarbitID.LUMBRIDGE_DIARY_MEDIUM_COMPLETE,
        VarbitID.LUMBRIDGE_DIARY_HARD_COMPLETE, VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE,
        VarbitID.MORYTANIA_DIARY_EASY_COMPLETE, VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE,
        VarbitID.MORYTANIA_DIARY_HARD_COMPLETE, VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE,
        VarbitID.VARROCK_DIARY_EASY_COMPLETE, VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE,
        VarbitID.VARROCK_DIARY_HARD_COMPLETE, VarbitID.VARROCK_DIARY_ELITE_COMPLETE,
        VarbitID.WESTERN_DIARY_EASY_COMPLETE, VarbitID.WESTERN_DIARY_MEDIUM_COMPLETE,
        VarbitID.WESTERN_DIARY_HARD_COMPLETE, VarbitID.WESTERN_DIARY_ELITE_COMPLETE,
        VarbitID.WILDERNESS_DIARY_EASY_COMPLETE, VarbitID.WILDERNESS_DIARY_MEDIUM_COMPLETE,
        VarbitID.WILDERNESS_DIARY_HARD_COMPLETE, VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE
    )));

    public static boolean isDiaryVarbit(int varbitId)
    {
        return DIARY_VARBITS.contains(varbitId);
    }

    private final Client client;

    @Inject
    public ProgressService(Client client)
    {
        this.client = client;
    }

    public ProgressSnapshot capture()
    {
        Map<String, String> quests = new LinkedHashMap<>();
        for (Quest quest : Quest.values())
        {
            quests.put(quest.getName(), quest.getState(client).name());
        }

        Map<String, String> diaries = new LinkedHashMap<>();
        addDiary(diaries, "Ardougne", VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE,
            VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE, VarbitID.ARDOUGNE_DIARY_HARD_COMPLETE,
            VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Desert", VarbitID.DESERT_DIARY_EASY_COMPLETE,
            VarbitID.DESERT_DIARY_MEDIUM_COMPLETE, VarbitID.DESERT_DIARY_HARD_COMPLETE,
            VarbitID.DESERT_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Falador", VarbitID.FALADOR_DIARY_EASY_COMPLETE,
            VarbitID.FALADOR_DIARY_MEDIUM_COMPLETE, VarbitID.FALADOR_DIARY_HARD_COMPLETE,
            VarbitID.FALADOR_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Fremennik", VarbitID.FREMENNIK_DIARY_EASY_COMPLETE,
            VarbitID.FREMENNIK_DIARY_MEDIUM_COMPLETE, VarbitID.FREMENNIK_DIARY_HARD_COMPLETE,
            VarbitID.FREMENNIK_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Kandarin", VarbitID.KANDARIN_DIARY_EASY_COMPLETE,
            VarbitID.KANDARIN_DIARY_MEDIUM_COMPLETE, VarbitID.KANDARIN_DIARY_HARD_COMPLETE,
            VarbitID.KANDARIN_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Karamja", Varbits.DIARY_KARAMJA_EASY, Varbits.DIARY_KARAMJA_MEDIUM,
            Varbits.DIARY_KARAMJA_HARD, Varbits.DIARY_KARAMJA_ELITE);
        addDiary(diaries, "Kourend & Kebos", VarbitID.KOUREND_DIARY_EASY_COMPLETE,
            VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE, VarbitID.KOUREND_DIARY_HARD_COMPLETE,
            VarbitID.KOUREND_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Lumbridge & Draynor", VarbitID.LUMBRIDGE_DIARY_EASY_COMPLETE,
            VarbitID.LUMBRIDGE_DIARY_MEDIUM_COMPLETE, VarbitID.LUMBRIDGE_DIARY_HARD_COMPLETE,
            VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Morytania", VarbitID.MORYTANIA_DIARY_EASY_COMPLETE,
            VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE, VarbitID.MORYTANIA_DIARY_HARD_COMPLETE,
            VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Varrock", VarbitID.VARROCK_DIARY_EASY_COMPLETE,
            VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE, VarbitID.VARROCK_DIARY_HARD_COMPLETE,
            VarbitID.VARROCK_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Western Provinces", VarbitID.WESTERN_DIARY_EASY_COMPLETE,
            VarbitID.WESTERN_DIARY_MEDIUM_COMPLETE, VarbitID.WESTERN_DIARY_HARD_COMPLETE,
            VarbitID.WESTERN_DIARY_ELITE_COMPLETE);
        addDiary(diaries, "Wilderness", VarbitID.WILDERNESS_DIARY_EASY_COMPLETE,
            VarbitID.WILDERNESS_DIARY_MEDIUM_COMPLETE, VarbitID.WILDERNESS_DIARY_HARD_COMPLETE,
            VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE);

        return new ProgressSnapshot(quests, diaries);
    }

    private void addDiary(Map<String, String> target, String name, int easy, int medium, int hard, int elite)
    {
        int[] values = {easy, medium, hard, elite};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++)
        {
            if (i > 0) result.append("  ");
            result.append(TIERS[i]).append(": ").append(client.getVarbitValue(values[i]) == 1 ? "Complete" : "Incomplete");
        }
        target.put(name, result.toString());
    }
}
