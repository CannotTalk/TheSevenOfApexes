package net.ardcameg.thesevenofapexes.abilities.rare;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class BlademastersProwessAbility {
    private BlademastersProwessAbility() {}

    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "blademasters_prowess_attack_damage");
    private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "blademasters_prowess_attack_speed");

    /**
     * 任務1：プレイヤーが放った矢のダメージを減少させる
     */
    public static void onArrowFired(EntityJoinLevelEvent event, int prowessCount, int prideMultiplier) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;

        int finalCount = prowessCount * prideMultiplier;

        // 遠距離攻撃ダメージ: -20%
        double currentDamage = arrow.getBaseDamage();
        double newDamage = currentDamage * (1.0 - (0.20 * finalCount));
        arrow.setBaseDamage(newDamage);
    }

    /**
     * 任務2：継続的な近接戦闘バフを適用する
     */
    public static void updatePassiveBuffs(Player player, int prowessCount, int prideMultiplier) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackDamage == null || attackSpeed == null) return;

        // まず既存の効果をクリア
        attackDamage.removeModifier(ATTACK_DAMAGE_ID);
        attackSpeed.removeModifier(ATTACK_SPEED_ID);

        if (prowessCount > 0) {
            int finalCount = prowessCount * prideMultiplier;
            // 近接攻撃ダメージ: +30%
            addModifier(attackDamage, ATTACK_DAMAGE_ID, finalCount * 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            // 攻撃速度: +15%
            addModifier(attackSpeed, ATTACK_SPEED_ID, finalCount * 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}