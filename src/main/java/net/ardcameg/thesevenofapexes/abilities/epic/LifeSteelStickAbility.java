// net/ardcameg/thesevenofapexes/abilities/epic/LifeSteelStickAbility.java

package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class LifeSteelStickAbility {
    private LifeSteelStickAbility() {}

    private static final ResourceLocation HEALTH_DEBUFF_ID =
            ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "life_steel_stick_health_debuff");

    /**
     * 任務1：攻撃時に与えたダメージの一部を吸収する効果を適用する
     * @param player 攻撃したプレイヤー
     * @param damageDealt プレイヤーが与えた最終ダメージ
     * @param stickCount "生命吸収の杖"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void applyLifeSteal(Player player, float damageDealt, int stickCount, int prideMultiplier) {
        if (stickCount <= 0 || damageDealt <= 0) return;

        float stealRatio = 0.1f * stickCount * prideMultiplier;
        float stealAmount = damageDealt * stealRatio;

        player.heal(stealAmount);
    }

    /**
     * 任務2：継続的な最大HP減少効果を適用、または解除する (新仕様)
     * @param player プレイヤー
     * @param stickCount "生命吸収の杖"の所持数
     */
    public static void updatePassiveDebuff(Player player, int stickCount) {
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // まず、既存のデバフをクリアする
        healthAttribute.removeModifier(HEALTH_DEBUFF_ID);

        // アイテムを持っている場合のみ、新しいデバフを適用する
        if (stickCount > 0) {
            // 複数持っていても効果は重ならないように、固定で-25%を設定
            AttributeModifier healthDebuff = new AttributeModifier(
                    HEALTH_DEBUFF_ID,
                    -0.25, // -25% (ハート2.5個分)
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            healthAttribute.addPermanentModifier(healthDebuff);
        }
    }
}