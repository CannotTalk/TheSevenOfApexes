package net.ardcameg.thesevenofapexes.client;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.EatBlockC2SPacket;
import net.ardcameg.thesevenofapexes.networking.packet.SoulReleaseC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = TheSevenOfApexes.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    private static int soulReleaseHoldTicks = 0;
    private static final int REQUIRED_HOLD_TICKS = 60;

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.EAT_BLOCK_KEY);
        event.register(KeyBinding.SOUL_RELEASE_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ClientTimerData.tick();

        if (KeyBinding.EAT_BLOCK_KEY.consumeClick()) {
            ModMessages.sendToServer(new EatBlockC2SPacket());
        }
        boolean hasForbiddenItem = mc.player.getInventory().items.stream().anyMatch(stack -> stack.getItem() instanceof ForbiddenItem);
        if (hasForbiddenItem && KeyBinding.SOUL_RELEASE_KEY.isDown()) {
            soulReleaseHoldTicks++;
            updateProgressBar(soulReleaseHoldTicks);
            if (soulReleaseHoldTicks >= REQUIRED_HOLD_TICKS) {
                ModMessages.sendToServer(new SoulReleaseC2SPacket());
                soulReleaseHoldTicks = 0;
            }
        } else {
            soulReleaseHoldTicks = 0;
        }
    }

    private static void updateProgressBar(int currentTicks) {
        float progress = Math.min(1.0f, (float) currentTicks / REQUIRED_HOLD_TICKS);
        int filledSteps = (int) (progress * 10);
        StringBuilder bar = new StringBuilder();
        bar.append(ChatFormatting.WHITE).append("[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filledSteps ? ChatFormatting.RED : ChatFormatting.GRAY).append("■");
        }
        bar.append(ChatFormatting.WHITE).append("]");
        Minecraft.getInstance().player.displayClientMessage(Component.literal(bar.toString()), true);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        HudManager.render(event.getGuiGraphics(), event.getPartialTick());
    }
}