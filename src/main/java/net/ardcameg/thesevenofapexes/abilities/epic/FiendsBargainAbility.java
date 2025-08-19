package net.ardcameg.thesevenofapexes.abilities.epic;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class FiendsBargainAbility {
    private FiendsBargainAbility() {}

    private static final int XP_COST_PER_ITEM = 5; // 1個あたりの経験値レベルコスト

    /**
     * "悪魔との契約書"の効果を適用する
     * @param event ダメージイベント (Pre)
     * @param player 攻撃したプレイヤー
     * @param target 攻撃された対象
     * @param bargainCount "契約書"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void apply(LivingDamageEvent.Pre event, Player player, LivingEntity target, int bargainCount, int prideMultiplier) {
        if (bargainCount <= 0) return;

        int finalCount = bargainCount * prideMultiplier;
        int totalXpCost = XP_COST_PER_ITEM + (int)((finalCount - 1) *  (XP_COST_PER_ITEM / 4)) ;

        // --- 1. 対価を支払えるか判定 ---
        if (player.experienceLevel >= totalXpCost) {
            // --- 契約成功 ---
            // 1. 対価を支払う
            player.giveExperienceLevels(-totalXpCost);

            // 2. ダメージを計算
            float currentDamage = event.getNewDamage();
            // クリティカル(+50%) + 追加ダメージ(+100%) = 150%ボーナス
            // これをアイテムの有効数分、乗算する
            float damageMultiplier = 1.0f + (1.5f * finalCount);
            float finalDamage = currentDamage * damageMultiplier;
            event.setNewDamage(finalDamage);

            // 3. 演出
            player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.5f, 2.0f);
            // TODO: ここにクリティカルヒットを示す特別なパーティクルを追加すると最高

        } else {
            // --- 契約失敗 ---
            // 1. 本来与えるはずだったダメージを記録
            float selfDamage = event.getNewDamage();
            selfDamage *= (1 - (finalCount - 1) * 0.05f);
            // 2. 相手への攻撃を無効化する
            event.setNewDamage(0f);

            // 3. 自分にダメージを跳ね返す
            player.hurt(player.damageSources().magic(), selfDamage);

            // 4. 演出
            player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}