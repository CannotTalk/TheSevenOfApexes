package net.ardcameg.thesevenofapexes.abilities.common;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class ShiningAuraAbility {
    private ShiningAuraAbility() {}

    /**
     * "輝くオーラ"の効果を適用する
     * @param player プレイヤー
     * @param auraCount "輝くオーラ"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void updateEffect(Player player, int auraCount, int prideMultiplier) {
        // --- 毎tick効果を管理 ---
        if (auraCount > 0) {
            // "傲慢"で効果が増幅される（発光エフェクトのレベルが上がる）
            int finalAmplifier = (auraCount * prideMultiplier) - 1;

            // 発光エフェクトを付与する。
            // 継続時間は短く設定し(5秒=100tick)、毎tick更新することで永続させる。
            // これにより、アイテムを外した時にすぐに効果が消える。
            // showParticlesとshowIconはfalseにして、見た目をクリーンに保つ。
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, finalAmplifier, true, false, false));
        }
        // auraCountが0の場合は何もしない。
        // MobEffectInstanceは時間経過で自然に消えるため、明示的に削除する必要はない。
    }
}