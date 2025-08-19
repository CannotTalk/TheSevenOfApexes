package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class BerserkersDragAbility {
    private BerserkersDragAbility() {}

    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "berserkers_drag_attack_damage");
    private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "berserkers_drag_attack_speed");
    private static final ResourceLocation MOVEMENT_SPEED_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "berserkers_drag_movement_speed_debuff");

    /**
     * 任務1：継続的なバフと、リスクの管理
     */
    public static void updateEffect(Player player, int dragCount, int prideMultiplier) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackDamage == null || attackSpeed == null) return;

        // まず、既存の効果をクリア
        attackDamage.removeModifier(ATTACK_DAMAGE_ID);
        attackSpeed.removeModifier(ATTACK_SPEED_ID);

        if (dragCount > 0) {
            int finalEffectiveCount = dragCount * prideMultiplier;
            // 攻撃力: +100% per count
            addModifier(attackDamage, ATTACK_DAMAGE_ID, finalEffectiveCount * 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            // 攻撃速度: +25% per count
            addModifier(attackSpeed, ATTACK_SPEED_ID, finalEffectiveCount * 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    /**
     * 任務2：ダメージ計算前に、受けるダメージを増やす
     */
    public static void onPlayerDamaged(LivingDamageEvent.Pre event, Player player, int dragCount, int prideMultiplier) {
        if (dragCount <= 0) return;

        int finalEffectiveCount = dragCount * prideMultiplier;
        float currentDamage = event.getNewDamage();
        // 受けるダメージ: +100% per count
        float newDamage = currentDamage * (1.0f + finalEffectiveCount);
        event.setNewDamage(newDamage);

        // 移動速度デバフを2秒間付与
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3)); // Slowness IV

    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}