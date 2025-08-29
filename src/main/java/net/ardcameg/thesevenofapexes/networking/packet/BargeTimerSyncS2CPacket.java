package net.ardcameg.thesevenofapexes.networking.packet;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BargeTimerSyncS2CPacket(int remainingTicks) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "barge_timer_sync");
    public static final Type<BargeTimerSyncS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, BargeTimerSyncS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> packet.write(buf),
            BargeTimerSyncS2CPacket::new
    );

    public BargeTimerSyncS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.remainingTicks);
    }

    @Override
    public Type<BargeTimerSyncS2CPacket> type() {
        return TYPE;
    }

    public static void handle(BargeTimerSyncS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (packet.remainingTicks > 0) {
                    // tickを秒に変換 (切り上げ)
                    int remainingSeconds = (packet.remainingTicks + 19) / 20;
                    // ステータスバー (アクションバー) にメッセージを表示
                    mc.player.displayClientMessage(
                            Component.translatable("message.seven_apexes.barge_timer", remainingSeconds),
                            true // trueでアクションバーに表示
                    );
                }
            }
        });
    }
}