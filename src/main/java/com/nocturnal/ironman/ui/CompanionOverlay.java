package com.nocturnal.ironman.ui;

import com.nocturnal.ironman.IronmanCompanionConfig;
import com.nocturnal.ironman.model.AccountSnapshot;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

public class CompanionOverlay extends OverlayPanel
{
    private final IronmanCompanionConfig config;
    private volatile AccountSnapshot snapshot;

    @Inject
    public CompanionOverlay(IronmanCompanionConfig config)
    {
        this.config = config;
    }

    public void configure()
    {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(Overlay.PRIORITY_LOW);
        setPreferredSize(new Dimension(220, 0));
    }

    public void setSnapshot(AccountSnapshot snapshot)
    {
        this.snapshot = snapshot;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        AccountSnapshot current = snapshot;
        if (!config.showOverlay() || current == null)
        {
            return null;
        }

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Ironman Companion")
            .right(current.getAccountType())
            .build());
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Combat")
            .right(Integer.toString(current.getCombatLevel()))
            .build());
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Total level")
            .right(Integer.toString(current.getTotalLevel()))
            .build());
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Quests")
            .right(current.getProgress().getCompletedQuestCount() + " / " + current.getProgress().getQuestCount())
            .build());

        if (current.getSlayerTask().isActive())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Slayer")
                .right(current.getSlayerTask().getRemaining() + " " + current.getSlayerTask().getTaskName())
                .build());
        }

        if (current.getNavigation() != null && current.getNavigation().getPlayerLocation() != null)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Location")
                .right(current.getLocation())
                .build());
        }

        return super.render(graphics);
    }
}
