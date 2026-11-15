package com.nocturnal.ironman.service;

import org.junit.Test;
import static org.junit.Assert.*;

public class SlayerServiceTest
{
    @Test
    public void selectsTheCorrectMasterSpecificStreak()
    {
        assertEquals(125, SlayerService.selectStreak(7, 500, 125, 12));
        assertEquals(12, SlayerService.selectStreak(10, 500, 125, 12));
        assertEquals(500, SlayerService.selectStreak(0, 500, 125, 12));
        assertEquals(0, SlayerService.selectStreak(7, 500, -1, 12));
    }
}
