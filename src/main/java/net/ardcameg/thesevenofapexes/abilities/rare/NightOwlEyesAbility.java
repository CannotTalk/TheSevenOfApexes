package net.ardcameg.thesevenofapexes.abilities.rare;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class NightOwlEyesAbility {
    private NightOwlEyesAbility() {}

    private static final ResourceLocation ATTACK_DAMAGE_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "night_owl_eyes_attack_debuff");
    private static final ResourceLocation MOVEMENT_SPEED_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "night_owl_eyes_movement_debuff");

    /**
     * "夜梟の瞳"の継続効果を管理する
     */
    public static void updateEffect(Player player, int eyeCount, int prideMultiplier) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attackDamage == null || movementSpeed == null) return;

        // --- まず、全ての効果をリセットする ---
        attackDamage.removeModifier(ATTACK_DAMAGE_DEBUFF_ID);
        movementSpeed.removeModifier(MOVEMENT_SPEED_DEBUFF_ID);

        // アイテムを持っていなければ、ここで処理終了
        if (eyeCount <= 0) return;

        // --- 条件に応じて効果を適用 ---
        if (player.level().isNight()) {
            // --- 夜のバフ ---
            // 暗視エフェクトを付与。アイコンが邪魔にならないように調整
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false, false));

        } else {
            // --- 昼のデバフ ---
            int finalCount = eyeCount * prideMultiplier;
            // 攻撃力: -25%
            addModifier(attackDamage, ATTACK_DAMAGE_DEBUFF_ID, finalCount * -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            // 移動速度: -10%
            addModifier(movementSpeed, MOVEMENT_SPEED_DEBUFF_ID, finalCount * -0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}