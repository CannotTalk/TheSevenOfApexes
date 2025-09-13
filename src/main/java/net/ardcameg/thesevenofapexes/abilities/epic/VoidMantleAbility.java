package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

public final class VoidMantleAbility {
    private VoidMantleAbility() {}
    private static final Random RANDOM = new Random();

    private static final String SNEAK_TICKS_TAG = "VoidMantleSneakTicks";
    // 我々の能力による透明化であることを示す、独立したNBTタグを使う
    private static final String MANTLE_INVISIBILITY_TAG = "MantleInvisibility";

    public static void updateEffect(Player player, int mantleCount, int prideMultiplier) {
        if (mantleCount <= 0) {
            if (isVanishedByMantle(player)) {
                unvanish(player);
            }
            return;
        }

        if (player.isSprinting() || !player.isShiftKeyDown()) {
            player.getPersistentData().putInt(SNEAK_TICKS_TAG, 0);
            if (isVanishedByMantle(player)) {
                unvanish(player);
            }
            return;
        }

        int sneakTicks = player.getPersistentData().getInt(SNEAK_TICKS_TAG);
        sneakTicks++;
        player.getPersistentData().putInt(SNEAK_TICKS_TAG, sneakTicks);

        if (!isVanishedByMantle(player)) {
            if (sneakTicks >= 60) {
                if (player.getFoodData().getFoodLevel() > 0) {
                    vanish(player);
                }
            }
        } else {
            // --- 既に外套で透明化している場合 ---
            // 1. サーバーの同期に打ち勝つため、毎tick、強制的に透明化と装備非表示を命令し続ける
            player.setInvisible(true);
            hideEquipment(player);

            float consumeIntervalTicks = Config.voidMantleConsumeIntervalTicks.getAsInt();
            int consumePoint = Config.voidMantleConsumeHungerPoint.get();

            // 2. 満腹度消費
            if (player.level().getGameTime() % consumeIntervalTicks == 0) {
                if (player.getFoodData().getFoodLevel() > 0) {
                    player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - consumePoint);
                } else {
                    unvanish(player);
                }
            }
        }
    }

    // NBTタグの有無で判定
    private static boolean isVanishedByMantle(Player player) {
        return player.getPersistentData().getBoolean(MANTLE_INVISIBILITY_TAG);
    }

    private static void vanish(Player player) {
        player.getPersistentData().putBoolean(MANTLE_INVISIBILITY_TAG, true); // 証拠を残す
        player.setInvisible(true);
        hideEquipment(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    private static void unvanish(Player player) {
        player.getPersistentData().remove(MANTLE_INVISIBILITY_TAG); // 証拠を消す
        player.getPersistentData().remove(SNEAK_TICKS_TAG);

        // もしプレイヤーが、他の理由（ポーションなど）で透明化しているなら、setInvisible(false)は呼び出さない
        if (!player.hasEffect(MobEffects.INVISIBILITY)) {
            player.setInvisible(false);
        }

        showEquipment(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5f);

        // --- 消失判定 ---
        List<ItemStack> mantles = BuffItemUtils.findItemsInBuffRow(player, ModItems.EPIC_VOID_MANTLE.get());
        float vanishChance = Config.voidMantleVanishChance.get().floatValue();
        for (ItemStack mantle : mantles) {
            if (RANDOM.nextFloat() < vanishChance) {
                mantle.shrink(1);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_BREAK, SoundSource.PLAYERS, 0.5f, 2.0f);
                BuffItemUtils.playTotemAnimation(player, ModItems.EPIC_VOID_MANTLE.get());
            }
        }
    }

    private static void hideEquipment(Player player) {
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

    private static void showEquipment(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> equipment = List.of(
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD)),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST)),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS)),
                    com.mojang.datafixers.util.Pair.of(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET))
            );
            serverPlayer.connection.send(new ClientboundSetEquipmentPacket(player.getId(), equipment));
            // インベントリの再同期は、防具だけなら不要
        }
    }
}