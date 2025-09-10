package net.ardcameg.thesevenofapexes.abilities.common;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class FragileSoulAbility {
    private FragileSoulAbility() {}

    private static final ResourceLocation HEALTH_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "fragile_soul_health_debuff");

    /**
     * "脆弱な魂"の最大体力減少効果を適用、または解除する
     * @param player 対象のプレイヤー
     * @param soulCount "脆弱な魂"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void updateEffect(Player player, int soulCount, int prideMultiplier) {
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // 毎回、まず既存の効果をクリアする
        healthAttribute.removeModifier(HEALTH_DEBUFF_ID);

        if (soulCount > 0) {
            // "傲慢"の効果で、デメリットも増幅される
            int finalCount = soulCount * prideMultiplier;
            // 1個あたりハート半分(-1)を減少させる
            double healthToDecrease = -1.0 * finalCount;

            AttributeModifier healthDebuff = new AttributeModifier(
                    HEALTH_DEBUFF_ID,
                    healthToDecrease,
                    AttributeModifier.Operation.ADD_VALUE
            );
            healthAttribute.addPermanentModifier(healthDebuff);
        }
    }
}