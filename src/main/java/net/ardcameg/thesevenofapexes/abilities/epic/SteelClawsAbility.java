package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public final class SteelClawsAbility {
    private SteelClawsAbility() {}
    private static final Random RANDOM = new Random();

    /**
     * "鋼鉄の爪"の即死効果を適用する
     */
    public static void apply(Player player, LivingEntity target, int clawCount, int prideMultiplier) {
        if (clawCount <= 0) return;

        int finalCount = clawCount * prideMultiplier;
        float procChanceBase = Config.steelClawsProcChanceBase.get().floatValue();
        float selfKillChance = Config.steelClawsSelfKillChance.get().floatValue();

        // --- 1. 発動確率を計算 ---
        float procChance = procChanceBase + (finalCount - 1) * (procChanceBase / 2);

        if (RANDOM.nextFloat() < procChance) {
            // --- 2. 確率で自爆するか判定 ---
            if (RANDOM.nextFloat() < selfKillChance) {
                // 自分を即死させる
                player.kill();
                player.sendSystemMessage(Component.translatable("message.seven_apexes.claws_activate"));
            } else {
                // ターゲットを即死させる
                target.kill();
                player.sendSystemMessage(Component.translatable("message.seven_apexes.claws_activate"));
                BuffItemUtils.playTotemAnimation(player, ModItems.EPIC_STEEL_CLAWS.get());
            }
        }
    }
}