package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
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
        if (moonSealCount <= 0) {
            // アイテムを持っていない場合は、念のため周囲のMobのスロー効果を解除する
            if (player.level().getGameTime() % 20 == 0) {
                slowDownNearbyEntities(player, 0, 1, false);
            }
            return;
        }

        int actualLevel = moonSealCount * prideMultiplier;

        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        // --- 1. 条件を判定 (ロジック修正) ---
        boolean isNight = player.level().isNight();

        // 「空の明るさ」と「ブロックの明るさ」を取得し、より明るい方を採用する
        int skyLight = player.level().getBrightness(LightLayer.SKY, player.blockPosition());
        int blockLight = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
        int finalLightLevel = Math.max(skyLight, blockLight);

        // 空が見えるかどうかも判定材料に加える
        boolean canSeeSky = player.level().canSeeSky(player.blockPosition());

        // 暗いかどうかの判定：夜、または（空が見えず、明るさが7以下）の場合
        boolean isDark = isNight || (!canSeeSky && finalLightLevel <= 7);

        boolean isBuffActive = hasSunSeal || isDark;
        boolean isDebuffActive = !hasSunSeal && !isDark;

        // --- 2. プレイヤー自身のデバフ効果 ---
        movementSpeed.removeModifier(MOVEMENT_SPEED_DEBUFF_ID);
        if (isDebuffActive) {
            float speedBaseMultiplier = Config.moonsealDebuffSpeedBaseMultiplier.get().floatValue();
            float speedMultiplierModifier = Config.moonsealDebuffSpeedMultiplierModifier.get().floatValue();
            addModifier(movementSpeed, MOVEMENT_SPEED_DEBUFF_ID,
                    speedBaseMultiplier + ((actualLevel - 1) * speedMultiplierModifier),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        // --- 3. プレイヤー自身のバフ効果 ---
        // 以前のコードでは耐性効果が永続しなかったため、効果時間を延長(20秒=400tick)
        if (isBuffActive) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1, true, false, false));
        }

        // --- 4. 周囲のMobへのデバフ効果 ---
        Vec3 currentPos = player.position();
        Vec3 lastPos = playerLastPos.get(player.getUUID());
        boolean isStandingStill = lastPos != null && currentPos.distanceToSqr(lastPos) < 0.0001;

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

            movementSpeed.removeModifier(NEARBY_MOB_SLOW_ID);

            if (isActive) {
                int actualLevel = moonSealCount * prideMultiplier;
                float slowdownBase = Config.moonsealBuffBaseSlowdown.get().floatValue();
                float slowdownModifier = Config.moonsealBuffSlowdownModifier.get().floatValue();
                double slowAmount = slowdownBase + ((actualLevel - 1) * slowdownModifier);
                AttributeModifier debuff = new AttributeModifier(NEARBY_MOB_SLOW_ID, slowAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                movementSpeed.addTransientModifier(debuff);
            }
        }
    }
}