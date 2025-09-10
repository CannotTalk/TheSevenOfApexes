package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
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
    public static void onPrePlayerDamage(LivingDamageEvent.Pre event, Player player, Entity attacker, int hourglassCount, int prideMultiplier) {
        if (hourglassCount <= 0 || !(attacker instanceof LivingEntity livingAttacker)) return;

        // 反射ダメージによる無限ループを防ぐ
        if (event.getSource().typeHolder().is(DamageTypes.THORNS)) return; // バニラのとげや我々の反射を弾く

        int finalCount = hourglassCount * prideMultiplier;

        float reflectBaseChance = Config.reversalHourglassProcBaseChance.get().floatValue();
        float reflectChance = reflectBaseChance + (finalCount - 1) * (reflectBaseChance / 2);

        if (RANDOM.nextFloat() < reflectChance) {
            // --- 反射成功！ ---
            float damageToReflect = event.getNewDamage();
            event.setNewDamage(0); // ダメージを完全にキャンセル
            livingAttacker.hurt(player.damageSources().thorns(player), damageToReflect);
            // TODO: 反射成功の派手なエフェクト
        }
        // 反射失敗時のカウンターダメージは Post イベントで行うので、ここでは何もしない
    }

    public static void onPostPlayerDamage(LivingDamageEvent.Post event, Player player, Entity attacker, int hourglassCount, int prideMultiplier) {
        if (hourglassCount <= 0 || !(attacker instanceof LivingEntity livingAttacker)) return;

        // 既に反射が成功している（ダメージが0になっている）場合はカウンターしない
        if (event.getNewDamage() <= 0) return;

        int finalCount = hourglassCount * prideMultiplier;
        float damageTaken = event.getNewDamage();
        float reflectRate = Config.reversalHourglassReflectRate.get().floatValue();

        // --- カウンターダメージ ---
        float counterDamage = damageTaken * reflectRate * finalCount;
        livingAttacker.hurt(player.damageSources().magic(), counterDamage);
        // TODO: カウンターダメージのエフェクト
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