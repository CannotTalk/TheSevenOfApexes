package net.ardcameg.thesevenofapexes.abilities.rare;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

public final class LastStandAbility {
    private LastStandAbility() {}

    private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "last_stand_attack_speed");
    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "last_stand_attack_damage");

    /**
     * 任務1：継続的なバフを、体力が低い時だけ適用する
     */
    public static void updatePassiveBuffs(Player player, int standCount, int prideMultiplier) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackSpeed == null || attackDamage == null) return;

        // --- 1. 発動条件を判定 ---
        boolean isActive = standCount > 0 && player.getHealth() <= player.getMaxHealth() * 0.25f;

        // --- 2. 条件に応じて効果を付与または解除 ---
        if (isActive) {
            int finalCount = standCount * prideMultiplier;
            float attackSpeedModifier = Config.lastStandAttackSpeedModifier.get().floatValue();
            float attackPowerModifier = Config.lastStandAttackPowerModifier.get().floatValue();
            // 攻撃速度: +50%
            addModifier(attackSpeed, ATTACK_SPEED_ID, finalCount * attackSpeedModifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            // 攻撃力: +25%
            addModifier(attackDamage, ATTACK_DAMAGE_ID, finalCount * attackPowerModifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        } else {
            // 条件を満たしていない場合、Modifierを確実に削除する
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
            attackDamage.removeModifier(ATTACK_DAMAGE_ID);
        }
    }

    /**
     * 任務2：あらゆる回復効果を増幅する
     */
    public static void onPlayerHeal(LivingHealEvent event, int standCount, int prideMultiplier) {
        int finalCount = standCount * prideMultiplier;
        float originalAmount = event.getAmount();

        // 1個で2倍、2個で3倍...というように、回復量を(finalCount + 1)倍にする
        float newAmount = originalAmount * (1.0f + finalCount);
        event.setAmount(newAmount);
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.removeModifier(id); // 常に最新の状態で上書きするために、一度削除する
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}