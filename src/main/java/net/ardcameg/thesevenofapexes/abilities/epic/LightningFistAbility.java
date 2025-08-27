package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random; // <-- java.util.Random を使うのは変わらない

public final class LightningFistAbility {
    private LightningFistAbility() {}

    public static final String CHAIN_COUNT_TAG = "LightningFistChainCount";

    // このクラス専用の、独立した乱数生成器インスタンスを作成する
    private static final Random INDEPENDENT_RANDOM = new Random();

    private static final DustParticleOptions REDSTONE_PARTICLE = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f);

    public static void applyAttackEffect(Player player, LivingEntity initialTarget, float initialDamage, int fistCount, int prideMultiplier) {
        if (fistCount <= 0 || player.level().isClientSide) return;

        ServerLevel level = (ServerLevel) player.level();

        Random random = INDEPENDENT_RANDOM;

        DamageSource damageSource = player.damageSources().playerAttack(player);
        int totalChainCount = 0;

        LivingEntity currentTarget = initialTarget;
        float currentDamage = initialDamage;

        int finalCount = fistCount * prideMultiplier;
        float chainBaseProbability = Config.lightningFistChainBaseProbability.get().floatValue();
        float chainAttackDecay = Config.lightningFistChainAttackDecay.get().floatValue();

        float chainProbability = chainBaseProbability + (chainBaseProbability / 2) * (Math.max(0, finalCount - 1));

        while (true) {
            final LivingEntity finalCurrentTarget = currentTarget;
            AABB searchArea = finalCurrentTarget.getBoundingBox().inflate(16.0);
            List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, searchArea, entity ->
                    entity instanceof Enemy &&
                            !entity.is(finalCurrentTarget) &&
                            entity.isAlive()
            );

            if (candidates.isEmpty()) {
                break;

            }
            LivingEntity nextTarget = candidates.get(random.nextInt(candidates.size()));
            BuffItemUtils.drawParticleLine(level, currentTarget, nextTarget, REDSTONE_PARTICLE);

            currentDamage *= (1 - chainAttackDecay);

            if(currentDamage < 0.01f){
                break;
            }

            nextTarget.hurt(damageSource, currentDamage);

            if (random.nextFloat() < chainProbability) {
                currentTarget = nextTarget;
                totalChainCount++;
            } else {
                break;
            }
        }

        player.getPersistentData().putInt(CHAIN_COUNT_TAG, totalChainCount);
    }
}