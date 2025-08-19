package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class WrathAbility {
    private WrathAbility() {}

    /**
     * "憤怒"の効果を適用する
     * @param player プレイヤー
     * @param attacker 攻撃者
     * @param finalCount "憤怒"の素の数 + "傲慢"の数
     * @param multiplier "傲慢"によって計算された効果倍率
     */
    public static void apply(Player player, Entity attacker, int finalCount, int multiplier) {
        if (finalCount <= 0) return;

        if(attacker == player) return; //自傷ダメージなら何もしない

        // 見た目だけの雷を落とす
        if (player.level() instanceof ServerLevel serverLevel) {
            LightningBolt visualLightning = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            visualLightning.setVisualOnly(true);
            visualLightning.setPos(attacker.position());
            serverLevel.addFreshEntity(visualLightning);
        }

        if (attacker instanceof LivingEntity livingAttacker) {
            // 最終的な発動回数でダメージ割合を計算
            float totalDamageMultiplier = 1.0f - (float)Math.pow(0.5, finalCount);
            // さらに、傲慢による効果倍率を掛け合わせる
            float damageAmount = livingAttacker.getMaxHealth() * totalDamageMultiplier * multiplier;
            livingAttacker.hurt(player.damageSources().lightningBolt(), damageAmount);
        }
    }
}