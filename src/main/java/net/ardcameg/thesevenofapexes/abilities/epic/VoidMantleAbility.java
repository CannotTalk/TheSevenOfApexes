package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance; // ★ import
import net.minecraft.world.effect.MobEffects;       // ★ import
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

public final class VoidMantleAbility {
    private VoidMantleAbility() {}
    private static final Random RANDOM = new Random();

    private static final String SNEAK_TICKS_TAG = "VoidMantleSneakTicks";
    // isVanishedタグは、もはやエフェクトの有無で判定できるので不要になる

    public static void updateEffect(Player player, int mantleCount, int prideMultiplier) {
        if (mantleCount <= 0) {
            if (isVanished(player)) {
                unvanish(player);
            }
            return;
        }

        if (player.isSprinting() || !player.isShiftKeyDown()) {
            player.getPersistentData().putInt(SNEAK_TICKS_TAG, 0);
            if (isVanished(player)) {
                unvanish(player);
            }
            return;
        }

        int sneakTicks = player.getPersistentData().getInt(SNEAK_TICKS_TAG);
        sneakTicks++;
        player.getPersistentData().putInt(SNEAK_TICKS_TAG, sneakTicks);

        if (!isVanished(player)) {
            if (sneakTicks >= 60) {
                if (player.getFoodData().getFoodLevel() > 0) {
                    vanish(player);
                }
            }
        } else {
            // 1. エフェクトが途切れないように、常に効果を更新し続ける
            // 効果時間は短く(2秒=40tick)して、無駄な負荷を避ける
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false, false));

            // 2. 2秒ごとに満腹度を消費
            if (player.level().getGameTime() % 40 == 0) {
                if (player.getFoodData().getFoodLevel() > 0) {
                    player.getFoodData().addExhaustion(4.0f);
                } else {
                    unvanish(player);
                }
            }
        }
    }

    // ★★★ isVanishedの判定方法を、エフェクトの有無に変更 ★★★
    private static boolean isVanished(Player player) {
        return player.hasEffect(MobEffects.INVISIBILITY);
    }

    private static void vanish(Player player) {
        // ★★★ player.setInvisible(true) の代わりに、透明化エフェクトを付与 ★★★
        // 効果時間を長く(例:10秒)設定し、tickごとに更新することで、エフェクトが途切れないようにする
        // Amplifier 0 = Level 1, アンビエント(パーティクル減), パーティクル非表示, アイコン非表示
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, true, false, false));
        hideArmor(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    private static void unvanish(Player player) {
        // ★★★ player.setInvisible(false) の代わりに、エフェクトを解除 ★★★
        player.removeEffect(MobEffects.INVISIBILITY);
        player.getPersistentData().remove(SNEAK_TICKS_TAG);
        showArmor(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5f);

        // --- 消失判定 (変更なし) ---
        List<ItemStack> mantles = BuffItemUtils.findItemsInBuffRow(player, ModItems.EPIC_VOID_MANTLE.get());
        for (ItemStack mantle : mantles) {
            if (RANDOM.nextFloat() < 0.05f) {
                mantle.shrink(1);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_BREAK, SoundSource.PLAYERS, 0.5f, 2.0f);
                BuffItemUtils.playTotemAnimation(player, ModItems.EPIC_VOID_MANTLE.get());
            }
        }
    }

    // 他のプレイヤーに、このプレイヤーの防具が見えなくなるようにパケットを送信する
    private static void hideArmor(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> equipment = List.of(
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.HEAD, ItemStack.EMPTY),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.CHEST, ItemStack.EMPTY),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.LEGS, ItemStack.EMPTY),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.FEET, ItemStack.EMPTY)
            );
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(player.getId(), equipment));
        }
    }

    // 他のプレイヤーに、このプレイヤーの防具が再び見えるようにパケットを送信する
    private static void showArmor(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> equipment = List.of(
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD)),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST)),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS)),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET))
            );
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(player.getId(), equipment));
        }
    }
}