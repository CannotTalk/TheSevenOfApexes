package net.ardcameg.thesevenofapexes.networking.packet;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PhoenixDebuffSyncS2CPacket(int remainingTicks) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "phoenix_debuff_sync");
    public static final Type<PhoenixDebuffSyncS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PhoenixDebuffSyncS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> packet.write(buf), // 第1引数: 書き込み担当 (StreamEncoder)
            PhoenixDebuffSyncS2CPacket::new    // 第2引数: 読み込み担当 (StreamDecoder)
    );

    public PhoenixDebuffSyncS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.remainingTicks);
    }

    @Override
    public Type<PhoenixDebuffSyncS2CPacket> type() {
        return TYPE;
    }

    public static void handle(PhoenixDebuffSyncS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (packet.remainingTicks > 0) {
                    int remainingSeconds = (packet.remainingTicks + 19) / 20;
                    mc.player.displayClientMessage(
                            Component.translatable("message.seven_apexes.phoenix_debuff", remainingSeconds),
                            true
                    );
                }
            }
        });
    }
}