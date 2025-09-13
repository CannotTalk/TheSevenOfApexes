package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class HeartOfGlassAbility {
    private HeartOfGlassAbility() {}

    private static final ResourceLocation HEALTH_OVERRIDE_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "heart_of_glass_override");

    public static void updateEffect(Player player, int itemCount, boolean reversed) {
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        healthAttribute.removeModifier(HEALTH_OVERRIDE_ID);

        if (itemCount > 0) {
            if(!reversed) {
                // 現在の最大HPを取得し、それを打ち消す負の値を計算
                double currentMaxHealth = healthAttribute.getValue();
                // HPを1にするための減算値
                double healthToSet = 1.0 - currentMaxHealth;

                AttributeModifier healthOverride = new AttributeModifier(
                        HEALTH_OVERRIDE_ID,
                        healthToSet,
                        AttributeModifier.Operation.ADD_VALUE
                );
                healthAttribute.addPermanentModifier(healthOverride);
            }else {
                AttributeModifier healthOverride = new AttributeModifier(
                        HEALTH_OVERRIDE_ID,
                        1.0,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                );
                healthAttribute.addPermanentModifier(healthOverride);
            }
        }
    }
}