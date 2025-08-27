package net.ardcameg.thesevenofapexes.abilities.rare;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class SnipersMonocleAbility {
    private SnipersMonocleAbility() {}

    private static final ResourceLocation MELEE_DAMAGE_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "snipers_monocle_melee_debuff");

    /**
     * 任務1：プレイヤーが矢を放った瞬間に、その矢を強化する
     */
    public static void onArrowFired(EntityJoinLevelEvent event, Player player, int monocleCount, int prideMultiplier) {
        // このメソッドが呼ばれる時点で、event.getEntity()はAbstractArrowであることが保証されている
        AbstractArrow arrow = (AbstractArrow) event.getEntity();

        int finalCount = monocleCount * prideMultiplier;

        // --- 矢の弾速(Velocity)を増加させる ---
        float velocityModifier = Config.snipersMonocleVelocityModifier.get().floatValue();
        float damageModifier = Config.snipersMonocleDamageModifier.get().floatValue();

        float velocityMultiplier = 1.0f + velocityModifier + ((velocityModifier / 2) * (finalCount - 1));
        arrow.setDeltaMovement(arrow.getDeltaMovement().scale(velocityMultiplier));

        // --- 矢の基礎ダメージ(Base Damage)を増加させる ---
        double damageBonus = damageModifier + ((damageModifier / 2) * (finalCount - 1));
        arrow.setBaseDamage(arrow.getBaseDamage() * (1.0 + damageBonus));
    }

    /**
     * 任務2：継続的な近接攻撃力減少デバフを適用する
     */
    public static void updatePassiveDebuff(Player player, int monocleCount, int prideMultiplier) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) return;

        attackDamage.removeModifier(MELEE_DAMAGE_DEBUFF_ID);

        if (monocleCount > 0) {
            int finalCount = monocleCount * prideMultiplier;
            // 近接攻撃ダメージ: -20%
            float damageModifier = -(Config.snipersMonocleMeleeDamageModifier.get().floatValue());
            addModifier(attackDamage, MELEE_DAMAGE_DEBUFF_ID, finalCount * damageModifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}