package net.ardcameg.thesevenofapexes.networking.packet;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayTotemAnimationS2CPacket(Item item) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "play_totem_animation");
    public static final Type<PlayTotemAnimationS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PlayTotemAnimationS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> packet.write(buf),
            PlayTotemAnimationS2CPacket::new
    );

    public PlayTotemAnimationS2CPacket(FriendlyByteBuf buf) {
        // 処理を一行にまとめ、コンストラクタの先頭でthis()を呼び出す
        this(BuiltInRegistries.ITEM.get(buf.readResourceLocation()));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(this.item));
    }

    @Override
    public Type<PlayTotemAnimationS2CPacket> type() {
        return TYPE;
    }

    public static void handle(PlayTotemAnimationS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.item != null && packet.item != Items.AIR) {
                Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(packet.item));
            }
        });
    }
}