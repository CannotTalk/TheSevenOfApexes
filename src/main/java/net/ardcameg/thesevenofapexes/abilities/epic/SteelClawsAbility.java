package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
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

        int finalEffectiveCount = clawCount * prideMultiplier;

        // --- 1. 発動確率を計算 ---
        // 基本2.5%、追加1個あたり1.25%上昇
        float procChance = 0.025f + (finalEffectiveCount - 1) * 0.0125f;

        if (RANDOM.nextFloat() < procChance) {
            // --- 2. 1%の確率で自爆するか判定 ---
            if (RANDOM.nextFloat() < 0.01f) {
                // 自分を即死させる
                player.kill();
            } else {
                // ターゲットを即死させる
                target.kill();
                // TODO: ここに派手な即死エフェクト（パーティクル、サウンド）を追加
                BuffItemUtils.playTotemAnimation(player, ModItems.EPIC_STEEL_CLAWS.get());
            }
        }
    }
}