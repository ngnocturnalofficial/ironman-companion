package com.nocturnal.ironman;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class IronmanCompanionDevLauncher
{
    private IronmanCompanionDevLauncher() {}

    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(IronmanCompanionPlugin.class);
        RuneLite.main(args);
    }
}
