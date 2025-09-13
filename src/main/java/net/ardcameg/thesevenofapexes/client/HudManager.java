package net.ardcameg.thesevenofapexes.client;

import net.ardcameg.thesevenofapexes.abilities.block.PurificationAbility;
import net.ardcameg.thesevenofapexes.abilities.forbidden.PactOfDecayAbility;
import net.ardcameg.thesevenofapexes.abilities.forbidden.UnstoppableImpulseAbility;
import net.ardcameg.thesevenofapexes.abilities.forbidden.WhispersOfTheVoidAbility;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HudManager {
    private static final int SPACING = 5;
    private static final int INITIAL_Y_OFFSET = 30;

    public static void render(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        if (Minecraft.getInstance().options.hideGui) return;
        float yOffset = INITIAL_Y_OFFSET;
        List<Map.Entry<String, ClientTimerData.TimerInfo>> sortedTimers = new ArrayList<>(ClientTimerData.getActiveTimers().entrySet());
        sortedTimers.sort(Comparator.comparingInt((Map.Entry<String, ClientTimerData.TimerInfo> entry) -> entry.getValue().timerState)
                .thenComparingInt((Map.Entry<String, ClientTimerData.TimerInfo> entry) -> entry.getValue().ticksLeft));

        for (Map.Entry<String, ClientTimerData.TimerInfo> entry : sortedTimers) {
            String timerId = entry.getKey();
            ClientTimerData.TimerInfo timerInfo = entry.getValue();
            if (timerInfo.timerState == ClientTimerData.TimerState.PAUSED.ordinal()) continue;

            RenderInfo renderInfo = getRenderInfoForTimer(timerId, timerInfo);
            CircularTimerHudRenderer.render(guiGraphics, yOffset, renderInfo, timerInfo, partialTick);
            yOffset += (CircularTimerHudRenderer.RADIUS * 2) + SPACING;
        }
    }

    public record RenderInfo(Color color, Item item, boolean shouldBlink) {}

    private static RenderInfo getRenderInfoForTimer(String timerId, ClientTimerData.TimerInfo timerInfo) {
        ClientTimerData.TimerState currentState = ClientTimerData.TimerState.values()[timerInfo.timerState];
        float displaySeconds = (float)timerInfo.ticksLeft / 20.0f;

        return switch (timerId) {
            case UnstoppableImpulseAbility.ID -> new RenderInfo(
                    (displaySeconds <= 10.0f && currentState == ClientTimerData.TimerState.COUNTING) ? new Color(255, 60, 60) : new Color(200, 200, 255),
                    ModItems.FORBIDDEN_UNSTOPPABLE_IMPULSE.get(),
                    false
            );
            case PactOfDecayAbility.ID -> new RenderInfo(new Color(80, 220, 80), ModItems.FORBIDDEN_PACT_OF_DECAY.get(), false);
            case WhispersOfTheVoidAbility.ID -> new RenderInfo(new Color(80, 80, 200), ModItems.FORBIDDEN_WHISPERS_OF_THE_VOID.get(), true);
            case WhispersOfTheVoidAbility.GRACE_ID -> new RenderInfo(new Color(255, 255, 100), ModItems.FORBIDDEN_WHISPERS_OF_THE_VOID.get(), false);
            case PurificationAbility.SURVIVAL_TRIAL_ID -> new RenderInfo(new Color(255, 120, 0), ModItems.FORBIDDEN_REVERSAL_ARTIFACT.get(), true);
            default -> new RenderInfo(Color.WHITE, Items.AIR, false);
        };
    }
}