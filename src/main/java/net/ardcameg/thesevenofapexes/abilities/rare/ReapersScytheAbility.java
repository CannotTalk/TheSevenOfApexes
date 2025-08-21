package net.ardcameg.thesevenofapexes.abilities.rare;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ReapersScytheAbility {
    private ReapersScytheAbility() {}

    private static final int WITHER_LEVEL = 1; // レベル2は内部的に1として扱う

    /**
     * 任務1：攻撃した相手に「衰弱」を付与する
     * @param target 攻撃された対象
     * @param scytheCount "漆黒鎌"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void applyWitherOnAttack(LivingEntity target, int scytheCount, int prideMultiplier) {
        if (scytheCount <= 0) return;

        int finalCount = scytheCount * prideMultiplier;
        int durationInSeconds = finalCount;
        int durationInTicks = durationInSeconds * 20; // 20tick/s

        // ターゲットに衰弱エフェクトを付与
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, durationInTicks, WITHER_LEVEL));
    }

    /**
     * 任務2：攻撃を受けた自分に「衰弱」を付与する
     * @param player 攻撃されたプレイヤー
     * @param scytheCount "漆黒鎌"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void applyWitherOnDamaged(Player player, int scytheCount, int prideMultiplier) {
        if (scytheCount <= 0) return;

        int finalCount = scytheCount * prideMultiplier;
        int durationInSeconds = finalCount;
        int durationInTicks = durationInSeconds * 20;

        // プレイヤー自身に衰弱エフェクトを付与
        player.addEffect(new MobEffectInstance(MobEffects.WITHER, durationInTicks, WITHER_LEVEL));
    }
}