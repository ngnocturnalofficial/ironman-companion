package com.nocturnal.ironman.service;

import com.nocturnal.ironman.model.SlayerTaskSnapshot;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

public class SlayerService
{
    private static final int KRYSTILIA_SLAYER_MASTER = 7;
    private static final int MORTIMER_SLAYER_MASTER = 10;

    private final Client client;

    @Inject
    public SlayerService(Client client)
    {
        this.client = client;
    }

    public SlayerTaskSnapshot capture()
    {
        int remaining = Math.max(0, client.getVarpValue(VarPlayerID.SLAYER_COUNT));
        int points = Math.max(0, client.getVarbitValue(VarbitID.SLAYER_POINTS));
        int streak = selectStreak(client.getVarbitValue(VarbitID.SLAYER_MASTER),
            client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED),
            client.getVarbitValue(VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED),
            client.getVarpValue(VarPlayerID.SLAYER_MORTIMER_TASKS_COMPLETED));
        if (remaining == 0)
        {
            return new SlayerTaskSnapshot(false, "No active task", "", 0, 0, points, streak);
        }

        int taskId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
        String taskName = lookupTaskName(taskId);
        String location = lookupTaskLocation(client.getVarpValue(VarPlayerID.SLAYER_AREA));
        int initial = Math.max(remaining, client.getVarpValue(VarPlayerID.SLAYER_COUNT_ORIGINAL));

        if (client.getVarbitValue(VarbitID.SLAYER_MODIFIER_ID) == 2)
        {
            int modifier = client.getVarbitValue(VarbitID.SLAYER_MODIFIER_VALUE);
            boolean negative = client.getVarbitValue(VarbitID.SLAYER_MODIFIER_NEGATIVE) == 1;
            initial += negative ? -modifier : modifier;
        }

        initial = Math.max(remaining, initial);
        return new SlayerTaskSnapshot(true, taskName, location, remaining, initial, points, streak);
    }

    static int selectStreak(int masterId, int standard, int wilderness, int mortimer)
    {
        int value = masterId == KRYSTILIA_SLAYER_MASTER ? wilderness
            : masterId == MORTIMER_SLAYER_MASTER ? mortimer : standard;
        return Math.max(0, value);
    }

    private String lookupTaskName(int taskId)
    {
        try
        {
            int row;
            if (taskId == 98)
            {
                int bossId = client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID);
                var rows = client.getDBRowsByValue(DBTableID.SlayerTaskSublist.ID,
                    DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID, 0, bossId);
                if (rows.isEmpty()) return "Slayer boss task (#" + bossId + ")";
                row = (Integer) client.getDBTableField(rows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0)[0];
            }
            else
            {
                var rows = client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId);
                if (rows.isEmpty()) return "Unknown Slayer task (#" + taskId + ")";
                row = rows.get(0);
            }
            Object[] values = client.getDBTableField(row, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0);
            return values.length == 0 || values[0] == null ? "Unknown Slayer task (#" + taskId + ")" : String.valueOf(values[0]);
        }
        catch (RuntimeException ex)
        {
            return "Unknown Slayer task (#" + taskId + ")";
        }
    }

    private String lookupTaskLocation(int areaId)
    {
        if (areaId <= 0) return "Not specified";
        try
        {
            var rows = client.getDBRowsByValue(DBTableID.SlayerArea.ID, DBTableID.SlayerArea.COL_AREA_ID, 0, areaId);
            if (rows.isEmpty()) return "Unknown area (#" + areaId + ")";
            Object[] values = client.getDBTableField(rows.get(0), DBTableID.SlayerArea.COL_AREA_NAME_IN_HELPER, 0);
            return values.length == 0 || values[0] == null ? "Unknown area (#" + areaId + ")" : String.valueOf(values[0]);
        }
        catch (RuntimeException ex)
        {
            return "Unknown area (#" + areaId + ")";
        }
    }
}
