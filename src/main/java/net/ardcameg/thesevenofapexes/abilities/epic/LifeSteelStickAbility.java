package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public final class LifeSteelStickAbility {
    private LifeSteelStickAbility() {}

    private static final ResourceLocation HEALTH_DEBUFF_ID =
            ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "life_steel_stick_health_debuff");


    /**
     * 任務1：攻撃時にライフスティール効果を適用する
     * @param player 攻撃したプレイヤー
     * @param target 攻撃された対象
     * @param stickCount "生命吸収の杖"の所持数
     * @param prideMultiplier "傲慢"の倍率
     */
    public static void applyLifeSteal(Player player, LivingEntity target, int stickCount, int prideMultiplier) {
        if (stickCount <= 0) return;

        // 1本あたり体力2を吸収。傲慢で強化される
        float stealAmount = 2.0f * stickCount * prideMultiplier;

        // プレイヤーの体力を回復する。最大体力は超えない
        player.heal(stealAmount);

        // TODO: ここにライフスティール専用のパーティクルやサウンドを追加するとさらに良くなる
        ServerLevel level = (ServerLevel) player.level();
    }

    /**
     * 任務2：継続的な最大HP減少効果を適用、または解除する
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
            // 1本あたり-10%。複数持っていても効果は重ならない、という仕様を実装
            // そのため、stickCountの値に関わらず、固定で-0.1 (10%) を設定
            AttributeModifier healthDebuff = new AttributeModifier(
                    HEALTH_DEBUFF_ID,
                    -0.1, // -10%
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            healthAttribute.addPermanentModifier(healthDebuff);
        }
    }
}