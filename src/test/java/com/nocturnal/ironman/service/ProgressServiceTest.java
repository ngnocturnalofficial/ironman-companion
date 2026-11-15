package com.nocturnal.ironman.service;

import net.runelite.api.Varbits;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProgressServiceTest
{
    @Test
    public void recognizesEveryDiaryTierVarbitFamily()
    {
        assertTrue(ProgressService.isDiaryVarbit(VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE));
        assertTrue(ProgressService.isDiaryVarbit(VarbitID.KOUREND_DIARY_ELITE_COMPLETE));
        assertTrue(ProgressService.isDiaryVarbit(VarbitID.WESTERN_DIARY_HARD_COMPLETE));
        assertTrue(ProgressService.isDiaryVarbit(Varbits.DIARY_KARAMJA_MEDIUM));
        assertFalse(ProgressService.isDiaryVarbit(VarbitID.SLAYER_POINTS));
    }
}
