package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.event.AdvancementTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class GoliathsGavelAbility {
    private GoliathsGavelAbility() {}

    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "goliaths_gavel_attack_damage");
    private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "goliaths_gavel_attack_speed");
    private static final ResourceLocation MOVEMENT_SPEED_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "goliaths_gavel_movement_speed");

    /**
     * 任務1：攻撃時に範囲ダメージとノックバックを発生させる
     */
    public static void applyAreaDamage(Player player, LivingEntity initialTarget, int gavelCount, int prideMultiplier) {
        if (gavelCount <= 0 || player.level().isClientSide) return;

        int finalCount = gavelCount * prideMultiplier;

        // --- 1. 範囲とダメージを計算 ---
        float baseRadius = Config.goliathsGavelBaseRadius.get().floatValue();
        float radius = baseRadius + (baseRadius / 2) * (finalCount - 1);
        // プレイヤーの攻撃ダメージを取得 (ただし、このアイテム自体の攻撃力ボーナスは除く)
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) / (1.0f + 0.25f * finalCount);
        float damageModifier = Config.goliathsGavelDamageModifier.get().floatValue();
        float areaDamage = baseDamage * damageModifier; // 範囲ダメージは基本攻撃力の75%

        // --- 2. 範囲内のターゲットを探す ---
        AABB searchArea = initialTarget.getBoundingBox().inflate(radius, 0.5, radius);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, searchArea, entity ->
                entity != player && entity != initialTarget && entity.isAlive()
        );

        int attackedTargets = 0;
        // --- 3. 範囲ダメージとノックバックを適用 ---
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), areaDamage);

            // ノックバック処理
            Vec3 knockbackDir = target.position().subtract(player.position()).normalize();
            target.knockback(0.5 * finalCount, knockbackDir.x, knockbackDir.z);

            attackedTargets++;
            if(attackedTargets >= 5){
                AdvancementTriggers.grantAdvancement((ServerPlayer) player, "overwhelming_force");
            }
        }

        // --- 4. 演出 ---
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, initialTarget.getX(), initialTarget.getY(0.5), initialTarget.getZ(), 1, 0, 0, 0, 0);
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }

    /**
     * 任務2：継続的なバフとデバフを適用する
     */
    public static void updatePassiveEffect(Player player, int gavelCount, int prideMultiplier) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attackDamage == null || attackSpeed == null || movementSpeed == null) return;

        // まず、既存の効果をクリア
        attackDamage.removeModifier(ATTACK_DAMAGE_ID);
        attackSpeed.removeModifier(ATTACK_SPEED_ID);
        movementSpeed.removeModifier(MOVEMENT_SPEED_ID);

        if (gavelCount > 0) {
            float attackDamageBuff = Config.goliathsGavelAttackPowerModifier.get().floatValue();
            float attackSpeedDebuff = Config.goliathsGavelAttackSpeedModifier.get().floatValue();
            float movementSpeedDebuff = Config.goliathsGavelMovementSpeedModifier.get().floatValue();

            int finalEffectiveCount = gavelCount * prideMultiplier;
            float attackDamageModifier = attackDamageBuff + (finalEffectiveCount - 1) * attackDamageBuff / 2;
            float attackSpeedModifier = attackSpeedDebuff + (finalEffectiveCount - 1) * attackDamageModifier / 2;
            float movementModifier = movementSpeedDebuff + (finalEffectiveCount - 1) * movementSpeedDebuff / 2;

            // 攻撃力: +25%
            addModifier(attackDamage, ATTACK_DAMAGE_ID, attackDamageModifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            // 攻撃速度: -50%
            addModifier(attackSpeed, ATTACK_SPEED_ID, attackSpeedModifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            // 移動速度: -15%
            addModifier(movementSpeed, MOVEMENT_SPEED_ID, movementModifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}