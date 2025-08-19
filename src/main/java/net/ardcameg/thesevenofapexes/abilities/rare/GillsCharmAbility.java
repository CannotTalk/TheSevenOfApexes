package net.ardcameg.thesevenofapexes.abilities.rare;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class GillsCharmAbility {
    private GillsCharmAbility() {}

    private static final ResourceLocation MOVEMENT_SPEED_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "gills_charm_movement_debuff");
    private static final ResourceLocation MINING_SPEED_BUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "gills_charm_mining_buff");

    /**
     * "鰓のお守り"の継続効果を管理する
     */
    public static void updateEffect(Player player, int charmCount, int prideMultiplier) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance miningSpeed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (movementSpeed == null || miningSpeed == null) return;

        // --- まず、全ての効果をリセット ---
        player.removeEffect(MobEffects.WATER_BREATHING);
        movementSpeed.removeModifier(MOVEMENT_SPEED_DEBUFF_ID);
        miningSpeed.removeModifier(MINING_SPEED_BUFF_ID);

        if (charmCount <= 0) return;

        int finalCount = charmCount * prideMultiplier;

        if (player.isUnderWater()) {
            // --- 水中でのバフ ---
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, finalCount - 1, true, false, false));

            // バニラの水中での採掘速度は1/5になるので、それを5倍して元に戻す
            addModifier(miningSpeed, MINING_SPEED_BUFF_ID, 4.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        } else {
            // --- 地上でのデバフ ---
            addModifier(movementSpeed, MOVEMENT_SPEED_DEBUFF_ID, finalCount * -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        // 念のため、古いものを削除してから追加する
        attribute.removeModifier(id);
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}