package net.ardcameg.thesevenofapexes.networking.packet;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.abilities.legendary.GluttonyAbility;
import net.ardcameg.thesevenofapexes.abilities.legendary.PrideAbility;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public record EatBlockC2SPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "eat_block");
    public static final CustomPacketPayload.Type<EatBlockC2SPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, EatBlockC2SPacket> STREAM_CODEC = StreamCodec.unit(new EatBlockC2SPacket());


    // typeメソッドは契約上、絶対必要
    @Override
    public Type<EatBlockC2SPacket> type() {
        return TYPE;
    }

    // handleメソッド
    public static void handle(EatBlockC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer player) {
                Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);

                int gluttonyCount = baseCounts.getOrDefault(ModItems.LEGENDARY_GLUTTONY.get(), 0);
                if (gluttonyCount > 0) {
                    int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
                    int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

                    GluttonyAbility.apply(player, gluttonyCount, prideMultiplier);
                }
            }
        });
    }
}