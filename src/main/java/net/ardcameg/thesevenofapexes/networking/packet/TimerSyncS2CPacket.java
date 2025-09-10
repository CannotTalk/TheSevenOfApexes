package net.ardcameg.thesevenofapexes.networking.packet;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.client.ClientTimerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TimerSyncS2CPacket(String timerId, int ticksLeft, int timerState, int animationTicks, int animationDuration, int animationTargetTicks, int maxTicks) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "timer_sync");
    public static final Type<TimerSyncS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TimerSyncS2CPacket> STREAM_CODEC = StreamCodec.of(
            // 【最終修正】エンコーダーを明確なラムダ式に書き換え
            (buf, packet) -> packet.write(buf),
            TimerSyncS2CPacket::new
    );

    // デコーダー（バイト配列からレコードを復元）
    private TimerSyncS2CPacket(RegistryFriendlyByteBuf buf) {
        this(
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf)
        );
    }

    // エンコーダー（レコードからバイト配列に変換）
    private void write(RegistryFriendlyByteBuf buf) {
        ByteBufCodecs.STRING_UTF8.encode(buf, this.timerId);
        ByteBufCodecs.VAR_INT.encode(buf, this.ticksLeft);
        ByteBufCodecs.VAR_INT.encode(buf, this.timerState);
        ByteBufCodecs.VAR_INT.encode(buf, this.animationTicks);
        ByteBufCodecs.VAR_INT.encode(buf, this.animationDuration);
        ByteBufCodecs.VAR_INT.encode(buf, this.animationTargetTicks);
        ByteBufCodecs.VAR_INT.encode(buf, this.maxTicks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final TimerSyncS2CPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientTimerData.updateTimer(packet.timerId, packet.ticksLeft, packet.timerState, packet.animationTicks, packet.animationDuration, packet.animationTargetTicks, packet.maxTicks));
    }
}