package net.ardcameg.thesevenofapexes.abilities.util;

import net.ardcameg.thesevenofapexes.abilities.forbidden.ReversalArtifactAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class WorldsRejectionAbility {
    private WorldsRejectionAbility() {}

    public static void updateBossBehavior(LivingEntity boss) {
        if (boss instanceof WitherBoss wither) {
            if (wither.level().getGameTime() % 20 != 0) return;
            ServerPlayer reversedPlayer = findReversedPlayerInRange(wither, 64.0);
            if (reversedPlayer != null) {
                enhanceWither(wither, reversedPlayer);
            }
        }
    }

    private static void enhanceWither(WitherBoss wither, ServerPlayer target) {
        Level level = wither.level();
        wither.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false));
        wither.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, true, false));
        if (level.random.nextFloat() < 0.25f) {
            int spawnCount = 1 + level.random.nextInt(2);
            for (int i = 0; i < spawnCount; i++) {
                WitherSkeleton skeleton = new WitherSkeleton(EntityType.WITHER_SKELETON, level);
                double angle = level.random.nextDouble() * 2 * Math.PI;
                double x = wither.getX() + Math.cos(angle) * 3.0;
                double z = wither.getZ() + Math.sin(angle) * 3.0;
                skeleton.setPos(x, wither.getY() + 1, z);
                level.addFreshEntity(skeleton);
            }
        }
        if (level.random.nextFloat() < 0.05f) {
            int spawnCount = 1 + level.random.nextInt(3);
            for (int i = 0; i < spawnCount; i++) {
                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
                double angle = level.random.nextDouble() * 2 * Math.PI;
                double xDis = 1 + level.random.nextInt(5) + level.random.nextDouble();
                double zDis = 1 + level.random.nextInt(5) + level.random.nextDouble();
                double x = wither.getX() + Math.cos(angle) * xDis;
                double z = wither.getZ() + Math.sin(angle) * zDis;
                lightningBolt.setPos(x, target.getY(), z);
                level.addFreshEntity(lightningBolt);
            }
        }
    }

    public static void onCrystalDestroyed(EndCrystal crystal, ServerPlayer player) {
        if (!ReversalArtifactAbility.checkForbiddenReversed(player)) return;
        Level level = crystal.level();
        level.explode(null, crystal.getX(), crystal.getY(), crystal.getZ(), 8.0F, Level.ExplosionInteraction.BLOCK);
        AABB effectArea = crystal.getBoundingBox().inflate(12.0);
        List<ServerPlayer> playersInArea = level.getEntitiesOfClass(ServerPlayer.class, effectArea);
        for (ServerPlayer p : playersInArea) {
            p.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
        }
    }

    public static void updateEnderDragonBehavior(EnderDragon dragon) {
        ServerPlayer reversedPlayer = findReversedPlayerInRange(dragon, 128.0);
        if (reversedPlayer == null) return;

        Level level = dragon.level();

        // フェーズ3：玉座での決死の抵抗 (1秒に1回)
        if (level.getGameTime() % 20 == 0) {
            if (dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING && dragon.isAlive()) {
                AABB debuffArea = dragon.getBoundingBox().inflate(30.0);
                // ここでは、reversedPlayerだけではなく、範囲内の全てのプレイヤーに影響を与える
                List<ServerPlayer> playersInArea = level.getEntitiesOfClass(ServerPlayer.class, debuffArea);
                for (ServerPlayer p : playersInArea) {
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                }
            }
        }

        // フェーズ2：汚染された息吹 (2秒に1回判定)
        if (level.getGameTime() % 40 == 0) {
            // ドラゴンファイトが進行中であることだけを確認
            if (dragon.getDragonFight() != null) {
                if (dragon.getRandom().nextFloat() < 0.25f) {
                    EnderMan enderMan = new EnderMan(EntityType.ENDERMAN, level);
                    enderMan.setPos(reversedPlayer.getX(), reversedPlayer.getY(), reversedPlayer.getZ());
                    enderMan.setTarget(reversedPlayer); // 召喚直後から反転プレイヤーを敵対
                    level.addFreshEntity(enderMan);
                }
            }
        }
    }

    private static ServerPlayer findReversedPlayerInRange(LivingEntity entity, double range) {
        if (entity.level().isClientSide()) return null;
        List<ServerPlayer> nearbyPlayers = entity.level().getEntitiesOfClass(
                ServerPlayer.class,
                entity.getBoundingBox().inflate(range),
                p -> !p.isSpectator() && !p.isCreative() // スペクテイターとクリエイティブを除外
        );
        for (ServerPlayer player : nearbyPlayers) {
            if (ReversalArtifactAbility.checkForbiddenReversed(player)) {
                return player; // 最初に見つかった有効な反転プレイヤーを返す
            }
        }
        return null;
    }
}