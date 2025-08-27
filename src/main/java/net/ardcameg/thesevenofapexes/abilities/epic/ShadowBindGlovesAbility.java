package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.abilities.util.StunAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Random;
import java.util.Set;

public final class ShadowBindGlovesAbility {
    private ShadowBindGlovesAbility() {}

    private static final Random RANDOM = new Random();

    /**
     * "影縫の手袋"の効果を適用する
     * @param player 攻撃したプレイヤー
     * @param target 攻撃された対象
     * @param gloveCount "手袋"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void apply(Player player, LivingEntity target, int gloveCount, int prideMultiplier, Set<LivingEntity> stunnedEntities) {
        if (gloveCount <= 0) return;

        // --- 1. 発動確率を計算 ---
        float procBaseChance = Config.shadowBindGlovesProcBaseChance.get().floatValue();
        int stunTicks = Config.shadowBindGlovesStunTicks.getAsInt();
        float procChance = procBaseChance + (procBaseChance / 2) * (gloveCount * prideMultiplier - 1);
        if (RANDOM.nextFloat() < procChance) {
            // --- 2. 10%の確率で自爆するか判定 ---
            if (RANDOM.nextFloat() < procChance) {
                if (RANDOM.nextFloat() < 0.1f) {
                    StunAbility.apply(player, stunTicks, stunnedEntities);
                } else {
                    StunAbility.apply(target, stunTicks, stunnedEntities);
                }
            }
        }
    }
}