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
            final PayloadRegistrar registrar = event.registrar(TheSevenOfApexes.MOD_ID)
                    .versioned("1.0");

            registrar.playToServer(
                    EatBlockC2SPacket.TYPE,         // 期待値: Type<T>
                    EatBlockC2SPacket.STREAM_CODEC, // 期待値: StreamCodec
                    EatBlockC2SPacket::handle       // 期待値: IPayloadHandler
            );

            // クライアント行きの手紙(S2C)の受付登録
            registrar.playToClient(
                    PhoenixDebuffSyncS2CPacket.TYPE,
                    PhoenixDebuffSyncS2CPacket.STREAM_CODEC,
                    // handler.client((packet, context) -> PhoenixDebuffSyncS2CPacket.handle(packet, context))
                    // 上記は以下のように省略できる
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
        });
    }

    // これは変更なし
    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    // 特定のプレイヤーに手紙を送るための便利メソッド
    public static void sendToPlayer(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}