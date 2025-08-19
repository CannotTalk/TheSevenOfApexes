package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MoonlightSacredSealAbility {
    private MoonlightSacredSealAbility() {}

    private static final ResourceLocation MOVEMENT_SPEED_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "moon_seal_movement_speed_debuff");
    private static final ResourceLocation NEARBY_MOB_SLOW_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "moon_seal_nearby_mob_slow");


    public static void updateEffect(Player player, int moonSealCount, int prideMultiplier, boolean hasSunSeal, Map<UUID, Vec3> playerLastPos) {
        int actualLevel = moonSealCount * prideMultiplier;

        // --- 属性(Attribute)を取得 ---
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        // --- 1. 条件を判定 ---
        boolean isNight = player.level().isNight();
        int lightLevel = player.level().getLightEmission(player.blockPosition());
        boolean isDark = isNight || lightLevel <= 7;
        boolean isBuffActive = moonSealCount > 0 && (hasSunSeal || isDark);
        boolean isDebuffActive = moonSealCount > 0 && !hasSunSeal && !isDark;

        // --- 2. プレイヤー自身のデバフ効果 ---
        if (isDebuffActive) {
            addModifier(movementSpeed, MOVEMENT_SPEED_DEBUFF_ID, -0.25 + ((actualLevel - 1) * -0.01), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        } else {
            movementSpeed.removeModifier(MOVEMENT_SPEED_DEBUFF_ID);
        }

        // --- 3. プレイヤー自身のバフ効果 ---
        if (isBuffActive) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, true, false, false));
        }

        // --- 4. 周囲のMobへのデバフ効果 ---
        Vec3 currentPos = player.position();
        Vec3 lastPos = playerLastPos.get(player.getUUID());
        boolean isStandingStill = lastPos != null && currentPos.distanceToSqr(lastPos) < 0.0001;

        // 1秒に1回だけ処理
        if (player.level().getGameTime() % 20 == 0) {
            slowDownNearbyEntities(player, moonSealCount, prideMultiplier, isBuffActive && isStandingStill);
        }
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.removeModifier(id);
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }

    private static void slowDownNearbyEntities(Player player, int moonSealCount, int prideMultiplier, boolean isActive) {
        int radius = 6 + (moonSealCount * 2);
        AABB area = player.getBoundingBox().inflate(radius);
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player);

        for (LivingEntity entity : nearbyEntities) {
            AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed == null) continue;

            if (isActive) {
                int actualLevel = moonSealCount * prideMultiplier;
                double slowAmount = -0.5 + ((actualLevel - 1) * -0.05); // -50%を基本に、追加で-5%ずつ
                AttributeModifier debuff = new AttributeModifier(NEARBY_MOB_SLOW_ID, slowAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                movementSpeed.addTransientModifier(debuff);
            } else {
                movementSpeed.removeModifier(NEARBY_MOB_SLOW_ID);
            }
        }
    }
}