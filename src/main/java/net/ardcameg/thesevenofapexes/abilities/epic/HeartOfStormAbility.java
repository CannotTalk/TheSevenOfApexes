// net/ardcameg/thesevenofapexes/abilities/epic/HeartOfStormAbility.java

package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.abilities.util.StunAbility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public final class HeartOfStormAbility {
    private HeartOfStormAbility() {}

    /**
     * "嵐の心臓"の効果を適用する
     * @param player プレイヤー
     * @param attacker 攻撃者
     * @param stormCount "嵐の心臓"の所持数
     * @param prideMultiplier "傲慢"による効果倍率
     * @param stunnedEntities スタン状態のエンティティを管理するセット
     */
    public static void apply(Player player, Entity attacker, int stormCount, int prideMultiplier, Set<LivingEntity> stunnedEntities) {
        // --- 事前チェック ---
        if (stormCount <= 0 || attacker == player || !(attacker instanceof LivingEntity livingAttacker) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // --- 確率計算 ---
        int finalCount = stormCount * prideMultiplier;
        float heartOfStormDamageProbability = Config.heartOfStormProbability.get().floatValue();
        float chanceModifier = Config.heartOfStormProcChanceModifier.get().floatValue();
        int stunTicks = Config.heartOfStormStunTicks.getAsInt();
        double triggerChance = 1.0 - Math.pow((1 - chanceModifier), finalCount);

        if (serverLevel.random.nextDouble() < triggerChance) {
            // --- 1. 攻撃者に雷を落とし、ダメージを与える ---
            // 視覚効果のみの雷を生成
            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            lightning.setVisualOnly(true);
            lightning.setPos(attacker.position());
            serverLevel.addFreshEntity(lightning);

            // 最大体力の25%のダメージを正確に与える
            float damageAmount = livingAttacker.getMaxHealth() * heartOfStormDamageProbability;
            livingAttacker.hurt(player.damageSources().lightningBolt(), damageAmount);

            // --- 2. プレイヤーを2秒間スタンさせる ---
            StunAbility.apply(player, stunTicks, stunnedEntities);
        }
    }
}