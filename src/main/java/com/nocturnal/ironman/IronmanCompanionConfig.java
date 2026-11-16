package com.nocturnal.ironman;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ironmancompanion")
public interface IronmanCompanionConfig extends Config
{
    @ConfigItem(keyName = "aiEnabled", name = "Enable AI assistant", description = "Enable the optional OpenAI-compatible AI assistant",
        warning = "When enabled, your IP address and the selected account snapshot are sent to the configured third-party AI endpoint. The OSRS Wiki is also queried. These services are not controlled or verified by RuneLite developers.")
    default boolean aiEnabled() { return false; }

    @ConfigItem(keyName = "apiEndpoint", name = "API endpoint", description = "OpenAI-compatible chat completions endpoint")
    default String apiEndpoint() { return "https://api.openai.com/v1/chat/completions"; }

    @ConfigItem(keyName = "apiKey", name = "API key", description = "Stored in RuneLite configuration; never share screenshots of this value", secret = true)
    default String apiKey() { return ""; }

    @ConfigItem(keyName = "model", name = "Model", description = "Model identifier accepted by your provider")
    default String model() { return "gpt-4o-mini"; }

    @ConfigItem(keyName = "includeBankInAi", name = "Include bank snapshot in AI context", description = "Send cached bank item names and quantities with AI requests")
    default boolean includeBankInAi() { return false; }

    @ConfigItem(keyName = "wikiLookupsEnabled", name = "Look up OSRS Wiki for AI", description = "Send the first 200 characters of your question to the public OSRS Wiki search API to retrieve current reference material")
    default boolean wikiLookupsEnabled() { return true; }

    @ConfigItem(keyName = "includeProgressInAi", name = "Include quests and diaries in AI context", description = "Send detected quest and achievement diary progress with AI requests")
    default boolean includeProgressInAi() { return true; }

    @ConfigItem(keyName = "showOverlay", name = "Show compact overlay", description = "Show account level, quest, Slayer and location information in the game scene")
    default boolean showOverlay() { return true; }
}
