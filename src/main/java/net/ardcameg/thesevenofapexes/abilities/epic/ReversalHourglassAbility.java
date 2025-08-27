package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Random;

public final class ReversalHourglassAbility {
    private ReversalHourglassAbility() {}
    private static final Random RANDOM = new Random();
    private static final ResourceLocation HEALTH_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "reversal_hourglass_health_debuff");

    /**
     * 任務1：ダメージを受けた後に、反射またはカウンターダメージを与える
     */
    public static void onPlayerDamaged(LivingDamageEvent.Post event, Player player, Entity attacker, int hourglassCount, int prideMultiplier) {
        if (hourglassCount <= 0 || !(attacker instanceof LivingEntity livingAttacker)) return;

        int finalCount = hourglassCount * prideMultiplier;
        float damageTaken = event.getNewDamage();

        // --- 1. 反射確率を計算 ---
        // 基本10%、追加1個あたり1%上昇
        float reflectBaseChance = Config.reversalHourglassProcBaseChance.get().floatValue();
        float reflectChance = reflectBaseChance + (finalCount - 1) * (reflectBaseChance / 2);
        float reflectRate = Config.reversalHourglassReflectRate.get().floatValue();

        if (RANDOM.nextFloat() < reflectChance) {
            // --- 反射成功！ ---
            // プレイヤーが受けたダメージを0にし、そのダメージをそのまま相手に返す
            // Postイベントではダメージ変更できないため、プレイヤーの体力を手動で戻す
            player.setHealth(player.getHealth() + damageTaken);
            livingAttacker.hurt(player.damageSources().thorns(player), damageTaken);
            // TODO: 反射成功の派手なエフェクト
        } else {
            // --- 反射失敗、カウンターダメージ ---
            // 受けたダメージの50%を計算
            float counterDamage = damageTaken * reflectRate * finalCount;
            livingAttacker.hurt(player.damageSources().magic(), counterDamage);
            // TODO: カウンターダメージのエフェクト
        }
    }

    /**
     * 任務2：継続的な最大HP減少効果を適用、または解除する
     */
    public static void updatePassiveDebuff(Player player, int hourglassCount) {
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        healthAttribute.removeModifier(HEALTH_DEBUFF_ID);

        float penalty = Config.reversalHourglassMaxHealthPenalty.get().floatValue();

        if (hourglassCount > 0) {
            // -25%のデバフ。複数持っていても重ならない
            AttributeModifier healthDebuff = new AttributeModifier(
                    HEALTH_DEBUFF_ID,
                    penalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            healthAttribute.addPermanentModifier(healthDebuff);
        }
    }
}