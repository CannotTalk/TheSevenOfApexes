package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

public final class ArrivalOfRevivalAbility {
    private ArrivalOfRevivalAbility() {}
    private static final Random RANDOM = new Random();

    /**
     * "復活の時"の効果を試みる
     * @param player 死亡しかけているプレイヤー
     * @return 復活に成功すればtrue
     */
    public static boolean attemptRevive(Player player, int revivalCount, int prideMultiplier) {
        if (!BuffItemUtils.consumeItemFromBuffRow(player, ModItems.EPIC_ARRIVAL_OF_REVIVAL.get())) {
            return false;
        }

        player.setHealth(1.0f);
        BuffItemUtils.clearAllDebuffs(player);
        int finalCount = revivalCount * prideMultiplier;

        // --- 爆発（範囲ダメージ + 視覚効果） ---
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            // 1. 視覚と聴覚のエフェクト
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 2, 1.0, 0.0, 0.0, 0);
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 1.0f);

            // 2. 範囲ダメージのロジック
            float baseRadius = Config.arrivalOfRevivalBaseRadius.get().floatValue();
            float radius = baseRadius + (baseRadius / 2) * (finalCount - 1); // 爆発の半径
            float damage = Config.arrivalOfRevivalBaseDamage.get().floatValue(); // 爆発ダメージ
            AABB searchArea = player.getBoundingBox().inflate(radius);

            // 範囲内のLivingEntity（プレイヤー自身を除く）を全て取得
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchArea, entity -> entity != player && entity.isAlive());

            for (LivingEntity target : targets) {
                // ダメージソースを「プレイヤーによる爆発」としてダメージを与える
                target.hurt(player.damageSources().explosion(player, null), damage);
            }
        }

        float clearProbability = Config.arrivalOfRevivalClearProbability.get().floatValue();
        if (RANDOM.nextFloat() < clearProbability) {
            player.getInventory().clearContent();
            player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0f, 0.5f);
        }

        BuffItemUtils.playTotemAnimation(player, ModItems.EPIC_ARRIVAL_OF_REVIVAL.get());

        return true;
    }
}