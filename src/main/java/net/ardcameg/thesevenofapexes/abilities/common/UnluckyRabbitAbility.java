package net.ardcameg.thesevenofapexes.abilities.common;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class UnluckyRabbitAbility {
    private UnluckyRabbitAbility() {}

    /**
     * "不運のウサギ"の効果を適用する
     * @param player プレイヤー
     * @param rabbitCount "不運のウサギ"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void updateEffect(Player player, int rabbitCount, int prideMultiplier) {
        if (rabbitCount > 0) {
            // "傲慢"の効果で不運のレベルが上がる
            int finalAmplifier = (rabbitCount * prideMultiplier) - 1;

            // 不運エフェクトを付与する。
            // 継続時間は短く設定し(1秒=20tick)、毎tick更新することで永続させる。
            player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 20, finalAmplifier, true, false, false));
        }
    }
}