// src/main/java/net/ardcameg/thesevenofapexes/client/ClientEvents.java
package net.ardcameg.thesevenofapexes.client;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.networking.ModMessages; // これを追加
import net.ardcameg.thesevenofapexes.networking.packet.EatBlockC2SPacket; // これを追加
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = TheSevenOfApexes.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.EAT_BLOCK_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBinding.EAT_BLOCK_KEY.consumeClick()) {
            // 郵便局を通して、新しい「ブロック食べたよ」の手紙をサーバーに送る
            ModMessages.sendToServer(new EatBlockC2SPacket());
        }
    }
}