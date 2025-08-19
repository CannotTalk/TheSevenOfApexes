package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes; // ★ import
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.resources.ResourceLocation; // ★ import
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance; // ★ import
import net.minecraft.world.entity.ai.attributes.AttributeModifier; // ★ import
import net.minecraft.world.entity.ai.attributes.Attributes; // ★ import
import net.minecraft.world.entity.player.Player;


public final class PhoenixFeatherAbility {
    private PhoenixFeatherAbility() {}

    // ★★★ 新しいAttributeModifierのためのIDを追加 ★★★
    private static final ResourceLocation HEALTH_DEBUFF_ID =
            ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "phoenix_feather_health_debuff");

    /**
     * "不死鳥の羽"の蘇生効果を適用する
     * @param player 死亡しかけているプレイヤー
     */
    public static boolean attemptRevive(Player player) {
        if (!BuffItemUtils.consumeItemFromBuffRow(player, ModItems.EPIC_PHOENIX_FEATHER.get())) {
            return false;
        }

        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);

        // 回復と同時に、全てのデバフを浄化する
        BuffItemUtils.clearAllDebuffs(player);

        // --- 3. デバフ効果 (AttributeModifier方式) ---
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            // 現在の最大体力の半分を、マイナスの値として計算
            double healthReduction = healthAttribute.getValue() * -0.5;

            AttributeModifier healthDebuff = new AttributeModifier(
                    HEALTH_DEBUFF_ID,
                    healthReduction,
                    AttributeModifier.Operation.ADD_VALUE
            );
            // 既存のものを削除してから追加する (安全のため)
            healthAttribute.removeModifier(HEALTH_DEBUFF_ID);
            healthAttribute.addTransientModifier(healthDebuff);
            player.getPersistentData().putInt("PhoenixFeatherDebuffTicks", 600);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 4, false, true,true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 4, false, true,true));
        }else {
            return false;
        }

        BuffItemUtils.playTotemAnimation(player, ModItems.EPIC_PHOENIX_FEATHER.get());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        return true;
    }
}