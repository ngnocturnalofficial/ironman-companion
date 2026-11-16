package com.nocturnal.ironman;

import com.google.inject.Provides;
import com.nocturnal.ironman.model.AccountSnapshot;
import com.nocturnal.ironman.service.AccountService;
import com.nocturnal.ironman.service.AiService;
import com.nocturnal.ironman.service.PlannerService;
import com.nocturnal.ironman.service.ProgressService;
import com.nocturnal.ironman.ui.CompanionPanel;
import com.nocturnal.ironman.ui.CompanionOverlay;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Ironman Companion",
    description = "Account-aware Ironman planning, bank intelligence, route planning and optional AI assistance",
    tags = {"ironman", "progression", "navigation", "transport", "fairy-rings", "farming", "herb-runs", "slayer", "bossing", "bank", "quests", "diaries", "ai"}
)
public class IronmanCompanionPlugin extends Plugin
{
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ClientToolbar clientToolbar;
    @Inject private OverlayManager overlayManager;
    @Inject private CompanionOverlay companionOverlay;
    @Inject private ConfigManager configManager;
    @Inject private AccountService accountService;
    @Inject private PlannerService plannerService;
    @Inject private AiService aiService;

    private CompanionPanel panel;
    private NavigationButton navButton;
    private int ticks;
    private int progressRefreshCounter;

    @Provides
    IronmanCompanionConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(IronmanCompanionConfig.class);
    }

    @Override
    protected void startUp()
    {
        panel = new CompanionPanel(plannerService, aiService, configManager, clientThread);
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = icon.createGraphics();
        graphics.setColor(new java.awt.Color(31, 110, 82));
        graphics.fillRoundRect(1, 1, 30, 30, 8, 8);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.drawString("I", 13, 21);
        graphics.dispose();
        navButton = NavigationButton.builder().tooltip("Ironman Companion").icon(icon).priority(5).panel(panel).build();
        clientToolbar.addNavigation(navButton);
        companionOverlay.configure();
        overlayManager.add(companionOverlay);
        clientThread.invokeLater(() -> {
            if (client.getGameState() == GameState.LOGGED_IN)
            {
                accountService.reloadBankCache();
                refreshSnapshot(false, true);
            }
        });
    }

    @Override
    protected void shutDown()
    {
        if (navButton != null) clientToolbar.removeNavigation(navButton);
        if (companionOverlay != null) companionOverlay.setSnapshot(null);
        if (companionOverlay != null) overlayManager.remove(companionOverlay);
        navButton = null;
        panel = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            accountService.reloadBankCache();
            refreshSnapshot(false, true);
        }
        else if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
        {
            accountService.clearSession();
            if (companionOverlay != null) companionOverlay.setSnapshot(null);
            SwingUtilities.invokeLater(() -> { if (panel != null) panel.refresh(null); });
        }
    }

    @Subscribe
    public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
    {
        clientThread.invokeLater(() -> {
            accountService.reloadBankCache();
            if (companionOverlay != null) companionOverlay.setSnapshot(null);
            SwingUtilities.invokeLater(() -> { if (panel != null) panel.refresh(null); });
            if (client.getGameState() == GameState.LOGGED_IN) refreshSnapshot(false, true);
        });
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (client.getGameState() == GameState.LOGGED_IN) refreshSnapshot(false, false);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        int varp = event.getVarpId();
        int varbit = event.getVarbitId();
        if (ProgressService.isDiaryVarbit(varbit))
        {
            if (client.getGameState() == GameState.LOGGED_IN) refreshSnapshot(false, true);
            return;
        }
        if (varp == VarPlayerID.SLAYER_COUNT || varp == VarPlayerID.SLAYER_AREA
            || varp == VarPlayerID.SLAYER_TARGET || varp == VarPlayerID.SLAYER_COUNT_ORIGINAL
            || varbit == VarbitID.SLAYER_TARGET_BOSSID || varbit == VarbitID.SLAYER_MODIFIER_ID
            || varbit == VarbitID.SLAYER_MODIFIER_VALUE || varbit == VarbitID.SLAYER_MODIFIER_NEGATIVE
            || varbit == VarbitID.SLAYER_MASTER || varbit == VarbitID.SLAYER_POINTS
            || varbit == VarbitID.SLAYER_TASKS_COMPLETED
            || varbit == VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED
            || varp == VarPlayerID.SLAYER_MORTIMER_TASKS_COMPLETED)
        {
            if (client.getGameState() == GameState.LOGGED_IN) refreshSnapshot(false, false);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        ticks++;
        if (ticks >= 10)
        {
            ticks = 0;
            if (client.getGameState() == GameState.LOGGED_IN)
            {
                boolean refreshProgress = ++progressRefreshCounter >= 5;
                if (refreshProgress) progressRefreshCounter = 0;
                refreshSnapshot(false, refreshProgress);
            }
        }
    }

    private void refreshSnapshot(boolean forceBankSync, boolean refreshProgress)
    {
        clientThread.invokeLater(() -> {
            if (client.getGameState() != GameState.LOGGED_IN) return;
            AccountSnapshot snapshot = accountService.capture(forceBankSync, refreshProgress);
            if (companionOverlay != null) companionOverlay.setSnapshot(snapshot);
            SwingUtilities.invokeLater(() -> { if (panel != null) panel.refresh(snapshot); });
        });
    }
}
