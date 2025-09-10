package net.ardcameg.thesevenofapexes.networking.packet;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// secondsHeldフィールドを削除し、空のレコードに戻す
public record SoulReleaseC2SPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "soul_release");
    public static final Type<SoulReleaseC2SPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SoulReleaseC2SPacket> STREAM_CODEC = StreamCodec.unit(new SoulReleaseC2SPacket());

    @Override
    public Type<SoulReleaseC2SPacket> type() {
        return TYPE;
    }

    public static void handle(SoulReleaseC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                boolean hasForbiddenItem = player.getInventory().items.stream()
                        .anyMatch(stack -> stack.getItem() instanceof ForbiddenItem);

                // サーバー側で最終確認。本当に禁忌アイテムを持っている場合のみ実行
                if (hasForbiddenItem) {
                    player.kill();
                }
            }
        });
    }
}