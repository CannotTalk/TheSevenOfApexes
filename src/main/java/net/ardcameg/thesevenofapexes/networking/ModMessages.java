package net.ardcameg.thesevenofapexes.networking;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.networking.packet.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModMessages {
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            final PayloadRegistrar registrar = event.registrar(TheSevenOfApexes.MOD_ID).versioned("1.0");

            registrar.playToServer(
                    EatBlockC2SPacket.TYPE,
                    EatBlockC2SPacket.STREAM_CODEC,
                    EatBlockC2SPacket::handle
            );

            registrar.playToClient(
                    PhoenixDebuffSyncS2CPacket.TYPE,
                    PhoenixDebuffSyncS2CPacket.STREAM_CODEC,
                    PhoenixDebuffSyncS2CPacket::handle
            );

            registrar.playToClient(
                    PlayTotemAnimationS2CPacket.TYPE,
                    PlayTotemAnimationS2CPacket.STREAM_CODEC,
                    PlayTotemAnimationS2CPacket::handle
            );

            registrar.playToClient(
                    BargeTimerSyncS2CPacket.TYPE,
                    BargeTimerSyncS2CPacket.STREAM_CODEC,
                    BargeTimerSyncS2CPacket::handle
            );

            registrar.playToServer(
                    SoulReleaseC2SPacket.TYPE,
                    SoulReleaseC2SPacket.STREAM_CODEC,
                    SoulReleaseC2SPacket::handle
            );

            registrar.playToClient(
                    TimerSyncS2CPacket.TYPE,
                    TimerSyncS2CPacket.STREAM_CODEC,
                    TimerSyncS2CPacket::handle
            );
        });
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToPlayer(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}