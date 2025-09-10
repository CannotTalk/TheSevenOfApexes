package net.ardcameg.thesevenofapexes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

import java.awt.Color;

public class CircularTimerHudRenderer {
    public static final int RADIUS = 22;
    private static final int LINE_WIDTH = 3;
    private static final int MARGIN = 10;

    public static void render(GuiGraphics guiGraphics, float yOffset, HudManager.RenderInfo renderInfo, ClientTimerData.TimerInfo timerInfo, DeltaTracker partialTick) {
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int centerX = screenWidth - RADIUS - MARGIN;
        int centerY = MARGIN + RADIUS + (int)yOffset;
        float pt = partialTick.getGameTimeDeltaPartialTick(true);

        ClientTimerData.TimerState currentState = ClientTimerData.TimerState.values()[timerInfo.timerState];
        float displaySeconds;
        float progress;

        float maxTimeTicks = timerInfo.maxTicks;
        float maxTimeSeconds = maxTimeTicks / 20.0f;
        if (maxTimeSeconds <= 0) maxTimeSeconds = 1.0f;

        // クライアントサイド補間は廃止し、サーバーからの値をそのまま表示する
        float interpolatedTicks = timerInfo.ticksLeft;

        if (currentState == ClientTimerData.TimerState.ANIMATING_IN || currentState == ClientTimerData.TimerState.ANIMATING_RESET) {
            int animDuration = timerInfo.animationDuration;
            int animTicks = timerInfo.animationTicks;
            float animProgress = Mth.clamp(1.0f - ((animTicks - pt) / (float)animDuration), 0.0f, 1.0f);
            float targetSeconds = (float)timerInfo.animationTargetTicks / 20.0f;
            if (currentState == ClientTimerData.TimerState.ANIMATING_RESET) {
                float startSeconds = (float)timerInfo.ticksLeft / 20.0f;
                displaySeconds = Mth.lerp(animProgress, startSeconds, targetSeconds);
            } else {
                displaySeconds = targetSeconds * animProgress;
            }
            progress = displaySeconds / maxTimeSeconds;
        } else { // COUNTING
            displaySeconds = interpolatedTicks / 20.0f;
            progress = interpolatedTicks / maxTimeTicks;
        }

        if (displaySeconds < 0.05f && currentState == ClientTimerData.TimerState.COUNTING) return;

        float alphaMultiplier = 1.0f;
        if (renderInfo.shouldBlink()) {
            long gameTime = Minecraft.getInstance().level.getGameTime();
            alphaMultiplier = 0.6f + 0.4f * Mth.sin(gameTime / 4.0f);
        }

        Color color = renderInfo.color();
        String text = String.format("%.1f", Math.max(0, displaySeconds));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawArc(guiGraphics, centerX, centerY, RADIUS, 0f, 360f, new Color(30, 30, 40, (int)(200 * alphaMultiplier)));
        drawArc(guiGraphics, centerX, centerY, RADIUS, -90f, 360f * Mth.clamp(progress, 0, 1), new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * alphaMultiplier)));

        Font font = Minecraft.getInstance().font;
        guiGraphics.renderFakeItem(renderInfo.item().getDefaultInstance(), centerX - 8, centerY - 12);
        guiGraphics.drawString(font, text, centerX - font.width(text) / 2, centerY + 4, color.getRGB(), true);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void drawArc(GuiGraphics guiGraphics, float cX, float cY, float r, float start, float sweep, Color color) {
        int segments = Math.max(1, (int) (Math.abs(sweep) * 0.5f));
        for (int i = 0; i < segments; i++) {
            float angle = (start + (i / (float) segments) * sweep) * Mth.DEG_TO_RAD;
            for(int w = 0; w < LINE_WIDTH; w++) {
                float px = cX + Mth.cos(angle) * (r - w);
                float py = cY + Mth.sin(angle) * (r - w);
                guiGraphics.fill((int)px, (int)py, (int)px + 1, (int)py + 1, color.getRGB());
            }
        }
    }
}