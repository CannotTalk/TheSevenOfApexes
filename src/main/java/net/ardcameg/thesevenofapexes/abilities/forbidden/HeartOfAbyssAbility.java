package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class HeartOfAbyssAbility {
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "heart_of_the_abyss_speed");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "heart_of_the_abyss_attack_speed");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "heart_of_the_abyss_attack_damage");

    private static final int firstRequired = Config.heartOfAbyssFirstRequire.get();
    private static final int secondRequired = Config.heartOfAbyssSecondRequire.get();
    private static final int thirdRequired = Config.heartOfAbyssThirdRequire.get();

    public static void update(ServerPlayer player) {
        clear(player);

        int abyssCount = BuffItemUtils.countAllItemsForPlayer(player, ModItems.FORBIDDEN_HEART_OF_THE_ABYSS.get());
        if (abyssCount <= 0) return;

        int otherForbiddenCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ForbiddenItem
                    && !stack.is(ModItems.FORBIDDEN_HEART_OF_THE_ABYSS.get())
                    && !stack.is(ModItems.FORBIDDEN_REVERSAL_ARTIFACT.get())) {
                otherForbiddenCount++;
            }
        }

        // 数に応じたバフを適用
        applyModifiers(player, otherForbiddenCount);
    }

    private static void applyModifiers(ServerPlayer player, int count) {
        // 一旦全てのModifierをクリア
        clear(player);

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance attackDamageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);

        if (count <= 0){
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, false));
        }
        if (speedAttr != null && count >= firstRequired) {
            AttributeModifier modifier = new AttributeModifier(SPEED_MODIFIER_ID, 0.01 * (count - (firstRequired - 1)), AttributeModifier.Operation.ADD_VALUE);
            speedAttr.addTransientModifier(modifier);
        }
        if (attackSpeedAttr != null && count >= secondRequired) {
            AttributeModifier modifier = new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, 0.1 * (count - (secondRequired - 1)), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            attackSpeedAttr.addTransientModifier(modifier);
        }
        if (attackDamageAttr != null && count >= thirdRequired) {
            AttributeModifier modifier = new AttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, 0.25 * (count - (thirdRequired - 1)), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            attackDamageAttr.addTransientModifier(modifier);
        }
        // 他のバフ（ダメージ軽減、生命吸収など）は、新しいイベントハンドラが必要になるため、後で追加します
    }

    private static void clear(ServerPlayer player) {
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_MODIFIER_ID);

        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttr != null) attackSpeedAttr.removeModifier(ATTACK_SPEED_MODIFIER_ID);

        AttributeInstance attackDamageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) attackDamageAttr.removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
    }
}