package net.ardcameg.thesevenofapexes.abilities.rare;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class GuardiansCrestAbility {
    private GuardiansCrestAbility() {}

    private static final ResourceLocation ARMOR_TOUGHNESS_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "guardians_crest_armor_toughness");

    /**
     * 任務1：継続的なバフを適用する
     */
    public static void updateEffect(Player player, int crestCount, int prideMultiplier) {
        AttributeInstance armorToughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness == null) return;

        armorToughness.removeModifier(ARMOR_TOUGHNESS_ID);

        if (crestCount > 0) {
            int finalCount = crestCount * prideMultiplier;
            addModifier(armorToughness, ARMOR_TOUGHNESS_ID, finalCount * 2.0, AttributeModifier.Operation.ADD_VALUE);
        }
    }

    // ★★★ onExhaustionメソッドは不要になったので、完全に削除します ★★★

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}