package net.ardcameg.thesevenofapexes.abilities.rare;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

public final class DeadeyeGlassAbility {
    private DeadeyeGlassAbility() {}

    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "deadeye_glass_attack_damage");

    /**
     * 任務1：攻撃がクリティカルかどうかで、ダメージを操作する
     * @param event クリティカルヒットイベント
     * @param glassCount "必中の眼鏡"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void onCriticalHit(CriticalHitEvent event, int glassCount, int prideMultiplier) {
        if (glassCount <= 0) return;

        if (event.isVanillaCritical()) {
            int finalCount = glassCount * prideMultiplier;
            float newMultiplier = event.getDamageMultiplier() + (0.5f * finalCount);
            event.setDamageMultiplier(newMultiplier);
        }
    }

    /**
     * 任務2：クリティカルでない攻撃のダメージを無効化する
     */
    public static void onNormalAttack(LivingDamageEvent.Pre event) {
        event.setNewDamage(0);
    }

    /**
     * 任務3：継続的な攻撃力バフを適用する（これはクリティカル計算の"基礎値"を上げるため）
     */
    public static void updatePassiveBuffs(Player player, int glassCount, int prideMultiplier) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) return;

        attackDamage.removeModifier(ATTACK_DAMAGE_ID);

        if (glassCount > 0) {
            int finalCount = glassCount * prideMultiplier;
            float attackPowerModifier = Config.deadeyeGlassAttackPowerModifier.get().floatValue();
            // 攻撃力:
            addModifier(attackDamage, ATTACK_DAMAGE_ID, finalCount * attackPowerModifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}