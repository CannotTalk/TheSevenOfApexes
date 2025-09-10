package net.ardcameg.thesevenofapexes.abilities.common;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class ForbiddenMemoryAbility {
    private ForbiddenMemoryAbility() {}

    /**
     * "禁忌の記憶"の効果を適用する
     * @param player ダメージを受けたプレイヤー
     * @param damageDealt 実際に受けたダメージ量
     * @param memoryCount "禁忌の記憶"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void apply(Player player, float damageDealt, int memoryCount, int prideMultiplier) {
        if (memoryCount <= 0 || damageDealt <= 0) return;

        // "傲慢"はデメリットも増幅させる
        int finalCount = memoryCount * prideMultiplier;

        // ダメージの半分(秒)を計算し、切り上げる
        int durationInSeconds = (int) Math.ceil(damageDealt / 2.0);

        // "傲慢"と所持数に応じて効果時間を乗算する
        int finalDurationInSeconds = durationInSeconds * finalCount;

        // 秒数をtickに変換 (1秒 = 20tick)
        int durationInTicks = finalDurationInSeconds * 20;

        // "吐き気"エフェクトをプレイヤーに付与する
        // 効果レベルは0で、効果時間だけを設定
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, durationInTicks, 0));
    }
}